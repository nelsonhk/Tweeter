package edu.byu.cs.tweeter.server.dao;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingRequest;
import edu.byu.cs.tweeter.model.net.request.GetStatusRequest;
import edu.byu.cs.tweeter.model.net.request.GetUserRequest;
import edu.byu.cs.tweeter.model.net.request.LoginRequest;
import edu.byu.cs.tweeter.model.net.request.LogoutRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.model.net.response.FollowingResponse;
import edu.byu.cs.tweeter.model.net.response.GetStatusResponse;
import edu.byu.cs.tweeter.model.net.response.GetUserResponse;
import edu.byu.cs.tweeter.model.net.response.LoginResponse;
import edu.byu.cs.tweeter.model.net.response.LogoutResponse;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Feeds;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Follows;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Statuses;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Users;
import edu.byu.cs.tweeter.util.Pair;

public interface IUserDAO {

    Pair<User, AuthToken> login(String username, String password);

    Pair<User, AuthToken> register(User user, String password);

    void logout(LogoutRequest logoutRequest);

    User getUser(String username);

    boolean isAuthTokenInTable(String token);

    int getFollowingCount(String username);

    int getFollowersCount(String username);

    void updateCounts(String follower_handle, String followee_handle, boolean follow);

    // for populating the users table with 10,000 fake users
    void batchWriteUsers(List<Users> users);

    void addUserBatch(List<Users> users);
}
