package org.thechiselgroup.biomixer.client.visualization_component.matrix;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * See documentation on resitrictions for Overlay Types when extending:
 * http://code.google.com/p/google-web-toolkit/wiki/OverlayTypes
 * 
 * @author everbeek
 * 
 */
public class MatrixJsonData extends JavaScriptObject {

    // @formatter:off
    /*-
     * (Trick: this dash prevents code formatting from clobbering my layout below!)
     * (Or one can use the commented out @ formatter stuff above and below the comment...)
     * 
     * We need to convert our Java structures to json.
     * The D3 will want the concepts as "nodes" and the mappings as "links".
     * The links will have numerically addressed "source" and "target" each,
     * relative to the index of the nodes in question.
     * The data format expected by D3 is like below:
     * 
     * {
     *      "nodes":[
     *         {"name":"CHEBI_48961","group":0}, 
     *         {"name":"GO_0009493","group":1}, 
     *         {"name":"CHEBI_38553","group":0}, 
     *         {"name":"GO_0005489","group":1}, 
     *         {"name":"D000456","group":2}, 
     *         {"name":"Alga","group":4}
     *     ],
     *     "links":[
     *         {"source":0, "target":1, "value": 1},
     *         {"source":2, "target":3, "value": 1},
     *         {"source":120, "target":119, "value": 1},
     *         {"source":126, "target":125, "value": 1},
     *         {"source":94, "target":118, "value": 1}
     *     ]
     *  }
     * 
     */
    // @formatter: on
    
    /**
     * Construct using the {@link MatrixJsonData#createMatrixJsonData()} method.
     */
    protected MatrixJsonData(){
    }
    
    public static final native MatrixJsonData createMatrixJsonData() /*-{
		var newMatrixData = {
			"nodes" : [],
			"links" : []
		};
		return newMatrixData;
    }-*/;

    /**
     * Rewrite this method if we actually need Java inspection of node list contents.
     * If so, probably make an overlay type for the node entries.
     * This is mostly a placeholder.
     */
    public final native JsArray<JavaScriptObject> getNodes() /*-{
		return this.nodes;
    }-*/; 
    
    /**
     * Rewrite this method if we actually need Java inspection of link list contents.
     * If so, probably make an overlay type for the link entries.
     * This is mostly a placeholder.
     */
    public final native JsArray<JavaScriptObject> getLinks() /*-{
		return this.links;
    }-*/; 
    
    public final native int getNodeLength() /*-{
		return this.nodes.length;

    }-*/; 
    
    public final native int getLinkLength() /*-{
		return this.links.length;
    }-*/;
    
    // Could change receiving code to expect link URIs, but this is indeed smaller data...
    public final native int pushLink(int sourceIndex, int targetIndex, int i) /*-{
		this.links.push({
			"source" : sourceIndex,
			"target" : targetIndex,
			"value" : 1
		});
		return this.links.length - 1;
    }-*/;

    public final native int pushNode(String nodeName, String uri, int groupForOntology) /*-{
		this.nodes.push({
			"name" : nodeName,
			"uri" : uri,
			"group" : groupForOntology,
		});
		return this.nodes.length - 1;
    }-*/;

}
