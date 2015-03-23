define(["require", "exports", "./GraphModifierCommand", "./DeletionSet", "./ExpansionSets", "UndoRedo/UndoRedoManager", "GraphView", "GraphModifierCommand", "DeletionSet", "ExpansionSets"], function (require, exports, GraphModifierCommand, DeletionSet, ExpansionSet) {
    var InitializationDeletionSet = (function () {
        function InitializationDeletionSet(graph, id, undoRedoBoss, expansionType, parentNode) {
            var _this = this;
            if (parentNode === void 0) { parentNode = null; }
            this.graph = graph;
            this.undoRedoBoss = undoRedoBoss;
            this.nodeDisplayName = "";
            var liveExpansionSets = [];
            this.deletionSet = new DeletionSet.DeletionSet(this.graph, liveExpansionSets, null);
            this.expansionSet = new ExpansionSet.ExpansionSet(id, parentNode, this.graph, liveExpansionSets, null, expansionType);
            this.deletionSet.addAssociatedExpansionSet(this.expansionSet);
            this.graphModifier = new GraphModifierCommand.GraphCompositeNodeCommand(graph, id.getDisplayId(), this.deletionSet, this.expansionSet, liveExpansionSets);
            this.expansionSet.graphModifier.addNameUpdateListener(id.internalId, function () {
                _this.updateDisplayName();
            });
            if (null != undoRedoBoss) {
                undoRedoBoss.addCommand(this.graphModifier);
            }
        }
        InitializationDeletionSet.prototype.updateExpansionNodeDisplayName = function (nodeDisplayName) {
            this.nodeDisplayName = nodeDisplayName;
        };
        InitializationDeletionSet.prototype.updateDisplayName = function () {
            if (-1 == this.expansionSet.getFullDisplayId().indexOf(this.nodeDisplayName)) {
                this.expansionSet.id.setDisplayId(this.expansionSet.id.getDisplayId() + ": " + this.nodeDisplayName);
            }
            this.graphModifier.setDisplayName(this.expansionSet.getFullDisplayId());
        };
        InitializationDeletionSet.prototype.addAllExpanding = function (nodes) {
            this.expansionSet.addAll(nodes);
            this.graphModifier.displayNameUpdated();
        };
        InitializationDeletionSet.prototype.addAllDeleting = function (nodes) {
            this.deletionSet.addAll(nodes);
        };
        InitializationDeletionSet.prototype.getGraphModifier = function () {
            return this.graphModifier;
        };
        return InitializationDeletionSet;
    })();
    exports.InitializationDeletionSet = InitializationDeletionSet;
});
