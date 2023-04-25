/*******************************************************************************
 * Copyright 2017 Chi Zhang
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package converter.rdf2ifc;

import com.buildingsmart.tech.ifcowl.vo.EntityVO;
import com.buildingsmart.tech.ifcowl.vo.TypeVO;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Map;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

/**
 * The class to convert from IFC STEP file to RDF file.
 *
 * @author Chi Zhang
 */
public class IFC2RDFConverter {

  /** Default namespace for the output RDF file. */
  public final String DEFAULT_PATH = "http://linkedbuildingdata.net/ifc/resources/";

  /**
   * @param inputModel path of the IFC STEP file.
   * @param outputStream outputStream Output stream of the RDF file.
   * @param lang The generated RDF syntax. Supported formats are Turtle, N-triples. It is based on
   *     the StreamRDFWriter in Jena https://jena.apache.org/documentation/io/streaming-io.html.
   * @param ifcVersion Set the IFC version of the input IFC file, supported IFC versions are
   *     IFC2X3_TC1, IFC2X3_FINAL, IFC4, IFC4X1_RC3, IFC4_ADD1 and IFC4_ADD2. If it is null, the
   *     converter parses the header in IFC file to automatically determine the IFC version. Only
   *     three IFC versions are supported using this way, they are IFC2X3_TC1 (if header contains
   *     "IFC2X3"), IFC4x1_RC3 (if header contains "IFC4x1") and IFC4_ADD1 (if header contains
   *     "IFC4").
   * @param baseURI Namespace for the output RDF file. If it is null, the default value is
   *     "http://linkedbuildingdata.net/ifc/resources" + timeLog+"/".
   * @param expid Set whether generate express id as a separate property for instances. Default is
   *     false.
   * @param merge Set whether to remove duplicate objects. Default is false. If it is set to true,
   *     the round trip IFC file might have less objects.
   * @param updateNS Set whether to update namespace for the ifcOWL ontology. Defaut value is false.
   *     If it is true, the converter will look up the namespace from the corresponding ifcOWL file
   *     in the resources folder and use it. Therefore the corresponding ifcOWL file must be updated
   *     before, otherwise it will still use the old namespace.
   */
  @SuppressWarnings("unchecked")
  public void convert(
      String inputModel,
      OutputStream outputStream,
      Lang lang,
      String ifcVersion,
      String baseURI,
      boolean expid,
      boolean merge,
      boolean updateNS)
      throws Exception {
    if (baseURI == null) {
      baseURI = this.DEFAULT_PATH;
    }
    OntModel schema = null;
    InputStream in = null;
    if (updateNS) {
      IfcVersion.initIfcNsMap();
    } else {
      IfcVersion.initDefaultIfcNsMap();
    }
    IfcVersion version = null;
    Header header = HeaderParser.parseHeader(new ByteArrayInputStream(inputModel.getBytes()));
    if (ifcVersion != null) {
      version = IfcVersion.getIfcVersion(ifcVersion);
    } else {
      version = IfcVersion.getIfcVersion(header);
    }
    String ontNS = IfcVersion.IfcNSMap.get(version);
    // CONVERSION
    schema = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
    in =
        getClass()
            .getClassLoader()
            .getResourceAsStream("schema/ifc2rdf/" + version.getLabel() + ".ttl");
    Reader reader = new InputStreamReader(in);
    schema.read(reader, null, "TTL");
    String expressTtl = "schema/ifc2rdf/express.ttl";
    InputStream expressTtlStream = getClass().getClassLoader().getResourceAsStream(expressTtl);
    OntModel expressModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
    expressModel.read(expressTtlStream, null, "TTL");
    String rdfList = "schema/ifc2rdf/list.ttl";
    InputStream rdfListStream = getClass().getClassLoader().getResourceAsStream(rdfList);
    OntModel listModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
    listModel.read(rdfListStream, null, "TTL");
    schema.add(expressModel);
    schema.add(listModel);
    // Model im = om.getDeductionsModel();
    InputStream fis =
        getClass()
            .getClassLoader()
            .getResourceAsStream("schema/ifc2rdf/ent" + version.getLabel() + ".ser");
    ObjectInputStream ois = new ObjectInputStream(fis);
    Map<String, EntityVO> ent = null;
    ent = (Map<String, EntityVO>) ois.readObject();
    ois.close();
    fis =
        getClass()
            .getClassLoader()
            .getResourceAsStream("schema/ifc2rdf/typ" + version.getLabel() + ".ser");
    ois = new ObjectInputStream(fis);
    Map<String, TypeVO> typ = null;
    typ = (Map<String, TypeVO>) ois.readObject();
    ois.close();
    RDFWriter conv =
        new RDFWriter(
            schema,
            expressModel,
            listModel,
            new ByteArrayInputStream(inputModel.getBytes()),
            baseURI,
            ent,
            typ,
            ontNS);
    conv.setRemoveDuplicates(merge);
    conv.setExpIdAsProperty(expid);
    if (lang == null) {
      lang = RDFLanguages.TURTLE;
    }
    String s = "# baseURI: " + baseURI;
    s += "\r\n# imports: " + ontNS.substring(0, ontNS.length() - 1) + "\r\n\r\n";
    outputStream.write(s.getBytes());
    outputStream.flush();
    conv.parseModel2Stream(outputStream, header, lang);
    in.close();
  }
}
