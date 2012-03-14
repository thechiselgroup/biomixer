package org.thechiselgroup.biomixer.server.data.json;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ForceDirectedLayoutLoader {
    private static String getTagValue(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
                .getChildNodes();
        Node nValue = nlList.item(0);
        return nValue.getNodeValue();
    }

    public static String httpGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        if (conn.getResponseCode() != 200) {
            throw new IOException(conn.getResponseMessage());
        }

        // Buffer the result into a string
        BufferedReader rd = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        return sb.toString();
    }

    public static void main(String argv[]) {
        try {
            // Get the results from the REST services and use them
            String result = httpGet("http://rest.bioontology.org/bioportal/ontologies?apikey=e93c4767-4d45-46c3-8833-b55ed2f6a4c2");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(
                    new ByteArrayInputStream(result.getBytes("utf-8"))));
            doc.getDocumentElement().normalize();

            // Initialize the JSON string
            String json = "{\"nodes\":[";

            NodeList nList = doc.getElementsByTagName("ontologyBean");

            Integer ids[] = new Integer[nList.getLength()];
            int count = 0;
            int temp;
            for (temp = 0; temp < 25; temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    // get the data about mappings to see if there are any
                    // mappings
                    String url = "http://rest.bioontology.org/bioportal/virtual/mappings/stats/ontologies/"
                            + Integer.parseInt(getTagValue("ontologyId",
                                    eElement))
                            + "?apikey=e93c4767-4d45-46c3-8833-b55ed2f6a4c2";
                    result = httpGet(url);
                    Document docTemp = dBuilder
                            .parse(new InputSource(new ByteArrayInputStream(
                                    result.getBytes("utf-8"))));
                    docTemp.getDocumentElement().normalize();
                    NodeList tempList = docTemp
                            .getElementsByTagName("ontologyMappingStatistics");

                    // if mappings were found, add ontology to the list
                    if (tempList.getLength() > 0) {
                        if (count > 0) {
                            json += ",";
                        }

                        // get the total number of terms in the ontology
                        url = "http://rest.bioontology.org/bioportal/virtual/ontology/"
                                + Integer.parseInt(getTagValue("ontologyId",
                                        eElement))
                                + "/all?&pagesize=50&pagenum=1&apikey=e93c4767-4d45-46c3-8833-b55ed2f6a4c2";
                        result = httpGet(url);
                        Document docNum = dBuilder.parse(new InputSource(
                                new ByteArrayInputStream(result
                                        .getBytes("utf-8"))));
                        docNum.getDocumentElement().normalize();
                        NodeList numList = docNum.getElementsByTagName("page");
                        Node numNode = numList.item(0);
                        String number = "0";
                        if (numNode.getNodeType() == Node.ELEMENT_NODE) {

                            Element numElement = (Element) numNode;
                            number = getTagValue("numResultsTotal", numElement);
                        }

                        // add ontology to the list
                        ids[count] = Integer.parseInt(getTagValue("ontologyId",
                                eElement));
                        json += "{\"name\":\""
                                + getTagValue("displayLabel", eElement)
                                + "\", \"number\":" + number + "}";
                        count++;
                    }
                }
            }

            json += "],\"links\":[";
            for (int k = 0; k < ids.length; k++) {
                System.out.println(ids[k]);
            }
            // counter for commas
            int mappingsCount = 0;
            for (temp = 0; temp < count; temp++) {
                String url = "http://rest.bioontology.org/bioportal/virtual/mappings/stats/ontologies/"
                        + ids[temp]
                        + "?apikey=e93c4767-4d45-46c3-8833-b55ed2f6a4c2";
                ;
                result = httpGet(url);

                // get the data about mappings and add them to the list
                Document docTemp = dBuilder.parse(new InputSource(
                        new ByteArrayInputStream(result.getBytes("utf-8"))));
                docTemp.getDocumentElement().normalize();
                NodeList tempList = docTemp
                        .getElementsByTagName("ontologyMappingStatistics");

                for (int i = 0; i < tempList.getLength(); i++) {
                    Node tempNode = tempList.item(i);
                    if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element mapElement = (Element) tempNode;
                        boolean onTheList = false;
                        int pos = 0;
                        for (int j = 0; j < count; j++) {
                            if (Integer.parseInt(getTagValue("ontologyId",
                                    mapElement)) == ids[j]) {
                                onTheList = true;
                                pos = j;
                            }
                        }
                        if (onTheList) {
                            if (mappingsCount > 0) {
                                json += ",";
                            }
                            int value = Integer.parseInt(getTagValue(
                                    "totalMappings", mapElement));
                            int sourceMappings = Integer.parseInt(getTagValue(
                                    "sourceMappings", mapElement));
                            if (value == 0) {
                                value = 1;
                            }
                            json += "{\"source\":" + temp + ", \"target\":"
                                    + pos + ", \"value\":" + value
                                    + ", \"sourceMappings\":" + sourceMappings
                                    + "}";
                            mappingsCount++;
                        }
                    }

                }
            }

            json += "]}";
            System.out.println("Number of ontologies " + temp);

            // Write the string into a file
            // Create file
            // FileWriter fstream = new FileWriter("data.json");
            // BufferedWriter out = new BufferedWriter(fstream);
            // out.write(json);

            // Close the output stream
            // out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}