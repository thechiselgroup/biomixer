define(["require", "exports", "./UndoRedo/UndoRedoManager", "./Utils", "ExpansionSets", "UndoRedo/UndoRedoManager", "Utils", "LayoutProvider"], function (require, exports, UndoRedoManager, Utils) {
    var GraphDataForD3 = (function () {
        function GraphDataForD3() {
            this.nodes = [];
            this.links = [];
        }
        return GraphDataForD3;
    })();
    exports.GraphDataForD3 = GraphDataForD3;
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
        function BaseGraphView() {
            var _this = this;
            this.defaultNodeColor = "#000000";
            this.defaultLinkColor = "#999";
            this.nodeHighlightColor = "#FC6854";
            this.alphaCutoff = 0.01;
            this.forceLayout = undefined;
            this.dragging = false;
            this.resizedWindowLambda = function () {
                d3.select("#graphRect").attr("width", _this.visWidth()).attr("height", _this.visHeight());
                d3.select("#graphSvg").attr("width", _this.visWidth()).attr("height", _this.visHeight());
                if (_this.forceLayout) {
                    _this.forceLayout.size([_this.visWidth(), _this.visHeight()]).linkDistance(_this.linkMaxDesiredLength());
                    _this.currentLambda(true);
                }
            };
            this.lastTimeChange = new Date().getTime();
            this.lastDisplayedTipsy = null;
            this.lastDisplayedTipsyData = null;
            this.lastDisplayedTipsySvg = null;
            this.layoutTimer = null;
            this.undoRedoBoss = new UndoRedoManager.UndoRedoManager(false, true);
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
            this.lastTimeChange = new Date().getTime();
        };
        BaseGraphView.prototype.getTimeStampLastGraphModification = function () {
            return this.lastTimeChange;
        };
        BaseGraphView.prototype.updateStartWithoutResume = function () {
            var _this = this;
            var resume = this.forceLayout.resume;
            this.forceLayout.resume = function () {
                return _this.forceLayout;
            };
            this.forceLayout.start();
            this.forceLayout.resume = resume;
        };
        BaseGraphView.prototype.setCurrentLayout = function (layoutLambda) {
            var outerLayoutTimer = this.layoutTimer;
            var outerThis = this;
            var layoutLastCalled = null;
            var timerWait = 100;
            this.currentLambda = layoutLambda;
            this.runCurrentLayout = function (refreshLayoutInner) {
                if (outerLayoutTimer == null && (layoutLastCalled == null || outerThis.getTimeStampLastGraphModification() > layoutLastCalled)) {
                    outerLayoutTimer = setTimeout(function () {
                        clearTimeout(outerLayoutTimer);
                        outerLayoutTimer = null;
                        layoutLastCalled = new Date().getTime();
                        outerThis.currentLambda(refreshLayoutInner);
                    }, timerWait);
                }
            };
        };
        BaseGraphView.prototype.getAdjacentLinks = function (node) {
            return d3.selectAll(BaseGraphView.linkSvgClass).filter(function (d, i) {
                return d.source === node || d.target === node;
            });
        };
        BaseGraphView.prototype.getNodeElement = function (node) {
            return $("#node_g_" + Utils.escapeIdentifierForId(node.getEntityId()));
        };
        BaseGraphView.prototype.isNodeHidden = function (node) {
            var element = d3.select("#node_g_" + Utils.escapeIdentifierForId(node.getEntityId()));
            if (null == element[0][0]) {
                return true;
            }
            else if (element.classed(BaseGraphView.hiddenNodeClass)) {
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
                    return aText.getEntityId() === linkLine.source.getEntityId() || aText.getEntityId() === linkLine.target.getEntityId();
                }).classed("dimmedNodeLabel", false).classed("highlightedNodeLabel", true);
                d3.selectAll(BaseGraphView.nodeSvgClass + ", " + BaseGraphView.nodeInnerSvgClass).classed("highlightedNode", true).filter(function (aNode, i) {
                    return aNode.getEntityId() === linkLine.source.getEntityId() || aNode.getEntityId() === linkLine.target.getEntityId();
                }).classed("dimmedNode", false).classed("highlightedNode", true);
                d3.selectAll(BaseGraphView.linkSvgClass).classed("dimmedLink", true);
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
                var sourceNode = d3.selectAll(BaseGraphView.nodeSvgClass + ", " + BaseGraphView.nodeInnerSvgClass).filter(function (d, i) {
                    return d === nodeData;
                });
                sourceNode.classed("highlightedNode", true).classed("dimmedNode", false);
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
                adjacentLinks.classed("dimmedLink", false).classed("highlightedLink", function (aLink, i) {
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
        BaseGraphView.prototype.findSubNode = function (nodeData) {
            var subnode = d3.selectAll(BaseGraphView.nodeGSvgClass).filter(function (d, i) {
                return d === nodeData;
            }).node();
            if (subnode === null) {
                return null;
            }
            return subnode;
        };
        BaseGraphView.prototype.nodeHider = function (nodeData, hiding) {
            var sourceGNode = this.findSubNode(nodeData);
            if (null == sourceGNode) {
                return;
            }
            if (hiding == null) {
                hiding = !(d3.select(sourceGNode).classed(BaseGraphView.hiddenNodeClass));
            }
            d3.select(sourceGNode).classed(BaseGraphView.hiddenNodeClass, hiding);
            d3.selectAll(BaseGraphView.nodeLabelSvgClass).filter(function (d, i) {
                return d === nodeData;
            }).classed(BaseGraphView.hiddenNodeLabelClass, hiding);
            var adjacentLinks = this.getAdjacentLinks(nodeData);
            adjacentLinks.classed(BaseGraphView.hiddenLinkBecauseOfHiddenNodeLabelClass, function (linkData, i) {
                var source = d3.selectAll(BaseGraphView.nodeGSvgClass).filter(function (d, i) {
                    return d === linkData.source;
                });
                var target = d3.selectAll(BaseGraphView.nodeGSvgClass).filter(function (d, i) {
                    return d === linkData.target;
                });
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
            links.classed("hiddenLink", hiding);
        };
        BaseGraphView.prototype.beforeNodeHighlight = function (targetNodeData) {
        };
        BaseGraphView.prototype.afterNodeUnhighlight = function (targetNodeData) {
        };
        BaseGraphView.prototype.animateHighlightNodesDeactivate = function () {
        };
        BaseGraphView.prototype.animateHighlightNodesActivate = function (matchingNodes) {
            var particle = function (d) {
                d3.select("#graphSvg").append("circle").attr("cx", d.x).attr("cy", d.y).attr("r", 1e-6).style("stroke", d.nodeColor).style("stroke-width", 3).style("stroke-opacity", 1).style("fill-opacity", 0).transition().duration(2000).ease(Math.sqrt).attr("r", 100).style("stroke-opacity", 1e-6).remove();
            };
            d3.selectAll(".node_g").filter(function (d) {
                return matchingNodes.indexOf(d) !== -1;
            }).each(particle);
        };
        BaseGraphView.nodeSvgClassSansDot = "node";
        BaseGraphView.nodeInnerSvgClassSansDot = "inner_node";
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
