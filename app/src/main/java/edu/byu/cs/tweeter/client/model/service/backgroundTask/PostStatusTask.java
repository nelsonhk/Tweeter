package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;

/**
 * Background task that posts a new status sent by a user.
 */
public class PostStatusTask extends AuthenticatedTask {

    /**
     * The new status being sent. Contains all properties of the status,
     * including the identity of the user sending the status.
     */
    private final Status status;

    public PostStatusTask(AuthToken authToken, Status status, Handler messageHandler) {
        super(authToken, messageHandler);
        this.status = status;
    }

    @Override
    protected void runTask() {
        // We could do this from the presenter, without a task and handler, but we will
        // eventually access the database from here when we aren't using dummy data.
        PostStatusRequest postStatusRequest = new PostStatusRequest(getAuthToken(), status);
        PostStatusResponse postStatusResponse;
        try {
            postStatusResponse = new ServerFacade().postStatus(postStatusRequest, "/poststatus");
            if (postStatusResponse.isSuccess()) {
                // Call sendSuccessMessage if successful
                sendSuccessMessage();
            } else {
                // or call sendFailedMessage if not successful
                 sendFailedMessage(postStatusResponse.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendExceptionMessage(e);
        }
    }

}
