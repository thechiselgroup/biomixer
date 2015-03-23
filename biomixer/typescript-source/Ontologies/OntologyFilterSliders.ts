///<amd-dependency path="Utils" />
///<amd-dependency path="GraphView" />
///<amd-dependency path="Ontologies/OntologyMappingOverview" />
///<amd-dependency path="Ontologies/OntologyGraph" />

import Utils = require("../Utils");
import GraphView = require("../GraphView");
import OntMap = require("./OntologyMappingOverview");
import OntologyGraph = require("./OntologyGraph");
    
export class MappingRangeSliders {
    
    //Keep track of node mapping values in order, so we can filter through them in ranks
    // This container is separate from the array of acronyms sorted by mapping counts...shall
    // they be combined?
    private sortedLinksByMapping: OntologyGraph.Link[] = [];
    
    constructor(
        private graph: OntologyGraph.OntologyGraph,
        private graphView: OntMap.OntologyMappingOverview,
        private centralOntologyAcronym: OntologyGraph.RawAcronym
        ){
    }

    addMenuComponents(menuSelector: string, softNodeCap: number){
        var minSliderAbsolute = 0;
        var maxSliderAbsolute = 0 == softNodeCap ? this.sortedLinksByMapping.length : softNodeCap; 
        
        $(menuSelector).append($("<label>").attr("for", "top-mappings-slider-amount").text("Ranked Mapping Range: "));
        $(menuSelector).append($("<label>").attr("type", "text").attr("id", "top-mappings-slider-amount")) // .css("border:0; color:#f6931f; font-weight:bold;"));
        $(menuSelector).append($("<div>").attr("id",  "top-mappings-slider-range" ));
        
        $( "#top-mappings-slider-range" ).slider({
            range: true,
            min: minSliderAbsolute,
            max: maxSliderAbsolute,
            values: [ minSliderAbsolute, maxSliderAbsolute ],
            slide: this.rangeSliderSlideEvent,
            change: this.rangeSliderSlideEvent
            }
        );
         
        this.updateTopMappingsSliderRange();
        
        // Need separate initialization for input text
        $( "#top-mappings-slider-amount" ).text( "Top "+ minSliderAbsolute + " - " + maxSliderAbsolute );
        
    }
    
    // Callback needs fat arrow or use of Utils.HasCallback.
    // Can use fat arrow instead of lambda closure because we don't need
    // the caller's" this".
    rangeSliderSlideEvent = (event, ui)=>{
        // Need to make it wider than 100% due to UI bugginess
        var bottom = $( "#top-mappings-slider-range" ).slider( "values", 0 ) + 1;
        var top = $( "#top-mappings-slider-range" ).slider( "values", 1 ) + 1;
        $( "#top-mappings-slider-amount" ).text( "Top "+ bottom + " - " + top );
        this.filterGraphOnMappingCounts();
    }
    
    changeTopMappingSliderValues(bottom: number, top: number){
        console.log("Programatically changing node filter cutoff at "+Utils.getTime());
        if(null == bottom){
            bottom = $( "#top-mappings-slider-range" ).slider('values', 0);
        } else if(bottom > 0){
            bottom = bottom - 1;
        }
        if(null == top){
            top = $( "#top-mappings-slider-range" ).slider('values', 1);
        } else if(top > 0){
            top = top - 1;
        }
        // The change event is triggered when values are changed. Map change event to appropriate function.
        $( "#top-mappings-slider-range" ).slider('values', [bottom, top]);
    }
    
    updateTopMappingsSliderRange(){
        this.sortedLinksByMapping = [];
        // Fill the sorted set every time in caase we are updating.
        // This shouldn't get called too often.
        var i = 0;
        var outerThis = this;
        d3.selectAll(GraphView.BaseGraphView.linkSvgClass).each( 
                function(d,i){
                    outerThis.sortedLinksByMapping[i] = d;
                }
        );
        
        // Descending sort so we can pick the top n.
        this.sortedLinksByMapping.sort(function(a,b){return b.value-a.value});
        
        var mappingMin = 1;
        var mappingMax = this.sortedLinksByMapping.length;
        
        $( "#top-mappings-slider-range" ).slider("option", "min", 0);
        $( "#top-mappings-slider-range" ).slider("option", "max", this.sortedLinksByMapping.length - 1);
    //  $( "#top-mappings-slider-range" ).slider("option", "values", [0, sortedLinksByMapping.length - 1]);
        $( "#top-mappings-slider-amount" ).text( "Top "+ mappingMin + " - " + mappingMax );
    }
    
    /**
     * Old filtering code. Based off of deleted sliders. Filtered on node size and/or arc size.
     * May be useful later, perhaps. Be sure to check that it still works with changes made since it was deprecated.
     */
    filterGraphDeprecated(){
        alert("Deprecated function called");
    //  var minNodeAbsolute = minNodePercentile * (nodeMax - nodeMin) + nodeMin;
    //  var maxNodeAbsolute = maxNodePercentile * (nodeMax - nodeMin) + nodeMin;
    //  var minArcAbsolute = minArcPercentile * (arcMax - arcMin) + arcMin;
    //  var maxArcAbsolute = maxArcPercentile * (arcMax - arcMin) + arcMin;
    //  
    //  // Iterate through all arcs, remove if their node or arc fails to pass
    //  // We don't need to iterate through all the nodes, because we will do so here.
    //  // That is, we know that our ontologies do not have detached nodes, so going over all arcs gets us all nodes.
    //  d3.selectAll("line").each( 
    //          function(d,i){
    //              console.log(parseInt(d.value) , minArcAbsolute , maxArcAbsolute);
    //              var hideArc = (parseInt(d.value) < minArcAbsolute || parseInt(d.value) > maxArcAbsolute);
    //              var hideSourceNode = (parseInt(d.source.number) < minNodeAbsolute || parseInt(d.source.number) > maxNodeAbsolute);
    //              var hideTargetNode = (parseInt(d.target.number) < minNodeAbsolute || parseInt(d.target.number) > maxNodeAbsolute);
    //              
    //              if(d.source.rawAcronym == centralOntologyAcronym){
    //                  hideSourceNode = false;
    //              }
    //              if(d.target.rawAcronym == centralOntologyAcronym){
    //                  hideTargetNode = false;
    //              }
    //              
    //              $(this).css("display", (hideArc || hideSourceNode || hideTargetNode) ? "none" : "");
    //              
    //              $("#node_circle_"+d.source.acronymForIds).css("display", (hideArc || hideSourceNode) ? "none" : "");
    //              $("#node_circle_"+d.target.acronymForIds).css("display", (hideArc || hideTargetNode) ? "none" : "");
    //              
    //              // TODO If we want this to be generic and refactorable, we should iterate over the parent of the circles...
    //              // These inner circles only really apply to the ontology nodes
    //              $("#node_circle_inner_"+d.source.acronymForIds).css("display", (hideArc || hideSourceNode) ? "none" : "");
    //              $("#node_circle_inner_"+d.target.acronymForIds).css("display", (hideArc || hideTargetNode) ? "none" : "");
    //                              
    //              $("#node_text_"+d.source.acronymForIds).css("display", (hideArc || hideSourceNode) ? "none" : "");
    //              $("#node_text_"+d.target.acronymForIds).css("display", (hideArc || hideTargetNode) ? "none" : "");
    //                              
    //              // The nodes have API calls they might need to make. This might change a little when expansion commands
    //              // are added to the system.
    //              if(!(hideArc || hideSourceNode)){
    //                  fetchMetricsAndDescriptionFunc(d.source);
    //              }
    //              if(!(hideArc || hideTargetNode)){
    //                  fetchMetricsAndDescriptionFunc(d.target);
    //              }
    //          }
    //      );
        
    }
        
    filterGraphOnMappingCounts(){
        // Grabbing min from 1 and max from 0 looks funny, but it does the trick. Pinky swear.
        var minNode = this.sortedLinksByMapping[$( "#top-mappings-slider-range" ).slider( "values", 1 )];
        var maxNode = this.sortedLinksByMapping[$( "#top-mappings-slider-range" ).slider( "values", 0 )];
        if(undefined === minNode || undefined === maxNode){
            return;
        }
        var minNodeAbsolute = minNode.value; // starts at big number
        var maxNodeAbsolute = maxNode.value; // starts at 0
        var minArcAbsolute = minNodeAbsolute;
        var maxArcAbsolute = maxNodeAbsolute;
        
        var topIndex = $( "#top-mappings-slider-range" ).slider( "values", 1 );
        var bottomIndex = $( "#top-mappings-slider-range" ).slider( "values", 0 );
        
        var outerThis = this;
        // Iterate through all arcs, remove if their node or arc fails to pass
        // We don't need to iterate through all the nodes, because we will do so here.
        // That is, we know that our ontologies do not have detached nodes, so going over all arcs gets us all nodes.
        $.each(this.sortedLinksByMapping,
                function(i, d){
                    // Work with arc first, then the attached nodes.
                    // var hideArc = (parseInt(d.value) < minArcAbsolute || parseInt(d.value) > maxArcAbsolute);
                    // var hideSourceNode = (parseInt(d.source.mapped_classes_to_central_node) < minNodeAbsolute || parseInt(d.source.mapped_classes_to_central_node) > maxNodeAbsolute);
                    // var hideTargetNode = (parseInt(d.target.mapped_classes_to_central_node) < minNodeAbsolute || parseInt(d.target.mapped_classes_to_central_node) > maxNodeAbsolute);
        
                    // Easier to iterate over index on sorted set than to inspect for values liek above. 
                    var hideArc = !(bottomIndex <= i && i <= topIndex);
    
                    // If we default to hiding all nodes, and that if we only set a node to visible if there is
                    // an arc connected to it that is set to visible, we only keep nodes with visible arcs present.
                    var hideSourceNodeBecauseOfHiddenArc = hideArc;
                    var hideTargetNodeBecauseOfHiddenArc = hideArc;
                    
                    // Always show central node
                    if(d.source.rawAcronym == outerThis.centralOntologyAcronym){
                        hideSourceNodeBecauseOfHiddenArc = false;
                    }
                    if(d.target.rawAcronym == outerThis.centralOntologyAcronym){
                        hideTargetNodeBecauseOfHiddenArc = false;
                    }
                    
                    if(!hideArc){
                        // console.log("Not hide arc: "+d.source.acronymForIds+"-to-"+d.target.acronymForIds);
                    }
                    
                    // $(this).css("display", (hideArc) ? "none" : "");
                    $("#link_line_"+d.source.acronymForIds+"-to-"+d.target.acronymForIds).css("display", (hideArc) ? "none" : "");
                    
                    $("#node_g_"+d.source.acronymForIds).find("*").css("display", (hideSourceNodeBecauseOfHiddenArc) ? "none" : "");
                    $("#node_g_"+d.target.acronymForIds).find("*").css("display", (hideTargetNodeBecauseOfHiddenArc) ? "none" : "");
    //              $("#node_g_"+d.source.acronymForIds).find(".nodetext").attr("x", function(){ return - outerThis.getComputedTextLength()/2; });
    //              $("#node_g_"+d.target.acronymForIds).find(".nodetext").attr("x", function(){ return - outerThis.getComputedTextLength()/2; });
    
                    // This should get all fo these:
                    // $("#node_circle_"+d.source.acronymForIds)
                    // $("#node_circle_inner_"+d.source.acronymForIds)
                    // $("#node_text_"+d.source.acronymForIds)
                    
                    // The nodes have API calls they might need to make. This might change a little when expansion commands
                    // are added to the system.
                    if(!hideSourceNodeBecauseOfHiddenArc){
                        // Refactor to add a method such as "NewNodeDisplayed" on the graphView instead of the Graph
                        // graph.fetchMetricsAndDescriptionFunc(d.source);
                        outerThis.graph.fetchNodeRestData(d.source);
                        
                    } 
    
                    if(!hideTargetNodeBecauseOfHiddenArc){
                        // graph.fetchMetricsAndDescriptionFunc(d.target);
                        outerThis.graph.fetchNodeRestData(d.target);
                    }
                }
            );
        
        // Firefox renders dx for text poorly, shifting things around oddly,
        // but x works for both Chrome and Firefox.
        $(GraphView.BaseGraphView.nodeLabelSvgClass).attr("x", function(){ return - this.getComputedTextLength()/2; });
        
        this.graphView.stampTimeGraphModified();
        
        this.graphView.runCurrentLayout();
    }
    
}
    
