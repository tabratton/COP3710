package homework.three;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

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
    var database = new Database(currentDir);
    database.createDatabase();

    var mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    var numFilesToParse = Files.list(Paths.get(jsonFilesDir)).count();
    for (var i = 1; i <= numFilesToParse; i++) {
      var movieArray = mapper.readValue(new File(jsonFilesDir + "\\page"
              + i + ".json"), Movies.class);
      database.insertEntries(movieArray);
    }

    database.closeConnection();
  }

}
