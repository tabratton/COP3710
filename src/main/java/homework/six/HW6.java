package homework.six;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.io.fs.FileUtils;

public class HW6 {
  private static final String DB_PATH = "target/movies-db";
  private static GraphDatabaseService graphDb;

  public static void main(String[] args) {
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
    var currentDir = System.getProperty("user.dir");
    var jsonFilesDir = currentDir + "\\movies";
    try (var tx = graphDb.beginTx()) {
      var mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);

      var numFilesToParse = Files.list(Paths.get(jsonFilesDir)).count();
      // Read JSON files.
      for (var i = 1; i <= numFilesToParse; i++) {
        var movieArray = mapper.readValue(new File(jsonFilesDir + "\\page"
            + i + ".json"), Movies.class);
        // Add movie node and actor nodes for that movie.
        for (var movie : movieArray.getMovies()) {
          var newMovie = graphDb.createNode(Labels.MOVIE);
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
          for (var cast : movie.getAbridged_cast()) {
            // If the actor does not exist in the graph already, add them.
            var actor = graphDb.findNode(Labels.ACTOR, "name", cast.getName());
            if (actor == null) {
              actor = graphDb.createNode(Labels.ACTOR);
              actor.setProperty("name", cast.getName());
              actor.setProperty("id", cast.getId());
            }
            // Now that the actor is in the graph, add relationships to the
            // movie representing all the characters they played.
            if (cast.getCharacters() != null) {
              for (var character : cast.getCharacters()) {
                var relationship = actor.createRelationshipTo(newMovie, RelationshipTypes.IN);
                relationship.setProperty("plays", character);
              }
              // If they did not play "characters" and are instead credited as
              // themselves, add the relationship as them playing themselves.
            } else {
              var relationship = actor.createRelationshipTo(newMovie, RelationshipTypes.IN);
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
    try (var tx = graphDb.beginTx()) {
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
    try (var tx = graphDb.beginTx()) {
      // Simply search for all nodes that match the label and property/value
      // pair.
      var nodes = graphDb.findNodes(Labels.MOVIE, "year", "" + year);
      while (nodes.hasNext()) {
        var movie = nodes.next();
        System.out.println(movie.getProperty("title"));
      }
      tx.success();
    }
  }

  private static void printCoStars_Java(String name) {
    try (var tx = graphDb.beginTx()) {
      // Find the starting node.
      var actor = graphDb.findNodes(Labels.ACTOR, "name", name).next();
      // Set traverse to a 2 deep breadth first search where the relationship
      // goes (start)-->(movie)<--(end). (end) will be a costar of (start).
      var traverser = graphDb.traversalDescription()
          .breadthFirst()
          .evaluator(Evaluators.toDepth(2))
          .relationships(RelationshipTypes.IN, Direction.INCOMING)
          .relationships(RelationshipTypes.IN, Direction.OUTGOING)
          .traverse(actor);
      // Print out all paths found that end in an actor. (Should be an
      // unnecessary check, but better to be safe than sorry.)
      for (var path : traverser) {
        if (path.length() > 0 && path.endNode().hasLabel(Labels.ACTOR)) {
          System.out.println(path.endNode().getProperty("name"));
        }
      }
      tx.success();
    }
  }

  private static void printMoviesIn_Cql(int year) {
    try (var tx = graphDb.beginTx()) {
      // Find all movies that match the given property. CQL seems nice.
      var result = graphDb.execute(String.format("MATCH (movie) WHERE"
          + " movie.year = '%d' RETURN movie.title;", year));
      while (result.hasNext()) {
        System.out.println(result.next().get("movie.title"));
      }
      tx.success();
    }
  }

  private static void printCoStars_Cql(String name) {
    try (var tx = graphDb.beginTx()) {
      // Same idea as in the Java version, but in my opinion much clearer and
      // cleaner in CQL.
      var result = graphDb.execute(String.format("MATCH (actor)-[:IN]->"
          + "(movie)<-[:IN]-(costar) WHERE actor.name = '%s' RETURN costar"
          + ".name;", name));
      while (result.hasNext()) {
        System.out.println(result.next().get("costar.name"));
      }
      tx.success();
    }
  }
}