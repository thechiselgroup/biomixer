var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../Utils", "../MouseSpinner", "../FetchFromApi", "../Menu", "../GraphView", "../ExpansionSets", "../TipsyToolTipsOnClick", "./OntologyGraph", "./OntologyRenderScaler", "./OntologyFilterSliders", "./OntologyLegend", "Utils", "Menu", "FetchFromApi", "GraphView", "ExpansionSets", "TipsyToolTips", "TipsyToolTipsOnClick", "UndoRedo/UndoRedoManager", "Ontologies/OntologyGraph", "Ontologies/OntologyFilterSliders", "Ontologies/OntologyRenderScaler", "Ontologies/OntologyLegend", "JQueryExtension"], function (require, exports, Utils, MouseSpinner, Fetch, Menu, GraphView, ExpansionSets, TipsyToolTips, OntologyGraph, OntologyRenderScaler, OntologyFilterSliders, OntologyLegend) {
    var OntologyMappingOverview = (function (_super) {
        __extends(OntologyMappingOverview, _super);
        function OntologyMappingOverview(centralOntologyAcronym, softNodeCap) {
            _super.call(this);
            this.centralOntologyAcronym = centralOntologyAcronym;
            this.softNodeCap = softNodeCap;
            this.alreadyHidTipsy = false;
            this.nodeUpdateTimer = false;
            this.minGravity = 0.1;
            this.maxGravity = 3.5;
            this.menu = new Menu.Menu();
            this.legend = new OntologyLegend.OntologyLegend(this.menu);
            this.vis = d3.select("#chart").append("svg:svg").attr("id", "graphSvg").attr("width", this.visWidth()).attr("height", this.visHeight()).attr("pointer-events", "all").on("click", this.menu.closeMenuLambda()).call(d3.behavior.zoom().on("zoom", this.redraw)).on("click", function () {
                TipsyToolTips.closeOtherTipsyTooltips();
            });
            this.vis.append('svg:rect').attr("width", this.visWidth()).attr("height", this.visHeight()).attr("id", "graphRect").style('fill', 'white');
            this.vis.append("svg:g").attr("id", "link_container");
            this.vis.append("svg:g").attr("id", "node_container");
            $(window).resize(this.resizedWindowLambda);
            this.resizedWindowLambda();
            Fetch.CacheRegistry.clearAllServiceRecordsKeepCacheData();
        }
        OntologyMappingOverview.prototype.filterGraphOnMappingCounts = function () {
            this.filterSliders.filterGraphOnMappingCounts();
        };
        OntologyMappingOverview.prototype.redraw = function () {
        };
        OntologyMappingOverview.prototype.initAndPopulateGraph = function () {
            this.ontologyGraph = new OntologyGraph.OntologyGraph(this, this.softNodeCap, this.centralOntologyAcronym);
            this.renderScaler = new OntologyRenderScaler.OntologyRenderScaler(this.vis);
            this.filterSliders = new OntologyFilterSliders.MappingRangeSliders(this.ontologyGraph, this, this.centralOntologyAcronym);
            this.initGraph();
            this.setCurrentLayout(this.executeCenterLayoutLambda(this));
            this.prepGraphMenu();
            var expId = new ExpansionSets.ExpansionSetIdentifer("ontology_neighbourhood_" + this.centralOntologyAcronym, "Initial load: " + this.centralOntologyAcronym);
            var expansionSet = new ExpansionSets.ExpansionSet(expId, null, this.ontologyGraph, [], this.undoRedoBoss, null);
            this.ontologyGraph.fetchOntologyNeighbourhood(this.centralOntologyAcronym, expansionSet);
        };
        OntologyMappingOverview.prototype.initGraph = function () {
            this.forceLayout = d3.layout.force();
            this.nodeDragBehavior = d3.behavior.drag().on("dragstart", this.dragstartLambda(this)).on("drag", this.dragmoveLambda(this)).on("dragend", this.dragendLambda(this));
            this.forceLayout.friction(0.9).gravity(.05).charge(-200).linkDistance(this.linkMaxDesiredLength()).size([this.visWidth(), this.visHeight()]).start();
        };
        OntologyMappingOverview.prototype.dragstartLambda = function (outerThis) {
            return function (d, i) {
                outerThis.alreadyHidTipsy = false;
                outerThis.dragging = true;
                $(".tipsy").hide();
                outerThis.forceLayout.stop();
            };
        };
        OntologyMappingOverview.prototype.dragmoveLambda = function (outerThis) {
            return function (d, i) {
                if (!outerThis.alreadyHidTipsy && (d3.event.dx != 0 || d3.event.dy != 0)) {
                    TipsyToolTips.closeOtherTipsyTooltips();
                    outerThis.alreadyHidTipsy = true;
                }
                d.px += d3.event.dx;
                d.py += d3.event.dy;
                d.x += d3.event.dx;
                d.y += d3.event.dy;
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
                d.fixed = true;
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
            tBody.append($("<tr></tr>").append($("<td></td>").append($("<div></div>").text(ontologyData["rawAcronym"] + ":" + ontologyData["name"]).attr("class", "popups-Header"))).append($("<td></td>").append($("<div>").attr("id", "popups-GrabHandle"))));
            var urlText = "http://bioportal.bioontology.org/ontologies/" + ontologyData["rawAcronym"] + "?p=summary";
            tBody.append($("<tr></tr>").append($("<td></td>").attr("align", "left").css({ "vertical-align": "top" }).append($("<div></div>").css(noWrapStyle).append($("<a></a>").attr("target", "_blank").attr("href", urlText).text("Open ontology homepage in tab")))));
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
                "Mapped: ": { "key": "mapped_classes_to_central_node", "style": noWrapStyle },
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
                tBody.append($("<tr></tr>").append($("<td></td>").attr("align", "left").css({ "vertical-align": "top" }).append($("<div></div>").css(style).append($("<b></b>").text(label)).append($("<span></span>").text(value)))));
            });
            return outerDiv.prop("outerHTML");
        };
        OntologyMappingOverview.prototype.precise_round = function (num, decimals) {
            return Math.round(num * Math.pow(10, decimals)) / Math.pow(10, decimals);
        };
        OntologyMappingOverview.prototype.populateNewGraphElements = function (graphD3Format) {
            this.populateNewGraphEdges(graphD3Format.links);
            this.populateNewGraphNodes(graphD3Format.nodes);
        };
        OntologyMappingOverview.prototype.populateNewGraphEdges = function (linksData) {
            var _this = this;
            if (linksData.length === 0) {
                return [];
            }
            var links = this.vis.select("#link_container").selectAll(GraphView.BaseGraphView.linkSvgClass).data(linksData, OntologyGraph.Link.D3IdentityFunction);
            var enteringLinks = links.enter().append("svg:line").attr("class", GraphView.BaseGraphView.linkSvgClassSansDot + " " + GraphView.BaseGraphView.ontologyLinkSvgClassSansDot).attr("id", function (d) {
                return "link_line_" + d.source.acronymForIds + "-to-" + d.target.acronymForIds;
            }).on("mouseover", this.highlightHoveredLinkLambda(this)).on("mouseout", this.unhighlightHoveredLinkLambda(this));
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
                enteringLinks.append("title").text(function (d) {
                    return "Number Of Mappings: " + d.numMappings;
                }).attr("id", function (d) {
                    return "link_title_" + d.source.acronymForIds + "-to-" + d.target.acronymForIds;
                });
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
            var nodes = this.vis.select("#node_container").selectAll("g.node_g").data(nodesData, OntologyGraph.Node.D3IdentityFunction);
            var enteringNodes = nodes.enter().append("svg:g").attr("class", GraphView.BaseGraphView.nodeGSvgClassSansDot).attr("id", function (d) {
                return "node_g_" + d.acronymForIds;
            }).call(this.nodeDragBehavior);
            enteringNodes.append("svg:circle").attr("id", function (d) {
                return "node_circle_" + d.acronymForIds;
            }).attr("class", GraphView.BaseGraphView.nodeSvgClassSansDot + " " + GraphView.BaseGraphView.ontologyNodeSvgClassSansDot).attr("cx", "0px").attr("cy", "0px").style("fill", this.defaultNodeColor).style("stroke", this.ontologyGraph.darkenColor(this.defaultNodeColor)).attr("data-radius_basis", function (d) {
                return d.number;
            }).attr("r", function (d) {
                return _this.renderScaler.ontologyNodeScalingFunc(d.number, d.rawAcronym);
            }).on("mouseover", this.highlightHoveredNodeLambda(this, true)).on("mouseout", this.unhighlightHoveredNodeLambda(this, true));
            enteringNodes.append("svg:circle").attr("id", function (d) {
                return "node_circle_inner_" + d.acronymForIds;
            }).attr("class", GraphView.BaseGraphView.nodeInnerSvgClassSansDot + " " + GraphView.BaseGraphView.ontologyNodeSvgClassSansDot).attr("cx", "0px").attr("cy", "0px").attr("pointer-events", "none").style("fill", this.ontologyGraph.brightenColor(this.defaultNodeColor)).style("stroke", this.ontologyGraph.darkenColor(this.defaultNodeColor)).attr("data-inner_radius_basis", function (d) {
                return d.mapped_classes_to_central_node;
            }).attr("data-outer_radius_basis", function (d) {
                return d.number;
            }).attr("r", function (d) {
                return _this.renderScaler.ontologyInnerNodeScalingFunc(d.mapped_classes_to_central_node, d.number, d.rawAcronym);
            }).on("mouseover", this.highlightHoveredNodeLambda(this, true)).on("mouseout", this.unhighlightHoveredNodeLambda(this, true));
            enteringNodes.append("svg:text").attr("id", function (d) {
                return "node_text_" + d.acronymForIds;
            }).attr("class", GraphView.BaseGraphView.nodeLabelSvgClassSansDot + " unselectable").attr("dx", 12).attr("dy", 1).text(function (d) {
                return d.name;
            }).style("pointer-events", "none").attr("unselectable", "on").attr("onmousedown", "noselect").attr("onselectstart", "function(){ return false;}");
            enteringNodes.each(TipsyToolTips.nodeTooltipOnClickLambda(this));
            if (!enteringNodes.empty()) {
                this.stampTimeGraphModified();
            }
            if (!enteringNodes.empty()) {
                this.forceLayout.on("tick", this.onLayoutTick());
            }
            this.filterSliders.changeTopMappingSliderValues(null, this.softNodeCap);
            if (!enteringNodes.empty()) {
                this.updateStartWithoutResume();
                this.renderScaler.updateNodeScalingFactor();
            }
        };
        OntologyMappingOverview.prototype.removeMissingGraphElements = function (graphD3Format) {
            this.forceLayout.nodes(graphD3Format.nodes);
            this.forceLayout.links(graphD3Format.links);
            var nodes = this.vis.selectAll("g.node_g").data(graphD3Format.nodes, OntologyGraph.Node.D3IdentityFunction);
            var links = this.vis.selectAll("." + GraphView.BaseGraphView.linkSvgClassSansDot).data(graphD3Format.links, OntologyGraph.Link.D3IdentityFunction);
            var nodesRemoved = nodes.exit().remove();
            var linksRemoved = links.exit().remove();
            this.filterSliders.updateTopMappingsSliderRange();
            this.filterSliders.rangeSliderSlideEvent(null, null);
            this.stampTimeGraphModified();
            this.forceLayout.on("tick", this.onLayoutTick());
            this.currentLambda();
        };
        OntologyMappingOverview.prototype.onLayoutTick = function () {
            var _this = this;
            var lastLabelShiftTime = jQuery.now();
            var lastGravityAdjustmentTime = jQuery.now();
            var firstTickTime = jQuery.now();
            var maxLayoutRunDuration = 10000;
            var maxGravityFrequency = 4000;
            var nodes = this.vis.selectAll("g.node_g");
            var links = this.vis.selectAll(GraphView.BaseGraphView.linkSvgClass);
            return function () {
                if (_this.forceLayout.alpha() < _this.alphaCutoff || jQuery.now() - firstTickTime > maxLayoutRunDuration) {
                    _this.forceLayout.stop();
                }
                var doLabelUpdateNextTime = false;
                if (jQuery.now() - lastGravityAdjustmentTime > maxGravityFrequency) {
                    nodes.attr("transform", function (d) {
                        return "translate(" + _this.gravityAdjustX(d.x) + "," + _this.gravityAdjustY(d.y) + ")";
                    });
                    lastGravityAdjustmentTime = jQuery.now();
                    doLabelUpdateNextTime = true;
                }
                else {
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
            };
        };
        OntologyMappingOverview.prototype.updateDataForNodesAndLinks = function (json) {
            var outerThis = this;
            var updateLinksFromJson = function (i, d) {
                var link = this.vis.select("#link_line_" + d.source.acronymForIds + "-to-" + d.target.acronymForIds);
                link.attr("data-thickness_basis", function (d) {
                    return d.value;
                });
                link.select("title").text(function (d) {
                    return "Number Of Mappings: " + d.numMappings;
                });
            };
            var updateNodesFromJson = function (i, d) {
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
                var inner_circles = node.select(GraphView.BaseGraphView.nodeInnerSvgClass);
                inner_circles.attr("data-inner_radius_basis", d.mapped_classes_to_central_node);
                inner_circles.attr("data-outer_radius_basis", d.number);
                inner_circles.transition().style("fill", d.innerNodeColor).style("stroke", d.nodeStrokeColor);
                if (outerThis.lastDisplayedTipsy != null && outerThis.lastDisplayedTipsy.css("visibility") == "visible") {
                    $(outerThis.lastDisplayedTipsy).children(".tipsy-inner").html(outerThis.createNodePopupTable(outerThis.lastDisplayedTipsySvg, outerThis.lastDisplayedTipsyData));
                }
            };
            $.each(json.links, updateLinksFromJson);
            $.each(json.nodes, updateNodesFromJson);
            if (outerThis.nodeUpdateTimer == false) {
                outerThis.nodeUpdateTimer = true;
                var busyCursorIdentifer = "ScalingTimer_" + new Date().toDateString();
                MouseSpinner.MouseSpinner.applyMouseSpinner(busyCursorIdentifer);
                window.setTimeout(function () {
                    console.log("TIMER RESET");
                    outerThis.nodeUpdateTimer = false;
                    outerThis.renderScaler.updateNodeScalingFactor();
                    MouseSpinner.MouseSpinner.haltSpinner(busyCursorIdentifer);
                }, 1000);
            }
        };
        OntologyMappingOverview.prototype.removeGraphPopulation = function (data) {
            console.log("Removing some graph elements " + Utils.getTime());
            var nodes = this.vis.selectAll("g.node_g").data(this.ontologyGraph.graphD3Format.nodes, OntologyGraph.Node.D3IdentityFunction);
            var links = this.vis.selectAll(GraphView.BaseGraphView.linkSvgClass).data(this.ontologyGraph.graphD3Format.links, OntologyGraph.Link.D3IdentityFunction);
            var nodesRemoved = nodes.exit().remove();
            var linksRemoved = links.exit().remove();
            if (!nodesRemoved.empty() || !linksRemoved.empty()) {
                this.updateStartWithoutResume();
            }
            this.filterSliders.updateTopMappingsSliderRange();
            this.filterSliders.rangeSliderSlideEvent(null, null);
        };
        OntologyMappingOverview.prototype.gravityAdjust = function (numb, visSize) {
            var alpha = 0.2 / this.forceLayout.nodes().length;
            if (numb < visSize * 0.05 || visSize * 0.95 < numb) {
                this.forceLayout.gravity(Math.min(this.maxGravity, this.forceLayout.gravity() * (1 + alpha)));
            }
            else if (visSize * 0.20 < numb && numb < visSize * 0.80) {
                this.forceLayout.gravity(Math.max(this.minGravity, this.forceLayout.gravity() * (1 - alpha)));
            }
            else {
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
                }
                var graphNodes = outerThis.ontologyGraph.graphD3Format.nodes;
                var graphLinks = outerThis.ontologyGraph.graphD3Format.links;
                var numberOfNodes = $(GraphView.BaseGraphView.nodeSvgClass).filter(function (i, d) {
                    return $(d).css("display") !== "none";
                }).length;
                _this.forceLayout.friction(0.01);
                _this.forceLayout.stop();
                var anglePerNode = 2 * Math.PI / (numberOfNodes - 1);
                var arcLength = _this.linkMaxDesiredLength();
                var i = 0;
                $.each(_this.ontologyGraph.sortedAcronymsByMappingCount, function (index, sortedAcronym) {
                    var acronym = sortedAcronym.acronym;
                    var node = sortedAcronym.node;
                    if (typeof node === "undefined") {
                        console.log("Undefined ontology entry");
                    }
                    var display = $("#node_circle_" + node.acronymForIds).css("display");
                    if ((display == null || display === "none")) {
                        node.x = outerThis.visWidth() / 2;
                        node.y = outerThis.visHeight() / 2;
                    }
                    else if (node.rawAcronym !== outerThis.centralOntologyAcronym) {
                        var angleForNode = i * anglePerNode;
                        i++;
                        node.x = outerThis.visWidth() / 2 + arcLength * Math.cos(angleForNode);
                        node.y = outerThis.visHeight() / 2 + arcLength * Math.sin(angleForNode);
                    }
                    else {
                        node.x = outerThis.visWidth() / 2;
                        node.y = outerThis.visHeight() / 2;
                    }
                });
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
            this.menu.initializeMenu();
            this.filterSliders.addMenuComponents(this.menu.getMenuSelector(), this.softNodeCap);
            this.legend.initialize();
        };
        OntologyMappingOverview.prototype.sortConceptNodesCentralOntologyName = function () {
            var outerThis = this;
            return this.ontologyGraph.graphD3Format.nodes.sort(function (a, b) {
                if (a.rawAcronym === b.rawAcronym) {
                    return 0;
                }
                return (a.rawAcronym < b.rawAcronym) ? -1 : 1;
            });
        };
        return OntologyMappingOverview;
    })(GraphView.BaseGraphView);
    exports.OntologyMappingOverview = OntologyMappingOverview;
});
