package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;

import java.io.IOException;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowingRequest;
import edu.byu.cs.tweeter.model.net.response.FollowingResponse;

/**
 * Background task that retrieves a page of other users being followed by a specified user.
 */
public class GetFollowingTask extends PagedUserTask {

    public GetFollowingTask(AuthToken authToken, User targetUser, int limit, User lastFollowee,
                            Handler messageHandler) {
        super(authToken, targetUser, limit, lastFollowee, messageHandler);
    }

    @Override
    protected final void runTask() throws IOException {
        String targetUserAlias = getTargetUser() == null ? null : getTargetUser().getAlias();
        String lastFolloweeAlias = getLastItem() == null ? null : getLastItem().getAlias();

        FollowingRequest followingRequest = new FollowingRequest(getAuthToken(),
                targetUserAlias, getLimit(), lastFolloweeAlias);
        FollowingResponse followingResponse = null;
        try {
            followingResponse = new ServerFacade().getFollowees(followingRequest, "/getfollowing");
        } catch (Exception e) {
            e.printStackTrace();
            sendExceptionMessage(e);
        }

        if (followingResponse.isSuccess()) {
            // Call sendSuccessMessage if successful
            items = followingResponse.getFollowees();
            hasMorePages = followingResponse.getHasMorePages();
            sendSuccessMessage();
        } else {
         // or call sendFailedMessage if not successful
             sendFailedMessage(followingResponse.getMessage());
        }
    }

//    @Override
//    protected FollowingResponse getItems() {
////        return getFakeData().getPageOfUsers(getLastItem(), getLimit(), getTargetUser());
//        String targetUserAlias = getTargetUser() == null ? null : getTargetUser().getAlias();
//        String lastFolloweeAlias = getLastItem() == null ? null : getLastItem().getAlias();
//
//        FollowingRequest followingRequest = new FollowingRequest(getAuthToken(),
//                targetUserAlias, getLimit(), lastFolloweeAlias);
//        FollowingResponse followingResponse = null;
//        try {
//            followingResponse = new ServerFacade().getFollowees(followingRequest, "/getfollowing");
//        } catch (Exception e) {
//            e.printStackTrace();
//            sendExceptionMessage(e);
//        }
////        return new Pair<>(followingResponse.getFollowees(), followingResponse.getHasMorePages());
//        return followingResponse;
//    }

}
