///<reference path="headers/require.d.ts" />
define(["require", "exports", "./GraphModifierCommand", "UndoRedo/UndoRedoManager", "GraphView", "GraphModifierCommand"], function (require, exports, GraphModifierCommand) {
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
    var ExpansionSet = (function () {
        /**
         * Parent node can be null for the initial expansion, when the expansion is not triggered
         * by a menu on an existing node.
         */
        function ExpansionSet(id, parentNode, graph, liveExpansionSets, undoRedoBoss, expansionType) {
            this.id = id;
            this.parentNode = parentNode;
            this.graph = graph;
            this.undoRedoBoss = undoRedoBoss;
            this.expansionType = expansionType;
            this.nodes = new Array();
            if (null != parentNode) {
                parentNode.expansionSetAsParent = this;
            }
            liveExpansionSets.push(this);
            this.graphModifier = new GraphModifierCommand.GraphAddNodesCommand(graph, this, liveExpansionSets);
            // Not really necessary, but maybe it would be convenient?
            // if(registerImmediately){
            //     this.thunderbirdsAreGo();
            // }
        }
        /**
         * The expansion set is cleared for application. Register in the undo/redo set.
         * Originally this was in the constructor. Using this method works when node cap checks are performed
         * when we know how many REST calls we will make, rather than when we are
         * adding the nodes subsequent to those REST calls.
         * That is, we ask the user about how many nodes they would like earlier rather than later,
         * and thus we need to register the expansion set at a different time than when we create it.
         */
        ExpansionSet.prototype.thunderbirdsAreGo = function () {
            if (null != this.undoRedoBoss) {
                this.undoRedoBoss.addCommand(this.graphModifier);
            }
        };
        ExpansionSet.prototype.addAll = function (nodes) {
            var _this = this;
            nodes.forEach(function (node, i, arr) {
                if (node.expansionSetAsMember !== undefined && node.expansionSetAsMember !== _this) {
                    // The natural flow of the graph populating logic results in multiple passes, due to D3 idioms.
                    // The best place to add nodes to expansion sets are right as we are finally populating the graph
                    // with nodes from an expasions, so we will handle redundant expasion set additions here.
                    // Also, I want to know if there are attempts to add a node to multiple expansion sets.
                    // We don't want that, because it would complicate semantics, especially for undo-redo
                    // functionality that relies on expansion sets.
                    console.log("Attempted change of set expansion ID on node: " + _this.id.getDisplayId() + ", expansion ID " + node.getEntityId());
                }
                else if (node.expansionSetAsMember !== undefined && node.expansionSetAsMember === _this) {
                }
                else {
                    node.expansionSetAsMember = _this;
                    _this.nodes.push(node);
                }
            });
            this.graphModifier.displayNameUpdated();
        };
        ExpansionSet.prototype.getGraphModifier = function () {
            return this.graphModifier;
        };
        ExpansionSet.prototype.getNumberOfNodesCurrentlyInGraph = function () {
            var numInGraph = 0;
            for (var i = 0; i < this.nodes.length; i++) {
                if (this.graph.containsNode(this.nodes[i])) {
                    numInGraph++;
                }
            }
            return numInGraph;
        };
        ExpansionSet.prototype.getNumberOfNodesMissing = function () {
            return this.graph.getNumberOfPotentialNodesToExpand(this.parentNode, this.expansionType);
        };
        /**
         * Convenience method dispatching into publically accessible GraphModifer.
         * If the expansion is aborted due to problems with having too many nodes
         * in the graph, we need to know it for later expansion attempts, and to
         * ensure that the remaining nodes coming to this expansion are rejected too.
         */
        ExpansionSet.prototype.expansionCutShort = function (setToTrue) {
            if (setToTrue === void 0) { setToTrue = false; }
            if (setToTrue) {
                this.graphModifier.commandCutShort(true);
            }
            return this.graphModifier.commandCutShort();
        };
        ExpansionSet.prototype.getNodes = function () {
            return this.nodes;
        };
        ExpansionSet.prototype.getFullDisplayId = function () {
            return this.id.getDisplayId() + " [" + this.nodes.length + "]";
        };
        return ExpansionSet;
    })();
    exports.ExpansionSet = ExpansionSet;
    var ExpansionSetIdentifer = (function () {
        // Only assign raw concept URI to this string
        //    expansionSetIdentifer; // strengthen duck typing
        function ExpansionSetIdentifer(internalId, displayId) {
            this.internalId = internalId;
            this.displayId = displayId;
        }
        ExpansionSetIdentifer.prototype.getDisplayId = function () {
            return this.displayId;
        };
        ExpansionSetIdentifer.prototype.setDisplayId = function (newString) {
            this.displayId = newString;
        };
        return ExpansionSetIdentifer;
    })();
    exports.ExpansionSetIdentifer = ExpansionSetIdentifer;
});
