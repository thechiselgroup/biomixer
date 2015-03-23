var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "UndoRedo/UndoRedoManager", "GraphView", "ExpansionSets", "DeletionSet", "LayoutProvider"], function (require, exports) {
    var CommonImplementor = (function () {
        function CommonImplementor(graph) {
            this.graph = graph;
            this.fixedLayout = {};
            this.displayNameChangeCallbacks = {};
            this.activeStepCallback = function (command) {
            };
            this.finalSnapshotTaken = false;
            this.cutShort = false;
        }
        CommonImplementor.prototype.applyLayoutImpl = function () {
            this.graph.getLayoutProvider().setLayoutFixedCoordinates(this.fixedLayout);
            this.graph.getLayoutProvider().applyFixedLayout();
            if (undefined !== this.storedLayoutRunner) {
                this.graph.getLayoutProvider().setNewLayoutWithoutRunning(this.storedLayoutRunner);
            }
        };
        CommonImplementor.prototype.snapshotLayoutImpl = function (finalSnapshot) {
            if (!this.finalSnapshotTaken) {
                this.fixedLayout = this.graph.getLayoutProvider().getLayoutPositionSnapshot();
                this.storedLayoutRunner = this.graph.getLayoutProvider().getLayoutRunner();
            }
            if (finalSnapshot) {
                this.finalSnapshotTaken = true;
            }
        };
        CommonImplementor.prototype.commandCutShort = function (setToTrue) {
            if (setToTrue === void 0) { setToTrue = false; }
            if (setToTrue) {
                this.cutShort = true;
            }
            return this.cutShort;
        };
        CommonImplementor.prototype.addActiveStepCallback = function (callback) {
            this.activeStepCallback = callback;
        };
        CommonImplementor.prototype.callActiveStepCallback = function () {
            if (null != this.activeStepCallback) {
                this.activeStepCallback(this.childImpl);
            }
        };
        CommonImplementor.prototype.addNameUpdateListener = function (targetId, callback) {
            if (null == this.displayNameChangeCallbacks[targetId]) {
                this.displayNameChangeCallbacks[targetId] = callback;
            }
        };
        CommonImplementor.prototype.displayNameUpdated = function () {
            for (var key in this.displayNameChangeCallbacks) {
                this.displayNameChangeCallbacks[key]();
            }
        };
        return CommonImplementor;
    })();
    exports.CommonImplementor = CommonImplementor;
    var GraphAddNodesCommand = (function (_super) {
        __extends(GraphAddNodesCommand, _super);
        function GraphAddNodesCommand(graph, expansionSet, liveExpansionSets) {
            _super.call(this, graph);
            this.graph = graph;
            this.expansionSet = expansionSet;
            this.liveExpansionSets = liveExpansionSets;
            this.redidLast = true;
            this.extraInteractions = {};
            this.childImpl = this;
        }
        GraphAddNodesCommand.prototype.snapshotLayout = function (finalSnapshot) {
            this.snapshotLayoutImpl(finalSnapshot);
        };
        GraphAddNodesCommand.prototype.applyLayout = function () {
            this.applyLayoutImpl();
        };
        GraphAddNodesCommand.prototype.getUniqueId = function () {
            if (undefined === this.id) {
                this.id = this.expansionSet.id.internalId + "_" + (GraphAddNodesCommand.counter++);
            }
            return this.id;
        };
        GraphAddNodesCommand.prototype.getDisplayName = function () {
            return this.expansionSet.getFullDisplayId();
        };
        GraphAddNodesCommand.prototype.executeRedo = function () {
            if (!this.redidLast) {
                this.redidLast = true;
                this.graph.addNodes(this.expansionSet.nodes, this.expansionSet);
                this.applyLayout();
            }
            else {
            }
        };
        GraphAddNodesCommand.prototype.executeUndo = function () {
            if (this.redidLast) {
                this.redidLast = false;
                this.graph.removeNodes(this.expansionSet.nodes);
            }
            else {
            }
        };
        GraphAddNodesCommand.prototype.preview = function () {
        };
        GraphAddNodesCommand.prototype.addExtraInteraction = function (nodeId, interactionType) {
            this.extraInteractions[nodeId] = interactionType;
        };
        GraphAddNodesCommand.prototype.nodeInteraction = function (nodeId) {
            var interactions = [];
            if (null === this.expansionSet.parentNode || this.expansionSet.parentNode.getEntityId() === nodeId) {
                interactions.push(this.expansionSet.expansionType);
            }
            for (var i = 0; i < this.expansionSet.nodes.length; i++) {
                var node = this.expansionSet.nodes[i];
                if (node.getEntityId() === nodeId) {
                    interactions.push(GraphAddNodesCommand.addedNodeInteraction);
                    var extraInteraction = this.extraInteractions[nodeId];
                    if (null != extraInteraction) {
                        interactions.push(extraInteraction);
                    }
                    break;
                }
            }
            return interactions;
        };
        GraphAddNodesCommand.addedNodeInteraction = "added node";
        GraphAddNodesCommand.counter = 0;
        return GraphAddNodesCommand;
    })(CommonImplementor);
    exports.GraphAddNodesCommand = GraphAddNodesCommand;
    var GraphRemoveNodesCommand = (function (_super) {
        __extends(GraphRemoveNodesCommand, _super);
        function GraphRemoveNodesCommand(graph, nodesToRemove, liveExpansionSets) {
            _super.call(this, graph);
            this.graph = graph;
            this.nodesToRemove = nodesToRemove;
            this.liveExpansionSets = liveExpansionSets;
            this.redidLast = false;
            this.childImpl = this;
        }
        GraphRemoveNodesCommand.prototype.snapshotLayout = function (finalSnapshot) {
            this.snapshotLayoutImpl(finalSnapshot);
        };
        GraphRemoveNodesCommand.prototype.applyLayout = function () {
            this.applyLayoutImpl();
        };
        GraphRemoveNodesCommand.prototype.getUniqueId = function () {
            if (undefined === this.id) {
                this.id = "remove_nodes_" + (GraphRemoveNodesCommand.counter++);
            }
            return this.id;
        };
        GraphRemoveNodesCommand.prototype.getDisplayName = function () {
            return "Removed " + this.nodesToRemove.nodes.length + " Node" + (this.nodesToRemove.nodes.length > 1 ? "s" : "");
        };
        GraphRemoveNodesCommand.prototype.executeRedo = function () {
            if (!this.redidLast) {
                this.redidLast = true;
                this.graph.removeNodes(this.nodesToRemove.nodes);
                this.applyLayout();
            }
            else {
            }
        };
        GraphRemoveNodesCommand.prototype.executeUndo = function () {
            if (this.redidLast) {
                this.redidLast = false;
                this.graph.addNodes(this.nodesToRemove.nodes, null);
            }
            else {
            }
        };
        GraphRemoveNodesCommand.prototype.preview = function () {
        };
        GraphRemoveNodesCommand.prototype.nodeInteraction = function (nodeId) {
            for (var i = 0; i < this.nodesToRemove.nodes.length; i++) {
                var node = this.nodesToRemove.nodes[i];
                if (node.getEntityId() === nodeId) {
                    return [GraphRemoveNodesCommand.deletionNodeInteraction];
                }
            }
            return null;
        };
        GraphRemoveNodesCommand.deletionNodeInteraction = "deleted node";
        GraphRemoveNodesCommand.counter = 0;
        return GraphRemoveNodesCommand;
    })(CommonImplementor);
    exports.GraphRemoveNodesCommand = GraphRemoveNodesCommand;
    var GraphCompositeNodeCommand = (function (_super) {
        __extends(GraphCompositeNodeCommand, _super);
        function GraphCompositeNodeCommand(graph, displayName, deletionSet, additionSet, liveExpansionSets) {
            _super.call(this, graph);
            this.graph = graph;
            this.displayName = displayName;
            this.deletionSet = deletionSet;
            this.additionSet = additionSet;
            this.liveExpansionSets = liveExpansionSets;
            this.redidLast = false;
            this.commands = [];
            this.childImpl = this;
            this.addCommand(deletionSet.getGraphModifier());
            this.addCommand(additionSet.getGraphModifier());
        }
        GraphCompositeNodeCommand.prototype.addCommand = function (newCommand) {
            this.commands.push(newCommand);
        };
        GraphCompositeNodeCommand.prototype.snapshotLayout = function (finalSnapshot) {
            this.snapshotLayoutImpl(finalSnapshot);
        };
        GraphCompositeNodeCommand.prototype.applyLayout = function () {
            this.applyLayoutImpl();
        };
        GraphCompositeNodeCommand.prototype.getUniqueId = function () {
            if (undefined === this.id) {
                this.id = "composite_command_" + (GraphCompositeNodeCommand.counter++);
            }
            return this.id;
        };
        GraphCompositeNodeCommand.prototype.getDisplayName = function () {
            return this.displayName;
        };
        GraphCompositeNodeCommand.prototype.setDisplayName = function (newName) {
            this.displayName = newName;
            this.displayNameUpdated();
        };
        GraphCompositeNodeCommand.prototype.executeRedo = function () {
            if (!this.redidLast) {
                this.redidLast = true;
                for (var i = 0; i < this.commands.length; i++) {
                    this.commands[i].executeRedo();
                }
                this.applyLayout();
            }
            else {
            }
        };
        GraphCompositeNodeCommand.prototype.executeUndo = function () {
            if (this.redidLast) {
                this.redidLast = false;
                for (var i = this.commands.length - 1; i >= 0; i--) {
                    this.commands[i].executeUndo();
                }
            }
            else {
            }
        };
        GraphCompositeNodeCommand.prototype.preview = function () {
        };
        GraphCompositeNodeCommand.prototype.addExtraInteraction = function (nodeId, interactionType) {
            this.additionSet.getGraphModifier().addExtraInteraction(nodeId, interactionType);
        };
        GraphCompositeNodeCommand.prototype.nodeInteraction = function (nodeId) {
            for (var i = this.commands.length - 1; i >= 0; i--) {
                var interactions = this.commands[i].nodeInteraction(nodeId);
                if (null !== interactions) {
                    return interactions;
                }
            }
            return null;
        };
        GraphCompositeNodeCommand.counter = 0;
        return GraphCompositeNodeCommand;
    })(CommonImplementor);
    exports.GraphCompositeNodeCommand = GraphCompositeNodeCommand;
});
