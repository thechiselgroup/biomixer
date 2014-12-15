///<reference path="headers/require.d.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../Utils", "../FetchFromApi", "../GraphView", "Utils", "FetchFromApi", "GraphView", "ExpansionSets", "LayoutProvider"], function(require, exports, Utils, Fetcher, GraphView) {
    // Apparently all modules in the same level directory can see eachother? I deleted the imports
    // above and I could still access everything!
    // Use these caps with a sorted array of nodes
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
        // This softNodeCap only affects API dispatch and rendering for nodes past the cap. It is used during
        // initialization only. Set to 0 means all nodes will be used.
        function OntologyGraph(graphView, softNodeCap, centralOntologyAcronym) {
            this.graphView = graphView;
            this.softNodeCap = softNodeCap;
            this.centralOntologyAcronym = centralOntologyAcronym;
            // Need:
            // sortedAcronymsByMappingCount
            // hardNodeCap
            // softNodeCap
            // a view object to get necessary information from the outside through
            this.graphD3Format = new OntologyD3Data();
            // Stores {acronyms,node} sorted by mapping count in descending order.
            // Limit it with hardNodeCap during init in dev only.
            // Slice it with softNodeCap during init.
            this.sortedAcronymsByMappingCount = [];
            this.centralOntologyNode = null;
            // Graph is responsible for its own node coloration...debate what this is: model attribute or view render?
            // In D3, the data model gets mingled with the view in this kind of way, so I feel this is ok.
            this.currentNodeColor = -1;
            this.nodeOrderedColors = d3.scale.category20().domain([0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]);
        }
        OntologyGraph.prototype.addNodes = function (newNodes, expansionSet) {
            expansionSet.addAll(newNodes);
            for (var i = 0; i < newNodes.length; i++) {
                // Only implementing here rather than in graphView because of this container...
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

        OntologyGraph.prototype.addEdges = function (newEdges) {
            for (var i = 0; i < newEdges.length; i++) {
                // Only implementing here rather than in graphView because of this container...
                this.graphD3Format.links.push(newEdges[i]);
            }

            // this.graphView.populateNewGraphEdges(this.graphD3Format.links);
            this.graphView.populateNewGraphElements(this.graphD3Format);
        };

        OntologyGraph.prototype.removeEdge = function () {
            console.log("Unimplemented. Get it done!");
        };

        OntologyGraph.prototype.getLayoutProvider = function () {
            // stub, don't need it yet
            return null;
        };

        OntologyGraph.prototype.setLayoutProvider = function (layoutProvider) {
            // stub, don't need it yet
        };

        OntologyGraph.prototype.fetchOntologyNeighbourhood = function (centralOntologyAcronym, expansionSet) {
            // I have confirmed that this is faster than BioMixer. Without removing
            // network latency in REST calls, it is approximately half as long from page load to
            // graph completion (on the order of 11 sec vs 22 sec)
            // TODO XXX Then try adding web workers around things to see if it affects it further.
            // TODO XXX I lose all the error handling and retry handling that I set up in BioMixer.
            // This is our first loss, that we have to futz with that again. It can be recreated, or if this
            // is fast enough, we can adapt things so that some of the Java work in BioMixer can be used here too
            // I mostly need to bypass the overall architecture of BioMixer to see how it affects loading speed
            // and responsivity, as well as to try using web workers (which don't work with GWT 2.5 right now)
            /* Adding BioPortal data for ontology overview graph (mapping neighbourhood of a single ontology node)
            1) Get the mapped ontology ids from the target ontology id [starts at line 126 in OntologyMappingNeighbourhood]
            http://bioportal.bioontology.org/ajax/jsonp?apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&userapikey=&path=%2Fvirtual%2Fmappings%2Fstats%2Fontologies%2F1033&callback=__gwt_jsonp__.P0.onSuccess
            - can create nodes and links with sparse meta-data now if we want, or we can wait for more data
            2) Get ontology details, which is one big json return [passed to line 167 for class OntologyMappingNeighbourhoodLoader nested class OntologyDetailsCallback]
            http://bioportal.bioontology.org/ajax/jsonp?apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&userapikey=&path=%2Fontologies%2F&callback=__gwt_jsonp__.P1.onSuccess
            - fill in nodes with details from this data
            3) Get ontology metrics for each ontology [line 82 in AutomaticOntologyExpander]
            - set node size (# of concepts), and tool tip properties of classes, individuals, properties, and notes
            http://bioportal.bioontology.org/ajax/jsonp?apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&userapikey=&path=%2Fontologies%2Fmetrics%2F45254&callback=__gwt_jsonp__.P7.onSuccess
            */
            // 1) Get mappings to central ontology
            var ontologyMappingUrl = buildOntologyMappingUrlNewApi(centralOntologyAcronym);
            var ontologyMappingCallback = new OntologyMappingCallback(this, ontologyMappingUrl, centralOntologyAcronym, expansionSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(ontologyMappingUrl);
            fetcher.fetch(ontologyMappingCallback);
        };

        /**
        * The functions attached to the nodes in here allow us to call per-node APIs as needed, rather than
        * all at once.
        *
        * When these functions are called, all dispatching and processing should happen without further consideration
        * from the caller. The function should return true if the call has been dispatched.
        *
        * Returns true if the dispatch was made...and if there was an error or other issue, it will not return true.
        *
        * Once called, the functions in here should replace themselves on the owning node with a function that returns true.
        *
        * @param node
        */
        OntologyGraph.prototype.fetchNodeRestData = function (node) {
            this.fetchNodeMetricsData(node);
            this.fetchNodeDescriptionData(node);

            return true;
        };

        OntologyGraph.prototype.fetchNodeMetricsData = function (node) {
            var ontologyMetricsUrl = buildOntologyMetricsUrlNewApi(node.rawAcronym);

            // Explicitly testing the cache for this to overcome existing speed problems.
            if (Fetcher.CacheRegistry.isNotRegisteredInCache(ontologyMetricsUrl)) {
                // Combined dispatch for the separate calls for metrics and descriptions.
                // The metric call has much of the info we need
                var ontologyMetricsCallback = new OntologyMetricsCallback(this, ontologyMetricsUrl, node);
                var fetcher = new Fetcher.RetryingJsonFetcher(ontologyMetricsUrl);
                fetcher.fetch(ontologyMetricsCallback);
            }
        };

        OntologyGraph.prototype.fetchNodeDescriptionData = function (node) {
            var ontologyDescriptionUrl = buildOntologyLatestSubmissionUrlNewApi(node.rawAcronym);

            // Explicitly testing the cache for this to overcome existing speed problems.
            if (Fetcher.CacheRegistry.isNotRegisteredInCache(ontologyDescriptionUrl)) {
                // If we want Description, I think we need to grab the most recent submission
                // and take it fromt here. This is another API call per ontology.
                // /ontologies/:acronym:/lastest_submission
                // Descriptions are in the submissions, so we need an additional call.
                var ontologyDescriptionCallback = new OntologyDescriptionCallback(this, ontologyDescriptionUrl, node);
                var fetcher = new Fetcher.RetryingJsonFetcher(ontologyDescriptionUrl);
                fetcher.fetch(ontologyDescriptionCallback);
            }
        };

        // 2nd arg is actually UndoRedoManager.NodeInteraction, but we don't sue undo here yet
        // and I didn't want to increase dependencies until we need to.
        OntologyGraph.prototype.getNumberOfPotentialNodesToExpand = function (incomingNodeId, nodeInteraction) {
            // Unimplemented.
            return -1;
        };

        /**
        * Provide ontology acronyms that should be kept in the graph whiel the rest are removed.
        * Removes both nodes and links.
        * Revise as necessary if a latent-link approach is used later.
        *
        * @param json
        */
        OntologyGraph.prototype.cropGraphToSubset = function (acronymsToKeep) {
            //$.each(acronymsToKeep, function(index, node){console.log("Cropping down to: "+node)});
            // $.each(ontologyNeighbourhoodJsonForGraph.nodes, function(index, node){console.log("Before removal: "+node.rawAcronym)});
            // $.each(ontologyNeighbourhoodJsonForGraph.links, function(index, link){console.log("Before removal: "+link.source.rawAcronym+" and "+link.target.rawAcronym)});
            this.graphD3Format.nodes = $.grep(this.graphD3Format.nodes, function (value) {
                return $.inArray(value.rawAcronym, acronymsToKeep) != -1;
            });

            this.graphD3Format.links = $.grep(this.graphD3Format.links, function (value) {
                return $.inArray(value.source.rawAcronym, acronymsToKeep) != -1 && $.inArray(value.target.rawAcronym, acronymsToKeep) != -1;
            });

            this.sortedAcronymsByMappingCount = $.grep(this.sortedAcronymsByMappingCount, function (entry) {
                return acronymsToKeep.indexOf(entry.acronym) != -1;
            });

            // $.each(ontologyNeighbourhoodJsonForGraph.nodes, function(index, node){console.log("After removal: "+node.rawAcronym)});
            // $.each(ontologyNeighbourhoodJsonForGraph.links, function(index, link){console.log("After removal: "+link.source.rawAcronym+" and "+link.target.rawAcronym)});
            this.graphView.removeMissingGraphElements(this.graphD3Format);
        };

        OntologyGraph.prototype.nextNodeColor = function () {
            this.currentNodeColor = this.currentNodeColor == 19 ? 0 : this.currentNodeColor + 1;
            return this.nodeOrderedColors(this.currentNodeColor);
        };

        OntologyGraph.prototype.brightenColor = function (outerColor) {
            // Outer color will be a 6 digit hex representation. Let's make it darker across all three factors.
            // Using lab() converts from hex RGB to the Cie L*A*B equivalent.
            return d3.lab(outerColor).brighter(1).toString();
        };

        OntologyGraph.prototype.darkenColor = function (outerColor) {
            // Outer color will be a 6 digit hex representation. Let's make it darker across all three factors.
            // Using lab() converts from hex RGB to the Cie L*A*B equivalent.
            return d3.lab(outerColor).darker(1).toString();
        };
        return OntologyGraph;
    })();
    exports.OntologyGraph = OntologyGraph;

    // Doesn't need REST call registry, so if I refactor, keep that in mind.
    var OntologyMappingCallback = (function (_super) {
        __extends(OntologyMappingCallback, _super);
        function OntologyMappingCallback(graph, url, centralOntologyAcronym, expansionSet) {
            var _this = this;
            _super.call(this, url, centralOntologyAcronym, 4 /* fullOntologyMapping */);
            this.graph = graph;
            this.centralOntologyAcronym = centralOntologyAcronym;
            this.expansionSet = expansionSet;
            // Need fat arrow definition rather than regular type, so that we can get lexical scoping of
            // "this" to refer to the class instance (a lamda by use of closure, I think) rather than whatever Javascript binds "this" to
            // when the callback is executed.
            // As a further complication, when we make anonymous functions within this method, our references to "this" get re-scoped
            // again. In order to cope with those, we need to use fat arrow => again!
            // If we are passing such a function to D3, and need "this" to refer to the element that D3 is operating
            // on, then we stay with fucntion() syntax, and make a new variable pointing to the object-instance "this",
            // and allow the function() to closure onto that variable. Did you catch all that?
            // For this case, the caller has no "this" of interest to us, so fat arrow works.
            this.callback = function (mappingData, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                // CORS enabled GET and POST do though!
                if (jqXHR != null) {
                    if (mappingData.errors != null) {
                        // We had an error. Handle it.
                        console.log("Failed to load mappings for: " + _this.centralOntologyAcronym);
                        return;
                    }
                }

                // Sort the arcs and nodes so that we make calls on the ones with highest mappings first
                $.each(mappingData, function (index, element) {
                    // Hard cap on nodes included. Great for dev purposes.
                    _this.graph.sortedAcronymsByMappingCount.push({ acronym: index, node: undefined });
                });
                _this.graph.sortedAcronymsByMappingCount.sort(function (a, b) {
                    return mappingData[b.acronym] - mappingData[a.acronym];
                });

                // Reduce to a useful number of nodes.
                if (hardNodeCap != 0 && _this.graph.sortedAcronymsByMappingCount.length > hardNodeCap) {
                    _this.graph.sortedAcronymsByMappingCount = _this.graph.sortedAcronymsByMappingCount.slice(0, hardNodeCap);
                }

                // Base this off of the possibly-filtered list.
                var numberOfMappedOntologies = _this.graph.sortedAcronymsByMappingCount.length;

                // And base the total off of the original list
                var originalNumberOfMappedOntologies = Object.keys(mappingData).length;

                var defaultNumOfTermsForSize = 10;

                var newNodesForExpansionSet = [];
                var newLinks = [];

                // New API example: http://data.bioontology.org/mappings/statistics/ontologies/SNOMEDCT/?apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1
                // Create the central node
                var centralOntologyNode = new Node();
                _this.graph.centralOntologyNode = centralOntologyNode;
                centralOntologyNode.name = "fetching" + " (" + _this.centralOntologyAcronym + ")";
                centralOntologyNode.description = "fetching description";
                centralOntologyNode.fixed = true; // lock central node
                centralOntologyNode.x = _this.graph.graphView.visWidth() / 2;
                centralOntologyNode.y = _this.graph.graphView.visHeight() / 2;
                centralOntologyNode.weight = numberOfMappedOntologies; // will increment as we loop
                centralOntologyNode.number = defaultNumOfTermsForSize; // number of terms
                centralOntologyNode.acronymForIds = Utils.escapeIdentifierForId(_this.centralOntologyAcronym);
                centralOntologyNode.rawAcronym = _this.centralOntologyAcronym;
                centralOntologyNode.nodeColor = _this.graph.nextNodeColor();
                centralOntologyNode.innerNodeColor = _this.graph.brightenColor(centralOntologyNode.nodeColor);
                centralOntologyNode.nodeStrokeColor = _this.graph.darkenColor(centralOntologyNode.nodeColor);
                centralOntologyNode.mapped_classes_to_central_node = 0;
                newNodesForExpansionSet.push(centralOntologyNode);

                // Lame loop to find the central node in our sorted set
                $.each(_this.graph.sortedAcronymsByMappingCount, function (index, sortedAcronym) {
                    if (sortedAcronym.acronym == centralOntologyNode.rawAcronym) {
                        sortedAcronym.node = centralOntologyNode;
                    }
                });

                var ontologyAcronymNodeMap = {};

                //		$(ontologyAcronymNodeMap).attr("vid:"+centralOntologyNode.rawAcronym, centralOntologyNode);
                ontologyAcronymNodeMap["vid:" + centralOntologyNode.rawAcronym] = centralOntologyNode;

                // TODO XXX Either the parsing or the looping here causes a visible glitch in rendering,
                // so this is the first place to try a web worker out.
                // Make some graph parts!
                // Original bug hidden by force layout, but I needed radians not degrees.
                // It looks very slightly different.
                var anglePerNode = 2 * Math.PI / numberOfMappedOntologies;
                var arcLength = _this.graph.graphView.linkMaxDesiredLength();
                var i = 0;

                // Used to iterate over raw mappingData, but I wanted things loaded and API calls made in order
                // of mapping counts.
                $.each(_this.graph.sortedAcronymsByMappingCount, function (index, sortedAcronym) {
                    var acronym = sortedAcronym.acronym;
                    var mappingCount = mappingData[acronym];

                    if (typeof acronym === "undefined") {
                        console.log("Undefined ontology entry");
                    }

                    // Create the neighbouring nodes
                    var ontologyNode = new Node();
                    ontologyNode.name = "fetching" + " (" + acronym + ")";
                    ontologyNode.description = "fetching description";
                    ontologyNode.weight = 1;
                    ontologyNode.fixed = false; // lock central node

                    // Compute starting positions to be in a circle for faster layout
                    var angleForNode = i * anglePerNode;
                    i++;
                    ontologyNode.x = _this.graph.graphView.visWidth() / 2 + arcLength * Math.cos(angleForNode); // start in middle and let them fly outward
                    ontologyNode.y = _this.graph.graphView.visHeight() / 2 + arcLength * Math.sin(angleForNode); // start in middle and let them fly outward
                    ontologyNode.number = defaultNumOfTermsForSize; // number of terms
                    ontologyNode.acronymForIds = Utils.escapeIdentifierForId(acronym);
                    ontologyNode.rawAcronym = acronym;
                    ontologyNode.nodeColor = _this.graph.nextNodeColor();
                    ontologyNode.innerNodeColor = _this.graph.brightenColor(ontologyNode.nodeColor);
                    ontologyNode.nodeStrokeColor = _this.graph.darkenColor(ontologyNode.nodeColor);
                    ontologyNode.mapped_classes_to_central_node = 0;
                    newNodesForExpansionSet.push(ontologyNode);

                    // TODO I feel like JS doesn't allow references like this...
                    //				$(ontologyAcronymNodeMap).attr("vid:"+ontologyNode.rawAcronym, ontologyNode);
                    ontologyAcronymNodeMap["vid:" + ontologyNode.rawAcronym] = ontologyNode;
                    sortedAcronym.node = ontologyNode;

                    // Make the links at the same time; they are done now!
                    var ontologyLink = new Link();
                    ontologyLink.source = centralOntologyNode;
                    ontologyLink.target = ontologyNode;
                    ontologyLink.value = mappingCount; // This gets used for link stroke thickness later.
                    ontologyLink.numMappings = mappingCount;
                    newLinks.push(ontologyLink);

                    // Get the node the data it needs from the link
                    ontologyNode.mapped_classes_to_central_node = ontologyLink.value;
                });

                // Make calls on all nodes we want to show when the graph first loads up
                // Well, we could, but there are lots of ontologies that do not have metric or details accessible to us,
                // and we don't know these until later. If we do this now, it disrupts the visualization.
                //		$.each(sortedAcronymsByMappingCount, function(index, rawAcronym){
                //			// fetch the node, make the individual calls
                //			var node = $(ontologyAcronymNodeMap).attr("vid:"+rawAcronym);
                //			node.fetchNodeRestData();
                //		})
                // Not sure about whether to do this here or not...
                // console.log("ontologyMappingCallback");
                _this.graph.addNodes(newNodesForExpansionSet, _this.expansionSet);
                _this.graph.addEdges(newLinks);

                // Once we have the graph populated, we have this one node we know we can call REST calls for, the central node!
                // The other nodes need to wait, since when we get the details call back later, we will see that many
                // of them are inaccessible. Making calls for those ones is a waste of time.
                // The easiest way to benchmark is to do *all* nodes right above in the loop
                // Get to "Processing details" log entry in 45 seconds when all are allowed right away, versus 1 second when
                // we only get the first one, and let the filter trigger the rest. Labels and node sizes are subsequently
                // quicker to appear as well.
                _this.graph.fetchNodeRestData(centralOntologyNode);

                //----------------------------------------------------------
                // 2) Get details for all the ontologies (and either create or update the nodes)
                var ontologyDetailsUrl = buildOntologyDetailsUrlNewApi();
                var ontologyDetailsCallback = new OntologyDetailsCallback(_this.graph, ontologyDetailsUrl, ontologyAcronymNodeMap);

                //		var fetcher = new RetryingJsonpFetcher(ontologyDetailsCallback);
                //		fetcher.retryFetch();
                var fetcher = new Fetcher.RetryingJsonFetcher(ontologyDetailsUrl);
                fetcher.fetch(ontologyDetailsCallback);
            };
        }
        return OntologyMappingCallback;
    })(Fetcher.CallbackObject);

    //Doesn't need REST call registry, so if I refactor, keep that in mind.
    /**
    * The primary, possibly sole purpose, of this call is to remove all the
    * ontology nodes that do not have data available, and would receive 403 errors
    * if we tried to retieve it. Those without mappings should already not be added to the graph.
    */
    var OntologyDetailsCallback = (function (_super) {
        __extends(OntologyDetailsCallback, _super);
        function OntologyDetailsCallback(graph, url, ontologyAcronymNodeMap) {
            var _this = this;
            _super.call(this, url, "", 3 /* metaData */); // only gets called once normally
            this.graph = graph;
            this.ontologyAcronymNodeMap = ontologyAcronymNodeMap;
            // Caller of callback has no "this" of interest, so fat arrow works
            this.callback = function (detailsDataRaw, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                // CORS enabled GET and POST do though!
                if (jqXHR != null) {
                    if (detailsDataRaw.errors != null) {
                        // We had an error. Handle it.
                        //                remove this from the dataset. It's no good!
                        return;
                    }
                }

                console.log("Processing details " + Utils.getTime());

                // Loop over ontologies and add their additional properties to the nodes
                // Recall that getting *all* ontology details is the easiest (only) way,
                // so we have to skip anything that is not defined.
                // We can get name and URI from the individual calls, as seen in OntologyDescriptionCallback
                var ontologiesSkipped = 0;
                var acronymsNotSkipped = [];
                $.each(detailsDataRaw, function (index, ontologyDetails) {
                    // I can't cherry pick, because this involves iterating
                    // through the entire set of ontologies to find each ontology entry.
                    // So, I will do a separate loop, and only use data for which there
                    // exists in the graph a corresponding ontology.
                    // Make use of details to add info to ontologies
                    var ontologyAcronym = ontologyDetails.acronym;
                    var node = _this.ontologyAcronymNodeMap["vid:" + ontologyAcronym];

                    if (typeof node === "undefined") {
                        // Skip node details that aren't in our graph
                        // or those that will give 403 errors if we
                        // try to fetch their details.
                        ontologiesSkipped += 1;
                    } else {
                        acronymsNotSkipped.push(ontologyAcronym);
                        // We used to grab ontology names and URIs here, but it isn't required, and doesn't
                        // help to have those prior to having the ontology sizes anyway.
                        // I removed it, but it was veyr simply grabbing the name and @id from the ontologyDetails.
                        //if(node.name !== undefined && (-1 === node.name.indexOf("fetching")) && node.name !== ontologyDetails.name){
                        //    console.log("No name match: "+node.name+" vs "+ontologyDetails.name);
                        //    node.name = ontologyDetails.name;
                        //    node.LABEL = ontologyDetails.name;
                        //}
                        //if(node.uriId !== undefined && node.uriId !== ontologyDetails["@id"]){
                        //    console.log("No uri match: "+node.uriId+" vs "+ontologyDetails["@id"]);
                        //    node.uriId = ontologyDetails["@id"]; // Use the URI instead of virtual id
                        //}
                    }
                });

                console.log("Cropping " + Utils.getTime());

                // We have to remove all nodes and edges which did not appear in details.
                _this.graph.cropGraphToSubset(acronymsNotSkipped);

                _this.graph.graphView.filterGraphOnMappingCounts();

                // We usually use very many of the ontologies, so it is likely cheaper to make the one
                // big call with no ontology acronym arguments than to cherry pick the ones we want details for.
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
            // Caller of callback has no "this" of interest, so fat arrow works
            this.callback = function (metricDataRaw, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                // CORS enabled GET and POST do though!
                if (jqXHR != null) {
                    if (metricDataRaw.errors != null) {
                        // We had an error. Handle it.
                        //                remove this from the dataset, it's no good!
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

                // self.node.weight = 1; // Changing weight re-triggers layout shifting, turns into a swirling mess.
                _this.node.numberOfClasses = numClasses;
                _this.node.numberOfIndividuals = numIndividuals;
                _this.node.numberOfProperties = numProperties;
                _this.node.number = nodeSizeBasis;

                // console.log("ontologyMetricsCallback");
                _this.graph.graphView.updateDataForNodesAndLinks({ nodes: [_this.node], links: [] });
                // filterGraph();
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
            // Caller of callback has no "this" of interest, so fat arrow works
            this.callback = function (latestSubmissionData, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                // CORS enabled GET and POST do though!
                if (jqXHR != null) {
                    if (latestSubmissionData.errors != null) {
                        // We had an error. Handle it.
                        //                remove this from the dataset, it's no good!
                        return;
                    }
                }

                var description = "";
                if (typeof latestSubmissionData !== "undefined") {
                    if (latestSubmissionData.description != null) {
                        description = latestSubmissionData.description;
                    } else if (typeof latestSubmissionData.error != null) {
                        description = latestSubmissionData.error;
                    }
                }

                _this.node.description = description;
                if (null != latestSubmissionData.ontology && null != latestSubmissionData.ontology.name) {
                    _this.node.name = latestSubmissionData.ontology.name + "(" + _this.node.rawAcronym + ")";
                    _this.node.LABEL = latestSubmissionData.ontology.name;

                    // This one is for the submission, don't use it: latestSubmissionData["@id"]
                    _this.node.uriId = latestSubmissionData.links.ontology;
                }

                // console.log("ontologyDescriptionCallback");
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
