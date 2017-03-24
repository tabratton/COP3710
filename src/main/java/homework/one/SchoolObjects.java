package homework.one;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

// Root tag is School-Data
@XmlRootElement(name = "School-Data")
@XmlAccessorType(XmlAccessType.FIELD)
public class SchoolObjects {

  @XmlElementWrapper(name = "SchoolObjects")
  @XmlElement(name = "SchoolObject")
  private List<SchoolObject> schoolObjectList;

  public List<SchoolObject> getSchoolList() {
    return this.schoolObjectList;
  }

  public void setSchoolList(List<SchoolObject> schoolObjects) {
    this.schoolObjectList = schoolObjects;
  }
}
