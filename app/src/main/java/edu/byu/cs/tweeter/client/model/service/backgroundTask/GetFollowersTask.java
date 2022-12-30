package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;

import java.io.IOException;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;

/**
 * Background task that retrieves a page of followers.
 */
public class GetFollowersTask extends PagedUserTask {

    public GetFollowersTask(AuthToken authToken, User targetUser, int limit, User lastFollower,
                            Handler messageHandler) {
        super(authToken, targetUser, limit, lastFollower, messageHandler);
    }

    @Override
    protected final void runTask() throws IOException {
        String targetUserAlias = getTargetUser() == null ? null : getTargetUser().getAlias();
        String lastFollowerAlias = getLastItem() == null ? null : getLastItem().getAlias();

        FollowersRequest followersRequest = new FollowersRequest(getAuthToken(),
                targetUserAlias, getLimit(), lastFollowerAlias);
        FollowersResponse followersResponse = null;
        try {
            followersResponse = new ServerFacade().getFollowers(followersRequest, "/getfollowers");
        } catch (Exception e) {
            e.printStackTrace();
            sendExceptionMessage(e);
        }

        if (followersResponse.isSuccess()) {
            // Call sendSuccessMessage if successful
            items = followersResponse.getFollowers();
            hasMorePages = followersResponse.getHasMorePages();
            sendSuccessMessage();
        } else {
         // or call sendFailedMessage if not successful
             sendFailedMessage(followersResponse.getMessage());
        }
    }

//    @Override
//    protected FollowersResponse getItems() {
////        return getFakeData().getPageOfUsers(getLastItem(), getLimit(), getTargetUser());
//        String targetUserAlias = getTargetUser() == null ? null : getTargetUser().getAlias();
//        String lastFollowerAlias = getLastItem() == null ? null : getLastItem().getAlias();
//
//        FollowersRequest followersRequest = new FollowersRequest(getAuthToken(),
//                targetUserAlias, getLimit(), lastFollowerAlias);
//        FollowersResponse followersResponse = null;
//        try {
//            followersResponse = new ServerFacade().getFollowers(followersRequest, "/getfollowers");
//        } catch (Exception e) {
//            e.printStackTrace();
//            sendExceptionMessage(e);
//        }
////        return new Pair<>(followersResponse.getFollowers(), followersResponse.getHasMorePages());
//        return followersResponse;
//    }

}
