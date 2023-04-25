package demo;

import converter.IfcConverter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import utils.RepositoryUtils;

public class Application {
  public static final String RDF_4_J_SERVER = "http://localhost:8080/rdf4j-server";

  public static final String path = "Week_37_11_sept_IFC_Schependomlaan_incl_planningsdata.ifc";
  public static final String IFC_PATH =
      "./IFC/" + path;


  public static void main(String[] args) {

    try {
      System.out.println("Loading IFC model...");
      String ifcModel = Files.readString(Path.of(IFC_PATH), Charset.defaultCharset());
      if (ifcModel == null) {
        System.out.println("ifcModel is null");
        return;
      }

      System.out.println("Converting ifc model to rdf");
      IfcConverter converter = new IfcConverter();
      String rdfModel = converter.toRdf(ifcModel);

      System.out.println("Writing named graph");

    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
