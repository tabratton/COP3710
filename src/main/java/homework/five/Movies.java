package homework.five;

import java.util.List;

/**
 * Class that maps the Movies object in the json file to
 * an ArrayList of movie objects.
 */
public class Movies {
  private List<MovieObject> movies;

  public List<MovieObject> getMovies() {
    return movies;
  }

  public void setMovies(List<MovieObject> movies) {
    this.movies = movies;
  }
}
