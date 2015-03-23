var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../Utils", "../MouseSpinner", "../FetchFromApi", "../Menu", "../GraphView", "../ExpansionSets", "../DeletionSet", "../TipsyToolTipsOnClick", "../CompositeExpansionDeletionSet", "./ConceptGraph", "./NestedOntologyConceptFilter", "./NestedExpansionSetConceptFilter", "./ConceptEdgeTypeFilter", "./ConceptFilterSliders", "./ConceptTour", "./ConceptLayouts", "./GraphImporterExporter", "../NodeFinderWidgets", "JQueryExtension", "Utils", "MouseSpinner", "Menu", "GraphView", "ExpansionSets", "DeletionSet", "FetchFromApi", "TipsyToolTips", "TipsyToolTipsOnClick", "UndoRedo/UndoRedoManager", "CompositeExpansionDeletionSet", "Concepts/ConceptGraph", "Concepts/ConceptFilterSliders", "Concepts/CherryPickConceptFilter", "Concepts/OntologyConceptFilter", "Concepts/NestedOntologyConceptFilter", "Concepts/ExpansionSetFilter", "Concepts/NestedExpansionSetConceptFilter", "Concepts/ConceptNodeFilterWidget", "Concepts/ConceptEdgeTypeFilter", "Concepts/ConceptFilterSliders", "Concepts/ConceptTour", "Concepts/ConceptLayouts", "Concepts/GraphImporterExporter", "NodeFinderWidgets", "Concepts/ConceptRenderScaler"], function (require, exports, Utils, MouseSpinner, Fetch, Menu, GraphView, ExpansionSets, DeletionSet, TipsyToolTipsOnClick, CompositeExpansionDeletionSet, ConceptGraph, NestedOntologyConceptFilter, NestedExpansionSetConceptFilter, ConceptEdgeTypeFilter, ConceptFilterSliders, ConceptTour, ConceptLayouts, ImporterExporter, NodeFinder) {
    var ConceptPathsToRoot = (function (_super) {
        __extends(ConceptPathsToRoot, _super);
        function ConceptPathsToRoot(centralOntologyAcronym, centralConceptSimpleUri, softNodeCap) {
            var _this = this;
            _super.call(this);
            this.centralOntologyAcronym = centralOntologyAcronym;
            this.centralConceptSimpleUri = centralConceptSimpleUri;
            this.softNodeCap = softNodeCap;
            this.nodeHeight = 8;
            this.expansionBoxWidth = 30;
            this.expansionBoxHeight = 8;
            this.nodeLabelPaddingWidth = 10;
            this.nodeLabelPaddingHeight = 10;
            this.enteringElementTransitionDuration = 1000;
            this.exitingElementTransitionDuration = 300;
            this.alreadyHidTipsy = false;
            this.pixelMap = [0, 10, -10, -7, -4, 4, 7];
            this.updateArcLineFunc = function (linkData, ignoreOffset) {
                if (ignoreOffset === void 0) { ignoreOffset = false; }
                var offset = _this.pixelMap[linkData.edgePositionSlot];
                if (ignoreOffset === true) {
                    offset = 0;
                }
                var sourceX = linkData.source.x;
                var sourceY = linkData.source.y;
                var targetX = linkData.target.x;
                var targetY = linkData.target.y;
                if (offset != 0) {
                    var targetVectorX = targetX - sourceX;
                    var targetVectorY = targetY - sourceY;
                    var norm = Math.sqrt(targetVectorX * targetVectorX + targetVectorY * targetVectorY);
                    var targetOrthVectorX = -1 * targetVectorY / norm;
                    var targetOrthVectorY = targetVectorX / norm;
                    var xDist = offset * targetOrthVectorX;
                    var yDist = offset * targetOrthVectorY;
                    sourceX += xDist;
                    sourceY += yDist;
                    targetX += xDist;
                    targetY += yDist;
                }
                var points = sourceX + "," + sourceY + " " + targetX + "," + targetY + " ";
                return points;
            };
            this.updateArcMarkerFunc = function (linkData, ignoreOffset) {
                if (ignoreOffset === void 0) { ignoreOffset = false; }
                if (linkData.relationType === _this.conceptGraph.relationLabelConstants.inheritance) {
                    return _this.computeArcMarkerForInheritance(linkData, ignoreOffset);
                }
                else if (linkData.relationType === _this.conceptGraph.relationLabelConstants.composition) {
                    return _this.computeArcMarkerForComposition(linkData, ignoreOffset);
                }
                else if (linkData.relationType === _this.conceptGraph.relationLabelConstants.mapping) {
                    return _this.computeArcMarkerForMapping(linkData, ignoreOffset);
                }
                else {
                    return _this.computeArcMarkerPropertyRelations(linkData, ignoreOffset);
                }
            };
            this.triSegLen = 14;
            this.triangleMarkerPointAngle = (25 / 360) * (2 * Math.PI);
            this.diamondLength = 14.0;
            this.diamondAngle = (45 / 360) * (2 * Math.PI);
            this.giveIEMarkerWarning = true;
            this.propertyRelationClassNames = ["inheritanceStyleLink", "compositionStyleLink", "mappingStyleLink"];
            if (null != this.centralConceptSimpleUri && null != this.centralOntologyAcronym) {
                this.centralConceptUri = ConceptGraph.ConceptGraph.computeNodeId(this.centralConceptSimpleUri, this.centralOntologyAcronym);
            }
            this.undoRedoBoss.initGui();
            this.menu = new Menu.Menu();
            this.tour = new ConceptTour.Tour(this, this.menu);
            this.visualization = $(ConceptPathsToRoot.VIZ_SELECTOR_ID + " option:selected").text();
            $(ConceptPathsToRoot.VIZ_SELECTOR_ID).change(function () {
                var selected = $(ConceptPathsToRoot.VIZ_SELECTOR_ID + " option:selected").text();
                if (_this.visualization !== selected) {
                    _this.visualization = selected;
                    _this.fetchInitialExpansion();
                }
            });
        }
        ConceptPathsToRoot.prototype.filterGraphOnMappingCounts = function () {
        };
        ConceptPathsToRoot.prototype.refreshVisualizationModeLambda = function () {
            var _this = this;
            return function (command) {
                $(ConceptPathsToRoot.VIZ_SELECTOR_ID + " option").each(function (i, selectedElement) {
                    if (command.getDisplayName().indexOf($(selectedElement).text()) != -1) {
                        _this.visualization = $(selectedElement).text();
                        $(ConceptPathsToRoot.VIZ_SELECTOR_ID + " option").each(function (i, deselectedElement) {
                            $(deselectedElement).attr("selected", null);
                        });
                        $(selectedElement).attr("selected", "selected");
                        return;
                    }
                });
            };
        };
        ConceptPathsToRoot.prototype.recomputeVisualizationOntoNode = function (node) {
            this.centralConceptSimpleUri = node.simpleConceptUri;
            this.centralOntologyAcronym = node.ontologyAcronym;
            this.centralConceptUri = ConceptGraph.ConceptGraph.computeNodeId(this.centralConceptSimpleUri, this.centralOntologyAcronym);
            this.fetchInitialExpansion(node);
        };
        ConceptPathsToRoot.prototype.cleanSlate = function () {
            if (this.forceLayout !== undefined) {
                this.forceLayout.nodes([]);
                this.forceLayout.links([]);
            }
            $("#chart").empty();
            d3.select("#chart").remove;
            var outerThis = this;
            this.vis = d3.select("#chart").append("svg:svg").attr("id", "graphSvg").attr("width", this.visWidth()).attr("height", this.visHeight()).attr("pointer-events", "all").on("click", function () {
                TipsyToolTipsOnClick.closeOtherTipsyTooltips();
            });
            this.vis.append("svg:rect").attr("width", this.visWidth()).attr("height", this.visHeight()).attr("id", "graphRect").style("fill", "white");
            this.vis.append("svg:g").attr("id", "link_container");
            this.vis.append("svg:g").attr("id", "node_container");
            $(window).resize(this.resizedWindowLambda);
            this.resizedWindowLambda();
            Fetch.CacheRegistry.clearAllServiceRecordsKeepCacheData();
        };
        ConceptPathsToRoot.prototype.redraw = function () {
        };
        ConceptPathsToRoot.prototype.initAndPopulateGraph = function () {
            this.cleanSlate();
            this.conceptGraph = new ConceptGraph.ConceptGraph(this, this.centralConceptUri, this.softNodeCap, this.undoRedoBoss);
            this.initGraph();
            this.filterSliders = new ConceptFilterSliders.ConceptRangeSliders(this.conceptGraph, this, this.centralConceptUri);
            this.layouts = new ConceptLayouts.ConceptLayouts(this.forceLayout, this.conceptGraph, this, this.centralConceptUri);
            this.conceptGraph.setLayoutProvider(this.layouts);
            this.nodeFinder = new NodeFinder.NodeFinder(this, this.conceptGraph);
            this.importerExporterWidget = new ImporterExporter.Widget(this);
            this.edgeTypeFilter = new ConceptEdgeTypeFilter.ConceptEdgeTypeFilter(this.conceptGraph, this, this.centralConceptUri);
            this.nestedOntologyConceptFilter = new NestedOntologyConceptFilter.NestedOntologyConceptFilter(this.conceptGraph, this, this.centralConceptUri);
            this.nestedExpansionConceptFilter = new NestedExpansionSetConceptFilter.NestedExpansionSetConceptFilter(this.conceptGraph, this, this.centralConceptUri);
            this.setCurrentLayout(this.layouts.runForceLayoutLambda());
            this.forceLayout.stop();
            this.forceLayout.start();
            this.forceLayout.resume();
            this.runCurrentLayout();
            this.prepGraphMenu();
            this.fetchInitialExpansion();
            MouseSpinner.MouseSpinner.haltSpinner("ConceptMain");
        };
        ConceptPathsToRoot.prototype.prepareForExpansionFromScratch = function (expId, expansionType, initParent) {
            var initSet = new CompositeExpansionDeletionSet.InitializationDeletionSet(this.conceptGraph, expId, this.undoRedoBoss, expansionType, initParent);
            initSet.getGraphModifier().addActiveStepCallback(this.refreshVisualizationModeLambda());
            this.deleteNodesForGraphInitialization(initSet, initParent);
            return initSet;
        };
        ConceptPathsToRoot.prototype.deleteNodesForGraphInitialization = function (initSet, exceptionFromDeletion) {
            var toDelete = this.conceptGraph.graphD3Format.nodes.filter(function (node, i) {
                return exceptionFromDeletion !== node;
            });
            initSet.addAllDeleting(toDelete);
            initSet.getGraphModifier().executeRedo();
        };
        ConceptPathsToRoot.prototype.fetchInitialExpansion = function (incomingRoot) {
            if (this.centralOntologyAcronym === undefined || this.centralConceptUri === undefined) {
                console.log("No ontology acoronym or no central concept id, empty graph left unfilled.");
                this.importerExporterWidget.openShareAndImportMenu();
                return;
            }
            var expId = new ExpansionSets.ExpansionSetIdentifer("conceptPathToRootInitialExpansion_" + this.centralOntologyAcronym + "__" + Utils.escapeIdentifierForId(this.centralConceptUri), String(this.visualization));
            var initSet = this.prepareForExpansionFromScratch(expId, this.visualization, incomingRoot);
            var expansionSet = initSet.expansionSet;
            if (this.visualization === ConceptGraph.PathOptionConstants.pathsToRootConstant) {
                this.setCurrentLayout(this.layouts.runVerticalTreeLayoutLambda());
                this.conceptGraph.fetchPathToRoot(this.centralOntologyAcronym, this.centralConceptSimpleUri, expansionSet, initSet);
            }
            else if (this.visualization === ConceptGraph.PathOptionConstants.termNeighborhoodConstant) {
                this.conceptGraph.fetchTermNeighborhood(this.centralOntologyAcronym, this.centralConceptSimpleUri, expansionSet, initSet);
            }
            else if (this.visualization === ConceptGraph.PathOptionConstants.mappingsNeighborhoodConstant) {
                this.setCurrentLayout(this.layouts.runCenterLayoutLambda());
                this.conceptGraph.fetchMappingsNeighborhood(this.centralOntologyAcronym, this.centralConceptSimpleUri, expansionSet, initSet);
                this.runCurrentLayout();
            }
        };
        ConceptPathsToRoot.prototype.conceptLinkSimplePopupFunction = function (d) {
            return "From: " + d.source.name + " (" + d.source.ontologyAcronym + ")" + " To: " + d.target.name + " (" + d.target.ontologyAcronym + ")" + " [" + d.relationLabel + "]";
        };
        ConceptPathsToRoot.prototype.conceptNodeSimplePopupFunction = function (d) {
            return "Number Of Terms: " + d.number;
        };
        ConceptPathsToRoot.prototype.conceptNodeLabelFunction = function (d) {
            return d.name;
        };
        ConceptPathsToRoot.prototype.initGraph = function () {
            this.forceLayout = d3.layout.force();
            this.forceLayout.nodes(this.conceptGraph.graphD3Format.nodes);
            this.forceLayout.links(this.conceptGraph.graphD3Format.links);
            this.nodeDragBehavior = d3.behavior.drag().on("dragstart", this.dragstartLambda(this)).on("drag", this.dragmoveLambda(this)).on("dragend", this.dragendLambda(this));
            this.forceLayout.size([this.visWidth(), this.visHeight()]).linkDistance(this.linkMaxDesiredLength());
        };
        ConceptPathsToRoot.prototype.collide = function (node) {
            var nodeR = parseInt(d3.select("#node_rect_" + node.conceptUriForIds).attr("width")) / 2;
            var nx1 = node.x - nodeR, nx2 = node.x + nodeR, ny1 = node.y - nodeR, ny2 = node.y + nodeR;
            return function (quad, x1, y1, x2, y2) {
                if (quad.point && (quad.point !== node)) {
                    var x = node.x - quad.point.x, y = node.y - quad.point.y, l = Math.sqrt(x * x + y * y);
                    var qpnoder = nodeR + parseInt(d3.select("#node_rect_" + quad.point.conceptUriForIds).attr("width")) / 2;
                    if (l < qpnoder) {
                        l = (l - qpnoder) / l * .5;
                        node.x -= x *= l;
                        node.y -= y *= l;
                        quad.point.x += x;
                        quad.point.y += y;
                    }
                }
                return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
            };
        };
        ConceptPathsToRoot.prototype.onLayoutTick = function () {
            var _this = this;
            var lastLabelShiftTime = jQuery.now();
            var lastGravityAdjustmentTime = jQuery.now();
            var firstTickTime = jQuery.now();
            var maxLayoutRunDuration = 10000;
            var maxGravityFrequency = 4000;
            return function () {
                var boundNodes = _this.vis.selectAll("g.node_g");
                var boundLinks = _this.vis.selectAll("polyline" + GraphView.BaseGraphView.linkSvgClass);
                var boundLinkMarkers = _this.vis.selectAll("polyline" + GraphView.BaseGraphView.linkMarkerSvgClass);
                if (_this.forceLayout.alpha() < _this.alphaCutoff || jQuery.now() - firstTickTime > maxLayoutRunDuration) {
                    _this.forceLayout.stop();
                    _this.forceLayout.alpha(0);
                }
                if (boundNodes.length > 0) {
                    var width = _this.visWidth();
                    var height = _this.visHeight();
                    var nodeHeight = _this.nodeHeight + _this.nodeLabelPaddingHeight / 2;
                    boundNodes.attr("transform", function (d) {
                        var nodeWidth = parseInt(d3.select("#node_rect_" + d.conceptUriForIds).attr("width")) / 2;
                        d.x = Math.max(nodeWidth, Math.min(width - nodeWidth, d.x));
                        d.y = Math.max(nodeHeight, Math.min(height - nodeHeight, d.y));
                        return "translate(" + d.x + "," + d.y + ")";
                    });
                }
                if (boundLinks.length > 0) {
                    boundLinks.attr("points", _this.updateArcLineFunc);
                    boundLinkMarkers.attr("points", _this.updateArcMarkerFunc);
                }
                if (null != _this.tour) {
                    _this.tour.refreshIntro();
                }
            };
        };
        ConceptPathsToRoot.prototype.dragstartLambda = function (outerThis) {
            return function (d, i) {
                outerThis.dragging = true;
                outerThis.alreadyHidTipsy = false;
                outerThis.forceLayout.stop();
            };
        };
        ConceptPathsToRoot.prototype.dragmoveLambda = function (outerThis) {
            return function (d, i) {
                if (!outerThis.alreadyHidTipsy && (d3.event.dx != 0 || d3.event.dy != 0)) {
                    TipsyToolTipsOnClick.closeOtherTipsyTooltips();
                    outerThis.alreadyHidTipsy = true;
                }
                d.px += d3.event.dx;
                d.py += d3.event.dy;
                d.x += d3.event.dx;
                d.y += d3.event.dy;
                var width = outerThis.visWidth();
                var height = outerThis.visHeight();
                var nodeHeight = outerThis.nodeHeight + outerThis.nodeLabelPaddingHeight / 2;
                d3.select(this).attr("transform", function (d) {
                    var nodeWidth = parseInt(d3.select("#node_rect_" + d.conceptUriForIds).attr("width")) / 2;
                    d.x = Math.max(nodeWidth, Math.min(width - nodeWidth, d.x));
                    d.y = Math.max(nodeHeight, Math.min(height - nodeHeight, d.y));
                    return "translate(" + d.x + "," + d.y + ")";
                });
                outerThis.vis.selectAll("polyline" + GraphView.BaseGraphView.linkSvgClass).filter(function (e) {
                    return e.source === d || e.target === d;
                }).attr("points", outerThis.updateArcLineFunc);
                outerThis.vis.selectAll("polyline" + GraphView.BaseGraphView.linkMarkerSvgClass).filter(function (e) {
                    return e.source === d || e.target === d;
                }).attr("points", outerThis.updateArcMarkerFunc);
            };
        };
        ConceptPathsToRoot.prototype.computeArcMarkerForInheritance = function (linkData, ignoreOffset) {
            if (ignoreOffset === void 0) { ignoreOffset = false; }
            var sourceX = linkData.source.x;
            var sourceY = linkData.source.y;
            var targetX = linkData.target.x;
            var targetY = linkData.target.y;
            var midPointX = sourceX + (targetX - sourceX) / 2;
            var midPointY = sourceY + (targetY - sourceY) / 2;
            var atanSourceTarget = Math.PI * 0.5 - Math.atan2(sourceX - targetX, sourceY - targetY);
            var triAngle1 = atanSourceTarget + this.triangleMarkerPointAngle;
            var triAngle2 = atanSourceTarget - this.triangleMarkerPointAngle;
            var triPointX1 = (this.triSegLen * Math.cos(triAngle1));
            var triPointY1 = (this.triSegLen * Math.sin(triAngle1));
            var triPointX2 = (this.triSegLen * Math.cos(triAngle2));
            var triPointY2 = (this.triSegLen * Math.sin(triAngle2));
            triPointX1 += midPointX;
            triPointY1 += midPointY;
            triPointX2 += midPointX;
            triPointY2 += midPointY;
            var points = +midPointX + "," + midPointY + " " + triPointX1 + "," + triPointY1 + " " + triPointX2 + "," + triPointY2 + " " + midPointX + "," + midPointY + " ";
            return points;
        };
        ConceptPathsToRoot.prototype.computeArcMarkerForComposition = function (linkData, ignoreOffset) {
            if (ignoreOffset === void 0) { ignoreOffset = false; }
            var offset = this.pixelMap[linkData.edgePositionSlot];
            if (ignoreOffset === true) {
                offset = 0;
            }
            var sourceX = linkData.source.x;
            var sourceY = linkData.source.y;
            var targetX = linkData.target.x;
            var targetY = linkData.target.y;
            var targetVectorX = targetX - sourceX;
            var targetVectorY = targetY - sourceY;
            var norm = Math.sqrt(targetVectorX * targetVectorX + targetVectorY * targetVectorY);
            var targetOrthVectorX = -1 * targetVectorY / norm;
            var targetOrthVectorY = targetVectorX / norm;
            var xDist = offset * targetOrthVectorX;
            var yDist = offset * targetOrthVectorY;
            if (linkData.relationType === this.conceptGraph.relationLabelConstants.composition) {
                sourceX += xDist;
                sourceY += yDist;
                targetX += xDist;
                targetY += yDist;
            }
            var atanSourceTarget = Math.PI * 0.5 - Math.atan2(sourceX - targetX, sourceY - targetY);
            var diamondAngle1 = atanSourceTarget + (this.diamondAngle);
            var diamondAngle2 = atanSourceTarget - (this.diamondAngle);
            var asq = (this.diamondLength / 2) * (this.diamondLength / 2);
            var diamondSideLength = Math.sqrt(asq + asq);
            var triPointX1 = (diamondSideLength * Math.cos(diamondAngle1));
            var triPointY1 = (diamondSideLength * Math.sin(diamondAngle1));
            var triPointX2 = -1 * (diamondSideLength * Math.cos(diamondAngle1));
            var triPointY2 = -1 * (diamondSideLength * Math.sin(diamondAngle1));
            var diamondXDelta = (this.diamondLength / 2 * Math.cos(atanSourceTarget));
            var diamondYDelta = (this.diamondLength / 2 * Math.sin(atanSourceTarget));
            var midPointX1 = sourceX + (targetX - sourceX) / 2 - diamondXDelta;
            var midPointY1 = sourceY + (targetY - sourceY) / 2 - diamondYDelta;
            var midPointX2 = sourceX + (targetX - sourceX) / 2 + diamondXDelta;
            var midPointY2 = sourceY + (targetY - sourceY) / 2 + diamondYDelta;
            triPointX1 += midPointX1;
            triPointY1 += midPointY1;
            triPointX2 += midPointX2;
            triPointY2 += midPointY2;
            var points = +midPointX1 + "," + midPointY1 + " " + triPointX1 + "," + triPointY1 + " " + midPointX2 + "," + midPointY2 + " " + triPointX2 + "," + triPointY2 + " " + midPointX1 + "," + midPointY1 + " ";
            return points;
        };
        ConceptPathsToRoot.prototype.computeArcMarkerForMapping = function (linkData, ignoreOffset) {
            if (ignoreOffset === void 0) { ignoreOffset = false; }
            return "";
        };
        ConceptPathsToRoot.prototype.computeArcMarkerPropertyRelations = function (linkData, ignoreOffset) {
            if (ignoreOffset === void 0) { ignoreOffset = false; }
            return "";
        };
        ConceptPathsToRoot.prototype.dragendLambda = function (outerThis) {
            return function (d, i) {
                outerThis.dragging = false;
            };
        };
        ConceptPathsToRoot.prototype.createNodePopupTable = function (conceptRect, conceptData) {
            var outerDiv = $("<div></div>");
            outerDiv.addClass("popups-Popup");
            var noWrapStyle = { "white-space": "nowrap" };
            var wrapStyle = {};
            var table = $("<table></table>");
            var tBody = $("<tbody></tbody>");
            outerDiv.append(table);
            table.append(tBody);
            tBody.append($("<tr></tr>").append($("<td></td>").append($("<div></div>").text(conceptData["name"]).attr("class", "popups-Header"))).append($("<td></td>").append($("<div>").attr("id", "popups-GrabHandle"))));
            {
                var outerThis = this;
                var checkboxInput = $("<input type='checkbox' " + (this.isNodeHidden(conceptData) ? "checked=checked" : "") + ">").attr("id", "popupCheckId");
                $(document.body).off().on("change", "#popupCheckId", function () {
                    console.log("hey");
                    outerThis.toggleHideNodeLambda(outerThis)(conceptData, 0);
                    outerThis.refreshOtherFilterCheckboxStates([conceptData], null);
                });
                var checkboxUnit = $("<span>").addClass("popupNodeHideCheckbox").append(checkboxInput).append($("<label>").attr("for", "popupCheckId").append("Hide Node"));
                tBody.append($("<tr></tr>").append($("<td></td>").append(checkboxUnit)));
            }
            var urlText = "http://bioportal.bioontology.org/ontologies/" + conceptData["ontologyAcronym"] + "?p=classes&conceptid=" + conceptData["simpleConceptUri"];
            tBody.append($("<tr></tr>").append($("<td></td>").attr("align", "left").css({ "vertical-align": "top" }).append($("<div></div>").css(noWrapStyle).append($("<a></a>").attr("target", "_blank").attr("href", urlText).text("Open concept homepage in tab")))));
            var ontologyUrlText = "http://bioportal.bioontology.org/ontologies/" + conceptData["ontologyAcronym"];
            tBody.append($("<tr></tr>").append($("<td></td>").attr("align", "left").css({ "vertical-align": "top" }).append($("<div></div>").css(noWrapStyle).append($("<b></b>").text("Ontology: ")).append($("<a></a>").attr("target", "_blank").attr("href", ontologyUrlText).text(conceptData["ontologyAcronym"])))));
            var jsonArgs = {
                "Concept ID: ": { "key": "simpleConceptUri", "style": noWrapStyle },
                "Synonyms: ": { "key": "synonym", "style": wrapStyle },
                "Definition: ": { "key": "definition", "style": wrapStyle }
            };
            $.each(jsonArgs, function (label, properties) {
                var style = properties["style"];
                var propertyKey = properties["key"];
                tBody.append($("<tr></tr>").append($("<td></td>").attr("align", "left").css({ "vertical-align": "top" }).append($("<div></div>").css(style).append($("<b></b>").text(label)).append($("<span></span>").text(conceptData[propertyKey])))));
            });
            return outerDiv.prop("outerHTML");
        };
        ConceptPathsToRoot.prototype.updateDataForNodesAndLinks = function (json) {
            var outerThis = this;
            var updateLinksFromJson = function (i, d) {
                var link = outerThis.vis.select("#link_line_" + d.id);
                link.select("title").text(function (d) {
                    return d.value;
                });
            };
            var updateNodesFromJson = function (i, d) {
                var node = outerThis.vis.select("#node_g_" + d.conceptUriForIds);
                var nodeRects = node.select(GraphView.BaseGraphView.nodeSvgClassSansDot);
                nodeRects.transition().style("fill", d.nodeColor);
                node.select("title").text(outerThis.conceptNodeSimplePopupFunction);
                node.select("text").text(outerThis.conceptNodeLabelFunction).attr("x", function () {
                    return -this.getComputedTextLength() / 2;
                });
                if (outerThis.lastDisplayedTipsy != null && outerThis.lastDisplayedTipsy.css("visibility") == "visible") {
                    console.log("This wont' work anymore");
                }
            };
            $.each(json.links, updateLinksFromJson);
            $.each(json.nodes, updateNodesFromJson);
        };
        ConceptPathsToRoot.prototype.populateNewGraphElements = function (graphD3Format) {
            this.populateNewGraphEdges(graphD3Format.links);
            this.populateNewGraphNodes(graphD3Format.nodes);
        };
        ConceptPathsToRoot.prototype.populateNewGraphEdges = function (linksData, temporaryEdges) {
            if (linksData.length == 0) {
                return [];
            }
            var outerThis = this;
            var links = this.vis.select("#link_container").selectAll("polyline" + GraphView.BaseGraphView.linkSvgClass).data(linksData, ConceptGraph.Link.d3IdentityFunc);
            var enteringLinks = links.enter().append("svg:g").attr("class", function (d) {
                return GraphView.BaseGraphView.linkSvgClassSansDot + " " + GraphView.BaseGraphView.linkClassSelectorPrefix + d.relationType + " " + outerThis.getLinkCssClass(d.relationType, d.relationSpecificToOntologyAcronym);
            }).attr("id", function (d) {
                return "link_g_" + d.id;
            });
            var enteringSubG = enteringLinks.append("svg:g").attr("id", function (d) {
                return "link_sub_g_" + d.id;
            }).attr("class", GraphView.BaseGraphView.linkSubGSvgClassSansDot).style("opacity", 0.0);
            var enteringPolylines = enteringSubG.append("svg:polyline").attr("class", function (d) {
                return GraphView.BaseGraphView.linkSvgClassSansDot + " " + GraphView.BaseGraphView.linkClassSelectorPrefix + d.relationType + " " + outerThis.getLinkCssClass(d.relationType, d.relationSpecificToOntologyAcronym);
            }).attr("id", function (d) {
                return "link_line_" + d.id;
            }).on("mouseover", this.highlightHoveredLinkLambda(this)).on("mouseout", this.unhighlightHoveredLinkLambda(this)).attr("data-thickness_basis", function (d) {
                return d.value;
            });
            var enteringArcMarkers = enteringSubG.append("svg:polyline").attr("class", function (d) {
                return GraphView.BaseGraphView.linkMarkerSvgClassSansDot + " " + GraphView.BaseGraphView.linkClassSelectorPrefix + d.relationType + " " + outerThis.getLinkCssClass(d.relationType, d.relationSpecificToOntologyAcronym);
            }).attr("id", function (d) {
                return "link_marker_" + d.id;
            }).on("mouseover", this.highlightHoveredLinkLambda(this)).on("mouseout", this.unhighlightHoveredLinkLambda(this));
            enteringLinks.append("title").text(this.conceptLinkSimplePopupFunction).attr("id", function (d) {
                return "link_title_" + d.id;
            });
            enteringSubG.transition().duration(this.enteringElementTransitionDuration).style("opacity", "1.0");
            if (!enteringLinks.empty()) {
                if (!temporaryEdges) {
                    this.runCurrentLayout(true);
                }
                else {
                    enteringPolylines.classed(GraphView.BaseGraphView.temporaryEdgeClass, true);
                }
                this.updateStartWithoutResume();
                enteringPolylines.attr("points", this.updateArcLineFunc);
                enteringArcMarkers.attr("points", this.updateArcMarkerFunc);
                this.edgeTypeFilter.updateFilterUI();
            }
        };
        ConceptPathsToRoot.prototype.markerAdderLambda = function () {
            var outerThis = this;
            return function (e) {
                if (outerThis.isIE()) {
                    if (outerThis.giveIEMarkerWarning) {
                        console.log("WARNING: Line markers not used for IE due to lack of support for valid SVG marker defs. Known IE SVG bug, they won't fix it.");
                        outerThis.giveIEMarkerWarning = false;
                    }
                    return "";
                }
                else {
                    return "url(#" + "LinkHeadMarker_" + outerThis.getLinkCssClass(e.relationType, e.relationSpecificToOntologyAcronym) + ")";
                }
            };
        };
        ConceptPathsToRoot.prototype.getLinkCssClass = function (relationType, relationSpecificToOntologyAcronym) {
            if (undefined !== relationSpecificToOntologyAcronym) {
                var cssClassName = relationSpecificToOntologyAcronym + "__" + relationType + "LinkStyle";
                if ($.stylesheet("." + cssClassName).rules().length === 0) {
                    var ontColor = this.conceptGraph.nextNodeColor(relationSpecificToOntologyAcronym);
                    var sheet = $.stylesheet("." + cssClassName);
                    sheet.css("stroke", ontColor);
                    sheet.css("fill", ontColor);
                    this.propertyRelationClassNames.push(cssClassName);
                }
                return cssClassName;
            }
            else if (-1 !== relationType.indexOf("is_a")) {
                return this.conceptGraph.relationTypeCssClasses["is_a"];
            }
            else if (-1 !== relationType.indexOf("part_of") || -1 !== relationType.indexOf("has_part")) {
                return this.conceptGraph.relationTypeCssClasses["part_of"];
            }
            else if (-1 !== $.inArray(relationType, ["ncbo-mapping", "maps_to"])) {
                return this.conceptGraph.relationTypeCssClasses["maps_to"];
            }
            else {
                console.log("Generated invalid link CSS class for type and acronym: " + relationType + " and " + relationSpecificToOntologyAcronym);
                return "undefined_link_css_class";
            }
        };
        ConceptPathsToRoot.prototype.isIE = function () {
            var ua = navigator.userAgent, tem, M = ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || [];
            if (/trident/i.test(M[1])) {
                return true;
            }
            else if (/msie/i.test(M[1])) {
                return true;
            }
            else {
                return false;
            }
        };
        ConceptPathsToRoot.prototype.defineCustomSVG = function () {
            var svgNode = $("#graphSvg");
            var defs = document.createElementNS("http://www.w3.org/2000/svg", "defs");
            var arcCssClassArray = ["inheritanceStyleLink", "compositionStyleLink", "mappingStyleLink"];
            var arcCssLabelArray = ["is a", "has a", "maps to"];
            for (var i = 0; i < arcCssClassArray.length; i++) {
                {
                    var cssClass = arcCssClassArray[i];
                    var marker = document.createElementNS("http://www.w3.org/2000/svg", "marker");
                    marker.setAttribute("id", "LinkHeadMarker_" + cssClass);
                    marker.setAttribute("class", cssClass + " linkMarker");
                    marker.setAttribute("viewBox", "0 0 10 10");
                    marker.setAttribute("refX", "0");
                    marker.setAttribute("refY", "5");
                    marker.setAttribute("markerUnits", "userSpaceOnUse");
                    marker.setAttribute("markerWidth", "10");
                    marker.setAttribute("markerHeight", "8");
                    marker.setAttribute("orient", "auto");
                    marker.setAttribute("overflow", "visible");
                    var path = document.createElementNS("http://www.w3.org/2000/svg", "path");
                    path.setAttribute("d", "M 0 0 L 10 5 L 0 10 z");
                    path.setAttribute("class", "linkMarker " + cssClass);
                    marker.appendChild(path);
                    defs.appendChild(marker);
                }
            }
            svgNode.append(defs);
        };
        ConceptPathsToRoot.prototype.populateNewGraphNodes = function (nodesData) {
            if (nodesData.length == 0) {
                return [];
            }
            var outerThis = this;
            var nodes = this.vis.select("#node_container").selectAll("g.node_g").data(nodesData, ConceptGraph.Node.d3IdentityFunc);
            var enteringNodes = nodes.enter().append("svg:g").attr("class", GraphView.BaseGraphView.nodeGSvgClassSansDot).attr("id", function (d) {
                return "node_g_" + d.conceptUriForIds;
            }).call(this.nodeDragBehavior).on("mouseover", this.highlightHoveredNodeLambda(this, true)).on("mouseout", this.unhighlightHoveredNodeLambda(this, true));
            var enteringSubG = enteringNodes.append("svg:g").attr("id", function (d) {
                return "node_sub_g_" + d.conceptUriForIds;
            }).attr("class", GraphView.BaseGraphView.nodeSubGSvgClassSansDot).style("opacity", 0.0);
            enteringSubG.append("svg:rect").attr("id", function (d) {
                return "node_rect_" + d.conceptUriForIds;
            }).attr("class", function (d) {
                var classes = GraphView.BaseGraphView.nodeSvgClassSansDot + " " + GraphView.BaseGraphView.conceptNodeSvgClassSansDot;
                if (d.simpleConceptUri === outerThis.centralConceptSimpleUri) {
                    classes += " centralNode";
                }
                return classes;
            }).style("fill", function (d) {
                return d.nodeColor;
            }).attr("height", this.nodeHeight).attr("width", this.nodeHeight);
            enteringSubG.each(TipsyToolTipsOnClick.nodeTooltipOnClickLambda(this));
            enteringSubG.append("svg:text").attr("id", function (d) {
                return "node_text_" + d.conceptUriForIds;
            }).attr("class", GraphView.BaseGraphView.nodeLabelSvgClassSansDot + " unselectable").text(function (d) {
                return d.name;
            }).style("pointer-events", "none").attr("unselectable", "on").attr("onmousedown", "noselect").attr("onselectstart", "function(){ return false;}");
            $(GraphView.BaseGraphView.nodeLabelSvgClass).each(function (i, d) {
                var textSize = this.getBBox();
                var rect = $(d).siblings().filter(GraphView.BaseGraphView.nodeSvgClass);
                rect.attr("width", textSize.width + outerThis.nodeLabelPaddingWidth);
                rect.attr("height", textSize.height + outerThis.nodeLabelPaddingHeight);
                rect.attr("x", -textSize.width / 2 - outerThis.nodeLabelPaddingWidth / 2);
                rect.attr("y", -textSize.height / 2 - outerThis.nodeLabelPaddingHeight / 2);
                $(d).attr("dx", -textSize.width / 2).attr("dy", outerThis.nodeLabelPaddingHeight / 2);
            });
            this.attachNodeMenu(enteringSubG);
            enteringSubG.transition().duration(this.enteringElementTransitionDuration).style("opacity", 1.0);
            if (!enteringNodes.empty()) {
                this.runCurrentLayout(true);
                this.updateStartWithoutResume();
                enteringNodes.attr("transform", function (d) {
                    return "translate(" + d.x + "," + d.y + ")";
                });
                this.edgeTypeFilter.updateFilterUI();
                this.nestedOntologyConceptFilter.updateFilterUI();
                this.nestedExpansionConceptFilter.updateFilterUI();
            }
        };
        ConceptPathsToRoot.prototype.removeMissingGraphElements = function () {
            this.forceLayout.nodes(this.conceptGraph.graphD3Format.nodes);
            this.forceLayout.links(this.conceptGraph.graphD3Format.links);
            var nodes = this.vis.selectAll("g.node_g").data(this.conceptGraph.graphD3Format.nodes, ConceptGraph.Node.d3IdentityFunc);
            var links = this.vis.selectAll("g." + GraphView.BaseGraphView.linkSvgClassSansDot).data(this.conceptGraph.graphD3Format.links, ConceptGraph.Link.d3IdentityFunc);
            var exitingNodes = nodes.exit();
            var exitingLinks = links.exit();
            var linksRemoved = exitingLinks.transition().duration(this.exitingElementTransitionDuration).style("opacity", 0.0);
            var nodesRemoved = exitingNodes.transition().duration(this.exitingElementTransitionDuration).style("opacity", 0.0).call(function () {
                exitingLinks.remove();
                exitingNodes.remove();
            });
            this.filterSliders.updateTopMappingsSliderRange();
            this.filterSliders.rangeSliderSlideEvent(null, null);
            if (!exitingNodes.empty() || !exitingLinks.empty()) {
                this.updateStartWithoutResume();
                this.nestedOntologyConceptFilter.updateFilterUI();
                this.edgeTypeFilter.updateFilterUI();
                this.nestedExpansionConceptFilter.updateFilterUI();
            }
        };
        ConceptPathsToRoot.prototype.attachNodeMenu = function (enteringNodes) {
            var expanderSvgs = enteringNodes.append("svg:svg").attr("overflow", "visible").attr("x", function (d) {
                return -1 * parseInt($("#node_rect_" + d.conceptUriForIds)[0].getAttribute("height"), 0) / 2;
            }).attr("y", function (d) {
                return parseInt($("#node_rect_" + d.conceptUriForIds)[0].getAttribute("height"), 0) / 2;
            }).on("click", this.showNodeExpanderPopupMenuLambda(this));
            expanderSvgs.append("svg:rect").attr("id", function (d) {
                return ConceptPathsToRoot.NODE_EXPANDER_INDICATOR_ID_PREFIX + d.conceptUriForIds;
            }).style("fill", "#c5effd").style("stroke", "#afc6e5").attr("height", this.expansionBoxHeight).attr("width", this.expansionBoxWidth).attr("overflow", "visible");
            expanderSvgs.append("svg:polygon").attr("points", "11.25,2 18.75,2 15,6 ").style("fill", "#000000").attr("x", function (d) {
                return -1 * (this.getAttribute("width") / 2);
            }).attr("y", function (d) {
                return parseInt($("#node_rect_" + d.conceptUriForIds)[0].getAttribute("height"), 0) / 2;
            }).attr("overflow", "visible");
        };
        ConceptPathsToRoot.prototype.showNodeExpanderPopupMenuLambda = function (outerThis) {
            return function (nodeData) {
                var rectWidth = 110;
                var rectHeight = 35;
                var fontXSvgPadding = 7;
                var fontYSvgPadding = 23;
                var innerSvg = d3.select(this).append("svg:svg").attr("id", "expanderMenu").attr("overflow", "visible").attr("y", 0).attr("x", -1 * (rectWidth / 2 + parseInt(d3.select(this).attr("x"), 0))).attr("width", rectWidth).attr("height", rectHeight * 2).style("z-index", 100).on("mouseleave", function () {
                    outerThis.unhighlightHoveredNodeLambda(outerThis, false)(nodeData, 0);
                    $("#expanderMenu").first().remove();
                }).on("mouseup", function () {
                    $("#expanderMenu").first().remove();
                });
                {
                    var conceptExpandSvg = innerSvg.append("svg:svg").attr("overflow", "visible").attr("y", 0).classed("expanderMenuItem", true);
                    var conceptExpandTextValue;
                    var conceptExpandFontFillColor;
                    var conceptExpandMouseUpFunc;
                    var hardTermExpansionCount = outerThis.conceptGraph.getNumberOfPotentialNodesToExpand(nodeData, ConceptGraph.PathOptionConstants.termNeighborhoodConstant);
                    if (hardTermExpansionCount != 0) {
                        conceptExpandTextValue = "Expand Concepts";
                        conceptExpandTextValue += " (" + hardTermExpansionCount + ")";
                        conceptExpandFontFillColor = "";
                        conceptExpandMouseUpFunc = function () {
                            $("#expanderMenu").first().remove();
                            var expId = new ExpansionSets.ExpansionSetIdentifer("concept_expand_" + nodeData.conceptUriForIds, "Concepts: " + nodeData.name + " (" + nodeData.ontologyAcronym + ")");
                            var expansionSet = new ExpansionSets.ExpansionSet(expId, nodeData, outerThis.conceptGraph, outerThis.conceptGraph.expMan.getActiveExpansionSets(), outerThis.undoRedoBoss, ConceptGraph.PathOptionConstants.termNeighborhoodConstant);
                            outerThis.conceptGraph.expandConceptNeighbourhood(nodeData, expansionSet);
                        };
                    }
                    else {
                        conceptExpandTextValue = "Concepts Already Expanded";
                        conceptExpandFontFillColor = "#AAAAAA";
                        conceptExpandMouseUpFunc = function () {
                            return false;
                        };
                    }
                    conceptExpandSvg.append("svg:rect").style("fill", "#FFFFFF").style("stroke", "#000000").attr("x", 0).attr("y", 0).attr("width", rectWidth).attr("height", rectHeight).on("mouseup", conceptExpandMouseUpFunc);
                    conceptExpandSvg.append("svg:text").text(conceptExpandTextValue).style("font-family", "Arial, sans-serif").style("font-size", "12px").style("fill", conceptExpandFontFillColor).attr("dx", fontXSvgPadding).attr("dy", fontYSvgPadding).style("font-weight", "inherit").attr("class", GraphView.BaseGraphView.nodeLabelSvgClassSansDot + " unselectable " + " expanderMenuText").style("pointer-events", "none").attr("unselectable", "on").attr("onmousedown", "noselect").attr("onselectstart", "function(){ return false;}");
                }
                {
                    var mappingExpandSvg = innerSvg.append("svg:svg").attr("overflow", "visible").attr("y", rectHeight).classed("expanderMenuItem", true);
                    var mappingExpandTextValue;
                    var mappingExpandFontFillColor;
                    var mappingExpandMouseUpFunc;
                    var hardMappingExpansionCount = outerThis.conceptGraph.getNumberOfPotentialNodesToExpand(nodeData, ConceptGraph.PathOptionConstants.mappingsNeighborhoodConstant);
                    if (hardMappingExpansionCount !== 0) {
                        mappingExpandTextValue = "Expand Mappings";
                        mappingExpandTextValue += " (" + hardMappingExpansionCount + ")";
                        mappingExpandFontFillColor = "";
                        mappingExpandMouseUpFunc = function () {
                            $("#expanderMenu").first().remove();
                            var expId = new ExpansionSets.ExpansionSetIdentifer("mapping_expand_" + nodeData.conceptUriForIds, "Mappings: " + nodeData.name + " (" + nodeData.ontologyAcronym + ")");
                            var expansionSet = new ExpansionSets.ExpansionSet(expId, nodeData, outerThis.conceptGraph, outerThis.conceptGraph.expMan.getActiveExpansionSets(), outerThis.undoRedoBoss, ConceptGraph.PathOptionConstants.mappingsNeighborhoodConstant);
                            outerThis.conceptGraph.expandMappingNeighbourhood(nodeData, expansionSet);
                        };
                    }
                    else {
                        mappingExpandTextValue = "Mappings Already Expanded";
                        mappingExpandFontFillColor = "#AAAAAA";
                        mappingExpandMouseUpFunc = function () {
                            return false;
                        };
                    }
                    mappingExpandSvg.append("svg:rect").style("fill", "#FFFFFF").style("stroke", "#000000").attr("x", 0).attr("y", 0).attr("width", rectWidth).attr("height", rectHeight).on("mouseup", mappingExpandMouseUpFunc);
                    mappingExpandSvg.append("svg:text").text(mappingExpandTextValue).style("font-family", "Arial, sans-serif").style("font-size", "12px").style("fill", mappingExpandFontFillColor).attr("x", fontXSvgPadding).attr("y", fontYSvgPadding).style("font-weight", "inherit").attr("class", GraphView.BaseGraphView.nodeLabelSvgClassSansDot + " unselectable " + " expanderMenuText").style("pointer-events", "none").attr("unselectable", "on").attr("onmousedown", "noselect").attr("onselectstart", "function(){ return false;}");
                }
                {
                    var centralizeNodeSvg = innerSvg.append("svg:svg").attr("overflow", "visible").attr("y", 2 * rectHeight).classed("expanderMenuItem", true);
                    centralizeNodeSvg.append("svg:rect").style("fill", "#FFFFFF").style("stroke", "#000000").attr("x", 0).attr("y", 0).attr("width", rectWidth).attr("height", rectHeight).on("mouseup", function () {
                        $("#expanderMenu").first().remove();
                        outerThis.recomputeVisualizationOntoNode(nodeData);
                    });
                    centralizeNodeSvg.append("svg:text").text(ConceptPathsToRoot.REFOCUS_NODE_TEXT).style("font-family", "Arial, sans-serif").style("font-size", "12px").attr("x", fontXSvgPadding).attr("y", fontYSvgPadding).style("font-weight", "inherit").attr("class", GraphView.BaseGraphView.nodeLabelSvgClassSansDot + " unselectable " + " expanderMenuText").style("pointer-events", "none").attr("unselectable", "on").attr("onmousedown", "noselect").attr("onselectstart", "function(){ return false;}");
                }
                {
                    var hideNodeSvg = innerSvg.append("svg:svg").attr("overflow", "visible").attr("y", 3 * rectHeight).classed("expanderMenuItem", true);
                    hideNodeSvg.append("svg:rect").style("fill", "#FFFFFF").style("stroke", "#000000").attr("x", 0).attr("y", 0).attr("width", rectWidth).attr("height", rectHeight).on("mouseup", function () {
                        $("#expanderMenu").first().remove();
                        outerThis.toggleHideNodeLambda(outerThis)(nodeData, 0);
                        outerThis.refreshOtherFilterCheckboxStates([nodeData], null);
                    });
                    hideNodeSvg.append("svg:text").text(outerThis.isNodeHidden(nodeData) ? "Unhide Node" : "Hide Node").style("font-family", "Arial, sans-serif").style("font-size", "12px").attr("x", fontXSvgPadding).attr("y", fontYSvgPadding).style("font-weight", "inherit").attr("class", GraphView.BaseGraphView.nodeLabelSvgClassSansDot + " unselectable " + " expanderMenuText").style("pointer-events", "none").attr("unselectable", "on").attr("onmousedown", "noselect").attr("onselectstart", "function(){ return false;}");
                }
                {
                    var maxWidth = 0;
                    $("#" + innerSvg.attr("id")).find("text").each(function (index, element) {
                        var box = element.getBoundingClientRect();
                        var elemWidth = box.right - box.left;
                        maxWidth = Math.max(maxWidth, elemWidth);
                    });
                    maxWidth += 2 * fontXSvgPadding + 4;
                    $("#" + innerSvg.attr("id")).attr("width", maxWidth);
                    $("#" + innerSvg.attr("id")).find("rect").attr("width", maxWidth);
                }
                d3.selectAll(".expanderMenuItem").on("mouseover", function (node) {
                    d3.select(this).classed("boldText", true);
                }).on("mouseout", function (node) {
                    d3.select(this).classed("boldText", false);
                });
            };
        };
        ConceptPathsToRoot.prototype.beforeNodeHighlight = function (targetNodeData) {
            this.conceptGraph.manifestTemporaryHoverEdges(targetNodeData);
        };
        ConceptPathsToRoot.prototype.afterNodeUnhighlight = function (targetNodeData) {
            this.conceptGraph.removeTemporaryHoverEdges(targetNodeData);
        };
        ConceptPathsToRoot.prototype.prepGraphMenu = function () {
            this.menu.initializeMenu("Layout & Filter Menu");
            this.tour.initializeMenu();
            this.layouts.addMenuComponents(this.menu.getMenuSelector());
            this.nodeFinder.addMenuComponents(this.menu.getMenuSelector(), true);
            this.importerExporterWidget.addMenuComponents(this.menu.getMenuSelector());
            this.edgeTypeFilter.addMenuComponents(this.menu.getMenuSelector(), true);
            this.nestedOntologyConceptFilter.addMenuComponents(this.menu.getMenuSelector(), false);
            this.nestedExpansionConceptFilter.addMenuComponents(this.menu.getMenuSelector(), true);
        };
        ConceptPathsToRoot.prototype.refreshOtherFilterCheckboxStates = function (affectedNodes, triggeringFilter) {
            if (triggeringFilter !== this.nestedExpansionConceptFilter) {
                this.nestedExpansionConceptFilter.updateCheckboxStateFromView(affectedNodes);
            }
            if (triggeringFilter !== this.nestedOntologyConceptFilter) {
                this.nestedOntologyConceptFilter.updateCheckboxStateFromView(affectedNodes);
            }
        };
        ConceptPathsToRoot.prototype.revealAllNodesAndRefreshFilterCheckboxes = function () {
            d3.selectAll("." + GraphView.BaseGraphView.hiddenNodeClass).classed(GraphView.BaseGraphView.hiddenNodeClass, false);
            d3.selectAll("." + GraphView.BaseGraphView.hiddenNodeLabelClass).classed(GraphView.BaseGraphView.hiddenNodeLabelClass, false);
            d3.selectAll("." + GraphView.BaseGraphView.hiddenLinkBecauseOfHiddenNodeLabelClass).classed(GraphView.BaseGraphView.hiddenLinkBecauseOfHiddenNodeLabelClass, false);
            this.nestedExpansionConceptFilter.updateFilterUI();
            this.nestedOntologyConceptFilter.updateFilterUI();
            this.nestedExpansionConceptFilter.checkmarkAllCheckboxes();
            this.nestedOntologyConceptFilter.checkmarkAllCheckboxes();
            this.runCurrentLayout(true);
        };
        ConceptPathsToRoot.prototype.deleteSelectedCheckboxesLambda = function (computeNodesToDeleteFunc) {
            var _this = this;
            if (null == computeNodesToDeleteFunc) {
                computeNodesToDeleteFunc = function () {
                    var allNodeElements = $("." + GraphView.BaseGraphView.hiddenNodeClass).filter("." + GraphView.BaseGraphView.nodeGSvgClassSansDot);
                    var nodes = [];
                    for (var elem in allNodeElements) {
                        var nodeId = nodeId.replace(GraphView.BaseGraphView.nodeGSvgClassSansDot + "_", "");
                        nodes.push(_this.conceptGraph.getNodeByIdUri(nodeId));
                    }
                    return nodes;
                };
            }
            return function () {
                var deletionSet = new DeletionSet.DeletionSet(_this.conceptGraph, _this.conceptGraph.expMan.getActiveExpansionSets(), _this.undoRedoBoss);
                var hiddenNodes = computeNodesToDeleteFunc();
                deletionSet.addAll(hiddenNodes);
                deletionSet.getGraphModifier().executeRedo();
                _this.refreshOtherFilterCheckboxStates(hiddenNodes, null);
            };
        };
        ConceptPathsToRoot.prototype.sortConceptNodesCentralOntologyName = function (nodesToSort) {
            if (null == nodesToSort) {
                nodesToSort = this.conceptGraph.graphD3Format.nodes;
            }
            return nodesToSort.sort(this.sortConceptNodesCentralOntologyNameLambda());
        };
        ConceptPathsToRoot.prototype.sortConceptNodesCentralOntologyNameLambda = function () {
            var outerThis = this;
            return function (a, b) {
                if (a.nodeId === b.nodeId) {
                    return 0;
                }
                if (a.nodeId === outerThis.conceptGraph.centralConceptUri) {
                    return -1;
                }
                else if (b.nodeId === outerThis.conceptGraph.centralConceptUri) {
                    return 1;
                }
                if (a.ontologyAcronym !== b.ontologyAcronym) {
                    if (a.ontologyAcronym === outerThis.centralOntologyAcronym) {
                        return -1;
                    }
                    else if (b.ontologyAcronym === outerThis.centralOntologyAcronym) {
                        return 1;
                    }
                    else {
                        return (a.ontologyAcronym < b.ontologyAcronym) ? -1 : 1;
                    }
                }
                return (a.name < b.name) ? -1 : 1;
            };
        };
        ConceptPathsToRoot.VIZ_SELECTOR_ID = "#visualization_selector";
        ConceptPathsToRoot.REFOCUS_NODE_TEXT = "Refocus Node";
        ConceptPathsToRoot.NODE_EXPANDER_INDICATOR_ID_PREFIX = "node_expander_indicator_";
        return ConceptPathsToRoot;
    })(GraphView.BaseGraphView);
    exports.ConceptPathsToRoot = ConceptPathsToRoot;
});
