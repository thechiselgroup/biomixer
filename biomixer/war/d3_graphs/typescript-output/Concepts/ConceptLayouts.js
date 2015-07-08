///<amd-dependency path="../JQueryExtension" />
define(["require", "exports", "../GraphView", "../Menu", "./ConceptGraph", "../JQueryExtension", "LayoutProvider", "GraphView", "Menu", "Concepts/ConceptPathsToRoot", "Concepts/ConceptGraph"], function (require, exports, GraphView, Menu, ConceptGraph) {
    var ConceptLayouts = (function () {
        function ConceptLayouts(forceLayout, graph, graphView, centralConceptUri, treeDepth, tempDepth) {
            if (treeDepth === void 0) { treeDepth = 0; }
            if (tempDepth === void 0) { tempDepth = 0; }
            this.forceLayout = forceLayout;
            this.graph = graph;
            this.graphView = graphView;
            this.centralConceptUri = centralConceptUri;
            /**
             * The fixed layout currently allows for storing of only a single layout, but interacts with undo/redo.
             */
            this.currentFixedLayoutData = {};
            this.lastTransition = null;
            this.staleTimerThreshold = 4000;
            this.desiredDuration = 500;
            this.lastRefreshArg = false;
        }
        ConceptLayouts.prototype.addMenuComponents = function (menuSelector) {
            // Add the butttons to the pop-out panel
            var layoutsContainer = $("<div>").attr("id", ConceptLayouts.layoutMenuContainerId);
            $(menuSelector).append(layoutsContainer);
            layoutsContainer.append($("<label>").addClass(Menu.Menu.menuLabelClass).text("Layouts"));
            layoutsContainer.append($("<br>"));
            var forceButton = $("<div>").attr("id", "forceLayoutButton").addClass("unselectable").addClass("layoutTextButton").append($("<div>").attr("id", "forceLayoutButtonIcon").css("float", "left").addClass("unselectable").addClass("iconLayoutButton").attr("title", "Force-Directed Layout")).append($("<div>").addClass("layoutText").text("Force"));
            var circleButton = $("<div>").attr("id", "circleLayoutButton").addClass("unselectable").addClass("layoutTextButton").append($("<div>").attr("id", "circleLayoutButtonIcon").css("float", "left").addClass("unselectable").addClass("iconLayoutButton").attr("title", "Circle Layout")).append($("<div>").addClass("layoutText").text("Circle"));
            var centerButton = $("<div>").attr("id", "centerLayoutButton").addClass("unselectable").addClass("layoutTextButton").append($("<div>").attr("id", "centerLayoutButtonIcon").css("float", "left").addClass("unselectable").addClass("iconLayoutButton").attr("title", "Center Layout")).append($("<div>").addClass("layoutText").text("Center"));
            var horizTreeButton = $("<div>").attr("id", "horizontalTreeLayoutButton").addClass("unselectable").addClass("layoutTextButton").append($("<div>").attr("id", "horizontalTreeLayoutButtonIcon").css("float", "left").addClass("unselectable").addClass("iconLayoutButton").attr("title", "Horizontal Tree Layout")).append($("<div>").addClass("layoutText").text("Horizontal"));
            var vertTreeButton = $("<div>").attr("id", "verticalTreeLayoutButton").addClass("unselectable").addClass("layoutTextButton").append($("<div>").attr("id", "verticalTreeLayoutButtonIcon").css("float", "left").addClass("unselectable").addClass("iconLayoutButton").attr("title", "Vertical Tree Layout")).append($("<div>").addClass("layoutText").text("Vertical"));
            var radialButton = $("<div>").attr("id", "radialLayoutButton").addClass("unselectable").addClass("layoutTextButton").append($("<div>").attr("id", "radialLayoutButtonIcon").css("float", "left").addClass("unselectable").addClass("iconLayoutButton").attr("title", "Radial Layout")).append($("<div>").addClass("layoutText").text("Radial"));
            var importButton = $("<input>").attr("id", "importedLayoutButton").addClass("nonIconLayoutButton").attr("type", "button").attr("value", "Imported Layout");
            var firstCol = $("<div>").css("float", "left");
            var secondCol = $("<div>").css("float", "left");
            var footer = $("<div>").css("clear", "both");
            layoutsContainer.append(firstCol);
            layoutsContainer.append(secondCol);
            layoutsContainer.append(footer);
            firstCol.append(centerButton);
            firstCol.append($("<br>"));
            firstCol.append(circleButton);
            firstCol.append($("<br>"));
            firstCol.append(forceButton);
            firstCol.append($("<br>"));
            firstCol.append(importButton);
            secondCol.append(vertTreeButton);
            secondCol.append($("<br>"));
            secondCol.append(horizTreeButton);
            secondCol.append($("<br>"));
            secondCol.append(radialButton);
            secondCol.append($("<br>"));
            d3.selectAll("#circleLayoutButton").on("click", this.applyNewLayoutLambda(this.runCircleLayoutLambda()));
            d3.selectAll("#forceLayoutButton").on("click", this.applyNewLayoutLambda(this.runForceLayoutLambda()));
            d3.selectAll("#centerLayoutButton").on("click", this.applyNewLayoutLambda(this.runCenterLayoutLambda()));
            d3.selectAll("#horizontalTreeLayoutButton").on("click", this.applyNewLayoutLambda(this.runHorizontalTreeLayoutLambda()));
            d3.selectAll("#verticalTreeLayoutButton").on("click", this.applyNewLayoutLambda(this.runVerticalTreeLayoutLambda()));
            d3.selectAll("#radialLayoutButton").on("click", this.applyNewLayoutLambda(this.runRadialLayoutLambda()));
            d3.selectAll("#importedLayoutButton").on("click", this.applyNewLayoutLambda(this.runFixedPositionLayoutLambda()));
            $("#importedLayoutButton").slideUp();
            var spacingContainer = $("<div>").attr("id", "spacingSliderContainer");
            $(menuSelector).append(spacingContainer);
            spacingContainer.append($("<label>").addClass(Menu.Menu.menuLabelClass).text("Spacing"));
            spacingContainer.append($("<br>"));
            //        var zoom = d3.behavior.zoom()
            //        .scaleExtent([1, 10])
            //        .on("zoom", this.zoomLambda());
            var spacingSliderDiv = $("<div>").attr("id", "spacingSliderDiv");
            spacingContainer.append(spacingSliderDiv);
            var spacingSlider = $("#spacingSliderDiv").slider({
                range: "min",
                min: 1000,
                max: 10000,
                value: 1000,
                slide: this.semanticZoom()
            });
            //        d3.selectAll("#spacingSlider").on("drag", this.zoomLambda());
            //        spacingSliderDiv.append(spacingSlider);
            //        spacingContainer.append(spacingSliderDiv);
        };
        ConceptLayouts.prototype.getLayoutPositionSnapshot = function () {
            var positions = {};
            var graphNodes = this.graph.graphD3Format.nodes;
            $.each(graphNodes, function (index, node) {
                positions[String(node.nodeId)] = { x: node.x, y: node.y };
            });
            return positions;
        };
        ConceptLayouts.prototype.setLayoutFixedCoordinates = function (layout) {
            if (undefined == layout) {
                return;
            }
            this.currentFixedLayoutData = layout;
        };
        ConceptLayouts.prototype.applyFixedLayout = function () {
            this.runFixedPositionLayoutLambda()(false);
        };
        ConceptLayouts.prototype.applyNewLayoutLambda = function (layoutLambda) {
            var outerThis = this;
            return function () {
                outerThis.graphView.setCurrentLayout(layoutLambda);
                outerThis.graphView.runCurrentLayout();
                // No, don't refresh minimap, it worsens performance
                // outerThis.graphView.renderMiniMap();
            };
        };
        ConceptLayouts.prototype.setNewLayoutWithoutRunning = function (layoutLambda) {
            this.graphView.setCurrentLayout(layoutLambda);
        };
        ConceptLayouts.prototype.getLayoutRunner = function () {
            return this.graphView.currentLambda; // Not runCurrentLayout, because that's a wrapped version
        };
        /**
         * If refresh, we use a timer to prevent stuttering.
         * If translateOnlyFixedNodes is true, then we have other nodes that will be unaffected
         * during the transition, probably because they are being animated in another way (such
         * as running a force layout concurrently that is meant to only apply to non-fixed nodes,
         * while we animate all of the fixed position nodes into their fixed positions).
         */
        ConceptLayouts.prototype.transitionNodes = function (refresh, transitionOnlyFixedNodes) {
            var _this = this;
            if (transitionOnlyFixedNodes === void 0) { transitionOnlyFixedNodes = false; }
            var outerThis = this;
            var graphNodes = outerThis.graph.graphD3Format.nodes;
            var graphLinks = outerThis.graph.graphD3Format.links;
            // This part is involved. To know if we have to smooth the time for the transition
            // as seen further down, we need to account for autoamtic layout refreshes and manual
            // triggers separately. In particular, we have to treat the first refresh in a series
            // of automatic refreshes as special and essentially equivalent to a manual non-refreshing
            // layout triggered by the user. Like I said, involved.
            var allowForDurationAdjustment = false;
            if (refresh === false) {
                // For non-refreshign manual layout triggers, do not allow time adjustment,
                // always use the maximal transition animation duration.
                this.lastRefreshArg = false;
            }
            else if (refresh === true) {
                // First refresh in series does not allow for time adjustment, subsequent do. 
                allowForDurationAdjustment = this.lastRefreshArg;
                this.lastRefreshArg = true;
            }
            var now = new Date().getTime();
            // When the graph is still growing, calls to the layout occur. This causes
            // a stuttering of movement if we do transitions, and without, the node
            // teleportation is confusing.
            // So, when we are refreshing a layout (node/edge addition), we need the
            // layout duration to continue along the path it was on. This *might* backfire
            // when we have an extremely long set of node or edge additions though.
            // If so, try having a minimum transition duration.
            var reduceDurationBy = 0;
            if (null !== this.lastTransition && allowForDurationAdjustment && (now - this.lastTransition) <= this.staleTimerThreshold) {
                reduceDurationBy = now - this.lastTransition;
            }
            var duration = this.desiredDuration - reduceDurationBy;
            // The filtering we do on these is required due to delays in removal caused by animation of node removal.
            // Before I added the D3 transition to the node removal function, this worked fine, but it throws errors
            // when the selection with a runnign transition suddenly loses members.
            d3.selectAll("g.node_g").filter(function (node, i) {
                return transitionOnlyFixedNodes ? node.fixed : true;
            }).filter(function (node, i) {
                return null !== outerThis.graph.containsNode(node);
            }).transition().duration(duration).ease("linear").attr("transform", function (d) {
                return "translate(" + d.x + "," + d.y + ")";
            });
            // NB If we are doing translateOnlyFixedNodes, will transitioning unfixed arcs break things?
            d3.selectAll(GraphView.BaseGraphView.linkSvgClass).filter(function (link, i) {
                return null !== outerThis.graph.containsNodeById(link.sourceId) && null !== outerThis.graph.containsNodeById(link.targetId) && null !== link.source && null !== link.target;
            }).transition().duration(duration).ease("linear").attr("points", outerThis.graphView.updateArcLineFunc);
            d3.selectAll(GraphView.BaseGraphView.linkMarkerSvgClass).filter(function (link, i) {
                return null !== outerThis.graph.containsNodeById(link.sourceId) && null !== outerThis.graph.containsNodeById(link.targetId) && null !== link.source && null !== link.target;
            }).transition().duration(duration).ease("linear").attr("points", outerThis.graphView.updateArcMarkerFunc);
            window.setTimeout(function () {
                _this.graphView.stampTimeLayoutModified();
                _this.graphView.renderMiniMap(true);
            }, duration + 100);
            if (this.lastTransition === null || !refresh || (now - this.lastTransition) > this.staleTimerThreshold) {
                this.lastTransition = new Date().getTime();
            }
        };
        ConceptLayouts.prototype.semanticZoom = function () {
            var outerThis = this;
            return function () {
                var zoomValue = $("#spacingSliderDiv").slider("value") / 1000;
                outerThis.forceLayout.stop();
                var graphNodes = outerThis.graph.graphD3Format.nodes;
                graphNodes.forEach(function (node) {
                    console.log("x before = ");
                    console.log(node.x);
                    node.x = node.x * zoomValue;
                    console.log("x after = ");
                    console.log(node.x);
                    node.y = node.y * zoomValue;
                });
                //            d3.select("#link_container")
                //                .attr("transform", "scale(" + zoomValue + ")");
                //            d3.select("#node_container")
                //                .attr("transform", "scale(" + zoomValue + ")");
                d3.selectAll("g.node_g").attr("transform", function (d) {
                    return "translate(" + d.x + ", " + d.y + ")";
                });
                d3.selectAll(GraphView.BaseGraphView.linkSvgClass).attr("points", outerThis.graphView.updateArcLineFunc);
                d3.selectAll(GraphView.BaseGraphView.linkMarkerSvgClass).attr("points", outerThis.graphView.updateArcMarkerFunc);
            };
        };
        ConceptLayouts.prototype.getAllOntologyAcronyms = function (graphNodes) {
            var ontologies = [];
            var outerThis = this;
            graphNodes.forEach(function (node) {
                if ($.inArray(node.ontologyAcronym, ontologies) === -1) {
                    ontologies.push(node.ontologyAcronym);
                }
            });
            ontologies = ontologies.sort(function (a, b) {
                if (a > b) {
                    return -1;
                }
                else if (a < b) {
                    return 1;
                }
                else {
                    return 0;
                }
            });
            return ontologies;
        };
        ConceptLayouts.prototype.getChildren = function (parentNode, graphLinks, graphNodes) {
            var outerThis = this;
            var children = [];
            graphLinks.forEach(function (link) {
                if (link.sourceId == parentNode.nodeId && link.relationType != "maps_to") {
                    graphNodes.forEach(function (node) {
                        if (node.nodeId == link.targetId && $.inArray(node, children) === -1 && parentNode.ontologyAcronym == node.ontologyAcronym) {
                            if (link.relationType == "is_a" || (link.relationType != "is_a" && node.inheritanceChild == false)) {
                                children.push(node);
                            }
                        }
                    });
                }
            });
            return children;
        };
        ConceptLayouts.prototype.markInheritanceChildren = function () {
            var outerThis = this;
            var graphNodes = outerThis.graph.graphD3Format.nodes;
            var graphLinks = outerThis.graph.graphD3Format.links;
            graphLinks = graphLinks.filter(function (l) {
                return l.relationType == "is_a";
            });
            graphLinks.forEach(function (link) {
                graphNodes.forEach(function (node) {
                    if (node.nodeId == link.targetId) {
                        node.inheritanceChild = true;
                    }
                });
            });
        };
        ConceptLayouts.prototype.getRoots = function (ontologyAcronym, graphNodes, graphLinks) {
            var outerThis = this;
            var nodes = graphNodes.filter(function (n) {
                return n.ontologyAcronym == ontologyAcronym;
            });
            var roots = [];
            var isRoot = true;
            nodes.forEach(function (node) {
                nodes.forEach(function (parent) {
                    parent.treeChildren.forEach(function (child) {
                        if (child.nodeId == node.nodeId) {
                            isRoot = false;
                        }
                    });
                });
                if (isRoot) {
                    roots.push(node);
                }
                isRoot = true;
            });
            return roots;
        };
        ConceptLayouts.prototype.resetGraphValues = function () {
            var graphNodes = this.graph.graphD3Format.nodes;
            //reset values for next layout
            graphNodes.forEach(function (node) {
                node.tempDepth = 0;
                node.depth = 0;
                node.x = 0;
                node.y = 0;
                node.children = null;
                node.parent = null;
                node.visited = false;
            });
        };
        ConceptLayouts.prototype.resetInheritanceChildValues = function () {
            var graphNodes = this.graph.graphD3Format.nodes;
            //reset values for the next layout
            graphNodes.forEach(function (node) {
                node.inheritanceChild = false;
            });
        };
        ConceptLayouts.prototype.computeDepth = function (parent) {
            if (parent.visited == false) {
                parent.visited = true;
                var outerThis = this;
                ConceptLayouts.tempDepth++;
                var children = parent.treeChildren;
                children.forEach(function (node) {
                    if (node.visited == false) {
                        outerThis.computeDepth(node);
                    }
                });
                if (ConceptLayouts.tempDepth > ConceptLayouts.tempTreeDepth) {
                    ConceptLayouts.tempTreeDepth = ConceptLayouts.tempDepth;
                }
                ConceptLayouts.tempDepth--;
            }
        };
        ConceptLayouts.prototype.depthFirstTraversal = function (parent) {
            if (parent.visited == false) {
                parent.visited = true;
                var outerThis = this;
                var children = parent.treeChildren;
                var treeChildren = [];
                children.forEach(function (node) {
                    if (node.visited == false) {
                        treeChildren.push(node);
                        outerThis.depthFirstTraversal(node);
                    }
                });
                parent.treeChildren = treeChildren;
            }
        };
        ConceptLayouts.prototype.prepareTreeData = function (graphNodes, graphLinks) {
            var outerThis = this;
            outerThis.resetGraphValues();
            outerThis.resetInheritanceChildValues();
            outerThis.markInheritanceChildren();
            //get children nodes excluding non-inheritance links to inheritance children
            graphNodes.forEach(function (node) {
                node.treeChildren = outerThis.getChildren(node, graphLinks, graphNodes);
            });
            //remove cycles first on non-inheritance then on inheritance children
            var nonInheritanceNodes = graphNodes.filter(function (node) {
                return node.inheritanceChild == false;
            });
            nonInheritanceNodes.forEach(function (node) {
                outerThis.resetGraphValues();
                outerThis.depthFirstTraversal(node);
            });
            var inheritanceNodes = graphNodes.filter(function (node) {
                return node.inheritanceChild == true;
            });
            inheritanceNodes.forEach(function (node) {
                outerThis.resetGraphValues();
                outerThis.depthFirstTraversal(node);
            });
            //remove additional parents
            outerThis.resetGraphValues();
            graphNodes.forEach(function (node) {
                var children = node.treeChildren;
                var treeChildren = [];
                children.forEach(function (child) {
                    if (child.visited == false) {
                        child.visited = true;
                        treeChildren.push(child);
                    }
                });
                node.treeChildren = treeChildren;
            });
            //calculate depth
            ConceptLayouts.fullTreeDepth = 0;
            ConceptLayouts.tempTreeDepth = 0;
            ConceptLayouts.tempDepth = 0;
            graphNodes.forEach(function (node) {
                outerThis.resetGraphValues();
                outerThis.computeDepth(node);
                if (ConceptLayouts.fullTreeDepth < ConceptLayouts.tempTreeDepth) {
                    ConceptLayouts.fullTreeDepth = ConceptLayouts.tempTreeDepth;
                }
            });
        };
        ConceptLayouts.prototype.buildTree = function (width, height) {
            var outerThis = this;
            var graphNodes = outerThis.graph.graphD3Format.nodes;
            var graphLinks = outerThis.graph.graphD3Format.links;
            graphNodes = graphNodes.sort(function (a, b) {
                if (a.ontologyAcronym > b.ontologyAcronym) {
                    return -1;
                }
                else if (a.ontologyAcronym < b.ontologyAcronym) {
                    return 1;
                }
                else {
                    return 0;
                }
            });
            graphNodes = graphNodes.sort(function (a, b) {
                if (a.nodeId > b.nodeId) {
                    return -1;
                }
                else if (a.nodeId < b.nodeId) {
                    return 1;
                }
                else {
                    return 0;
                }
            });
            outerThis.prepareTreeData(graphNodes, graphLinks);
            //calculate tree height and adjust for phantom nodes
            ConceptLayouts.fullTreeDepth--; //adjust depth for the number of links (not nodes)
            if (ConceptLayouts.fullTreeDepth != 0) {
                height = height * (ConceptLayouts.fullTreeDepth + 2) / (ConceptLayouts.fullTreeDepth);
            }
            var ontologies = outerThis.getAllOntologyAcronyms(graphNodes);
            var mainTree = d3.layout.tree().size([width, height]).children(function (parent) {
                if (parent.name == "main_phantom_root") {
                    //create ontology roots
                    var ontologyRoots = [];
                    ontologies.forEach(function (ontologyName) {
                        var ontologyRoot = new ConceptGraph.Node();
                        ontologyRoot.name = ontologyName;
                        ontologyRoots.push(ontologyRoot);
                    });
                    return ontologyRoots;
                }
                else if ($.inArray(parent.name, ontologies) != -1) {
                    var roots;
                    roots = outerThis.getRoots(parent.name, graphNodes, graphLinks);
                    return roots;
                }
                else {
                    return parent.treeChildren;
                }
            });
            var primaryRoot = new ConceptGraph.Node();
            primaryRoot.name = "main_phantom_root"; //temporary identifier for the primary root
            var treeNodes = mainTree.nodes(primaryRoot); // build tree based on primary phantom root  
            // shift the tree by 2 node distances for main node and ontolofy nodes
            graphNodes.forEach(function (node) {
                node.y = node.y - height * 2 / (ConceptLayouts.fullTreeDepth + 2);
            });
        };
        ConceptLayouts.prototype.runRadialLayoutLambda = function () {
            var outerThis = this;
            return function (refreshLayout) {
                if (refreshLayout) {
                }
                outerThis.graph.graphView.stampTimeLayoutModified();
                outerThis.forceLayout.stop();
                var graphNodes = outerThis.graph.graphD3Format.nodes;
                var graphLinks = outerThis.graph.graphD3Format.links;
                var ontologies = outerThis.getAllOntologyAcronyms(graphNodes);
                var numOfRoots = 0;
                ontologies.forEach(function (o) {
                    var roots = outerThis.getRoots(o, graphNodes, graphLinks);
                    numOfRoots += roots.length;
                });
                var minShift = 0.05 * outerThis.graphView.visHeight();
                var maxShift = outerThis.graphView.visHeight() / 2 - minShift;
                var yShift = numOfRoots * 20;
                if (yShift < minShift) {
                    yShift = minShift;
                }
                if (yShift > maxShift) {
                    yShift = maxShift;
                }
                var treeWidth = 360;
                var treeHeight = (outerThis.graphView.visHeight() - yShift - 100) / 2;
                outerThis.buildTree(treeWidth, treeHeight);
                $.each(graphNodes, function (index, element) {
                    var radius = element.y + yShift / 2;
                    var angle = (element.x) / 180 * Math.PI;
                    graphNodes[index].x = outerThis.graphView.visWidth() / 2 + radius * Math.cos(angle);
                    graphNodes[index].y = outerThis.graphView.visHeight() / 2 + radius * Math.sin(angle);
                });
                outerThis.transitionNodes(refreshLayout);
            };
        };
        ConceptLayouts.prototype.runVerticalTreeLayoutLambda = function () {
            var outerThis = this;
            return function (refreshLayout) {
                if (refreshLayout) {
                }
                outerThis.graph.graphView.stampTimeLayoutModified();
                outerThis.forceLayout.stop();
                var xShift = 0.05 * outerThis.graphView.visWidth();
                var yShift = 0.2 * outerThis.graphView.visHeight();
                var treeWidth = outerThis.graphView.visWidth() - xShift;
                var treeHeight = outerThis.graphView.visHeight() - yShift;
                var graphNodes = outerThis.graph.graphD3Format.nodes;
                outerThis.buildTree(treeWidth, treeHeight);
                $.each(graphNodes, function (index, element) {
                    graphNodes[index].x = element.x + xShift / 2;
                    graphNodes[index].y = element.y + yShift / 2;
                });
                outerThis.transitionNodes(refreshLayout);
            };
        };
        ConceptLayouts.prototype.runHorizontalTreeLayoutLambda = function () {
            var outerThis = this;
            return function (refreshLayout) {
                if (refreshLayout) {
                }
                outerThis.graph.graphView.stampTimeLayoutModified();
                outerThis.forceLayout.stop();
                var graphNodes = outerThis.graph.graphD3Format.nodes;
                var xShift = 0.3 * outerThis.graphView.visHeight();
                var yShift = 0.05 * outerThis.graphView.visWidth();
                var treeWidth = outerThis.graphView.visHeight() - yShift;
                var treeHeight = outerThis.graphView.visWidth() - xShift;
                outerThis.buildTree(treeWidth, treeHeight);
                $.each(graphNodes, function (index, element) {
                    var xValue = element.x;
                    graphNodes[index].x = element.y + xShift / 2;
                    graphNodes[index].y = xValue + yShift / 2;
                });
                outerThis.transitionNodes(refreshLayout);
            };
        };
        ConceptLayouts.prototype.runCircleLayoutLambda = function () {
            var outerThis = this;
            return function (refreshLayout) {
                if (refreshLayout) {
                }
                outerThis.graph.graphView.stampTimeLayoutModified();
                outerThis.forceLayout.stop();
                var graphNodes = outerThis.graph.graphD3Format.nodes;
                var graphLinks = outerThis.graph.graphD3Format.links;
                var numberOfConcepts = Object.keys(graphNodes).length;
                var anglePerNode = 2 * Math.PI / numberOfConcepts; // 360/numberOfMappedOntologies;
                var arcLength = outerThis.graphView.linkMaxDesiredLength();
                var i = 0;
                $.each(graphNodes, function (index, element) {
                    var angleForNode = i * anglePerNode;
                    i++;
                    graphNodes[index].x = outerThis.graphView.visWidth() / 2 + arcLength * Math.cos(angleForNode); // start in middle and let them fly outward
                    graphNodes[index].y = outerThis.graphView.visHeight() / 2 + arcLength * Math.sin(angleForNode); // start in middle and let them fly outward
                });
                outerThis.transitionNodes(refreshLayout);
            };
        };
        ConceptLayouts.prototype.runCenterLayoutLambda = function () {
            var outerThis = this;
            return function (refreshLayout) {
                if (refreshLayout) {
                }
                outerThis.graph.graphView.stampTimeLayoutModified();
                outerThis.forceLayout.stop();
                var graphNodes = outerThis.graph.graphD3Format.nodes;
                var graphLinks = outerThis.graph.graphD3Format.links;
                var numberOfConcepts = Object.keys(graphNodes).length - 1;
                var anglePerNode = 2 * Math.PI / numberOfConcepts; // 360/numberOfMappedOntologies;
                var arcLength = outerThis.graphView.linkMaxDesiredLength();
                var i = 0;
                $.each(graphNodes, function (index, node) {
                    if (node.nodeId != outerThis.centralConceptUri) {
                        var angleForNode = i * anglePerNode;
                        i++;
                        node.x = outerThis.graphView.visWidth() / 2 + arcLength * Math.cos(angleForNode); // start in middle and let them fly outward
                        node.y = outerThis.graphView.visHeight() / 2 + arcLength * Math.sin(angleForNode); // start in middle and let them fly outward
                    }
                    else {
                        node.x = outerThis.graphView.visWidth() / 2;
                        node.y = outerThis.graphView.visHeight() / 2;
                    }
                });
                outerThis.transitionNodes(refreshLayout);
            };
        };
        ConceptLayouts.prototype.runForceLayoutLambda = function () {
            var outerThis = this;
            return function (refreshLayout) {
                if (refreshLayout) {
                    // If we add a node to force layout, reheat it slightly
                    outerThis.forceLayout.resume();
                    return;
                }
                outerThis.graph.graphView.stampTimeLayoutModified();
                var graphNodes = outerThis.graph.graphD3Format.nodes;
                // The nodes may have been fixed in the fixed layout, or when dragging them.
                // If we are not merely refreshing, let them all be free to move.
                $.each(graphNodes, function (index, node) {
                    node.fixed = false;
                });
                outerThis.forceLayout.friction(0.3).gravity(0.05).linkStrength(0.1).charge(-800);
                outerThis.forceLayout.on("tick", outerThis.graphView.onLayoutTick());
                outerThis.forceLayout.start();
            };
        };
        ConceptLayouts.prototype.runFixedPositionLayoutLambda = function () {
            var outerThis = this;
            return function (refreshLayout) {
                if (refreshLayout) {
                }
                outerThis.graph.graphView.stampTimeLayoutModified();
                outerThis.forceLayout.stop();
                var graphNodes = outerThis.graph.graphD3Format.nodes;
                $.each(graphNodes, function (index, node) {
                    if (undefined !== outerThis.currentFixedLayoutData[String(node.nodeId)]) {
                        node.x = outerThis.currentFixedLayoutData[String(node.nodeId)].x;
                        node.y = outerThis.currentFixedLayoutData[String(node.nodeId)].y;
                        node.fixed = true;
                    }
                    else {
                    }
                });
                // The transition is needed for the fixed positions, but not the force layout ones...
                outerThis.transitionNodes(refreshLayout, true);
            };
        };
        ConceptLayouts.prototype.updateFixedLayoutDatum = function (nodeId, coordinates) {
            this.currentFixedLayoutData[String(nodeId)] = coordinates;
            $("#importedLayoutButton").slideDown();
        };
        ConceptLayouts.prototype.updateFixedLayoutData = function (newPositions) {
            for (var nodeId in newPositions) {
                this.currentFixedLayoutData[nodeId] = newPositions[nodeId];
            }
            if (this.currentFixedLayoutData !== undefined) {
                $("#importedLayoutButton").slideDown();
            }
            else {
                $("#importedLayoutButton").slideUp();
            }
        };
        ConceptLayouts.layoutMenuContainerId = "layoutMenuContainer";
        return ConceptLayouts;
    })();
    exports.ConceptLayouts = ConceptLayouts;
});
