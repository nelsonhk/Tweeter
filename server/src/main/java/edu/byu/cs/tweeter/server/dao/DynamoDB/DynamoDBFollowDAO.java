package edu.byu.cs.tweeter.server.dao.DynamoDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingRequest;
import edu.byu.cs.tweeter.model.net.request.IsFollowerRequest;
import edu.byu.cs.tweeter.model.net.request.UnfollowRequest;
import edu.byu.cs.tweeter.model.net.response.GetFollowersCountResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowingCountResponse;
import edu.byu.cs.tweeter.model.net.response.IsFollowerResponse;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Follows;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Users;
import edu.byu.cs.tweeter.server.dao.IFollowDAO;
import edu.byu.cs.tweeter.util.Pair;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

/**
 * A DAO for accessing 'following' data from the database.
 */
public class DynamoDBFollowDAO implements IFollowDAO {

    private static final String TableName = "follows";
    public static final String IndexName = "followee_handle-follower_handle-index";

    private static final String FolloweeAttr = "followee_handle";
    private static final String FollowerAttr = "follower_handle";

    // DynamoDB client
    private static DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
            .region(Region.US_WEST_2)
            .build();

    private static DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();

    private static boolean isNonEmptyString(String value) {
        return (value != null && value.length() > 0);
    }

    @Override
    public void follow(User followee, User follower) {
        DynamoDbTable<Follows> table = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class));

        Follows follows = new Follows();
        follows.setFollower_handle(follower.getAlias());
        follows.setFollower_name(follower.getName());
        follows.setFollowee_handle(followee.getAlias());
        follows.setFollowee_name(followee.getName());
        table.putItem(follows);
    }

    @Override
    public void unfollow(String follower_handle, String followee_handle) {
        DynamoDbTable<Follows> table = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class));

        Key key = Key.builder()
                .partitionValue(follower_handle).sortValue(followee_handle)
                .build();
        table.deleteItem(key);
    }

    @Override
    public boolean isFollower(String followee_handle, String follower_handle) {
        DynamoDbTable<Follows> table = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class));
        Key key = Key.builder().partitionValue(follower_handle).sortValue(followee_handle).build();
        Follows follows = table.getItem(key);
        if (follows == null) {
            throw new RuntimeException("Follow relationship does not exist between " + follower_handle + " and " + followee_handle);
        } else {
            return true;
        }
    }

    @Override
    public List<Follows> getFollowees(String follower_handle, int pageSize, String lastFolloweeAlias) {
        assert pageSize > 0;
        assert follower_handle != null;

        DynamoDbTable<Follows> table = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class));
        Key key = Key.builder()
                .partitionValue(follower_handle)
                .build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key)).scanIndexForward(true);

        if(isNonEmptyString(lastFolloweeAlias)) {
            // Build up the Exclusive Start Key (telling DynamoDB where you left off reading items)
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(FollowerAttr, AttributeValue.builder().s(follower_handle).build());
            startKey.put(FolloweeAttr, AttributeValue.builder().s(lastFolloweeAlias).build());

            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest request = requestBuilder.build();

        return table.query(request)
                .items()
                .stream()
                .limit(pageSize)
                .collect(Collectors.toList());
    }


    @Override
    public List<Follows> getFollowers(String followee_handle, int pageSize, String lastFollower) {
        assert pageSize > 0;
        assert followee_handle != null;

        DynamoDbIndex<Follows> index = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class)).index(IndexName);
        Key key = Key.builder()
                .partitionValue(followee_handle)
                .build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key))
                .limit(pageSize);

        if(isNonEmptyString(lastFollower)) {
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(FolloweeAttr, AttributeValue.builder().s(followee_handle).build());
            startKey.put(FollowerAttr, AttributeValue.builder().s(lastFollower).build());

            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest request = requestBuilder.build();

        List<Follows> follows = new ArrayList<>();

        SdkIterable<Page<Follows>> results2 = index.query(request);
        PageIterable<Follows> pages = PageIterable.create(results2);
        // limit 1 page, with pageSize items
        pages.stream()
                .limit(1)
                .forEach(followsPage -> followsPage.items().forEach(v -> follows.add(v)));

        return follows;
    }

    @Override
    public List<Follows> getAllFollowers(String followee_handle) {
        assert followee_handle != null;

        DynamoDbIndex<Follows> index = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class)).index(IndexName);
        Key key = Key.builder()
                .partitionValue(followee_handle)
                .build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key));

        QueryEnhancedRequest request = requestBuilder.build();

        List<Follows> follows = new ArrayList<>();

        SdkIterable<Page<Follows>> results2 = index.query(request);
        PageIterable<Follows> pages = PageIterable.create(results2);
        // limit 1 page, with pageSize items
        pages.stream()
                .limit(1)
                .forEach(followsPage -> followsPage.items().forEach(v -> follows.add(v)));

        return follows;
    }


    /**
     *
     * For populating the db with dummy data
     * @param followers
     * @param followee_alias
     */
    @Override
    public void addFollowersBatch(List<String> followers, String followee_alias) {
        List<Follows> batchToWrite = new ArrayList<>();
        for (String follower_alias : followers) {
            Follows followsBean = new Follows();
            followsBean.setFollowee_handle(followee_alias);
            followsBean.setFollower_handle(follower_alias);
            batchToWrite.add(followsBean);

            if (batchToWrite.size() == 25) {
                // package this batch up and send to DynamoDB.
                batchWriteFollowers(batchToWrite);
                batchToWrite = new ArrayList<>();
            }
        }

        // write any remaining
        if (batchToWrite.size() > 0) {
            // package this batch up and send to DynamoDB.
            batchWriteFollowers(batchToWrite);
        }
    }

    // for populating the users table with 10,000 fake follows
    @Override
    public void batchWriteFollowers(List<Follows> follows) {
        if(follows.size() > 25)
            throw new RuntimeException("Too many users to write");

        DynamoDbTable<Follows> table = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class));
        WriteBatch.Builder<Follows> writeBuilder = WriteBatch.builder(Follows.class).mappedTableResource(table);
        for (Follows item : follows) {
            writeBuilder.addPutItem(builder -> builder.item(item));
        }
        BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest = BatchWriteItemEnhancedRequest.builder()
                .writeBatches(writeBuilder.build()).build();

        try {
            BatchWriteResult result = enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);

            // just hammer dynamodb again with anything that didn't get written this time
            if (result.unprocessedPutItemsForTable(table).size() > 0) {
                batchWriteFollowers(result.unprocessedPutItemsForTable(table));
            }

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}
