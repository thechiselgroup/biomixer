var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../Utils", "../FetchFromApi", "../GraphView", "Utils", "FetchFromApi", "GraphView", "ExpansionSets", "LayoutProvider"], function (require, exports, Utils, Fetcher, GraphView) {
    var hardNodeCap = 0;
    var Node = (function (_super) {
        __extends(Node, _super);
        function Node() {
            _super.call(this);
        }
        Node.prototype.getEntityId = function () {
            return String(this.acronymForIds);
        };
        Node.D3IdentityFunction = function (d) {
            return String(d.rawAcronym);
        };
        return Node;
    })(GraphView.BaseNode);
    exports.Node = Node;
    var Link = (function (_super) {
        __extends(Link, _super);
        function Link() {
            _super.call(this);
        }
        Link.D3IdentityFunction = function (d) {
            return d.source.rawAcronym + "-to-" + d.target.rawAcronym;
        };
        return Link;
    })(GraphView.BaseLink);
    exports.Link = Link;
    var OntologyD3Data = (function (_super) {
        __extends(OntologyD3Data, _super);
        function OntologyD3Data() {
            _super.apply(this, arguments);
        }
        return OntologyD3Data;
    })(GraphView.GraphDataForD3);
    exports.OntologyD3Data = OntologyD3Data;
    var OntologyGraph = (function () {
        function OntologyGraph(graphView, softNodeCap, centralOntologyAcronym) {
            this.graphView = graphView;
            this.softNodeCap = softNodeCap;
            this.centralOntologyAcronym = centralOntologyAcronym;
            this.graphD3Format = new OntologyD3Data();
            this.sortedAcronymsByMappingCount = [];
            this.centralOntologyNode = null;
            this.currentNodeColor = -1;
            this.nodeOrderedColors = d3.scale.category20().domain([0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]);
        }
        OntologyGraph.prototype.addNodes = function (newNodes, expansionSet) {
            expansionSet.addAll(newNodes);
            for (var i = 0; i < newNodes.length; i++) {
                this.graphD3Format.nodes.push(newNodes[i]);
            }
            this.graphView.populateNewGraphElements(this.graphD3Format);
        };
        OntologyGraph.prototype.removeNodes = function (nodesToRemove) {
            console.log("Unimplemented. Get on it!");
        };
        OntologyGraph.prototype.containsNode = function (node) {
            return this.graphD3Format.nodes.indexOf(node) !== -1;
        };
        OntologyGraph.prototype.findNodesByName = function (substringRaw) {
            var substringLower = substringRaw.toLowerCase();
            var matchNodes = this.graphD3Format.nodes.filter(function (node, index, nodes) {
                return node.name.toLowerCase().search(substringLower) > -1 || node.rawAcronym.toLowerCase().search(substringLower) > -1;
            });
            return matchNodes;
        };
        OntologyGraph.prototype.addNodeToGraph = function (newNodeId) {
            console.log("Unimplemented. Allow for addition of ontology nodes by acronym or URI if desired.");
        };
        OntologyGraph.prototype.addEdges = function (newEdges) {
            for (var i = 0; i < newEdges.length; i++) {
                this.graphD3Format.links.push(newEdges[i]);
            }
            this.graphView.populateNewGraphElements(this.graphD3Format);
        };
        OntologyGraph.prototype.removeEdge = function () {
            console.log("Unimplemented. Get it done!");
        };
        OntologyGraph.prototype.getLayoutProvider = function () {
            return null;
        };
        OntologyGraph.prototype.setLayoutProvider = function (layoutProvider) {
        };
        OntologyGraph.prototype.fetchOntologyNeighbourhood = function (centralOntologyAcronym, expansionSet) {
            var ontologyMappingUrl = buildOntologyMappingUrlNewApi(centralOntologyAcronym);
            var ontologyMappingCallback = new OntologyMappingCallback(this, ontologyMappingUrl, centralOntologyAcronym, expansionSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(ontologyMappingUrl);
            fetcher.fetch(ontologyMappingCallback, true);
        };
        OntologyGraph.prototype.fetchNodeRestData = function (node) {
            this.fetchNodeMetricsData(node);
            this.fetchNodeDescriptionData(node);
            return true;
        };
        OntologyGraph.prototype.fetchNodeMetricsData = function (node) {
            var ontologyMetricsUrl = buildOntologyMetricsUrlNewApi(node.rawAcronym);
            if (Fetcher.CacheRegistry.isNotRegisteredInCache(ontologyMetricsUrl)) {
                var ontologyMetricsCallback = new OntologyMetricsCallback(this, ontologyMetricsUrl, node);
                var fetcher = new Fetcher.RetryingJsonFetcher(ontologyMetricsUrl);
                fetcher.fetch(ontologyMetricsCallback, true);
            }
        };
        OntologyGraph.prototype.fetchNodeDescriptionData = function (node) {
            var ontologyDescriptionUrl = buildOntologyLatestSubmissionUrlNewApi(node.rawAcronym);
            if (Fetcher.CacheRegistry.isNotRegisteredInCache(ontologyDescriptionUrl)) {
                var ontologyDescriptionCallback = new OntologyDescriptionCallback(this, ontologyDescriptionUrl, node);
                var fetcher = new Fetcher.RetryingJsonFetcher(ontologyDescriptionUrl);
                fetcher.fetch(ontologyDescriptionCallback, true);
            }
        };
        OntologyGraph.prototype.getNumberOfPotentialNodesToExpand = function (incomingNodeId, nodeInteraction) {
            return -1;
        };
        OntologyGraph.prototype.cropGraphToSubset = function (acronymsToKeep) {
            this.graphD3Format.nodes = $.grep(this.graphD3Format.nodes, function (value) {
                return $.inArray(value.rawAcronym, acronymsToKeep) != -1;
            });
            this.graphD3Format.links = $.grep(this.graphD3Format.links, function (value) {
                return $.inArray(value.source.rawAcronym, acronymsToKeep) != -1 && $.inArray(value.target.rawAcronym, acronymsToKeep) != -1;
            });
            this.sortedAcronymsByMappingCount = $.grep(this.sortedAcronymsByMappingCount, function (entry) {
                return acronymsToKeep.indexOf(entry.acronym) != -1;
            });
            this.graphView.removeMissingGraphElements(this.graphD3Format);
        };
        OntologyGraph.prototype.nextNodeColor = function () {
            this.currentNodeColor = this.currentNodeColor == 19 ? 0 : this.currentNodeColor + 1;
            return this.nodeOrderedColors(this.currentNodeColor);
        };
        OntologyGraph.prototype.brightenColor = function (outerColor) {
            return d3.lab(outerColor).brighter(1).toString();
        };
        OntologyGraph.prototype.darkenColor = function (outerColor) {
            return d3.lab(outerColor).darker(1).toString();
        };
        return OntologyGraph;
    })();
    exports.OntologyGraph = OntologyGraph;
    var OntologyMappingCallback = (function (_super) {
        __extends(OntologyMappingCallback, _super);
        function OntologyMappingCallback(graph, url, centralOntologyAcronym, expansionSet) {
            var _this = this;
            _super.call(this, url, centralOntologyAcronym, 4 /* fullOntologyMapping */);
            this.graph = graph;
            this.centralOntologyAcronym = centralOntologyAcronym;
            this.expansionSet = expansionSet;
            this.callback = function (mappingData, textStatus, jqXHR) {
                if (jqXHR != null) {
                    if (mappingData.errors != null) {
                        console.log("Failed to load mappings for: " + _this.centralOntologyAcronym);
                        return;
                    }
                }
                $.each(mappingData, function (index, element) {
                    _this.graph.sortedAcronymsByMappingCount.push({ acronym: index, node: undefined });
                });
                _this.graph.sortedAcronymsByMappingCount.sort(function (a, b) {
                    return mappingData[String(b.acronym)] - mappingData[String(a.acronym)];
                });
                if (hardNodeCap != 0 && _this.graph.sortedAcronymsByMappingCount.length > hardNodeCap) {
                    _this.graph.sortedAcronymsByMappingCount = _this.graph.sortedAcronymsByMappingCount.slice(0, hardNodeCap);
                }
                var numberOfMappedOntologies = _this.graph.sortedAcronymsByMappingCount.length;
                var originalNumberOfMappedOntologies = Object.keys(mappingData).length;
                var defaultNumOfTermsForSize = 10;
                var newNodesForExpansionSet = [];
                var newLinks = [];
                var centralOntologyNode = new Node();
                _this.graph.centralOntologyNode = centralOntologyNode;
                centralOntologyNode.name = "fetching" + " (" + _this.centralOntologyAcronym + ")";
                centralOntologyNode.description = "fetching description";
                centralOntologyNode.fixed = true;
                centralOntologyNode.x = _this.graph.graphView.visWidth() / 2;
                centralOntologyNode.y = _this.graph.graphView.visHeight() / 2;
                centralOntologyNode.weight = numberOfMappedOntologies;
                centralOntologyNode.number = defaultNumOfTermsForSize;
                centralOntologyNode.acronymForIds = Utils.escapeIdentifierForId(_this.centralOntologyAcronym);
                centralOntologyNode.rawAcronym = _this.centralOntologyAcronym;
                centralOntologyNode.nodeColor = _this.graph.nextNodeColor();
                centralOntologyNode.innerNodeColor = _this.graph.brightenColor(centralOntologyNode.nodeColor);
                centralOntologyNode.nodeStrokeColor = _this.graph.darkenColor(centralOntologyNode.nodeColor);
                centralOntologyNode.mapped_classes_to_central_node = 0;
                newNodesForExpansionSet.push(centralOntologyNode);
                $.each(_this.graph.sortedAcronymsByMappingCount, function (index, sortedAcronym) {
                    if (sortedAcronym.acronym == centralOntologyNode.rawAcronym) {
                        sortedAcronym.node = centralOntologyNode;
                    }
                });
                var ontologyAcronymNodeMap = {};
                ontologyAcronymNodeMap["vid:" + centralOntologyNode.rawAcronym] = centralOntologyNode;
                var anglePerNode = 2 * Math.PI / numberOfMappedOntologies;
                var arcLength = _this.graph.graphView.linkMaxDesiredLength();
                var i = 0;
                $.each(_this.graph.sortedAcronymsByMappingCount, function (index, sortedAcronym) {
                    var acronym = sortedAcronym.acronym;
                    var mappingCount = mappingData[String(acronym)];
                    if (typeof acronym === "undefined") {
                        console.log("Undefined ontology entry");
                    }
                    var ontologyNode = new Node();
                    ontologyNode.name = "fetching" + " (" + acronym + ")";
                    ontologyNode.description = "fetching description";
                    ontologyNode.weight = 1;
                    ontologyNode.fixed = false;
                    var angleForNode = i * anglePerNode;
                    i++;
                    ontologyNode.x = _this.graph.graphView.visWidth() / 2 + arcLength * Math.cos(angleForNode);
                    ontologyNode.y = _this.graph.graphView.visHeight() / 2 + arcLength * Math.sin(angleForNode);
                    ontologyNode.number = defaultNumOfTermsForSize;
                    ontologyNode.acronymForIds = Utils.escapeIdentifierForId(acronym);
                    ontologyNode.rawAcronym = acronym;
                    ontologyNode.nodeColor = _this.graph.nextNodeColor();
                    ontologyNode.innerNodeColor = _this.graph.brightenColor(ontologyNode.nodeColor);
                    ontologyNode.nodeStrokeColor = _this.graph.darkenColor(ontologyNode.nodeColor);
                    ontologyNode.mapped_classes_to_central_node = 0;
                    newNodesForExpansionSet.push(ontologyNode);
                    ontologyAcronymNodeMap["vid:" + ontologyNode.rawAcronym] = ontologyNode;
                    sortedAcronym.node = ontologyNode;
                    var ontologyLink = new Link();
                    ontologyLink.source = centralOntologyNode;
                    ontologyLink.target = ontologyNode;
                    ontologyLink.value = mappingCount;
                    ontologyLink.numMappings = mappingCount;
                    newLinks.push(ontologyLink);
                    ontologyNode.mapped_classes_to_central_node = ontologyLink.value;
                });
                _this.graph.addNodes(newNodesForExpansionSet, _this.expansionSet);
                _this.graph.addEdges(newLinks);
                _this.graph.fetchNodeRestData(centralOntologyNode);
                var ontologyDetailsUrl = buildOntologyDetailsUrlNewApi();
                var ontologyDetailsCallback = new OntologyDetailsCallback(_this.graph, ontologyDetailsUrl, ontologyAcronymNodeMap);
                var fetcher = new Fetcher.RetryingJsonFetcher(ontologyDetailsUrl);
                fetcher.fetch(ontologyDetailsCallback, true);
            };
        }
        return OntologyMappingCallback;
    })(Fetcher.CallbackObject);
    var OntologyDetailsCallback = (function (_super) {
        __extends(OntologyDetailsCallback, _super);
        function OntologyDetailsCallback(graph, url, ontologyAcronymNodeMap) {
            var _this = this;
            _super.call(this, url, "", 3 /* metaData */);
            this.graph = graph;
            this.ontologyAcronymNodeMap = ontologyAcronymNodeMap;
            this.callback = function (detailsDataRaw, textStatus, jqXHR) {
                if (jqXHR != null) {
                    if (detailsDataRaw.errors != null) {
                        return;
                    }
                }
                console.log("Processing details " + Utils.getTime());
                var ontologiesSkipped = 0;
                var acronymsNotSkipped = [];
                $.each(detailsDataRaw, function (index, ontologyDetails) {
                    var ontologyAcronym = ontologyDetails.acronym;
                    var node = _this.ontologyAcronymNodeMap["vid:" + ontologyAcronym];
                    if (typeof node === "undefined") {
                        ontologiesSkipped += 1;
                    }
                    else {
                        acronymsNotSkipped.push(ontologyAcronym);
                    }
                });
                console.log("Cropping " + Utils.getTime());
                _this.graph.cropGraphToSubset(acronymsNotSkipped);
                _this.graph.graphView.filterGraphOnMappingCounts();
                console.log("ontologyDetailsCallback, removed " + ontologiesSkipped + " '403 error' ontologies of total " + detailsDataRaw.length + " " + Utils.getTime());
                _this.graph.graphView.updateDataForNodesAndLinks({ nodes: _this.graph.graphD3Format.nodes, links: [] });
            };
        }
        return OntologyDetailsCallback;
    })(Fetcher.CallbackObject);
    var OntologyMetricsCallback = (function (_super) {
        __extends(OntologyMetricsCallback, _super);
        function OntologyMetricsCallback(graph, url, node) {
            var _this = this;
            _super.call(this, url, String(node.rawAcronym), 3 /* metaData */);
            this.graph = graph;
            this.node = node;
            this.callback = function (metricDataRaw, textStatus, jqXHR) {
                if (jqXHR != null) {
                    if (metricDataRaw.errors != null) {
                        return;
                    }
                }
                var metricData = metricDataRaw;
                var nodeSizeBasis = 100;
                var numClasses = 0, numIndividuals = 0, numProperties = 0;
                if (typeof metricData !== "undefined") {
                    if (metricData.classes != null) {
                        numClasses = metricData.classes;
                        nodeSizeBasis = numClasses;
                    }
                    if (metricData.individuals != null) {
                        numIndividuals = metricData.individuals;
                    }
                    if (metricData.properties != null) {
                        numProperties = metricData.properties;
                    }
                }
                _this.node.numberOfClasses = numClasses;
                _this.node.numberOfIndividuals = numIndividuals;
                _this.node.numberOfProperties = numProperties;
                _this.node.number = nodeSizeBasis;
                _this.graph.graphView.updateDataForNodesAndLinks({ nodes: [_this.node], links: [] });
            };
        }
        return OntologyMetricsCallback;
    })(Fetcher.CallbackObject);
    var OntologyDescriptionCallback = (function (_super) {
        __extends(OntologyDescriptionCallback, _super);
        function OntologyDescriptionCallback(graph, url, node) {
            var _this = this;
            _super.call(this, url, String(node.rawAcronym), 3 /* metaData */);
            this.graph = graph;
            this.node = node;
            this.callback = function (latestSubmissionData, textStatus, jqXHR) {
                if (jqXHR != null) {
                    if (latestSubmissionData.errors != null) {
                        return;
                    }
                }
                var description = "";
                if (typeof latestSubmissionData !== "undefined") {
                    if (latestSubmissionData.description != null) {
                        description = latestSubmissionData.description;
                    }
                    else if (typeof latestSubmissionData.error != null) {
                        description = latestSubmissionData.error;
                    }
                }
                _this.node.description = description;
                if (null != latestSubmissionData.ontology && null != latestSubmissionData.ontology.name) {
                    _this.node.name = latestSubmissionData.ontology.name + "(" + _this.node.rawAcronym + ")";
                    _this.node.LABEL = latestSubmissionData.ontology.name;
                    _this.node.uriId = latestSubmissionData.links.ontology;
                }
                _this.graph.graphView.updateDataForNodesAndLinks({ nodes: [_this.node], links: [] });
            };
        }
        return OntologyDescriptionCallback;
    })(Fetcher.CallbackObject);
    function buildOntologyMappingUrlNewApi(centralOntologyAcronym) {
        return "http://" + Utils.getBioportalUrl() + "/mappings/statistics/ontologies/" + centralOntologyAcronym;
    }
    function buildOntologyDetailsUrlNewApi() {
        return "http://" + Utils.getBioportalUrl() + "/ontologies";
    }
    function buildOntologyMetricsUrlNewApi(ontologyAcronym) {
        return "http://" + Utils.getBioportalUrl() + "/ontologies/" + ontologyAcronym + "/metrics";
    }
    function buildOntologyLatestSubmissionUrlNewApi(ontologyAcronym) {
        return "http://" + Utils.getBioportalUrl() + "/ontologies/" + ontologyAcronym + "/latest_submission";
    }
});
