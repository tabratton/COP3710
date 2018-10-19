package homework.five;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an example that shows how to connect to a running mongodb server. In
 * addition, it also saves some objects to a database.
 */
public class MongoTest {
  @SuppressWarnings("deprecation")
  public static void main(String args[]) throws UnknownHostException {
    // connecting to a running mongodb server
    var mongoClient = new MongoClient("localhost", 27017);

    // creating/finding a database named "fgcu"
    var db = mongoClient.getDatabase("fgcu");

    // creating/finding a collection named "likes"
    var likes = db.getCollection("likes");

    // building a list of documents
    var documents = loadDocuments();

    // inserting the documents
    likes.insertMany(documents);

    // find all names under category "Education"
    System.out.println("All \"Education\" Category");
    var results = likes.find(Filters.eq("category", "Education")).iterator();
    while (results.hasNext()) {
      var result = results.next();
      System.out.println("\t" + result.get("name"));
    }

    System.out.println("All FGCU related");
    likes.find(Filters.and(Filters.lt("name", "FGCUZ"), Filters.gt("name", "FGCU")))
        .forEach(new Block<Document>() {

          public void apply(Document doc) {
            System.out.println("\t" + doc.get("name"));
          }

        });

    // disconnecting from the server
    mongoClient.close();
  }

  private static List<Document> loadDocuments() {
    var documents = new ArrayList<Document>();

    var doc = new Document();
    doc.append("category", "Education");
    doc.append("name", "College & University");
    var list = new ArrayList<Document>();
    list.add(new Document("name", "College & University"));
    doc.append("category_list", list);
    documents.add(doc);

    doc = new Document();
    doc.put("category", "Public figure");
    doc.append("name", "Azul The Eagle");
    list = new ArrayList<>();
    list.add(new Document("name", "Public Figure"));
    doc.append("category_list", list);
    documents.add(doc);

    doc = new Document();
    doc.append("category", "University");
    doc.append("name", "Parsons Lab, FGCU");
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Sports/recreation/activities");
    doc.append("name", "FGCU Intramural Sports");
    list = new ArrayList<>();
    list.add(new Document("name", "Sports & Recreation"));
    doc.append("category_list", list);
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Education");
    doc.append("name", "FGCU's ACE Program");
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Education");
    doc.append("name", "FGCU Week of Welcome");
    doc.append("category_list", list);
    documents.add(doc);

    doc = new Document();
    doc.append("category", "University");
    doc.append("name", "FGCU U.A. Whitaker College of Engineering");
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Education");
    doc.append("name", "Whitaker Center for STEM Education at Florida Gulf"
        + " Coast University");
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Education");
    doc.append("name", "Florida Gulf Coast University Student Support"
        + " Services");
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Sports/recreation/activities");
    doc.append("name", "FGCU Outdoor Pursuits");
    list = new ArrayList<>();
    list.add(new Document("name", "Outdoor Recreation"));
    doc.append("category_list", list);
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Education");
    doc.append("name", "FGCU New Student Programs");
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Education");
    doc.append("name", "Office of Undergraduate Research and Scholarship at FGCU");
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Education");
    doc.append("name", "Office of Community Outreach");
    list = new ArrayList<>();
    list.add(new Document("name", "College & University"));
    doc.append("category_list", list);
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Education");
    doc.append("name", "FGCU - Continuing Education");
    list = new ArrayList<>();
    list.add(new Document("name", "School"));
    list.add(new Document("name", "Classes"));
    doc.append("category_list", list);
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Local Business");
    doc.append("name", "Coastal Watershed Institute");
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Education");
    doc.append("name", "Journalism at FGCU");
    documents.add(doc);

    doc = new Document();
    doc.append("category", "University");
    doc.append("name", "Florida Gulf Coast University PGA Golf Management"
        + " Program");
    list = new ArrayList<>();
    list.add(new Document("name", "College & University"));
    doc.append("category_list", list);
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Education");
    doc.append("name", "Center for Environmental and Sustainability Education"
        + " at FGCU");
    list = new ArrayList<>();
    list.add(new Document("name", "Education"));
    doc.append("category_list", list);
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Education");
    doc.append("name", "FGCU Dean of Students' Office");
    list = new ArrayList<>();
    list.add(new Document("name", "Education"));
    doc.append("category_list", list);
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Organization");
    doc.append("name", "FGCU Leadership Development");
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Education");
    doc.append("name", "FGCU International Services Office");
    list = new ArrayList<>();
    list.add(new Document("name", "Education"));
    doc.append("category_list", list);
    documents.add(doc);

    doc = new Document();
    doc.append("category", "University");
    doc.append("name", "FGCU Graduate Studies");
    list = new ArrayList<>();
    list.add(new Document("name", "College & University"));
    doc.append("category_list", list);
    documents.add(doc);

    doc = new Document();
    doc.append("category", "University");
    doc.append("name", "FGCU Campus Recreation");
    list = new ArrayList<>();
    list.add(new Document("name", "College & University"));
    doc.append("category_list", list);
    documents.add(doc);

    doc = new Document();
    doc.append("category", "University");
    doc.append("name", "Chris Sale");
    documents.add(doc);

    doc = new Document();
    doc.append("category", "Restaurant/cafe");
    doc.append("name", "Eagle Dining");
    list = new ArrayList<>();
    list.add(new Document("name", "Restaurant"));
    doc.append("category_list", list);
    documents.add(doc);

    return documents;
  }
}

