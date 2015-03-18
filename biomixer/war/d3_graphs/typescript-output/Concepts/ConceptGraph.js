///<reference path="headers/require.d.ts" />
///<reference path="headers/simplemodal.d.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../Utils", "../FetchFromApi", "../GraphView", "../ExpansionSets", "./ExpansionManager", "../TipsyToolTipsOnClick", "./PropertyRelationsExpander", "Utils", "MouseSpinner", "FetchFromApi", "GraphView", "LayoutProvider", "ExpansionSets", "Concepts/ExpansionManager", "UndoRedo/UndoRedoManager", "TipsyToolTipsOnClick", "CompositeExpansionDeletionSet", "Concepts/PropertyRelationsExpander"], function(require, exports, Utils, Fetcher, GraphView, ExpansionSets, ExpansionManager, TipsyToolTipsOnClick, PropRel) {
    var PathOptionConstants = (function () {
        function PathOptionConstants() {
        }
        PathOptionConstants.termNeighborhoodConstant = "Term Neighborhood";
        PathOptionConstants.pathsToRootConstant = "Path to Root";
        PathOptionConstants.mappingsNeighborhoodConstant = "Mappings Neighborhood";
        PathOptionConstants.singleNodeConstant = "Single Node";
        PathOptionConstants.singleNodeOrSubordinateConstant = "Single Node or Subordinate of Another Call";
        return PathOptionConstants;
    })();
    exports.PathOptionConstants = PathOptionConstants;

    var Node = (function (_super) {
        __extends(Node, _super);
        function Node() {
            _super.call(this);
        }
        // nodeColor: string; // nextNodeColor(conceptNode.ontologyAcronym);
        //    uriId: string; // = ontologyDetails["@id"]; // Use the URI instead of virtual id
        //    LABEL: string; // = ontologyDetails.name;
        Node.prototype.getEntityId = function () {
            return String(this.nodeId);
        };

        /**
        * Use with the D3 data() method for binding link models into D3.
        */
        Node.d3IdentityFunc = function (d) {
            return String(d.nodeId);
        };
        return Node;
    })(GraphView.BaseNode);
    exports.Node = Node;

    var Link = (function (_super) {
        __extends(Link, _super);
        function Link() {
            _super.call(this);
            this.source = null;
            this.target = null;
            this.value = 1;
        }
        /**
        * Use with the D3 data() method for binding link models into D3.
        */
        Link.d3IdentityFunc = function (d) {
            return d.rawId;
        };
        return Link;
    })(GraphView.BaseLink);
    exports.Link = Link;

    

    var ConceptD3Data = (function (_super) {
        __extends(ConceptD3Data, _super);
        function ConceptD3Data() {
            _super.apply(this, arguments);
        }
        return ConceptD3Data;
    })(GraphView.GraphDataForD3);
    exports.ConceptD3Data = ConceptD3Data;

    var DeferredCallbacks = (function () {
        function DeferredCallbacks(graph) {
            this.graph = graph;
            this.wrappedParseNodeCallbacks = [];
        }
        DeferredCallbacks.prototype.addCallback = function (callback, expansionSet) {
            var expSetUpdateWrapper = function (haltExpansions, maxNodesToGet) {
                // Have to check if we are stopping, mark the related expansion set if so,
                // or fetch the node otherwise.
                if (haltExpansions || expansionSet.expansionCutShort()) {
                    // Why do we check if the expansion set is already cut short??? That seems silly.
                    // We check if the expansion set is cut short in case we want other effects that setting it as halted.
                    // Setting expansionCutShort() prevents nodes later in the associated expansion set
                    // from being loaded when they are past the cutoff.
                    expansionSet.expansionCutShort(haltExpansions);
                    return;
                } else {
                    expansionSet.thunderbirdsAreGo();
                    callback(maxNodesToGet);
                }
            };
            this.wrappedParseNodeCallbacks.push(expSetUpdateWrapper);

            // I originally wanted to use the length of the wrapped callbacks,
            // but that is re-produced every time the user triggers an expansion.
            // Instead I need to use the expansion set's counts, because callbacks can
            // be entered into here that already correspond to a loaded node, and will
            // elegantly resolve later.
            // Even though these counts don't match, and that the wrapped callbacks will generally be
            // bigger, it's ok; the graph system is set up to not redundantly load nodes. I cannot easily
            // handle that from here, so I can't intelligently prevent wrapped callbacks from being called
            // when they will be dead ends, except from the caller. This is my safety in case
            // that is not done by the caller.
            this.graph.refreshNodeCapDialogNodeCount(expansionSet.getNumberOfNodesMissing());
        };

        DeferredCallbacks.prototype.complete = function (haltExpansions, maxNodesToGet) {
            var i = 0;
            for (i = 0; i < this.wrappedParseNodeCallbacks.length; i++) {
                if (i === maxNodesToGet && !haltExpansions) {
                    break;
                }

                // This is vital to communicate to the expansion set for some
                // later nodes coming in, to prevent duplicate dialogs and accidental
                // node loading.
                // We also tell each expansion how many nodes it was allowed to load
                // via this process, although it will need to account for any nodes loaded
                // prior to the node cap dialog being presented.
                this.wrappedParseNodeCallbacks[i](haltExpansions, maxNodesToGet);
            }

            // Cut out whatever we processed. Leave any we didn't (due to max nodes argument).
            this.wrappedParseNodeCallbacks = this.wrappedParseNodeCallbacks.slice(i);
            this.wrappedParseNodeCallbacks = [];
        };
        return DeferredCallbacks;
    })();

    var ConceptGraph = (function () {
        function ConceptGraph(graphView, centralConceptUri, softNodeCap, undoBoss) {
            this.graphView = graphView;
            this.centralConceptUri = centralConceptUri;
            this.softNodeCap = softNodeCap;
            this.undoBoss = undoBoss;
            this.graphD3Format = new ConceptD3Data();
            // To track nodes that we have in the graph (by id):
            this.conceptIdNodeMap = {};
            this.elementIdNodeMap = {};
            this.nodeMapChanged = false;
            this.ontologiesInGraph = new Array();
            /**
            * Used for stylings, not selections
            */
            this.relationTypeCssClasses = {
                "is_a": "inheritanceStyleLink",
                "part_of": "compositionStyleLink",
                "maps_to": "mappingStyleLink"
            };
            this.relationLabelConstants = {
                "inheritance": "is_a",
                "composition": "part_of",
                "mapping": "maps_to"
            };
            /**
            * When we have too many nodes in the graph, we warn the user about it every time we have added an
            * additional nodeCapInterval nodes.
            */
            this.nodeCapInterval = 20;
            this.nextNodeWarningCount = undefined;
            this.deferredParseNodeCallBack = undefined;
            this.times = 0;
            /**
            * Used in the refresh and modal trigger methods, to determine if the expansion has been allowed to fully expand.
            */
            this.currentModalDialogIncomingNodeCount = 0;
            // Graph is responsible for its own node coloration...debate what this is: model attribute or view render?
            // In D3, the data model gets mingled with the view in this kind of way, so I feel this is ok.
            this.currentNodeColor = -1;
            this.nodeOrderedColors = d3.scale.category20().domain([0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]);
            this.ontologyColorMap = {};
            // Passing undo boss makes the msot sense, since expansions are very graphy,
            // so the graph can own the expansion manager and merely use the undo system.
            this.expMan = new ExpansionManager.ExpansionManager(undoBoss);
        }
        ConceptGraph.prototype.addNodeToIdMap = function (conceptNode) {
            this.nodeMapChanged = true;
            this.conceptIdNodeMap[String(conceptNode.nodeId)] = conceptNode;
            this.elementIdNodeMap[String(conceptNode.conceptUriForIds)] = conceptNode;
        };

        ConceptGraph.prototype.removeNodeFromIdMap = function (conceptNode) {
            this.nodeMapChanged = true;
            delete this.conceptIdNodeMap[String(conceptNode.nodeId)];
            delete this.elementIdNodeMap[String(conceptNode.conceptUriForIds)];
        };

        /**
        * Perfect for testing to see if nodes are deleted, when other entities hold on
        * to them for a good reason (expansion sets).
        */
        ConceptGraph.prototype.nodeIsInIdMap = function (node) {
            return undefined !== this.getNodeByUri(node.nodeId);
        };

        ConceptGraph.prototype.getNodeByUri = function (uri) {
            return this.conceptIdNodeMap[String(uri)];
        };

        /**
        * Accepts strings because this will be used when we have an SVG elment and need
        * the node model that corresponds; thus it comes off the element as a string.
        */
        ConceptGraph.prototype.getNodeByIdUri = function (idSafeUri) {
            return this.elementIdNodeMap[String(idSafeUri)];
        };

        ConceptGraph.prototype.getOntologiesInGraph = function () {
            if (!this.nodeMapChanged) {
                return this.ontologiesInGraph;
            }
            this.nodeMapChanged = false;
            var ontologies = {};
            this.ontologiesInGraph = new Array();
            for (var i = 0; i < this.graphD3Format.nodes.length; i++) {
                var nodeData = this.graphD3Format.nodes[i];
                if (ontologies[String(nodeData.ontologyAcronym)] === undefined) {
                    this.ontologiesInGraph.push(nodeData.ontologyAcronym);
                }
                ontologies[String(nodeData.ontologyAcronym)] = nodeData.ontologyAcronym;
            }
            return this.ontologiesInGraph;
        };

        ConceptGraph.prototype.convertEdgeTypeLabelToEdgeClass = function () {
        };

        ConceptGraph.prototype.getLayoutProvider = function () {
            return this.layoutProvider;
        };

        ConceptGraph.prototype.setLayoutProvider = function (layoutProvider) {
            this.layoutProvider = layoutProvider;
        };

        /**
        * Used with expansion sets. Note that the node is already created here.
        */
        ConceptGraph.prototype.addNodes = function (newNodes, expansionSet) {
            // Deletion sets lead back into addNodes() when done, but both they and expansion
            // sets are ignorant of eachother. Allow null to be passed here.
            if (null !== expansionSet) {
                expansionSet.addAll(newNodes);
            }
            for (var i = 0; i < newNodes.length; i++) {
                // Only implementing here rather than in graphView because of this container...
                this.graphD3Format.nodes.push(newNodes[i]);

                // Also, we like looking them up by id
                this.addNodeToIdMap(newNodes[i]);
            }

            this.graphView.stampTimeGraphModified();

            this.graphView.populateNewGraphElements(this.graphD3Format);
            for (var i = 0; i < newNodes.length; i++) {
                // If there are implicit edges from before that link from an existing node to this new one,
                // we can now manifest them.
                this.manifestEdgesForNewNode(newNodes[i]);
            }

            // Special case...
            // Trying to get mapping expansions to have their mapping edges added when redoing
            // an undo. Without this, the mapping arcs don't get processed for the target nodes
            // as they run through the manifestEdgesForNewNode() because they are registered on
            // the source node only.
            if (null != expansionSet && null != expansionSet.parentNode) {
                this.manifestEdgesForNewNode(expansionSet.parentNode);
            }
        };

        ConceptGraph.prototype.removeNodes = function (nodesToRemove) {
            TipsyToolTipsOnClick.closeOtherTipsyTooltips();

            this.graphD3Format.nodes = this.graphD3Format.nodes.filter(function (node, index, nodes) {
                // Keep only those that do not appear in the removal array
                return nodesToRemove.indexOf(node) === -1;
            });
            for (var i = 0; i < nodesToRemove.length; i++) {
                // Only implementing here rather than in graphView because of this container...
                // Also, we like looking them up by id
                this.removeNodeFromIdMap(nodesToRemove[i]);
            }

            this.graphView.stampTimeGraphModified();

            // Edges depend on nodes when rendering, but not vice versa, so let's
            // remove them first
            this.removeManifestEdges(nodesToRemove);
            this.graphView.removeMissingGraphElements(this.graphD3Format);
        };

        ConceptGraph.prototype.containsNode = function (node) {
            return this.conceptIdNodeMap[String(node.nodeId)] !== undefined;
        };

        ConceptGraph.prototype.containsNodeById = function (nodeId) {
            return this.conceptIdNodeMap[String(nodeId)] !== undefined;
        };

        ConceptGraph.prototype.findNodesByName = function (substringRaw) {
            var substringLower = substringRaw.toLowerCase();
            var matchNodes = this.graphD3Format.nodes.filter(function (node, index, nodes) {
                // Keep only those that do not appear in the removal array
                var keep = node.name.toLowerCase().search(substringLower) > -1;
                $.each(node.synonym, function (i, entry) {
                    keep = keep || entry.search(substringLower) > -1;
                });

                return keep;
            });
            return matchNodes;
        };

        ConceptGraph.prototype.addEdges = function (newEdges, temporaryEdges) {
            if (newEdges.length == 0) {
                // Saves a lot of work deeper down.
                return;
            }
            for (var i = 0; i < newEdges.length; i++) {
                // Only implementing here rather than in graphView because of this container...
                if (this.edgeNotInGraph(newEdges[i])) {
                    newEdges[i].source = this.conceptIdNodeMap[String(newEdges[i].sourceId)];
                    newEdges[i].target = this.conceptIdNodeMap[String(newEdges[i].targetId)];
                    this.graphD3Format.links.push(newEdges[i]);
                }
            }
            this.graphView.populateNewGraphEdges(this.graphD3Format.links, temporaryEdges);
        };

        /**
        * See removeManifestEdges for model book keeping that must be done
        * prior to removing edges from the view.
        * Gets called when removing temporary mapping edges (those edges that only render on node hover).
        */
        ConceptGraph.prototype.removeEdges = function (edgesToRemove) {
            this.graphD3Format.links = this.graphD3Format.links.filter(function (link, index, links) {
                // Keep only those that do not appear in the removal array
                return edgesToRemove.indexOf(link) === -1;
            });

            this.graphView.stampTimeGraphModified();

            this.graphView.removeMissingGraphElements(this.graphD3Format);

            for (var l = 0; l < edgesToRemove.length; l++) {
                // Was doing this earlier, but D3 cries if I do it before re-binding,
                // I think because I use the source and target as parts of the
                // identifier function.
                edgesToRemove[l].source = null;
                edgesToRemove[l].target = null;
            }
        };

        ConceptGraph.prototype.removeManifestEdges = function (nodesToRemove) {
            var edgesToDelete = [];
            for (var i = 0; i < nodesToRemove.length; i++) {
                // For each node we are removing, we de-manifest its edges, and re-register it into the
                // registry so it can be manifested again later. Annoying, but the register system was
                // extremely vital to prevent wasteful REST calls.
                var node = nodesToRemove[i];
                var incidentEdges = this.getAdjacentLinks(node);
                for (var l = 0; l < incidentEdges.length; l++) {
                    var edge = incidentEdges[l];
                    edgesToDelete.push(edge);
                }
            }

            this.graphView.stampTimeGraphModified();

            this.removeEdges(edgesToDelete);
        };

        ConceptGraph.prototype.getOntologyAcronymFromOntologyUrl = function (ontologyUri) {
            var urlBeforeAcronym = "ontologies/";
            var urlAfterAcronym = "/";
            return ontologyUri.substring(ontologyUri.lastIndexOf(urlBeforeAcronym) + urlBeforeAcronym.length);
        };

        /**
        * Counts the number of unadded nodes for a given expansion type (currently mapping or term neighbourhood expansions,
        * not any others). This allows us ot estimate the number of nodes that would be added if the expansion were performed.
        * Gives the current outstanding number, not the total.
        */
        ConceptGraph.prototype.getNumberOfPotentialNodesToExpand = function (expandingNode, nodeInteraction) {
            var _this = this;
            var expandingNodeId = expandingNode.nodeId;
            var numNewNodesIncoming = 0;
            var otherCount = 0;
            var edges = this.expMan.edgeRegistry.getEdgesFor(expandingNodeId);
            var nodesSeen = {};
            edges.forEach(function (edge) {
                // Currently, the only mapping edges are "maps_to", and all others count as term neighbourhood types.
                var edgeExpansionType = edge.relationType === "maps_to" ? PathOptionConstants.mappingsNeighborhoodConstant : PathOptionConstants.termNeighborhoodConstant;

                var otherConceptId = (edge.sourceId === expandingNodeId) ? edge.targetId : edge.sourceId;

                if (_this.nodeMayBeExpanded1(otherConceptId, expandingNodeId, nodeInteraction, edgeExpansionType) && null == nodesSeen[String(otherConceptId)]) {
                    numNewNodesIncoming++;
                    nodesSeen[String(otherConceptId)] = true;
                }
            });

            return numNewNodesIncoming;
        };

        // When we are about to do a fetch that will result in a node expansion, we need to
        // see if the user thinks we already have too many nodes.
        // This re-wrapping of callbacks is necessary because we can only do the user check prior
        // to asynchronous events, because Javascript has no capacity for stalling execution.
        // If it did, some other options would be available to us.
        // NB We could have this inside the fetching utility or inside the node related callbacks, except
        // that the former would complicate non-node fetches, and the latter would necessitate doing the
        // fetch and calling the callback, costing us the fetch but still saving on node load.
        ConceptGraph.prototype.checkForNodeCap = function (fetchCallback, expansionSet, numberNewNodesComing) {
            // Assuming we reliably check for capping prior to dispatching node fetches, we can
            // know how many nodes are incoming for a given expansion set by incrementing it here.
            if (0 === numberNewNodesComing) {
                // Not actually adding anything, skip dialog check. Better for caller to check, isn't it?
                return;
            }

            if (expansionSet.expansionCutShort()) {
                // If we are rejecting node parse callbacks for this expansion set, then let the callback that was passed in
                // simply fade away into the everlasting garbage collector.
                // NB The expansion set will not be reused even if the same expansion is retriggered.
                return;
            }

            if (undefined === this.nextNodeWarningCount) {
                this.nextNodeWarningCount = this.softNodeCap;
            }

            while (this.graphD3Format.nodes.length < (this.nextNodeWarningCount - this.nodeCapInterval) && this.softNodeCap < this.nextNodeWarningCount) {
                // We have to account for shrinking graphs. Just because the user said ok to lots of nodes previously
                // doesn't mean they will say ok again.
                // Decrement our current cap until we are closer to the existing number of nodes.
                this.nextNodeWarningCount -= this.nodeCapInterval;
                this.nextNodeWarningCount = Math.max(this.nextNodeWarningCount, this.softNodeCap);
            }

            var dialogOpen = $("#confirm").is(":visible");
            if (dialogOpen || ((this.graphD3Format.nodes.length + 1) > this.nextNodeWarningCount) || ((this.graphD3Format.nodes.length + numberNewNodesComing) > this.nextNodeWarningCount)) {
                // So, this logic lets all possible mappings through, because mappings calls are registered in a quick loop
                // and the fetches get called before any node is processed from any of those fetches.
                if (undefined === this.deferredParseNodeCallBack) {
                    this.deferredParseNodeCallBack = new DeferredCallbacks(this);
                }

                // If we are currently awaiting user input to the node cap dialog, then we stick the callback right into
                // the queue. It will be dealt with one way or another once the user has responded.
                // NB This means that each dialog will be responding for *any* expansion sets being worked with while it is
                // open. Will it be problematic if two come near each other??
                this.deferredParseNodeCallBack.addCallback(fetchCallback, expansionSet);

                this.showNodeCapDialog(numberNewNodesComing);
            } else if (!dialogOpen) {
                // We're below the cap and the expansion hasn't been previously cut short
                // so we will execute the fetch.
                // This also gets called on the expansion set internal to deferredParseNodeCallback above.
                expansionSet.thunderbirdsAreGo();
                fetchCallback(numberNewNodesComing);
            }
        };

        ConceptGraph.prototype.nodeCapResponseCallback = function (haltExpansion, nodesToAdd) {
            if (!haltExpansion) {
                this.nextNodeWarningCount += nodesToAdd;
            }
            this.deferredParseNodeCallBack.complete(haltExpansion, nodesToAdd);
        };

        ConceptGraph.prototype.refreshNodeCapDialogNodeCount = function (numberNewNodesComing) {
            $("#nodeCapDialogMessage").empty().append($("<span>").text("You are about to add multiple nodes to the visualization." + "\n" + "In total, ")).append($("<b>").text(this.graphD3Format.nodes.length)).append($("<span>").text(" nodes are in the graph, but ")).append($("<b>").text(numberNewNodesComing)).append($("<span>").text(" more may be added." + "\n\n" + "Would you like to limit the number of nodes? If you change your mind, you can re-expand the concepts/mappings later."));

            $("label#capDialogLabel").text("Load up to: ");
            if (undefined === numberNewNodesComing) {
                $("input#capDialogInput").attr("max", 20 + "");
            } else {
                $("input#capDialogInput").attr("max", numberNewNodesComing + "");
                this.currentModalDialogIncomingNodeCount = numberNewNodesComing;
            }
        };

        ConceptGraph.prototype.showNodeCapDialog = function (numberNewNodesComing) {
            this.currentModalDialogIncomingNodeCount = 0;
            var outerThis = this;
            $('#confirm').modal({
                closeHTML: "<a href='#' title='Close' class='modal-close'>x</a>",
                position: ["20%"],
                overlayId: 'confirm-overlay',
                containerId: 'confirm-container',
                onShow: function (dialog) {
                    var modal = this;

                    var message = $("<div>").attr("id", "nodeCapDialogMessage").css("white-space", "pre-wrap");

                    // Tired of fighting with CSS
                    $("#confirm-container").css("height", "auto");
                    $('.message', dialog.data[0]).append(message);
                    $("div.buttons").css("width", "auto");

                    var nodesToLoadInput = $("<input>").attr("id", "capDialogInput").attr("name", "capDialogInput").attr({ "type": "number", "min": "0", "max": numberNewNodesComing + "" }).css("width", "4em").change(function () {
                        $(".yes").text("Add " + $("#capDialogInput").val());
                    }).keyup(function () {
                        $(".yes").text("Add " + $("#capDialogInput").val());
                    }).val(outerThis.nodeCapInterval + "");
                    var nodesToLoadLabel = $("<label>").attr("id", "capDialogLabel").attr("for", "capDialogInput");
                    outerThis.refreshNodeCapDialogNodeCount(numberNewNodesComing);

                    // if the user clicks "yes"
                    $('.yes', dialog.data[0]).click(function () {
                        // close the dialog
                        modal.close(); // or $.modal.close();

                        // call the callback
                        // If we allow anything less than all of the nodes in, the expansion has to be treated as halted.
                        var nodesToAdd = parseInt(nodesToLoadInput.val(), 10);

                        // Do not want to *halt* the expansion if it is all the nodes available...
                        // var haltExpansion: boolean = nodesToAdd === outerThis.currentModalDialogIncomingNodeCount;
                        // TODO What about the fact that there are still incoming counts arriving when the user
                        // has the opportunity to select how many to add?
                        outerThis.nodeCapResponseCallback(false, nodesToAdd);
                    });

                    $('.no', dialog.data[0]).click(function () {
                        // close the dialog
                        modal.close(); // or $.modal.close();

                        // call the callback
                        outerThis.nodeCapResponseCallback(true, 0);
                    });

                    $(".yes").before(nodesToLoadLabel);
                    nodesToLoadLabel.after(nodesToLoadInput);

                    $(".yes").text("Add " + outerThis.nodeCapInterval);
                    $(".no").text("Stop");
                    $("div.buttons").css("padding", "0px 5px 5px 0px");

                    // http://jqueryui.com/draggable/#handle
                    $($("#confirm-container")).draggable({ handle: $("#simplemodal-GrabHandle") });
                }
            });
        };

        ConceptGraph.computeNodeId = function (conceptData, ontologyAcronym) {
            if (null == ontologyAcronym) {
                ontologyAcronym = this.computeOntologyAcronym(conceptData);
            }
            if (null != conceptData["@id"]) {
                conceptData = conceptData["@id"];
            }
            return (Utils.escapeIdentifierForId(conceptData) + ConceptGraph.CONCEPT_URI_SEPARATOR + ontologyAcronym);
        };

        ConceptGraph.prototype.computeNodeId = function (conceptData, ontologyAcronym) {
            return ConceptGraph.computeNodeId(conceptData, ontologyAcronym);
        };

        ConceptGraph.computeOntologyAcronym = function (conceptData) {
            var ontologyUri;
            if (undefined != conceptData.links) {
                ontologyUri = conceptData.links.ontology;
            } else {
                ontologyUri = conceptData;
            }

            // "http://data.bioontology.org/ontologies/<acronym>"
            var urlBeforeAcronym = "ontologies/";
            return ontologyUri.substring(ontologyUri.lastIndexOf(urlBeforeAcronym) + urlBeforeAcronym.length);
        };

        ConceptGraph.prototype.computeOntologyAcronym = function (conceptData) {
            return ConceptGraph.computeOntologyAcronym(conceptData);
        };

        ConceptGraph.prototype.parseNode = function (index, conceptData, expansionSet) {
            var ontologyAcronym = this.computeOntologyAcronym(conceptData);
            var nodeId = this.computeNodeId(conceptData);

            if (this.conceptIdNodeMap[String(nodeId)] !== undefined) {
                // Race conditions involving REST latency can lead to multiple
                // attempts to create the same node, particularly when composition
                // and inheritance overlap (regardless if that is desireable in ontologies).
                return this.conceptIdNodeMap[String(nodeId)];
            }

            // Some node ids are not referenced within Bioportal. That is, if the "self" URL is followed,
            // there will be a 404 error. I can detect this sooner than that by checking to see if they
            // have no prefLabel supplied.
            // I know understand these ids to be (possibly) blank nodes. Read about blank nodes and Skolemization.
            // It's dirty business, but I think we should render these nodes, since they provide structural information.
            // I am not sure about the best way to label them, but I feel that "<no prefLabel>" is accurate enough and could
            // elicit users to click the node to see if it has any description, etc.
            var prefLabel = conceptData.prefLabel;
            if (prefLabel == null) {
                console.log("Missing prefLabel for concept: id=" + conceptData["@id"] + " (link = " + conceptData.links.self + ")");

                // return null;
                prefLabel = "<no prefLabel>";
            }

            // Create the concept nodes that exist on the paths-to-root for the central concept,
            // including the central concept node.
            var conceptNode = new Node();
            conceptNode.nodeId = nodeId;
            conceptNode.conceptUriForIds = Utils.escapeIdentifierForId(String(nodeId));
            conceptNode.simpleConceptUri = conceptData["@id"];
            conceptNode.name = prefLabel;
            conceptNode.type = conceptData["@type"];
            conceptNode.description = "fetching description";
            conceptNode.definition = conceptData["definition"];
            conceptNode.synonym = (null == conceptData["synonym"]) ? [] : conceptData["synonym"];
            conceptNode.weight = 1;

            //            conceptNode.depth = 0;
            conceptNode.tempDepth = 0;
            conceptNode.fixed = false;

            // conceptNode.x = this.graphView.visWidth()/2; // start in middle and let them fly outward
            // conceptNode.y = this.graphView.visHeight()/2; // start in middle and let them fly outward
            conceptNode.ontologyAcronym = ontologyAcronym;
            conceptNode.ontologyUri = conceptData.links.ontology;
            conceptNode.ontologyUriForIds = encodeURIComponent(conceptNode.ontologyUri);
            conceptNode.nodeColor = this.nextNodeColor(conceptNode.ontologyAcronym);

            // TODO Shall I reference the caller, or handle these in another way? How did I do similar stuff in ontology graph?
            // Could accumulate in caller?
            this.addNodes([conceptNode], expansionSet);

            // Understanding arcs:
            // Concept links come from different calls. We will probably need to use the links container
            // to collect all possible links that we know about, indexed by the concept that is not currently
            // included in our graph. When we get another concept added to the graph, we look it up in there,
            // add all the links to the graph, and remove the entries from the possible-links object.
            // This works only if we are able to add any given node prior to having to sort through its relations.
            // This also means that adding links has to be done in a separate process, and can't happen
            // in a smooth way when processing node information.
            // In Biomixer, these links were added as unrendered objects as they came up I think. We don't want
            // unrendered SVG in D3.
            // In any case, relations don't show up in the paths_to_root data anyway, so we need a separate process
            // because of that alone :)
            // We will need to inspect for relations in the registry, to see if there are any
            // implicit ones that have now been fulfilled by this node being added...is that correct to do here?
            // Registry should probably only have edges indexed by the *non-present* nodes, so that there is a simple
            // lookup for incoming nodes.
            // We also check for node endpoints in the graph before registering the implicit edges, so there's no risk of
            // adding an edge when it should instead be manifested in the graph.
            // Moved this into the addNodes() call
            // this.manifestEdgesForNewNode(conceptNode);
            return conceptNode;
        };

        /**
        * Created for composition expansions, where we never have node data available. Can be used any time we need to expand a specific node,
        * and when we know the ontology of that node (such as when doing concept expansions).
        */
        ConceptGraph.prototype.expandMappedConcept = function (newConceptId, newConceptMappingData, relatedConceptId, expansionType, expansionSet) {
            var newNodeUri = this.computeNodeId(newConceptMappingData);
            if (expansionType === PathOptionConstants.mappingsNeighborhoodConstant && this.nodeMayBeExpanded(newNodeUri, relatedConceptId, expansionType, expansionSet)) {
                // Moved node cap check to caller of this, where we have an estimate of incoming nodes.
                var url = newConceptMappingData.links.self;
                var callback = new FetchOneConceptCallback(this, url, newNodeUri, expansionSet);
                var fetcher = new Fetcher.RetryingJsonFetcher(url);
                fetcher.fetch(callback, true);
            }
        };

        /**
        * Created for composition expansions, where we never have node data available. Can be used any time we need to expand a specific node,
        * and when we know the ontology of that node (such as when doing concept expansions).
        */
        ConceptGraph.prototype.expandRelatedConcept = function (conceptsOntology, newConceptId, relatedConceptId, expansionType, expansionSet) {
            var newNodeId = this.computeNodeId(newConceptId, conceptsOntology);
            if (this.nodeMayBeExpanded(newNodeId, relatedConceptId, expansionType, expansionSet)) {
                // Moved node cap check to caller of this, where we have an estimate of incoming nodes.
                var url = this.buildConceptUrlNewApi(conceptsOntology, newConceptId);
                var callback = new FetchOneConceptCallback(this, url, newNodeId, expansionSet);
                var fetcher = new Fetcher.RetryingJsonFetcher(url);
                fetcher.fetch(callback, true);
            }
        };

        /**
        * Intended for adding arbitrary concepts, on the basis of their URI, to the graph.
        */
        ConceptGraph.prototype.addNodeToGraph = function (newConceptId) {
            // To get the ontology id...do a search for the concept id, then iterate through the results to find
            // an entity with a perfectly matching concept id!!!
            var url = this.buildConceptSearchUrlNewApi(newConceptId);
            var callback = new SearchOneConceptCallback(this, url, newConceptId);
            var fetcher = new Fetcher.RetryingJsonFetcher(url);
            fetcher.fetch(callback, true);
        };

        ConceptGraph.prototype.nodeMayBeExpanded1 = function (newConceptId, relatedConceptId, nodeRelationExpansionType, desiredRelationType) {
            return relatedConceptId !== newConceptId && nodeRelationExpansionType === desiredRelationType && !(String(newConceptId) in this.conceptIdNodeMap) && this.nodeIsAccessible(newConceptId);
        };

        ConceptGraph.prototype.nodeMayBeExpanded = function (newConceptId, relatedConceptId, nodeRelationExpansionType, expansionSet) {
            if (null === expansionSet.parentNode || null === nodeRelationExpansionType) {
                return false;
            }
            return relatedConceptId === expansionSet.parentNode.nodeId && this.nodeMayBeExpanded1(newConceptId, expansionSet.parentNode.nodeId, nodeRelationExpansionType, expansionSet.expansionType);
        };

        /**
        * Some nodes result in 403, 404 or other errors when REST calls re amde, and they will not be available.
        * We have to account for this nodes to offer the user accurate (and non-confusing) expansion estimates.
        */
        ConceptGraph.prototype.nodeIsAccessible = function (newConceptId) {
            return !this.expMan.nodeIsInaccessible(newConceptId);
        };

        ConceptGraph.prototype.expandAndParseNodeIfNeeded = function (newConceptId, relatedConceptId, conceptPropertiesData, expansionType, expansionSet, parentName) {
            // Can determine on the basis of the relatedConceptId if we should request data for the
            // conceptId provided, or if we should parse provided conceptProperties (if any).
            // TODO PROBLEM What if the conceptId is already going to be fetched and processed because
            // it has a fetcher running on the basis of some other relation?
            // In paths to root, it won't happen, because we would only want to parse from the original call.
            // In mappings, we only expand mapped nodes in the original call.
            // In term neighbourhood, we do indeed parse nodes on the basis of parent and child
            // relations, as well as composition relations. But...only if they are related to the
            // central one. So simply checking for that combination of facts here works out fine.
            // For path to root, we only expand those path to root nodes.
            // For term neighbourhood, we only expand the direct neighbours of the central node.
            // For mappings, we only expand based on the first mapping call.
            // This will go through a whole process of adding the node, if the node is supposed to be
            // expanded for the current visualization (children and parents for term neighbourhood).
            // Because we expand for term neighbourhood relation calls, and those come in two flavors
            // (node with properties for children and parents, and just node IDs for compositions)
            // we want to support parsing the data directly as well as fetching additional data.
            if (this.nodeMayBeExpanded(newConceptId, relatedConceptId, expansionType, expansionSet) && this.nodeIsAccessible(newConceptId)) {
                // Manifest the node; parse the properties if available.
                // We know that we will get the composition relations via a properties call,
                // and that has all the data we need from a separate call for properties...
                // but that subsystem relies on the fact that the node is created already.
                if (!(conceptPropertiesData === undefined) && Object.keys(conceptPropertiesData).length > 0 && expansionType !== PathOptionConstants.mappingsNeighborhoodConstant) {
                    // Would process the node data available for mappings, but we need the "prefLabel" property,
                    // which is not included therein, so we need a separate call rather than immediate parsing.
                    // The delay in rendering seems to be negligable in practice.
                    // Otherwise, we parse such as when it is a child or parent inheritance relation for term neighbourhood
                    var conceptNode = this.parseNode(undefined, conceptPropertiesData, expansionSet);
                    if (null == conceptNode) {
                        return;
                    }

                    // Don't show progress indicator, because this node won't have its relations expanded further down,
                    // because it never receives the directCallForExpansionType argument
                    this.fetchConceptRelations(conceptNode, conceptPropertiesData, expansionSet);
                } else {
                    console.log("Error: no data passed to expansion and parsing method");
                }
            }
        };

        /*
        * Parent and child arguments determine arrow direction. Relation type can
        * reflect inheritance, composition, or mapping.
        * I *think* that every time we register one of these, we should check and see if
        * the endpoints are in the graph, and if so, manifest the edge right away.
        * Likewise, I think, we should check for edge inclusions every time a node is
        * manifested. Otherwise we end up with problems...if...data integrity is not perfect
        * in a given ontology (has_part and part_of are not symmetrically stated, even though
        * semantically they necessitate each other; if not symmetrically defined, we will only
        * find the relation when manifesting nodes in one order, unless we always look for
        * edges when manifesting nodes).
        */
        ConceptGraph.prototype.manifestOrRegisterImplicitRelation = function (parentIdUri, childIdUri, relationId, relationProperty) {
            if (parentIdUri === childIdUri) {
                // Some mappings data is based off of having the same URI, which is mind boggling to me.
                // We have no use for self relations in this domain.
                return;
            }

            if (!this.nodeIsAccessible(childIdUri) || !this.nodeIsAccessible(parentIdUri)) {
                return;
            }

            // Either register it as an implicit relation, or manifest it if both nodes are in graph.
            var edge = new Link();

            // edge source and targe tobjects will be set when manifesting the edge (when we know we have
            // node objects to add there). They are looked up by these ids.
            // TODO source/target and parent/child are not clear...which way do we need this to be?
            // I prefer using parent/child in model, but for the graph, arrow representation is clearer
            // using source and target.
            edge.sourceId = parentIdUri;
            edge.targetId = childIdUri;
            edge.rawId = edge.sourceId + "-to-" + edge.targetId + "-of-" + relationId;
            edge.relationType = relationId;

            if (relationId === this.relationLabelConstants.inheritance || relationId === this.relationLabelConstants.mapping) {
                edge.edgePositionSlot = 0;
            } else if (relationId === this.relationLabelConstants.composition) {
                edge.edgePositionSlot = 1;
            } else {
                // Since this comes from actual edge objects, they will indeed be created serially and have different values here.
                var numExistingEdgesBetweenPair = this.expMan.edgeRegistry.getEdgesFor(edge.sourceId, edge.targetId).length;
                edge.edgePositionSlot = numExistingEdgesBetweenPair + 2; // +2 for the inheritance and composition positions
            }

            if (relationProperty === undefined) {
                edge.relationLabel = relationId;
            } else {
                if (undefined === relationProperty.label) {
                    var idSections = relationId.split("__");
                    edge.relationLabel = idSections[idSections.length - 1];
                } else {
                    edge.relationLabel = relationProperty.label;
                }
                edge.relationSpecificToOntologyAcronym = relationProperty.ontologyAcronym;
            }
            edge.id = Utils.escapeIdentifierForId(edge.sourceId) + "-to-" + Utils.escapeIdentifierForId(edge.targetId) + "-of-" + relationId;
            edge.value = 1; // This gets used for link stroke thickness later...not needed for concepts?

            // Changing the registry to be permanent, and to have no assumptions about gaph population.
            // All edges we learn about from REST services are permanently registered, and available for manifestation.
            // Do we need to check if it is already registered? Probably not!
            // We always want to register, since the registry is the permanent store of known edges.
            var preExistingEdge = this.registerImplicitEdge(edge);

            if (null !== preExistingEdge) {
                // We found a matching edge in the registry, so we'll ditch this instance.
                // This happens in paths to root when we generate arcs from a different set of calls.
                return;
            }

            // It checks to make sure the endpoints are extant, so we can fire it off right away.
            if (this.isEdgeForTemporaryRenderOnly(edge)) {
                // Prefer check here than in caller
                return;
            }
            this.manifestEdge([edge], false);
        };

        /**
        * Can be used when the edge does not have both endpoints in the graph, or when removing
        * edges that were added only temporarily.
        */
        ConceptGraph.prototype.registerImplicitEdge = function (edge) {
            return this.expMan.edgeRegistry.addEdgeToRegistry(edge, this);
        };

        /**
        * Manifests any provided edges, but possibly not temporary edges, depending on arguments.
        */
        ConceptGraph.prototype.manifestEdge = function (edges, allowTemporary) {
            var _this = this;
            var edgesToRender = [];
            $.each(edges, function (index, edge) {
                // Only ever manifest edges with endpoints in the graph
                var source = _this.conceptIdNodeMap[String(edge.sourceId)];
                var target = _this.conceptIdNodeMap[String(edge.targetId)];
                if (undefined === source || undefined === target || !_this.nodeInGraph(source) || !_this.nodeInGraph(target)) {
                    return;
                }

                if (!allowTemporary && _this.isEdgeForTemporaryRenderOnly(edge)) {
                    return;
                }

                edgesToRender.push(edge);
            });

            if (!allowTemporary) {
                this.graphView.stampTimeGraphModified();
            }

            this.addEdges(edgesToRender, allowTemporary);
        };

        ConceptGraph.prototype.manifestEdgesForNewNode = function (conceptNode) {
            // Because registry contains edges for which there *was* no node for the index,
            // and there *are* nodes for the other ends of the edge, we can manifest all of
            /// them when we are doing so due to a new node appearing.
            var allEdges = this.expMan.edgeRegistry.getEdgesFor(conceptNode.nodeId);
            this.manifestEdge(allEdges, false);
        };

        ConceptGraph.prototype.hasNonMappingEdgeAdjacent = function (nodeId) {
            var _this = this;
            return this.expMan.edgeRegistry.getEdgesFor(nodeId).some(function (link) {
                var source = _this.conceptIdNodeMap[String(link.sourceId)];
                var target = _this.conceptIdNodeMap[String(link.targetId)];
                if (undefined === source || undefined === target || !_this.nodeInGraph(source) || !_this.nodeInGraph(target)) {
                    return false;
                } else {
                    return link.relationType !== _this.relationLabelConstants.mapping;
                }
            });
        };

        ConceptGraph.prototype.isEdgeForTemporaryRenderOnly = function (edge) {
            // For mapping edges, if neither endpoint has triggered a mapping expansion, we won't
            // want to render the edge all the time.
            // But, now I also want to render mapping arcs that connect two nodes that each have
            // any non-mapping arc. Those nodes have higher relevance than a clustered cohort
            // of mutually mapped nodes, which would produce a hairball.
            if (edge.relationType === this.relationLabelConstants.mapping) {
                if (this.expMan.wasConceptClearedForExpansion(edge.sourceId, PathOptionConstants.mappingsNeighborhoodConstant) || this.expMan.wasConceptClearedForExpansion(edge.targetId, PathOptionConstants.mappingsNeighborhoodConstant)) {
                    // If one of the endpoints was expanded along mapping neighbourhood space, we will render the edge.
                    return false;
                } else if (this.hasNonMappingEdgeAdjacent(edge.sourceId) && this.expMan.edgeRegistry.getEdgesFor(edge.targetId)) {
                    // If both endpoints are interesting (meaning, both have non-mapping edges attached),
                    // then we will also render it.
                    return false;
                } else {
                    return true;
                }
            }

            return false;
        };

        ConceptGraph.prototype.manifestTemporaryHoverEdges = function (conceptNode) {
            var _this = this;
            var temporaryEdges = [];
            var nodeEdges = this.expMan.edgeRegistry.getEdgesFor(conceptNode.nodeId);

            // If clearedForMap, then technically all the mapping edges should be visible, so there's no reason to
            // look over the edges.
            var clearedForMap = this.expMan.wasConceptClearedForExpansion(conceptNode.nodeId, PathOptionConstants.mappingsNeighborhoodConstant);
            if (clearedForMap) {
                return;
            }

            $.each(nodeEdges, function (index, edge) {
                if (edge.relationType === _this.relationLabelConstants.mapping) {
                    var otherNodeId = (edge.sourceId === conceptNode.nodeId) ? edge.targetId : edge.sourceId;
                    var otherNodeClearedMap = _this.expMan.wasConceptClearedForExpansion(otherNodeId, PathOptionConstants.mappingsNeighborhoodConstant);
                    var otherNodeInGraph = _this.conceptIdNodeMap[String(otherNodeId)] != null;
                    if (!otherNodeClearedMap && temporaryEdges.indexOf(edge) === -1 && otherNodeInGraph) {
                        // If the other node is cleared, the edge should be already rendered.
                        temporaryEdges.push(edge);
                    }
                }
            });

            this.manifestEdge(temporaryEdges, true);
        };

        ConceptGraph.prototype.removeTemporaryHoverEdges = function (conceptNode) {
            var temporaryEdgesSelected = d3.selectAll("." + GraphView.BaseGraphView.temporaryEdgeClass);
            var temporaryEdgeData = [];
            temporaryEdgesSelected.each(function (d, i) {
                temporaryEdgeData.push(d);
            });
            this.removeEdges(temporaryEdgeData);
        };

        /**
        * This is important because children and parent calls can result in the same relations
        * being returned. I am not yet confident that we only need one of these calls though.
        * I am concerned that they may not always return equivalent results.
        *
        * @param edge
        * @returns {Boolean}
        */
        ConceptGraph.prototype.edgeNotInGraph = function (edge) {
            var length = this.graphD3Format.links.length;
            for (var i = 0; i < length; i++) {
                var item = this.graphD3Format.links[i];
                if (item.sourceId === edge.sourceId && item.targetId === edge.targetId && item.relationType === edge.relationType) {
                    return false;
                }
            }
            return true;
        };

        ConceptGraph.prototype.nodeInGraph = function (node) {
            return this.conceptIdNodeMap[(String)(node.nodeId)] !== undefined;
        };

        ConceptGraph.prototype.getAdjacentLinks = function (node) {
            var adjacentEdges = [];
            var length = this.graphD3Format.links.length;
            for (var i = 0; i < length; i++) {
                var link = this.graphD3Format.links[i];
                if (link.source === node || link.target === node) {
                    adjacentEdges.push(link);
                }
            }
            return adjacentEdges;
        };

        /**
        * initSet is only passed so we can rename it when we have the core node's name
        */
        ConceptGraph.prototype.fetchPathToRoot = function (centralOntologyAcronym, centralConceptUri, expansionSet, initSet) {
            // I have confirmed that this is faster than BioMixer. Without removing
            // network latency in REST calls, it is approximately half as long from page load to
            // graph completion (on the order of 11 sec vs 22 sec)
            // Tried web workers, but D3 doesn't play well with that, and they aren't appropriate
            // for REST call handling.
            /* Adding BioPortal data for ontology overview graph (mapping neighbourhood of a single ontology node)
            1) Get the root to path for the central concept
            http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F82968002/paths_to_root/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P0.onSuccess
            - create the nodes, and do any prep for subsequent REST calls
            2) Get relational data (children, parents and mappings) for all concepts in the path to root
            http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F82968002/parents/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P0.onSuccess
            - fill in nodes with details from this data TODO Look at Biomixer to see what we need
            3) Get properties for all concepts in path to root
            http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F82968002/properties/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P0.onSuccess
            - set node properties
            */
            // 1) Get paths to root for the central concept
            // Technically, the path to root does *not* use the normal wildfire expansion technique,
            // since we can get the full et of nodes to expand directly from the path to root REST call.
            // This mean that we don't need to enter the root node (nor path nodes) into the expansion registry...
            var pathsToRootUrl = this.buildPathToRootUrlNewApi(centralOntologyAcronym, centralConceptUri);

            // TODO Think about fetching the target node separately...we have to check for root node presence in the callback,
            // so we can associate the root node object with the expansion set as soon as possible.
            // Currently, it relies on the order of the results from the call, in order to get the
            // target node first.
            var pathsToRootCallback = new PathsToRootCallback(this, pathsToRootUrl, centralOntologyAcronym, centralConceptUri, expansionSet, initSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(pathsToRootUrl);
            fetcher.fetch(pathsToRootCallback, true);
        };

        ConceptGraph.prototype.expandConceptNeighbourhood = function (nodeData, expansionSet) {
            var centralConceptUrl = this.buildConceptUrlNewApi(nodeData.ontologyAcronym, nodeData.simpleConceptUri);
            var centralCallback = new FetchConceptRelationsCallback(this, centralConceptUrl, nodeData, PathOptionConstants.termNeighborhoodConstant, expansionSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(centralConceptUrl);
            fetcher.fetch(centralCallback, true);
        };

        ConceptGraph.prototype.expandMappingNeighbourhood = function (nodeData, expansionSet) {
            // Cannot just call fetchMappings() directly because we need the link from the base concept URL
            var centralConceptUrl = this.buildConceptUrlNewApi(nodeData.ontologyAcronym, nodeData.simpleConceptUri);
            var centralCallback = new FetchConceptRelationsCallback(this, centralConceptUrl, nodeData, PathOptionConstants.mappingsNeighborhoodConstant, expansionSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(centralConceptUrl);
            fetcher.fetch(centralCallback, true);
        };

        /**
        * initSet is only passed so we can rename it when we have the core node's name
        */
        ConceptGraph.prototype.fetchTermNeighborhood = function (centralOntologyAcronym, centralConceptUri, expansionSet, initSet) {
            // 1) Get term neighbourhood for the central concept by fetching term and marking it for expansion
            // Parsers that follow will expand neighbourhing concepts.
            var centralConceptUrl = this.buildConceptUrlNewApi(centralOntologyAcronym, centralConceptUri);
            var centralCallback = new FetchTargetConceptCallback(this, centralConceptUrl, centralConceptUri, PathOptionConstants.termNeighborhoodConstant, expansionSet, initSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(centralConceptUrl);
            fetcher.fetch(centralCallback, true);
        };

        /**
        * initSet is only passed so we can rename it when we have the core node's name
        */
        ConceptGraph.prototype.fetchMappingsNeighborhood = function (centralOntologyAcronym, centralConceptUri, expansionSet, initSet) {
            // Should I call the mapping, inferring the URL, or should I call for the central node, add it, and use conditional expansion in the relation parser?
            // http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F410607006/mappings/?apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P109.onSuccess
            // Get central concept immediately, and let the relation parser that will be called expand
            // related nodes conditioned on whether their related node is this to-be-expanded one.
            // Loading that node will get the mappings, and subsequently the concepts mapped to.
            // The mapping parser will fetch individual mapped concepts as it finds them by checking to see
            // if we are in the mapping visualization. I could make this explicit by copying the mappings code
            // here, but then we have duplicate code. If we decide it reads poorly to have it so detached
            // in the process, we can copy it here.
            var centralConceptUrl = this.buildConceptUrlNewApi(centralOntologyAcronym, centralConceptUri);
            var centralCallback = new FetchTargetConceptCallback(this, centralConceptUrl, centralConceptUri, PathOptionConstants.mappingsNeighborhoodConstant, expansionSet, initSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(centralConceptUrl);
            fetcher.fetch(centralCallback, true);
        };

        ConceptGraph.prototype.fetchConceptRelations = function (conceptNode, conceptData, expansionSet, directCallForExpansionType) {
            // 2) Get relational data for all the concepts, create links from them
            // fetchBatchRelations(); // don't exist, because of COR issues on server, cross domain, and spec issues.
            // Children requests have paging, which needs cycling internally.
            // If the PathOptions argument is compatible with any of the below methods, it wil auto-expand
            // the nodes therein.
            this.fetchChildren(conceptNode, conceptData.links.children, 1, directCallForExpansionType, expansionSet);
            this.fetchParents(conceptNode, conceptData.links.parents, directCallForExpansionType, expansionSet);
            this.fetchMappings(conceptNode, conceptData.links.mappings, directCallForExpansionType, expansionSet);
            this.fetchCompositionRelations(conceptNode, directCallForExpansionType, expansionSet);
        };

        ConceptGraph.prototype.fetchChildren = function (conceptNode, relationsUrl, pageRequested, directCallForExpansionType, expansionSet) {
            var giveUserBusyIndicator;
            if (directCallForExpansionType === PathOptionConstants.termNeighborhoodConstant) {
                giveUserBusyIndicator = true;
            } else {
                giveUserBusyIndicator = false;
            }

            // Children requests have paging, which needs cycling internally.
            relationsUrl = Utils.addOrUpdateUrlParameter(relationsUrl, "page", pageRequested + "");
            var conceptRelationsCallback = new ConceptChildrenRelationsCallback(this, relationsUrl, conceptNode, this.conceptIdNodeMap, directCallForExpansionType, expansionSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(relationsUrl);
            fetcher.fetch(conceptRelationsCallback, giveUserBusyIndicator);
        };

        ConceptGraph.prototype.fetchParents = function (conceptNode, relationsUrl, directCallForExpansionType, expansionSet) {
            var giveUserBusyIndicator;
            if (directCallForExpansionType === PathOptionConstants.termNeighborhoodConstant) {
                giveUserBusyIndicator = true;
            } else {
                giveUserBusyIndicator = false;
            }
            var conceptRelationsCallback = new ConceptParentsRelationsCallback(this, relationsUrl, conceptNode, this.conceptIdNodeMap, directCallForExpansionType, expansionSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(relationsUrl);
            fetcher.fetch(conceptRelationsCallback, giveUserBusyIndicator);
        };

        ConceptGraph.prototype.fetchMappings = function (conceptNode, relationsUrl, directCallForExpansionType, expansionSet) {
            var giveUserBusyIndicator;
            if (directCallForExpansionType === PathOptionConstants.mappingsNeighborhoodConstant) {
                giveUserBusyIndicator = true;
            } else {
                giveUserBusyIndicator = false;
            }
            var conceptRelationsCallback = new ConceptMappingsRelationsCallback(this, relationsUrl, conceptNode, this.conceptIdNodeMap, directCallForExpansionType, expansionSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(relationsUrl);
            fetcher.fetch(conceptRelationsCallback, giveUserBusyIndicator);
        };

        ConceptGraph.prototype.fetchCompositionRelations = function (conceptNode, directCallForExpansionType, expansionSet) {
            var giveUserBusyIndicator;
            if (directCallForExpansionType === PathOptionConstants.termNeighborhoodConstant) {
                giveUserBusyIndicator = true;
            } else {
                giveUserBusyIndicator = false;
            }

            // NB Within the parsing call, we will get the ontology's property based relations (composition plus others) if it isn't fetched.
            // That's only a single call per ontology, but it does delay the properties call until that other call is made first.
            // Since all we use the properties for at the moment is composite and these other relations, that's an ok delay.
            var relationsUrl = this.buildConceptCompositionsRelationUrl(conceptNode);
            var conceptRelationsCallback = new ConceptCompositionRelationsCallback(this, relationsUrl, conceptNode, this.conceptIdNodeMap, directCallForExpansionType, expansionSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(relationsUrl);
            fetcher.fetch(conceptRelationsCallback, giveUserBusyIndicator);
        };

        // http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F82968002/paths_to_root/?format=jsonp&apikey=efcfb6e1-bcf8-4a5d-a46a-3ae8867241a1&callback=__gwt_jsonp__.P0.onSuccess
        ConceptGraph.prototype.buildPathToRootUrlNewApi = function (centralOntologyAcronym, centralConceptUri) {
            // String() converts object String back to primitive string. Go figure.
            return "http://" + Utils.getBioportalUrl() + "/ontologies/" + centralOntologyAcronym + "/classes/" + encodeURIComponent(String(centralConceptUri)) + "/paths_to_root/";
        };

        // This is unused. See description. Leaving as documentation.
        ConceptGraph.prototype.buildTermNeighborhoodUrlNewApi = function (centralOntologyAcronym, centralConceptUri) {
            // Term neighborhood requires the core concept call, then properties, mappings, children and parents (in no particular order).
            // Since those all need to be called for *any* node being loaded, this visualization mode relies upon cascading expansion as
            // relations are parsed. Thus, the URL for this call is really just a concept node URL. The subsquent functions
            // will check the visualization mode to decide whether they are expanding the fetched relations or not.
            // String() converts object String back to primitive string. Go figure.
            return this.buildConceptUrlNewApi(centralOntologyAcronym, centralConceptUri);
        };

        // This might be unused, because we may navigate to the mappings URL along the link data provided from the new API.
        ConceptGraph.prototype.buildMappingsNeighborhoodUrlNewApi = function (centralOntologyAcronym, centralConceptUri) {
            // From the mappings results, we add all of the discovered nodes.
            // String() converts object String back to primitive string. Go figure.
            return "http://" + Utils.getBioportalUrl() + "/ontologies/" + centralOntologyAcronym + "/classes/" + encodeURIComponent(String(centralConceptUri)) + "/mappings/";
        };

        ConceptGraph.prototype.buildConceptUrlNewApi = function (ontologyAcronym, conceptUri) {
            // String() converts object String back to primitive string. Go figure.
            return "http://" + Utils.getBioportalUrl() + "/ontologies/" + ontologyAcronym + "/classes/" + encodeURIComponent(String(conceptUri));
        };

        ConceptGraph.prototype.buildConceptSearchUrlNewApi = function (conceptUri) {
            // String() converts object String back to primitive string. Go figure.
            return "http://" + Utils.getBioportalUrl() + "/search/?require_exact_match=true&also_search_properties=false&q=" + encodeURIComponent(String(conceptUri));
        };

        ConceptGraph.prototype.buildConceptCompositionsRelationUrl = function (concept) {
            return "http://" + Utils.getBioportalUrl() + "/ontologies/" + concept.ontologyAcronym + "/classes/" + encodeURIComponent(String(concept.simpleConceptUri)) + "?include=properties";
        };

        //If we can use batch calls for the parent, child and mappings of each node, we save 2 REST calls per node.
        //If we can use batch calls for parent, child, and mapping for several nodes, we save a lot more, but the response
        //size and response times might be too long. We can use bulk asking for just one of the three relational data
        //properties.
        //Nodes also need a properties call each, which might be done in bulk.
        ConceptGraph.prototype.buildBatchRelationUrl = function (concept) {
            // Unused currently due to specification issues
            // 400-800 for children, properties each, 500-900 for parents, 500-900 for mappings
            // 500-1.2s for all four combined. Looks like savings to me.
            return "http://" + Utils.getBioportalUrl() + "/ontologies/" + concept.ontologyAcronym + "/classes/" + concept.conceptUriForIds + "?include=children,parents,mappings,properties";
        };

        ConceptGraph.prototype.buildBatchRelationUrlAndPostData = function (concepts) {
            // Given a set of concepts, create a batch API call to retrieve their parents, children and mappings
            // http://stagedata.bioontology.org/documentation#nav_batch
            var url = "http://" + Utils.getBioportalUrl() + "/batch/";

            // TEMP TEST
            // url = "http://stagedata.bioontology.org/batch?";
            var classCollection = [];
            var postObject = {
                "http://www.w3.org/2002/07/owl#Class": {
                    "collection": classCollection
                },
                "include": "children, parents, mappings, properties"
            };
            $.each(concepts, function (i, d) {
                classCollection.push({
                    "class": d.id,
                    "ontology": d.ontologyUri
                });
            });

            //  console.log(postObject);
            // TEMP TEST
            postObject = {
                "http://www.w3.org/2002/07/owl#Class": {
                    "collection": [
                        {
                            "class": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Information_Resource",
                            "ontology": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#"
                        },
                        {
                            "class": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Data_Resource",
                            "ontology": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#"
                        },
                        {
                            "class": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Clinical_Care_Data",
                            "ontology": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#"
                        },
                        {
                            "class": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#Aggregate_Human_Data",
                            "ontology": "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#"
                        }
                    ],
                    "include": "prefLabel,synonym"
                }
            };

            return {
                "url": url,
                "data": postObject
            };
        };

        ConceptGraph.prototype.nextNodeColor = function (ontologyRawAcronym) {
            var ontologyAcronym = String(ontologyRawAcronym);
            if (!(ontologyAcronym in this.ontologyColorMap)) {
                this.currentNodeColor = this.currentNodeColor == 19 ? 0 : this.currentNodeColor + 1;
                this.ontologyColorMap[ontologyAcronym] = this.nodeOrderedColors(this.currentNodeColor);
            }
            return this.ontologyColorMap[ontologyAcronym];
        };

        ConceptGraph.prototype.brightenColor = function (outerColor) {
            // Outer color will be a 6 digit hex representation. Let's make it darker across all three factors.
            // Using lab() converts from hex RGB to the Cie L*A*B equivalent.
            return d3.lab(outerColor).brighter(1).toString();
        };

        ConceptGraph.prototype.darkenColor = function (outerColor) {
            // Outer color will be a 6 digit hex representation. Let's make it darker across all three factors.
            // Using lab() converts from hex RGB to the Cie L*A*B equivalent.
            return d3.lab(outerColor).darker(1).toString();
        };
        ConceptGraph.CONCEPT_URI_SEPARATOR = "::";
        return ConceptGraph;
    })();
    exports.ConceptGraph = ConceptGraph;

    var PathsToRootCallback = (function (_super) {
        __extends(PathsToRootCallback, _super);
        function PathsToRootCallback(graph, url, centralOntologyAcronym, centralConceptUri, expansionSet, initSet) {
            var _this = this;
            _super.call(this, url, String(centralOntologyAcronym) + ":" + String(centralConceptUri), 1 /* nodesMultiple */);
            this.graph = graph;
            this.centralOntologyAcronym = centralOntologyAcronym;
            this.centralConceptUri = centralConceptUri;
            this.expansionSet = expansionSet;
            this.initSet = initSet;
            this.callback = function (pathsToRootData, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                // CORS enabled GET and POST do though!
                if (jqXHR != null) {
                    if (pathsToRootData.errors != null) {
                        // We had an error. Handle it.
                        // Well...in this case, if there's an error, there's not much to do.
                        // TODO Give user a message sayign the operation failed
                        console.log("Failed to load paths to root: " + _this.centralConceptUri);
                        return;
                    }
                }

                var numberOfConcepts = Object.keys(pathsToRootData).length;
                var newNodesForExpansionGraph = {};

                // Go backwards through results to get the target node first, so we can have it immediately for
                // the expansion set parent.
                var collapsedPathsToRootData = {};
                for (var pathIndex = 0; pathIndex < pathsToRootData.length; pathIndex++) {
                    for (var conceptIndex = pathsToRootData[pathIndex].length - 1; conceptIndex >= 0; conceptIndex--) {
                        var nodeData = pathsToRootData[pathIndex][conceptIndex];
                        var newNodeId = String(_this.graph.computeNodeId(nodeData));
                        if (newNodesForExpansionGraph[newNodeId] === undefined) {
                            var conceptNode = _this.graph.parseNode(undefined, nodeData, _this.expansionSet);

                            if (null == conceptNode) {
                                continue;
                            }

                            if (conceptNode.simpleConceptUri === _this.centralConceptUri) {
                                _this.expansionSet.parentNode = conceptNode;
                                if (_this.initSet !== null) {
                                    _this.initSet.updateExpansionNodeDisplayName(conceptNode.name + " (" + conceptNode.ontologyAcronym + ")");
                                }
                            }
                            newNodesForExpansionGraph[conceptNode.getEntityId()] = conceptNode;
                            collapsedPathsToRootData[conceptNode.getEntityId()] = nodeData;
                        }

                        // Regardless of whether we had to parse this node in the current loop or not,
                        // we need to identify it and get path arcs prepared. We may have parsed it in a previous
                        // iteration.
                        var currentNode = newNodesForExpansionGraph[newNodeId];

                        // Create link between this node and its predecessor within the current path
                        // Note that the multiple paths are single-inheritance, that is, within each pathsToRootData
                        // index, we have a single lineage. Thus, any predecessor in the array is a child of the node
                        // we are currently working with.
                        var parentIndex = conceptIndex + 1;
                        if (parentIndex < pathsToRootData[pathIndex].length) {
                            var parentData = pathsToRootData[pathIndex][parentIndex];
                            var parentNodeId = String(_this.graph.computeNodeId(parentData));
                            var parentNode = newNodesForExpansionGraph[parentNodeId];

                            // Note also that normally when we parse for arcs, we will parse for the node if possible. Since
                            // we go in reverse order, the parent has already been parsed and is known to us.
                            var conceptRelationsCallback = new ConceptParentsRelationsCallback(_this.graph, "", parentNode, _this.graph.conceptIdNodeMap, PathOptionConstants.pathsToRootConstant, _this.expansionSet);
                            conceptRelationsCallback.callback([nodeData], textStatus, jqXHR);
                        }
                    }
                }

                for (var nodeId in newNodesForExpansionGraph) {
                    var node = newNodesForExpansionGraph[nodeId];
                    var data = collapsedPathsToRootData[node.getEntityId()];

                    // No busy indicator here because we have our path of nodes already
                    _this.graph.fetchConceptRelations(node, data, _this.expansionSet);
                }
            };
        }
        return PathsToRootCallback;
    })(Fetcher.CallbackObject);

    var FetchTargetConceptCallback = (function (_super) {
        __extends(FetchTargetConceptCallback, _super);
        function FetchTargetConceptCallback(graph, url, conceptUri, directCallForExpansionType, expansionSet, initSet) {
            var _this = this;
            _super.call(this, url, String(conceptUri), 0 /* nodeSingle */); //+":"+directCallForExpansionType);
            this.graph = graph;
            this.conceptUri = conceptUri;
            this.directCallForExpansionType = directCallForExpansionType;
            this.expansionSet = expansionSet;
            this.initSet = initSet;
            this.callback = function (conceptPropertiesData, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                // CORS enabled GET and POST do though!
                if (jqXHR != null) {
                    if (conceptPropertiesData.errors != null) {
                        // We had an error. Handle it.
                        // Well...in this case, if there's an error, there's not much to do.
                        // TODO Give user a message sayign the operation failed
                        console.log("Failed to load target node: " + _this.conceptUri);
                        return;
                    }
                }

                var conceptNode = _this.graph.parseNode(undefined, conceptPropertiesData, _this.expansionSet);
                if (null == conceptNode) {
                    return;
                }

                // This is the vital difference from the FetchOneConceptCallback
                _this.expansionSet.parentNode = conceptNode;

                if (_this.initSet !== null) {
                    _this.initSet.updateExpansionNodeDisplayName(conceptNode.name + " (" + conceptNode.ontologyAcronym + ")");
                }

                // As we grab related concepts, we might expand them if their relation matches the expansion we are using.
                // TODO The indicator isn't necessarily true it should be so only for the expansion-matching relation call.
                console.log("Fix to make optional or default...or allow null...to defer decisions of busy indicator to sub-fetchers");
                _this.graph.fetchConceptRelations(conceptNode, conceptPropertiesData, _this.expansionSet, _this.directCallForExpansionType);
            };
        }
        return FetchTargetConceptCallback;
    })(Fetcher.CallbackObject);

    var FetchOneConceptCallback = (function (_super) {
        __extends(FetchOneConceptCallback, _super);
        function FetchOneConceptCallback(graph, url, conceptUri, expansionSet) {
            var _this = this;
            _super.call(this, url, String(conceptUri), 0 /* nodeSingle */); //+":"+directCallForExpansionType);
            this.graph = graph;
            this.conceptUri = conceptUri;
            this.expansionSet = expansionSet;
            this.directCallForExpansionType = PathOptionConstants.singleNodeOrSubordinateConstant;
            this.callback = function (conceptPropertiesData, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                // CORS enabled GET and POST do though!
                if (jqXHR != null) {
                    if (conceptPropertiesData.errors != null) {
                        // We had an error. Handle it.
                        _this.graph.expMan.purgeInaccessibleNode(_this.conceptUri);
                        return;
                    }
                }

                var fetchCall = function () {
                    var conceptNode = _this.graph.parseNode(undefined, conceptPropertiesData, _this.expansionSet);
                    if (null == conceptNode) {
                        return;
                    }

                    // As we grab related concepts, we might expand them if their relation matches the expansion we are using.
                    _this.graph.fetchConceptRelations(conceptNode, conceptPropertiesData, _this.expansionSet);
                };

                // Removed cap check within FetchOneConceptCallback, because it is better to check prior to this.
                // Used to conditionally check the cap on the basis of incoming argument.
                fetchCall();
            };
        }
        return FetchOneConceptCallback;
    })(Fetcher.CallbackObject);
    exports.FetchOneConceptCallback = FetchOneConceptCallback;

    var SearchOneConceptCallback = (function (_super) {
        __extends(SearchOneConceptCallback, _super);
        function SearchOneConceptCallback(graph, url, conceptUri, priorityLoadNoCapCheck) {
            if (typeof priorityLoadNoCapCheck === "undefined") { priorityLoadNoCapCheck = false; }
            var _this = this;
            _super.call(this, url, String(conceptUri), 0 /* nodeSingle */); //+":"+directCallForExpansionType);
            this.graph = graph;
            this.conceptUri = conceptUri;
            this.priorityLoadNoCapCheck = priorityLoadNoCapCheck;
            this.directCallForExpansionType = PathOptionConstants.singleNodeConstant;
            this.callback = function (conceptMatchData, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                // CORS enabled GET and POST do though!
                if (jqXHR != null) {
                    if (conceptMatchData.errors != null) {
                        // We had an error. Handle it.
                        // This error could represent a totally invalid id, and we don't know the ontology
                        // the potential node belongs to anyway. Thus, we won't purge it.
                        // this.graph.expMan.purgeInaccessibleNode(this.conceptUri);
                        return;
                    }
                }

                // Even though we expect few nodes to match, we still will check the cap.
                var conceptPropertiesData = [];
                for (var i in conceptMatchData.collection) {
                    // This approach is optimistic; there are paged search results, but if I require
                    // an exact hit, we should get an exact hit, shouldn't we?
                    // http://data.bioontology.org/search?q=http://purl.obolibrary.org/obo/UBERON_0018255
                    var hit = conceptMatchData.collection[i];
                    if (hit.matchType === "id" && hit["@id"] === _this.conceptUri) {
                        conceptPropertiesData.push(hit);
                    }
                }
                if (conceptPropertiesData.length == 0) {
                    alert("Failed to import node for provided id: '" + _this.conceptUri + "'");

                    // this.graph.undoBoss.removeCommand(this.expansionSet.graphModifier);
                    return;
                } else {
                    var expId = new ExpansionSets.ExpansionSetIdentifer("arbitraryConceptAddition_" + Utils.escapeIdentifierForId(_this.conceptUri), "Added Arbitrary Node");
                    var expansionSet = new ExpansionSets.ExpansionSet(expId, null, _this.graph, _this.graph.expMan.getActiveExpansionSets(), _this.graph.undoBoss, PathOptionConstants.singleNodeConstant);
                    var lastConceptNode;
                    var lastConceptNodeData;
                    var fetchCall = function () {
                        for (var j = 0; j < conceptPropertiesData.length; j++) {
                            lastConceptNodeData = conceptPropertiesData[j];
                            var node = _this.addNode(lastConceptNodeData, expansionSet);
                            if (null !== node) {
                                lastConceptNode = node;
                            }
                        }
                        if (expansionSet.nodes.length === 1) {
                            expansionSet.id.setDisplayId(expansionSet.id.getDisplayId() + " (" + lastConceptNode.ontologyAcronym + ")");
                        } else {
                            expansionSet.id.setDisplayId(expansionSet.id.getDisplayId() + " (multiple ontologies)");
                        }
                    };

                    // var ontologyUri = conceptData.links.ontology;
                    // Check cap using the last node we found in the search results.
                    var lastNodeId = _this.graph.computeNodeId(conceptPropertiesData[0]);
                    _this.graph.checkForNodeCap(fetchCall, expansionSet, conceptPropertiesData.length);
                }
            };
        }
        SearchOneConceptCallback.prototype.addNode = function (conceptPropertiesData, expansionSet) {
            var conceptNode = this.graph.parseNode(undefined, conceptPropertiesData, expansionSet);
            if (null == conceptNode) {
                return null;
            }

            if (expansionSet.nodes.length === 0) {
                expansionSet.id.setDisplayId("Added: " + conceptNode.name);
            }

            // As we grab related concepts, we might expand them if their relation matches the expansion we are using.
            this.graph.fetchConceptRelations(conceptNode, conceptPropertiesData, expansionSet);

            return conceptNode;
        };
        return SearchOneConceptCallback;
    })(Fetcher.CallbackObject);
    exports.SearchOneConceptCallback = SearchOneConceptCallback;

    /**
    * Similar to FetchOneConcept, except for when we knwo we have the node already.
    */
    var FetchConceptRelationsCallback = (function (_super) {
        __extends(FetchConceptRelationsCallback, _super);
        function FetchConceptRelationsCallback(graph, url, node, directCallForExpansionType, expansionSet) {
            var _this = this;
            // Arguably we could call this a nodesMultiple CallbackVariety...
            _super.call(this, url, String(node.nodeId), 2 /* links */); //+":"+directCallForExpansionType);
            this.graph = graph;
            this.node = node;
            this.directCallForExpansionType = directCallForExpansionType;
            this.expansionSet = expansionSet;
            this.callback = function (conceptPropertiesData, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                // CORS enabled GET and POST do though!
                if (jqXHR != null) {
                    if (conceptPropertiesData.errors != null) {
                        // We had an error. Handle it.
                        // No relations available? Why not? I'm not sure how to recover.
                        return;
                    }
                }

                // As we grab related concepts, we might expand them if their relation matches the expansion we are using.
                _this.graph.fetchConceptRelations(_this.node, conceptPropertiesData, _this.expansionSet, _this.directCallForExpansionType);
            };
        }
        return FetchConceptRelationsCallback;
    })(Fetcher.CallbackObject);

    // currently oriented to grabbing data for a single concept. Might do batch later when that works server side
    // for cross domain requests.
    // Can process mapping, parent, properties, and children, even if not all are passed in.
    // This is useful given that parents don't show up if children are requested.
    var ConceptCompositionRelationsCallback = (function (_super) {
        __extends(ConceptCompositionRelationsCallback, _super);
        function ConceptCompositionRelationsCallback(graph, url, conceptNode, conceptNodeIdMap, directCallForExpansionType, expansionSet) {
            var _this = this;
            _super.call(this, url, String(conceptNode.nodeId), 2 /* links */); //+":"+directCallForExpansionType);
            this.graph = graph;
            this.conceptNode = conceptNode;
            this.conceptNodeIdMap = conceptNodeIdMap;
            this.directCallForExpansionType = directCallForExpansionType;
            this.expansionSet = expansionSet;
            this.callback = function (relationsDataRaw, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                // CORS enabled GET and POST do though!
                if (jqXHR != null) {
                    if (relationsDataRaw.errors != null) {
                        // We had an error. Handle it.
                        // No relations available? Why not? I'm not sure how to recover.
                        return;
                    }
                }

                var outerThis = _this;

                // Before we parse this data, we have to make sure we have fetched the correpsonding
                // ontology's property relations data. This tells us what properties exist that
                // represent relations that we can add as arcs.
                // If we don't have that data yet, tell the registry, and provide a callback to come right back
                // here. The registry will get the required info, then call back to here.
                if (!PropRel.OntologyPropertyRelationsRegistry.contains(_this.conceptNode.ontologyAcronym)) {
                    PropRel.OntologyPropertyRelationsRegistry.fetchOntologyPropertyRelations(_this.conceptNode, function () {
                        // We wrap the callbkac we are currently in, so that we can re-enter it later
                        // when we have the necessary ontology data.
                        _this.callback(relationsDataRaw, textStatus, jqXHR);
                    });
                    return;
                }

                // NB The part_of and has_part relations below are based on SNOMEDCT (and possibly other)
                // property relations that were in the very oldest versions of Biomixer. With the API call
                // for relational properties now available, these non-inheritance relations will become
                // more common and diverse, in ontologies that publish the properties.
                // Note that at the time of writing, SNOMEDCT does not provide any relation properties,
                // but when we get the properties for concepts, we find has_part and part_of (as well as
                // other apparent relations, such as has_laterality). I am keeping the two hard coded ones here,
                // and using published property relations for additional arc types.
                // Loop over results, properties, then mappings, parents, children.
                var funcsToCall = [];
                $.each(relationsDataRaw.properties, function (propertyId, propertyValue) {
                    // NB Composition relations can only be parsed from properties received with the "include=properties"
                    // parameter. This means that although properties are received elsewhere (path to root, children),
                    // those property sets never give us the composition relations.
                    // But...children property sets do have all the other things we need to get the seed of data for a node
                    // (being the @id and the ontology link from which we need to extract the true-and-valid ontology acronym)
                    // We also have some grandfathered special cases. Has_part and part_of show up at least in SNOMDED, but without
                    // a corresponding relation property definition on the ontology itself. We can keep that,. but we can't double parse
                    // said relation on other ontologies that do have a relation property definition.
                    // This meta data (http://data.bioontology.org/metadata/treeView) is not useful for graphs.
                    // There could be other metadata like this though...
                    if (Utils.endsWith(propertyId, "treeView")) {
                        return;
                    }

                    // Check for ontology declared property relations.
                    var matchedRelationProp = PropRel.OntologyPropertyRelationsRegistry.matchedAvailableRelations(_this.conceptNode.ontologyAcronym, propertyId);

                    // want label for the arc name, want idEscaped for the id...where am I getting the actual concept that is related?
                    if (matchedRelationProp !== undefined) {
                        $.each(propertyValue, function (i, relatedPartId) {
                            if (relatedPartId.indexOf("http") !== 0) {
                                // Non-relational properties do indeed appear in the ontology property results.
                                // If it isn't a concept URI, we will skip it.
                                // Just skipping non-uris is not even adequate, since there could be additional non-concept
                                // uris used. I don't think there is an automated way of knowing which properties are
                                // actually relational.
                                // console.log("Skipped '"+relatedPartId+"' ("+propertyId+")");
                                return;
                            }
                            var newRelatedNodeId = _this.graph.computeNodeId(relatedPartId, _this.conceptNode.ontologyAcronym);
                            _this.graph.manifestOrRegisterImplicitRelation(_this.conceptNode.nodeId, newRelatedNodeId, matchedRelationProp.idEscaped, matchedRelationProp);
                            if (_this.graph.nodeMayBeExpanded(newRelatedNodeId, _this.conceptNode.nodeId, PathOptionConstants.termNeighborhoodConstant, _this.expansionSet)) {
                                funcsToCall.push(function () {
                                    _this.graph.expandRelatedConcept(_this.conceptNode.ontologyAcronym, relatedPartId, _this.conceptNode.nodeId, PathOptionConstants.termNeighborhoodConstant, _this.expansionSet);
                                });
                            }
                        });
                        return;
                    }

                    // See line 71 TermWithoutRelationsJsonParser for how it was dealt with in Java.
                    // We already parsed for other (automatic) properties when we first got this node's
                    // data, so here we only do composite relations and maybe additional properties if needed.
                    // This is properties such as: "http://purl.bioontology.org/ontology/SNOMEDCT/has_part"
                    // I know, not the most general property name...
                    if (Utils.endsWith(propertyId, "has_part")) {
                        $.each(propertyValue, function (index, childPartId) {
                            // TODO Need to register all node ids we get, so that for the different visualizations, we can expand differently.
                            // For path to root, we only expand those path to root nodes (determined at beginning)
                            // For term neighbourhood, we only expand the direct neighbours of the central node (determined during fetches).
                            // For mappings, we only expand based on the first mapping call (determined during fetches).
                            // Ergo, we need to expand composition mappings if we are in the term neighbourhood vis.
                            // PROBLEM Seems like I want to manifest nodes before doing arcs, but in this case, I want to know
                            // if the relation exists so I can fetch the node data...
                            var newChildNodeId = _this.graph.computeNodeId(childPartId, _this.conceptNode.ontologyAcronym);
                            _this.graph.manifestOrRegisterImplicitRelation(_this.conceptNode.nodeId, newChildNodeId, _this.graph.relationLabelConstants.composition);

                            if (_this.graph.nodeMayBeExpanded(newChildNodeId, _this.conceptNode.nodeId, PathOptionConstants.termNeighborhoodConstant, _this.expansionSet)) {
                                funcsToCall.push(function () {
                                    _this.graph.expandRelatedConcept(_this.conceptNode.ontologyAcronym, childPartId, _this.conceptNode.nodeId, PathOptionConstants.termNeighborhoodConstant, _this.expansionSet);
                                });
                            }
                        });
                        return;
                    }

                    if (Utils.endsWith(propertyId, "part_of")) {
                        $.each(propertyValue, function (index, parentPartId) {
                            var newParentNodeId = _this.graph.computeNodeId(parentPartId, _this.conceptNode.ontologyAcronym);
                            _this.graph.manifestOrRegisterImplicitRelation(newParentNodeId, _this.conceptNode.nodeId, _this.graph.relationLabelConstants.composition);

                            if (_this.graph.nodeMayBeExpanded(newParentNodeId, _this.conceptNode.nodeId, PathOptionConstants.termNeighborhoodConstant, _this.expansionSet)) {
                                funcsToCall.push(function () {
                                    _this.graph.expandRelatedConcept(_this.conceptNode.ontologyAcronym, parentPartId, _this.conceptNode.nodeId, PathOptionConstants.termNeighborhoodConstant, _this.expansionSet);
                                });
                            }
                        });
                        return;
                    }
                });

                if (funcsToCall.length === 0) {
                    return;
                }

                var fetchCall = function (maxToAdd) {
                    var numAdded = 0;
                    $.each(funcsToCall, function (i, propertyRelationFunc) {
                        if (null != maxToAdd && numAdded >= maxToAdd) {
                            return;
                        }
                        numAdded++;
                        propertyRelationFunc();
                    });
                };

                _this.graph.checkForNodeCap(fetchCall, _this.expansionSet, funcsToCall.length);
            };
        }
        return ConceptCompositionRelationsCallback;
    })(Fetcher.CallbackObject);

    var ConceptChildrenRelationsCallback = (function (_super) {
        __extends(ConceptChildrenRelationsCallback, _super);
        function ConceptChildrenRelationsCallback(graph, url, conceptNode, conceptIdNodeMap, directCallForExpansionType, expansionSet) {
            var _this = this;
            _super.call(this, url, String(conceptNode.nodeId), 1 /* nodesMultiple */); //+":"+directCallForExpansionType);
            this.graph = graph;
            this.conceptNode = conceptNode;
            this.conceptIdNodeMap = conceptIdNodeMap;
            this.directCallForExpansionType = directCallForExpansionType;
            this.expansionSet = expansionSet;
            this.callback = function (relationsDataRaw, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                // CORS enabled GET and POST do though!
                if (jqXHR != null) {
                    if (relationsDataRaw.errors != null) {
                        // We had an error. Handle it.
                        // No relations available? Why not? I'm not sure how to recover.
                        return;
                    }
                }

                // Example: http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F91837002/children
                var childrenToAdd = [];
                $.each(relationsDataRaw.collection, function (index, child) {
                    var childId = _this.graph.computeNodeId(child);
                    _this.graph.manifestOrRegisterImplicitRelation(_this.conceptNode.nodeId, childId, _this.graph.relationLabelConstants.inheritance);
                    if (_this.graph.nodeMayBeExpanded(childId, _this.conceptNode.nodeId, PathOptionConstants.termNeighborhoodConstant, _this.expansionSet)) {
                        childrenToAdd.push(child);
                        return;
                    }
                });

                // Wrap what we want to do, so that it can be controlled by the node-cap dialog system.
                // We can indeed allow these to be asyncrhonously returned to, while still executing the
                // paging fetch seen after this loop
                var groupedFetchCall = function (maxToAdd) {
                    var numAdded = 0;
                    $.each(childrenToAdd, function (index, child) {
                        if (null != maxToAdd && numAdded >= maxToAdd) {
                            return false;
                        }
                        numAdded++;

                        var childId = _this.graph.computeNodeId(child);

                        // Was parsed in ConceptRelationshipJsonParser near line 75 (parseNewChildren)
                        // We have a complication though...paged results! Oh great...
                        // That alone is reason to fire these events separately anyway, but we can keep all the parsing stuck in this same
                        // place and fire off an additional REST call.
                        _this.graph.expandAndParseNodeIfNeeded(childId, _this.conceptNode.nodeId, child, PathOptionConstants.termNeighborhoodConstant, _this.expansionSet, _this.conceptNode.name);
                        _this.graph.manifestOrRegisterImplicitRelation(_this.conceptNode.nodeId, childId, _this.graph.relationLabelConstants.inheritance);
                    });
                };

                // As we loop through children, the dialog will likely increment the count while the user looks at it.
                // if the user stops the expansion via this dialog, the expansion set is told that it was halted,
                // and it should not harass the user with the same question for this expansion.
                _this.graph.checkForNodeCap(groupedFetchCall, _this.expansionSet, childrenToAdd.length);

                // Children paging...only if children called directly?
                var pageNumber = relationsDataRaw["page"];
                var maxPageNumber = relationsDataRaw["pageCount"];
                if (maxPageNumber > pageNumber) {
                    _this.graph.fetchChildren(_this.conceptNode, _this.url, pageNumber + 1, _this.directCallForExpansionType, _this.expansionSet);
                }
            };
        }
        return ConceptChildrenRelationsCallback;
    })(Fetcher.CallbackObject);

    var ConceptParentsRelationsCallback = (function (_super) {
        __extends(ConceptParentsRelationsCallback, _super);
        function ConceptParentsRelationsCallback(graph, url, conceptNode, conceptIdNodeMap, directCallForExpansionType, expansionSet) {
            var _this = this;
            _super.call(this, url, String(conceptNode.nodeId), 1 /* nodesMultiple */); //+":"+directCallForExpansionType);
            this.graph = graph;
            this.conceptNode = conceptNode;
            this.conceptIdNodeMap = conceptIdNodeMap;
            this.directCallForExpansionType = directCallForExpansionType;
            this.expansionSet = expansionSet;
            this.callback = function (relationsDataRaw, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                // CORS enabled GET and POST do though!
                if (jqXHR != null) {
                    if (relationsDataRaw.errors != null) {
                        // We had an error. Handle it.
                        // No relations available? Why not? I'm not sure how to recover.
                        return;
                    }
                }

                var parentsToAdd = [];
                $.each(relationsDataRaw, function (index, parent) {
                    var parentId = _this.graph.computeNodeId(parent);
                    _this.graph.manifestOrRegisterImplicitRelation(parentId, _this.conceptNode.nodeId, _this.graph.relationLabelConstants.inheritance);
                    if (_this.graph.nodeMayBeExpanded(parentId, _this.conceptNode.nodeId, PathOptionConstants.termNeighborhoodConstant, _this.expansionSet)) {
                        parentsToAdd.push(parent);
                        return;
                    }
                });

                // Wrap what we want to do, so that it can be controlled by the node-cap dialog system.
                // We can indeed allow these to be asynchronously returned to, while still executing the
                // paging fetch seen after this loop
                var groupedFetchCall = function (maxToAdd) {
                    var numAdded = 0;
                    $.each(parentsToAdd, function (index, parent) {
                        if (null != maxToAdd && numAdded >= maxToAdd) {
                            return false;
                        }
                        numAdded++;

                        var parentId = _this.graph.computeNodeId(parent);

                        // Save the data in case we expand to include this node
                        _this.graph.expandAndParseNodeIfNeeded(parentId, _this.conceptNode.nodeId, parent, PathOptionConstants.termNeighborhoodConstant, _this.expansionSet, _this.conceptNode.name);
                        _this.graph.manifestOrRegisterImplicitRelation(parentId, _this.conceptNode.nodeId, _this.graph.relationLabelConstants.inheritance);
                    });
                };

                _this.graph.checkForNodeCap(groupedFetchCall, _this.expansionSet, parentsToAdd.length);
            };
        }
        return ConceptParentsRelationsCallback;
    })(Fetcher.CallbackObject);

    var ConceptMappingsRelationsCallback = (function (_super) {
        __extends(ConceptMappingsRelationsCallback, _super);
        function ConceptMappingsRelationsCallback(graph, url, conceptNode, conceptNodeIdMap, directCallForExpansionType, expansionSet) {
            var _this = this;
            _super.call(this, url, String(conceptNode.nodeId), 2 /* links */); //+":"+directCallForExpansionType);
            this.graph = graph;
            this.conceptNode = conceptNode;
            this.conceptNodeIdMap = conceptNodeIdMap;
            this.directCallForExpansionType = directCallForExpansionType;
            this.expansionSet = expansionSet;
            this.callback = function (relationsDataRaw, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                // CORS enabled GET and POST do though!
                if (jqXHR != null) {
                    if (relationsDataRaw.errors != null) {
                        // We had an error. Handle it.
                        // No relations available? Why not? I'm not sure how to recover.
                        return;
                    }
                }

                // We have to collect the mappings to prevent some infinite loops. They can appear multiple times.
                var mappingTargetIds = {};
                var mappingTargets = [];
                var expectedExpansionCount = 0;
                $.each(relationsDataRaw, function (index, mapping) {
                    var firstConceptId = _this.graph.computeNodeId(mapping.classes[0]);
                    var secondConceptId = _this.graph.computeNodeId(mapping.classes[1]);
                    var newConceptId;

                    // Check the ids, grab the one opposite the sourcing concept
                    var newConceptData = undefined;
                    if (_this.conceptNode.nodeId === firstConceptId) {
                        newConceptData = mapping.classes[1];
                        newConceptId = secondConceptId;
                    }
                    if (_this.conceptNode.nodeId === secondConceptId) {
                        newConceptData = mapping.classes[0];
                        newConceptId = firstConceptId;
                    }

                    if (newConceptId === _this.conceptNode.nodeId) {
                        // This data error is not very helpful to see...
                        // console.log("Error: mapping occurred without source as at both endpoints: "+firstConceptId+" and "+secondConceptId+" for call to "+this.url);
                    } else if (newConceptId === undefined || String(newConceptId) === "" || newConceptId === null) {
                        console.log("Error: mapping occurred without source as an endpoint: " + firstConceptId + " and " + secondConceptId + " for source " + _this.conceptNode.nodeId + " for call to " + _this.url);
                    } else {
                        // Sort endpoints to make mapping edges singular. Otherwise we get an edge going each way.
                        var firstId = newConceptId > _this.conceptNode.nodeId ? newConceptId : _this.conceptNode.nodeId;
                        var secondId = newConceptId > _this.conceptNode.nodeId ? _this.conceptNode.nodeId : newConceptId;
                        _this.graph.manifestOrRegisterImplicitRelation(firstId, secondId, _this.graph.relationLabelConstants.mapping);

                        // Catches self referential maps,
                        var newNodeRawUri = _this.graph.computeNodeId(newConceptData);
                        if (null == mappingTargetIds[String(newConceptId)]) {
                            if (_this.graph.nodeMayBeExpanded(newNodeRawUri, _this.conceptNode.nodeId, PathOptionConstants.mappingsNeighborhoodConstant, _this.expansionSet)) {
                                mappingTargetIds[String(newConceptId)] = true;
                                mappingTargets.push(newConceptData);
                                expectedExpansionCount++;
                            }
                        }
                    }
                });

                var fetchCall = function (maxToAdd) {
                    var added = 0;
                    $.each(mappingTargets, function (newConceptId, newConceptData) {
                        if (null != maxToAdd && added >= maxToAdd) {
                            return false;
                        }
                        _this.graph.expandMappedConcept(newConceptId, newConceptData, _this.conceptNode.nodeId, _this.directCallForExpansionType, _this.expansionSet);
                        added++;
                    });
                };

                _this.graph.checkForNodeCap(fetchCall, _this.expansionSet, expectedExpansionCount);
            };
        }
        return ConceptMappingsRelationsCallback;
    })(Fetcher.CallbackObject);
});
