package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.UnfollowRequest;
import edu.byu.cs.tweeter.model.net.response.UnfollowResponse;

/**
 * Background task that removes a following relationship between two users.
 */
public class UnfollowTask extends AuthenticatedTask {

    /**
     * The user that is being followed.
     */
    private final User followee;

    public UnfollowTask(AuthToken authToken, User followee, Handler messageHandler) {
        super(authToken, messageHandler);
        this.followee = followee;
    }

    @Override
    protected void runTask() {
        // We could do this from the presenter, without a task and handler, but we will
        // eventually access the database from here when we aren't using dummy data.
        UnfollowRequest unfollowRequest = new UnfollowRequest(getAuthToken(), Cache.getInstance().getCurrUser(), followee);
        UnfollowResponse unfollowResponse;
        try {
            unfollowResponse = new ServerFacade().unfollow(unfollowRequest, "/unfollow");
            if (unfollowResponse.isSuccess()) {
                // Call sendSuccessMessage if successful
                sendSuccessMessage();
            } else {
                sendFailedMessage(unfollowResponse.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // or call sendFailedMessage if not successful
            sendExceptionMessage(e);
        }
    }


}
