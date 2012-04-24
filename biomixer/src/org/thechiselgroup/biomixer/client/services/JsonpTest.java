package org.thechiselgroup.biomixer.client.services;

import java.io.Serializable;
import java.util.Map;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.json.TotoeJsonParser;
import org.thechiselgroup.biomixer.client.services.term.FullTermResponseJsonParser;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.client.workbench.util.url.JsonpUrlFetchService;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * XXX Temporary class for trying out stuff related to jsonp.
 * 
 * @author drusk
 * 
 */
public class JsonpTest extends GWTTestCase {

    public static void main(String[] args) {
    }

    @Override
    public String getModuleName() {
        return "org.thechiselgroup.biomixer.BioMixerWorkbench";
    }

    public void testJsonp() {
        // String url = new NcboJsonpRestUrlBuilderFactory()
        // .createUrlBuilder()
        // .uriParameter(
        // "path",
        // "/virtual/ontology/1487?conceptid="
        // + UriUtils
        // .encodeURIComponent("http://who.int/bodysystem.owl#BodySystem"))
        // .toString();
        // String url = new NcboJsonpRestUrlBuilderFactory().createUrlBuilder()
        // .parameter("path", "%2Fvirtual%2Fontology%2F1487").toString();

        // System.out.println(url);
        // String url =
        // "http://stage.bioontology.org/ajax/jsonp?path=%2Fvirtual%2Fontology%2F1516%3Flight%3D1%26norelations%3D1%26conceptid%3DO80-O84.9&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a";

        String url = "http://stage.bioontology.org/ajax/jsonp?path=%2Fvirtual%2Fontology%2F1070%3Fconceptid%3Dhttp%253A%252F%252Fpurl.org%252Fobo%252Fowl%252FGO%2523GO_0007569&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a";
        // &conceptid=http%3A%2F%2Fwho.int%2Fbodysystem.owl%23BodySystem

        // String encode = UriUtils.encodeURIComponent("body system");
        // System.out.println("Encode test: " + encode);

        JsonpUrlFetchService urlFetch = new JsonpUrlFetchService();
        urlFetch.fetchURL(url, new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                System.err.println("FAIL:\n" + caught.getMessage());
            }

            @Override
            public void onSuccess(String result) {

                FullTermResponseJsonParser parser = new FullTermResponseJsonParser(
                        new TotoeJsonParser());
                ResourceNeighbourhood neighbourhood = parser
                        .parseNeighbourhood("1516", result);
                System.out.println(neighbourhood.getResources().size());
                Map<String, Serializable> partialProperties = neighbourhood
                        .getPartialProperties();
                UriList childUris = (UriList) partialProperties
                        .get(Concept.CHILD_CONCEPTS);
                UriList parentUris = (UriList) partialProperties
                        .get(Concept.PARENT_CONCEPTS);

                System.out.println("child uris");
                for (String child : childUris) {
                    System.out.println(child);
                }

                System.out.println("parent uris");
                for (String parent : parentUris) {
                    System.out.println(parent);
                }

                for (Resource resource : neighbourhood.getResources()) {
                    System.out.println(resource
                            .getValue(Concept.CONCEPT_CHILD_COUNT));
                }

                // System.out.println("Result:\n" + result);
                // JSONObject jsonObject = JSONParser.parseStrict(result)
                // .isObject();
                // if (jsonObject == null) {
                // System.err.println("ERROR");
                // }
                //
                // JSONValue select = JsonPath.select(jsonObject,
                // "$.success.data[0].id");
                // System.out.println("Data:");
                // JSONString string = select.isString();
                // System.out.println(string.stringValue());

            }
        });

    }

}
