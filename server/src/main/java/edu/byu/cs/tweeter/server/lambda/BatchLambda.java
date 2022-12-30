package edu.byu.cs.tweeter.server.lambda;

import static edu.byu.cs.tweeter.server.net.JsonSerializer.deserialize;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Follows;
import edu.byu.cs.tweeter.server.dao.IDAOFactory;
import edu.byu.cs.tweeter.server.dao.IFollowDAO;
import edu.byu.cs.tweeter.server.dao.IStatusDAO;
import edu.byu.cs.tweeter.server.dao.IUserDAO;
import edu.byu.cs.tweeter.server.net.SQSMessageObject;
import edu.byu.cs.tweeter.server.service.AuthService;
import edu.byu.cs.tweeter.server.service.StatusService;

public class BatchLambda extends Handler implements RequestHandler<SQSEvent, Void> {

    private IFollowDAO mIFollowDAO = mDAOFactory.getFollowDAO();

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage msg : event.getRecords()) {
            Status post = deserialize(msg.getBody(), Status.class);
            List<Follows> followsList = mIFollowDAO.getAllFollowers(post.getUser().getAlias());
            List<String> allFollowers = new ArrayList<>();

            for (Follows follower : followsList) {
                allFollowers.add(follower.getFollower_handle());
            }

            for (int i = 0; i < allFollowers.size(); i+=25) {
                int batchSize = 25;
                if (allFollowers.size() - i < 25) {
                    batchSize = allFollowers.size() - i;
                }
                List<String> sublist = allFollowers.subList(i, i + batchSize);
                SQSMessageObject messageObject = new SQSMessageObject(post, sublist);
                new StatusService(mDAOFactory).pushToQueue(messageObject,
                        "https://sqs.us-west-2.amazonaws.com/606855416153/Tweeter-Queue2");
            }

        }
        return null;
    }

}
