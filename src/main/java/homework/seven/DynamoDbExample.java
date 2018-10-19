package homework.seven;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This example shows you how to create a client for DynamoDB Local.
 *
 * @author Dahai Guo
 */
public class DynamoDbExample {
  private static DynamoDB dynamoDb;

  public static void main(String[] args) throws InterruptedException {

    var client = new AmazonDynamoDBClient(new ProfileCredentialsProvider());
    client.setEndpoint("http://localhost:8000");
    client.setSignerRegionOverride("local");

    dynamoDb = new DynamoDB(client);

    deleteExampleTable();

    createTableExample();

    var tables = dynamoDb.listTables();
    var table = tables.iterator();
    var numOfTables = 0;
    while (table.hasNext()) {
      System.out.println(table.next().getDescription());
      numOfTables++;
    }

    System.out.printf("%d tables exist in the database.", numOfTables);

    // see what you can do with such a "dynamoDb" object
    // 1. Working with tables
    //    http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/JavaDocumentAPIWorkingWithTables.html
    // 2. Working with indexes
    //    http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/GSIJavaDocumentAPI.html
  }

  private static void deleteExampleTable() throws InterruptedException {

    var table = dynamoDb.getTable("testTable");
    try {
      System.out.println("Issuing DeleteTable request for " + "testTable");
      table.delete();

      System.out.println("Waiting for testTable to be deleted... this may take a while...");

      table.waitForDelete();
    } catch (ResourceNotFoundException ex) {
      System.err.println("DeleteTable request failed for testTable");
      System.err.println(ex.getMessage());
    }
  }

  private static void createTableExample() throws InterruptedException {
    var attributeDefinitions = new ArrayList<AttributeDefinition>();
    attributeDefinitions.add(new AttributeDefinition().withAttributeName("Id").withAttributeType("N"));

    var keySchema = new ArrayList<KeySchemaElement>();
    keySchema.add(new KeySchemaElement().withAttributeName("Id").withKeyType(KeyType.HASH));

    var request = new CreateTableRequest()
        .withTableName("testTable")
        .withKeySchema(keySchema)
        .withAttributeDefinitions(attributeDefinitions)
        .withProvisionedThroughput(new ProvisionedThroughput()
            .withReadCapacityUnits(10L)
            .withWriteCapacityUnits(10L));

    var table = dynamoDb.createTable(request);

    System.out.println(table.getDescription());

    table.waitForActive();
  }
}

