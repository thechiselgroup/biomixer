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
    
    static sampleEdgeClassSansDot: string = "filter_link_sample";
    static sampleMarkerClassSansDot: string = "filter_link_sample_marker";
    
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
            function(d: ConceptGraph.Link){
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
                    var checkboxSampleArc = this.generateSampleArcIndicator(linkTypeName, linkTypeToOntology[linkTypeName]);
                    
                    this.filterContainer.append(
                    $("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox")
                        .append(
                            $("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "")
                            .change(
                                function(){
                                    var linkHideCandidates = outerThis.computeCheckboxElementDomain(linkTypeName);
                                    outerThis.checkboxChanged(linkHideCandidates, $(this));
                                }
                            )
                        )
                    .append(checkboxSampleArc)
                        .append(
                            $("<label>").attr("for",checkId)
                            .append(
                                // checkboxColoredSquare+
                                "&nbsp;"+checkboxLabel)
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
    
    // Mostly deprecated, now that we have sample arcs for the legend.
    generateColoredSquareIndicator(linkTypeLabel: string, ontologyAcronym: ConceptGraph.RawAcronym): string {
        if(undefined !== this.conceptGraph.relationTypeCssClasses[linkTypeLabel]){
            return "<span style='font-size: large;' class='"+this.conceptGraph.relationTypeCssClasses[linkTypeLabel]+"'>\u25A0</span>";
        } else {
            // If it isn't a predefined arc type, it is an ontology dependent relation property. Give it the ontology color.
           return "<span style='font-size: large; color: "+this.conceptGraph.nextNodeColor(ontologyAcronym)+";'>\u25A0</span>";
        }
    }
    
    generateSampleArcIndicator(relationType: string, relationSpecificToOntologyAcronym: ConceptGraph.RawAcronym): Element {
        // This is largely code copied from ConceptPathsToRoot...
        // This duplication is fragile, but there's no easy way to convert D3 idioms for
        // bound data to one-off element creation like this.
        var linkContainer = document.createElementNS(d3.ns.prefix.svg, 'svg');

        // Make fake link data to drive the arc positioning functions from the ConceptGraph.
        // We don't need actual nodes as source and target, just coordinates.
        var markerCompensation = relationType === this.conceptGraph.relationLabelConstants.inheritance ? 15 : 0 ;
        var finalTargetXCoordinate = 50; 
        var initialTargetXCoordinate = finalTargetXCoordinate + markerCompensation; // See uses later...
        var arcHeight = 7;
        var linkData = new ConceptGraph.Link();
        linkData.relationType = relationType;
        linkData.source = <ConceptGraph.Node>{x: 0, y: arcHeight};
        linkData.target = <ConceptGraph.Node>{x: initialTargetXCoordinate, y: arcHeight};
        linkData.relationSpecificToOntologyAcronym = relationSpecificToOntologyAcronym;
        
        // Make sample edge container
        d3.select(linkContainer)
        .attr("style", ()=>{
                if(this.graphView.getLinkCssClass(relationType) !== "propertyRelationLink"){
                    return "";
                } else {
                    // Wanted to do this in the getLinkCssClass, by adding a dynamic CSS rule, but
                    // there were a lot of browser issues, and I trusted none of the libraries for it.
                    // So...use a style per edge instead. Too bad.
                    var ontColor = this.conceptGraph.nextNodeColor(relationSpecificToOntologyAcronym);
                    return " stroke: "+ontColor+"; fill: "+ontColor+"; color: "+ontColor+"; ";
                }
            }
        )
        .attr("width", finalTargetXCoordinate).attr("height", 2 * arcHeight)
        .attr("class",
            ()=>{
                return ConceptEdgeTypeFilter.sampleEdgeClassSansDot
                +" "+GraphView.BaseGraphView.linkClassSelectorPrefix+relationType
                +" "+this.graphView.getLinkCssClass(relationType)
                ;
            }
        )
        .attr("id", function(){ return "filter_link_g_"+relationType});
        
        // Make sample marker
        // Make it first because we're going to adjust the line length after,
        // to get the marker centered better.
        d3.select(linkContainer).append("svg:polyline")
        .attr("class",
            ()=>{
                return ConceptEdgeTypeFilter.sampleMarkerClassSansDot
                +" "+GraphView.BaseGraphView.linkClassSelectorPrefix+relationType
                +" "+this.graphView.getLinkCssClass(relationType);
            }
        )
        .attr("id", function(d: ConceptGraph.Link){ return "filter_link_marker_"+relationType})
        .attr("points", this.graphView.updateArcMarkerFunc(linkData, true))
        ;
        
        // Adjust target endpoint to be desired line length; we had it further out
        // to compensate for marker size.
        linkData.target.x = finalTargetXCoordinate;
        
        // Make sample edge line
        d3.select(linkContainer).append("svg:polyline")
        .attr("class",
            ()=>{
                return ConceptEdgeTypeFilter.sampleEdgeClassSansDot
                +" "+GraphView.BaseGraphView.linkClassSelectorPrefix+relationType
                +" "+this.graphView.getLinkCssClass(relationType);
            }
        )
        .attr("id", function(){ return "filter_link_line_"+relationType})
        .attr("points", this.graphView.updateArcLineFunc(linkData, true))
        ;
        
        return linkContainer;
    }
    
    computeCheckId(linkName: string): string {
        return this.getClassName()+"_for_"+linkName;
    }
    
    computeCheckboxElementDomain(linkTypeName: string): D3.Selection {
        // Special class for this sort of selection is constructed this way, with link_ prefix
        return d3.selectAll(
            "."+GraphView.BaseGraphView.linkClassSelectorPrefix+linkTypeName
            +":not(."+ConceptEdgeTypeFilter.sampleEdgeClassSansDot+")"
            +":not(."+ConceptEdgeTypeFilter.sampleMarkerClassSansDot+")"
        );
    }
    
    checkboxChanged(setOfHideCandidates: D3.Selection, checkbox: JQuery){
        if (checkbox.is(':checked')) {
            this.graphView.unhideLinks(setOfHideCandidates);
        } else {
            this.graphView.hideLinks(setOfHideCandidates);
        }
    }
    
}