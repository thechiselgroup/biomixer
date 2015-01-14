///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="../Utils" />
///<amd-dependency path="../NodeFilterWidget" />
///<amd-dependency path="./ConceptNodeFilterWidget" />
///<amd-dependency path="./CherryPickConceptFilter" />
///<amd-dependency path="./ConceptPathsToRoot" />
///<amd-dependency path="./ConceptGraph" />
///<amd-dependency path="../ExpansionSets" />
///<amd-dependency path="../Menu" />

import Utils = require("../Utils");
import FilterWidget = require("../NodeFilterWidget");
import ConceptFilterWidget = require("./ConceptNodeFilterWidget");
import ExpansionSetFilter = require("./ExpansionSetFilter");
import ConceptFilter = require("./CherryPickConceptFilter");
import PathToRoot = require("./ConceptPathsToRoot");
import ConceptGraph = require("./ConceptGraph");
import ExpansionSets = require("../ExpansionSets");
import Menu = require("../Menu");

/**
 * This forms a widget that is equivalent to the concept filter and the expansion set filter combined, with nested tree representation
 * of the filtering checkboxes. That is, under each expansion checkbox are the checkboxes for the concepts therein, which may be
 * hidden and revealed with a +/- collapse button.
 * 
 * It is implemented as a composition, driving the different behavior for the two checkbox types (concept and expansion set)
 * from classes that already implemented those as separate sets of widgets.
 */
export class NestedExpansionSetConceptFilter extends ConceptFilterWidget.AbstractConceptNodeFilterWidget<any> implements FilterWidget.INodeFilterWidget<ConceptGraph.RawAcronym, ConceptGraph.Node> {
    
    static SUB_MENU_TITLE = "Node Expansions Displayed";
    
    static NESTED_CONTAINER_CLASS = "nestedExpansionSetConceptContainer";
    
    static NESTED_FILTER_CLASSNAME_PREFIX = "NestedExpansionSetConceptFilter";
    
    pathToRootView: PathToRoot.ConceptPathsToRoot;
    
    expansionsFilter: ExpansionSetFilter.ExpansionSetFilter;
    conceptFilter: ConceptFilter.CherryPickConceptFilter;
    
    constructor(
        conceptGraph: ConceptGraph.ConceptGraph,
        graphView: PathToRoot.ConceptPathsToRoot,
        public centralConceptUri: ConceptGraph.ConceptURI
        ){
        super(NestedExpansionSetConceptFilter.SUB_MENU_TITLE, graphView, conceptGraph);
        this.implementation = this;
        this.pathToRootView = graphView;
        this.expansionsFilter = new ExpansionSetFilter.ExpansionSetFilter(conceptGraph, graphView);
        this.conceptFilter = new ConceptFilter.CherryPickConceptFilter(conceptGraph, graphView, centralConceptUri);
        
        this.expansionsFilter.modifyClassName(NestedExpansionSetConceptFilter.NESTED_FILTER_CLASSNAME_PREFIX); //_"+this.expansionFilter.getClassName());
        this.conceptFilter.modifyClassName(NestedExpansionSetConceptFilter.NESTED_FILTER_CLASSNAME_PREFIX); //_"+this.conceptFilter.getClassName());
    }
    
    generateCheckboxLabel(arg: ConceptGraph.RawAcronym): string;
    generateCheckboxLabel(arg: ConceptGraph.Node): string;
    generateCheckboxLabel(arg: any): string {
        return (Utils.getClassName(arg) === "ExpansionSet") ? this.expansionsFilter.generateCheckboxLabel(arg) : this.conceptFilter.generateCheckboxLabel(arg) ;
    }
    
    generateColoredSquareIndicator(arg: ConceptGraph.RawAcronym): string;
    generateColoredSquareIndicator(arg: ConceptGraph.Node): string;
    generateColoredSquareIndicator(arg: any): string {
        return (Utils.getClassName(arg) === "ExpansionSet") ? this.expansionsFilter.generateColoredSquareIndicator(arg) : this.conceptFilter.generateColoredSquareIndicator(arg) ;
    }
    
    computeCheckId(arg: ConceptGraph.RawAcronym): string;
    computeCheckId(arg: ConceptGraph.Node): string;
    computeCheckId(arg: any): string {
        return ( (Utils.getClassName(arg) === "ExpansionSet") ? this.expansionsFilter.computeCheckId(arg) : this.conceptFilter.computeCheckId(arg) );
    }
        
    computeCheckboxElementDomain(arg: ConceptGraph.RawAcronym): Array<ConceptGraph.Node>;
    computeCheckboxElementDomain(arg: ConceptGraph.Node): Array<ConceptGraph.Node>;
    computeCheckboxElementDomain(arg: any): Array<ConceptGraph.Node> {
        return (Utils.getClassName(arg) === "ExpansionSet") ? this.expansionsFilter.computeCheckboxElementDomain(arg) : this.conceptFilter.computeCheckboxElementDomain(arg) ;
    }
  
    getFilterTargets(): Array<any> {
        var concepts: Array<ConceptGraph.Node> = this.conceptFilter.getFilterTargets();
        var ontologies: Array<ExpansionSets.ExpansionSet<ConceptGraph.Node>> = this.expansionsFilter.getFilterTargets();
        var both = new Array<any>();
        both = both.concat(ontologies);
        both = both.concat(concepts);
        return both;
    }

    checkboxChanged(checkboxContextData: ConceptGraph.RawAcronym, setOfHideCandidates: Array<ConceptGraph.Node>, checkbox: JQuery);
    checkboxChanged(checkboxContextData: ConceptGraph.Node, setOfHideCandidates: Array<ConceptGraph.Node>, checkbox: JQuery);
    checkboxChanged(checkboxContextData: any, setOfHideCandidates: Array<ConceptGraph.Node>, checkbox: JQuery){
        var result;
        // We need to update the expansion or concept composite checkbox widgets depending on which one was toggled.
        if(Utils.getClassName(checkboxContextData) === "ExpansionSet"){
            result = this.expansionsFilter.checkboxChanged(checkboxContextData, setOfHideCandidates, checkbox);
            this.conceptFilter.updateCheckboxStateFromView(result);
        } else {
            result = this.conceptFilter.checkboxChanged(checkboxContextData, setOfHideCandidates, checkbox);
            this.expansionsFilter.updateCheckboxStateFromView(result);
        }
        
        return result;
    }
    
    /**
     * Synchronize checkboxes with changes made via other checkboxes.
     * Will make the expansion set checkboxes less opaque if any of the individual
     * nodes in the expansion set differ in their state from the most recent toggled
     * state of this checkbox. That is, if all were hidden or shown, then one
     * was shown or hidden, the expansion set checkbox will be changed visually
     * to indicate inconsistent state. 
     */
    updateCheckboxStateFromView(affectedNodes: ConceptGraph.Node[]){
        this.expansionsFilter.updateCheckboxStateFromView(affectedNodes);
        this.conceptFilter.updateCheckboxStateFromView(affectedNodes);
    }
    
    getHoverNeedsAdjacentHighlighting(): boolean{
        return false;
    }
    
    computeExpansionConceptDivId(expansionSet: ExpansionSets.ExpansionSet<ConceptGraph.Node>){
        return "conceptDiv_"+this.expansionsFilter.computeCheckId(expansionSet);
    }
    
    // Override for nesting
    updateFilterUI(){
        // Remove missing ones, whatever is left over in this collection
        var checkboxSpanClass = this.getCheckboxSpanClass();
        var preExistingCheckboxes = $("."+checkboxSpanClass);
        var checkboxesPopulatedOrReUsed = $("");
        var outerThis = this;
        
        // Can I generalize this sorting and node group for when we will have expansion sets? Maybe...
        var expansionFilterTargets = this.expansionsFilter.getFilterTargets();
        var conceptFilterTargets = this.conceptFilter.getFilterTargets();
        
        var expansionSets: Array<ExpansionSets.ExpansionSet<ConceptGraph.Node>>= [];
        
        // Add new expansion set checkboxes.
        // Differs from parent class version at least because there is a +/- expander button preceding the checkbox
        $.each(expansionFilterTargets, (i, target: ExpansionSets.ExpansionSet<ConceptGraph.Node>) =>
            {
                expansionSets.push(target);
                var checkId = this.implementation.computeCheckId(target);
                var spanId = "span_"+checkId;
                if(0 === $("#"+spanId).length){
                    // We store some arbitrary containers of nodes to hide for each checkbox. Seems data consumptive.
                    
                    var checkboxLabel = this.implementation.generateCheckboxLabel(target);
                    var checkboxColoredSquare = this.implementation.generateColoredSquareIndicator(target);
                    
                                        
                    var labelExpanderIcon = $("<label>").addClass(Menu.Menu.menuItemExpanderLabelClass)
                            .addClass("unselectable").attr("unselectable", "on") // IE8
                            .text("+")
                            // Will double-trigger because of the span's click handler
                            // .click(()=>{ expanderClickFunction(); })
                        ;
                    
                    var innerHidingContainer = $("<div>")
                                                .addClass(NestedExpansionSetConceptFilter.NESTED_CONTAINER_CLASS)
                                                .attr("id", this.computeExpansionConceptDivId(target))
                                                .css("display", "none")
                                                ;
                    
                    
                    var expanderClickFunction = (open?: boolean)=>{
                        // Used for the button, as well as for a programmatic callback for when we want to display the submenu
                        // for special purposes.
                        var expanderIndicatorUpdate = ()=>{labelExpanderIcon.text( $(innerHidingContainer).css("display") === "none" ? "+" : "-"); };
                        if(undefined !== open){
                            if(open){
                                $(innerHidingContainer).slideDown('fast', expanderIndicatorUpdate);
                            } else {
                                $(innerHidingContainer).slideUp('fast', expanderIndicatorUpdate);
                            }
                        } else {
                            // Don't have a preference of what to do? Toggle it.
                            $(innerHidingContainer).slideToggle('fast', expanderIndicatorUpdate);
                        }
                    }; 
                    
                    var checkbox = $("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "")
                            .addClass(this.getCheckboxClass())
                            .click(
                                function(event){
                                    // I made the span control the +/- toggle, but clicks were going through the
                                    // checkbox, toggling both the checkbox and triggering my +/- toggle function.
                                    event.stopPropagation();    
                                }
                            )
                            .change(
                                function(event){
                                    var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(target);
                                    outerThis.implementation.checkboxChanged(target, nodeHideCandidates, $(this));
                                }
                            );
                    
                    var spanOfExpanderAndCheckbox = $("<span>").append(labelExpanderIcon).append(checkbox)
                        .click(()=>{ expanderClickFunction(); });    
                    ;
                    
                    labelExpanderIcon.text( $(innerHidingContainer).css("display") === "none" ? "+" : "-"); 
        
                    this.filterContainer.append(
                    $("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox")
                        .mouseenter(
                                outerThis.implementation.checkboxHoveredLambda(target)
                            )
                        .mouseleave(
                                outerThis.implementation.checkboxUnhoveredLambda(target)
                            )
                        .append(
                            spanOfExpanderAndCheckbox
                        )
                        .append(
                            $("<label>").attr("for",checkId)
                            .append(checkboxColoredSquare+"&nbsp;"+checkboxLabel)
                        ).append(
                            innerHidingContainer
                        )
                    );
                }
                checkboxesPopulatedOrReUsed = checkboxesPopulatedOrReUsed.add("#"+spanId);
            }
        );
        
        // Add new concept checkboxes, nested below corresponding expansion set checkbox
        $.each(expansionSets, (i, expSet: ExpansionSets.ExpansionSet<ConceptGraph.Node>) =>{
        
        // Originally I iterated over the concept filter targets, but it was more sensible
        // to collect the expansion sets and iterate over those, knowing that the concepts
        // would reliably connect with the filter.
        // $.each(conceptFilterTargets, (i, target: ConceptGraph.Node) =>
        $.each(expSet.getNodes(), (i, target: ConceptGraph.Node) =>
            {
                var checkId = this.implementation.computeCheckId(target);
                var spanId = "span_"+checkId;
                if(0 === $("#"+spanId).length){
                    // We store some arbitrary containers of nodes to hide for each checkbox. Seems data consumptive.
                    
                    var checkboxLabel = this.implementation.generateCheckboxLabel(target);
                    var checkboxColoredSquare = this.implementation.generateColoredSquareIndicator(target);
                    
                    // TODO Find expansion set on the basis of a single node.
                    // Am I 100% sure that there is never overlap?
                    var correspondingExpansionInnerHidingContainer = $("#"+this.computeExpansionConceptDivId(expSet));
                    
                    correspondingExpansionInnerHidingContainer
                    .append(
                    $("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox")
                        // To offset the nested element from the parent.
                        .css("padding-left", "2em")
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
                        )
                    );
                }
                checkboxesPopulatedOrReUsed = checkboxesPopulatedOrReUsed.add("#"+spanId);
            }
        );
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
        // $("."+this.getCheckboxClass()).prop("checked", "checked").removeClass(AbstractNodeFilterWidget.SOME_SELECTED_CSS);
        this.conceptFilter.checkmarkAllCheckboxes();
        this.expansionsFilter.checkmarkAllCheckboxes();
    }
    
}