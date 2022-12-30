package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.net.request.LoginRequest;
import edu.byu.cs.tweeter.model.net.response.LoginResponse;

/**
 * Background task that logs in a user (i.e., starts a session).
 */
public class LoginTask extends AuthenticateTask {

    public LoginTask(String username, String password, Handler messageHandler) {
        super(messageHandler, username, password);
    }

    @Override
    protected LoginResponse runAuthenticationTask() {
//        User loggedInUser = getFakeData().getFirstUser();
//        AuthToken authToken = getFakeData().getAuthToken();
        LoginRequest loginRequest = new LoginRequest(username, password);
        LoginResponse loginResponse = null;
        try {
            loginResponse = new ServerFacade().login(loginRequest, "/login");
            Cache.getInstance().setCurrUserAuthToken(loginResponse.getAuthToken());
            Cache.getInstance().setCurrUser(loginResponse.getUser());
        } catch (Exception e) {
            e.printStackTrace();
            sendExceptionMessage(e);
        }
//        return new Pair<>(loggedInUser, authToken);
        return loginResponse;
    }
}
