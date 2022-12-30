package edu.byu.cs.tweeter.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowingCountRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowingCountResponse;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;

/**
 * Register
 * GetFollowers
 * GetFollowingCount and/or GetFollowersCount
 */

public class ServerFacadeTest {

    ServerFacade serverFacade;

    @BeforeEach
    public void setup() {
        serverFacade = new ServerFacade();
    }

    @Test
    public void registerSuccess() {
        RegisterRequest registerRequest = new RegisterRequest("", "",
                "", "", "");
        try {
            RegisterResponse registerResponse = serverFacade.register(registerRequest, "/register");
            Assertions.assertNotNull(registerResponse.getAuthToken());
            Assertions.assertEquals("Allen", registerResponse.getUser().getFirstName());
            Assertions.assertEquals("Anderson", registerResponse.getUser().getLastName());
            Assertions.assertEquals("@allen", registerResponse.getUser().getAlias());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void registerFail() {
        RegisterRequest registerRequest = new RegisterRequest(null, "",
                "", "", "");
        try {
            RegisterResponse registerResponse = serverFacade.register(registerRequest, "/register");
            Assertions.assertNull(registerResponse.getAuthToken());
            Assertions.assertFalse(registerResponse.isSuccess());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getFollowersSuccess() {
        FollowersRequest followersRequest = new FollowersRequest(new AuthToken(), "", 10, "");
        try {
            FollowersResponse followersResponse = serverFacade.getFollowers(followersRequest, "getfollowers");
            Assertions.assertTrue(followersResponse.isSuccess());
            Assertions.assertNotNull(followersResponse.getFollowers());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getFollowersFail() {
        FollowersRequest followersRequest = new FollowersRequest(null, "", 10, "");
        try {
            FollowersResponse followersResponse = serverFacade.getFollowers(followersRequest, "getfollowers");
            Assertions.assertTrue(followersResponse.isSuccess());
            Assertions.assertNotNull(followersResponse.getFollowers());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getFollowingCountSuccess() {
        GetFollowingCountRequest getFollowingCountRequest = new GetFollowingCountRequest(new AuthToken(),
                new User("", "", ""));
        try {
            GetFollowingCountResponse getFollowingCountResponse =
                    serverFacade.getFollowingCount(getFollowingCountRequest, "getfollowingcount");
            Assertions.assertNotNull(getFollowingCountResponse.getNumPeople());
            Assertions.assertTrue(getFollowingCountResponse.isSuccess());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getFollowingCountFail() {
        GetFollowingCountRequest getFollowingCountRequest = new GetFollowingCountRequest(null,
                new User("", "", ""));
        try {
            GetFollowingCountResponse getFollowingCountResponse =
                    serverFacade.getFollowingCount(getFollowingCountRequest, "getfollowingcount");
            Assertions.assertEquals(0, getFollowingCountResponse.getNumPeople());
            Assertions.assertFalse(getFollowingCountResponse.isSuccess());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
