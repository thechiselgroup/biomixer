define(["require", "exports", "./GraphModifierCommand", "UndoRedo/UndoRedoManager", "GraphView", "GraphModifierCommand"], function (require, exports, GraphModifierCommand) {
    var ExpansionSet = (function () {
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
        }
        ExpansionSet.prototype.thunderbirdsAreGo = function () {
            if (null != this.undoRedoBoss) {
                this.undoRedoBoss.addCommand(this.graphModifier);
            }
        };
        ExpansionSet.prototype.addAll = function (nodes) {
            var _this = this;
            nodes.forEach(function (node, i, arr) {
                if (node.expansionSetAsMember !== undefined && node.expansionSetAsMember !== _this) {
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
