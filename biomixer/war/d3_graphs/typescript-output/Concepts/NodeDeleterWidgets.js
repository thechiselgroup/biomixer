define(["require", "exports", "../Menu", "../JQueryExtension", "GraphView", "Menu", "Concepts/ConceptPathsToRoot", "Concepts/ConceptGraph", "Concepts/CherryPickConceptFilter", "Concepts/OntologyConceptFilter", "Concepts/ExpansionSetFilter", "DeletionSet", "CompositeExpansionDeletionSet", "UndoRedo/UndoRedoManager"], function (require, exports, Menu) {
    var NodeDeleterWidgets = (function () {
        function NodeDeleterWidgets(graph, graphView, undoRedoBoss) {
            this.graph = graph;
            this.graphView = graphView;
            this.undoRedoBoss = undoRedoBoss;
        }
        NodeDeleterWidgets.prototype.addMenuComponents = function (menuSelector) {
            var deleterContainer = $("<div>").attr("id", "nodeDeletionMenuContainer");
            $(menuSelector).append(deleterContainer);
            deleterContainer.append($("<label>").addClass(Menu.Menu.menuLabelClass).text("Node Management"));
            deleterContainer.append($("<br>"));
            {
                deleterContainer.append($("<input>").addClass("addSingleConceptButton").addClass("nodeCommandButton").attr("id", "addSingleConceptButton").attr("type", "button").attr("value", "Add Concept Using URI"));
                d3.selectAll("#addSingleConceptButton").on("click", this.showSingleNodeImportDialog());
                var footer = $("<div>").attr("id", NodeDeleterWidgets.nodeImporterFooterDiv).css("clear", "both");
                deleterContainer.append($("<br>"));
                deleterContainer.append(footer);
            }
            deleterContainer.append($("<br>"));
            deleterContainer.append($("<br>"));
            {
                deleterContainer.append($("<input>").attr("class", "nodeDeleterButton nodeCommandButton").attr("id", "nodeDeleterButton").attr("type", "button").attr("value", "Remove All Unchecked Nodes"));
                d3.selectAll("#nodeDeleterButton").on("click", this.graphView.deleteSelectedCheckboxesLambda());
            }
            {
                deleterContainer.append($("<input>").attr("class", "nodeUnhiderButton nodeCommandButton").attr("id", "nodeUnhiderButton").attr("type", "button").attr("value", "Reset All Node Checkboxes"));
                d3.selectAll("#nodeUnhiderButton").on("click", this.revealUnselectedCheckboxesLambda());
            }
            deleterContainer.append($("<br>"));
        };
        NodeDeleterWidgets.prototype.showSingleNodeImportDialog = function () {
            var _this = this;
            return function () {
                var dialog = $("#" + NodeDeleterWidgets.messageDivId);
                if (dialog.length != 0) {
                    dialog.slideUp(200, function () {
                        dialog.detach();
                    });
                }
                else {
                    var message = "To import a single node, paste the URI id (found via BioPortal) into the field.";
                    _this.messagePrompt(message, "", _this.importSingleNodeCallbackLambda());
                }
            };
        };
        NodeDeleterWidgets.prototype.importSingleNodeCallbackLambda = function () {
            var _this = this;
            return function (event) {
                event.stopPropagation();
                var dialog = $("#" + NodeDeleterWidgets.messageDivId);
                var messageField = $("#" + NodeDeleterWidgets.messageTextId);
                var importData = messageField.first().val();
                dialog.slideUp(200, function () {
                    dialog.detach();
                });
                if (importData.length === 0) {
                    return;
                }
                _this.graph.addNodeToGraph(importData);
            };
        };
        NodeDeleterWidgets.prototype.closeDialogLambda = function () {
            return function (event) {
                var dialog = $("#" + NodeDeleterWidgets.messageDivId);
                dialog.slideUp(200, function () {
                    dialog.detach();
                });
            };
        };
        NodeDeleterWidgets.prototype.messagePrompt = function (message, fieldContent, okCallback) {
            var dialog = $("#" + NodeDeleterWidgets.messageDivId);
            if (undefined !== dialog) {
                dialog.detach();
            }
            dialog = $("<div>").attr("id", NodeDeleterWidgets.messageDivId).addClass(NodeDeleterWidgets.messageDivClass).addClass("opaqueMenu");
            var messageParagraph = $("<p>").addClass(NodeDeleterWidgets.messageParagraphId);
            messageParagraph.text(message);
            var messageField = $("<textarea>").attr("id", NodeDeleterWidgets.messageTextId).addClass(NodeDeleterWidgets.messageTextId);
            messageField.text(fieldContent);
            messageField.select();
            var cancelButton = undefined;
            var okButtonText;
            if (null === okCallback) {
                okCallback = this.closeDialogLambda();
                okButtonText = "Close";
            }
            else {
                cancelButton = $("<button>").addClass(NodeDeleterWidgets.messageBoxButtonClass).addClass("addSingleConceptButton").addClass("plainBoxButton").text("Cancel").click(this.closeDialogLambda());
                okButtonText = "Apply";
            }
            var okButton = $("<button>").addClass(NodeDeleterWidgets.messageBoxButtonClass).addClass("addSingleConceptButton").addClass("plainBoxButton").text(okButtonText).click(okCallback);
            dialog.append(messageParagraph).append(messageField).append($("<br>")).append(okButton);
            if (undefined !== cancelButton) {
                dialog.append(cancelButton);
            }
            dialog.css("display", "none");
            $("#" + NodeDeleterWidgets.nodeImporterFooterDiv).append(dialog);
            dialog.slideDown("fast");
        };
        NodeDeleterWidgets.prototype.revealUnselectedCheckboxesLambda = function () {
            var _this = this;
            var outerThis = this;
            return function () {
                _this.graphView.revealAllNodesAndRefreshFilterCheckboxes();
            };
        };
        NodeDeleterWidgets.messageTextId = "singleNodeImportMessageBoxTextArea";
        NodeDeleterWidgets.messageParagraphId = "singleNodeImportMessageBoxMessage";
        NodeDeleterWidgets.messageDivId = "singleNodeImportMessageBox";
        NodeDeleterWidgets.messageDivClass = "singleNodeImportMessageBoxWithField";
        NodeDeleterWidgets.messageBoxButtonClass = "singleNodeImportMessageBoxButton";
        NodeDeleterWidgets.nodeImporterFooterDiv = "singleNodeImportFooterDiv";
        return NodeDeleterWidgets;
    })();
    exports.NodeDeleterWidgets = NodeDeleterWidgets;
});
