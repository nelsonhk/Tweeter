package edu.byu.cs.tweeter.model.net.response;

public class GetCountResponse extends Response {

    private int numPeople;

    /**
     * Successful at getting follower/following count; success is true and numFollowers/numFollowing is set by constructor
     * @param numPeople
     */
    public GetCountResponse(int numPeople) {
        super(true);
        this.numPeople = numPeople;
    }

    /**
     * Unsuccessful in getting followers/following count; success is false and sets message
     * @param message
     */
    public GetCountResponse(String message) {
        super(false, message);
    }

    public int getNumPeople() {
        return numPeople;
    }

    public void setNumPeople(int numPeople) {
        this.numPeople = numPeople;
    }
}
