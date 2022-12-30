package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.GetFollowersCountRequest;
import edu.byu.cs.tweeter.model.net.response.GetCountResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowersCountResponse;

/**
 * Background task that queries how many followers a user has.
 */
public class GetFollowersCountTask extends GetCountTask {

    public GetFollowersCountTask(AuthToken authToken, User targetUser, Handler messageHandler) {
        super(authToken, targetUser, messageHandler);
    }

    @Override
    protected GetCountResponse runCountTask() {
        GetFollowersCountRequest getFollowersCountRequest = new GetFollowersCountRequest(getAuthToken(),getTargetUser());
        GetFollowersCountResponse getFollowersCountResponse = null;
        try {
            getFollowersCountResponse = new ServerFacade().getFollowersCount(getFollowersCountRequest,
                    "/getfollowerscount");
        } catch (Exception e) {
            e.printStackTrace();
            sendExceptionMessage(e);
        }
        return getFollowersCountResponse;
    }
}
