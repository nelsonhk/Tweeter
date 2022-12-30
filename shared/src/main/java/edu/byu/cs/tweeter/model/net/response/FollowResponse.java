package edu.byu.cs.tweeter.model.net.response;

public class FollowResponse extends Response {

    /**
     * For successful follow operation; sends true for success
     * @param
     */
    public FollowResponse() {
        super(true);
    }

    /**
     * For unsuccessful follow operation; sends false for success and a message
     * @param message
     */
    public FollowResponse(String message) {
        super(false, message);
    }

}
