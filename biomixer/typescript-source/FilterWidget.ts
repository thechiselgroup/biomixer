///<reference path="headers/require.d.ts" />

///<amd-dependency path="./Utils" />
///<amd-dependency path="./Menu" />
///<amd-dependency path="./GraphView" />

import Utils = require("./Utils");
import Menu = require("./Menu");
import GraphView = require("./GraphView");

export class FilterWidget<N extends GraphView.BaseNode, L extends GraphView.BaseLink<GraphView.BaseNode>> {

    
    filterContainer: JQuery;
    
    public className: string;
    
    public subMenuTitle: string;
    
    constructor(
        public graphView: GraphView.GraphView<N, L>
        ){
        this.className =  Utils.getClassName(this);
    }
    
    addMenuComponents(menuSelector: string){
        this.filterContainer = $("<div>").attr("id", this.className+"Container").addClass("scroll-div").css("height", 100);
        $(menuSelector).append(this.filterContainer);
        
        this.filterContainer.append($("<label>").addClass(Menu.Menu.menuLabelClass).text(this.subMenuTitle));
    }
    
    updateFilterUI(){
        // Remove missing ones, whatever is left over in this collection
        var checkboxSpanClass = this.className+"_filterCheckboxSpan";
        var preExistingCheckboxes = $("."+checkboxSpanClass);
        var newCheckboxes = [];
        var outerThis = this;
        
        // Can I generalize this sorting and node group for when we will have expansion sets? Maybe...
        var sortedNodes = this.graphView.sortConceptNodesCentralOntologyName();
        
        
        // Add new ones
        $.each(sortedNodes, (i, node: N) =>
            {
                var checkId = this.computeCheckId(node);
                var spanId = "span_"+checkId;
                if(0 === $("#"+spanId).length){
                    var nodeHideCandidates = this.computeCheckboxElementDomain(node);
                    var checkboxLabel = this.generateCheckboxLabel(node);
                    var checkboxColoredSquare = this.generateColoredSquareIndicator(node);
                    
                    this.filterContainer.append(
                    $("<span>").attr("id", spanId).addClass(checkboxSpanClass)
                        .mouseenter(outerThis.checkboxHoveredLambda(node))
                        .mouseleave(outerThis.checkboxUnhoveredLambda(node))
                        .append("<br>")
                        .append(
                            $("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "")
                            .change(
                                function(){ outerThis.checkboxChanged(node, nodeHideCandidates, $(this)) }
                            )
                        )
                        .append(
                            $("<label>").attr("for",checkId)
                            .append(checkboxColoredSquare+"&nbsp;"+checkboxLabel)
                            // The ontology checkbox corresponding to the central concept's ontology will be styled.
                            .toggleClass("centralNode", this.styleAsCentralNode(node))
                        )
                    );
                    newCheckboxes.push($(spanId));
                }
            }
        );
        
        // This was bugging things up. Why did I think I needed it?
        // TODO Was this to prevent a different problem?
//        // Keep only those checkboxes for which we looped over a node
//        preExistingCheckboxes.not(newCheckboxes).remove();
    }
    
    // TODO These should not be empty implementations. Can I rig an interface to enforce
    // these without empty bodies like I did with the GraphView?
    
    generateCheckboxLabel(node: N): string {
        return "";
    }
    
    generateColoredSquareIndicator(node: N): string {
        return "";
    }
    
    styleAsCentralNode(node: N): boolean {
        return false;
    }
    
    computeCheckId(node: N): string {
        return "";
    }
    
    computeCheckboxElementDomain(node: N): Array<N> {
        return null;
    }
    
    checkboxChanged(checkboxContextData: N, setOfHideCandidates, checkboxIsChecked: JQuery){
        return null;
    }
    
    checkboxHoveredLambda(node: N): (event: JQueryMouseEventObject)=>void {
        return function(event: JQueryMouseEventObject){};
    }
    
    checkboxUnhoveredLambda(node: N): (event: JQueryMouseEventObject)=>void {
        return function(event: JQueryMouseEventObject){};
    }
  
}