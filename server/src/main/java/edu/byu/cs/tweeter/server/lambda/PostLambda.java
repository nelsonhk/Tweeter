package edu.byu.cs.tweeter.server.lambda;

import static edu.byu.cs.tweeter.server.net.JsonSerializer.deserialize;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Feeds;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Follows;
import edu.byu.cs.tweeter.server.dao.IStatusDAO;
import edu.byu.cs.tweeter.server.net.SQSMessageObject;

public class PostLambda extends Handler implements RequestHandler<SQSEvent, Void> {

    IStatusDAO mIStatusDAO = mDAOFactory.getStatusDAO();

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage msg : event.getRecords()) {
            SQSMessageObject sqsMessageObject = deserialize(msg.getBody(), SQSMessageObject.class);

            Status post = sqsMessageObject.getPost();
            List<String> allFollowers = sqsMessageObject.getFollowers();

            List<Feeds> batchToWrite = new ArrayList<>();
            for (String alias : allFollowers) {
                Feeds feedBean = new Feeds();
                feedBean.setAuthor_alias(post.getUser().getAlias());
                feedBean.setReceiver_alias(alias);

                Long datetime = Long.parseLong(String.valueOf(new Date().getTime()));
                feedBean.setDatetime(datetime);

                feedBean.setPost_content(post.getPost());
                feedBean.setMentions(post.getMentions());
                feedBean.setUrls(post.getUrls());

                batchToWrite.add(feedBean);

                if (batchToWrite.size() == 25) {
                    // package this batch up and send to DynamoDB.
                    mIStatusDAO.batchWriteToFeed(batchToWrite);
                    batchToWrite = new ArrayList<>();
                }
            }

            // write any remaining
            if (batchToWrite.size() > 0) {
                // package this batch up and send to DynamoDB.
                mIStatusDAO.batchWriteToFeed(batchToWrite);
            }

        }
        return null;
    }
}
