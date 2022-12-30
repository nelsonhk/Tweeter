package edu.byu.cs.tweeter.server.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.GetUserRequest;
import edu.byu.cs.tweeter.model.net.request.LoginRequest;
import edu.byu.cs.tweeter.model.net.request.LogoutRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.response.GetUserResponse;
import edu.byu.cs.tweeter.model.net.response.LoginResponse;
import edu.byu.cs.tweeter.model.net.response.LogoutResponse;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;
import edu.byu.cs.tweeter.server.dao.IDAOFactory;
import edu.byu.cs.tweeter.server.dao.IUserDAO;
import edu.byu.cs.tweeter.util.Pair;

public class UserService {

    IUserDAO mIUserDAO;
    AuthService mAuthService = new AuthService();

    public UserService(IDAOFactory daoFactory) {
        mIUserDAO = daoFactory.getUserDAO();
    }

    public LoginResponse login(LoginRequest request) {
        if(request.getUsername() == null){
            throw new RuntimeException("[Bad Request] Missing a username");
        } else if(request.getPassword() == null) {
            throw new RuntimeException("[Bad Request] Missing a password");
        }

        Pair<User, AuthToken> daoResponse = mIUserDAO.login(request.getUsername(), request.getPassword());
        if (daoResponse.getFirst() == null && daoResponse.getSecond() == null) {
            return new LoginResponse("Invalid password");
        } else {
            return new LoginResponse(daoResponse.getFirst(), daoResponse.getSecond());
        }
//        return mIUserDAO.login(request.getUsername(), request.getPassword());
    }

    public RegisterResponse register(RegisterRequest registerRequest) {
//        System.out.println("Username: " + registerRequest.getUsername());
        if(registerRequest.getUsername() == null) {
            throw new RuntimeException("[Bad Request] Missing a username");
        } else if(registerRequest.getPassword() == null) {
            throw new RuntimeException("[Bad Request] Missing a password");
        } else if(registerRequest.getFirstName() == null) {
            throw new RuntimeException("[Bad Request] Missing a first name");
        } else if(registerRequest.getLastName() == null) {
            throw new RuntimeException("[Bad Request] Missing a last name");
        } else if(registerRequest.getImage() == null) {
            throw new RuntimeException("[Bad Request] Missing profile image");
        }

        // convert String imageURL to byte array
        byte[] imageByteArray = Base64.getDecoder().decode(registerRequest.getImage());

        // upload image to S3
        String path = "images/" + registerRequest.getUsername() + ".jpg";
        String imageURL = uploadFile("milestone4a", path,
                new ByteArrayInputStream(imageByteArray), imageByteArray.length, "image/jpeg");
        if (imageURL != null) {
            // get URL from S3, send alongside registerRequest
            User user = new User(registerRequest.getFirstName(), registerRequest.getLastName(), registerRequest.getUsername(),
                    imageURL);

            Pair<User, AuthToken> daoResponse = mIUserDAO.register(user, registerRequest.getPassword());
            if (daoResponse.getFirst() == null && daoResponse.getSecond() == null) {
                return new RegisterResponse("Exception occurred while registering user");
            } else {
                return new RegisterResponse(daoResponse.getFirst(), daoResponse.getSecond());
            }
        } else {
            return new RegisterResponse("Could not register user, uploaded file was null");
        }
    }

    private String uploadFile(String bucketName, String keyName,
                               InputStream content, long contentLength, String mimeType) {
        final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentLength);
            metadata.setContentType(mimeType);

            s3.putObject(new PutObjectRequest(bucketName, keyName, content, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            String imageURL = s3.getUrl(bucketName, keyName).toString();
            return imageURL;
        } catch (AmazonServiceException e) {
            System.out.println(e.getErrorMessage());
            e.printStackTrace();
            return null;
        }
    }

    public LogoutResponse logout(LogoutRequest logoutRequest) {
        if (logoutRequest.getAuthToken() == null) {
            throw new RuntimeException("[Bad Request] Missing auth token");
        }

        try {
            mIUserDAO.logout(logoutRequest);
            return new LogoutResponse();
        } catch (Exception e) {
            return new LogoutResponse(e.getMessage());
        }
    }

    public GetUserResponse getUser(GetUserRequest getUserRequest) {
        if (getUserRequest.getAlias() == null) {
            throw new RuntimeException("[Bad Request] Missing user alias");
        } else if (getUserRequest.getAuthToken() == null) {
            throw new RuntimeException("[Bad Request] Missing authtoken");
        }

        if (!mAuthService.isValidToken(getUserRequest.getAuthToken())) {
            return new GetUserResponse("[Bad Request] Cannot get user with expired authtoken");
        } else {
            try {
                return new GetUserResponse(mIUserDAO.getUser(getUserRequest.getAlias()));
            } catch (Exception e) {
                return new GetUserResponse(e.getMessage());
            }
        }
    }

}
