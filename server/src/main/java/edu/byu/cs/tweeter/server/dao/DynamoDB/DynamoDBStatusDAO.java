package edu.byu.cs.tweeter.server.dao.DynamoDB;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.GetStatusRequest;
import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Feeds;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Follows;
import edu.byu.cs.tweeter.server.dao.DynamoDB.dto.Statuses;
import edu.byu.cs.tweeter.server.dao.IStatusDAO;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class DynamoDBStatusDAO implements IStatusDAO {

    private static final String TableNameStatuses = "statuses";
    private static final String TableNameFeed = "feeds";

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

    // put status in story table
    @Override
    public void putStatusInStory(Status post) {
        DynamoDbTable<Statuses> table = enhancedClient.table(TableNameStatuses, TableSchema.fromBean(Statuses.class));

        Statuses statuses = new Statuses();
        statuses.setUser_alias(post.getUser().getAlias());
        statuses.setMentions(post.getMentions());
        statuses.setPost_content(post.getPost());
        statuses.setUrls(post.getUrls());

        Long datetime = Long.parseLong(String.valueOf(new Date().getTime()));
        statuses.setDatetime(datetime);
//        statuses.setDatetime(Long.parseLong(post.datetime));
        table.putItem(statuses);
    }

    @Override
    public void batchWriteToFeed(List<Feeds> feedBeans) {
        if(feedBeans.size() > 25)
            throw new RuntimeException("Too many users to write");

        DynamoDbTable<Feeds> table = enhancedClient.table(TableNameFeed, TableSchema.fromBean(Feeds.class));
        WriteBatch.Builder<Feeds> writeBuilder = WriteBatch.builder(Feeds.class).mappedTableResource(table);
        for (Feeds item : feedBeans) {
            writeBuilder.addPutItem(builder -> builder.item(item));
        }
        BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest = BatchWriteItemEnhancedRequest.builder()
                .writeBatches(writeBuilder.build()).build();

        try {
            BatchWriteResult result = enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);

            // just hammer dynamodb again with anything that didn't get written this time
            if (result.unprocessedPutItemsForTable(table).size() > 0) {
                batchWriteToFeed(result.unprocessedPutItemsForTable(table));
            }

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public List<Feeds> getFeed(int pageSize, String lastStatusDateTime, String user_alias) {

        assert pageSize > 0;
        assert user_alias != null;

        DynamoDbTable<Feeds> table = enhancedClient.table(TableNameFeed, TableSchema.fromBean(Feeds.class));
        Key key = Key.builder()
                .partitionValue(user_alias)
                .build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key)).scanIndexForward(false);

        if(isNonEmptyString(lastStatusDateTime)) {
            // Build up the Exclusive Start Key (telling DynamoDB where you left off reading items)
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put("receiver_alias", AttributeValue.builder().s(user_alias).build());
            startKey.put("datetime", AttributeValue.builder().s(lastStatusDateTime).build());

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
    public List<Statuses> getStory(int pageSize, String lastStatusDateTime, String user_alias) {

        assert pageSize > 0;
        assert user_alias != null;

        DynamoDbTable<Statuses> table = enhancedClient.table(TableNameStatuses, TableSchema.fromBean(Statuses.class));
        Key key = Key.builder()
                .partitionValue(user_alias)
                .build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key)).scanIndexForward(false);

        if(isNonEmptyString(lastStatusDateTime)) {
            // Build up the Exclusive Start Key (telling DynamoDB where you left off reading items)
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put("user_alias", AttributeValue.builder().s(user_alias).build());
            startKey.put("datetime", AttributeValue.builder().s(lastStatusDateTime).build());

            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest request = requestBuilder.build();

        return table.query(request)
                .items()
                .stream()
                .limit(pageSize)
                .collect(Collectors.toList());
    }

}
