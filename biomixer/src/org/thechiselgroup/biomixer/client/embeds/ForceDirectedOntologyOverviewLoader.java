/*******************************************************************************
 * Copyright 2012 David Rusk
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.embeds;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.util.collections.SingleItemIterable;
import org.thechiselgroup.biomixer.client.services.ontology.overview.OntologyOverviewServiceAsync;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbedLoader;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbeddedViewLoader;
import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

/**
 * Calls the javascript implementation of the force directed ontologies overview
 * layout
 * 
 * @author drusk
 * 
 */
public class ForceDirectedOntologyOverviewLoader implements EmbeddedViewLoader {

    @Inject
    private ErrorHandler errorHandler;

    public static final String EMBED_MODE = "fd_oo";

    @Inject
    private OntologyOverviewServiceAsync ontologyOverviewService;

    private native void applyD3Layout(Element div, String json)/*-{

		var jsonObject = eval('(' + json + ')');

		var w = 1400, h = 1000

		var vis = $wnd.d3.select(div).append("svg:svg").attr("width", w).attr(
				"height", h).attr("pointer-events", "all").append('svg:g');

		drawLayout(jsonObject);
		function drawLayout(json) {
			var force = self.force = $wnd.d3.layout.force().nodes(json.nodes)
					.links(json.links).gravity(.05).distance(600).charge(-100)
					.size([ w, h ]).start();

			var link = vis.selectAll("line.link").data(json.links).enter()
					.append("svg:line").attr("class", "link").attr("x1",
							function(d) {
								return d.source.x;
							}).attr("y1", function(d) {
						return d.source.y;
					}).attr("x2", function(d) {
						return d.target.x;
					}).attr("y2", function(d) {
						return d.target.y;
					}).style("stroke-width", function(d) {
						return Math.sqrt(Math.ceil(d.value / 10));
					});

			link.append("title").text(function(d) {
				return "Number Of Mappings: " + d.sourceMappings;
			});

			//link.on("mouseover", highlightLink()).on("mouseout",
			//		changeColourBack("#496BB0", "#999"));

			var node = vis.selectAll("g.node").data(json.nodes).enter().append(
					"svg:g").attr("class", "node").call(force.drag);

			node.append("svg:circle").attr("class", "circle").attr("cx", "0px")
					.attr("cy", "0px").style("fill", "#496BB0").attr("r",
							function(d) {
								return Math.sqrt((d.number) / 10);
							});
			//.on("mouseover",
			//changeColour("#FC6854", "#ff1", "#ff1", .1)).on(
			//"mouseout", changeColourBack("#496BB0", "#999"));

			node.append("title").text(function(d) {
				return "Number Of Terms: " + d.number;
			});

			node.append("svg:text").attr("class", "nodetext").attr("dx", 12)
					.attr("dy", 1).text(function(d) {
						return d.name;
					});

			node.append("svg:text").attr("class", "nodetext").attr("x", 12)
					.attr("y", 1).text(function(d) {
						return d.name;
					});

			force.on("tick", function() {
				link.attr("x1", function(d) {
					return d.source.x;
				}).attr("y1", function(d) {
					return d.source.y;
				}).attr("x2", function(d) {
					return d.target.x;
				}).attr("y2", function(d) {
					return d.target.y;
				});

				node.attr("transform", function(d) {
					return "translate(" + d.x + "," + d.y + ")";
				});
			});
		}
    }-*/;

    @Override
    public Iterable<String> getEmbedModes() {
        return new SingleItemIterable<String>(EMBED_MODE);
    }

    @Override
    public void loadView(WindowLocation windowLocation, String embedMode,
            final AsyncCallback<IsWidget> callback, EmbedLoader loader) {

        // won't need WindowLocation for this embed because it just loads data
        // from a file on server

        // 1. get ontology data from file on server
        ontologyOverviewService
                .getOntologyOverviewAsJson(new ErrorHandlingAsyncCallback<String>(
                        errorHandler) {

                    @Override
                    protected void runOnSuccess(String json) {
                        // 2. call javascript code passing in json

                        // using label to get an empty div
                        Label label = new Label();
                        applyD3Layout(label.getElement(), json);
                        callback.onSuccess(label);
                    }

                });
    }
}
