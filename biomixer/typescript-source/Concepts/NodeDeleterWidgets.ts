///<reference path="headers/require.d.ts" />
///<reference path="headers/d3.d.ts" />

///<amd-dependency path="../JQueryExtension" />

///<amd-dependency path="GraphView" />
///<amd-dependency path="Menu" />
///<amd-dependency path="Concepts/ConceptPathsToRoot" />
///<amd-dependency path="Concepts/ConceptGraph" />
///<amd-dependency path="Concepts/CherryPickConceptFilter" />
///<amd-dependency path="Concepts/OntologyConceptFilter" />
///<amd-dependency path="Concepts/ExpansionSetFilter" />
///<amd-dependency path="DeletionSet" />
///<amd-dependency path="CompositeExpansionDeletionSet" />
///<amd-dependency path="UndoRedo/UndoRedoManager" />



import GraphView = require("../GraphView");
import Menu = require("../Menu");
import PathToRootView = require("./ConceptPathsToRoot");
import ConceptGraph = require("./ConceptGraph");
import CherryPickConceptFilter = require("./CherryPickConceptFilter");
import OntologyConceptFilter = require("./OntologyConceptFilter");
import ExpansionSetFilter = require("./ExpansionSetFilter");
import DeletionSet = require("../DeletionSet");
import CompositeExpansionDeletionSet = require("../CompositeExpansionDeletionSet");
import UndoRedoManager = require("../UndoRedo/UndoRedoManager");

// Deprecated. This functionality has moved to other UI components.
export class NodeDeleterWidgets {

    constructor(
        public graph: ConceptGraph.ConceptGraph,
        public graphView: PathToRootView.ConceptPathsToRoot,
        private undoRedoBoss: UndoRedoManager.UndoRedoManager
    ){
        
    }

    addMenuComponents(menuSelector: string){
        // Add the butttons to the pop-out panel
        var deleterContainer = $("<div>").attr("id", "nodeDeletionMenuContainer");
        $(menuSelector).append(deleterContainer);
        deleterContainer.append($("<label>").addClass(Menu.Menu.menuLabelClass).text("Node Management"));
        deleterContainer.append($("<br>"));
        
        {
            deleterContainer.append($("<input>")
                    .attr("class", "addSingleConceptButton nodeCommandButton")
                    .attr("id", "addSingleConceptButton")
                    .attr("type", "button")
                    .attr("value", "Add Concept Using URI"));
            d3.selectAll("#addSingleConceptButton").on("click", this.showSingleNodeImportDialog());
            var footer = $("<div>").attr("id", NodeDeleterWidgets.nodeImporterFooterDiv).css("clear", "both");
            deleterContainer.append($("<br>"));
            deleterContainer.append(footer);
        }
    
        deleterContainer.append($("<br>"));
        deleterContainer.append($("<br>"));
    
        {
            deleterContainer.append($("<input>")
                    .attr("class", "nodeDeleterButton nodeCommandButton")
                    .attr("id", "nodeDeleterButton")
                    .attr("type", "button")
                    .attr("value", "Remove All Unchecked Nodes"));
        
            d3.selectAll("#nodeDeleterButton").on("click", this.graphView.deleteSelectedCheckboxesLambda());
        }
        
        {
            deleterContainer.append($("<input>")
                    .attr("class", "nodeUnhiderButton nodeCommandButton")
                    .attr("id", "nodeUnhiderButton")
                    .attr("type", "button")
                    .attr("value", "Reset All Node Checkboxes"));
        
            d3.selectAll("#nodeUnhiderButton").on("click", this.revealUnselectedCheckboxesLambda());
        }

        deleterContainer.append($("<br>"));
    }
    
    private showSingleNodeImportDialog(){
        return ()=>{
            var dialog = $("#"+NodeDeleterWidgets.messageDivId);
            if(dialog.length != 0){
                // If it's there already, hide it. This button can act as a toggle.
                dialog.slideUp(200, ()=>{ dialog.detach() });
            } else {
                var message = "To import a single node, paste the URI id (found via BioPortal) into the field."
                    ;
                this.messagePrompt(message, "", this.importSingleNodeCallbackLambda());
            }
        };
    }
    
    private importSingleNodeCallbackLambda(){
        return (event: JQueryEventObject)=>{
            event.stopPropagation();
            var dialog = $("#"+NodeDeleterWidgets.messageDivId);
            var messageField = $("#"+NodeDeleterWidgets.messageTextId);
            var importData = messageField.first().val();
            dialog.slideUp(200, ()=>{ dialog.detach() });
            
            if(importData.length === 0){
                return;
            }
            this.graph.addNodeToGraph(importData);
        }  
    }
    
    static messageTextId = "singleNodeImportMessageBoxTextArea";
    static messageParagraphId = "singleNodeImportMessageBoxMessage";
    static messageDivId = "singleNodeImportMessageBox";
    static messageDivClass  ="singleNodeImportMessageBoxWithField";
    static messageBoxButtonClass = "singleNodeImportMessageBoxButton";
    static nodeImporterFooterDiv = "singleNodeImportFooterDiv";
    
    private closeDialogLambda(){
        return (event: JQueryEventObject)=>{
            var dialog = $("#"+NodeDeleterWidgets.messageDivId);
            dialog.slideUp(200, ()=>{ dialog.detach() });
        }
    }
    
    private messagePrompt(message: string, fieldContent: string, okCallback){
        // Remove any existing version of this panel. It is an embedded modal singleton unique as a unicorn.
        var dialog = $("#"+NodeDeleterWidgets.messageDivId);
        if(undefined !== dialog){
            dialog.detach();
        }
        
        // Create the new one.
        dialog = $("<div>").attr("id", NodeDeleterWidgets.messageDivId).addClass(NodeDeleterWidgets.messageDivClass).addClass("opaqueMenu");
        var messageParagraph = $("<p>").addClass(NodeDeleterWidgets.messageParagraphId);
        messageParagraph.text(message);
        var messageField = $("<textarea>").attr("id", NodeDeleterWidgets.messageTextId).addClass(NodeDeleterWidgets.messageTextId);
        messageField.text(fieldContent);
        messageField.select();
        
        // Default the ok button to close the box. If it is to something more useful, then create a cancel button
        // to allow the user to simply close the box.
        var cancelButton = undefined;
        var okButtonText;
        if(null === okCallback){
            okCallback = this.closeDialogLambda();
            okButtonText = "Close";
        } else {
            cancelButton =  $("<button>").addClass(NodeDeleterWidgets.messageBoxButtonClass).addClass("addSingleConceptButton")
                .text("Cancel").click(this.closeDialogLambda());
            okButtonText = "Apply";
        }
        var okButton = $("<button>").addClass(NodeDeleterWidgets.messageBoxButtonClass).addClass("addSingleConceptButton")
            .text(okButtonText).click(okCallback);
        dialog
            .append(messageParagraph)
            .append(messageField)
            .append($("<br>"))
            .append(okButton)
        ;
        if(undefined !== cancelButton){
            dialog.append(cancelButton);
        }
        
        
        dialog.css("display", "none");
        $("#"+NodeDeleterWidgets.nodeImporterFooterDiv).append(dialog);
        dialog.slideDown("fast");
        
    }
    
    revealUnselectedCheckboxesLambda(){
        var outerThis = this;
        return ()=>{
            // Refresh all the checkboxes.
            this.graphView.revealAllNodesAndRefreshFilterCheckboxes();
        }
    }

}