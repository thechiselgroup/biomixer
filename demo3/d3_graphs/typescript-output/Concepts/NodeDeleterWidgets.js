///<reference path="headers/require.d.ts" />
///<reference path="headers/d3.d.ts" />
define(["require", "exports", "../GraphView", "../Menu", "../DeletionSet", "../JQueryExtension", "GraphView", "Menu", "Concepts/ConceptPathsToRoot", "Concepts/ConceptGraph", "Concepts/CherryPickConceptFilter", "Concepts/OntologyConceptFilter", "Concepts/ExpansionSetFilter", "DeletionSet", "CompositeExpansionDeletionSet", "UndoRedo/UndoRedoManager"], function(require, exports, GraphView, Menu, DeletionSet) {
    var NodeDeleterWidgets = (function () {
        function NodeDeleterWidgets(graph, graphView, undoRedoBoss) {
            this.graph = graph;
            this.graphView = graphView;
            this.undoRedoBoss = undoRedoBoss;
        }
        NodeDeleterWidgets.prototype.addMenuComponents = function (menuSelector) {
            // Add the butttons to the pop-out panel
            var deleterContainer = $("<div>").attr("id", "nodeDeletionMenuContainer");
            $(menuSelector).append(deleterContainer);
            deleterContainer.append($("<label>").addClass(Menu.Menu.menuLabelClass).text("Node Management"));
            deleterContainer.append($("<br>"));
             {
                deleterContainer.append($("<input>").attr("class", "nodeDeleterButton nodeCommandButton").attr("id", "nodeDeleterButton").attr("type", "button").attr("value", "Remove All Unchecked Nodes"));

                d3.selectAll("#nodeDeleterButton").on("click", this.deleteSelectedCheckboxesLambda());
            }

             {
                deleterContainer.append($("<input>").attr("class", "nodeUnhiderButton nodeCommandButton").attr("id", "nodeUnhiderButton").attr("type", "button").attr("value", "Reset All Node Checkboxes"));

                d3.selectAll("#nodeUnhiderButton").on("click", this.revealUnselectedCheckboxesLambda());
            }

            deleterContainer.append($("<br>"));
        };

        NodeDeleterWidgets.prototype.deleteNodesForGraphInitialization = function (initSet) {
            var outerThis = this;

            initSet.addAllDeleting(this.graph.graphD3Format.nodes);

            // Execute the deletion by "redoing" the deletion set.
            // For other commands, this isn't necessarily possible, but when
            // it is, it is preferable to having duplicate code in redo and where
            // the command is created (and applied).
            initSet.getGraphModifier().executeRedo();
        };

        /**
        * This will delete all nodes that correspond to the currently active or selected
        * filter checkboxes.
        * TODO I can do far less fiddly organizational work if the system can handle redundant
        * delete and add attempts. This is because the ontologies and expansion sets and individual
        * nodes will overlap with each other. If I have to sort through and approve deletions
        * here, it will be bug prone. If the graph can safely receive redundant attempts, this class
        * will be much easier to create.
        * Or...I need a DeletionSet anyway, so I can simply add all nodes from each checkbox to the
        * set, which will itself prevent multiple attempts.
        */
        NodeDeleterWidgets.prototype.deleteSelectedCheckboxesLambda = function () {
            var _this = this;
            return function () {
                // NB What do I do about expansion sets that have nodes deleted from them? Well, for undo/redo
                // it doesn't matter at all, because you can't back up to the expansion without undoing the
                // deletion. For filtering, it will have some dangling uselessness. Lastly, when we update
                // the filter GUI, the expansion set checkboxes will naturally disappear do to the way
                // I implemented the checkbox populating system; it goes from nodes up to expansion sets
                // (and similarly, from nodes to ontologies). So if all the nodes of an expansion set
                // are deleted, regardless of how or in what order that occurs, the checkbox will disappear.
                // The same goes for ontologies.
                // When we undo...the checkboxes aren't necessarily in the correct state, but they do
                // re-appear.
                // Gather active checkbox
                var deletionSet = new DeletionSet.DeletionSet(_this.graph, _this.undoRedoBoss);

                // Add expansion sets, ontologies, and individual nodes all on the basis
                // of their hidden status (as determined via CSS classes set by the filters).
                // We are actually pretty agnostic about how they got that way...but if we
                // use that CSS class via any other thing that filter boxes, we could have a problem.
                // Trying to use filter statuses directly would have worse repercussions.
                var outerThis = _this;

                // Grab all the hidden nodes, but make sure we only get the g elements. If we change what elements
                // get the hiding class value, we will have an issue...but that change could be applying it to more
                // than one element corresponding to the same node...so I made it restrictive.
                var hiddenNodeElements = $("." + GraphView.BaseGraphView.hiddenNodeClass).filter("." + GraphView.BaseGraphView.nodeGSvgClassSansDot);
                var nodesToAdd = [];
                hiddenNodeElements.each(function (i, element) {
                    var nodeId = element["id"];

                    // We construct node_g ids like this: "node_g_"+d.conceptUriForIds
                    // So simply remove that prefix from the id to get the node model's id.
                    nodeId = nodeId.replace(GraphView.BaseGraphView.nodeGSvgClassSansDot + "_", "");
                    var node = outerThis.graph.getNodeByIdUri(nodeId);
                    nodesToAdd.push(node);
                });
                deletionSet.addAll(nodesToAdd);

                // Execute the deletion by "redoing" the deletion set.
                // For other commands, this isn't necessarily possible, but when
                // it is, it is preferable to having duplicate code in redo and where
                // the command is created (and applied).
                deletionSet.getGraphModifier().executeRedo();
            };
        };

        NodeDeleterWidgets.prototype.revealUnselectedCheckboxesLambda = function () {
            var _this = this;
            var outerThis = this;
            return function () {
                // Refresh all the checkboxes.
                _this.graphView.revealAllNodesAndRefreshFilterCheckboxes();
            };
        };
        return NodeDeleterWidgets;
    })();
    exports.NodeDeleterWidgets = NodeDeleterWidgets;
});
