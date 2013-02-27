package org.thechiselgroup.biomixer.client.visualization_component.matrix;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Element;

/**
 * Exists mostly for cleanliness. It is nicer to have native methods tucked
 * away.
 * 
 * @author everbeek
 * 
 */
public class NeoD3MatrixJavascriptInterface {
	
    public NeoD3MatrixJavascriptInterface() {

    }

    // This was the very simple way to load data from the prototype. The data is
    // already prepared, and there was no way to change it. The prototype
    // demonstrated the graphics and the mouse interaction, but not data
    // swapping.
    protected native void applyD3Layout(Element div,
            JSONObject matrixJSONContextObject, String jsonString)/*-{
		$wnd.updateMatrixLayoutString(div, matrixJSONContextObject, jsonString);
    }-*/;

    // Uses the same method as the less cool JSONObject receiving version
    protected native void applyD3Layout(Element div,
            JSONObject matrixJSONContextObject, MatrixJsonData jsonMatrixData)/*-{
		$wnd.updateMatrixLayout(div, matrixJSONContextObject, jsonMatrixData);
    }-*/;

    protected native void applyD3Layout(Element div,
            JSONObject matrixJSONContextObject, JSONObject jsonObject)/*-{
		$wnd.updateMatrixLayout(div, matrixJSONContextObject, jsonObject);
    }-*/;

    protected native JSONObject initD3Layout(Element div)/*-{
		return $wnd.initMatrixLayout(div);
    }-*/;
}