///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="../Utils" />
///<amd-dependency path="../FilterWidget" />
///<amd-dependency path="../GraphView" />
///<amd-dependency path="Concepts/ConceptPathsToRoot" />
///<amd-dependency path="Concepts/ConceptGraph" />
///<amd-dependency path="Concepts/PropertyRelationsExpander" />

import FilterWidget = require("../FilterWidget");
import GraphView = require("../GraphView");
import PathToRoot = require("./ConceptPathsToRoot");
import ConceptGraph = require("./ConceptGraph");
import PropRel = require("./PropertyRelationsExpander");

/**
 * Vaguely resembles the sibling node filtering classes, with similarly named method names, but the
 * requirements are different enough that it doesn't share specialized behaviors with them.
 */
export class ConceptEdgeTypeFilter extends FilterWidget.AbstractFilterWidget implements FilterWidget.IFilterWidget {
    
    static SUB_MENU_TITLE: string = "Edge Types Displayed";
    
    constructor(
        public conceptGraph: ConceptGraph.ConceptGraph,
        public graphView: PathToRoot.ConceptPathsToRoot,
        public centralConceptUri: ConceptGraph.ConceptURI
        ){
        super(ConceptEdgeTypeFilter.SUB_MENU_TITLE);
    }
    
    updateFilterUI(){
        // Remove missing ones, whatever is left over in this collection
        var checkboxSpanClass = this.getCheckboxSpanClass();
        var preExistingCheckboxes = $("."+checkboxSpanClass);
        var checkboxesPopulatedOrReUsed = $("");
        var outerThis = this;
        
        // We can grab all the arcs, then check them for the relation id, and populate that way rather than using the explicit hard coded
        // types. This is the way it works for ontologies.
        var linkTypes: {[type: string]: string} = {};
        var linkTypeToOntology: {[type: string]: ConceptGraph.RawAcronym} = {};
        d3.selectAll("."+GraphView.BaseGraphView.linkSvgClassSansDot).each(
            (d: ConceptGraph.Link)=>{
                linkTypes[d.relationType] = d.relationLabel;
                linkTypeToOntology[d.relationType] = d.relationSpecificToOntologyAcronym;
            }
        );

        
        // Add new ones
        $.each(linkTypes, (linkTypeName: string, linkTypeLabel: string) =>
            {
                var checkId = this.computeCheckId(linkTypeName);
                var spanId = "span_"+checkId;
                if(0 === $("#"+spanId).length){
                    // We store some arbitrary containers of nodes to hide for each checkbox. Seems data consumptive.
                    
                    var checkboxLabel = this.generateCheckboxLabel(linkTypeLabel);
                    // TODO Use existing CSS to assign same color to this square
                    var checkboxColoredSquare = this.generateColoredSquareIndicator(linkTypeLabel, linkTypeToOntology[linkTypeName]);
                    
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
        
        // Put our favorite relations in order at top. We do this in reverse order of preference though.
        $("#"+"span_"+this.computeCheckId(this.conceptGraph.relationLabelConstants.mapping)).prependTo(this.filterContainer);
        $("#"+"span_"+this.computeCheckId(this.conceptGraph.relationLabelConstants.composition)).prependTo(this.filterContainer);
        $("#"+"span_"+this.computeCheckId(this.conceptGraph.relationLabelConstants.inheritance)).prependTo(this.filterContainer);
        
        // Keep only those checkboxes for which we looped over a node
        preExistingCheckboxes.not(checkboxesPopulatedOrReUsed).remove();
    }
    
    generateCheckboxLabel(linkTypeLabel: string): string {
        return "\""+linkTypeLabel+"\"";
    }
    
    generateColoredSquareIndicator(linkTypeLabel: string, ontologyAcronym: ConceptGraph.RawAcronym): string {
        if(undefined !== this.conceptGraph.relationTypeCssClasses[linkTypeLabel]){
            return "<span style='font-size: large;' class='"+this.conceptGraph.relationTypeCssClasses[linkTypeLabel]+"'>\u25A0</span>";
        } else {
            // If it isn't a predefined arc type, it is an ontology dependent relation property. Give it the ontology color.
           return "<span style='font-size: large; color: "+this.conceptGraph.nextNodeColor(ontologyAcronym)+";'>\u25A0</span>";
        }
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