package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;

import java.io.IOException;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.GetStatusRequest;
import edu.byu.cs.tweeter.model.net.response.GetStatusResponse;

/**
 * Background task that retrieves a page of statuses from a user's feed.
 */
public class GetFeedTask extends PagedStatusTask {

    public GetFeedTask(AuthToken authToken, User targetUser, int limit, Status lastStatus,
                       Handler messageHandler) {
        super(authToken, targetUser, limit, lastStatus, messageHandler);
    }

    @Override
    protected final void runTask() throws IOException {

        String targetUserAlias = getTargetUser() == null ? null : getTargetUser().getAlias();
        Status lastStatus = getLastItem() == null ? null : getLastItem();

        GetStatusRequest getFeedRequest = new GetStatusRequest(getAuthToken(), targetUserAlias, getLimit(), lastStatus);
        GetStatusResponse getFeedResponse = null;
        try {
            getFeedResponse = new ServerFacade().getFeed(getFeedRequest, "/getfeed");
        } catch (Exception e) {
            e.printStackTrace();
            sendExceptionMessage(e);
        }

        if (getFeedResponse.isSuccess()) {
            // Call sendSuccessMessage if successful
            items = getFeedResponse.getStatuses();
            hasMorePages = getFeedResponse.getHasMorePages();
            sendSuccessMessage();
        } else {
            // or call sendFailedMessage if not successful
            sendFailedMessage(getFeedResponse.getMessage());
        }
    }

//    @Override
//    protected GetStatusResponse getItems() {
////        return getFakeData().getPageOfStatus(getLastItem(), getLimit());
//
//        String targetUserAlias = getTargetUser() == null ? null : getTargetUser().getAlias();
//        Status lastStatus = getLastItem() == null ? null : getLastItem();
//
//        GetFeedRequest getFeedRequest = new GetFeedRequest(getAuthToken(), targetUserAlias, getLimit(), lastStatus);
//        GetStatusResponse getFeedResponse = null;
//        try {
//            getFeedResponse = new ServerFacade().getFeed(getFeedRequest, "/getfeed");
//        } catch (Exception e) {
//            e.printStackTrace();
//            sendExceptionMessage(e);
//        }
////        return new Pair<>(getFeedResponse.getStatuses(), getFeedResponse.getHasMorePages());
//        return getFeedResponse;
//    }
}
