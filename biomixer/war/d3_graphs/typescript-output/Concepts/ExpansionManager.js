define(["require", "exports", "../GraphModifierCommand", "Concepts/ConceptGraph", "UndoRedo/UndoRedoManager", "GraphModifierCommand", "ExpansionSets"], function (require, exports, GraphModifierCommand) {
    var ExpansionManager = (function () {
        function ExpansionManager(undoBoss) {
            this.undoBoss = undoBoss;
            this.edgeRegistry = new EdgeRegistry();
        }
        ExpansionManager.prototype.purgeInaccessibleNode = function (conceptUri) {
            this.edgeRegistry.purgeInaccessibleNode(conceptUri);
        };
        ExpansionManager.prototype.nodeIsInaccessible = function (conceptUri) {
            return this.edgeRegistry.nodeIsInaccessible(conceptUri);
        };
        ExpansionManager.prototype.wasConceptClearedForExpansion = function (conceptUri, expansionType) {
            var returnVal = false;
            var crumbTrail = this.undoBoss.getCrumbHistory();
            var conceptUriForIds = String(conceptUri);
            for (var i = crumbTrail.length - 1; i >= 0; i--) {
                var command = crumbTrail[i];
                var nodeInteractions = command.nodeInteraction(conceptUriForIds);
                if (null == nodeInteractions) {
                    return returnVal;
                }
                else if (nodeInteractions.indexOf(expansionType) !== -1) {
                    returnVal = true;
                    return returnVal;
                }
                else if (nodeInteractions.indexOf(GraphModifierCommand.GraphRemoveNodesCommand.deletionNodeInteraction) !== -1) {
                    returnVal = false;
                    return returnVal;
                }
            }
            return returnVal;
        };
        ExpansionManager.prototype.getActiveExpansionSets = function () {
            var history = this.undoBoss.getCrumbHistory();
            var currentUndoLevel = history[history.length - 1];
            return this.getExpansionSets(currentUndoLevel).slice();
        };
        ExpansionManager.prototype.getExpansionSets = function (command) {
            if (command instanceof GraphModifierCommand.GraphAddNodesCommand) {
                return command.liveExpansionSets;
            }
            else if (command instanceof GraphModifierCommand.GraphCompositeNodeCommand) {
                return command.liveExpansionSets;
            }
            else if (command instanceof GraphModifierCommand.GraphRemoveNodesCommand) {
                return command.liveExpansionSets;
            }
            return [];
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
            this.twoWayEdgeRegistry = {};
            this.inaccessibleNodes = [];
        }
        EdgeRegistry.prototype.addEdgeToRegistry = function (edge, graph) {
            var sourceStr = String(edge.sourceId);
            var targetStr = String(edge.targetId);
            if (!graph.nodeIsAccessible(edge.sourceId) || !graph.nodeIsAccessible(edge.targetId)) {
                return null;
            }
            if (!(sourceStr in this.twoWayEdgeRegistry)) {
                this.twoWayEdgeRegistry[sourceStr] = [];
            }
            if (!(targetStr in this.twoWayEdgeRegistry)) {
                this.twoWayEdgeRegistry[targetStr] = [];
            }
            var existingEdges = $.grep(this.twoWayEdgeRegistry[sourceStr], function (e, i) {
                return e.targetId === edge.targetId && e.sourceId === edge.sourceId && e.relationType === edge.relationType;
            });
            if (existingEdges.length > 0) {
                return existingEdges[0];
            }
            else {
                this.twoWayEdgeRegistry[sourceStr].push(edge);
                this.twoWayEdgeRegistry[targetStr].push(edge);
                return null;
            }
        };
        EdgeRegistry.prototype.getEdgesFor = function (firstNodeIdRaw, secondNodeIdRaw) {
            var firstNodeId = String(firstNodeIdRaw);
            var secondNodeId = String(secondNodeIdRaw);
            if (undefined === this.twoWayEdgeRegistry[firstNodeId]) {
                return [];
            }
            if (null == secondNodeIdRaw) {
                return this.twoWayEdgeRegistry[firstNodeId];
            }
            else {
                var edgesToTarget = [];
                for (var i = 0; i < this.twoWayEdgeRegistry[firstNodeId].length; i++) {
                    var edge = this.twoWayEdgeRegistry[firstNodeId][i];
                    if (String(edge.sourceId) === secondNodeId || String(edge.targetId) === secondNodeId) {
                        edgesToTarget.push(edge);
                    }
                }
                return edgesToTarget;
            }
        };
        EdgeRegistry.prototype.purgeInaccessibleNode = function (conceptUri) {
            var sourceIdStr = String(conceptUri);
            for (var oneIdStr in this.twoWayEdgeRegistry) {
                var edges = this.twoWayEdgeRegistry[oneIdStr];
                this.twoWayEdgeRegistry[oneIdStr] = $.grep(edges, function (edge) {
                    return !(edge.sourceId === conceptUri || edge.targetId === conceptUri);
                });
                if (this.twoWayEdgeRegistry[oneIdStr].length === 0) {
                    delete this.twoWayEdgeRegistry[oneIdStr];
                }
            }
            this.inaccessibleNodes.push(conceptUri);
            console.log("Purged " + conceptUri);
        };
        EdgeRegistry.prototype.nodeIsInaccessible = function (conceptUri) {
            return this.inaccessibleNodes.indexOf(conceptUri) !== -1;
        };
        return EdgeRegistry;
    })();
    exports.EdgeRegistry = EdgeRegistry;
});
