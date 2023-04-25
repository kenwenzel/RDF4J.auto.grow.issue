package converter;

import converter.rdf2ifc.IFC2RDFConverter;
import converter.rdf2ifc.RDF2IFCConverter;
import java.io.ByteArrayOutputStream;
import org.apache.jena.riot.Lang;

public class IfcConverter {
  public String fromRdf(String inputModel) throws Exception {
    RDF2IFCConverter rdf2IFCConverter = new RDF2IFCConverter();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    rdf2IFCConverter.convert(inputModel, outputStream, Lang.TURTLE);
    String ifcModel = outputStream.toString();
    return ifcModel;
  }

  public String toRdf(String inputModel) throws Exception {
    IFC2RDFConverter ifc2RDFConverter = new IFC2RDFConverter();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ifc2RDFConverter.convert(inputModel, outputStream, null, null, null, false, false, false);
    String rdfModel = outputStream.toString();
    return rdfModel;
  }
}
