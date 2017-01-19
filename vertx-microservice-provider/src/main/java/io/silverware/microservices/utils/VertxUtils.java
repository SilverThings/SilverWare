/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2016 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */

package io.silverware.microservices.utils;

import io.silverware.microservices.annotations.Deployment;
import io.silverware.microservices.enums.VerticleType;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public abstract class VertxUtils {

   private static final Logger log = LogManager.getLogger(VertxUtils.class);

   private static final Set<String> forbiddenVerticles = new HashSet<>();

   static {
      //load the forbidden verticles from the XML file
      NodeList verticleNodeList = getVerticlesFromXML(VertxConstants.FORBIDDEN_VERTICLES);

      if (verticleNodeList != null) {
         for (int i = 0; i < verticleNodeList.getLength(); i++) {
            forbiddenVerticles.add(verticleNodeList.item(i).getTextContent());
         }
      }
   }

   public static Set<String> getForbiddenVerticles() {
      return Collections.unmodifiableSet(forbiddenVerticles);
   }

   /**
    * Extracts the {@link DeploymentOptions} from {@link Deployment} annotation
    *
    * @param deploymentAnnotation {@link Deployment} annotation
    * @return {@link DeploymentOptions} for the given annotation
    */
   public static DeploymentOptions getDeploymentOptionsFromAnnotation(final Deployment deploymentAnnotation) {

      DeploymentOptions result = new DeploymentOptions();

      //if no deployment annotation is present return the default settings
      if (deploymentAnnotation == null) {
         return result;
      }

      //verticle type
      if (deploymentAnnotation.type() == VerticleType.WORKER) {
         result.setWorker(true);
      } else if (deploymentAnnotation.type() == VerticleType.MULTI_THREADED_WORKER) {
         result.setMultiThreaded(true);
      }

      //number of instances
      if (deploymentAnnotation.instances() > 1) {
         result.setInstances(deploymentAnnotation.instances());
      }

      //verticle isolation group
      if (!deploymentAnnotation.isolationGroup().isEmpty()) {
         result.setIsolationGroup(deploymentAnnotation.isolationGroup());
      }

      //verticle isolated classes
      if (deploymentAnnotation.isolatedClasses().length != 0) {
         result.setIsolatedClasses(Arrays.asList(deploymentAnnotation.isolatedClasses()));
      }

      //verticle extra classpath
      if (deploymentAnnotation.extraClasspath().length != 0) {
         result.setExtraClasspath(Arrays.asList(deploymentAnnotation.extraClasspath()));
      }

      //verticle high availability
      if (deploymentAnnotation.ha()) {
         result.setHa(true);
      }

      //verticle JSON config
      String jsonFileName = deploymentAnnotation.config();

      if (!jsonFileName.isEmpty()) {
         try (FileReader fileReader = new FileReader(jsonFileName)) {
            String jsonContent = IOUtils.toString(fileReader);
            result.setConfig(new JsonObject(jsonContent));
         } catch (Exception ex) {
            log.error("Invalid json config file: ", ex);
         }
      }

      return result;
   }

   /**
    * Extracts the {@link DeploymentOptions} from the xml element
    *
    * @param verticleElement verticle element
    * @return the {@link DeploymentOptions} for the given verticle element
    */
   public static DeploymentOptions getDeploymentOptionsFromXml(final Node verticleElement) {

      DeploymentOptions result = new DeploymentOptions();
      NamedNodeMap attributes = verticleElement.getAttributes();

      for (int i = 0; i < attributes.getLength(); i++) {

         //verticle type
         if (attributes.getNamedItem(VertxConstants.TYPE) != null) {
            if (attributes.getNamedItem(VertxConstants.TYPE).getNodeValue().equals(VertxConstants.TYPE_WORKER)) {
               result.setWorker(true);
            } else if (attributes.getNamedItem(VertxConstants.TYPE).getNodeValue().equals(VertxConstants.TYPE_MULTI_THREADED_WORKER)) {
               result.setMultiThreaded(true);
            }
         }

         //number of instances
         if (attributes.getNamedItem(VertxConstants.INSTANCES) != null) {
            result.setInstances(Integer.parseInt(attributes.getNamedItem(VertxConstants.INSTANCES).getNodeValue()));
         }

         //verticle isolation group
         if (attributes.getNamedItem(VertxConstants.ISOLATION_GROUP) != null) {
            result.setIsolationGroup(attributes.getNamedItem(VertxConstants.ISOLATION_GROUP).getNodeValue());
         }

         //verticle isolated classes
         if (attributes.getNamedItem(VertxConstants.ISOLATED_CLASSES) != null) {
            result.setIsolatedClasses(Arrays.asList(attributes.getNamedItem(VertxConstants.ISOLATED_CLASSES).getNodeValue().split(" ")));
         }

         //verticle extra classpath
         if (attributes.getNamedItem(VertxConstants.EXTRA_CLASSPATH) != null) {
            result.setExtraClasspath(Arrays.asList(attributes.getNamedItem(VertxConstants.EXTRA_CLASSPATH).getNodeValue().split(" ")));
         }

         //verticle high availability
         if (attributes.getNamedItem(VertxConstants.HA) != null) {
            result.setHa(Boolean.valueOf(attributes.getNamedItem(VertxConstants.HA).getNodeValue()));
         }

         //verticle JSON config
         if (attributes.getNamedItem(VertxConstants.CONFIG) != null) {
            try (FileReader fileReader = new FileReader(attributes.getNamedItem(VertxConstants.CONFIG).getNodeValue())) {
               String jsonContent = IOUtils.toString(fileReader);
               result.setConfig(new JsonObject(jsonContent));
            } catch (Exception ex) {
               log.error("Invalid json config file: ", ex);
            }
         }
      }

      return result;
   }

   /**
    * Return the {@link String} representing the given {@link DeploymentOptions}
    *
    * @param options {@link DeploymentOptions} to be printed
    * @return {@link String} representing the given options
    */
   public static String printDeploymentOptions(DeploymentOptions options) {
      StringBuilder sb = new StringBuilder("{ ");
      sb.append("type = ");
      if (options.isWorker()) {
         sb.append(VerticleType.WORKER);
      } else if (options.isMultiThreaded()) {
         sb.append(VerticleType.MULTI_THREADED_WORKER);
      } else {
         sb.append(VerticleType.STANDARD);
      }

      sb.append(", instances = ").append(options.getInstances());
      sb.append(", isolationGroup = ").append(options.getIsolationGroup() == null ? VertxConstants.NOT_AVAILABLE : options.getIsolationGroup());
      sb.append(", isolatedClasses = ").append(options.getIsolatedClasses() == null ? VertxConstants.NOT_AVAILABLE : options.getIsolatedClasses());
      sb.append(", extraClasspath = ").append(options.getExtraClasspath() == null ? VertxConstants.NOT_AVAILABLE : options.getExtraClasspath());
      sb.append(", ha = ").append(options.isHa());
      sb.append(", config = ").append(options.getConfig() == null ? VertxConstants.NOT_AVAILABLE : options.getConfig()).append(" }");
      return sb.toString();
   }

   /**
    * Extracts the verticle elements from the XML file
    *
    * @param fileName the name of the file
    * @return {@link NodeList} of verticle elements from the file
    */
   public static NodeList getVerticlesFromXML(String fileName) {

      URL configURL = VertxUtils.class.getClassLoader().getResource(fileName);

      if (configURL == null) {
         return null;
      }

      try {
         if (!validateAgainstXSD(configURL)) {
            return null;
         }

         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
         Document document = documentBuilder.parse(configURL.toString());

         document.getDocumentElement().normalize();

         return document.getElementsByTagName("verticle");

      } catch (IOException | SAXException | ParserConfigurationException ex) {
         log.error("Error loading configuration file " + fileName, ex);
      }

      return null;
   }

   private static boolean validateAgainstXSD(URL url) {
      try {
         URL schemaURL = ClassLoader.getSystemResource(VertxConstants.VERTX_SCHEMA);
         SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
         Schema schema = factory.newSchema(schemaURL);
         Validator validator = schema.newValidator();
         validator.validate(new StreamSource(url.toString()));

         return true;
      } catch (SAXException | IOException ex) {
         log.error("Cannot validate configuration file " + url.getFile(), ex);
         return false;
      }
   }
}
