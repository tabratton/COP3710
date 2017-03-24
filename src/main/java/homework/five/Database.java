package homework.five;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Database {
  /**
   * Add all the movies in a Movies object to the MongoDB database.
   *
   * @param movies Movies object containing all movies to be added.
   */
  public static void insertMovies(Movies movies) {
    ArrayList<Document> documents = new ArrayList<Document>();
    // Iterate through all movies.
    for (MovieObject movie : movies.getMovies()) {
      Document doc = new Document();
      // Add basic information for each movie.
      doc.append("id", movie.getId());
      doc.append("mpaa_rating", movie.getMpaa_rating());
      doc.append("audience_score", movie.getRatings().getAudience_score());
      doc.append("critics_score", movie.getRatings().getCritics_score());
      doc.append("title", movie.getTitle());
      doc.append("year", movie.getYear());
      ArrayList<Document> characters = new ArrayList<Document>();
      // Go through the cast of the movie and ad the character information.
      for (int i = 0; i < movie.getAbridged_cast().size(); i++) {
        Cast cast = movie.getAbridged_cast().get(i);
        // Store actor information for later use.
        Main.allActors.put(cast.getId(), cast.getName());
        if (cast.getCharacters() != null) {
          for (String string : cast.getCharacters()) {
            Document document = new Document();
            document.append("actorId", cast.getId());
            document.append("character", string);
            characters.add(document);
          }
        }
      }
      doc.append("characters", characters);
      documents.add(doc);
    }
    Main.movies.insertMany(documents);
  }

  /**
   * Insert the actor with the given id and name into the MongoDB.
   *
   * @param id   The ID of the actor.
   * @param name The name of the actor.
   */
  public static void insertActor(String id, String name) {
    Document doc = new Document();
    // Add basic actor information.
    doc.append("id", id);
    doc.append("name", name);
    ArrayList<Document> characters = new ArrayList<Document>();
    // Find all movies that the actor has been in.
    MongoCursor<Document> result = Main.movies.find(Filters.elemMatch(
        "characters", Filters.eq("actorId", id))).iterator();
    // Go through all movies they're in and add basic movie/character
    // information.
    while (result.hasNext()) {
      Document movie = result.next();
      for (Document ch : (List<Document>) movie.get("characters")) {
        if (ch.get("actorId").equals(id)) {
          Document document = new Document();
          document.append("movieId", movie.get("id"));
          document.append("character", ch.get("character"));
          document.append("movieTitle", movie.get("title"));
          characters.add(document);
        }
      }
    }
    doc.append("characters", characters);
    Main.actors.insertOne(doc);
  }
}
