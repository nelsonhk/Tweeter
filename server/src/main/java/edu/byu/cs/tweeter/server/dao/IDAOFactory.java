package edu.byu.cs.tweeter.server.dao;

public interface IDAOFactory {

    // declare methods for getting each type of DAO class?

    IFollowDAO getFollowDAO();
    IStatusDAO getStatusDAO();
    IUserDAO getUserDAO();

}
