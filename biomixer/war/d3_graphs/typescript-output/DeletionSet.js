///<reference path="headers/require.d.ts" />
define(["require", "exports", "./GraphModifierCommand", "UndoRedo/UndoRedoManager", "GraphView", "GraphModifierCommand", "ExpansionSets"], function(require, exports, GraphModifierCommand) {
    /**
    * Expansion sets are a way of collecting together nodes that were loaded for a common
    * purpose; I would say at the same time, but loading is done with so much asynchonicity
    * that this would be inaccurate.
    *
    * By collecting nodes loaded as cohorts, we can then filter them in and out, or use that
    * data to drive an undo-redo engine.
    *
    * Other uses might arise.
    */
    var DeletionSet = (function () {
        /**
        * Parent node can be null for the initial expansion, when the expansion is not triggered
        * by a menu on an existing node.
        */
        function DeletionSet(graph, liveExpansionSets, undoRedoBoss) {
            this.graph = graph;
            this.liveExpansionSets = liveExpansionSets;
            this.undoRedoBoss = undoRedoBoss;
            this.associatedExpansionSet = null;
            // would use set, but we have to convert to array to pass through...
            this.nodes = new Array();
            this.graphModifier = new GraphModifierCommand.GraphRemoveNodesCommand(graph, this, this.liveExpansionSets);

            if (null != undoRedoBoss) {
                undoRedoBoss.addCommand(this.graphModifier);
            }
        }
        DeletionSet.prototype.addAll = function (incomingNodes) {
            var _this = this;
            incomingNodes.forEach(function (node, i) {
                if (_this.nodes.indexOf(node) === -1) {
                    _this.nodes.push(node);
                }
            });

            // We need to recompute what expansion sets are still alive. Any that
            // have had all of their nodes deleted need to be removed from the
            // live expansion set collection.
            // This is like the elephant graveyard of expansion sets. Or death row.
            var deathRow = [];
            for (var expSetIndex in this.liveExpansionSets) {
                var expSet = this.liveExpansionSets[expSetIndex];
                var expSetNodes = expSet.getNodes();
                var guilty = true;
                for (var nodeIndex in expSetNodes) {
                    var node = expSetNodes[nodeIndex];

                    // if the graph contains a given node, and that node
                    // is not being deleted in this deletion set, then
                    // the expansion set is still alive (assuming
                    // the expansion set was alive just prior to now).
                    if (this.graph.containsNode(node) && this.nodes.indexOf(node) === -1) {
                        guilty = false;
                        continue;
                    }
                }

                if (guilty && expSet !== this.associatedExpansionSet) {
                    deathRow.push(expSet);
                }
            }

            // Execute them.
            this.liveExpansionSets = this.liveExpansionSets.filter(function (expSet, i) {
                return -1 === deathRow.indexOf(expSet);
            });
            if (null != this.undoRedoBoss) {
                this.undoRedoBoss.updateUI(this.graphModifier);
            }
        };

        DeletionSet.prototype.getGraphModifier = function () {
            return this.graphModifier;
        };

        /**
        * Sort of odd, but useful for testing things in a way consistent with ExpansionSets
        */
        DeletionSet.prototype.numberOfNodesCurrentlyInGraph = function () {
            var numInGraph = 0;
            for (var i = 0; i < this.nodes.length; i++) {
                if (this.graph.containsNode(this.nodes[i])) {
                    numInGraph++;
                }
            }
            return numInGraph;
        };

        DeletionSet.prototype.addAssociatedExpansionSet = function (expSet) {
            this.associatedExpansionSet = expSet;
        };
        return DeletionSet;
    })();
    exports.DeletionSet = DeletionSet;
});
