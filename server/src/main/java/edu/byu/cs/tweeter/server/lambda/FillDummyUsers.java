package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Users;
import edu.byu.cs.tweeter.server.dao.IFollowDAO;
import edu.byu.cs.tweeter.server.dao.IUserDAO;

public class FillDummyUsers extends Handler implements RequestHandler {

    // How many follower users to add
    // We recommend you test this with a smaller number first, to make sure it works for you
    private final static int NUM_USERS = 10000;

    // The alias of the user to be followed by each user created
    // This example code does not add the target user, that user must be added separately.
    private final static String FOLLOW_TARGET = "@hannah";


    @Override
    public Object handleRequest(Object input, Context context) {

        // Get instance of DAOs by way of the Abstract Factory Pattern
        IUserDAO userDAO = mDAOFactory.getUserDAO();
        IFollowDAO followDAO = mDAOFactory.getFollowDAO();

        List<String> followers = new ArrayList<>();
        List<Users> users = new ArrayList<>();

        // Iterate over the number of users you will create
        for (int i = 1; i <= NUM_USERS; i++) {

            String name = "testUser " + i;
            String alias = "test_user" + i;

            // Note that in this example, a UserDTO only has a name and an alias.
            // The url for the profile image can be derived from the alias in this example
            Users user = new Users();
            user.setUser_alias(alias);
            user.setFirstName(name);
            users.add(user);

            // Note that in this example, to represent a follows relationship, only the aliases
            // of the two users are needed
            followers.add(alias);
        }

        // Call the DAOs for the database logic
        if (users.size() > 0) {
            userDAO.addUserBatch(users);
        }
        if (followers.size() > 0) {
            followDAO.addFollowersBatch(followers, FOLLOW_TARGET);
        }

        return null;
    }
}
