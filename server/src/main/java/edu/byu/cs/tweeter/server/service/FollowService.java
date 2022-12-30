package edu.byu.cs.tweeter.server.service;

import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowersCountRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowingCountRequest;
import edu.byu.cs.tweeter.model.net.request.GetUserRequest;
import edu.byu.cs.tweeter.model.net.request.IsFollowerRequest;
import edu.byu.cs.tweeter.model.net.request.UnfollowRequest;
import edu.byu.cs.tweeter.model.net.response.FollowResponse;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.model.net.response.FollowingResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowersCountResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowingCountResponse;
import edu.byu.cs.tweeter.model.net.response.IsFollowerResponse;
import edu.byu.cs.tweeter.model.net.response.UnfollowResponse;
import edu.byu.cs.tweeter.server.dao.DynamoDB.DynamoDBFollowDAO;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Follows;
import edu.byu.cs.tweeter.server.dao.IDAOFactory;
import edu.byu.cs.tweeter.server.dao.IFollowDAO;
import edu.byu.cs.tweeter.server.dao.IUserDAO;
import edu.byu.cs.tweeter.util.Pair;

/**
 * Contains the business logic for getting the users a user is following.
 */
public class FollowService {

    IFollowDAO mIFollowDAO;
    IUserDAO mIUserDAO;
    AuthService mAuthService = new AuthService();

    public FollowService(IDAOFactory IDAOFactory) {
        mIUserDAO = IDAOFactory.getUserDAO();
        mIFollowDAO = IDAOFactory.getFollowDAO();
    }

    /**
     * Returns the users that the user specified in the request is following. Uses information in
     * the request object to limit the number of followees returned and to return the next set of
     * followees after any that were returned in a previous request. Uses the {@link DynamoDBFollowDAO} to
     * get the followees.
     *
     * @param request contains the data required to fulfill the request.
     * @return the followees.
     */
    public FollowingResponse getFollowees(FollowingRequest request) {
        if(request.getFollowerAlias() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a follower alias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[Bad Request] Request needs to have a positive limit");
        }

        String follower_handle = request.getFollowerAlias();
        int pageSize = request.getLimit();
        String lastFolloweeAlias = request.getLastFolloweeAlias();

        List<Follows> allFollows = mIFollowDAO.getFollowees(follower_handle, pageSize, lastFolloweeAlias);

        List<User> allFollowees = new ArrayList<>(pageSize);

        for (Follows followee : allFollows) {
            String followee_username = followee.getFollowee_handle();
            try {
                allFollowees.add(mIUserDAO.getUser(followee_username));
            } catch (Exception e) {
                return new FollowingResponse(e.getMessage());
            }
        }

        List<User> responseFollowees = new ArrayList<>(pageSize);
        boolean hasMorePages = false;

        if(pageSize > 0) {
            if (allFollowees != null) {
                int followeesIndex = getFolloweesStartingIndex(lastFolloweeAlias, allFollowees);

                for(int limitCounter = 0; followeesIndex < allFollowees.size() && limitCounter < pageSize; followeesIndex++, limitCounter++) {
                    responseFollowees.add(allFollowees.get(followeesIndex));
                }

                hasMorePages = followeesIndex < allFollowees.size();
            }
        }

        return new FollowingResponse(responseFollowees, hasMorePages);
    }

    public FollowersResponse getFollowers(FollowersRequest followersRequest) {
        if(followersRequest.getTargetUserAlias() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a target user alias");
        } else if(followersRequest.getLimit() <= 0) {
            throw new RuntimeException("[Bad Request] Request needs to have a positive limit");
        }

        String followee_handle = followersRequest.getTargetUserAlias();
        int pageSize = followersRequest.getLimit();
        String lastFollowerAlias = followersRequest.getLastFollowerAlias();

        List<Follows> allFollows = mIFollowDAO.getFollowers(followee_handle,
                pageSize, lastFollowerAlias);

        List<User> allFollowers = new ArrayList<>(pageSize);

        for (Follows follower : allFollows) {
            String follower_username = follower.getFollower_handle();
            try {
                allFollowers.add(mIUserDAO.getUser(follower_username));
            } catch (Exception e) {
                return new FollowersResponse(e.getMessage());
            }
        }

        List<User> responseFollowers = new ArrayList<>(pageSize);

        boolean hasMorePages = false;

        if(pageSize > 0) {
            if (allFollowers != null) {
                int followeesIndex = getFolloweesStartingIndex(lastFollowerAlias, allFollowers);

                for(int limitCounter = 0; followeesIndex < allFollowers.size() && limitCounter < pageSize; followeesIndex++, limitCounter++) {
                    responseFollowers.add(allFollowers.get(followeesIndex));
                }

                hasMorePages = followeesIndex < allFollowers.size();
            }
        }

//        return new FollowersResponse(allFollowers, false);
        return new FollowersResponse(responseFollowers, hasMorePages);
    }

    public FollowResponse follow(FollowRequest followRequest) {
        if (followRequest.getFollowee() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a followee");
        } else if (followRequest.getAuthToken() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have an authtoken");
        }

        if (!mAuthService.isValidToken(followRequest.getAuthToken())) {
            return new FollowResponse("[Bad Request] Cannot follow with expired authtoken");
        } else {

            User followee = followRequest.getFollowee();
            User follower = followRequest.getFollower();

            mIFollowDAO.follow(followee, follower);
            mIUserDAO.updateCounts(follower.getAlias(), followee.getAlias(), true);

            return new FollowResponse();
        }
    }

    public UnfollowResponse unfollow(UnfollowRequest unfollowRequest) {
        if (unfollowRequest.getFollowee() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a followee");
        } else if (unfollowRequest.getAuthToken() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have an authtoken");
        }

        if (!mAuthService.isValidToken(unfollowRequest.getAuthToken())) {
            return new UnfollowResponse("[Bad Request] Cannot follow with expired authtoken");
        } else {

            String follower_alias = unfollowRequest.getFollower().getAlias();
            String followee_alias = unfollowRequest.getFollowee().getAlias();

            mIFollowDAO.unfollow(follower_alias, followee_alias);
            mIUserDAO.updateCounts(follower_alias, followee_alias, false);

            return new UnfollowResponse();
        }
    }

    public GetFollowersCountResponse getFollowersCount(GetFollowersCountRequest getFollowersCountRequest) {
        if (getFollowersCountRequest.getAuthToken() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have an authtoken");
        } else if (getFollowersCountRequest.getTargetUser() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a target user");
        }

        try {
            int followersCount = mIUserDAO.getFollowersCount(getFollowersCountRequest.getTargetUser().getAlias());
            return new GetFollowersCountResponse(followersCount);
        } catch (Exception e) {
            return new GetFollowersCountResponse(e.getMessage());
        }
    }

    public GetFollowingCountResponse getFollowingCount(GetFollowingCountRequest getFollowingCountRequest) {
        if (getFollowingCountRequest.getAuthToken() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have an authtoken");
        } else if (getFollowingCountRequest.getTargetUser() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a target user");
        }

        try {
            int followingCount = mIUserDAO.getFollowingCount(getFollowingCountRequest.getTargetUser().getAlias());
            return new GetFollowingCountResponse(followingCount);
        } catch (Exception e) {
            return new GetFollowingCountResponse(e.getMessage());
        }
    }

    public IsFollowerResponse isFollower(IsFollowerRequest isFollowerRequest) {
        if (isFollowerRequest.getFollower() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a follower");
        } else if (isFollowerRequest.getFollowee() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a followee");
        } else if (isFollowerRequest.getAuthToken() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have an authtoken");
        }

        String followee_handle = isFollowerRequest.getFollowee().getAlias();
        String follower_handle = isFollowerRequest.getFollower().getAlias();

        try {
            boolean isFollower = mIFollowDAO.isFollower(followee_handle, follower_handle);
            return new IsFollowerResponse(isFollower);
        } catch (Exception e) {
            return new IsFollowerResponse(e.getMessage());
        }
    }

    private int getFolloweesStartingIndex(String lastFolloweeAlias, List<User> allFollowees) {

        int followeesIndex = 0;

        if(lastFolloweeAlias != null) {
            // This is a paged request for something after the first page. Find the first item
            // we should return
            for (int i = 0; i < allFollowees.size(); i++) {
                if(lastFolloweeAlias.equals(allFollowees.get(i).getAlias())) {
                    // We found the index of the last item returned last time. Increment to get
                    // to the first one we should return
                    followeesIndex = i + 1;
                    break;
                }
            }
        }

        return followeesIndex;
    }


}
