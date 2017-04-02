package homework.seven;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class HW7 {

  private static DynamoDB dynamoDb;

  public static void main(String[] args) throws IOException,
      InterruptedException {
    String currentDir = System.getProperty("user.dir");
    String jsonFilesDir = currentDir + "\\movies";
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        false);

    AmazonDynamoDBClient client = new AmazonDynamoDBClient(
        new ProfileCredentialsProvider());

    client.setEndpoint("http://localhost:8000");
    client.setSignerRegionOverride("local");

    dynamoDb = new DynamoDB(client);

    deleteExistingTable();
    createTable();

    long numFilesToParse = Files.list(Paths.get(jsonFilesDir)).count();
    // Read JSON files.
    for (long i = 1; i <= numFilesToParse; i++) {
      Movies movieArray = mapper.readValue(new File(jsonFilesDir + "\\page"
          + i + ".json"), Movies.class);
      // Add movie node and actor nodes for that movie.
      for (MovieObject movie : movieArray.getMovies()) {
        addMovieToDatabase(movie);
      }
    }

    queryOne();
    queryTwo();
  }

  private static void createTable() throws
      InterruptedException {
    ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();

    attributeDefinitions.add(new AttributeDefinition()
        .withAttributeName("Title")
        .withAttributeType("S"));
    attributeDefinitions.add(new AttributeDefinition()
        .withAttributeName("ReleaseYear")
        .withAttributeType("S"));
    attributeDefinitions.add(new AttributeDefinition()
        .withAttributeName("Rating")
        .withAttributeType("S"));

    ArrayList<KeySchemaElement> tableKeySchema = new ArrayList<>();
    tableKeySchema.add(new KeySchemaElement()
        .withAttributeName("ReleaseYear")
        .withKeyType(KeyType.HASH));
    tableKeySchema.add(new KeySchemaElement()
        .withAttributeName("Title")
        .withKeyType(KeyType.RANGE));

    GlobalSecondaryIndex secondaryIndex = new GlobalSecondaryIndex()
        .withIndexName("RatingIndex")
        .withProvisionedThroughput(new ProvisionedThroughput()
            .withReadCapacityUnits(10L)
            .withWriteCapacityUnits(10L))
        .withProjection(new Projection().withProjectionType(ProjectionType.ALL));

    ArrayList<KeySchemaElement> indexKeySchema = new ArrayList<>();

    indexKeySchema.add(new KeySchemaElement()
        .withAttributeName("Rating")
        .withKeyType(KeyType.HASH));

    secondaryIndex.setKeySchema(indexKeySchema);

    CreateTableRequest createTableRequest = new CreateTableRequest()
        .withTableName("Movies")
        .withProvisionedThroughput(new ProvisionedThroughput()
            .withReadCapacityUnits(10L)
            .withWriteCapacityUnits(10L))
        .withAttributeDefinitions(attributeDefinitions)
        .withKeySchema(tableKeySchema)
        .withGlobalSecondaryIndexes(secondaryIndex);

    Table table = dynamoDb.createTable(createTableRequest);

    table.waitForActive();
  }

  private static void deleteExistingTable() throws
      InterruptedException {

    Table table = dynamoDb.getTable("Movies");
    try {
      table.delete();
      table.waitForDelete();
    } catch (ResourceNotFoundException ex) {
      System.err.println(ex.getMessage());
    }
  }

  private static void addMovieToDatabase(MovieObject movie) {
    Item item = new Item()
        .withPrimaryKey("ReleaseYear", movie.getYear(), "Title", movie
            .getTitle())
        .withString("Rating", movie.getMpaa_rating())
        .withNumber("Score", movie.getRatings().getAudience_score());
    Table table = dynamoDb.getTable("Movies");
    table.putItem(item);
  }

  private static void queryOne() {
    Table table = dynamoDb.getTable("Movies");

    HashMap<String, String> nameMap = new HashMap<>();
    nameMap.put("#yr", "Year");
    nameMap.put("#tl", "Title");

    HashMap<String, Object> valueMap = new HashMap<>();
    valueMap.put(":yyyy", 2005);
    valueMap.put(":bgw", "The P");

    QuerySpec spec = new QuerySpec()
        .withKeyConditionExpression("#y = :v_year and begins_with (#t,"
            + " :v_title)")
        .withNameMap(new NameMap()
            .with("#y", "ReleaseYear")
            .with("#t", "Title"))
        .withValueMap(new ValueMap()
            .withString(":v_year", "2005")
            .withString(":v_title", "The P"))
        .withConsistentRead(true);

    ItemCollection<QueryOutcome> items = table.query(spec);

    Iterator<Item> iterator = items.iterator();
    System.out.println("----------- Query 1 -----------");
    System.out.println("Movies from 2005 that begin with \"The P\"");
    while (iterator.hasNext()) {
      Item item = iterator.next();
      System.out.printf("\t%s%n", item.getString("Title"));
    }
  }

  private static void queryTwo() {
    Table table = dynamoDb.getTable("Movies");
    Index index = table.getIndex("RatingIndex");

    QuerySpec spec = new QuerySpec()
        .withKeyConditionExpression("#r = :v_rating")
        .withNameMap(new NameMap()
            .with("#r", "Rating"))
        .withValueMap(new ValueMap()
            .withString(":v_rating","PG"));

    ItemCollection<QueryOutcome> items = index.query(spec);
    Iterator<Item> iterator = items.iterator();

    System.out.println("----------- Query 2 -----------");

    double sum = 0;
    int count = 0;

    while (iterator.hasNext()) {
      sum += iterator.next().getInt("Score");
      count++;
    }

    System.out.printf("Average score is %.2f for all PG movies.%n",
        sum / count);

  }
}
