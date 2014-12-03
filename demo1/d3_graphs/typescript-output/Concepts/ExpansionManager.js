///<reference path="headers/require.d.ts" />
define(["require", "exports", "../GraphModifierCommand", "Concepts/ConceptGraph", "UndoRedo/UndoRedoManager", "GraphModifierCommand", "ExpansionSets"], function(require, exports, GraphModifierCommand) {
    var ExpansionManager = (function () {
        function ExpansionManager(undoBoss) {
            this.undoBoss = undoBoss;
            this.edgeRegistry = new EdgeRegistry();
        }
        /**
        * This is used to determine things like if a given mapping arc is to be fully rendered,
        * or if the neighbors of a term we are processing should be fetched. That depends on hwo
        * we got the term to begin with. That context is available in the undo stack of expansions,
        * which works well when we have undone or redone them, and works better than a previous
        * system of explicit whitelisting for expansions.
        *
        * If this returns true, the node in question is allowed to fetch and add related nodes within the expansion
        * type specified. Those nodes do not (normally) inherit this property.
        *
        * If the paths to root functionality were not fulfilled via a special REST call, that system would allow
        * expanded parent nodes to inherit this privelege and pass it on to their parents.
        *
        * The optional flag should really only be used internally by the class, but change the method semantics to
        * determine if the expansion set associated with the node is currently fully loaded into the graph.
        */
        ExpansionManager.prototype.wasConceptClearedForExpansion = function (conceptUri, expansionType) {
            return this.findConceptExpansionSetInHistory(conceptUri, expansionType).cleared;
        };

        ExpansionManager.prototype.findConceptExpansionSetInHistory = function (conceptUri, expansionType) {
            var returnVal = { cleared: false, fullyManifested: false, numTotal: -1, numMissing: -1 };
            var crumbTrail = this.undoBoss.getCrumbHistory();
            var conceptUriForIds = String(conceptUri);
            for (var i = crumbTrail.length - 1; i >= 0; i--) {
                var command = crumbTrail[i];

                // Get the interaction that the crumb command had with the node in question.
                // Could match the expansion type provided, be an addition, or be a deletion.
                var nodeInteraction = command.nodeInteraction(conceptUriForIds);
                if (nodeInteraction === expansionType) {
                    // This is done for a different use case, which requires all of the same code except this.
                    returnVal.fullyManifested = command.areCommandNodesCurrentlyLoaded();

                    // The way that the command is set as cut short really matters a lot to this functionality.
                    // At this time, it occurs when the number of nodes allowed in the expansion cap dialog is
                    // less than the total available to expand.
                    returnVal.cleared = true; //!command.commandCutShort();
                    returnVal.numTotal = command.numberOfNodesInCommand();
                    returnVal.numMissing = returnVal.numTotal - command.numberOfCommandNodesCurrentlyLoaded();
                    return returnVal;
                } else if (nodeInteraction === GraphModifierCommand.GraphRemoveNodesCommand.deletionNodeInteraction) {
                    // For deleted, labelling it as not cleared is the important part
                    // but we'll fill in all the things.
                    returnVal.fullyManifested = false;
                    returnVal.cleared = false;
                    returnVal.numTotal = command.numberOfNodesInCommand();
                    returnVal.numMissing = returnVal.numTotal - command.numberOfCommandNodesCurrentlyLoaded();
                    return returnVal;
                }
            }
            return returnVal;
        };

        ExpansionManager.prototype.isConceptExpansionSetFullyManifested = function (conceptUri, expansionType) {
            return this.findConceptExpansionSetInHistory(conceptUri, expansionType);
        };

        /**
        * Collect all expansion sets that are from the current undo level backwards.
        * Do not return ones that are empty (expansions that resulted in no nodes being added),
        * and do not return expansion sets for which no node is rendered currently.
        */
        ExpansionManager.prototype.getActiveExpansionSets = function () {
            var expansionSets = new Array();
            var history = this.undoBoss.getCrumbHistory();
            this.recursiveExpansionSets(history, expansionSets);
            return expansionSets;
        };

        ExpansionManager.prototype.recursiveExpansionSets = function (commands, expansionSets) {
            for (var i = commands.length - 1; i >= 0; i--) {
                var command = commands[i];
                if (command instanceof GraphModifierCommand.GraphAddNodesCommand) {
                    var expansionSet = command.expansionSet;
                    if (expansionSet.nodes.length > 0 && expansionSet.getNumberOfNodesCurrentlyInGraph() > 0) {
                        expansionSets.push(expansionSet);
                    }
                } else if (command instanceof GraphModifierCommand.GraphCompositeNodeCommand) {
                    var moreCommands = command.commands;
                    this.recursiveExpansionSets(moreCommands, expansionSets);
                }
            }
        };

        ExpansionManager.prototype.getExpansionSetsThatNodeIsParentOf = function (node) {
            var parentageSets = new Array();
            var allSets = this.getActiveExpansionSets();
            for (var i = 0; i < allSets.length; i++) {
                var parentSet = allSets[i];
                if (parentSet.parentNode === node) {
                    parentageSets.push(parentSet);
                }
            }
            return parentageSets;
        };

        ExpansionManager.prototype.getExpansionSetsThatNodeIsChildOf = function (node) {
            var childSets = new Array();
            var allSets = this.getActiveExpansionSets();
            for (var i = 0; i < allSets.length; i++) {
                var childSet = allSets[i];
                if (childSet.nodes.indexOf(node) != -1) {
                    childSets.push(childSet);
                }
            }
            return childSets;
        };
        return ExpansionManager;
    })();
    exports.ExpansionManager = ExpansionManager;

    var EdgeRegistry = (function () {
        function EdgeRegistry() {
            // NB We cannot simplify this away by adding edges directly to node objects, since node objects are not instantiated when
            // edges are discovered, but we can associate them with node ids.
            // Maps conceptIds not present in the graph to concept ids in the graph for which an edge exists.
            // To track edges that we know about that haven't yet been added to the graph (by node not yet in graph and node in graph and type).
            // Edges must be tracked before they are valid, or else a superfluous amount of REST calls would be needed.
            // Need to always keep these edges, and to map by both source and target. We only get them when parsing, and we don't re-parse
            // when we have stored objects encapsulating previous data.
            // Made registry flat; used to be more structured, but it complicated without fulfilled requirements for said structure.
            this.twoWayEdgeRegistry = {};
        }
        /**
        * If the edge is already represented in the registry, it is returned, and the instance passed in will
        * not be added to the registry.
        */
        EdgeRegistry.prototype.addEdgeToRegistry = function (edge) {
            // Assumes only mapping edge types. Change that if things change, right? ;)
            var sourceStr = String(edge.sourceId);
            var targetStr = String(edge.targetId);

            // Source oriented
            if (!(sourceStr in this.twoWayEdgeRegistry)) {
                this.twoWayEdgeRegistry[sourceStr] = [];
            }

            // Target oriented
            if (!(targetStr in this.twoWayEdgeRegistry)) {
                this.twoWayEdgeRegistry[targetStr] = [];
            }

            // Need type as an index as well because some ontologies could have multiple edge types between entities.
            var existingEdges = $.grep(this.twoWayEdgeRegistry[sourceStr], function (e, i) {
                return e.targetId === edge.targetId && e.sourceId === edge.sourceId && e.relationType === edge.relationType;
            });

            if (existingEdges.length > 0) {
                return existingEdges[0];
            } else {
                this.twoWayEdgeRegistry[sourceStr].push(edge);
                this.twoWayEdgeRegistry[targetStr].push(edge);
                return null;
            }
        };

        EdgeRegistry.prototype.getEdgesFor = function (nodeId) {
            if (undefined === this.twoWayEdgeRegistry[nodeId]) {
                return [];
            }
            return this.twoWayEdgeRegistry[nodeId];
        };
        return EdgeRegistry;
    })();
    exports.EdgeRegistry = EdgeRegistry;
});
