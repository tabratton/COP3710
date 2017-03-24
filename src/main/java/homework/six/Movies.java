package homework.six;

import java.util.ArrayList;

/**
 * Class that maps the Movies object in the json file to
 * an ArrayList of movie objects.
 */
public class Movies {
  private ArrayList<MovieObject> movies;

  public ArrayList<MovieObject> getMovies() {
    return movies;
  }

  public void setMovies(ArrayList<MovieObject> movies) {
    this.movies = movies;
  }
}
