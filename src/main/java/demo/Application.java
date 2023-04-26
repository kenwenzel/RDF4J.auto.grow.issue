package demo;

import converter.IfcConverter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import utils.RepositoryUtils;

public class Application {
  public static final String RDF_4_J_SERVER = "http://localhost:8080/rdf4j-server";

  public static final String IFC_FILE = "Week_37_11_sept_IFC_Schependomlaan_incl_planningsdata.ifc";
  public static final String IFC_FILE_UPDATED =
      "Week_37_11_sept_IFC_Schependomlaan_incl_planningsdata2.ifc";
  public static final String IFC_PATH = "./IFC/" + IFC_FILE;
  public static final String IFC_PATH_UPDATED = "./IFC/" + IFC_FILE_UPDATED;
  public static final String REPOSITORY_ID = "demo_ifc";

  public static void main(String[] args) {
    RepositoryConnection connection = null;

    try {
      // ifc model
      logMessage("Loading IFC model");
      String ifcModel = Files.readString(Path.of(IFC_PATH), Charset.defaultCharset());
      if (ifcModel == null) {
        logMessage("ifcModel is null");
        return;
      }

      logMessage("Converting ifc model to rdf");
      IfcConverter converter = new IfcConverter();
      String rdfModel = converter.toRdf(ifcModel);

      RepositoryUtils repositoryUtils = new RepositoryUtils(RDF_4_J_SERVER);
      if (!repositoryUtils.exists(REPOSITORY_ID)) {
        logMessage("Creating repository");
        repositoryUtils.createRepository(REPOSITORY_ID);
      }

      logMessage("Writing named graph");
      String graph = "http://example.org/" + IFC_FILE;
      connection = repositoryUtils.getConnection(REPOSITORY_ID);

      logMessage("First transaction start");
      connection.begin();

      var factory = SimpleValueFactory.getInstance();
      IRI context = factory.createIRI(graph);
      InputStream inputStream = new ByteArrayInputStream(rdfModel.getBytes());
      connection.add(inputStream, RDFFormat.TURTLE, context);

      logMessage("Committing first transaction");
      connection.commit();
      logMessage("First transaction committed");

      // updated ifc model
      logMessage("Loading updated ifc model");
      String ifcModelUpdated =
          Files.readString(Path.of(IFC_PATH_UPDATED), Charset.defaultCharset());
      if (ifcModelUpdated == null) {
        logMessage("ifcModelUpdated is null");
        return;
      }

      logMessage("Converting updated ifc model to rdf");
      String rdfModelUpdated = converter.toRdf(ifcModelUpdated);

      logMessage("Writing updated named graph");
      connection.begin();

      // delete context
      connection.clear(context);

      inputStream = new ByteArrayInputStream(rdfModelUpdated.getBytes());
      connection.add(inputStream, RDFFormat.TURTLE, context);

      logMessage("Committing second transaction");
      connection.commit();
      logMessage("Second transaction committed");

      connection.close();

    } catch (Exception e) {
      e.printStackTrace();
      if (connection != null) {
        connection.close();
      }
      throw new RuntimeException(e);
    }
  }

  public static void logMessage(String message) {
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    String formattedDateTime = now.format(formatter);
    System.out.println(formattedDateTime + " " + message);
  }
}
