package edu.byu.cs.tweeter.server.dao.DynamoDB;

import edu.byu.cs.tweeter.server.dao.IDAOFactory;
import edu.byu.cs.tweeter.server.dao.IFollowDAO;
import edu.byu.cs.tweeter.server.dao.IStatusDAO;
import edu.byu.cs.tweeter.server.dao.IUserDAO;

public class DynamoDBFactory implements IDAOFactory {

    //define classes for getting each type of DAO class?

    @Override
    public IFollowDAO getFollowDAO() {
        return new DynamoDBFollowDAO();
    }

    @Override
    public IStatusDAO getStatusDAO() {
        return new DynamoDBStatusDAO();
    }

    @Override
    public IUserDAO getUserDAO() {
        return new DynamoDBUserDAO();
    }

}
