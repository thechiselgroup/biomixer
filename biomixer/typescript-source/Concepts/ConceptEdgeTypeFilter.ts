///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="../Utils" />
///<amd-dependency path="../FilterWidget" />
///<amd-dependency path="../Menu" />
///<amd-dependency path="Concepts/ConceptGraph" />

import FilterWidget = require("../FilterWidget");
import Menu = require("../Menu");
import Utils = require("../Utils");
import PathToRoot = require("./ConceptPathsToRoot");
import ConceptGraph = require("./ConceptGraph");

/**
 * Vaguely resembles the sibling node filtering classes, with similarly named method names, but the
 * requirements are different enough that it doesn't share specialized behaviors with them.
 */
export class ConceptEdgeTypeFilter extends FilterWidget.AbstractFilterWidget implements FilterWidget.IFilterWidget {
    
    static SUB_MENU_TITLE: string = "Edge Types Rendered";
    
    constructor(
        public conceptGraph: ConceptGraph.ConceptGraph,
        public graphView: PathToRoot.ConceptPathsToRoot,
        public centralConceptUri: ConceptGraph.ConceptURI
        ){
        super(ConceptEdgeTypeFilter.SUB_MENU_TITLE);
    }
    
    updateFilterUI(){
        // Remove missing ones, whatever is left over in this collection
        var checkboxSpanClass = this.getClassName()+"_filterCheckboxSpan";
        var preExistingCheckboxes = $("."+checkboxSpanClass);
        var checkboxesPopulatedOrReUsed = $("");
        var outerThis = this;
        
        var linkTypes = this.conceptGraph.relationLabelConstants;

        // Add new ones
        $.each(linkTypes, (linkTypeName: string, linkTypeLabel: string) =>
            {
                var checkId = this.computeCheckId(linkTypeName);
                var spanId = "span_"+checkId;
                if(0 === $("#"+spanId).length){
                    // We store some arbitrary containers of nodes to hide for each checkbox. Seems data consumptive.
                    
                    var checkboxLabel = this.generateCheckboxLabel(linkTypeLabel);
                    // TODO Use existing CSS to assign same color to this square
                    var checkboxColoredSquare = this.generateColoredSquareIndicator(linkTypeLabel);
                    
                    this.filterContainer.append(
                    $("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox")
                        .append(
                            $("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "")
                            .change(
                                function(){
                                    var linkHideCandidates = outerThis.computeCheckboxElementDomain(linkTypeLabel);
                                    outerThis.checkboxChanged(linkHideCandidates, $(this));
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
        
        // Keep only those checkboxes for which we looped over a node
        preExistingCheckboxes.not(checkboxesPopulatedOrReUsed).remove();
    }
    
    generateCheckboxLabel(linkTypeLabel: string): string {
        return "\""+linkTypeLabel+"\"";
    }
    
    generateColoredSquareIndicator(linkTypeLabel: string): string {
        return "<span style='font-size: large;' class='"+this.conceptGraph.relationTypeCssClasses[linkTypeLabel]+"'>\u25A0</span>";
    }
    
    computeCheckId(linkName: string): string {
        return this.getClassName()+"_for_"+linkName;
    }
    
    computeCheckboxElementDomain(linkTypeLabel: string): D3.Selection {
        return d3.selectAll("."+this.conceptGraph.relationTypeCssClasses[linkTypeLabel]);
    }
    
    checkboxChanged(setOfHideCandidates: D3.Selection, checkbox: JQuery){
        if (checkbox.is(':checked')) {
            this.graphView.unhideLinks(setOfHideCandidates);
        } else {
            this.graphView.hideLinks(setOfHideCandidates);
        }
    }
    
}