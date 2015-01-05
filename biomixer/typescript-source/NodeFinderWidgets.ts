///<reference path="headers/require.d.ts" />
///<reference path="headers/d3.d.ts" />

///<amd-dependency path="GraphView" />
///<amd-dependency path="Menu" />

import GraphView = require("./GraphView");
import Menu = require("./Menu");

export class NodeFinder<N extends GraphView.BaseNode, L extends GraphView.BaseLink<any>> {

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
                .attr("id", "findNodeInputBox");

        var findFunc = this.highlightNodeNameMatches(this.graphModel, this.graphView, searchInput);
        
        var searchButton = $("<input>")
                .attr("class", "buttonWithMargins")
                .attr("id", "searchButton")
                .attr("type", "button")
                .attr("value", "Find")
                
            ;
                
        layoutsContainer.append($("<label for=findNodeInputBox>").addClass(Menu.Menu.menuLabelClass).text("Find Node Names Containing... "));
        layoutsContainer.append(searchInput);
        layoutsContainer.append(searchButton);
        layoutsContainer.append($("<br>"));
        
        searchInput
            .on("keydown", (event: JQueryKeyEventObject)=>{
                    if(event.which === 13){ // enter is 13
                        event.preventDefault();
                        findFunc();
                    }
                    console.log(event.keyCode);
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
                    console.log(event.keyCode);
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
    
}