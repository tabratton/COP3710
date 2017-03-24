package homework.three;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class Database {

  private Connection connection;

  /**
   * Initializes the Database object with a connection to interact with a
   * database.
   *
   * @param currentDir Current working directory.
   */
  public Database(String currentDir) {
    try {
      connection = DriverManager.getConnection("jdbc:h2:" + currentDir
          + "\\movies", "sa", "");
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
      System.exit(-1);
    }
  }

  /**
   * Closes database connection.
   */
  public void closeConnection() {
    try {
      connection.close();
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
      System.exit(-1);
    }
  }

  /**
   * Drops all tables if they exist and then creates them again.
   */
  public void createDatabase() {
    dropTables();
    createMovieTable();
    createActorTable();
    createCharacterTable();
  }

  /**
   * Inserts all entries found in a Movies object to the database.
   *
   * @param movies A Movies object that contains an ArrayList of MovieObjects.
   */
  public void insertEntries(Movies movies) {
    for (MovieObject movie : movies.getMovies()) {
      createMovieEntry(movie);
      createActorEntries(movie);
      createCharacterEntries(movie);
    }
  }

  private void createMovieEntry(MovieObject movie) {
    // Gather data from the MovieObject and create a statement String.
    String movieId = movie.getId();
    String title = movie.getTitle();
    String year = movie.getYear();
    String mpaaRating = movie.getMpaa_rating();
    int audienceScore = movie.getRatings().getAudience_score();
    int criticsScore = movie.getRatings().getCritics_score();
    String executeString = String.format("INSERT INTO MOVIE (id, title, year,"
            + " mpaa_rating, audience_score, critics_score) VALUES ('%s', '%s',"
            + " '%s', '%s', %d, %d)", movieId, title.replaceAll("'", "''"), year,
        mpaaRating, audienceScore, criticsScore);

    try {
      executeStatements(executeString);
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
  }

  private void createActorEntries(MovieObject movie) {
    // Get cast list from the movie object.
    ArrayList<Cast> actors = movie.getAbridged_cast();
    for (Cast actor : actors) {
      // Gather actor data from cast objects and create a statement String.
      String id = actor.getId();
      String name = actor.getName();
      String executeString = String.format("INSERT INTO ACTOR (id, name)"
          + " VALUES ('%s', '%s')", id, name.replaceAll("'", "''"));

      try {
        executeStatements(executeString);
      } catch (SQLException ex) {
        System.out.println(ex.getMessage());
      }
    }
  }

  private void createCharacterEntries(MovieObject movie) {
    // Get cast list from movie object.
    ArrayList<Cast> actors = movie.getAbridged_cast();
    for (Cast actor : actors) {
      // Get list of characters that the actor played (Could be more than one).
      ArrayList<String> characters = actor.getCharacters();
      // Check to make sure that they actually played a character (Could have
      // been none).
      if (characters != null) {
        for (String character : characters) {
          // Create a statement String for each character.
          String executeString = String.format("INSERT INTO CHARACTER"
                  + " (character, actor_id, movie_id) VALUES ('%s', '%s', '%s')",
              character.replaceAll("'", "''"), actor.getId(), movie.getId());

          try {
            executeStatements(executeString);
          } catch (SQLException ex) {
            System.out.println(ex.getMessage());
          }
        }
      }
    }
  }

  // Drops all tables used in this program.
  private void dropTables() {
    String dropCharacter = "DROP TABLE IF EXISTS character";
    String dropActor = "DROP TABLE IF EXISTS actor";
    String dropMovie = "DROP TABLE IF EXISTS movie";

    try {
      executeStatements(dropCharacter);
      executeStatements(dropActor);
      executeStatements(dropMovie);
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
  }

  // Creates movie table
  private void createMovieTable() {
    String executeString = "CREATE TABLE movie"
        + "("
        + "id varchar(100) NOT NULL UNIQUE,"
        + "title varchar(100),"
        + "year smallint,"
        + "mpaa_rating varchar(10),"
        + "audience_score smallint,"
        + "critics_score smallint,"
        + "CONSTRAINT pk_movie_id PRIMARY KEY (id)"
        + ")";

    try {
      executeStatements(executeString);
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

  }

  // Creates actor table.
  private void createActorTable() {
    String executeString = "CREATE TABLE actor"
        + "("
        + "id varchar(100) NOT NULL UNIQUE,"
        + "name varchar(100),"
        + "CONSTRAINT pk_actor_id PRIMARY KEY (id)"
        + ")";

    try {
      executeStatements(executeString);
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
  }

  // Creates character table.
  private void createCharacterTable() {
    String executeString = "CREATE TABLE character"
        + "("
        + "character varchar(100) NOT NULL,"
        + "actor_id varchar(100) NOT NULL,"
        + "movie_id varchar(100) NOT NULL,"
        + "CONSTRAINT pk_ch_ai_mi PRIMARY KEY (character, actor_id, movie_id),"
        + "CONSTRAINT fk_a_id FOREIGN KEY (actor_id) REFERENCES actor (id),"
        + "CONSTRAINT fk_m_id FOREIGN KEY (movie_id) REFERENCES movie (id)"
        + ")";

    try {
      executeStatements(executeString);
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
  }

  // Generic method to deal with all database updates to cut down on code
  // duplication.
  private void executeStatements(String execute) throws SQLException {
    PreparedStatement stmt = null;

    try {
      stmt = this.connection.prepareStatement(execute);
      stmt.executeUpdate();
    } catch (SQLException ex) {
      ex.getMessage();
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }
  }
}
