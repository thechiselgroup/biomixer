///<reference path="headers/require.d.ts" />
define(["require", "exports", "./UndoRedo/UndoRedoManager", "./Utils", "./Menu", "./ExportSvgToImage", "ExpansionSets", "UndoRedo/UndoRedoManager", "Utils", "LayoutProvider"], function (require, exports, UndoRedoManager, Utils, Menu, PrintSvg) {
    var GraphDataForD3 = (function () {
        function GraphDataForD3() {
            this.nodes = [];
            this.links = [];
        }
        return GraphDataForD3;
    })();
    exports.GraphDataForD3 = GraphDataForD3;
    // The silly extends are to facilitate specialized typing to the inheriting classes.
    // In Java, it'd be more like ? extends in the things that need.
    // If we don't like it, we need to change the expansionSet references in nodes
    // to something like strings, which can be used in a registry. This was what I changed
    // from because it was an annoying pattern; annoying generics in class defs are better.
    //export class BaseNode<SubN extends BaseNode<any>> implements D3.Layout.GraphNode {
    var BaseNode = (function () {
        function BaseNode() {
        }
        BaseNode.prototype.getEntityId = function () {
            return "Error, must override this method.";
        };
        BaseNode.prototype.getExpansionSet = function () {
            return this.expansionSetAsMember;
        };
        return BaseNode;
    })();
    exports.BaseNode = BaseNode;
    var BaseLink = (function () {
        function BaseLink() {
        }
        return BaseLink;
    })();
    exports.BaseLink = BaseLink;
    var BaseGraphView = (function () {
        function BaseGraphView(attachScreenshotButton) {
            var _this = this;
            //var defaultNodeColor = "#496BB0";
            this.defaultNodeColor = "#000000";
            this.defaultLinkColor = "#999";
            this.nodeHighlightColor = "#FC6854";
            this.alphaCutoff = 0.01; // used to stop the layout early in the tick() callback
            this.forceLayout = undefined;
            this.dragging = false;
            this.resizedWindowLambda = function () {
                d3.select("#graphRect").attr("width", _this.visWidth()).attr("height", _this.visHeight());
                d3.select("#graphSvg").attr("width", _this.visWidth()).attr("height", _this.visHeight());
                // TODO Layouts not relying on force need additional support here.
                // This might need to call back into an instance method named something like "layoutResized"
                if (_this.forceLayout) {
                    _this.forceLayout.size([_this.visWidth(), _this.visHeight()]).linkDistance(_this.linkMaxDesiredLength());
                    // If needed, move all the nodes towards the new middle here.
                    // this.forceLayout.resume(); // wasn't doing the trick
                    _this.currentLambda(true); // direct retrigger the current layout.
                }
            };
            this.lastTimeChange = new Date().getTime();
            // These are needed to do a refresh of popups when new data arrives and the user has the popup open
            this.lastDisplayedTipsy = null;
            this.lastDisplayedTipsyData = null;
            this.lastDisplayedTipsySvg = null;
            this.layoutTimer = null;
            this.undoRedoBoss = new UndoRedoManager.UndoRedoManager(false, true);
            if (attachScreenshotButton) {
                this.attachScreenshotButton();
            }
        }
        BaseGraphView.prototype.visWidth = function () {
            return $("#chart").width();
        };
        BaseGraphView.prototype.visHeight = function () {
            return $("#chart").height();
        };
        BaseGraphView.prototype.linkMaxDesiredLength = function () {
            return Math.min(this.visWidth(), this.visHeight()) / 2 - 50;
        };
        BaseGraphView.prototype.stampTimeGraphModified = function () {
            // Things like temporary edges, etc, indicate that the caller must control this.
            this.lastTimeChange = new Date().getTime();
        };
        BaseGraphView.prototype.getTimeStampLastGraphModification = function () {
            return this.lastTimeChange;
        };
        BaseGraphView.prototype.updateStartWithoutResume = function () {
            var _this = this;
            // When start(0 is called, the last thing it does is to call resume(),
            // which calls alpha(.1). I need this to not occur...
            var resume = this.forceLayout.resume;
            this.forceLayout.resume = function () {
                return _this.forceLayout;
            };
            this.forceLayout.start();
            this.forceLayout.resume = resume;
        };
        BaseGraphView.prototype.setCurrentLayout = function (layoutLambda) {
            // This timer delay plus time stamp system cut from 56 calls down to 6 calls in a 5 node 6 arc graph load.
            var outerLayoutTimer = this.layoutTimer;
            var outerThis = this;
            var layoutLastCalled = null;
            var timerWait = 100;
            this.currentLambda = layoutLambda;
            this.runCurrentLayout = function (refreshLayoutInner) {
                // We only allow one layout request to run at a time, and with
                // a short delay between requests. Ok, it's always single threaded,
                // but the point is to avoid hitting a layout because we added one
                // node or edge, only to hit it again milliseconds later. Using the
                // timer lets the next few edges or nodes to come in before making
                // the call, thus thinning out layour refreshes.
                if (outerLayoutTimer == null && (layoutLastCalled == null || outerThis.getTimeStampLastGraphModification() > layoutLastCalled)) {
                    outerLayoutTimer = setTimeout(function () {
                        // console.log("calling");
                        clearTimeout(outerLayoutTimer);
                        outerLayoutTimer = null;
                        layoutLastCalled = new Date().getTime();
                        outerThis.currentLambda(refreshLayoutInner);
                    }, timerWait);
                }
            };
        };
        //    
        //    immediateLayoutRun(layoutLambda: {(refreshLayout?: boolean):void}){
        //        layoutLambda();
        //    }
        BaseGraphView.prototype.getAdjacentLinks = function (node) {
            return d3.selectAll(BaseGraphView.linkSvgClass).filter(function (d, i) {
                return d.source === node || d.target === node;
            });
        };
        BaseGraphView.prototype.getNodeElement = function (node) {
            // TODO Refactor these #node_g_ constants! There's an issue for this.
            return $("#node_g_" + Utils.escapeIdentifierForId(node.getEntityId()));
        };
        BaseGraphView.prototype.isNodeHidden = function (node) {
            // TODO Refactor these #node_g_ constants! There's an issue for this.
            var element = d3.select("#node_g_" + Utils.escapeIdentifierForId(node.getEntityId()));
            if (null == element[0][0]) {
                // Already deleted
                return true;
            }
            else if (element.classed(BaseGraphView.hiddenNodeClass)) {
                // hidden...but...is this the old way of deleting, before I changed it to remove
                // nodes entirely from the graph?
                return true;
            }
            else {
                return false;
            }
        };
        BaseGraphView.prototype.getUnhiddenNodes = function () {
            return $(".node_g:not(.hiddenNode)");
        };
        BaseGraphView.prototype.highlightHoveredLinkLambda = function (outerThis) {
            return function (linkLine, i) {
                if (outerThis.dragging) {
                    return;
                }
                d3.selectAll(BaseGraphView.nodeLabelSvgClass).classed("highlightedNodeLabel", true).filter(function (aText, i) {
                    return (null !== linkLine.source && null !== linkLine.target) && (aText.getEntityId() === linkLine.source.getEntityId() || aText.getEntityId() === linkLine.target.getEntityId());
                }).classed("dimmedNodeLabel", false).classed("highlightedNodeLabel", true);
                // TODO change the getEntityId to accessing the source and target uri,
                // because sometimes links have null on either point due to race conditions,
                // when the user moves off of a node onto a temporary arc, just as it is to
                // be removed.
                d3.selectAll(BaseGraphView.nodeSvgClass + ", " + BaseGraphView.nodeInnerSvgClass).classed("highlightedNode", true).filter(function (aNode, i) {
                    return (null !== linkLine.source && null !== linkLine.target) && (aNode.getEntityId() === linkLine.source.getEntityId() || aNode.getEntityId() === linkLine.target.getEntityId());
                }).classed("dimmedNode", false).classed("highlightedNode", true);
                d3.selectAll(BaseGraphView.linkSvgClass).classed("dimmedLink", true);
                // if we ever use this method attached to anything other than a link hover over, it won't
                // work, because the "this" reference below won't be a line rendered, but whatever we
                // attached the method to.
                // d3.select(this)
                // Defensively, I changed it to grab the correct link via d3.select().filter() instead.
                d3.selectAll(BaseGraphView.linkSvgClass).filter(function (d, i) {
                    return d === linkLine;
                }).classed("dimmedLink", false).classed("highlightedLink", true);
            };
        };
        BaseGraphView.prototype.highlightHoveredNodeLambda = function (outerThis, highlightAdjacentNodes) {
            return function (nodeData, i) {
                if (outerThis.dragging) {
                    return;
                }
                outerThis.beforeNodeHighlight(nodeData);
                // In a previous pass, we may have highlighted a link. Don't clobber it!
                d3.selectAll(BaseGraphView.linkSvgClass).classed("dimmedLink", function (aLink, i) {
                    return !d3.select(this).classed("highlightedLink");
                });
                d3.selectAll(BaseGraphView.nodeSvgClass + ", " + BaseGraphView.nodeInnerSvgClass).classed("dimmedNode", function (aNode, i) {
                    return !d3.select(this).classed("highlightedNode");
                });
                d3.selectAll(BaseGraphView.nodeLabelSvgClass).classed("dimmedNodeLabel", function (aNode, i) {
                    return !d3.select(this).classed("highlightedNodeLabel");
                }).filter(function (aText, i) {
                    return aText.getEntityId() === nodeData.getEntityId();
                }).classed("dimmedNodeLabel", false).classed("highlightedNodeLabel", true);
                // D3 doesn't have a way to get from bound data to what it is bound to?
                // Doing it thsi way isntead of d3.select(this) so I can re-use this method with things like
                // checkboxes outside the graph, which will trigger graph behaviors.
                var sourceNode = d3.selectAll(BaseGraphView.nodeSvgClass + ", " + BaseGraphView.nodeInnerSvgClass).filter(function (d, i) {
                    return d === nodeData;
                });
                sourceNode.classed("highlightedNode", true).classed("dimmedNode", false);
                // Get the hovered node to the top of the SVG render stack.
                // This is important for node menu rendering, so that it will inherit
                // the z-order of the node, and be above other nodes rather than beneath.
                d3.select("#node_container").selectAll('.node_g').sort(function (a, b) {
                    if (a.getEntityId() === nodeData.getEntityId()) {
                        return 1;
                    }
                    else {
                        if (b.getEntityId() === nodeData.getEntityId()) {
                            return -1;
                        }
                        else {
                            return 0;
                        }
                    }
                });
                // There must be a less loopy, data oriented way to achieve this.
                // I recently modified it to *not* use x and y coordinates to identify ndoes and edges, which was heinous.
                // Looping over everything is just as ugly (but fast enough in practice).
                var adjacentLinks = outerThis.getAdjacentLinks(nodeData);
                if (highlightAdjacentNodes) {
                    adjacentLinks.each(function (aLink) {
                        d3.selectAll(BaseGraphView.nodeSvgClass + ", " + BaseGraphView.nodeInnerSvgClass).filter(function (otherNode, i) {
                            return aLink.source.getEntityId() === otherNode.getEntityId() || aLink.target.getEntityId() === otherNode.getEntityId();
                        }).classed("dimmedNode", false).classed("highlightedNode", true).each(function (aNode) {
                            d3.selectAll(BaseGraphView.nodeLabelSvgClass).filter(function (text, i) {
                                return aNode.getEntityId() === text.getEntityId();
                            }).classed("dimmedNodeLabel", false).classed("highlightedNodeLabel", true);
                        });
                    });
                }
                // Hide all edges, then show those that have both endpoints shown
                adjacentLinks.classed("dimmedLink", false).classed("highlightedLink", function (aLink, i) {
                    // Would use JQuery, but across diff node types we could have future problems with ID creation.
                    // This is more future proof.
                    var firstEndpoints = d3.selectAll(BaseGraphView.nodeSvgClass).filter(function (otherNode, i) {
                        return aLink.source.getEntityId() === otherNode.getEntityId();
                    });
                    var secondEndpoint = d3.selectAll(BaseGraphView.nodeSvgClass).filter(function (otherNode, i) {
                        return aLink.target.getEntityId() === otherNode.getEntityId();
                    });
                    return firstEndpoints.classed("highlightedNode") && secondEndpoint.classed("highlightedNode");
                });
            };
        };
        BaseGraphView.prototype.unhighlightHoveredLinkLambda = function (outerThis) {
            return function (linkData, i) {
                outerThis.removeAllNodeHighlighting();
                outerThis.removeAllLinkHighlighting();
            };
        };
        BaseGraphView.prototype.unhighlightHoveredNodeLambda = function (outerThis, hoverAdjacent) {
            return function (nodeData, i) {
                outerThis.removeAllNodeHighlighting();
                outerThis.removeAllLinkHighlighting();
                outerThis.afterNodeUnhighlight(nodeData);
            };
        };
        BaseGraphView.prototype.removeAllNodeHighlighting = function () {
            d3.selectAll(BaseGraphView.nodeSvgClass + ", " + BaseGraphView.nodeInnerSvgClass).classed("dimmedNode", false).classed("highlightedNode", false);
            d3.selectAll(BaseGraphView.nodeLabelSvgClass).classed("dimmedNodeLabel", false).classed("highlightedNodeLabel", false);
        };
        BaseGraphView.prototype.removeAllLinkHighlighting = function () {
            d3.selectAll(BaseGraphView.linkSvgClass).classed("dimmedLink", false).classed("highlightedLink", false);
        };
        BaseGraphView.prototype.hideNodeLambda = function (outerThis) {
            return function (nodeData, i) {
                outerThis.nodeHider(nodeData, true);
            };
        };
        BaseGraphView.prototype.unhideNodeLambda = function (outerThis) {
            return function (nodeData, i) {
                outerThis.nodeHider(nodeData, false);
            };
        };
        BaseGraphView.prototype.toggleHideNodeLambda = function (outerThis) {
            return function (nodeData, i) {
                outerThis.nodeHider(nodeData, undefined);
            };
        };
        // Returns "Node" from JQuery/HTML, not a graph node model object
        BaseGraphView.prototype.findSubNode = function (nodeData) {
            var subnode = d3.selectAll(BaseGraphView.nodeGSvgClass).filter(function (d, i) {
                return d === nodeData;
            }).node();
            if (subnode === null) {
                // When we have deleted nodes (which can be re-done), then hide an expansion
                // set that included some deleted nodes, we need to fall out fo this. There
                // might be other occassions where this happens, and I don't want expansion
                // sets, nor ontology filter systems, to be aware of deletion status, so
                // I will recover from the error here.
                return null;
            }
            return subnode;
        };
        BaseGraphView.prototype.nodeHider = function (nodeData, hiding) {
            // Hide the node and label away first
            var sourceGNode = this.findSubNode(nodeData);
            // When we have deleted nodes (which can be re-done), then hide an expansion
            // set that included some deleted nodes, we need to fall out fo this. There
            // might be other occassions where this happens, and I don't want expansion
            // sets, nor ontology filter systems, to be aware of deletion status, so
            // I will recover from the error here.
            if (null == sourceGNode) {
                return;
            }
            if (hiding == null) {
                hiding = !(d3.select(sourceGNode).classed(BaseGraphView.hiddenNodeClass));
            }
            // In order to hide any baggage (like expander menu indicators), we need to grab the parent
            d3.select(sourceGNode).classed(BaseGraphView.hiddenNodeClass, hiding);
            d3.selectAll(BaseGraphView.nodeLabelSvgClass).filter(function (d, i) {
                return d === nodeData;
            }).classed(BaseGraphView.hiddenNodeLabelClass, hiding);
            // Hide edges too
            var adjacentLinks = this.getAdjacentLinks(nodeData);
            adjacentLinks.classed(BaseGraphView.hiddenLinkBecauseOfHiddenNodeLabelClass, function (linkData, i) {
                // Look at both endpoints of link, see if both are hidden
                var source = d3.selectAll(BaseGraphView.nodeGSvgClass).filter(function (d, i) {
                    return d === linkData.source;
                });
                var target = d3.selectAll(BaseGraphView.nodeGSvgClass).filter(function (d, i) {
                    return d === linkData.target;
                });
                // if hiding, we hide the link no matter what
                // if not hiding, then we pass false if either node is hidden
                return hiding || source.classed(BaseGraphView.hiddenNodeClass) || target.classed(BaseGraphView.hiddenNodeClass);
            });
        };
        BaseGraphView.prototype.hideLinks = function (links) {
            this.linkHider(links, true);
        };
        BaseGraphView.prototype.unhideLinks = function (links) {
            this.linkHider(links, false);
        };
        BaseGraphView.prototype.linkHider = function (links, hiding) {
            // Different style from the node hider() above, but I don't mind much.
            links.classed("hiddenLink", hiding);
        };
        BaseGraphView.prototype.beforeNodeHighlight = function (targetNodeData) {
            // Nothing by default
        };
        BaseGraphView.prototype.afterNodeUnhighlight = function (targetNodeData) {
            // Nothing by default
        };
        BaseGraphView.prototype.animateHighlightNodesDeactivate = function () {
        };
        BaseGraphView.prototype.animateHighlightNodesActivate = function (matchingNodes) {
            var particle = function (d) {
                // console.log(d);
                d3.select("#graphSvg").append("circle").attr("cx", d.x).attr("cy", d.y).attr("r", 1e-6).style("stroke", d.nodeColor).style("stroke-width", 3).style("stroke-opacity", 1).style("fill-opacity", 0).transition().duration(2000).ease(Math.sqrt).attr("r", 100).style("stroke-opacity", 1e-6).remove();
            };
            //        console.log(matchingNodes);
            //        
            //        console.log(d3.selectAll(".node_g")
            //        .filter((d)=>{return matchingNodes.indexOf(d) !== -1; }));
            d3.selectAll(".node_g").filter(function (d) {
                return matchingNodes.indexOf(d) !== -1;
            }).each(particle);
            //        function pulse() {
            //          var rect = d3.select(this);
            //          (function loop() {
            //            rect = rect.transition()
            //                .duration(750)
            //                .style("fill", color(Math.random() * 5 | 0))
            //                .each("end", function() { if (this.__transition__.count < 2) loop(); });
            //          })();
            /////////
            //        d3.timer(function() {
            //  context.clearRect(0, 0, width, height);
            //
            //  var z = d3.hsl(++i % 360, 1, .5).rgb(),
            //      c = "rgba(" + z.r + "," + z.g + "," + z.b + ",",
            //      x = x0 += (x1 - x0) * .1,
            //      y = y0 += (y1 - y0) * .1;
            //
            //  d3.select({}).transition()
            //      .duration(2000)
            //      .ease(Math.sqrt)
            //      .tween("circle", function() {
            //        return function(t) {
            //          context.strokeStyle = c + (1 - t) + ")";
            //          context.beginPath();
            //          context.arc(x, y, r * t, 0, pithing);
            //          context.stroke();
            //        };
            //      });
            //});
        };
        BaseGraphView.prototype.attachScreenshotButton = function () {
            var screenshotButton = $("<label>").attr("id", "graphToJpegButton").attr("class", "nodeCommandButton").addClass("unselectable").addClass(Menu.Menu.topBarButtonClass).text("Screenshot");
            $(Menu.Menu.menuBarSelector).append(screenshotButton);
            screenshotButton.click(function (event) {
                event.stopPropagation();
                PrintSvg.ExportSvgToImage.exportSvgAsPng("#graphSvg");
            });
        };
        BaseGraphView.prototype.attachFullscreenButton = function () {
            var fullScreenButton = $("<label>").attr("id", "iframeToFullscreenButton").attr("class", "nodeCommandButton").addClass("unselectable").addClass(Menu.Menu.topBarButtonClass).text("Fullscreen");
            $(Menu.Menu.menuBarSelector).append(fullScreenButton);
            fullScreenButton.click(function (event) {
                event.stopPropagation();
                // The "*" means I don't care what the origin of the receiving window is. For this request,
                // no data is moving across, so anything works.
                window.top.postMessage("biomixer_full_screen_request", '*');
            });
            window.onmessage = function (e) {
                if (e.data === "biomixer_full_screen_request") {
                    console.log("Full sreen button pressed, when Biomixer loaded as main frame.");
                }
            };
        };
        BaseGraphView.prototype.computeStrokeAndFillLinkEndpoints = function (sourceX, sourceY, targetX, targetY, desiredEdgeThickness, extraOffset) {
            if (extraOffset === void 0) { extraOffset = 0; }
            var pointsObj = { sourceX: sourceX, sourceY: sourceY, targetX: targetX, targetY: targetY, sourceXb: sourceX, sourceYb: sourceY, targetXb: targetX, targetYb: targetY };
            // Get orthogonal vector, by changing x and y and flipping sign on first component (x).
            // We'll want the vector relative to source, then the same repeated for target...but since
            // we know the target orthogonal vector is parallel to the source orthogonal vector, we can
            // infer it.
            // We need it separately for the offset and line thickness, since the offset applies to both legs
            // of the polyline with the same sign, and the thickness applies to both with opposite sign.
            // Make is_a and has_a arcs move away from each other by enough that we can see them both
            // for when both relations exist ilinkData.source.xn a pair of nodes
            // Do it for the offset
            // Kick the special arcs (composition) a couple pixels away
            var xDistOffset = 0;
            var yDistOffset = 0;
            if (extraOffset !== 0) {
                // Pretty much same logic as below, but diff variable names.
                // Could route it through itself recursively, but is that easier to maintain??
                var targetVectorXOffset = pointsObj.targetX - pointsObj.sourceX;
                var targetVectorYOffset = pointsObj.targetY - pointsObj.sourceY;
                targetVectorXOffset += (targetVectorXOffset === 0) ? 1 : 0;
                targetVectorYOffset += (targetVectorYOffset === 0) ? 1 : 0;
                var normOffset = Math.sqrt(targetVectorXOffset * targetVectorXOffset + targetVectorYOffset * targetVectorYOffset);
                var targetOrthVectorXOffset = -1 * targetVectorYOffset / normOffset;
                var targetOrthVectorYOffset = targetVectorXOffset / normOffset;
                xDistOffset = extraOffset * targetOrthVectorXOffset;
                yDistOffset = extraOffset * targetOrthVectorYOffset;
            }
            // Now do it for the arc thickness
            var halfEdgeThickness = desiredEdgeThickness / 2;
            // Now, make the switchbacks, that will make the polyline into a box. This way we can
            // have transparent edges that can be moused over, and opaque centers that can be seen.
            var targetVectorX = pointsObj.targetX - pointsObj.sourceX;
            var targetVectorY = pointsObj.targetY - pointsObj.sourceY;
            targetVectorX += (targetVectorX === 0) ? 1 : 0;
            targetVectorY += (targetVectorY === 0) ? 1 : 0;
            var norm = Math.sqrt(targetVectorX * targetVectorX + targetVectorY * targetVectorY);
            var targetOrthVectorX = -1 * targetVectorY / norm;
            var targetOrthVectorY = targetVectorX / norm;
            var xDist = halfEdgeThickness * targetOrthVectorX;
            var yDist = halfEdgeThickness * targetOrthVectorY;
            // Apply to points object. Note signs of these.
            pointsObj.sourceXb += +xDist + xDistOffset;
            pointsObj.sourceYb += +yDist + yDistOffset;
            pointsObj.targetXb += +xDist + xDistOffset;
            pointsObj.targetYb += +yDist + yDistOffset;
            pointsObj.sourceX += -xDist + xDistOffset;
            pointsObj.sourceY += -yDist + yDistOffset;
            pointsObj.targetX += -xDist + xDistOffset;
            pointsObj.targetY += -yDist + yDistOffset;
            return pointsObj;
        };
        BaseGraphView.prototype.computeStrokeAndFillLinkEndpointsString = function (sourceX, sourceY, targetX, targetY, desiredEdgeThickness, extraOffset) {
            if (extraOffset === void 0) { extraOffset = 0; }
            var pointsObj = this.computeStrokeAndFillLinkEndpoints(sourceX, sourceY, targetX, targetY, desiredEdgeThickness, extraOffset);
            // Create starting point
            var points = pointsObj.sourceX + "," + pointsObj.sourceY + " " + pointsObj.targetX + "," + pointsObj.targetY + " ";
            // Add the segment for the fill thickness
            points += pointsObj.targetXb + "," + pointsObj.targetYb + " ";
            // Add back in reverse order
            points += pointsObj.targetXb + "," + pointsObj.targetYb + " " + pointsObj.sourceXb + "," + pointsObj.sourceYb + " ";
            // Add the other segment for the fill thickness
            points += pointsObj.sourceX + "," + pointsObj.sourceY + " ";
            return points;
        };
        BaseGraphView.nodeSvgClassSansDot = "node";
        BaseGraphView.nodeInnerSvgClassSansDot = "inner_node"; // Needed for ontology double-node effect
        BaseGraphView.nodeGSvgClassSansDot = "node_g";
        BaseGraphView.nodeSubGSvgClassSansDot = "node_sub_g";
        BaseGraphView.nodeLabelSvgClassSansDot = "nodetext";
        BaseGraphView.linkSvgClassSansDot = "link";
        BaseGraphView.linkSubGSvgClassSansDot = "sub_link";
        BaseGraphView.linkMarkerSvgClassSansDot = "linkmarker";
        BaseGraphView.linkLabelSvgClassSansDot = "linktext";
        BaseGraphView.linkClassSelectorPrefix = "link_";
        BaseGraphView.ontologyNodeSvgClassSansDot = "ontologyNode";
        BaseGraphView.ontologyLinkSvgClassSansDot = "ontologyMappingLink";
        BaseGraphView.conceptNodeSvgClassSansDot = "conceptNode";
        BaseGraphView.conceptLinkSvgClassSansDot = "conceptLink";
        BaseGraphView.hiddenNodeClass = "hiddenNode";
        BaseGraphView.hiddenNodeLabelClass = "hiddenNodeLabel";
        BaseGraphView.hiddenLinkBecauseOfHiddenNodeLabelClass = "hiddenBecauseOfNodeLink";
        BaseGraphView.temporaryEdgeClass = "temporaryLink";
        BaseGraphView.nodeSvgClass = "." + BaseGraphView.nodeSvgClassSansDot;
        BaseGraphView.nodeInnerSvgClass = "." + BaseGraphView.nodeInnerSvgClassSansDot;
        BaseGraphView.nodeGSvgClass = "." + BaseGraphView.nodeGSvgClassSansDot;
        BaseGraphView.nodeLabelSvgClass = "." + BaseGraphView.nodeLabelSvgClassSansDot;
        BaseGraphView.linkSvgClass = "." + BaseGraphView.linkSvgClassSansDot;
        BaseGraphView.linkMarkerSvgClass = "." + BaseGraphView.linkMarkerSvgClassSansDot;
        BaseGraphView.linkLabelSvgClass = "." + BaseGraphView.linkLabelSvgClassSansDot;
        return BaseGraphView;
    })();
    exports.BaseGraphView = BaseGraphView;
});
