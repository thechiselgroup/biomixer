///<reference path="headers/require.d.ts" />
///<reference path="headers/d3.d.ts" />

///<amd-dependency path="GraphView" />
///<amd-dependency path="Menu" />

import GraphView = require("./GraphView");
import Menu = require("./Menu");

export class NodeFinder<N extends GraphView.BaseNode, L extends GraphView.BaseLink<any>> {

    static singleNodeImportFieldId = "singleNodeImportMessageBoxTextArea";
    static singleNodeImportButtonClass = "singleNodeImportMessageBoxButton";
    static nodeAdditionText = "To import a single node, paste the URI id (found via BioPortal) into the field.";
    
    
    static locateNodesInputClass = "locateNodeByNameInput";
    static locateNodesButtonClass = "locateNodeByNameButtonIcon";
    static menuExpanderButton = "menuExpanderUtilityButton";
    static locateNodesButtonText = "Locate nodes in graph based on Name/Synonyms";
    
    constructor(
        public graphView: GraphView.GraphView<N, L>,
        public graphModel: GraphView.Graph<N>
    ){
        
    }
    
    addMenuComponents(menuSelector: string, defaultHideContainer: boolean){

        var containers = Menu.Menu.slideToggleHeaderContainer("nodeFinderMenuContainer"+"OuterContainer", "nodeFinderMenuContainer"+"ScrollContainer", "Node Utilities", defaultHideContainer);
        var layoutsContainer = containers.inner;

        $(menuSelector).append(containers.outer);

        var searchInput = $("<input>")
                .addClass(NodeFinder.locateNodesInputClass)
                .attr("title", NodeFinder.locateNodesButtonText)
                .attr("id", "findNodeInputBox");

        var findFunc = this.highlightNodeNameMatches(this.graphModel, this.graphView, searchInput);
        
        var searchButton = $("<div>")
                .attr("id", "nodeNameSearchButton")
                .addClass("unselectable")
                .attr("title", NodeFinder.locateNodesButtonText)
                .append(
                    $("<div>")
                        .addClass("unselectable")
                        .addClass(NodeFinder.locateNodesButtonClass)
                        .addClass(NodeFinder.menuExpanderButton)
                )
                .append(
                    $("<label>")
                        .text("Locate Node")
                        .css("padding-top", "2px")
                        .css("display", "block")
                )
            ;
        
        var searchDiv = $("<div>").addClass("clearfix");
        searchDiv.append(searchInput);
        searchDiv.append(searchButton);
        layoutsContainer.append(searchDiv);
        layoutsContainer.append($("<br>"));
        
        var addUriInput = $("<input>")
                .addClass(NodeFinder.locateNodesInputClass)
                .attr("id", NodeFinder.singleNodeImportFieldId).addClass(NodeFinder.singleNodeImportFieldId)
                ;

        var addUriButton =
            $("<label>")
                .addClass("unselectable")
                .addClass("addSingleConceptButton")
                .addClass(NodeFinder.singleNodeImportButtonClass)
                .css("padding-top", "1px")
                .css("display", "block")
                .text("Add Concept Using URI")
                .css("margin-left", "2px")
                .css("padding-left", "3px")
                .css("padding-right", "3px")
                .css("border-width", "1px")
                .css("border-style", "solid")
                .css("float", "left")
            ;
        
        var addUriDiv = $("<div>").addClass("clearfix")
            .attr("title", NodeFinder.nodeAdditionText)    
        ;
        addUriDiv.append(addUriInput);
        addUriDiv.append(addUriButton);
        layoutsContainer.append(addUriDiv);
        layoutsContainer.append($("<br>"));
        
        addUriButton
            .click(this.importSingleNodeCallbackLambda())
        ;
        
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
    
    
    private importSingleNodeCallbackLambda(){
        return (event: JQueryEventObject)=>{
            event.stopPropagation();
            var messageField = $("#"+NodeFinder.singleNodeImportFieldId);
            var importData = messageField.first().val();
            
            if(importData.length === 0){
                return;
            }
            this.graphModel.addNodeToGraph(importData);
            messageField.first().val("");
        }  
    }
    
}