///<reference path="headers/require.d.ts" />

///<amd-dependency path="./Utils" />
///<amd-dependency path="./FilterWidget" />
///<amd-dependency path="./Menu" />
///<amd-dependency path="./GraphView" />
///<amd-dependency path="./Concepts/ConceptGraph" />

import Utils = require("./Utils");
import FilterWidget = require("./FilterWidget");
import Menu = require("./Menu");
import GraphView = require("./GraphView");
import ConceptGraph = require("./Concepts/ConceptGraph");

export class AbstractNodeFilterWidget<N extends GraphView.BaseNode, L extends GraphView.BaseLink<GraphView.BaseNode>> extends FilterWidget.AbstractFilterWidget implements FilterWidget.IFilterWidget {
    
    static SOME_SELECTED_CSS = "filterCheckboxSomeSelected";
    
    public implementation: INodeFilterWidget<N, L>
    
    constructor(
        subMenuTitle: string,
        public graphView: GraphView.GraphView<N, L>
        ){
        super(subMenuTitle);
    }
    
    updateFilterUI(){
        // Remove missing ones, whatever is left over in this collection
        var checkboxSpanClass = this.getCheckboxSpanClass();
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
                                outerThis.implementation.checkboxHoveredLambda(node)
                            )
                        .mouseleave(
                                outerThis.implementation.checkboxUnhoveredLambda(node)
                            )
                        .append(
                            $("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "")
                            .addClass(this.getCheckboxClass())
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
    
    /**
     * Sets all checkboxes to be checked. Does not (appear!) to *trigger* the checkboxes though; this affects
     * the view only.
     */
    checkmarkAllCheckboxes(){
        $("."+this.getCheckboxClass()).prop("checked", "checked").removeClass(AbstractNodeFilterWidget.SOME_SELECTED_CSS);
    }
    
}

export interface INodeFilterWidget<N extends GraphView.BaseNode, L extends GraphView.BaseLink<GraphView.BaseNode>> extends AbstractNodeFilterWidget<N, L> {
    
    generateCheckboxLabel(node: N): string;
    
    generateColoredSquareIndicator(node: N): string;
    
    styleAsCentralNode(node: N): boolean;
    
    computeCheckId(node: N): string;
    
    computeCheckboxElementDomain(node: N): Array<N>;
    
    checkboxChanged(checkboxContextData: N, setOfHideCandidates: Array<N>, checkboxIsChecked: JQuery): void;
    
    checkboxHoveredLambda(nodeRelatedToCheckbox: N): (event: JQueryMouseEventObject)=>void;
    
    checkboxUnhoveredLambda(nodeRelatedToCheckbox: N): (event: JQueryMouseEventObject)=>void;
    
    getHoverNeedsAdjacentHighlighting(): boolean;

    updateCheckboxStateFromView(affectedNodes: N[]): void;
}