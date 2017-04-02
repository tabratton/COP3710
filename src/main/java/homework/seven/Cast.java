package homework.seven;

import java.util.ArrayList;

/**
 * Class that maps the rating object in the json files to a Java object.
 */
public class Cast {
  private String name;
  private String id;
  private ArrayList<String> characters;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ArrayList<String> getCharacters() {
    return characters;
  }

  public void setCharacters(ArrayList<String> characters) {
    this.characters = characters;
  }
}
