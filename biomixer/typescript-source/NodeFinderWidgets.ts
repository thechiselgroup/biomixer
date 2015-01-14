///<reference path="headers/require.d.ts" />
///<reference path="headers/d3.d.ts" />

///<amd-dependency path="GraphView" />
///<amd-dependency path="Menu" />

import GraphView = require("./GraphView");
import Menu = require("./Menu");

export class NodeFinder<N extends GraphView.BaseNode, L extends GraphView.BaseLink<any>> {

    static messageTextId = "singleNodeImportMessageBoxTextArea";
    static messageParagraphId = "singleNodeImportMessageBoxMessage";
    static messageDivId = "singleNodeImportMessageBox";
    static messageDivClass  ="singleNodeImportMessageBoxWithField";
    static messageBoxButtonClass = "singleNodeImportMessageBoxButton";
    static nodeImporterFooterDiv = "singleNodeImportFooterDiv";
    
    static locateNodesInputClass = "locateNodeByNameInput";
    static locateNodesButtonClass = "locateNodeByNameButtonIcon";
    static menuExpanderButton = "menuExpanderButton";
    static locateNodesButtonText = "Locate nodes in graph based on name";
    
    constructor(
        public graphView: GraphView.GraphView<N, L>,
        public graphModel: GraphView.Graph<N>
    ){
        
    }
    
    addMenuComponents(menuSelector: string){
        // Add the butttons to the pop-out panel
        var layoutsContainer = $("<div>").attr("id", "nodeFinderMenuContainer");
        $(menuSelector).append(layoutsContainer);

        var searchInput = $("<input>")
                .addClass(NodeFinder.locateNodesInputClass)
                .attr("id", "findNodeInputBox");

        var findFunc = this.highlightNodeNameMatches(this.graphModel, this.graphView, searchInput);
        
        var searchButton = $("<div>")
                .attr("id", "nodeNameSearchButton")
                .addClass(NodeFinder.locateNodesButtonClass)
                .addClass(NodeFinder.menuExpanderButton)
                .attr("title", NodeFinder.locateNodesButtonText)
            ;
                
        layoutsContainer.append($("<label for=findNodeInputBox>").addClass(Menu.Menu.menuLabelClass).text("Node Utilities"));
        var searchDiv = $("<div>").addClass("clearfix");
        searchDiv.append(searchInput);
        searchDiv.append(searchButton);
        layoutsContainer.append(searchDiv);
        layoutsContainer.append($("<br>"));
        
        layoutsContainer.append($("<input>")
                .addClass("addSingleConceptButton")
                .addClass("nodeCommandButton")
                .attr("id", "addSingleConceptButton")
                .attr("type", "button")
                .attr("value", "Add Concept Using URI"));
        d3.selectAll("#addSingleConceptButton").on("click", this.showSingleNodeImportDialog());
        var footer = $("<div>").attr("id", NodeFinder.nodeImporterFooterDiv).css("clear", "both");
        layoutsContainer.append($("<br>"));
        layoutsContainer.append(footer);
    
        layoutsContainer.append($("<br>"));
        
        searchInput
            .on("keydown", (event: JQueryKeyEventObject)=>{
                    if(event.which === 13){ // enter is 13
                        event.preventDefault();
                        findFunc();
                    }
                    // console.log(event.keyCode);
                })
        ;
        
        // for the button, after click or Enter key press, move focus back to the input box for nicer interactions.
        searchButton
            .on("click", ()=>{ findFunc(); searchInput.focus(); })
            .on("keydown", (event: JQueryKeyEventObject)=>{
                    if(event.which === 13){ // enter is 13
                        event.preventDefault();
                        findFunc();
                        searchInput.focus();
                    }
                    // console.log(event.keyCode);
                })
        ;
        
    }

    private highlightNodeNameMatches(graphModel: GraphView.Graph<N>, graphView: GraphView.GraphView<N, L>, textInput: JQuery){
        return ()=>{
            graphView.animateHighlightNodesDeactivate();
            var trimmed = $.trim(textInput.val());
            if("" === trimmed){
                return;
            }
            var matchingNodes = graphModel.findNodesByName(trimmed);
            graphView.animateHighlightNodesActivate(matchingNodes);
        }
    }
    
    private showSingleNodeImportDialog(){
        return ()=>{
            var dialog = $("#"+NodeFinder.messageDivId);
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
            var dialog = $("#"+NodeFinder.messageDivId);
            var messageField = $("#"+NodeFinder.messageTextId);
            var importData = messageField.first().val();
            dialog.slideUp(200, ()=>{ dialog.detach() });
            
            if(importData.length === 0){
                return;
            }
            this.graphModel.addNodeToGraph(importData);
        }  
    }
    
    private closeDialogLambda(){
        return (event: JQueryEventObject)=>{
            var dialog = $("#"+NodeFinder.messageDivId);
            dialog.slideUp(200, ()=>{ dialog.detach() });
        }
    }
    
    private messagePrompt(message: string, fieldContent: string, okCallback){
        // Remove any existing version of this panel. It is an embedded modal singleton unique as a unicorn.
        var dialog = $("#"+NodeFinder.messageDivId);
        if(undefined !== dialog){
            dialog.detach();
        }
        
        // Create the new one.
        dialog = $("<div>").attr("id", NodeFinder.messageDivId).addClass(NodeFinder.messageDivClass).addClass("opaqueMenu");
        var messageParagraph = $("<p>").addClass(NodeFinder.messageParagraphId);
        messageParagraph.text(message);
        var messageField = $("<textarea>").attr("id", NodeFinder.messageTextId).addClass(NodeFinder.messageTextId);
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
            cancelButton =  $("<button>").addClass(NodeFinder.messageBoxButtonClass).addClass("addSingleConceptButton")
                .text("Cancel").click(this.closeDialogLambda());
            okButtonText = "Apply";
        }
        var okButton = $("<button>").addClass(NodeFinder.messageBoxButtonClass).addClass("addSingleConceptButton")
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
        $("#"+NodeFinder.nodeImporterFooterDiv).append(dialog);
        dialog.slideDown("fast");
        
    }
    
}