package homework.six;

import java.util.List;

/**
 * Class that maps the rating object in the json files to a Java object.
 */
public class Cast {
  private String name;
  private String id;
  private List<String> characters;

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

  public List<String> getCharacters() {
    return characters;
  }

  public void setCharacters(List<String> characters) {
    this.characters = characters;
  }
}
