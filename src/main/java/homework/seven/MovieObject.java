package homework.seven;

import java.util.List;

/**
 * Class that maps the movie object in the json files to a Java object.
 */
public class MovieObject {
  private String id;
  private String title;
  private String year;
  private String mpaa_rating;
  private Rating ratings;
  private List<Cast> abridged_cast;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public String getMpaa_rating() {
    return mpaa_rating;
  }

  public void setMpaa_rating(String mpaa_rating) {
    this.mpaa_rating = mpaa_rating;
  }

  public Rating getRatings() {
    return ratings;
  }

  public void setRatings(Rating ratings) {
    this.ratings = ratings;
  }

  public List<Cast> getAbridged_cast() {
    return abridged_cast;
  }

  public void setAbridged_cast(List<Cast> abridged_cast) {
    this.abridged_cast = abridged_cast;
  }
}
