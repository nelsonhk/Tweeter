package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Bundle;
import android.os.Handler;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.IsFollowerRequest;
import edu.byu.cs.tweeter.model.net.response.IsFollowerResponse;

/**
 * Background task that determines if one user is following another.
 */
public class IsFollowerTask extends AuthenticatedTask {

    public static final String IS_FOLLOWER_KEY = "is-follower";

    /**
     * The alleged follower.
     */
    private final User follower;

    /**
     * The alleged followee.
     */
    private final User followee;

    private boolean isFollower = false;

    public IsFollowerTask(AuthToken authToken, User follower, User followee, Handler messageHandler) {
        super(authToken, messageHandler);
        this.follower = follower;
        this.followee = followee;
    }

    @Override
    protected void runTask() {
//        isFollower = new Random().nextInt() > 0;
        IsFollowerRequest isFollowerRequest = new IsFollowerRequest(getAuthToken(), follower,followee);
        IsFollowerResponse isFollowerResponse;
        try {
            isFollowerResponse = new ServerFacade().isFollower(isFollowerRequest, "/isfollower");
            if (isFollowerResponse.isSuccess()) {
                // Call sendSuccessMessage if successful
                isFollower = isFollowerResponse.isFollower();
                sendSuccessMessage();
            } else {
                // or call sendFailedMessage if not successful
                sendFailedMessage(isFollowerResponse.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendExceptionMessage(e);
        }
    }

    @Override
    protected void loadSuccessBundle(Bundle msgBundle) {
        msgBundle.putBoolean(IS_FOLLOWER_KEY, isFollower);
    }
}
