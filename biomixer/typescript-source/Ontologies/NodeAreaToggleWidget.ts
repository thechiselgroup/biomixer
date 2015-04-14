///<amd-dependency path="GraphView" />
///<amd-dependency path="Menu" />

import GraphView = require("../GraphView");
import OntologyGraphView = require("./OntologyMappingOverview");
import Menu = require("../Menu");

export class NodeAreaToggleWidgets {

    static usePercentile: boolean = true;
    
    static toggleButtonLabelId = "percentileToggleButtonLabel"
    
    static singleNodeImportButtonClass = "nodeAreaToggleBoxButton";
    static nodeAdditionText = "To change the node areas from percentage to ";
        
    static nodeUtilityContainer = "nodeUtilityContainer";
    static locateNodesButtonText = "Toggle node areas from counts to percentage and back";
    
    constructor(
        public graphView: OntologyGraphView.OntologyMappingOverview,
        public graphModel: GraphView.Graph<any>
    ){
        
    }
    
    addMenuComponents(menuSelector: string, defaultHideContainer: boolean){

        var containers = Menu.Menu.slideToggleHeaderContainer("nodeFinderMenuContainer"+"OuterContainer", "nodeFinderMenuContainer"+"ScrollContainer", "Node Utilities", defaultHideContainer);
        var layoutsContainer = containers.inner;

        $(menuSelector).append(containers.outer);
        var toggleButtonLabel = $("<label>")
                        .text("Toggle Percentile") 
                        .attr("id", NodeAreaToggleWidgets.toggleButtonLabelId)
                        .css("padding-top", "2px")
                        .css("padding-bottom", "-2px")
                        .addClass("unselectable")
                        .addClass("plainBoxButton");
        // Update the text once it is attached to the DOM
        var toggleFunc = ()=>{
            this.flipProportionalPercentageState(this.graphModel, this.graphView);
            this.updateLabelText();
        };
        var toggleButton = $("<div>")
                .attr("id", "proportionalAreaToggleButton")
                .addClass("unselectable")
                .addClass("boxButton")
                .attr("title", NodeAreaToggleWidgets.locateNodesButtonText)
                .on("click", toggleFunc)
                .append(
                    $("<div>")
                        .addClass("unselectable")
                )
                .append(toggleButtonLabel)
            ;
        
        var searchDiv = $("<div>").addClass(NodeAreaToggleWidgets.nodeUtilityContainer).addClass("clearfix");
        searchDiv.append(toggleButton);
        layoutsContainer.append(searchDiv);
        
        // Set up label text now that it is attached to DOM
        this.updateLabelText();
        
    }
    
    private updateLabelText(){
        if(NodeAreaToggleWidgets.usePercentile){
            $("#"+NodeAreaToggleWidgets.toggleButtonLabelId).text("Toggle Nodes To Percentile Area")
        } else {
            $("#"+NodeAreaToggleWidgets.toggleButtonLabelId).text("Toggle Nodes To Absolute Area")
        }
    }

    private flipProportionalPercentageState(graphModel: GraphView.Graph<any>, graphView: OntologyGraphView.OntologyMappingOverview){
        NodeAreaToggleWidgets.usePercentile = !NodeAreaToggleWidgets.usePercentile;
        graphView.renderScaler.updateNodeScalingFactor();
    }
    
}