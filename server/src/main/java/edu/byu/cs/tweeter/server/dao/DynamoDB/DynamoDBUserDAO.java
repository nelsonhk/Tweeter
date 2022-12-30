package edu.byu.cs.tweeter.server.dao.DynamoDB;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.LogoutRequest;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Authtokens;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Users;
import edu.byu.cs.tweeter.server.dao.IUserDAO;
import edu.byu.cs.tweeter.util.Pair;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class DynamoDBUserDAO implements IUserDAO {

    private static final String TableNameUsers = "users";
    private static final String TableNameAuthtokens = "authtokens";

    // DynamoDB client
    private static DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
            .region(Region.US_WEST_2)
            .build();

    private static DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();


    @Override
    public Pair<User, AuthToken> login(String username, String password) {

        DynamoDbTable<Users> table = enhancedClient.table(TableNameUsers, TableSchema.fromBean(Users.class));
        Key key = Key.builder().partitionValue(username).build();

        Users users = table.getItem(key);
        if (users == null) {
            System.out.println("[Bad Request] User does not exist. Could not log in");
            return null;
        } else {
            try {
                if (validatePassword(password, users.getPassword())) {
                    User user = new User(users.getFirstName(), users.getLastName(), users.getUser_alias(), users.getImageUrl());

                    String token = UUID.randomUUID().toString();
                    String datetime = String.valueOf(new Date().getTime());

                    AuthToken authToken = new AuthToken(token, datetime);
                    saveAuthToken(authToken);

                    return new Pair<>(user, authToken);
                } else {
                    return new Pair<>(null, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public Pair<User, AuthToken> register(User user, String password) {

        DynamoDbTable<Users> table = enhancedClient.table(TableNameUsers, TableSchema.fromBean(Users.class));

        try {
            Users users = new Users();
            users.setUser_alias(user.getAlias());
            users.setFirstName(user.getFirstName());
            users.setLastName(user.getLastName());
            users.setImageUrl(user.getImageUrl());
            users.setFollowersCount(0);
            users.setFollowingCount(0);

            // hash the password
            String generatedSecuredPasswordHash = generateStrongPasswordHash(password);
            users.setPassword(generatedSecuredPasswordHash);
            table.putItem(users);

            // generate authtoken, save to the authtokens table
            String token = UUID.randomUUID().toString();
            String datetime = String.valueOf(new Date().getTime());

            AuthToken authToken = new AuthToken(token, datetime);
            saveAuthToken(authToken);

            return new Pair<>(user, authToken);
        } catch (Exception e) {
            e.printStackTrace();
            return new Pair<>(null, null);
        }

    }

    @Override
    public void logout(LogoutRequest logoutRequest) {

        DynamoDbTable<Authtokens> table = enhancedClient.table(TableNameAuthtokens, TableSchema.fromBean(Authtokens.class));
        //Long datetime = Long.parseLong(logoutRequest.getAuthToken().datetime);
        Key key = Key.builder().partitionValue(logoutRequest.getAuthToken().token).build();
        Authtokens authtokens = table.getItem(key);
        if (authtokens == null) {
            throw new RuntimeException("[Bad Request] Current authtoken cannot be found");
        } else {
            table.deleteItem(key);
        }
    }

    @Override
    public User getUser(String username) {

        DynamoDbTable<Users> table = enhancedClient.table(TableNameUsers, TableSchema.fromBean(Users.class));
        Key key = Key.builder().partitionValue(username).build();
        Users users = table.getItem(key);

        if (users == null) {
            throw new RuntimeException("Cannot get user");
        } else {
            User user = new User(users.getFirstName(), users.getLastName(), users.getUser_alias(),
                    users.getImageUrl());
            return user;
        }
    }

    @Override
    public int getFollowingCount(String username) {
        DynamoDbTable<Users> table = enhancedClient.table(TableNameUsers, TableSchema.fromBean(Users.class));
        Key key = Key.builder().partitionValue(username).build();
        Users users = table.getItem(key);

        if (users == null) {
            throw new RuntimeException("Cannot get user for following count");
        } else {
            return users.getFollowingCount();
        }
    }

    @Override
    public int getFollowersCount(String username) {
        DynamoDbTable<Users> table = enhancedClient.table(TableNameUsers, TableSchema.fromBean(Users.class));
        Key key = Key.builder().partitionValue(username).build();
        Users users = table.getItem(key);

        if (users == null) {
            throw new RuntimeException("Cannot get user for followers count");
        } else {
            return users.getFollowersCount();
        }
    }

    @Override
    public void updateCounts(String follower_handle, String followee_handle, boolean follow) {

        DynamoDbTable<Users> table = enhancedClient.table(TableNameUsers, TableSchema.fromBean(Users.class));
        Key followerKey = Key.builder().partitionValue(follower_handle).build();
        Key followeeKey = Key.builder().partitionValue(followee_handle).build();
        Users follower = table.getItem(followerKey);
        Users followee = table.getItem(followeeKey);

        if (followee != null & follower != null) {
            if (follow) {
                follower.setFollowingCount(follower.getFollowingCount() + 1);
                followee.setFollowersCount(followee.getFollowersCount() + 1);
            } else {
                follower.setFollowingCount(follower.getFollowingCount() - 1);
                followee.setFollowersCount(followee.getFollowersCount() - 1);
            }
            table.updateItem(follower);
            table.updateItem(followee);
        } else {
            System.out.println("Could not update counts because users are null");
        }
    }

    // Save AuthToken to authtokens table
    private void saveAuthToken(AuthToken authToken) {
        DynamoDbTable<Authtokens> table = enhancedClient.table(TableNameAuthtokens, TableSchema.fromBean(Authtokens.class));

        Long datetime = Long.parseLong(authToken.getDatetime());
        Authtokens authtokens = new Authtokens();
        authtokens.setToken(authToken.getToken());
        authtokens.setDatetime(datetime);

        try {
            table.putItem(authtokens);
        } catch (Exception e) {
            System.out.println("Couldn't save authtoken");
            throw e;
        }
    }

    @Override
    public boolean isAuthTokenInTable(String token) {
        DynamoDbTable<Authtokens> table = enhancedClient.table(TableNameAuthtokens, TableSchema.fromBean(Authtokens.class));
        Key key = Key.builder().partitionValue(token).build();
        Authtokens authtokens = table.getItem(key);

        return authtokens != null;
    }

    /**
     * Following methods are for salting and hashing the passwords
     */
    private static boolean validatePassword(String originalPassword, String storedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] parts = storedPassword.split(":");
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = fromHex(parts[1]);
        byte[] hash = fromHex(parts[2]);

        PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] testHash = skf.generateSecret(spec).getEncoded();

        int diff = hash.length ^ testHash.length;
        for(int i = 0; i < hash.length && i < testHash.length; i++)
        {
            diff |= hash[i] ^ testHash[i];
        }
        return diff == 0;
    }
    private static byte[] fromHex(String hex) throws NoSuchAlgorithmException
    {
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++)
        {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    private static String generateStrongPasswordHash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        int iterations = 1000;
        char[] chars = password.toCharArray();
        byte[] salt = getSalt();

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return iterations + ":" + toHex(salt) + ":" + toHex(hash);
    }

    private static byte[] getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    private static String toHex(byte[] array) throws NoSuchAlgorithmException
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }

    @Override
    public void addUserBatch(List<Users> users) {
        List<Users> batchToWrite = new ArrayList<>();
        for (Users u : users) {
            Users userBean = new Users();
            userBean.setUser_alias(u.getUser_alias());
            userBean.setFirstName(u.getFirstName());
            batchToWrite.add(userBean);

            if (batchToWrite.size() == 25) {
                // package this batch up and send to DynamoDB.
                batchWriteUsers(batchToWrite);
                batchToWrite = new ArrayList<>();
            }
        }

        // write any remaining
        if (batchToWrite.size() > 0) {
            // package this batch up and send to DynamoDB.
            batchWriteUsers(batchToWrite);
        }
    }

    // for populating the users table with 10,000 fake users
    @Override
    public void batchWriteUsers(List<Users> users) {
        if(users.size() > 25)
            throw new RuntimeException("Too many users to write");

        DynamoDbTable<Users> table = enhancedClient.table(TableNameUsers, TableSchema.fromBean(Users.class));
        WriteBatch.Builder<Users> writeBuilder = WriteBatch.builder(Users.class).mappedTableResource(table);
        for (Users item : users) {
            writeBuilder.addPutItem(builder -> builder.item(item));
        }
        BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest = BatchWriteItemEnhancedRequest.builder()
                .writeBatches(writeBuilder.build()).build();

        try {
            BatchWriteResult result = enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);

            // just hammer dynamodb again with anything that didn't get written this time
            if (result.unprocessedPutItemsForTable(table).size() > 0) {
                batchWriteUsers(result.unprocessedPutItemsForTable(table));
            }

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}
