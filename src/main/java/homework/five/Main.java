package homework.five;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Main {

  public static MongoCollection<Document> movies;
  public static MongoCollection<Document> actors;
  public static Map<String, String> allActors;

  /**
   * Reads all json files from movies folder and adds their data to the
   * database.
   *
   * @param args No command line arguments.
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    var currentDir = System.getProperty("user.dir");
    var jsonFilesDir = currentDir + "\\movies";
    var mongoClient = new MongoClient("localhost", 27017);
    var db = mongoClient.getDatabase("moviedatabase");
    movies = db.getCollection("movies");
    actors = db.getCollection("actors");
    movies.drop();
    actors.drop();
    allActors = new HashMap<>();

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try (var files = Files.list(Paths.get(jsonFilesDir))) {
      files.forEach(file -> {
        try {
          var movieArray = mapper.readValue(file.toFile(), Movies.class);
          // Add information for each movie to the database.
          Database.insertMovies(movieArray);
        } catch (IOException ex) {
          System.out.println("Fail at " + file.toString());
        }
      });
    }

    var actorId = allActors.keySet();
    for (var id : actorId) {
      Database.insertActor(id, allActors.get(id));
    }

    findHanksCharacters();
    findHanksCoStars();

    mongoClient.close();
  }

  public static void findHanksCharacters() {
    System.out.println("Tom Hanks has played");
    // Find all movies that Tom Hanks has been in, along with the character
    // info.
    var tomCharacters = (List<Document>) actors.find(Filters.eq("name", "Tom Hanks"))
        .iterator()
        .next()
        .get("characters");
    // Print all character/movie info.
    for (var ch : tomCharacters) {
      System.out.printf("\t\"%s\" in \"%s\"%n", ch.get("character"), ch.get("movieTitle"));
    }
  }

  public static void findHanksCoStars() {
    // Find all movies that Tom Hanks is in.
    var result = movies.find(Filters.elemMatch(
        "characters", Filters.eq("actorId", "162655641"))).iterator();
    var uniqueCostars = new HashSet<String>();
    System.out.println("Tom Hanks' co-stars are");
    // Go through all movies.
    while (result.hasNext()) {
      var movie = result.next();
      // Go through the characters in each movie.
      for (var ch : (List<Document>) movie.get("characters")) {
        // If the character is not a Tom Hanks character.
        if (!ch.get("actorId").equals("162655641")) {
          // Find the corresponding actor info for that character.
          var costarInfo = actors.find(Filters.eq("id", ch.get("actorId"))).iterator().next();
          // If that actor and movie combination has not already been seen.
          if (!uniqueCostars.contains(String.format("%s%s", movie.get("title"), costarInfo.get("name")))) {
            // Print info.
            System.out.printf("\t%-25sin\t%s%n", costarInfo.get("name"), movie.get("title"));
          }

          // Add actor and movie combination to the set of already discovered
          // co stars.
          uniqueCostars.add(String.format("%s%s", movie.get("title"), costarInfo.get("name")));
        }
      }
    }
  }
}


