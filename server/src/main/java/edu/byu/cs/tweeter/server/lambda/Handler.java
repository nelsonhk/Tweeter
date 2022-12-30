package edu.byu.cs.tweeter.server.lambda;

import edu.byu.cs.tweeter.server.dao.DynamoDB.DynamoDBFactory;
import edu.byu.cs.tweeter.server.dao.IDAOFactory;

public class Handler {

    protected static IDAOFactory mDAOFactory = new DynamoDBFactory();

}
