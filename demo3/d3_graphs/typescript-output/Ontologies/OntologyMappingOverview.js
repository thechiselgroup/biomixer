///<reference path="headers/require.d.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../Utils", "../FetchFromApi", "../Menu", "../GraphView", "../ExpansionSets", "../TipsyToolTipsOnClick", "./OntologyGraph", "./OntologyRenderScaler", "./OntologyFilterSliders", "./OntologyLegend", "Utils", "Menu", "FetchFromApi", "GraphView", "ExpansionSets", "TipsyToolTips", "TipsyToolTipsOnClick", "UndoRedo/UndoRedoManager", "Ontologies/OntologyGraph", "Ontologies/OntologyFilterSliders", "Ontologies/OntologyRenderScaler", "Ontologies/OntologyLegend", "JQueryExtension"], function(require, exports, Utils, Fetch, Menu, GraphView, ExpansionSets, TipsyToolTips, OntologyGraph, OntologyRenderScaler, OntologyFilterSliders, OntologyLegend) {
    // If I don't extend and implement both, I have to define things I want implemented in the base class,
    // and I won't be forced to define things declared in the interface. Using the interface as the
    // type later leads to a full contract of behavior; the doubling up of interface and base class
    // here is only important for implementations.
    var OntologyMappingOverview = (function (_super) {
        __extends(OntologyMappingOverview, _super);
        function OntologyMappingOverview(centralOntologyAcronym, softNodeCap) {
            _super.call(this);
            this.centralOntologyAcronym = centralOntologyAcronym;
            this.softNodeCap = softNodeCap;
            //    dragstart: {(): {(d: any, i: number): void}} =
            //    function() {
            //                console.log("redefine this for dragstart");
            //
            //        var outerThis = this;
            //        return function(d, i) {
            //            console.log("dragstart");
            //            outerThis.dragging = true;
            //            // $(this).tipsy('hide');
            //            $(".tipsy").hide();
            //            // stops the force auto positioning before you start dragging
            //            // This will halt the layout entirely, so if it tends to be unfinished for
            //            // long enough for a user to want to drag a node, we need to make this more complicated...
            //            outerThis.forceLayout.stop();
            //        }
            //    }
            // I could get rid of this function's typing...D3 doesn't check on the way in.
            // Also, it's naming...I believe it is a lambda and a closure (closes over context,
            // and returns an anonymous function (returns a lambda).
            this.alreadyHidTipsy = false;
            this.nodeUpdateTimer = false;
            // Seeing if I can modulate graph gravity using bounding boxes...
            // when the nodes are outside the box, tweak the gravity higher by a small amount,
            // and decrease it when the nodes are further from the edge
            // This is happening for each node as it updates, so keep that in mind...
            this.minGravity = 0.1;
            this.maxGravity = 3.5;

            this.menu = new Menu.Menu();
            this.legend = new OntologyLegend.OntologyLegend(this.menu);

            // Had to set div#chart.gallery height = 100% in CSS,
            // but this was only required in Firefox. I can't see why.
            this.vis = d3.select("#chart").append("svg:svg").attr("id", "graphSvg").attr("width", this.visWidth()).attr("height", this.visHeight()).attr("pointer-events", "all").on("click", this.menu.closeMenuLambda()).call(d3.behavior.zoom().on("zoom", this.redraw)).on("click", function () {
                TipsyToolTips.closeOtherTipsyTooltips();
            });

            this.vis.append('svg:rect').attr("width", this.visWidth()).attr("height", this.visHeight()).attr("id", "graphRect").style('fill', 'white');

            this.vis.append("svg:g").attr("id", "link_container");
            this.vis.append("svg:g").attr("id", "node_container");

            $(window).resize(this.resizedWindowLambda);

            this.resizedWindowLambda();

            // We don't really need this right now, but it's a good reminder that we need to clear
            // service records and keep the cache whenever creating a graph.
            Fetch.CacheRegistry.clearAllServiceRecordsKeepCacheData();
        }
        // TODO Is this overshadowing or is this using the member defined in the parent class?
        // Put a re-callable layout function in runCurrentLayout.
        // TODO Refactor something. Leaving this way to prevent too much code change that isn't simpyl TypeScript refactoring.
        OntologyMappingOverview.prototype.filterGraphOnMappingCounts = function () {
            this.filterSliders.filterGraphOnMappingCounts();
        };

        OntologyMappingOverview.prototype.redraw = function () {
            //  console.log("redrawing D3", d3.event.translate, d3.event.scale);
            //  vis.attr("transform",
            //      "translate(" + d3.event.translate + ")"
            //      + " scale(" + d3.event.scale + ")");
        };

        OntologyMappingOverview.prototype.initAndPopulateGraph = function () {
            this.ontologyGraph = new OntologyGraph.OntologyGraph(this, this.softNodeCap, this.centralOntologyAcronym);
            this.renderScaler = new OntologyRenderScaler.OntologyRenderScaler(this.vis);
            this.filterSliders = new OntologyFilterSliders.MappingRangeSliders(this.ontologyGraph, this, this.centralOntologyAcronym);
            this.initGraph();

            this.setCurrentLayout(this.executeCenterLayoutLambda(this));

            this.prepGraphMenu();

            // Will do async stuff and add to graph
            var expId = new ExpansionSets.ExpansionSetIdentifer("ontology_neighbourhood_" + this.centralOntologyAcronym, "Initial load: " + this.centralOntologyAcronym);
            var expansionSet = new ExpansionSets.ExpansionSet(expId, null, this.ontologyGraph, this.undoRedoBoss, null);
            this.ontologyGraph.fetchOntologyNeighbourhood(this.centralOntologyAcronym, expansionSet);
            // If you want to toy with the original static data, try this:
            //  populateGraph(json);
        };

        OntologyMappingOverview.prototype.initGraph = function () {
            this.forceLayout = d3.layout.force();

            //  forceLayout.drag()
            //  .on("dragstart", function(){})
            //  .on("dragend", function(){dragging = false;});
            // nodeDragBehavior = forceLayout.drag;
            this.nodeDragBehavior = d3.behavior.drag().on("dragstart", this.dragstartLambda(this)).on("drag", this.dragmoveLambda(this)).on("dragend", this.dragendLambda(this));

            // See the gravityAdjust(), which is called in tick() and modulates
            // gravity to keep nodes within the view frame.
            // If charge() is adjusted, the base gravity and tweaking of it probably needs tweaking as well.
            this.forceLayout.friction(0.9).gravity(.05).charge(-200).linkDistance(this.linkMaxDesiredLength()).size([this.visWidth(), this.visHeight()]).start();
            // console.log("Is it force distance or link distance above?");
        };

        OntologyMappingOverview.prototype.dragstartLambda = function (outerThis) {
            return function (d, i) {
                outerThis.alreadyHidTipsy = false;
                outerThis.dragging = true;
                $(".tipsy").hide();

                // stops the force auto positioning before you start dragging
                // This will halt the layout entirely, so if it tends to be unfinished for
                // long enough for a user to want to drag a node, we need to make this more complicated...
                outerThis.forceLayout.stop();
            };
        };

        // Need this definition style to satisfy scoping of "this" to the D3 context.
        // Since this is at the class level, and both the class and D3 "this" are used,
        // I need to define "outerThis" for the class instance, and to do so I need to
        // create a lambda closure, not just a regular one.
        OntologyMappingOverview.prototype.dragmoveLambda = function (outerThis) {
            return function (d, i) {
                if (!outerThis.alreadyHidTipsy && (d3.event.dx != 0 || d3.event.dy != 0)) {
                    TipsyToolTips.closeOtherTipsyTooltips();
                    outerThis.alreadyHidTipsy = true;
                }

                // http://bl.ocks.org/norrs/2883411
                // https://github.com/mbostock/d3/blob/master/src/layout/force.js
                // Original dragmove() had call to force.resume(), which I needed to remove when the graph was stable.
                d.px += d3.event.dx;
                d.py += d3.event.dy;
                d.x += d3.event.dx;
                d.y += d3.event.dy;

                // Don't need tick if I update the node and associated arcs appropriately.
                // forceLayout.resume();
                // ontologyTick(); // this is the key to make it work together with updating both px,py,x,y on d !
                d3.select(this).attr("transform", function (d) {
                    return "translate(" + d.x + "," + d.y + ")";
                });

                outerThis.vis.selectAll(GraphView.BaseGraphView.linkSvgClass).filter(function (e, i) {
                    return e.source == d || e.target == d;
                }).attr("x1", function (e) {
                    return e.source.x;
                }).attr("y1", function (e) {
                    return e.source.y;
                }).attr("x2", function (e) {
                    return e.target.x;
                }).attr("y2", function (e) {
                    return e.target.y;
                });
            };
        };

        OntologyMappingOverview.prototype.dragendLambda = function (outerThis) {
            return function (d, i) {
                outerThis.dragging = false;
                $(".tipsy").show();

                // of course set the node to fixed so the force doesn't include the node in its auto positioning stuff
                d.fixed = true;
                // Don't need the tick(), don't want the resume.
                // ontologyTick(true);
                // forceLayout.resume();
            };
        };

        OntologyMappingOverview.prototype.createNodePopupTable = function (ontologyCircle, ontologyData) {
            var isRootNode = ontologyData.rawAcronym === this.centralOntologyAcronym;
            var outerDiv = $("<div></div>");
            outerDiv.addClass("popups-Popup");

            var noWrapStyle = { "white-space": "nowrap" };
            var wrapStyle = {};

            var table = $("<table></table>");
            var tBody = $("<tbody></tbody>");
            outerDiv.append(table);
            table.append(tBody);

            tBody.append($("<tr></tr>").append($("<td></td>").append($("<div></div>").text(ontologyData["rawAcronym"] + ":" + ontologyData["name"]).attr("class", "popups-Header gwt-Label avatar avatar-resourceSet GK40RFKDB dragdrop-handle"))));

            var urlText = "http://bioportal.bioontology.org/ontologies/" + ontologyData["rawAcronym"] + "?p=summary";
            tBody.append($("<tr></tr>").append($("<td></td>").attr("align", "left").css({ "vertical-align": "top" }).append($("<div></div>").addClass("gwt-HTML").css(noWrapStyle).append($("<a></a>").attr("target", "_blank").attr("href", urlText).text("Open ontology homepage in tab")))));

            // Root node doesn't need these, and it's confusing with them included.
            var jsonLeaveOutOfRoot = ["Num Mappings: ", "Mapped: "];

            var jsonArgs = {
                "Ontology Name: ": { "key": "name", "style": noWrapStyle },
                "Ontology Acronym: ": { "key": "rawAcronym", "style": noWrapStyle },
                "Ontology URI: ": { "key": "uriId", "style": noWrapStyle },
                "Description: ": { "key": "description", "style": wrapStyle },
                "Num Classes: ": { "key": "numberOfClasses", "style": noWrapStyle },
                "Num Individuals: ": { "key": "numberOfIndividuals", "style": noWrapStyle },
                "Num Properties: ": { "key": "numberOfProperties", "style": noWrapStyle },
                "Num Mappings: ": { "key": "mapped_classes_to_central_node", "style": noWrapStyle },
                "Mapped: ": { "key": "mapped_classes_to_central_node", "style": noWrapStyle }
            };
            var outerThis = this;
            $.each(jsonArgs, function (label, properties) {
                if (isRootNode && -1 !== $.inArray(label, jsonLeaveOutOfRoot)) {
                    return;
                }
                var style = properties["style"];
                var propertyKey = properties["key"];
                var value = ontologyData[propertyKey];
                if (label === "Mapped: ") {
                    value = outerThis.precise_round(100 * parseInt(ontologyData["mapped_classes_to_central_node"]) / parseInt(ontologyData["numberOfClasses"]), 1);
                    value += "%";
                }
                tBody.append($("<tr></tr>").append($("<td></td>").attr("align", "left").css({ "vertical-align": "top" }).append($("<div></div>").addClass("gwt-HTML").css(style).append($("<b></b>").text(label)).append($("<span></span>").text(value)))));
            });

            return outerDiv.prop("outerHTML");
        };

        OntologyMappingOverview.prototype.precise_round = function (num, decimals) {
            return Math.round(num * Math.pow(10, decimals)) / Math.pow(10, decimals);
        };

        /**
        * This function should be used when adding brand new nodes and links to the
        * graph. Do not call it to update properties of graph elements.
        */
        OntologyMappingOverview.prototype.populateNewGraphElements = function (graphD3Format) {
            this.populateNewGraphEdges(graphD3Format.links);
            this.populateNewGraphNodes(graphD3Format.nodes);
        };

        OntologyMappingOverview.prototype.populateNewGraphEdges = function (linksData) {
            var _this = this;
            if (linksData.length === 0) {
                return [];
            }

            // Data constancy via key function() passed to data()
            // Link stuff first
            var links = this.vis.select("#link_container").selectAll(GraphView.BaseGraphView.linkSvgClass).data(linksData, OntologyGraph.Link.D3IdentityFunction);

            // console.log("Before append links: "+links[0].length+" links.enter(): "+links.enter()[0].length+" links.exit(): "+links.exit()[0].length+" links from selectAll: "+vis.selectAll("line.link")[0].length);
            // Add new stuff
            var enteringLinks = links.enter().append("svg:line").attr("class", GraphView.BaseGraphView.linkSvgClassSansDot + " " + GraphView.BaseGraphView.ontologyLinkSvgClassSansDot).attr("id", function (d) {
                return "link_line_" + d.source.acronymForIds + "-to-" + d.target.acronymForIds;
            }).on("mouseover", this.highlightHoveredLinkLambda(this)).on("mouseout", this.unhighlightHoveredLinkLambda(this));

            // console.log("After append links: "+links[0].length+" links.enter(): "+links.enter()[0].length+" links.exit(): "+links.exit()[0].length+" links from selectAll: "+vis.selectAll("line.link")[0].length);
            // Update Basic properties
            if (!enteringLinks.empty()) {
                enteringLinks.attr("x1", function (d) {
                    return d.source.x;
                }).attr("y1", function (d) {
                    return d.source.y;
                }).attr("x2", function (d) {
                    return d.target.x;
                }).attr("y2", function (d) {
                    return d.target.y;
                }).attr("data-thickness_basis", function (d) {
                    return d.value;
                });

                // Update Tool tip
                enteringLinks.append("title").text(function (d) {
                    return "Number Of Mappings: " + d.numMappings;
                }).attr("id", function (d) {
                    return "link_title_" + d.source.acronymForIds + "-to-" + d.target.acronymForIds;
                });

                // Update *all* links scalings given new links are present
                this.renderScaler.updateLinkScalingFactor();
                links.style("stroke-width", function (d) {
                    return _this.renderScaler.ontologyLinkScalingFunc(d.value);
                });
            }

            if (!enteringLinks.empty()) {
                this.filterSliders.updateTopMappingsSliderRange();
                this.updateStartWithoutResume();
            }
        };

        OntologyMappingOverview.prototype.populateNewGraphNodes = function (nodesData) {
            var _this = this;
            if (nodesData.length === 0) {
                return [];
            }

            // Node stuff now
            var nodes = this.vis.select("#node_container").selectAll("g.node_g").data(nodesData, OntologyGraph.Node.D3IdentityFunction);

            // Add new stuff
            var enteringNodes = nodes.enter().append("svg:g").attr("class", GraphView.BaseGraphView.nodeGSvgClassSansDot).attr("id", function (d) {
                return "node_g_" + d.acronymForIds;
            }).call(this.nodeDragBehavior);

            // Easiest to use JQuery to get at existing enter() circles
            // Otherwise we futz with things like the enter()select(function) below
            // I think that the lack of way to grab child elements from the enter() selection while they are
            // data bound (as is usual for most D3 selections), is what is preventing me from udpating using D3
            // idioms. This means no D3 implicit selection loops.
            // Therefore I need to update using JQuery selections on unqiue element IDs
            // Basic properties
            enteringNodes.append("svg:circle").attr("id", function (d) {
                return "node_circle_" + d.acronymForIds;
            }).attr("class", GraphView.BaseGraphView.nodeSvgClassSansDot + " " + GraphView.BaseGraphView.ontologyNodeSvgClassSansDot).attr("cx", "0px").attr("cy", "0px").style("fill", this.defaultNodeColor).style("stroke", this.ontologyGraph.darkenColor(this.defaultNodeColor)).attr("data-radius_basis", function (d) {
                return d.number;
            }).attr("r", function (d) {
                return _this.renderScaler.ontologyNodeScalingFunc(d.number, d.rawAcronym);
            }).on("mouseover", this.highlightHoveredNodeLambda(this, true)).on("mouseout", this.unhighlightHoveredNodeLambda(this, true));

            // Add a second circle that represents the mapped classes of the ontology.
            enteringNodes.append("svg:circle").attr("id", function (d) {
                return "node_circle_inner_" + d.acronymForIds;
            }).attr("class", GraphView.BaseGraphView.nodeInnerSvgClassSansDot + " " + GraphView.BaseGraphView.ontologyNodeSvgClassSansDot).attr("cx", "0px").attr("cy", "0px").attr("pointer-events", "none").style("fill", this.ontologyGraph.brightenColor(this.defaultNodeColor)).style("stroke", this.ontologyGraph.darkenColor(this.defaultNodeColor)).attr("data-inner_radius_basis", function (d) {
                return d.mapped_classes_to_central_node;
            }).attr("data-outer_radius_basis", function (d) {
                return d.number;
            }).attr("r", function (d) {
                return _this.renderScaler.ontologyInnerNodeScalingFunc(d.mapped_classes_to_central_node, d.number, d.rawAcronym);
            }).on("mouseover", this.highlightHoveredNodeLambda(this, true)).on("mouseout", this.unhighlightHoveredNodeLambda(this, true));

            // Label
            enteringNodes.append("svg:text").attr("id", function (d) {
                return "node_text_" + d.acronymForIds;
            }).attr("class", GraphView.BaseGraphView.nodeLabelSvgClassSansDot + " unselectable").attr("dx", 12).attr("dy", 1).text(function (d) {
                return d.name;
            }).style("pointer-events", "none").attr("unselectable", "on").attr("onmousedown", "noselect").attr("onselectstart", "function(){ return false;}");

            enteringNodes.each(TipsyToolTips.nodeTooltipOnClickLambda(this));

            // Would do exit().remove() here if it weren't re-entrant, so to speak.
            //        // XXX Doing this a second time destroys the visualization!
            //        // How would we do it on only new things?
            //        // Oh! It is because we are using the links and nodes references,
            //        // and losing references to the existing nodes and links.
            //        // I really want to make sure I keep trakc of whether we
            //        // have all nodes/links, or just new ones...
            //        var lastLabelShiftTime = jQuery.now();
            //        var lastGravityAdjustmentTime = jQuery.now();
            //        var firstTickTime = jQuery.now();
            //        var maxLayoutRunDuration = 10000;
            //        var maxGravityFrequency = 4000;
            //        // Fat arrow closure because we don't need a dynamic scoped "this" for the tick method.
            //        this.ontologyTick = () => {
            //            // Stop the layout early. The circular initialization makes it ok.
            //            if (this.forceLayout.alpha() < this.alphaCutoff || jQuery.now() - firstTickTime > maxLayoutRunDuration) {
            //                this.forceLayout.stop();
            //            }
            //
            //            // Do I want nodes to avoid one another?
            //            // http://bl.ocks.org/mbostock/3231298
            //    //      var q = d3.geom.quadtree(nodes),
            //    //        i = 0,
            //    //        n = nodes.length;
            //    //      while (++i < n) q.visit(collide(nodes[i]));
            //    //      function collide(node) {
            //    //            var r = node.radius + 16,
            //    //                nx1 = node.x - r,
            //    //                nx2 = node.x + r,
            //    //                ny1 = node.y - r,
            //    //                ny2 = node.y + r;
            //    //            return function(quad, x1, y1, x2, y2) {
            //    //              if (quad.point && (quad.point !== node)) {
            //    //                var x = node.x - quad.point.x,
            //    //                    y = node.y - quad.point.y,
            //    //                    l = Math.sqrt(x * x + y * y),
            //    //                    r = node.radius + quad.point.radius;
            //    //                if (l < r) {
            //    //                  l = (l - r) / l * .5;
            //    //                  node.x -= x *= l;
            //    //                  node.y -= y *= l;
            //    //                  quad.point.x += x;
            //    //                  quad.point.y += y;
            //    //                }
            //    //              }
            //    //              return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
            //    //            };
            //    //       svg.selectAll("circle")
            //    //        .attr("cx", function(d) { return d.x; })
            //    //        .attr("cy", function(d) { return d.y; });
            //    //       }
            //
            //            // For every iteration of the layout (until it stabilizes)
            //            // Using this bounding box on nodes and links works, but leads to way too much overlap for the
            //            // labels...Bostock is correct in saying that gravity adjustments can get better results.
            //            // gravityAdjust() functions are pass through; they want to inspect values,
            //            // not modify them!
            //            var doLabelUpdateNextTime = false;
            //            if(jQuery.now() - lastGravityAdjustmentTime > maxGravityFrequency){
            //                nodes.attr("transform", function(d) { return "translate(" + this.gravityAdjustX(d.x) + "," + this.gravityAdjustY(d.y) + ")"; });
            //                lastGravityAdjustmentTime = jQuery.now();
            //                doLabelUpdateNextTime = true;
            //            } else {
            //                nodes.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
            //            }
            //
            //            links
            //              .attr("x1", function(d) { return d.source.x; })
            //              .attr("y1", function(d) { return d.source.y; })
            //              .attr("x2", function(d) { return d.target.x; })
            //              .attr("y2", function(d) { return d.target.y; });
            //
            //            // I want labels to aim out of middle of graph, to make more room
            //            // It slows rendering, so I will only do it sometimes
            //            // Commented all this out because I liked centering them instead.
            //    //      if((jQuery.now() - lastLabelShiftTime > 2000) && !doLabelUpdateNextTime){
            //    //          $.each($(".nodetext"), function(i, text){
            //    //              text = $(text);
            //    //              if(text.position().left >= visWidth()/2){
            //    //                  text.attr("dx", 12);
            //    //                  text.attr("x", 12);
            //    //              } else {
            //    //                  text.attr("dx", - 12 - text.get(0).getComputedTextLength());
            //    //                  text.attr("x", - 12 - text.get(0).getComputedTextLength());
            //    //              }
            //    //          })
            //    //          lastLabelShiftTime = jQuery.now();
            //    //      }
            //
            //
            //        }
            if (!enteringNodes.empty()) {
                this.stampTimeGraphModified();
            }

            if (!enteringNodes.empty()) {
                this.forceLayout.on("tick", this.onLayoutTick());
                //            this.attachNodeMenu(enteringNodes);
            }

            // Make sure we have initialized the filter slider to be at the softNodeCap.
            // The filter function will lead to individual API calls being dispatched on nodes.
            // It will (in the future) also trigger layout adaptation to added or removed nodes.
            this.filterSliders.changeTopMappingSliderValues(null, this.softNodeCap);

            if (!enteringNodes.empty()) {
                this.updateStartWithoutResume();
                this.renderScaler.updateNodeScalingFactor();
                // enteringNodes.attr("transform", function(d: OntologyGraph.Node) { return "translate(" + d.x + "," + d.y + ")"; });
            }
        };

        OntologyMappingOverview.prototype.removeMissingGraphElements = function (graphD3Format) {
            // Have problems if we don't pass the containers back in like this.
            // Removed edges will not be properly removed, and exceptions will
            // be thrown.
            this.forceLayout.nodes(graphD3Format.nodes);
            this.forceLayout.links(graphD3Format.links);

            var nodes = this.vis.selectAll("g.node_g").data(graphD3Format.nodes, OntologyGraph.Node.D3IdentityFunction);
            var links = this.vis.selectAll("." + GraphView.BaseGraphView.linkSvgClassSansDot).data(graphD3Format.links, OntologyGraph.Link.D3IdentityFunction);

            var nodesRemoved = nodes.exit().remove();
            var linksRemoved = links.exit().remove();

            // Update filter sliders. Filtering and layout refresh should be updated within the slider event function.
            this.filterSliders.updateTopMappingsSliderRange();
            this.filterSliders.rangeSliderSlideEvent(null, null); // Bad to pass nulls when I know it will work, or ok?

            this.stampTimeGraphModified();
            this.forceLayout.on("tick", this.onLayoutTick());

            // this.runCurrentLayout();
            this.currentLambda();
        };

        // Fat arrow closure because we don't need a dynamic scoped "this" for the tick method.
        OntologyMappingOverview.prototype.onLayoutTick = function () {
            var _this = this;
            // XXX Doing this a second time destroys the visualization!
            // How would we do it on only new things?
            // Oh! It is because we are using the links and nodes references,
            // and losing references to the existing nodes and links.
            // I really want to make sure I keep trakc of whether we
            // have all nodes/links, or just new ones...
            var lastLabelShiftTime = jQuery.now();
            var lastGravityAdjustmentTime = jQuery.now();
            var firstTickTime = jQuery.now();
            var maxLayoutRunDuration = 10000;
            var maxGravityFrequency = 4000;

            // This improved layout behavior dramatically.
            var nodes = this.vis.selectAll("g.node_g");

            // Links have a g element aroudn them too, for ordering effects, but we set the link endpoints, not the g positon.
            var links = this.vis.selectAll(GraphView.BaseGraphView.linkSvgClass);
            return function () {
                // Stop the layout early. The circular initialization makes it ok.
                if (_this.forceLayout.alpha() < _this.alphaCutoff || jQuery.now() - firstTickTime > maxLayoutRunDuration) {
                    _this.forceLayout.stop();
                }

                // Do I want nodes to avoid one another?
                // http://bl.ocks.org/mbostock/3231298
                //      var q = d3.geom.quadtree(nodes),
                //        i = 0,
                //        n = nodes.length;
                //      while (++i < n) q.visit(collide(nodes[i]));
                //      function collide(node) {
                //            var r = node.radius + 16,
                //                nx1 = node.x - r,
                //                nx2 = node.x + r,
                //                ny1 = node.y - r,
                //                ny2 = node.y + r;
                //            return function(quad, x1, y1, x2, y2) {
                //              if (quad.point && (quad.point !== node)) {
                //                var x = node.x - quad.point.x,
                //                    y = node.y - quad.point.y,
                //                    l = Math.sqrt(x * x + y * y),
                //                    r = node.radius + quad.point.radius;
                //                if (l < r) {
                //                  l = (l - r) / l * .5;
                //                  node.x -= x *= l;
                //                  node.y -= y *= l;
                //                  quad.point.x += x;
                //                  quad.point.y += y;
                //                }
                //              }
                //              return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
                //            };
                //       svg.selectAll("circle")
                //        .attr("cx", function(d) { return d.x; })
                //        .attr("cy", function(d) { return d.y; });
                //       }
                // For every iteration of the layout (until it stabilizes)
                // Using this bounding box on nodes and links works, but leads to way too much overlap for the
                // labels...Bostock is correct in saying that gravity adjustments can get better results.
                // gravityAdjust() functions are pass through; they want to inspect values,
                // not modify them!
                var doLabelUpdateNextTime = false;
                if (jQuery.now() - lastGravityAdjustmentTime > maxGravityFrequency) {
                    nodes.attr("transform", function (d) {
                        return "translate(" + _this.gravityAdjustX(d.x) + "," + _this.gravityAdjustY(d.y) + ")";
                    });
                    lastGravityAdjustmentTime = jQuery.now();
                    doLabelUpdateNextTime = true;
                } else {
                    nodes.attr("transform", function (d) {
                        return "translate(" + d.x + "," + d.y + ")";
                    });
                }

                links.attr("x1", function (d) {
                    return d.source.x;
                }).attr("y1", function (d) {
                    return d.source.y;
                }).attr("x2", function (d) {
                    return d.target.x;
                }).attr("y2", function (d) {
                    return d.target.y;
                });
                // I want labels to aim out of middle of graph, to make more room
                // It slows rendering, so I will only do it sometimes
                // Commented all this out because I liked centering them instead.
                //      if((jQuery.now() - lastLabelShiftTime > 2000) && !doLabelUpdateNextTime){
                //          $.each($(".nodetext"), function(i, text){
                //              text = $(text);
                //              if(text.position().left >= visWidth()/2){
                //                  text.attr("dx", 12);
                //                  text.attr("x", 12);
                //              } else {
                //                  text.attr("dx", - 12 - text.get(0).getComputedTextLength());
                //                  text.attr("x", - 12 - text.get(0).getComputedTextLength());
                //              }
                //          })
                //          lastLabelShiftTime = jQuery.now();
                //      }
            };
        };

        /**
        * We cannot update the graph with new node or link properties *efficiently* using D3.
        * This is because, although you can use the enter() selection, you cannot sub-select within
        * it to access the children DOM elements, and using other D3 ways of getting at the elements
        * fails to have them bound to the data as they are in the enter() selection [meaning that
        * data based property settings fail].
        *
        * Explicit looping allows us to cherry pick data, and do fewer DOM changes than I could
        * when using D3's data().enter() selection results.
        *
        * @param json
        */
        OntologyMappingOverview.prototype.updateDataForNodesAndLinks = function (json) {
            // console.log("Updating with data:");
            // console.log(json);
            var outerThis = this;

            var updateLinksFromJson = function (i, d) {
                // Given a json encoded graph element, update all of the nested elements associated with it
                // cherry pick elements that we might otherwise get by class "link"
                var link = this.vis.select("#link_line_" + d.source.acronymForIds + "-to-" + d.target.acronymForIds);
                link.attr("data-thickness_basis", function (d) {
                    return d.value;
                });
                link.select("title").text(function (d) {
                    return "Number Of Mappings: " + d.numMappings;
                });
            };

            var updateNodesFromJson = function (i, d) {
                // Given a json encoded graph element, update all of the nested elements associated with it
                // cherry pick elements that we might otherwise get by class "node"
                var node = outerThis.vis.select("#node_g_" + d.acronymForIds);

                node.select("title").text(function (d) {
                    return "Number Of Terms: " + d.number + "<br/> and <br/>" + "Number Of Mappings: " + d.mapped_classes_to_central_node;
                });
                node.select("text").text(function (d) {
                    return d.name;
                }).attr("x", function () {
                    return -this.getComputedTextLength() / 2;
                });

                var circles = node.select(GraphView.BaseGraphView.nodeSvgClass);
                circles.attr("data-radius_basis", d.number);
                circles.transition().style("fill", d.nodeColor).style("stroke", d.nodeStrokeColor);

                // Update the inner circles too
                var inner_circles = node.select(GraphView.BaseGraphView.nodeInnerSvgClass);
                inner_circles.attr("data-inner_radius_basis", d.mapped_classes_to_central_node);
                inner_circles.attr("data-outer_radius_basis", d.number);
                inner_circles.transition().style("fill", d.innerNodeColor).style("stroke", d.nodeStrokeColor);

                // Refresh popup if currently open
                if (outerThis.lastDisplayedTipsy != null && outerThis.lastDisplayedTipsy.css("visibility") == "visible") {
                    $(outerThis.lastDisplayedTipsy).children(".tipsy-inner").html(outerThis.createNodePopupTable(outerThis.lastDisplayedTipsySvg, outerThis.lastDisplayedTipsyData));
                }
            };

            $.each(json.links, updateLinksFromJson);
            $.each(json.nodes, updateNodesFromJson);

            if (outerThis.nodeUpdateTimer == false) {
                outerThis.nodeUpdateTimer = true;
                window.setTimeout(function () {
                    console.log("TIMER RESET");
                    outerThis.nodeUpdateTimer = false;
                    outerThis.renderScaler.updateNodeScalingFactor();
                    // The link thickness does not receive new data right now,
                    // otherwise we'd want to call the update factor function here.
                    // updateLinkScalingFactor();
                }, 1000);
            }
        };

        OntologyMappingOverview.prototype.removeGraphPopulation = function (data) {
            console.log("Removing some graph elements " + Utils.getTime());

            var nodes = this.vis.selectAll("g.node_g").data(this.ontologyGraph.graphD3Format.nodes, OntologyGraph.Node.D3IdentityFunction);
            var links = this.vis.selectAll(GraphView.BaseGraphView.linkSvgClass).data(this.ontologyGraph.graphD3Format.links, OntologyGraph.Link.D3IdentityFunction);

            var nodesRemoved = nodes.exit().remove();
            var linksRemoved = links.exit().remove();

            // Do I need start() or not? Number of elements before and after implies not.
            if (!nodesRemoved.empty() || !linksRemoved.empty()) {
                this.updateStartWithoutResume();
            }

            // Update filter sliders. Filtering and layout refresh should be updated within the slider event function.
            this.filterSliders.updateTopMappingsSliderRange();
            this.filterSliders.rangeSliderSlideEvent(null, null); // Bad to pass nulls when I know it will work, or ok?
        };

        OntologyMappingOverview.prototype.gravityAdjust = function (numb, visSize) {
            var alpha = 0.2 / this.forceLayout.nodes().length;
            if (numb < visSize * 0.05 || visSize * 0.95 < numb) {
                // console.log("increase");
                this.forceLayout.gravity(Math.min(this.maxGravity, this.forceLayout.gravity() * (1 + alpha)));
            } else if (visSize * 0.20 < numb && numb < visSize * 0.80) {
                // console.log("decrease");
                this.forceLayout.gravity(Math.max(this.minGravity, this.forceLayout.gravity() * (1 - alpha)));
            } else {
                // leave gravity as it is
            }
            return numb;
        };
        OntologyMappingOverview.prototype.gravityAdjustX = function (numb) {
            return this.gravityAdjust(numb, this.visWidth());
        };
        OntologyMappingOverview.prototype.gravityAdjustY = function (numb) {
            return this.gravityAdjust(numb, this.visHeight());
        };

        OntologyMappingOverview.prototype.executeCenterLayoutLambda = function (ontologyView) {
            var _this = this;
            var outerThis = ontologyView;
            return function (refreshLayout) {
                if (refreshLayout) {
                    // Act normal, redo the whole layout
                }

                var graphNodes = outerThis.ontologyGraph.graphD3Format.nodes;
                var graphLinks = outerThis.ontologyGraph.graphD3Format.links;

                // This is the most up to date way to know how many nodes we are laying out, assuming we don't care to position
                // undisplayed nodes
                var numberOfNodes = $(GraphView.BaseGraphView.nodeSvgClass).filter(function (i, d) {
                    return $(d).css("display") !== "none";
                }).length;
                _this.forceLayout.friction(0.01);
                _this.forceLayout.stop();

                // We won't be using the central node for this
                var anglePerNode = 2 * Math.PI / (numberOfNodes - 1);
                var arcLength = _this.linkMaxDesiredLength();
                var i = 0;

                // TODO get sortedAcronyms from the OntologiesGraph model
                $.each(_this.ontologyGraph.sortedAcronymsByMappingCount, function (index, sortedAcronym) {
                    var acronym = sortedAcronym.acronym;
                    var node = sortedAcronym.node;

                    // var node = $(ontologyAcronymNodeMap).attr("vid:"+node.acronym);
                    if (typeof node === "undefined") {
                        console.log("Undefined ontology entry");
                    }

                    //              if(typeof visibleNodes[node.acronymForIds] === "undefined")
                    //              $("#node_g_"+d.source.acronymForIds).find("*").css("display", (hideSourceNodeBecauseOfHiddenArc || hideSourceNode) ? "none" : "");
                    var display = $("#node_circle_" + node.acronymForIds).css("display");
                    if ((display == null || display === "none")) {
                        // For lack of a better thing to do, set off screen invisible nodes to center.
                        node.x = outerThis.visWidth() / 2;
                        node.y = outerThis.visHeight() / 2;
                    } else if (node.rawAcronym !== outerThis.centralOntologyAcronym) {
                        var angleForNode = i * anglePerNode;
                        i++;
                        node.x = outerThis.visWidth() / 2 + arcLength * Math.cos(angleForNode); // start in middle and let them fly outward
                        node.y = outerThis.visHeight() / 2 + arcLength * Math.sin(angleForNode); // start in middle and let them fly outward
                    } else {
                        // Central node goes in middle, and the unrendered nodes can go anywhere really, so put them in the middle.
                        node.x = outerThis.visWidth() / 2;
                        node.y = outerThis.visHeight() / 2;
                    }
                });

                // Actually the central node isn't in the set, so grab it directly and position it
                _this.ontologyGraph.centralOntologyNode.x = outerThis.visWidth() / 2;
                _this.ontologyGraph.centralOntologyNode.y = outerThis.visHeight() / 2;

                var animationDuration = 400;
                d3.selectAll("g.node_g").transition().duration(animationDuration).attr("transform", function (d) {
                    return "translate(" + d.x + "," + d.y + ")";
                });

                d3.selectAll(GraphView.BaseGraphView.linkSvgClass).transition().duration(animationDuration).attr("x1", function (d) {
                    return d.source.x;
                }).attr("y1", function (d) {
                    return d.source.y;
                }).attr("x2", function (d) {
                    return d.target.x;
                }).attr("y2", function (d) {
                    return d.target.y;
                });
            };
        };

        OntologyMappingOverview.prototype.prepGraphMenu = function () {
            // Node filter for ontology graphs. Allows filtering of nodes by size, and arcs by size.
            this.menu.initializeMenu();
            this.filterSliders.addMenuComponents(this.menu.getMenuSelector(), this.softNodeCap);
            this.legend.initialize();
        };

        OntologyMappingOverview.prototype.sortConceptNodesCentralOntologyName = function () {
            var outerThis = this;
            return this.ontologyGraph.graphD3Format.nodes.sort(function (a, b) {
                if (a.rawAcronym === b.rawAcronym) {
                    // Exact same unqiue identifiers?
                    return 0;
                }

                // Alphabetical ontologies
                return (a.rawAcronym < b.rawAcronym) ? -1 : 1;
            });
        };
        return OntologyMappingOverview;
    })(GraphView.BaseGraphView);
    exports.OntologyMappingOverview = OntologyMappingOverview;
});
