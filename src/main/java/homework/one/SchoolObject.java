package homework.one;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class SchoolObject {
  private String id;
  private String about;
  private String founded;
  private int likes;
  private String link;
  private Location location;
  private String name;
  private int talking_about_count;
  private String username;
  private String website;
  private int were_here_count;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAbout() {
    return about;
  }

  public void setAbout(String about) {
    this.about = about;
  }

  public String getFounded() {
    return founded;
  }

  public void setFounded(String founded) {
    this.founded = founded;
  }

  public int getLikes() {
    return likes;
  }

  public void setLikes(int likes) {
    this.likes = likes;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getTalking_about_count() {
    return talking_about_count;
  }

  public void setTalking_about_count(int talking_about_count) {
    this.talking_about_count = talking_about_count;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public int getWere_here_count() {
    return were_here_count;
  }

  public void setWere_here_count(int were_here_count) {
    this.were_here_count = were_here_count;
  }
}
