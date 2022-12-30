package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;

import java.io.IOException;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.GetStatusRequest;
import edu.byu.cs.tweeter.model.net.response.GetStatusResponse;

/**
 * Background task that retrieves a page of statuses from a user's story.
 */
public class GetStoryTask extends PagedStatusTask {

    public GetStoryTask(AuthToken authToken, User targetUser, int limit, Status lastStatus,
                        Handler messageHandler) {
        super(authToken, targetUser, limit, lastStatus, messageHandler);
    }

    @Override
    protected final void runTask() throws IOException {

        String targetUserAlias = getTargetUser() == null ? null : getTargetUser().getAlias();
        Status lastStatus = getLastItem() == null ? null : getLastItem();

        GetStatusRequest getStoryRequest = new GetStatusRequest(getAuthToken(), targetUserAlias, getLimit(), lastStatus);
        GetStatusResponse getStoryResponse = null;
        try {
            getStoryResponse = new ServerFacade().getStory(getStoryRequest, "/getstory");
        } catch (Exception e) {
            e.printStackTrace();
            sendExceptionMessage(e);
        }

        if (getStoryResponse.isSuccess()) {
            // Call sendSuccessMessage if successful
            items = getStoryResponse.getStatuses();
            hasMorePages = getStoryResponse.getHasMorePages();
            sendSuccessMessage();
        } else {
            // or call sendFailedMessage if not successful
            sendFailedMessage(getStoryResponse.getMessage());
        }
    }

//    @Override
//    protected GetStoryResponse getItems() {
////        return getFakeData().getPageOfStatus(getLastItem(), getLimit());
//        String targetUserAlias = getTargetUser() == null ? null : getTargetUser().getAlias();
//        Status lastStatus = getLastItem() == null ? null : getLastItem();
//
//        GetStoryRequest getStoryRequest = new GetStoryRequest(getAuthToken(), targetUserAlias, getLimit(), lastStatus);
//        GetStoryResponse getStoryResponse = null;
//        try {
//            getStoryResponse = new ServerFacade().getStory(getStoryRequest, "/getstory");
//        } catch (Exception e) {
//            e.printStackTrace();
//            sendExceptionMessage(e);
//        }
////        return new Pair<>(getStoryResponse.getStatuses(), getStoryResponse.getHasMorePages());
//        return getStoryResponse;
//    }
}
