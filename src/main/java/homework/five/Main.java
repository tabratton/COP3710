package homework.five;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

  public static MongoCollection<Document> movies;
  public static MongoCollection<Document> actors;
  public static HashMap<String, String> allActors;

  /**
   * Reads all json files from movies folder and adds their data to the
   * database.
   *
   * @param args No command line arguments.
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    String currentDir = System.getProperty("user.dir");
    String jsonFilesDir = currentDir + "\\movies";
    MongoClient mongoClient = new MongoClient("localhost", 27017);
    MongoDatabase db = mongoClient.getDatabase("moviedatabase");
    movies = db.getCollection("movies");
    actors = db.getCollection("actors");
    movies.drop();
    actors.drop();
    allActors = new HashMap<String, String>();

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    long numFilesToParse = Files.list(Paths.get(jsonFilesDir)).count();
    for (long i = 1; i <= numFilesToParse; i++) {
      Movies movieArray = mapper.readValue(new File(jsonFilesDir + "\\page"
          + i + ".json"), Movies.class);
      Database.insertMovies(movieArray);
    }

    Set<String> actorId = allActors.keySet();
    for (String id : actorId) {
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
    List<Document> tomCharacters = (List<Document>) actors.find(Filters.eq(
        "name", "Tom Hanks")).iterator().next().get("characters");
    // Print all character/movie info.
    for (Document ch : tomCharacters) {
      System.out.printf("\t\"%s\" in \"%s\"%n", ch.get("character"), ch.get(
          "movieTitle"));
    }
  }

  public static void findHanksCoStars() {
    // Find all movies that Tom Hanks is in.
    MongoCursor<Document> result = movies.find(Filters.elemMatch(
        "characters", Filters.eq("actorId", "162655641"))).iterator();
    HashSet<String> uniqueCostars = new HashSet<String>();
    System.out.println("Tom Hanks' co-stars are");
    // Go through all movies.
    while (result.hasNext()) {
      Document movie = result.next();
      // Go through the characters in each movie.
      for (Document ch : (List<Document>) movie.get("characters")) {
        // If the character is not a Tom Hanks character.
        if (!ch.get("actorId").equals("162655641")) {
          // Find the corresponding actor info for that character.
          Document costarInfo = actors.find(Filters.eq("id", ch.get(
              "actorId"))).iterator().next();
          // If that actor and movie combination has not already been seen.
          if (!uniqueCostars.contains(String.format("%s%s", movie.get("title"),
              costarInfo.get("name")))) {
            // Print info.
            System.out.printf("\t%-25sin\t%s%n", costarInfo.get("name"),
                movie.get("title"));
          }
          // Add actor and movie combination to the set of already discovered
          // co stars.
          uniqueCostars.add(String.format("%s%s", movie.get("title"),
              costarInfo.get("name")));
        }
      }
    }
  }
}


