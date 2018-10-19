package homework.seven;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class HW7 {

  private static DynamoDB dynamoDb;

  public static void main(String[] args) throws Exception {

    String currentDir = System.getProperty("user.dir");
    String jsonFilesDir = currentDir + "\\movies";
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    AmazonDynamoDBClient client = new AmazonDynamoDBClient(
        new ProfileCredentialsProvider());

    client.setEndpoint("http://localhost:8000");
    client.setSignerRegionOverride("local");
    dynamoDb = new DynamoDB(client);

    deleteExistingTable();
    createTable();

    try (var files = Files.list(Paths.get(jsonFilesDir))) {
      files.forEach(file -> {
        try {
          var movieArray = mapper.readValue(file.toFile(), Movies.class);
          // Add information for each movie to the database.
          movieArray.getMovies().forEach(HW7::addMovieToDatabase);
        } catch (IOException ex) {
          System.out.println("Fail at " + file.toString());
        }
      });
    }

    queryOne();
    queryTwo();
  }

  private static void createTable() throws InterruptedException {

    // Define the required attributes of the table.
    var attributeDefinitions = new ArrayList<AttributeDefinition>();
    attributeDefinitions.add(new AttributeDefinition()
        .withAttributeName("Title")
        .withAttributeType("S"));
    attributeDefinitions.add(new AttributeDefinition()
        .withAttributeName("ReleaseYear")
        .withAttributeType("S"));
    attributeDefinitions.add(new AttributeDefinition()
        .withAttributeName("Rating")
        .withAttributeType("S"));

    // Define the primary index hash key and range key.
    var tableKeySchema = new ArrayList<KeySchemaElement>();
    tableKeySchema.add(new KeySchemaElement()
        .withAttributeName("ReleaseYear")
        .withKeyType(KeyType.HASH));
    tableKeySchema.add(new KeySchemaElement()
        .withAttributeName("Title")
        .withKeyType(KeyType.RANGE));

    // Define the global secondary index.
    var secondaryIndex = new GlobalSecondaryIndex()
        .withIndexName("RatingIndex")
        .withProvisionedThroughput(new ProvisionedThroughput()
            .withReadCapacityUnits(10L)
            .withWriteCapacityUnits(10L))
        .withProjection(new Projection().withProjectionType(ProjectionType.ALL));

    // Define the hash key for the global secondary index.
    var indexKeySchema = new ArrayList<KeySchemaElement>();
    indexKeySchema.add(new KeySchemaElement()
        .withAttributeName("Rating")
        .withKeyType(KeyType.HASH));

    secondaryIndex.setKeySchema(indexKeySchema);

    // Create the table with the prepared information.
    var createTableRequest = new CreateTableRequest()
        .withTableName("Movies")
        .withProvisionedThroughput(new ProvisionedThroughput()
            .withReadCapacityUnits(10L)
            .withWriteCapacityUnits(10L))
        .withAttributeDefinitions(attributeDefinitions)
        .withKeySchema(tableKeySchema)
        .withGlobalSecondaryIndexes(secondaryIndex);

    var table = dynamoDb.createTable(createTableRequest);

    table.waitForActive();
  }

  private static void deleteExistingTable() throws InterruptedException {

    // Get the table and delete it if it exists.
    var table = dynamoDb.getTable("Movies");
    try {
      table.delete();
      table.waitForDelete();
    } catch (ResourceNotFoundException ex) {
      System.err.println("Failed to delete table: Table does not already exist, continuing program.");
    }
  }

  private static void addMovieToDatabase(MovieObject movie) {
    // Create an item with the Year, Title, Rating, and Score for that movie.
    var item = new Item()
        .withPrimaryKey("ReleaseYear", movie.getYear(), "Title", movie.getTitle())
        .withString("Rating", movie.getMpaa_rating())
        .withNumber("Score", movie.getRatings().getAudience_score());
    var table = dynamoDb.getTable("Movies");
    // Put the item in the table.
    table.putItem(item);
  }

  private static void queryOne() {
    // Define a query that matches with the hash key and filters results with
    // the range key.
    var spec = new QuerySpec()
        .withKeyConditionExpression("#y = :v_year and begins_with (#t,:v_title)")
        .withNameMap(new NameMap().with("#y", "ReleaseYear").with("#t", "Title"))
        .withValueMap(new ValueMap().withString(":v_year", "2005").withString(":v_title", "The P"))
        .withConsistentRead(true);

    // Query the table.
    var table = dynamoDb.getTable("Movies");
    var items = table.query(spec);

    // For all of the returned results, print out the name of the movie.
    var iterator = items.iterator();
    System.out.println("----------- Query 1 -----------");
    System.out.println("Movies from 2005 that begin with \"The P\"");
    while (iterator.hasNext()) {
      var item = iterator.next();
      System.out.printf("\t%s%n", item.getString("Title"));
    }
  }

  private static void queryTwo() {

    // Get the table and the secondary index of the table for searching.
    var table = dynamoDb.getTable("Movies");
    var index = table.getIndex("RatingIndex");

    // Define a query that matches with the secondary global index's hash key.
    var spec = new QuerySpec()
        .withKeyConditionExpression("#r = :v_rating")
        .withNameMap(new NameMap().with("#r", "Rating"))
        .withValueMap(new ValueMap().withString(":v_rating", "PG"));

    // Sum the ratings of all the returned items and divide by the total
    // number of items returned to find the average. Integer division, so the
    // number is very likely not 100% accurate.
    var items = index.query(spec);
    var iterator = items.iterator();
    System.out.println("----------- Query 2 -----------");
    var sum = 0;
    var count = 0;
    while (iterator.hasNext()) {
      sum += iterator.next().getInt("Score");
      count++;
    }
    System.out.printf("Average score is %d for all PG movies.%n", sum / (count > 0 ? count : 1));
  }
}
