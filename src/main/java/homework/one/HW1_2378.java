package homework.one;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class HW1_2378 {
  public static void main(String[] args) throws JAXBException, IOException {

    var mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    var objArray = mapper.readValue(new File("schools.json"), SchoolObject[].class);

    var schoolObjects = Arrays.asList(objArray);
    var objs = new SchoolObjects();
    objs.setSchoolList(schoolObjects);

    var context = JAXBContext.newInstance(SchoolObjects.class);
    var marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    // Write to File
    marshaller.marshal(objs, new File("schools.xml"));
  }
}
