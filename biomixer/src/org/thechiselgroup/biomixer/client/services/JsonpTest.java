package org.thechiselgroup.biomixer.client.services;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.json.TotoeJsonParser;
import org.thechiselgroup.biomixer.client.services.term.TermWithoutRelationshipsJsonParser;
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
        String url = "http://stage.bioontology.org/ajax/jsonp?path=%2Fvirtual%2Fontology%2F1516%3Flight%3D1%26norelations%3D1%26conceptid%3DO80-O84.9&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a";

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

                TermWithoutRelationshipsJsonParser parser = new TermWithoutRelationshipsJsonParser(
                        new TotoeJsonParser());
                Resource concept = parser.parseConcept("1516", result);
                System.out.println("Full id: "
                        + (String) concept.getValue(Concept.FULL_ID));

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
