///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="../Utils" />
///<amd-dependency path="../FilterWidget" />
///<amd-dependency path="../Menu" />
///<amd-dependency path="Concepts/ConceptGraph" />

import FilterWidget = require("../FilterWidget");
import Menu = require("../Menu");
import PathToRoot = require("./ConceptPathsToRoot");
import ConceptGraph = require("./ConceptGraph");
    
export class OntologyConceptFilter extends FilterWidget.FilterWidget {
    
    subMenuTitle = "Ontologies Rendered";
    
    constructor(
        private conceptGraph: ConceptGraph.ConceptGraph,
        private graphView: PathToRoot.ConceptPathsToRoot,
        private centralConceptUri: ConceptGraph.ConceptURI
        ){
        super();
    }
    
    updateFilterUI(){
        // Remove missing ones, whatever is left over in this collection
        var checkboxSpanClass = this.className+"_filterCheckboxSpan";
        var preExistingCheckboxes = $("."+checkboxSpanClass);
        var newCheckboxes = [];
        var outerThis = this;
        
        var sortedNodes = this.graphView.sortConceptNodesCentralOntologyName();
        
        var usedOntologies = [];
        
        // Add new ones
        $.each(sortedNodes, (i, node: ConceptGraph.Node) =>
            {
                if(-1 !== $.inArray(String(node.ontologyAcronym), usedOntologies)){
                    return;
                }
                usedOntologies.push(String(node.ontologyAcronym));
                
                var checkId = this.className+"_for_"+String(node.ontologyAcronym);
                var spanId = "span_"+checkId;
                var checkboxLabel = node.ontologyAcronym;
                var checkboxColoredSquare = "<span style='color: "+node.nodeColor+"'>\u25A0</span>";
                if($(spanId).empty()){
                    this.filterContainer.append(
                    $("<span>").attr("id", spanId).addClass(checkboxSpanClass)
                    // TODO Do we want multi-node hover for ontology checkboxes? Maybe?
//                        .mouseenter(outerThis.checkboxHoveredLambda(node))
//                        .mouseleave(outerThis.checkboxUnhoveredLambda(node))
                        .append("<br>")
                        .append(
                            $("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "")
                            .change(
                                function(){
                                    var checkBox = $(this);
                                    if (checkBox.is(':checked')) {
                                        // Unhide those that are checked, as well as edges with both endpoints visible
                                        $.each(sortedNodes,
                                            function(i, node: ConceptGraph.Node){ outerThis.graphView.unhideNodeLambda(outerThis.graphView)(node, 0); }
                                        );
                                    } else {
                                        // Hide those that are unchecked, as well as edges with no endpoints visible
                                        $.each(sortedNodes,
                                            function(i, node: ConceptGraph.Node){ outerThis.graphView.hideNodeLambda(outerThis.graphView)(node, 0); }
                                        );
                                    }
                                }
                            )
                        )
                        .append(
                            $("<label>").attr("for",checkId)
                            .append(checkboxColoredSquare+"&nbsp;"+checkboxLabel)
                            // The ontology checkbox corresponding to the central concept's ontology will be styled.
                            .toggleClass("centralNode", node.rawConceptUri === outerThis.conceptGraph.centralConceptUri)
                        )
                    );
                }
                newCheckboxes.push($(spanId));
            }
        );
        
        // Keep only those checkboxes for which we looped over a node
        preExistingCheckboxes.not(newCheckboxes).remove();
    }
    
    checkboxHoveredLambda(node: ConceptGraph.Node){
        var graphView: PathToRoot.ConceptPathsToRoot = this.graphView;
        return function(eventObject: JQueryMouseEventObject){
            // Technically, the span over the checkbox is the element
            // Find the graph node that corresponds, and fire its mouse enter behavior.
            graphView.highlightHoveredNodeLambda(graphView)(node, 0);
        }
    }
    
    checkboxUnhoveredLambda(node: ConceptGraph.Node){
        var graphView: PathToRoot.ConceptPathsToRoot = this.graphView;
        return function(eventObject: JQueryMouseEventObject){
            // Technically, the span over the checkbox is the element
            // Find the graph node that corresponds, and fire its mouse leave behavior.
            graphView.unhighlightHoveredNodeLambda(graphView)(node, 0);
        };
    }
    
}