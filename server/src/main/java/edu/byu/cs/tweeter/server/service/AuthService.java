package edu.byu.cs.tweeter.server.service;

import java.util.Date;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.server.dao.DynamoDB.DynamoDBUserDAO;
import edu.byu.cs.tweeter.server.dao.IUserDAO;

public class AuthService {

    //TODO: use factory pattern to pass in this userDAO
    IUserDAO userDAO = new DynamoDBUserDAO();

    public boolean isValidToken(AuthToken authToken) {

        // check to make sure authtoken is in the authtoken table
        if (userDAO.isAuthTokenInTable(authToken.token)) {

            // check to make sure authtoken is not expire
            Long currentTime = Long.parseLong(String.valueOf(new Date().getTime()));
            Long authTokenDateTime = Long.parseLong(authToken.getDatetime());

            long diff = (currentTime - authTokenDateTime);

            return diff < 3000000;
        }

        return false;
    }
}
