package homework.six;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.io.fs.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HW6 {
  private static final String DB_PATH = "target/movies-db";
  private static GraphDatabaseService graphDb;

  public static void main(String[] args)
      throws JsonParseException, JsonMappingException, IOException {

    createDb(); // creating a graph database in DB_PATH
    load(); // loading the movie data from the json files in folder "movies"
    doQueries(); // do some queries
    shutDown(); // shuts down the database
  }

  private static void createDb() {
    try {
      FileUtils.deleteRecursively(new File(DB_PATH));
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }

    graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(DB_PATH));
  }

  private static void load() {
    String currentDir = System.getProperty("user.dir");
    String jsonFilesDir = currentDir + "\\movies";
    try (Transaction tx = graphDb.beginTx()) {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
          false);

      long numFilesToParse = Files.list(Paths.get(jsonFilesDir)).count();
      // Read JSON files.
      for (long i = 1; i <= numFilesToParse; i++) {
        Movies movieArray = mapper.readValue(new File(jsonFilesDir + "\\page"
            + i + ".json"), Movies.class);
        // Add movie node and actor nodes for that movie.
        for (MovieObject movie : movieArray.getMovies()) {
          Node newMovie = graphDb.createNode(Labels.MOVIE);
          // Set properties.
          newMovie.setProperty("id", movie.getId());
          newMovie.setProperty("title", movie.getTitle());
          newMovie.setProperty("year", movie.getYear());
          newMovie.setProperty("mpaa_rating", movie.getMpaa_rating());
          newMovie.setProperty("critic_score", movie.getRatings()
              .getCritics_score());
          newMovie.setProperty("audience_score", movie.getRatings()
              .getAudience_score());
          // Add actors and relationships to movies.
          for (Cast cast : movie.getAbridged_cast()) {
            // If the actor does not exist in the graph already, add them.
            Node actor = graphDb.findNode(Labels.ACTOR, "name",
                cast.getName());
            if (actor == null) {
              actor = graphDb.createNode(Labels.ACTOR);
              actor.setProperty("name", cast.getName());
              actor.setProperty("id", cast.getId());
            }
            // Now that the actor is in the graph, add relationships to the
            // movie representing all the characters they played.
            if (cast.getCharacters() != null) {
              for (String character : cast.getCharacters()) {
                Relationship relationship = actor.createRelationshipTo(newMovie,
                    RelationshipTypes.IN);
                relationship.setProperty("plays", character);
              }
            // If they did not play "characters" and are instead credited as
            // themselves, add the relationship as them playing themselves.
            } else {
              Relationship relationship = actor.createRelationshipTo(
                  newMovie, RelationshipTypes.IN);
              relationship.setProperty("plays", cast.getName());
            }
          }
        }
      }
      tx.success();
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }

  private static void shutDown() {
    graphDb.shutdown();
    System.out.println("\nShutting down database...");
  }

  private static void doQueries() {
    try (Transaction tx = graphDb.beginTx()) {

      System.out.println("--------- Movies in 1980 (Java)-----------");
      printMoviesIn_Java(1980);
      System.out.println("\n--------- Tautou's Co-Stars (Java)-----------");
      printCoStars_Java("Audrey Tautou");
      System.out.println("\n--------- Movies in 1980 (CQL)-----------");
      printMoviesIn_Cql(1980);
      System.out.println("\n--------- Tautou's Co-Stars (CQL)-----------");
      printCoStars_Cql("Audrey Tautou");

      tx.success();
    }
  }

  private static void printMoviesIn_Java(int year) {
    try (Transaction tx = graphDb.beginTx()) {
      // Simply search for all nodes that match the label and property/value
      // pair.
      ResourceIterator nodes = graphDb.findNodes(Labels.MOVIE, "year", ""
          + year);
      while (nodes.hasNext()) {
        Node movie = (Node) nodes.next();
        System.out.println(movie.getProperty("title"));
      }
      tx.success();
    }
  }

  private static void printCoStars_Java(String name) {
    try (Transaction tx = graphDb.beginTx()) {
      // Find the starting node.
      Node actor = graphDb.findNodes(Labels.ACTOR, "name", name).next();
      // Set traverse to a 2 deep breadth first search where the relationship
      // goes (start)-->(movie)<--(end). (end) will be a costar of (start).
      Traverser traverser = graphDb.traversalDescription()
          .breadthFirst()
          .evaluator(Evaluators.toDepth(2))
          .relationships(RelationshipTypes.IN, Direction.INCOMING)
          .relationships(RelationshipTypes.IN, Direction.OUTGOING)
          .traverse(actor);
      // Print out all paths found that end in an actor. (Should be an
      // unnecessary check, but better to be safe than sorry.)
      for (Path path : traverser) {
        if (path.length() > 0 && path.endNode().hasLabel(Labels.ACTOR)) {
          System.out.println(path.endNode().getProperty("name"));
        }
      }
      tx.success();
    }
  }

  private static void printMoviesIn_Cql(int year) {
    try (Transaction tx = graphDb.beginTx()) {
      // Find all movies that match the given property. CQL seems nice.
      Result result = graphDb.execute(String.format("MATCH (movie) WHERE"
          + " movie.year = '%d' RETURN movie.title;", year));
      while (result.hasNext()) {
        System.out.println(result.next().get("movie.title"));
      }
      tx.success();
    }
  }

  private static void printCoStars_Cql(String name) {
    try (Transaction tx = graphDb.beginTx()) {
      // Same idea as in the Java version, but in my opinion much clearer and
      // cleaner in CQL.
      Result result = graphDb.execute(String.format("MATCH (actor)-[:IN]->"
              + "(movie)<-[:IN]-(costar) WHERE actor.name = '%s' RETURN costar"
              + ".name;", name));
      while (result.hasNext()) {
        System.out.println(result.next().get("costar.name"));
      }
      tx.success();
    }
  }
}