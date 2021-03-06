package homework.five;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.List;

public class querytest {
  public static void main(String[] args) {
    var mongoClient = new MongoClient("localhost", 27017);
    var db = mongoClient.getDatabase("moviedatabase");
//    var movies = db.getCollection("movies");
    var actors = db.getCollection("actors");


    var result = actors.find(Filters.eq("name", "Tom Hanks")).iterator();
    System.out.println("Tom Hanks has played");
    var tomCharacters = (List<Document>) result.next().get("characters");
    for (var ch : tomCharacters) {
      System.out.printf("\t\"%s\" in \"%s\"%n", ch.get("character"), ch.get("movieTitle"));
    }

//    result = movies.find(Filters.elemMatch(
//        "characters", Filters.eq("actorId", "162655641"))).iterator();
//    HashSet<String> uniqueCostars = new HashSet<String>();
//    System.out.println("Tom Hanks' co-stars are");
//    while (result.hasNext()) {
//      Document movie = result.next();
//      for (Document ch : (List<Document>) movie.get("characters")) {
//        if (!ch.get("actorId").equals("162655641")) {
//          Document costarInfo = actors.find(Filters.eq("id", ch
//              .get("actorId"))).iterator().next();
//          if (!uniqueCostars.contains(String.format("%s%s", movie.get("title"),
//              costarInfo.get("name")))) {
//            System.out.printf("\t%-25sin\t%s%n", costarInfo.get("name"),
//                movie
//                .get("title"));
//          }
//          uniqueCostars.add(String.format("%s%s", movie.get("title"),
//              costarInfo.get("name")));
//        }
//      }
//    }

    mongoClient.close();
  }
}
