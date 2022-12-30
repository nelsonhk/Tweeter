package edu.byu.cs.tweeter.model.net.response;

public class LogoutResponse extends Response {

    /**
     * Creates a response indicating that the corresponding request was unsuccessful.
     *
     * @param message a message describing why the request was unsuccessful.
     */
    public LogoutResponse(String message) {
        super(false, message);
    }

    public LogoutResponse() {
        super(true);
    }


}
