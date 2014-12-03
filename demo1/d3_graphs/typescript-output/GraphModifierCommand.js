///<reference path="headers/require.d.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "UndoRedo/UndoRedoManager", "GraphView", "ExpansionSets", "DeletionSet", "LayoutProvider"], function(require, exports) {
    /**
    * I really want an abstract class for this, but Typescript doesn't allow for that.
    * My method of a combination of an interface and a stubbed class isn't feeling right here,
    * so I will use this as a static implementor for the classes in this module.
    */
    var CommonImplementor = (function () {
        function CommonImplementor(graph) {
            this.graph = graph;
            this.fixedLayout = {};
            this.finalSnapshotTaken = false;
            this.cutShort = false;
        }
        CommonImplementor.prototype.applyLayoutImpl = function () {
            this.graph.getLayoutProvider().setLayoutFixedCoordinates(this.fixedLayout);

            // Trigger the fixed layout algorithm immediately
            this.graph.getLayoutProvider().applyFixedLayout();

            // Set up the original algorithm to be used
            if (undefined !== this.storedLayoutRunner) {
                // Can be undefined in the case of composite commands that do not defer snapshotting to composed commands.
                this.graph.getLayoutProvider().setNewLayoutWithoutRunning(this.storedLayoutRunner);
            }
        };

        /**
        * If the snapshot is the final one, it cannot be updated or overwritten again. Only do this when
        * a new expansion has occurred, and it is acceptable to freeze the undo step's node positions.
        */
        CommonImplementor.prototype.snapshotLayoutImpl = function (finalSnapshot) {
            // Record position of all nodes, for undo/redo. Also keep track of the layout algorithm
            // in force, so that if the user expands from that step in the undo/redo stack, it will
            // retrigger the appropriate algorithm.
            // But...once we have made this step "undoable", we freeze the fixed layout and applied algorithm
            if (!this.finalSnapshotTaken) {
                // Using a simple method of snapshotting layouts.
                this.fixedLayout = this.graph.getLayoutProvider().getLayoutPositionSnapshot();
                this.storedLayoutRunner = this.graph.getLayoutProvider().getLayoutRunner();
            }
            if (finalSnapshot) {
                this.finalSnapshotTaken = true;
            }
        };

        /**
        * If the expansion is aborted due to problems with having too many nodes
        * in the graph, we need to know it for later expansion attempts, and to
        * ensure that the remaining nodes coming to this expansion are rejected too.
        */
        CommonImplementor.prototype.commandCutShort = function (setToTrue) {
            if (typeof setToTrue === "undefined") { setToTrue = false; }
            if (setToTrue) {
                this.cutShort = true;
            }
            return this.cutShort;
        };

        CommonImplementor.prototype.areCommandNodesCurrentlyLoaded = function () {
            return this.childImpl.numberOfNodesInCommand() === this.childImpl.numberOfCommandNodesCurrentlyLoaded();
        };
        return CommonImplementor;
    })();
    exports.CommonImplementor = CommonImplementor;

    /**
    * This command allows for the addition of nodes (and undo and redo). Edges are not really
    * added and removed in the same sense, so there is no related class for edges at this time.
    * If they were, we'd would bundle edges to be added or removed with the nodes they were
    * added or removed with.
    */
    var GraphAddNodesCommand = (function (_super) {
        __extends(GraphAddNodesCommand, _super);
        function GraphAddNodesCommand(graph, expansionSet) {
            _super.call(this, graph);
            this.graph = graph;
            this.expansionSet = expansionSet;
            this.redidLast = true;
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
            return this.expansionSet.id.displayId;
        };

        // TODO This implies that nodes should be added to the graph only
        // via the ExpansionSet, so that the logic is the same when adding a node
        // as when redoing the addition of a set. Hmmm...
        GraphAddNodesCommand.prototype.executeRedo = function () {
            if (!this.redidLast) {
                this.redidLast = true;
                this.graph.addNodes(this.expansionSet.nodes, this.expansionSet);

                // Ha, we don't do and undo layouts, I just realized...
                // We apply previous and next from the fenceposts!
                // this.layoutSnapshot.executeRedo();
                this.applyLayout();
            } else {
                console.log("Trying to redo same command twice in a row");
            }
        };

        GraphAddNodesCommand.prototype.executeUndo = function () {
            if (this.redidLast) {
                this.redidLast = false;
                this.graph.removeNodes(this.expansionSet.nodes);
                // NB We don't undo layouts, we only do them.
                // The incoming command will apply its layout.
            } else {
                console.log("Trying to undo same command twice in a row");
            }
        };

        GraphAddNodesCommand.prototype.preview = function () {
        };

        GraphAddNodesCommand.prototype.nodeInteraction = function (nodeId) {
            if (null === this.expansionSet.parentNode || this.expansionSet.parentNode.getEntityId() === nodeId) {
                return this.expansionSet.expansionType;
            }
            for (var i = 0; i < this.expansionSet.nodes.length; i++) {
                var node = this.expansionSet.nodes[i];
                if (node.getEntityId() === nodeId) {
                    return GraphAddNodesCommand.addedNodeInteraction;
                }
            }
            return null;
        };

        GraphAddNodesCommand.prototype.numberOfCommandNodesCurrentlyLoaded = function () {
            return this.expansionSet.getNumberOfNodesCurrentlyInGraph();
        };

        /**
        * Gives the number of nodes there would be if all were loaded,
        * excluding any permanently failed callbacks.
        */
        GraphAddNodesCommand.prototype.numberOfNodesInCommand = function () {
            return this.expansionSet.getNumberOfNodesAssociatedWithExpansion();
        };
        GraphAddNodesCommand.addedNodeInteraction = "added node";

        GraphAddNodesCommand.counter = 0;
        return GraphAddNodesCommand;
    })(CommonImplementor);
    exports.GraphAddNodesCommand = GraphAddNodesCommand;

    var GraphRemoveNodesCommand = (function (_super) {
        __extends(GraphRemoveNodesCommand, _super);
        // For node removal, we will want to generalize expansion sets, and collect adjacent node removals
        // into one set of removed nodes.
        function GraphRemoveNodesCommand(graph, nodesToRemove) {
            _super.call(this, graph);
            this.graph = graph;
            this.nodesToRemove = nodesToRemove;
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
                // I don't have a useful name to give sets of removed nodes, since they are fully arbitrary,
                // unlike expansion sets.
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

                // Don't undo layouts, onyl redo, so that incoming step will apply its layout.
                this.applyLayout();
            } else {
                console.log("Trying to redo same command twice in a row");
            }
        };

        GraphRemoveNodesCommand.prototype.executeUndo = function () {
            if (this.redidLast) {
                this.redidLast = false;
                this.graph.addNodes(this.nodesToRemove.nodes, null);
            } else {
                console.log("Trying to undo same command twice in a row");
            }
        };

        GraphRemoveNodesCommand.prototype.preview = function () {
        };

        GraphRemoveNodesCommand.prototype.nodeInteraction = function (nodeId) {
            for (var i = 0; i < this.nodesToRemove.nodes.length; i++) {
                var node = this.nodesToRemove.nodes[i];
                if (node.getEntityId() === nodeId) {
                    return GraphRemoveNodesCommand.deletionNodeInteraction;
                }
            }
            return null;
        };

        GraphRemoveNodesCommand.prototype.numberOfCommandNodesCurrentlyLoaded = function () {
            return this.nodesToRemove.numberOfNodesCurrentlyInGraph();
        };

        /**
        * Gives the number of nodes there would be if all were loaded,
        * excluding any permanently failed callbacks.
        */
        GraphRemoveNodesCommand.prototype.numberOfNodesInCommand = function () {
            return this.nodesToRemove.nodes.length;
        };
        GraphRemoveNodesCommand.deletionNodeInteraction = "deleted node";

        GraphRemoveNodesCommand.counter = 0;
        return GraphRemoveNodesCommand;
    })(CommonImplementor);
    exports.GraphRemoveNodesCommand = GraphRemoveNodesCommand;

    var GraphCompositeNodeCommand = (function (_super) {
        __extends(GraphCompositeNodeCommand, _super);
        function GraphCompositeNodeCommand(graph, displayName) {
            _super.call(this, graph);
            this.graph = graph;
            this.displayName = displayName;
            this.redidLast = false;
            this.commands = [];
            this.childImpl = this;
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
            // Danger! Caller is wholly responsible for content of this display name.
            // It shouldn't change twice, and the caller shouldn't be trying to change it twice.
            this.displayName = newName;
        };

        GraphCompositeNodeCommand.prototype.executeRedo = function () {
            if (!this.redidLast) {
                this.redidLast = true;
                for (var i = 0; i < this.commands.length; i++) {
                    this.commands[i].executeRedo();
                }

                // Only apply layouts on redo, so that incoming steps get their layout.
                this.applyLayout();
            } else {
                console.log("Trying to redo same command twice in a row");
            }
        };

        GraphCompositeNodeCommand.prototype.executeUndo = function () {
            if (this.redidLast) {
                this.redidLast = false;
                for (var i = this.commands.length - 1; i >= 0; i--) {
                    this.commands[i].executeUndo();
                }
            } else {
                console.log("Trying to undo same command twice in a row");
            }
        };

        GraphCompositeNodeCommand.prototype.preview = function () {
        };

        GraphCompositeNodeCommand.prototype.nodeInteraction = function (nodeId) {
            for (var i = this.commands.length - 1; i >= 0; i--) {
                var interaction = this.commands[i].nodeInteraction(nodeId);
                if (null !== interaction) {
                    return interaction;
                }
            }
            return null;
        };

        GraphCompositeNodeCommand.prototype.numberOfCommandNodesCurrentlyLoaded = function () {
            var nodeCount = 0;
            for (var i = 0; i < this.commands.length; i++) {
                nodeCount += this.commands[i].numberOfCommandNodesCurrentlyLoaded();
            }
            return nodeCount;
        };

        /**
        * Gives the number of nodes there would be if all were loaded,
        * excluding any permanently failed callbacks.
        */
        GraphCompositeNodeCommand.prototype.numberOfNodesInCommand = function () {
            var nodeCount = 0;
            for (var i = 0; i < this.commands.length; i++) {
                nodeCount += this.commands[i].numberOfNodesInCommand();
            }
            return nodeCount;
        };
        GraphCompositeNodeCommand.counter = 0;
        return GraphCompositeNodeCommand;
    })(CommonImplementor);
    exports.GraphCompositeNodeCommand = GraphCompositeNodeCommand;
});
