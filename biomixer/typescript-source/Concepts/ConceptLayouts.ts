///<reference path="headers/require.d.ts" />
///<reference path="headers/d3.d.ts" />

///<amd-dependency path="../JQueryExtension" />

///<amd-dependency path="GraphView" />
///<amd-dependency path="Concepts/ConceptGraph" />


import GraphView = require('../GraphView');
import ConceptGraphView = require('./ConceptPathsToRoot');
import ConceptGraph = require('./ConceptGraph');

export class ConceptLayouts {

    constructor(
        public forceLayout: D3.Layout.ForceLayout,
        public graph: ConceptGraph.ConceptGraph,
        public graphView: ConceptGraphView.ConceptPathsToRoot,
        public centralConceptUri: ConceptGraph.ConceptURI
    ){
        
    }
    
    
    addMenuComponents(menuSelector: string, softNodeCap: number){
        // Add the butttons to the pop-out panel
        $(menuSelector).append($("<input>")
                .attr("class", "layoutButton")
                .attr("id", "forceLayoutButton")
                .attr("type", "button")
                .attr("value", "Force-Directed Layout"));
        $(menuSelector).append($("<br>"));
        
        $(menuSelector).append($("<input>")
                .attr("class", "layoutButton")
                .attr("id", "circleLayoutButton")
                .attr("type", "button")
                .attr("value", "Circle Layout"));
        $(menuSelector).append($("<br>"));
        
        $(menuSelector).append($("<input>")
                .attr("class", "layoutButton")
                .attr("id", "centerLayoutButton")
                .attr("type", "button")
                .attr("value", "Center Layout"));
    
        
        d3.selectAll("#circleLayoutButton").on("click", this.runCircleLayoutLambda());
        d3.selectAll("#forceLayoutButton").on("click", this.runForceLayoutLambda());
        d3.selectAll("#centerLayoutButton").on("click", this.runCenterLayoutLambda());
    
    }
    
    runCircleLayoutLambda(){
        var outerThis = this;
        return function(){
            outerThis.forceLayout.stop();
            var graphNodes = outerThis.graph.graphD3Format.nodes;
            var graphLinks = outerThis.graph.graphD3Format.links;
                
            var numberOfConcepts = Object.keys(graphNodes).length;
    
            var anglePerNode =2*Math.PI / numberOfConcepts; // 360/numberOfMappedOntologies;
            var arcLength = outerThis.graphView.linkMaxDesiredLength();
            var i = 0;
            
            $.each(graphNodes,
                function(index, element){
                    var acronym = index;
    
                    if(typeof acronym === "undefined"){
                        console.log("Undefined concept entry");
                    }
                    
                    var angleForNode = i * anglePerNode; 
                    i++;
                    graphNodes[index].x = outerThis.graphView.visWidth()/2 + arcLength*Math.cos(angleForNode); // start in middle and let them fly outward
                    graphNodes[index].y = outerThis.graphView.visHeight()/2 + arcLength*Math.sin(angleForNode); // start in middle and let them fly outward
                }
            );
            
            d3.selectAll("g.node")
                .transition()
                .duration(2500)
                .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
            
            d3.selectAll("line")
                .transition()
                .duration(2500)
                .attr("x1", function(d){return d.source.x;})
                .attr("y1", function(d){return d.source.y;})
                .attr("x2", function(d){return d.target.x;})
                .attr("y2", function(d){return d.target.y;});
    
        };
    }
    
    runCenterLayoutLambda(){
        var outerThis = this;
        return function(){
            outerThis.forceLayout.stop();
            var graphNodes = outerThis.graph.graphD3Format.nodes;
            var graphLinks = outerThis.graph.graphD3Format.links;
                
            var numberOfConcepts = Object.keys(graphNodes).length-1;
    
            var anglePerNode =2*Math.PI / numberOfConcepts; // 360/numberOfMappedOntologies;
            var arcLength = outerThis.graphView.linkMaxDesiredLength();
            var i = 0;
            
            $.each(graphNodes,
                function(acronym, node){
                    if(typeof acronym === "undefined"){
                        console.log("Undefined concept entry");
                    }
                    
                    if(node.conceptId!=outerThis.centralConceptUri){
                        var angleForNode = i * anglePerNode; 
                        i++;
                        node.x = outerThis.graphView.visWidth()/2 + arcLength*Math.cos(angleForNode); // start in middle and let them fly outward
                        node.y = outerThis.graphView.visHeight()/2 + arcLength*Math.sin(angleForNode); // start in middle and let them fly outward
                    }else{
                        node.x = outerThis.graphView.visWidth()/2; 
                        node.y = outerThis.graphView.visHeight()/2;
                        //alert(node.id+centralConceptUri);
                        
                    }
                }
            );
            
            d3.selectAll("g.node")
                .transition()
                .duration(2500)
                .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
            
            d3.selectAll("line")
                .transition()
                .duration(2500)
                .attr("x1", function(d){return d.source.x;})
                .attr("y1", function(d){return d.source.y;})
                .attr("x2", function(d){return d.target.x;})
                .attr("y2", function(d){return d.target.y;});
    
        };
    }
    
    
    runForceLayoutLambda(){
        var outerThis = this;
        return function(){
            outerThis.forceLayout.on("tick", outerThis.graphView.onLayoutTick());
            outerThis.forceLayout.start();
    
        };
    }
    
}