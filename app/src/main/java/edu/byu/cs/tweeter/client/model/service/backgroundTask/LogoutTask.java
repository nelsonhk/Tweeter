package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.net.request.LogoutRequest;
import edu.byu.cs.tweeter.model.net.response.LogoutResponse;

/**
 * Background task that logs out a user (i.e., ends a session).
 */
public class LogoutTask extends AuthenticatedTask {

    public LogoutTask(AuthToken authToken, Handler messageHandler) {
        super(authToken, messageHandler);
    }

    @Override
    protected void runTask() {
        // We could do this from the presenter, without a task and handler, but we will
        // eventually remove the auth token from the DB and will need this then.

        LogoutRequest logoutRequest = new LogoutRequest(Cache.getInstance().getCurrUserAuthToken());
        LogoutResponse logoutResponse = null;
        try {
            logoutResponse = new ServerFacade().logout(logoutRequest, "/logout");
            if (logoutResponse.isSuccess()) {
                // Call sendSuccessMessage if successful
                sendSuccessMessage();
            } else {
                sendFailedMessage(logoutResponse.getMessage());
            }
        } catch (Exception e) {
            // or call sendFailedMessage if not successful
            sendExceptionMessage(e);
        }
    }
}
