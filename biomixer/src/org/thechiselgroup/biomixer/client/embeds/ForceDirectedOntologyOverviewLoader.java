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

		var w = $wnd.innerWidth, h = $wnd.innerHeight

		//initialize the visualization
		var vis = $wnd.d3.select(div).append("svg:svg").attr("width", w).attr(
				"height", h).attr("pointer-events", "all").append('svg:g')
				.call($wnd.d3.behavior.zoom().on("zoom", redraw)).append(
						'svg:g');

		vis.append('svg:rect').attr('width', w).attr('height', h).attr('fill',
				'white');

		//redraw on zoom
		function redraw() {
			console.log("here", $wnd.d3.event.translate, $wnd.d3.event.scale);
			vis.attr("transform", "translate(" + $wnd.d3.event.translate + ")"
					+ " scale(" + $wnd.d3.event.scale + ")");
		}

		drawLayout(jsonObject);

		function drawLayout(json) {
			//initialize the force directed layout
			var force = self.force = $wnd.d3.layout.force().nodes(json.nodes)
					.links(json.links).gravity(.05).distance(600).charge(-100)
					.size([ w, h ]).start();

			//initialize links
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
					}).style("stroke", "#999").style("stroke-opacity", 0.6);

			link.append("title").text(function(d) {
				return "Number Of Mappings: " + d.sourceMappings;
			});

			link.on("mouseover", highlightLink()).on("mouseout",
					changeColourBack("#496BB0", "#999"));

			//initialize nodes
			var node = vis.selectAll("g.node").data(json.nodes).enter().append(
					"svg:g").attr("class", "node").call(force.drag);

			node.append("svg:circle").attr("class", "circle").attr("cx", "0px")
					.attr("cy", "0px").style("fill", "#496BB0").attr("r",
							function(d) {
								return Math.sqrt((d.number) / 10);
							}).style("fill-opacity", 0.7).style("stroke",
							"#3d3d3d").style("stroke-width", "4px").on(
							"mouseover",
							changeColour("#FC6854", "#ff1", "#ff1", .1)).on(
							"mouseout", changeColourBack("#496BB0", "#999"));

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

		// highlight nodes and link on mouse over the link
		function highlightLink() {
			return function(d, i) {

				xSourcePos = d.source.x;
				ySourcePos = d.source.y;
				xTargetPos = d.target.x;
				yTargetPos = d.target.y;

				$wnd.d3.selectAll("text").style("opacity", .2).filter(
						function(g, i) {
							return g.x == d.source.x || g.y == d.source.y
									|| g.x == d.target.x || g.y == d.target.y;
						}).style("opacity", 1);

				$wnd.d3.selectAll("line").style("stroke-opacity", .1);
				$wnd.d3.selectAll("circle").style("fill-opacity", .1).style(
						"stroke-opacity", .2).filter(
						function(g, i) {
							return g.x == d.source.x || g.y == d.source.y
									|| g.x == d.target.x || g.y == d.target.y
						}).style("fill-opacity", 1).style("stroke-opacity", 1);
				$wnd.d3.select(this).style("stroke-opacity", 1).style("stroke",
						"#3d3d3d");

			}
		}

		//highlight nodes and links on mouse over the node
		function changeColour(circleFill, lineFill, circlesFill, opacity) {
			return function(d, i) {

				xPos = d.x;
				yPos = d.y;

				$wnd.d3.selectAll("line").style("stroke-opacity", .1);
				$wnd.d3.selectAll("circle").style("fill-opacity", .1).style(
						"stroke-opacity", .2);

				$wnd.d3.selectAll("text").style("opacity", .2).filter(
						function(g, i) {
							return g.x == d.x
						}).style("opacity", 1);

				var sourceNode = $wnd.d3.select(this).style("fill", circleFill)
						.style("fill-opacity", 1).style("stroke-opacity", 1);

				var adjacentLinks = $wnd.d3.selectAll("line").filter(
						function(d, i) {
							return d.source.x == xPos && d.source.y == yPos;
						}).style("stroke-opacity", 1)
						.style("stroke", "#3d3d3d").each(
								function(d) {
									$wnd.d3.selectAll("circle").filter(
											function(g, i) {
												return d.target.x == g.x
														&& d.target.y == g.y;
											}).style("fill-opacity", 1).style(
											"stroke-opacity", 1).each(
											function(d) {
												$wnd.d3.selectAll("text")
														.filter(function(g, i) {
															return g.x == d.x
														}).style("opacity", 1);
											});
								});
			};
		}

		// change the colours back to the initial state
		function changeColourBack(circleFill, lineFill) {
			return function(d, i) {
				$wnd.d3.selectAll("circle").style("fill", circleFill).style(
						"fill-opacity", .75).style("stroke-opacity", 1);
				$wnd.d3.selectAll("line").style("stroke", lineFill).style(
						"stroke-opacity", .75);
				$wnd.d3.selectAll("text").style("opacity", 1);
			};
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
