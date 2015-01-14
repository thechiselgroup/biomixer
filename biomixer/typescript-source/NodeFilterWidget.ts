///<reference path="headers/require.d.ts" />

///<amd-dependency path="./Utils" />
///<amd-dependency path="./FilterWidget" />
///<amd-dependency path="./Menu" />
///<amd-dependency path="./GraphView" />
///<amd-dependency path="./Concepts/ConceptPathsToRoot" />
///<amd-dependency path="./Concepts/ConceptGraph" />

import Utils = require("./Utils");
import FilterWidget = require("./FilterWidget");
import Menu = require("./Menu");
import GraphView = require("./GraphView");
import ConceptPathsToRoot = require("./Concepts/ConceptPathsToRoot");
import ConceptGraph = require("./Concepts/ConceptGraph");

export class AbstractNodeFilterWidget<FilterTarget, N extends GraphView.BaseNode, L extends GraphView.BaseLink<GraphView.BaseNode>> extends FilterWidget.AbstractFilterWidget implements FilterWidget.IFilterWidget {
    
    static SOME_SELECTED_CSS = "filterCheckboxSomeSelected";
    
    public implementation: INodeFilterWidget<FilterTarget, N>
    
    constructor(
        subMenuTitle: string,
        public graphView: GraphView.GraphView<N, L>
        ){
        super(subMenuTitle);
    }
    
    /*
     * Override to add the checkbox reset and node delete buttons
     */
    addMenuComponents(menuSelector: string, defaultHideContainer: boolean): void {
        super.addMenuComponents(menuSelector, defaultHideContainer);
        // Important to use implementation, since it might override...
        this.addResetAndDeleteButtonsToMenuComponents(
            ()=>{this.implementation.checkmarkAllCheckboxes()},
            this.implementation.deleteSelectedCheckboxesLambda(()=>{ return this.getHiddenAssociatedNodes(); })
        );
    }
    
    /**
     * Get all the nodes associated with the filter, that are currently hidden.
     */
    getHiddenAssociatedNodes(): Array<N>{
        var targets: Array<FilterTarget> = this.implementation.getFilterTargets();
        var nodes: Array<N> = [];
        for(var i in targets){
            var targ = targets[i];
            var node: Array<N> = this.implementation.computeCheckboxElementDomain(targ);
            for(var j in node){
                var n = node[j];
                if(this.graphView.isNodeHidden(n)){
                    nodes.push(n);
                }
            }
        }

        return nodes;
    }
    
    updateFilterUI(){
        // Remove missing ones, whatever is left over in this collection
        var checkboxSpanClass = this.getCheckboxSpanClass();
        var preExistingCheckboxes = $("."+checkboxSpanClass);
        var checkboxesPopulatedOrReUsed = $("");
        var outerThis = this;
        
        // Can I generalize this sorting and node group for when we will have expansion sets? Maybe...
        var filterTargets = this.implementation.getFilterTargets();
        // Add new ones
        $.each(filterTargets, (i, target: FilterTarget) =>
            {
                var checkId = this.implementation.computeCheckId(target);
                var spanId = "span_"+checkId;
                if(0 === $("#"+spanId).length){
                    // We store some arbitrary containers of nodes to hide for each checkbox. Seems data consumptive.
                    
                    var checkboxLabel = this.implementation.generateCheckboxLabel(target);
                    var checkboxColoredSquare = this.implementation.generateColoredSquareIndicator(target);
                    
                    this.filterContainer.append(
                    $("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox")
                        .mouseenter(
                                outerThis.implementation.checkboxHoveredLambda(target)
                            )
                        .mouseleave(
                                outerThis.implementation.checkboxUnhoveredLambda(target)
                            )
                        .append(
                            $("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "")
                            .addClass(this.getCheckboxClass())
                            .change(
                                function(){
                                    var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(target);
                                    outerThis.implementation.checkboxChanged(target, nodeHideCandidates, $(this));
                                }
                            )
                        )
                        .append(
                            $("<label>").attr("for",checkId)
                            .append(checkboxColoredSquare+"&nbsp;"+checkboxLabel)
//                            // The ontology checkbox corresponding to the central concept's ontology will be styled.
//                            .toggleClass("centralNode", this.implementation.styleAsCentralNode(node))
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
     * Intended to trigger each un-checked checkbox in order to undo its action and change its state to checked.
     */
    checkmarkAllCheckboxes(){
        $("."+this.getCheckboxClass()+":not(:checked)")
            .trigger("click")
        ;
    }
    
}

export interface INodeFilterWidget<FilterTarget, N extends GraphView.BaseNode> extends AbstractNodeFilterWidget<FilterTarget, N, any> {

    generateCheckboxLabel(filterTarget: FilterTarget): string;
    
    generateColoredSquareIndicator(filterTarget: FilterTarget): string;
    
    //styleAsCentralNode(node: FilterTarget): boolean;
    
    computeCheckId(filterTarget: FilterTarget): string;
    
    computeCheckboxElementDomain(filterTarget: FilterTarget): Array<N>;
    
    /**
     * Gets the objects that the filter is interested in (e.g. nodes, ontologies, expansion sets...)
     */
    getFilterTargets(): Array<FilterTarget>;
    
    checkboxChanged(checkboxContextData: FilterTarget, setOfHideCandidates: Array<N>, checkboxIsChecked: JQuery): Array<N>;
    
    checkboxHoveredLambda(filterTargetRelatedToCheckbox: FilterTarget): (event: JQueryMouseEventObject)=>void;
    
    checkboxUnhoveredLambda(filterTargetRelatedToCheckbox: FilterTarget): (event: JQueryMouseEventObject)=>void;
    
    getHoverNeedsAdjacentHighlighting(): boolean;

    updateCheckboxStateFromView(affectedNodes: N[]): void;

    deleteSelectedCheckboxesLambda(computeNodesToDeleteFunc?: ()=>Array<N>);

}