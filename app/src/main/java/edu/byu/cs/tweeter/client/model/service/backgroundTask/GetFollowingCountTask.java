package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.GetFollowingCountRequest;
import edu.byu.cs.tweeter.model.net.response.GetFollowingCountResponse;

/**
 * Background task that queries how many other users a specified user is following.
 */
public class GetFollowingCountTask extends GetCountTask {

    public GetFollowingCountTask(AuthToken authToken, User targetUser, Handler messageHandler) {
        super(authToken, targetUser, messageHandler);
    }

    @Override
    protected GetFollowingCountResponse runCountTask() {
        GetFollowingCountRequest getFollowingCountRequest = new GetFollowingCountRequest(getAuthToken(), getTargetUser());
        GetFollowingCountResponse getFollowingCountResponse = null;
        try {
            getFollowingCountResponse = new ServerFacade().getFollowingCount(getFollowingCountRequest,
                    "/getfollowingcount");
        } catch (Exception e) {
            e.printStackTrace();
            sendExceptionMessage(e);
        }
        return getFollowingCountResponse;
    }
}
