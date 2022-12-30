package edu.byu.cs.tweeter.server.dao;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingRequest;
import edu.byu.cs.tweeter.model.net.request.IsFollowerRequest;
import edu.byu.cs.tweeter.model.net.request.UnfollowRequest;
import edu.byu.cs.tweeter.model.net.response.GetFollowersCountResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowingCountResponse;
import edu.byu.cs.tweeter.model.net.response.IsFollowerResponse;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Follows;
import edu.byu.cs.tweeter.util.Pair;

public interface IFollowDAO {

    List<Follows> getFollowees(String follower_handle, int pageSize, String lastFolloweeAlias);

    List<Follows> getFollowers(String followee_handle, int pageSize, String lastFollower);

    List<Follows> getAllFollowers(String followee_handle);

    void follow(User followee, User follower);

    void unfollow(String follower_handle, String followee_handle);

    boolean isFollower(String followee_handle, String follower_handle);

    // for populating the db with 10,000 fake following
    void addFollowersBatch(List<String> followers, String followee_alias);

    void batchWriteFollowers(List<Follows> follows);

}
