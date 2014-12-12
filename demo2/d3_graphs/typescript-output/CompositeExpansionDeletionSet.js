///<reference path="headers/require.d.ts" />
define(["require", "exports", "./GraphModifierCommand", "./DeletionSet", "./ExpansionSets", "UndoRedo/UndoRedoManager", "GraphView", "GraphModifierCommand", "DeletionSet", "ExpansionSets"], function(require, exports, GraphModifierCommand, DeletionSet, ExpansionSet) {
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
        function InitializationDeletionSet(graph, id, undoRedoBoss, expansionType) {
            this.graph = graph;
            this.undoRedoBoss = undoRedoBoss;
            // We don't know the parent node for initial expansions. Before this composite class, we used ExpansionSet
            // with null parent node and it worked.
            this.expansionSet = new ExpansionSet.ExpansionSet(id, null, this.graph, null, expansionType);
            this.deletionSet = new DeletionSet.DeletionSet(this.graph, null);

            this.graphModifier = new GraphModifierCommand.GraphCompositeNodeCommand(graph, id.displayId, this.deletionSet, this.expansionSet);

            undoRedoBoss.addCommand(this.graphModifier);
        }
        InitializationDeletionSet.prototype.updateExpansionNodeDisplayName = function (nodeDisplayName) {
            this.graphModifier.setDisplayName(this.graphModifier.getDisplayName() + ": " + nodeDisplayName);
            this.undoRedoBoss.updateUI(this.graphModifier);
        };

        InitializationDeletionSet.prototype.addAllExpanding = function (nodes) {
            this.expansionSet.addAll(nodes);
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
