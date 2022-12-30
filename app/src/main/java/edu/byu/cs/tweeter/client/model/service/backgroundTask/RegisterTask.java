package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;

/**
 * Background task that creates a new user account and logs in the new user (i.e., starts a session).
 */
public class RegisterTask extends AuthenticateTask {

    /**
     * The user's first name.
     */
    private final String firstName;
    
    /**
     * The user's last name.
     */
    private final String lastName;

    /**
     * The base-64 encoded bytes of the user's profile image.
     */
    private final String image;

    public RegisterTask(String firstName, String lastName, String username, String password,
                        String image, Handler messageHandler) {
        super(messageHandler, username, password);
        this.firstName = firstName;
        this.lastName = lastName;
        this.image = image;
    }

    @Override
    protected RegisterResponse runAuthenticationTask() {
        User registeredUser = null;
        AuthToken authToken = null;
        RegisterRequest registerRequest = new RegisterRequest(firstName, lastName, username, password, image);
        RegisterResponse registerResponse = null;
        try {
            registerResponse = new ServerFacade().register(registerRequest, "/register");
            registeredUser = registerResponse.getUser();
            authToken = registerResponse.getAuthToken();
            Cache.getInstance().setCurrUserAuthToken(authToken);
            Cache.getInstance().setCurrUser(registeredUser);
        } catch (Exception e) {
            sendExceptionMessage(e);
        }
        return registerResponse;
//        return new Pair<>(registeredUser, authToken);
//        User registeredUser = getFakeData().getFirstUser();
//        AuthToken authToken = getFakeData().getAuthToken();
    }
}
