package edu.byu.cs.tweeter.server.service;

import static edu.byu.cs.tweeter.server.net.JsonSerializer.serialize;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.GetStatusRequest;
import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.response.GetStatusResponse;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Feeds;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Statuses;
import edu.byu.cs.tweeter.server.dao.IDAOFactory;
import edu.byu.cs.tweeter.server.dao.IFollowDAO;
import edu.byu.cs.tweeter.server.dao.IStatusDAO;
import edu.byu.cs.tweeter.server.dao.IUserDAO;

public class StatusService {

    IStatusDAO mIStatusDAO;
    IFollowDAO mIFollowDAO;
    IUserDAO mIUserDAO;
    AuthService mAuthService = new AuthService();

    public StatusService(IDAOFactory daoFactory) {
        mIUserDAO = daoFactory.getUserDAO();
        mIStatusDAO = daoFactory.getStatusDAO();
        mIFollowDAO = daoFactory.getFollowDAO();
    }

    public GetStatusResponse getFeed(GetStatusRequest getFeedRequest) {
        if(getFeedRequest.getTargetUserAlias() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a target user alias");
        } else if(getFeedRequest.getLimit() <= 0) {
            throw new RuntimeException("[Bad Request] Request needs to have a positive limit");
        }

        String lastStatusDateTime = null;
        if (getFeedRequest.getLastStatus() != null) {
            lastStatusDateTime = getFeedRequest.getLastStatus().datetime;
        }

        List<Feeds> feeds = mIStatusDAO.getFeed(getFeedRequest.getLimit(),
                lastStatusDateTime, getFeedRequest.getTargetUserAlias());

        List<Status> allStatuses = new ArrayList<>(getFeedRequest.getLimit());

        for (Feeds post : feeds) {
            String status_username = post.getAuthor_alias();
            User user = mIUserDAO.getUser(status_username);
            Status status = new Status(post.getPost_content(), user, new Date(post.getDatetime()).toString(),
                    post.getUrls(), post.getMentions());
            allStatuses.add(status);
        }

        List<Status> responseStatuses = new ArrayList<>(getFeedRequest.getLimit());

        boolean hasMorePages = false;

        if(getFeedRequest.getLimit() > 0) {
            if (allStatuses != null) {
                int statusIndex = getStatusesStartingIndex(getFeedRequest.getLastStatus(), allStatuses);

                for(int limitCounter = 0; statusIndex < allStatuses.size() && limitCounter < getFeedRequest.getLimit(); statusIndex++, limitCounter++) {
                    responseStatuses.add(allStatuses.get(statusIndex));
                }

                hasMorePages = statusIndex < allStatuses.size();
            }
        }

        return new GetStatusResponse(responseStatuses, hasMorePages);
    }

    public GetStatusResponse getStory(GetStatusRequest getStoryRequest) {
        if(getStoryRequest.getTargetUserAlias() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a target user alias");
        } else if(getStoryRequest.getLimit() <= 0) {
            throw new RuntimeException("[Bad Request] Request needs to have a positive limit");
        }

        String lastStatusDateTime = null;
        if (getStoryRequest.getLastStatus() != null) {
            lastStatusDateTime = getStoryRequest.getLastStatus().datetime;
        }

        List<Statuses> statuses = mIStatusDAO.getStory(getStoryRequest.getLimit(), lastStatusDateTime,
                getStoryRequest.getTargetUserAlias());

        List<Status> allStatuses = new ArrayList<>(getStoryRequest.getLimit());

        for (Statuses post : statuses) {
            String status_username = post.getUser_alias();
            // get user for specified user_alias
            User user = mIUserDAO.getUser(status_username);
            Status status = new Status(post.getPost_content(), user, new Date(post.getDatetime()).toString(),
                    post.getUrls(), post.getMentions());
            allStatuses.add(status);
        }

        List<Status> responseStatuses = new ArrayList<>(getStoryRequest.getLimit());

        boolean hasMorePages = false;

        if(getStoryRequest.getLimit() > 0) {
            if (allStatuses != null) {
                int statusIndex = getStatusesStartingIndex(getStoryRequest.getLastStatus(), allStatuses);

                for(int limitCounter = 0; statusIndex < allStatuses.size() && limitCounter < getStoryRequest.getLimit(); statusIndex++, limitCounter++) {
                    responseStatuses.add(allStatuses.get(statusIndex));
                }

                hasMorePages = statusIndex < allStatuses.size();
            }
        }

        return new GetStatusResponse(responseStatuses, hasMorePages);
    }

    public PostStatusResponse postStatus(PostStatusRequest postStatusRequest) {
        if (postStatusRequest.getStatus() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a status to post");
        } else if (postStatusRequest.getAuthToken() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have an authtoken");
        }

//        if (!mAuthService.isValidToken(postStatusRequest.getAuthToken())) {
//            return new PostStatusResponse("[Bad Request] Cannot post with expired authtoken");
//        } else {

            try {

                mIStatusDAO.putStatusInStory(postStatusRequest.getStatus());
                pushToQueue(postStatusRequest.getStatus(),
                        "https://sqs.us-west-2.amazonaws.com/606855416153/Tweeter-Queue1");

//                mIStatusDAO.putStatusInFeed(postStatusRequest.getStatus(), new User());
                return new PostStatusResponse();
            } catch (Exception e) {
                return new PostStatusResponse(e.getMessage());
            }

//        }
    }

    public void pushToQueue(Object object, String url) {
        String queueUrl = url;

        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(serialize(object));

        AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        SendMessageResult send_msg_result = sqs.sendMessage(send_msg_request);

        String msgId = send_msg_result.getMessageId();
        System.out.println("Message ID: " + msgId);
    }

    private int getStatusesStartingIndex(Status lastStatus, List<Status> allStatuses) {

        int statusIndex = 0;

        if(lastStatus != null) {
            // This is a paged request for something after the first page. Find the first item
            // we should return
            for (int i = 0; i < allStatuses.size(); i++) {
                if(lastStatus.equals(allStatuses.get(i))) {
                    // We found the index of the last item returned last time. Increment to get
                    // to the first one we should return
                    statusIndex = i + 1;
                    break;
                }
            }
        }

        return statusIndex;
    }

}
