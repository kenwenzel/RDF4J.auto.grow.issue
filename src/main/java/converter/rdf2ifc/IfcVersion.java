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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chi The class to manage mappings between IFC verions and namespaces of ifcOWL.
 */
public class IfcVersion {

  /** */
  private String label;

  /**
   * @param label
   */
  public IfcVersion(String label) {
    this.label = label;
  }

  public static final IfcVersion IFC2X3_TC1 = new IfcVersion("IFC2X3_TC1");

  public static final IfcVersion IFC2X3_FINAL = new IfcVersion("IFC2X3_Final");

  public static final IfcVersion IFC4 = new IfcVersion("IFC4");

  public static final IfcVersion IFC4X1_RC3 = new IfcVersion("IFC4x1_RC3");

  public static final IfcVersion IFC4_ADD1 = new IfcVersion("IFC4_ADD1");

  public static final IfcVersion IFC4_ADD2 = new IfcVersion("IFC4_ADD2");

  public static Map<IfcVersion, String> IfcNSMap = new HashMap<IfcVersion, String>();

  public static Map<String, IfcVersion> NSIfcMap = new HashMap<String, IfcVersion>();

  /**
   * @return label of the IFC version
   */
  public String getLabel() {
    return label;
  }

  /**
   * Get required IFC version.
   *
   * @param versionName Name of the IFC version. It can be (ignore case) "IFC2X3_TC1",
   *     "IFC2X3_Final", "IFC4", "IFC4x1_RC3", "IFC4_ADD1","IFC4_ADD2".
   * @return
   * @throws IfcVersionException
   */
  public static IfcVersion getIfcVersion(String versionName) throws IfcVersionException {
    IfcVersion[] versions = {IFC2X3_TC1, IFC2X3_FINAL, IFC4, IFC4X1_RC3, IFC4_ADD1, IFC4_ADD2};
    for (IfcVersion v : versions) {
      if (v.getLabel().equalsIgnoreCase(versionName)) {
        return v;
      }
    }
    throw new IfcVersionException("Cannot find required IFC version: " + versionName);
  }

  /**
   * Intialize mapping between IFC versions and namespaces defined in nl.tue.ddss.convert.Namespace
   * class.
   */
  public static void initDefaultIfcNsMap() {
    IfcNSMap.put(IFC2X3_TC1, Namespace.IFC2X3_TC1);
    IfcNSMap.put(IFC2X3_FINAL, Namespace.IFC2X3_FINAL);
    IfcNSMap.put(IFC4, Namespace.IFC4);
    IfcNSMap.put(IFC4X1_RC3, Namespace.IFC4X1_RC3);
    IfcNSMap.put(IFC4_ADD1, Namespace.IFC4_ADD1);
    IfcNSMap.put(IFC4_ADD2, Namespace.IFC4_ADD2);
  }

  /**
   * Intialize mapping between namespaces defined in nl.tue.ddss.convert.Namespace class and IFC
   * versions.
   *
   * @throws IfcVersionException
   */
  public static void initDefaultNsIfcMap() throws IfcVersionException {
    NSIfcMap.put(Namespace.IFC2X3_TC1, IFC2X3_TC1);
    NSIfcMap.put(Namespace.IFC2X3_FINAL, IFC2X3_FINAL);
    NSIfcMap.put(Namespace.IFC4, IFC4);
    NSIfcMap.put(Namespace.IFC4X1_RC3, IFC4X1_RC3);
    NSIfcMap.put(Namespace.IFC4_ADD1, IFC4_ADD1);
    NSIfcMap.put(Namespace.IFC4_ADD2, IFC4_ADD2);
  }

  /**
   * Intialize mapping between IFC versions and namespaces defined in ifcOWL files (*.ttl) in the
   * resources folder.
   *
   * @throws IfcVersionException
   */
  public static void initIfcNsMap() throws IfcVersionException {
    IfcNSMap.put(IFC2X3_TC1, loadNamespace(IFC2X3_TC1));
    IfcNSMap.put(IFC2X3_FINAL, loadNamespace(IFC2X3_FINAL));
    IfcNSMap.put(IFC4, loadNamespace(IFC4));
    IfcNSMap.put(IFC4X1_RC3, loadNamespace(IFC4X1_RC3));
    IfcNSMap.put(IFC4_ADD1, loadNamespace(IFC4_ADD1));
    IfcNSMap.put(IFC4_ADD2, loadNamespace(IFC4_ADD2));
  }

  /**
   * Intialize mapping betweennamespaces defined in ifcOWL files (*.ttl) in the resources folder and
   * IFC versions.
   *
   * @throws IfcVersionException
   */
  public static void initNsIfcMap() throws IfcVersionException {
    NSIfcMap.put(loadNamespace(IFC2X3_TC1), IFC2X3_TC1);
    NSIfcMap.put(loadNamespace(IFC2X3_FINAL), IFC2X3_FINAL);
    NSIfcMap.put(loadNamespace(IFC4), IFC4);
    NSIfcMap.put(loadNamespace(IFC4X1_RC3), IFC4X1_RC3);
    NSIfcMap.put(loadNamespace(IFC4_ADD1), IFC4_ADD1);
    NSIfcMap.put(loadNamespace(IFC4_ADD2), IFC4_ADD2);
  }

  /**
   * Load updated namespace from corresponding ifcOWL file.
   *
   * @param ifcVersion
   * @return updated namespace
   * @throws IfcVersionException
   */
  private static String loadNamespace(IfcVersion ifcVersion) throws IfcVersionException {
    InputStream in =
        IfcVersion.class.getClassLoader().getResourceAsStream(ifcVersion.getLabel() + ".ttl");
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        line = line.replace(" ", "");
        if (line.startsWith("@prefixifc:") || line.startsWith("@prefixifcowl:")) {
          return line.substring(line.indexOf("<") + 1, line.indexOf(">"));
        }
      }
    } catch (IOException e) {
      throw new IfcVersionException(
          "Cannot find name space for "
              + ifcVersion.getLabel()
              + ", please check location and format of ifcOWL ontology files");
    }
    throw new IfcVersionException(
        "Cannot find name space for "
            + ifcVersion.getLabel()
            + ", please check location and format of ifcOWL ontology files");
  }

  /**
   * Get required IFC version
   *
   * @param header header data parsed from IFC STEP file
   * @return
   * @throws IfcVersionException
   */
  public static IfcVersion getIfcVersion(Header header) throws IfcVersionException {
    String strLine = header.getSchema_identifiers().toString();
    if (strLine.length() > 0) {
      if (strLine.indexOf("IFC2X3") != -1) return IFC2X3_TC1;
      if (strLine.indexOf("IFC4X1") != -1) return IFC4X1_RC3;
      if (strLine.indexOf("IFC4") != -1) return IFC4_ADD2;
    }
    throw new IfcVersionException("Cannot determine which IFC version the model it is");
  }
}
