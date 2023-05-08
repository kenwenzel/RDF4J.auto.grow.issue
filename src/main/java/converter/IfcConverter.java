package converter;

import converter.rdf2ifc.IFC2RDFConverter;
import java.io.ByteArrayOutputStream;

public class IfcConverter {

  public String toRdf(String inputModel) throws Exception {
    IFC2RDFConverter ifc2RDFConverter = new IFC2RDFConverter();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ifc2RDFConverter.convert(inputModel, outputStream, null, null, null, false, false, false);
    String rdfModel = outputStream.toString();
    return rdfModel;
  }
}
