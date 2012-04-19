package org.thechiselgroup.biomixer.client.services.term;

import name.pehl.totoe.json.client.JsonPath;

import org.thechiselgroup.biomixer.client.services.NcboJsonpRestUrlBuilderFactory;
import org.thechiselgroup.biomixer.client.workbench.util.url.JsonpUrlFetchService;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class TermTest extends GWTTestCase {

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
        String url = new NcboJsonpRestUrlBuilderFactory().createUrlBuilder()
                .parameter("path", "%2Fvirtual%2Fontology%2F1487").toString();

        // &conceptid=http%3A%2F%2Fwho.int%2Fbodysystem.owl%23BodySystem

        System.out.println(url);

        JsonpUrlFetchService urlFetch = new JsonpUrlFetchService();
        urlFetch.fetchURL(url, new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                System.err.println("FAIL:\n" + caught.getMessage());
            }

            @Override
            public void onSuccess(String result) {
                System.out.println("Result:\n" + result);
                JSONObject jsonObject = JSONParser.parseStrict(result)
                        .isObject();
                if (jsonObject == null) {
                    System.err.println("ERROR");
                }

                JSONValue select = JsonPath.select(jsonObject,
                        "$.success.data[0].id");
                System.out.println("Data:");
                JSONString string = select.isString();
                System.out.println(string.stringValue());

            }
        });

    }

}
