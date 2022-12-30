package edu.byu.cs.tweeter.model.net.response;

public class GetFollowersCountResponse extends GetCountResponse {

    public GetFollowersCountResponse(int numFollowers) {
        super(numFollowers);
    }

    public GetFollowersCountResponse(String message) {
        super(message);
    }

}
