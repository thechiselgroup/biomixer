///<reference path="headers/require.d.ts" />
define(["require", "exports", "./GraphModifierCommand", "./DeletionSet", "./ExpansionSets", "UndoRedo/UndoRedoManager", "GraphView", "GraphModifierCommand", "DeletionSet", "ExpansionSets"], function (require, exports, GraphModifierCommand, DeletionSet, ExpansionSet) {
    /**
     * This class is a version of the deletion set that is oriented towards re-initialization of the view,
     * which occurs when a base expansion change is triggered. It needs to track nodes deleted,
     * but it also needs the expansion. It is a composite action that should be bundled together.
     * Pardon the poor naming.
     */
    var InitializationDeletionSet = (function () {
        /**
         * Parent node can be null for the initial expansion, when the expansion is not triggered
         * by a menu on an existing node.
         */
        function InitializationDeletionSet(graph, id, undoRedoBoss, expansionType, parentNode) {
            var _this = this;
            if (parentNode === void 0) { parentNode = null; }
            this.graph = graph;
            this.undoRedoBoss = undoRedoBoss;
            this.nodeDisplayName = "";
            // We don't know the parent node for initial expansions. Before this composite class, we used ExpansionSet
            // with null parent node and it worked.
            // We always want this for the initialization deletion set.
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
        /**
         * We need to be able to add the expansion node's name to the set. Use this when that is available.
         */
        InitializationDeletionSet.prototype.updateExpansionNodeDisplayName = function (nodeDisplayName) {
            this.nodeDisplayName = nodeDisplayName;
        };
        InitializationDeletionSet.prototype.updateDisplayName = function () {
            // Hackish. I don't see a more elegant way, but maybe I can refactor display names altogether?
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
