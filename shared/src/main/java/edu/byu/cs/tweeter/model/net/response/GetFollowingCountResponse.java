package edu.byu.cs.tweeter.model.net.response;

public class GetFollowingCountResponse extends GetCountResponse {

    public GetFollowingCountResponse(int numFollowing) {
        super(numFollowing);
    }

    public GetFollowingCountResponse(String message) {
        super(message);
    }
}
