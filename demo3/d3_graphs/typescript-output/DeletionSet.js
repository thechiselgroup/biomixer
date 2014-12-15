///<reference path="headers/require.d.ts" />
define(["require", "exports", "./GraphModifierCommand", "UndoRedo/UndoRedoManager", "GraphView", "GraphModifierCommand"], function(require, exports, GraphModifierCommand) {
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
        function DeletionSet(graph, undoRedoBoss) {
            this.graph = graph;
            this.undoRedoBoss = undoRedoBoss;
            // would use set, but we have to convert to array to pass through...
            this.nodes = new Array();
            this.graphModifier = new GraphModifierCommand.GraphRemoveNodesCommand(graph, this);

            if (null != undoRedoBoss) {
                undoRedoBoss.addCommand(this.graphModifier);
            }
        }
        DeletionSet.prototype.addAll = function (nodes) {
            var _this = this;
            nodes.forEach(function (node, i, arr) {
                if (_this.nodes.indexOf(node) === -1) {
                    _this.nodes.push(node);
                }
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
        return DeletionSet;
    })();
    exports.DeletionSet = DeletionSet;
});
