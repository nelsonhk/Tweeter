package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowRequest;
import edu.byu.cs.tweeter.model.net.response.FollowResponse;

/**
 * Background task that establishes a following relationship between two users.
 */
public class FollowTask extends AuthenticatedTask {
    /**
     * The user that is being followed.
     */
    private final User followee;

    public FollowTask(AuthToken authToken, User followee, Handler messageHandler) {
        super(authToken, messageHandler);
        this.followee = followee;
    }

    @Override
    protected void runTask() {
        // We could do this from the presenter, without a task and handler, but we will
        // eventually access the database from here when we aren't using dummy data.
        FollowRequest followRequest = new FollowRequest(getAuthToken(), Cache.getInstance().getCurrUser(), followee);
        FollowResponse followResponse;
        try {
            followResponse = new ServerFacade().follow(followRequest, "/follow");
            if (followResponse.isSuccess()) {
                // Call sendSuccessMessage if successful
                sendSuccessMessage();
            } else {
                sendFailedMessage(followResponse.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // or call sendFailedMessage if not successful
            sendExceptionMessage(e);
        }
    }

}
