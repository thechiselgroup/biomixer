var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../Utils", "../FetchFromApi", "../GraphView", "../ExpansionSets", "./ExpansionManager", "../TipsyToolTipsOnClick", "./PropertyRelationsExpander", "Utils", "MouseSpinner", "FetchFromApi", "GraphView", "LayoutProvider", "ExpansionSets", "Concepts/ExpansionManager", "UndoRedo/UndoRedoManager", "TipsyToolTipsOnClick", "CompositeExpansionDeletionSet", "Concepts/PropertyRelationsExpander"], function (require, exports, Utils, Fetcher, GraphView, ExpansionSets, ExpansionManager, TipsyToolTipsOnClick, PropRel) {
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
        Node.prototype.getEntityId = function () {
            return String(this.nodeId);
        };
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
                if (haltExpansions || expansionSet.expansionCutShort()) {
                    expansionSet.expansionCutShort(haltExpansions);
                    return;
                }
                else {
                    expansionSet.thunderbirdsAreGo();
                    return callback(maxNodesToGet);
                }
            };
            this.wrappedParseNodeCallbacks.push(expSetUpdateWrapper);
            this.graph.refreshNodeCapDialogNodeCount(expansionSet.getNumberOfNodesMissing());
        };
        DeferredCallbacks.prototype.complete = function (haltExpansions, maxNodesToGet) {
            var i = 0;
            for (i = 0; i < this.wrappedParseNodeCallbacks.length; i++) {
                if (i === maxNodesToGet && !haltExpansions) {
                    break;
                }
                var claimedNodes = this.wrappedParseNodeCallbacks[i](haltExpansions, maxNodesToGet);
                maxNodesToGet -= claimedNodes;
            }
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
            this.conceptIdNodeMap = {};
            this.elementIdNodeMap = {};
            this.nodeMapChanged = false;
            this.ontologiesInGraph = new Array();
            this.relationTypeCssClasses = {
                "is_a": "inheritanceStyleLink",
                "part_of": "compositionStyleLink",
                "maps_to": "mappingStyleLink",
            };
            this.relationLabelConstants = {
                "inheritance": "is_a",
                "composition": "part_of",
                "mapping": "maps_to",
            };
            this.nodeCapInterval = 20;
            this.nextNodeWarningCount = undefined;
            this.deferredParseNodeCallBack = undefined;
            this.times = 0;
            this.currentModalDialogIncomingNodeCount = 0;
            this.currentNodeColor = -1;
            this.nodeOrderedColors = d3.scale.category20().domain([0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]);
            this.ontologyColorMap = {};
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
        ConceptGraph.prototype.nodeIsInIdMap = function (node) {
            return undefined !== this.getNodeByUri(node.nodeId);
        };
        ConceptGraph.prototype.getNodeByUri = function (uri) {
            return this.conceptIdNodeMap[String(uri)];
        };
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
        ConceptGraph.prototype.addNodes = function (newNodes, expansionSet) {
            if (null !== expansionSet) {
                expansionSet.addAll(newNodes);
            }
            for (var i = 0; i < newNodes.length; i++) {
                this.graphD3Format.nodes.push(newNodes[i]);
                this.addNodeToIdMap(newNodes[i]);
            }
            this.graphView.stampTimeGraphModified();
            this.graphView.populateNewGraphElements(this.graphD3Format);
            for (var i = 0; i < newNodes.length; i++) {
                this.manifestEdgesForNewNode(newNodes[i]);
            }
            if (null != expansionSet && null != expansionSet.parentNode) {
                this.manifestEdgesForNewNode(expansionSet.parentNode);
            }
        };
        ConceptGraph.prototype.removeNodes = function (nodesToRemove) {
            TipsyToolTipsOnClick.closeOtherTipsyTooltips();
            this.graphD3Format.nodes = this.graphD3Format.nodes.filter(function (node, index, nodes) {
                return nodesToRemove.indexOf(node) === -1;
            });
            for (var i = 0; i < nodesToRemove.length; i++) {
                this.removeNodeFromIdMap(nodesToRemove[i]);
            }
            this.graphView.stampTimeGraphModified();
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
                return;
            }
            for (var i = 0; i < newEdges.length; i++) {
                if (this.edgeNotInGraph(newEdges[i])) {
                    newEdges[i].source = this.conceptIdNodeMap[String(newEdges[i].sourceId)];
                    newEdges[i].target = this.conceptIdNodeMap[String(newEdges[i].targetId)];
                    this.graphD3Format.links.push(newEdges[i]);
                }
            }
            this.graphView.populateNewGraphEdges(this.graphD3Format.links, temporaryEdges);
        };
        ConceptGraph.prototype.removeEdges = function (edgesToRemove) {
            this.graphD3Format.links = this.graphD3Format.links.filter(function (link, index, links) {
                return edgesToRemove.indexOf(link) === -1;
            });
            this.graphView.stampTimeGraphModified();
            this.graphView.removeMissingGraphElements(this.graphD3Format);
            for (var l = 0; l < edgesToRemove.length; l++) {
                edgesToRemove[l].source = null;
                edgesToRemove[l].target = null;
            }
        };
        ConceptGraph.prototype.removeManifestEdges = function (nodesToRemove) {
            var edgesToDelete = [];
            for (var i = 0; i < nodesToRemove.length; i++) {
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
        ConceptGraph.prototype.getNumberOfPotentialNodesToExpand = function (expandingNode, nodeInteraction) {
            var _this = this;
            var expandingNodeId = expandingNode.nodeId;
            var numNewNodesIncoming = 0;
            var otherCount = 0;
            var edges = this.expMan.edgeRegistry.getEdgesFor(expandingNodeId);
            var nodesSeen = {};
            edges.forEach(function (edge) {
                var edgeExpansionType = edge.relationType === "maps_to" ? PathOptionConstants.mappingsNeighborhoodConstant : PathOptionConstants.termNeighborhoodConstant;
                var otherConceptId = (edge.sourceId === expandingNodeId) ? edge.targetId : edge.sourceId;
                if (_this.nodeMayBeExpanded1(otherConceptId, expandingNodeId, nodeInteraction, edgeExpansionType) && null == nodesSeen[String(otherConceptId)]) {
                    numNewNodesIncoming++;
                    nodesSeen[String(otherConceptId)] = true;
                }
            });
            return numNewNodesIncoming;
        };
        ConceptGraph.prototype.checkForNodeCap = function (fetchCallback, expansionSet, numberNewNodesComing) {
            if (0 === numberNewNodesComing) {
                return;
            }
            if (expansionSet.expansionCutShort()) {
                return;
            }
            if (undefined === this.nextNodeWarningCount) {
                this.nextNodeWarningCount = this.softNodeCap;
            }
            while (this.graphD3Format.nodes.length < (this.nextNodeWarningCount - this.nodeCapInterval) && this.softNodeCap < this.nextNodeWarningCount) {
                this.nextNodeWarningCount -= this.nodeCapInterval;
                this.nextNodeWarningCount = Math.max(this.nextNodeWarningCount, this.softNodeCap);
            }
            var dialogOpen = $("#confirm").is(":visible");
            if (dialogOpen || ((this.graphD3Format.nodes.length + 1) > this.nextNodeWarningCount) || ((this.graphD3Format.nodes.length + numberNewNodesComing) > this.nextNodeWarningCount)) {
                if (undefined === this.deferredParseNodeCallBack) {
                    this.deferredParseNodeCallBack = new DeferredCallbacks(this);
                }
                this.deferredParseNodeCallBack.addCallback(fetchCallback, expansionSet);
                this.showNodeCapDialog(numberNewNodesComing);
            }
            else if (!dialogOpen) {
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
            }
            else {
                $("input#capDialogInput").attr("max", numberNewNodesComing + "");
                this.currentModalDialogIncomingNodeCount = numberNewNodesComing;
            }
        };
        ConceptGraph.prototype.showNodeCapDialog = function (numberNewNodesComing) {
            this.currentModalDialogIncomingNodeCount = 0;
            var outerThis = this;
            $('#confirm').modal({
                closeHTML: "<a href='#' title='Close' class='modal-close'>x</a>",
                position: ["20%",],
                overlayId: 'confirm-overlay',
                containerId: 'confirm-container',
                onShow: function (dialog) {
                    var modal = this;
                    var message = $("<div>").attr("id", "nodeCapDialogMessage").css("white-space", "pre-wrap");
                    $("#confirm-container").css("height", "auto");
                    $('.message', dialog.data[0]).append(message);
                    $("div.buttons").css("width", "auto");
                    var nodesToLoadInput = $("<input>").attr("id", "capDialogInput").attr("name", "capDialogInput").attr({ "type": "number", "min": "0", "max": numberNewNodesComing + "", }).css("width", "4em").change(function () {
                        $(".yes").text("Add " + $("#capDialogInput").val());
                    }).keyup(function () {
                        $(".yes").text("Add " + $("#capDialogInput").val());
                    }).val(outerThis.nodeCapInterval + "");
                    var nodesToLoadLabel = $("<label>").attr("id", "capDialogLabel").attr("for", "capDialogInput");
                    outerThis.refreshNodeCapDialogNodeCount(numberNewNodesComing);
                    $('.yes', dialog.data[0]).click(function () {
                        modal.close();
                        var nodesToAdd = parseInt(nodesToLoadInput.val(), 10);
                        outerThis.nodeCapResponseCallback(false, nodesToAdd);
                    });
                    $('.no', dialog.data[0]).click(function () {
                        modal.close();
                        outerThis.nodeCapResponseCallback(true, 0);
                    });
                    $(".yes").before(nodesToLoadLabel);
                    nodesToLoadLabel.after(nodesToLoadInput);
                    $(".yes").text("Add " + outerThis.nodeCapInterval);
                    $(".no").text("Stop");
                    $("div.buttons").css("padding", "0px 5px 5px 0px");
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
            }
            else {
                ontologyUri = conceptData;
            }
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
                return this.conceptIdNodeMap[String(nodeId)];
            }
            var prefLabel = conceptData.prefLabel;
            if (prefLabel == null) {
                console.log("Missing prefLabel for concept: id=" + conceptData["@id"] + " (link = " + conceptData.links.self + ")");
                prefLabel = "<no prefLabel>";
            }
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
            conceptNode.tempDepth = 0;
            conceptNode.fixed = false;
            conceptNode.ontologyAcronym = ontologyAcronym;
            conceptNode.ontologyUri = conceptData.links.ontology;
            conceptNode.ontologyUriForIds = encodeURIComponent(conceptNode.ontologyUri);
            conceptNode.nodeColor = this.nextNodeColor(conceptNode.ontologyAcronym);
            this.addNodes([conceptNode], expansionSet);
            return conceptNode;
        };
        ConceptGraph.prototype.expandMappedConcept = function (newConceptId, newConceptMappingData, relatedConceptId, expansionType, expansionSet) {
            var newNodeUri = this.computeNodeId(newConceptMappingData);
            if (expansionType === PathOptionConstants.mappingsNeighborhoodConstant && this.nodeMayBeExpanded(newNodeUri, relatedConceptId, expansionType, expansionSet)) {
                var url = newConceptMappingData.links.self;
                var callback = new FetchOneConceptCallback(this, url, newNodeUri, expansionSet);
                var fetcher = new Fetcher.RetryingJsonFetcher(url);
                fetcher.fetch(callback, true);
            }
        };
        ConceptGraph.prototype.expandRelatedConcept = function (conceptsOntology, newConceptId, relatedConceptId, expansionType, expansionSet) {
            var newNodeId = this.computeNodeId(newConceptId, conceptsOntology);
            if (this.nodeMayBeExpanded(newNodeId, relatedConceptId, expansionType, expansionSet)) {
                var url = this.buildConceptUrlNewApi(conceptsOntology, newConceptId);
                var callback = new FetchOneConceptCallback(this, url, newNodeId, expansionSet);
                var fetcher = new Fetcher.RetryingJsonFetcher(url);
                fetcher.fetch(callback, true);
            }
        };
        ConceptGraph.prototype.addNodeToGraph = function (newConceptId) {
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
        ConceptGraph.prototype.nodeIsAccessible = function (newConceptId) {
            return !this.expMan.nodeIsInaccessible(newConceptId);
        };
        ConceptGraph.prototype.expandAndParseNodeIfNeeded = function (newConceptId, relatedConceptId, conceptPropertiesData, expansionType, expansionSet, parentName) {
            if (this.nodeMayBeExpanded(newConceptId, relatedConceptId, expansionType, expansionSet) && this.nodeIsAccessible(newConceptId)) {
                if (!(conceptPropertiesData === undefined) && Object.keys(conceptPropertiesData).length > 0 && expansionType !== PathOptionConstants.mappingsNeighborhoodConstant) {
                    var conceptNode = this.parseNode(undefined, conceptPropertiesData, expansionSet);
                    if (null == conceptNode) {
                        return;
                    }
                    this.fetchConceptRelations(conceptNode, conceptPropertiesData, expansionSet);
                }
                else {
                    console.log("Error: no data passed to expansion and parsing method");
                }
            }
        };
        ConceptGraph.prototype.manifestOrRegisterImplicitRelation = function (parentIdUri, childIdUri, relationId, relationProperty) {
            if (parentIdUri === childIdUri) {
                return;
            }
            if (!this.nodeIsAccessible(childIdUri) || !this.nodeIsAccessible(parentIdUri)) {
                return;
            }
            var edge = new Link();
            edge.sourceId = parentIdUri;
            edge.targetId = childIdUri;
            edge.rawId = edge.sourceId + "-to-" + edge.targetId + "-of-" + relationId;
            edge.relationType = relationId;
            if (relationId === this.relationLabelConstants.inheritance || relationId === this.relationLabelConstants.mapping) {
                edge.edgePositionSlot = 0;
            }
            else if (relationId === this.relationLabelConstants.composition) {
                edge.edgePositionSlot = 1;
            }
            else {
                var numExistingEdgesBetweenPair = this.expMan.edgeRegistry.getEdgesFor(edge.sourceId, edge.targetId).length;
                edge.edgePositionSlot = numExistingEdgesBetweenPair + 2;
            }
            if (relationProperty === undefined) {
                edge.relationLabel = relationId;
            }
            else {
                if (undefined === relationProperty.label) {
                    var idSections = relationId.split("__");
                    edge.relationLabel = idSections[idSections.length - 1];
                }
                else {
                    edge.relationLabel = relationProperty.label;
                }
                edge.relationSpecificToOntologyAcronym = relationProperty.ontologyAcronym;
            }
            edge.id = Utils.escapeIdentifierForId(edge.sourceId) + "-to-" + Utils.escapeIdentifierForId(edge.targetId) + "-of-" + relationId;
            edge.value = 1;
            var preExistingEdge = this.registerImplicitEdge(edge);
            if (null !== preExistingEdge) {
                return;
            }
            if (this.isEdgeForTemporaryRenderOnly(edge)) {
                return;
            }
            this.manifestEdge([edge], false);
        };
        ConceptGraph.prototype.registerImplicitEdge = function (edge) {
            return this.expMan.edgeRegistry.addEdgeToRegistry(edge, this);
        };
        ConceptGraph.prototype.manifestEdge = function (edges, allowTemporary) {
            var _this = this;
            var edgesToRender = [];
            $.each(edges, function (index, edge) {
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
                }
                else {
                    return link.relationType !== _this.relationLabelConstants.mapping;
                }
            });
        };
        ConceptGraph.prototype.isEdgeForTemporaryRenderOnly = function (edge) {
            if (edge.relationType === this.relationLabelConstants.mapping) {
                if (this.expMan.wasConceptClearedForExpansion(edge.sourceId, PathOptionConstants.mappingsNeighborhoodConstant) || this.expMan.wasConceptClearedForExpansion(edge.targetId, PathOptionConstants.mappingsNeighborhoodConstant)) {
                    return false;
                }
                else if (this.hasNonMappingEdgeAdjacent(edge.sourceId) && this.expMan.edgeRegistry.getEdgesFor(edge.targetId)) {
                    return false;
                }
                else {
                    return true;
                }
            }
            return false;
        };
        ConceptGraph.prototype.manifestTemporaryHoverEdges = function (conceptNode) {
            var _this = this;
            var temporaryEdges = [];
            var nodeEdges = this.expMan.edgeRegistry.getEdgesFor(conceptNode.nodeId);
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
        ConceptGraph.prototype.fetchPathToRoot = function (centralOntologyAcronym, centralConceptUri, expansionSet, initSet) {
            var pathsToRootUrl = this.buildPathToRootUrlNewApi(centralOntologyAcronym, centralConceptUri);
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
            var centralConceptUrl = this.buildConceptUrlNewApi(nodeData.ontologyAcronym, nodeData.simpleConceptUri);
            var centralCallback = new FetchConceptRelationsCallback(this, centralConceptUrl, nodeData, PathOptionConstants.mappingsNeighborhoodConstant, expansionSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(centralConceptUrl);
            fetcher.fetch(centralCallback, true);
        };
        ConceptGraph.prototype.fetchTermNeighborhood = function (centralOntologyAcronym, centralConceptUri, expansionSet, initSet) {
            var centralConceptUrl = this.buildConceptUrlNewApi(centralOntologyAcronym, centralConceptUri);
            var centralCallback = new FetchTargetConceptCallback(this, centralConceptUrl, centralConceptUri, PathOptionConstants.termNeighborhoodConstant, expansionSet, initSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(centralConceptUrl);
            fetcher.fetch(centralCallback, true);
        };
        ConceptGraph.prototype.fetchMappingsNeighborhood = function (centralOntologyAcronym, centralConceptUri, expansionSet, initSet) {
            var centralConceptUrl = this.buildConceptUrlNewApi(centralOntologyAcronym, centralConceptUri);
            var centralCallback = new FetchTargetConceptCallback(this, centralConceptUrl, centralConceptUri, PathOptionConstants.mappingsNeighborhoodConstant, expansionSet, initSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(centralConceptUrl);
            fetcher.fetch(centralCallback, true);
        };
        ConceptGraph.prototype.fetchConceptRelations = function (conceptNode, conceptData, expansionSet, directCallForExpansionType) {
            this.fetchChildren(conceptNode, conceptData.links.children, 1, directCallForExpansionType, expansionSet);
            this.fetchParents(conceptNode, conceptData.links.parents, directCallForExpansionType, expansionSet);
            this.fetchMappings(conceptNode, conceptData.links.mappings, directCallForExpansionType, expansionSet);
            this.fetchCompositionRelations(conceptNode, directCallForExpansionType, expansionSet);
        };
        ConceptGraph.prototype.fetchChildren = function (conceptNode, relationsUrl, pageRequested, directCallForExpansionType, expansionSet) {
            var giveUserBusyIndicator;
            if (directCallForExpansionType === PathOptionConstants.termNeighborhoodConstant) {
                giveUserBusyIndicator = true;
            }
            else {
                giveUserBusyIndicator = false;
            }
            relationsUrl = Utils.addOrUpdateUrlParameter(relationsUrl, "page", pageRequested + "");
            var conceptRelationsCallback = new ConceptChildrenRelationsCallback(this, relationsUrl, conceptNode, this.conceptIdNodeMap, directCallForExpansionType, expansionSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(relationsUrl);
            fetcher.fetch(conceptRelationsCallback, giveUserBusyIndicator);
        };
        ConceptGraph.prototype.fetchParents = function (conceptNode, relationsUrl, directCallForExpansionType, expansionSet) {
            var giveUserBusyIndicator;
            if (directCallForExpansionType === PathOptionConstants.termNeighborhoodConstant) {
                giveUserBusyIndicator = true;
            }
            else {
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
            }
            else {
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
            }
            else {
                giveUserBusyIndicator = false;
            }
            var relationsUrl = this.buildConceptCompositionsRelationUrl(conceptNode);
            var conceptRelationsCallback = new ConceptCompositionRelationsCallback(this, relationsUrl, conceptNode, this.conceptIdNodeMap, directCallForExpansionType, expansionSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(relationsUrl);
            fetcher.fetch(conceptRelationsCallback, giveUserBusyIndicator);
        };
        ConceptGraph.prototype.buildPathToRootUrlNewApi = function (centralOntologyAcronym, centralConceptUri) {
            return "http://" + Utils.getBioportalUrl() + "/ontologies/" + centralOntologyAcronym + "/classes/" + encodeURIComponent(String(centralConceptUri)) + "/paths_to_root/";
        };
        ConceptGraph.prototype.buildTermNeighborhoodUrlNewApi = function (centralOntologyAcronym, centralConceptUri) {
            return this.buildConceptUrlNewApi(centralOntologyAcronym, centralConceptUri);
        };
        ConceptGraph.prototype.buildMappingsNeighborhoodUrlNewApi = function (centralOntologyAcronym, centralConceptUri) {
            return "http://" + Utils.getBioportalUrl() + "/ontologies/" + centralOntologyAcronym + "/classes/" + encodeURIComponent(String(centralConceptUri)) + "/mappings/";
        };
        ConceptGraph.prototype.buildConceptUrlNewApi = function (ontologyAcronym, conceptUri) {
            return this.buildConceptCompositionsRelationUrl(null, ontologyAcronym, conceptUri);
        };
        ConceptGraph.prototype.buildConceptSearchUrlNewApi = function (conceptUri) {
            return "http://" + Utils.getBioportalUrl() + "/search/?require_exact_match=true&also_search_properties=false&q=" + encodeURIComponent(String(conceptUri));
        };
        ConceptGraph.prototype.buildConceptCompositionsRelationUrl = function (concept, ontologyAcronym, conceptUri) {
            if (null != concept) {
                ontologyAcronym = concept.ontologyAcronym;
                conceptUri = concept.simpleConceptUri;
            }
            return "http://" + Utils.getBioportalUrl() + "/ontologies/" + ontologyAcronym + "/classes/" + encodeURIComponent(String(conceptUri)) + "?include=properties,definition,synonym,prefLabel";
        };
        ConceptGraph.prototype.buildBatchRelationUrl = function (concept) {
            return "http://" + Utils.getBioportalUrl() + "/ontologies/" + concept.ontologyAcronym + "/classes/" + concept.conceptUriForIds + "?include=children,parents,mappings,properties";
        };
        ConceptGraph.prototype.buildBatchRelationUrlAndPostData = function (concepts) {
            var url = "http://" + Utils.getBioportalUrl() + "/batch/";
            var classCollection = [];
            var postObject = {
                "http://www.w3.org/2002/07/owl#Class": {
                    "collection": classCollection
                },
                "include": "children, parents, mappings, properties",
            };
            $.each(concepts, function (i, d) {
                classCollection.push({
                    "class": d.id,
                    "ontology": d.ontologyUri,
                });
            });
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
                "data": postObject,
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
            return d3.lab(outerColor).brighter(1).toString();
        };
        ConceptGraph.prototype.darkenColor = function (outerColor) {
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
            _super.call(this, url, String(centralOntologyAcronym) + ":" + String(centralConceptUri), Fetcher.CallbackVarieties.nodesMultiple);
            this.graph = graph;
            this.centralOntologyAcronym = centralOntologyAcronym;
            this.centralConceptUri = centralConceptUri;
            this.expansionSet = expansionSet;
            this.initSet = initSet;
            this.callback = function (pathsToRootData, textStatus, jqXHR) {
                if (jqXHR != null) {
                    if (pathsToRootData.errors != null) {
                        console.log("Failed to load paths to root: " + _this.centralConceptUri);
                        return;
                    }
                }
                var numberOfConcepts = Object.keys(pathsToRootData).length;
                var newNodesForExpansionGraph = {};
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
                        var currentNode = newNodesForExpansionGraph[newNodeId];
                        var parentIndex = conceptIndex + 1;
                        if (parentIndex < pathsToRootData[pathIndex].length) {
                            var parentData = pathsToRootData[pathIndex][parentIndex];
                            var parentNodeId = String(_this.graph.computeNodeId(parentData));
                            var parentNode = newNodesForExpansionGraph[parentNodeId];
                            var conceptRelationsCallback = new ConceptParentsRelationsCallback(_this.graph, "", parentNode, _this.graph.conceptIdNodeMap, PathOptionConstants.pathsToRootConstant, _this.expansionSet);
                            conceptRelationsCallback.callback([nodeData], textStatus, jqXHR);
                        }
                    }
                }
                for (var nodeId in newNodesForExpansionGraph) {
                    var node = newNodesForExpansionGraph[nodeId];
                    var data = collapsedPathsToRootData[node.getEntityId()];
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
            _super.call(this, url, String(conceptUri), Fetcher.CallbackVarieties.nodeSingle);
            this.graph = graph;
            this.conceptUri = conceptUri;
            this.directCallForExpansionType = directCallForExpansionType;
            this.expansionSet = expansionSet;
            this.initSet = initSet;
            this.callback = function (conceptPropertiesData, textStatus, jqXHR) {
                if (jqXHR != null) {
                    if (conceptPropertiesData.errors != null) {
                        console.log("Failed to load target node: " + _this.conceptUri);
                        return;
                    }
                }
                var conceptNode = _this.graph.parseNode(undefined, conceptPropertiesData, _this.expansionSet);
                if (null == conceptNode) {
                    return;
                }
                _this.expansionSet.parentNode = conceptNode;
                if (_this.initSet !== null) {
                    _this.initSet.updateExpansionNodeDisplayName(conceptNode.name + " (" + conceptNode.ontologyAcronym + ")");
                }
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
            _super.call(this, url, String(conceptUri), Fetcher.CallbackVarieties.nodeSingle);
            this.graph = graph;
            this.conceptUri = conceptUri;
            this.expansionSet = expansionSet;
            this.directCallForExpansionType = PathOptionConstants.singleNodeOrSubordinateConstant;
            this.callback = function (conceptPropertiesData, textStatus, jqXHR) {
                if (jqXHR != null) {
                    if (conceptPropertiesData.errors != null) {
                        _this.graph.expMan.purgeInaccessibleNode(_this.conceptUri);
                        return;
                    }
                }
                var fetchCall = function () {
                    var conceptNode = _this.graph.parseNode(undefined, conceptPropertiesData, _this.expansionSet);
                    if (null == conceptNode) {
                        return;
                    }
                    _this.graph.fetchConceptRelations(conceptNode, conceptPropertiesData, _this.expansionSet);
                };
                fetchCall();
            };
        }
        return FetchOneConceptCallback;
    })(Fetcher.CallbackObject);
    exports.FetchOneConceptCallback = FetchOneConceptCallback;
    var SearchOneConceptCallback = (function (_super) {
        __extends(SearchOneConceptCallback, _super);
        function SearchOneConceptCallback(graph, url, conceptUri, priorityLoadNoCapCheck) {
            var _this = this;
            if (priorityLoadNoCapCheck === void 0) { priorityLoadNoCapCheck = false; }
            _super.call(this, url, String(conceptUri), Fetcher.CallbackVarieties.nodeSingle);
            this.graph = graph;
            this.conceptUri = conceptUri;
            this.priorityLoadNoCapCheck = priorityLoadNoCapCheck;
            this.directCallForExpansionType = PathOptionConstants.singleNodeConstant;
            this.callback = function (conceptMatchData, textStatus, jqXHR) {
                if (jqXHR != null) {
                    if (conceptMatchData.errors != null) {
                        return;
                    }
                }
                var conceptPropertiesData = [];
                for (var i in conceptMatchData.collection) {
                    var hit = conceptMatchData.collection[i];
                    if (hit.matchType === "id" && hit["@id"] === _this.conceptUri) {
                        conceptPropertiesData.push(hit);
                    }
                }
                if (conceptPropertiesData.length == 0) {
                    alert("Failed to import node for provided id: '" + _this.conceptUri + "'");
                    return;
                }
                else {
                    var expId = new ExpansionSets.ExpansionSetIdentifer("arbitraryConceptAddition_" + Utils.escapeIdentifierForId(_this.conceptUri), "Added Arbitrary Node");
                    var expansionSet = new ExpansionSets.ExpansionSet(expId, null, _this.graph, _this.graph.expMan.getActiveExpansionSets(), _this.graph.undoBoss, PathOptionConstants.singleNodeConstant);
                    var lastConceptNode;
                    var lastConceptNodeData;
                    var fetchCall = function (maxNodesToGet) {
                        for (var j = 0; j < conceptPropertiesData.length; j++) {
                            if (j >= maxNodesToGet) {
                                break;
                            }
                            lastConceptNodeData = conceptPropertiesData[j];
                            var node = _this.addNode(lastConceptNodeData, expansionSet);
                            if (null !== node) {
                                lastConceptNode = node;
                            }
                        }
                        if (expansionSet.nodes.length === 1) {
                            expansionSet.id.setDisplayId(expansionSet.id.getDisplayId() + " (" + lastConceptNode.ontologyAcronym + ")");
                        }
                        else {
                            expansionSet.id.setDisplayId(expansionSet.id.getDisplayId() + " (multiple ontologies)");
                        }
                        return j;
                    };
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
            this.graph.fetchConceptRelations(conceptNode, conceptPropertiesData, expansionSet);
            return conceptNode;
        };
        return SearchOneConceptCallback;
    })(Fetcher.CallbackObject);
    exports.SearchOneConceptCallback = SearchOneConceptCallback;
    var FetchConceptRelationsCallback = (function (_super) {
        __extends(FetchConceptRelationsCallback, _super);
        function FetchConceptRelationsCallback(graph, url, node, directCallForExpansionType, expansionSet) {
            var _this = this;
            _super.call(this, url, String(node.nodeId), Fetcher.CallbackVarieties.links);
            this.graph = graph;
            this.node = node;
            this.directCallForExpansionType = directCallForExpansionType;
            this.expansionSet = expansionSet;
            this.callback = function (conceptPropertiesData, textStatus, jqXHR) {
                if (jqXHR != null) {
                    if (conceptPropertiesData.errors != null) {
                        return;
                    }
                }
                _this.graph.fetchConceptRelations(_this.node, conceptPropertiesData, _this.expansionSet, _this.directCallForExpansionType);
            };
        }
        return FetchConceptRelationsCallback;
    })(Fetcher.CallbackObject);
    var ConceptCompositionRelationsCallback = (function (_super) {
        __extends(ConceptCompositionRelationsCallback, _super);
        function ConceptCompositionRelationsCallback(graph, url, conceptNode, conceptNodeIdMap, directCallForExpansionType, expansionSet) {
            var _this = this;
            _super.call(this, url, String(conceptNode.nodeId), Fetcher.CallbackVarieties.links);
            this.graph = graph;
            this.conceptNode = conceptNode;
            this.conceptNodeIdMap = conceptNodeIdMap;
            this.directCallForExpansionType = directCallForExpansionType;
            this.expansionSet = expansionSet;
            this.callback = function (relationsDataRaw, textStatus, jqXHR) {
                if (jqXHR != null) {
                    if (relationsDataRaw.errors != null) {
                        return;
                    }
                }
                var outerThis = _this;
                if (!PropRel.OntologyPropertyRelationsRegistry.contains(_this.conceptNode.ontologyAcronym)) {
                    PropRel.OntologyPropertyRelationsRegistry.fetchOntologyPropertyRelations(_this.conceptNode, function () {
                        _this.callback(relationsDataRaw, textStatus, jqXHR);
                    });
                    return;
                }
                var funcsToCall = [];
                $.each(relationsDataRaw.properties, function (propertyId, propertyValue) {
                    if (Utils.endsWith(propertyId, "treeView")) {
                        return;
                    }
                    var matchedRelationProp = PropRel.OntologyPropertyRelationsRegistry.matchedAvailableRelations(_this.conceptNode.ontologyAcronym, propertyId);
                    if (matchedRelationProp !== undefined) {
                        $.each(propertyValue, function (i, relatedPartId) {
                            if (relatedPartId.indexOf("http") !== 0) {
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
                    if (Utils.endsWith(propertyId, "has_part")) {
                        $.each(propertyValue, function (index, childPartId) {
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
                    return numAdded;
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
            _super.call(this, url, String(conceptNode.nodeId), Fetcher.CallbackVarieties.nodesMultiple);
            this.graph = graph;
            this.conceptNode = conceptNode;
            this.conceptIdNodeMap = conceptIdNodeMap;
            this.directCallForExpansionType = directCallForExpansionType;
            this.expansionSet = expansionSet;
            this.callback = function (relationsDataRaw, textStatus, jqXHR) {
                if (jqXHR != null) {
                    if (relationsDataRaw.errors != null) {
                        return;
                    }
                }
                var childrenToAdd = [];
                $.each(relationsDataRaw.collection, function (index, child) {
                    var childId = _this.graph.computeNodeId(child);
                    _this.graph.manifestOrRegisterImplicitRelation(_this.conceptNode.nodeId, childId, _this.graph.relationLabelConstants.inheritance);
                    if (_this.graph.nodeMayBeExpanded(childId, _this.conceptNode.nodeId, PathOptionConstants.termNeighborhoodConstant, _this.expansionSet)) {
                        childrenToAdd.push(child);
                        return;
                    }
                });
                var groupedFetchCall = function (maxToAdd) {
                    var numAdded = 0;
                    $.each(childrenToAdd, function (index, child) {
                        if (null != maxToAdd && numAdded >= maxToAdd) {
                            return false;
                        }
                        numAdded++;
                        var childId = _this.graph.computeNodeId(child);
                        _this.graph.expandAndParseNodeIfNeeded(childId, _this.conceptNode.nodeId, child, PathOptionConstants.termNeighborhoodConstant, _this.expansionSet, _this.conceptNode.name);
                        _this.graph.manifestOrRegisterImplicitRelation(_this.conceptNode.nodeId, childId, _this.graph.relationLabelConstants.inheritance);
                    });
                    return numAdded;
                };
                _this.graph.checkForNodeCap(groupedFetchCall, _this.expansionSet, childrenToAdd.length);
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
            _super.call(this, url, String(conceptNode.nodeId), Fetcher.CallbackVarieties.nodesMultiple);
            this.graph = graph;
            this.conceptNode = conceptNode;
            this.conceptIdNodeMap = conceptIdNodeMap;
            this.directCallForExpansionType = directCallForExpansionType;
            this.expansionSet = expansionSet;
            this.callback = function (relationsDataRaw, textStatus, jqXHR) {
                if (jqXHR != null) {
                    if (relationsDataRaw.errors != null) {
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
                var groupedFetchCall = function (maxToAdd) {
                    var numAdded = 0;
                    $.each(parentsToAdd, function (index, parent) {
                        if (null != maxToAdd && numAdded >= maxToAdd) {
                            return false;
                        }
                        numAdded++;
                        var parentId = _this.graph.computeNodeId(parent);
                        _this.graph.expandAndParseNodeIfNeeded(parentId, _this.conceptNode.nodeId, parent, PathOptionConstants.termNeighborhoodConstant, _this.expansionSet, _this.conceptNode.name);
                        _this.graph.manifestOrRegisterImplicitRelation(parentId, _this.conceptNode.nodeId, _this.graph.relationLabelConstants.inheritance);
                    });
                    return numAdded;
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
            _super.call(this, url, String(conceptNode.nodeId), Fetcher.CallbackVarieties.links);
            this.graph = graph;
            this.conceptNode = conceptNode;
            this.conceptNodeIdMap = conceptNodeIdMap;
            this.directCallForExpansionType = directCallForExpansionType;
            this.expansionSet = expansionSet;
            this.callback = function (relationsDataRaw, textStatus, jqXHR) {
                if (jqXHR != null) {
                    if (relationsDataRaw.errors != null) {
                        return;
                    }
                }
                var mappingTargetIds = {};
                var mappingTargets = [];
                var expectedExpansionCount = 0;
                $.each(relationsDataRaw, function (index, mapping) {
                    var firstConceptId = _this.graph.computeNodeId(mapping.classes[0]);
                    var secondConceptId = _this.graph.computeNodeId(mapping.classes[1]);
                    var newConceptId;
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
                    }
                    else if (newConceptId === undefined || String(newConceptId) === "" || newConceptId === null) {
                        console.log("Error: mapping occurred without source as an endpoint: " + firstConceptId + " and " + secondConceptId + " for source " + _this.conceptNode.nodeId + " for call to " + _this.url);
                    }
                    else {
                        var firstId = newConceptId > _this.conceptNode.nodeId ? newConceptId : _this.conceptNode.nodeId;
                        var secondId = newConceptId > _this.conceptNode.nodeId ? _this.conceptNode.nodeId : newConceptId;
                        _this.graph.manifestOrRegisterImplicitRelation(firstId, secondId, _this.graph.relationLabelConstants.mapping);
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
                    return added;
                };
                _this.graph.checkForNodeCap(fetchCall, _this.expansionSet, expectedExpansionCount);
            };
        }
        return ConceptMappingsRelationsCallback;
    })(Fetcher.CallbackObject);
});
