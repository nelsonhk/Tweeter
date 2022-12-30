package edu.byu.cs.tweeter.server.dao.DynamoDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Follows;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class FollowsDAO {

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

//    // Add a follows relationship based on follower_handle (partition key)
//    public void addFollows(String follower_handle, String follower_name, String followee_handle, String followee_name) {
//        DynamoDbTable<Follows> table = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class));
//
//        Follows follows = new Follows();
//        follows.setFollower_handle(follower_handle);
//        follows.setFollower_name(follower_name);
//        follows.setFollowee_handle(followee_handle);
//        follows.setFollowee_name(followee_name);
//        table.putItem(follows);
//    }

//    // Gets a follow relationship
//    public Follows getFollows(String follower_handle, String followee_handle) {
//        DynamoDbTable<Follows> table = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class));
//        Key key = Key.builder().partitionValue(follower_handle).sortValue(followee_handle).build();
//
//        Follows follows = table.getItem(key);
//        if (follows == null) {
//            System.out.println("Follow relationship does not exist between " + follower_handle + " and " + followee_handle);
//            return null;
//        } else {
//            return follows;
//        }
//    }

    // Not used in DynamoDBFollowDAO
//    public void updateFollows(String follower_handle, String followee_handle, String new_followee_name) {
//        DynamoDbTable<Follows> table = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class));
//
//        if (getFollows(follower_handle, followee_handle) != null) {
//            Follows follows = getFollows(follower_handle, followee_handle);
//            follows.setFollowee_name(new_followee_name);
//            table.updateItem(follows);
//        } else {
//            System.out.println("Could not update, follows relationship does not exist");
//            // could also add it here if it doesn't exist, it's just a product choice
//        }
//
//    }

//    // Delete a relationship from the table using follower_handle
//    public void deleteFollows(String follower_handle, String followee_handle) {
//        DynamoDbTable<Follows> table = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class));
//        Key key = Key.builder()
//                .partitionValue(follower_handle).sortValue(followee_handle)
//                .build();
//        table.deleteItem(key);
//    }

    // Gets all the people that follower_handle is following (unpaginated)
    public List<Follows> getFolloweesUnpaginated(String follower_handle, String lastFollowee) {
        DynamoDbTable<Follows> table = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class));
        Key key = Key.builder()
                .partitionValue(follower_handle)
                .build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key)).scanIndexForward(true);

        if(isNonEmptyString(lastFollowee)) {
            // Build up the Exclusive Start Key (telling DynamoDB where you left off reading items)
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(FollowerAttr, AttributeValue.builder().s(follower_handle).build());
            startKey.put(FolloweeAttr, AttributeValue.builder().s(lastFollowee).build());

            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest request = requestBuilder.build();

        return table.query(request)
                .items()
                .stream()
                .collect(Collectors.toList());
    }

//    // Gets a page of the people that follower_handle is following (paginated)
//    public List<Follows> getFollowees(String follower_handle, int pageSize, String lastFollowee) {
//        DynamoDbTable<Follows> table = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class));
//        Key key = Key.builder()
//                .partitionValue(follower_handle)
//                .build();
//
//        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
//                .queryConditional(QueryConditional.keyEqualTo(key)).scanIndexForward(true);
//
//        if(isNonEmptyString(lastFollowee)) {
//            // Build up the Exclusive Start Key (telling DynamoDB where you left off reading items)
//            Map<String, AttributeValue> startKey = new HashMap<>();
//            startKey.put(FollowerAttr, AttributeValue.builder().s(follower_handle).build());
//            startKey.put(FolloweeAttr, AttributeValue.builder().s(lastFollowee).build());
//
//            requestBuilder.exclusiveStartKey(startKey);
//        }
//
//        QueryEnhancedRequest request = requestBuilder.build();
//
//        return table.query(request)
//                .items()
//                .stream()
//                .limit(pageSize)
//                .collect(Collectors.toList());
//    }

//    // Gets a page of the people following followee_handle (paginated)
//    public List<Follows> getFollowers(String followee_handle, int pageSize, String lastFollower) {
//        DynamoDbIndex<Follows> index = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class)).index(IndexName);
//        Key key = Key.builder()
//                .partitionValue(followee_handle)
//                .build();
//
//        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
//                .queryConditional(QueryConditional.keyEqualTo(key))
//                // Unlike Tables, querying from an Index returns a PageIterable, so we want to just ask for
//                // 1 page with pageSize items
//                .limit(pageSize);
//
//        if(isNonEmptyString(lastFollower)) {
//            Map<String, AttributeValue> startKey = new HashMap<>();
//            startKey.put(FolloweeAttr, AttributeValue.builder().s(followee_handle).build());
//            startKey.put(FollowerAttr, AttributeValue.builder().s(lastFollower).build());
//
//            requestBuilder.exclusiveStartKey(startKey);
//        }
//
//        QueryEnhancedRequest request = requestBuilder.build();
//
//        List<Follows> follows = new ArrayList<>();
//
//        SdkIterable<Page<Follows>> results2 = index.query(request);
//        PageIterable<Follows> pages = PageIterable.create(results2);
//        // limit 1 page, with pageSize items
//        pages.stream()
//                .limit(1)
//                .forEach(followsPage -> followsPage.items().forEach(v -> follows.add(v)));
//
//        return follows;
//    }

    // Gets all the people following followee_handle (unpaginated)
    public List<Follows> getFollowersUnpaginated(String followee_handle, String lastFollower) {
        DynamoDbIndex<Follows> index = enhancedClient.table(TableName, TableSchema.fromBean(Follows.class)).index(IndexName);
        Key key = Key.builder()
                .partitionValue(followee_handle)
                .build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key));
                // Unlike Tables, querying from an Index returns a PageIterable, so we want to just ask for
                // 1 page with pageSize items

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

}
