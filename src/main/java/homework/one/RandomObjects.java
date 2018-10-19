package homework.one;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;

//root tag is Random-Data
@XmlRootElement(name = "Random-Data")
@XmlAccessorType(XmlAccessType.FIELD)
public class RandomObjects {

  @XmlElementWrapper(name = "RandomObjects")
  @XmlElement(name = "RandomObject")
  private List<RandomObject> randomObjectList;

  public List<RandomObject> getRandomList() {
    return randomObjectList;
  }

  public void setRandomList(List<RandomObject> randomObjects) {
    this.randomObjectList = randomObjects;
  }

  public static void main(String[] args) throws JAXBException, IOException {

    var mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    var objArray = mapper.readValue(new File("random.json"), RandomObject[].class);

    var randomObjects = Arrays.asList(objArray);
    var objs = new RandomObjects();
    objs.setRandomList(randomObjects);

    var context = JAXBContext.newInstance(RandomObjects.class);
    var marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    // Write to File
    marshaller.marshal(objs, new File("random.xml"));
  }
}
