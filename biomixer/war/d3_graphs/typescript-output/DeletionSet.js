define(["require", "exports", "./GraphModifierCommand", "UndoRedo/UndoRedoManager", "GraphView", "GraphModifierCommand", "ExpansionSets"], function (require, exports, GraphModifierCommand) {
    var DeletionSet = (function () {
        function DeletionSet(graph, liveExpansionSets, undoRedoBoss) {
            this.graph = graph;
            this.liveExpansionSets = liveExpansionSets;
            this.undoRedoBoss = undoRedoBoss;
            this.associatedExpansionSet = null;
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
            var deathRow = [];
            for (var expSetIndex in this.liveExpansionSets) {
                var expSet = this.liveExpansionSets[expSetIndex];
                var expSetNodes = expSet.getNodes();
                var guilty = true;
                for (var nodeIndex in expSetNodes) {
                    var node = expSetNodes[nodeIndex];
                    if (this.graph.containsNode(node) && this.nodes.indexOf(node) === -1) {
                        guilty = false;
                        continue;
                    }
                }
                if (guilty && expSet !== this.associatedExpansionSet) {
                    deathRow.push(expSet);
                }
            }
            this.liveExpansionSets = this.liveExpansionSets.filter(function (expSet, i) {
                return -1 === deathRow.indexOf(expSet);
            });
            this.graphModifier.displayNameUpdated();
        };
        DeletionSet.prototype.getGraphModifier = function () {
            return this.graphModifier;
        };
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
