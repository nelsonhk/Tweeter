package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Bundle;
import android.os.Handler;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.GetUserRequest;
import edu.byu.cs.tweeter.model.net.response.GetUserResponse;

/**
 * Background task that returns the profile for a specified user.
 */
public class GetUserTask extends AuthenticatedTask {

    public static final String USER_KEY = "user";

    /**
     * Alias (or handle) for user whose profile is being retrieved.
     */
    private final String alias;

    private User user;

    public GetUserTask(AuthToken authToken, String alias, Handler messageHandler) {
        super(authToken, messageHandler);
        this.alias = alias;
    }

    @Override
    protected void runTask() {
//        user = getUser();
        GetUserRequest getUserRequest = new GetUserRequest(getAuthToken(), alias);
        GetUserResponse getUserResponse;
        try {
            getUserResponse = new ServerFacade().getUser(getUserRequest, "/getuser");
            if (getUserResponse.isSuccess()) {
                // Call sendSuccessMessage if successful
                user = getUserResponse.getUser();
                sendSuccessMessage();
            } else {
                // or call sendFailedMessage if not successful
                 sendFailedMessage(getUserResponse.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendExceptionMessage(e);
        }
    }

    @Override
    protected void loadSuccessBundle(Bundle msgBundle) {
        msgBundle.putSerializable(USER_KEY, user);
    }

    private User getUser() {
        return getFakeData().findUserByAlias(alias);
    }
}
