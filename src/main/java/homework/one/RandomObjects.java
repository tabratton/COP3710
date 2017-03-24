package homework.one;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

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

  public static void main(String[] args) throws JAXBException,
      JsonParseException, JsonMappingException, IOException {

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    RandomObject[] objArray = mapper.readValue(new File("random.json"),
        RandomObject[].class);

    List<RandomObject> randomObjects = Arrays.asList(objArray);
    RandomObjects objs = new RandomObjects();
    objs.setRandomList(randomObjects);

    JAXBContext context = JAXBContext.newInstance(RandomObjects.class);
    Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    // Write to File
    marshaller.marshal(objs, new File("random.xml"));
  }
}
