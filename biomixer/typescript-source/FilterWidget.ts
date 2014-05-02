///<reference path="headers/require.d.ts" />

///<amd-dependency path="./Utils" />
///<amd-dependency path="./Menu" />
///<amd-dependency path="./GraphView" />

import Utils = require("./Utils");
import Menu = require("./Menu");
import GraphView = require("./GraphView");

export class FilterWidget<N extends GraphView.BaseNode, L extends GraphView.BaseLink<GraphView.BaseNode>> {
    
    public implementation: IFilterWidget<N, L>
    
    filterContainer: JQuery;
    
    public className: string;
    
    public subMenuTitle: string;
    
    constructor(
        public graphView: GraphView.GraphView<N, L>
        ){
        this.className =  Utils.getClassName(this);
    }
    
    addMenuComponents(menuSelector: string){
        var outerContainer = $("<div>").attr("id", this.className+"OuterContainer");
        $(menuSelector).append(outerContainer);
        
        outerContainer.append(
            $("<label>")
                .addClass(Menu.Menu.menuLabelClass).text(this.subMenuTitle));
        
        this.filterContainer = $("<div>").attr("id", this.className+"ScrollContainer")
                .addClass("scroll-div").css("height", 100);
        
        outerContainer.append(this.filterContainer);
        
    }
    
    updateFilterUI(){
        // Remove missing ones, whatever is left over in this collection
        var checkboxSpanClass = this.className+"_filterCheckboxSpan";
        var preExistingCheckboxes = $("."+checkboxSpanClass);
        var checkboxesPopulatedOrReUsed = $("");
        var outerThis = this;
        
        // Can I generalize this sorting and node group for when we will have expansion sets? Maybe...
        var sortedNodes = this.graphView.sortConceptNodesCentralOntologyName();

        // Add new ones
        $.each(sortedNodes, (i, node: N) =>
            {
                var checkId = this.implementation.computeCheckId(node);
                var spanId = "span_"+checkId;
                if(0 === $("#"+spanId).length){
                    // We store some arbitrary containers of nodes to hide for each checkbox. Seems data consumptive.
                    
                    var checkboxLabel = this.implementation.generateCheckboxLabel(node);
                    var checkboxColoredSquare = this.implementation.generateColoredSquareIndicator(node);
                    
                    this.filterContainer.append(
                    $("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox")
                        .mouseenter(
                                function(){ 
                                    var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(node);
                                    outerThis.implementation.checkboxHoveredLambda(nodeHideCandidates);
                                }
                            )
                        .mouseleave(
                                function(){ 
                                    var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(node);
                                    outerThis.implementation.checkboxUnhoveredLambda(nodeHideCandidates);
                                }
                            )
//                        .append("<br>")
                        .append(
                            $("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "")
                            .change(
                                function(){ 
                                    var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(node);
                                    outerThis.implementation.checkboxChanged(node, nodeHideCandidates, $(this));
                                }
                            )
                        )
                        .append(
                            $("<label>").attr("for",checkId)
                            .append(checkboxColoredSquare+"&nbsp;"+checkboxLabel)
                            // The ontology checkbox corresponding to the central concept's ontology will be styled.
                            .toggleClass("centralNode", this.implementation.styleAsCentralNode(node))
                        )
                    );
                }
                checkboxesPopulatedOrReUsed = checkboxesPopulatedOrReUsed.add("#"+spanId);
            }
        );
        
        // Keep only those checkboxes for which we looped over a node
        preExistingCheckboxes.not(checkboxesPopulatedOrReUsed).remove();
    }

}

export interface IFilterWidget<N extends GraphView.BaseNode, L extends GraphView.BaseLink<GraphView.BaseNode>> extends FilterWidget<N, L> {
    
    generateCheckboxLabel(node: N): string;
    
    generateColoredSquareIndicator(node: N): string;
    
    styleAsCentralNode(node: N): boolean;
    
    computeCheckId(node: N): string;
    
    computeCheckboxElementDomain(node: N): Array<N>;
    
    checkboxChanged(checkboxContextData: N, setOfHideCandidates: Array<N>, checkboxIsChecked: JQuery): void;
    
    checkboxHoveredLambda(setOfHideCandidates: Array<N>): (event: JQueryMouseEventObject)=>void;
    
    checkboxUnhoveredLambda(setOfHideCandidates: Array<N>): (event: JQueryMouseEventObject)=>void;
}
