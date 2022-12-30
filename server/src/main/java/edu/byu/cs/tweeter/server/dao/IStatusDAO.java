package edu.byu.cs.tweeter.server.dao;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.GetStatusRequest;
import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Feeds;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Statuses;

public interface IStatusDAO {

    List<Feeds> getFeed(int pageSize, String lastStatusDateTime, String user_alias);

    List<Statuses> getStory(int pageSize, String lastStatusDateTime, String user_alias);

    void putStatusInStory(Status post);

//    void putStatusInFeed(Status status, User user);

    void batchWriteToFeed(List<Feeds> feedBeans);

}
