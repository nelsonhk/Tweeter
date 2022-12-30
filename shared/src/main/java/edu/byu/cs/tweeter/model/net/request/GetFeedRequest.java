//package edu.byu.cs.tweeter.model.net.request;
//
//import edu.byu.cs.tweeter.model.domain.AuthToken;
//import edu.byu.cs.tweeter.model.domain.Status;
//import edu.byu.cs.tweeter.model.domain.User;
//
//public class GetFeedRequest {
//
//    private AuthToken authToken;
//    private String targetUserAlias;
//    private int limit;
//    private Status lastStatus;
//
//    private GetFeedRequest() {}
//
//    public GetFeedRequest(AuthToken authToken, String targetUserAlias, int limit, Status lastStatus) {
//        this.authToken = authToken;
//        this.targetUserAlias = targetUserAlias;
//        this.limit = limit;
//        this.lastStatus = lastStatus;
//    }
//
//    public AuthToken getAuthToken() {
//        return authToken;
//    }
//
//    public void setAuthToken(AuthToken authToken) {
//        this.authToken = authToken;
//    }
//
//    public int getLimit() {
//        return limit;
//    }
//
//    public void setLimit(int limit) {
//        this.limit = limit;
//    }
//
//    public Status getLastStatus() {
//        return lastStatus;
//    }
//
//    public void setLastStatus(Status lastStatus) {
//        this.lastStatus = lastStatus;
//    }
//
//    public String getTargetUserAlias() {
//        return targetUserAlias;
//    }
//
//    public void setTargetUserAlias(String targetUserAlias) {
//        this.targetUserAlias = targetUserAlias;
//    }
//}
