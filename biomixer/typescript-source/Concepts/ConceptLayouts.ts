///<reference path="headers/require.d.ts" />
///<reference path="headers/d3.d.ts" />

///<amd-dependency path="../JQueryExtension" />

///<amd-dependency path="GraphView" />
///<amd-dependency path="Menu" />
///<amd-dependency path="Concepts/ConceptPathsToRoot" />
///<amd-dependency path="Concepts/ConceptGraph" />


import GraphView = require("../GraphView");
import Menu = require("../Menu");
import ConceptGraphView = require("./ConceptPathsToRoot");
import ConceptGraph = require("./ConceptGraph");

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
        var layoutsContainer = $("<div>").attr("id", "layoutMenuContainer");
        $(menuSelector).append(layoutsContainer);
                
        layoutsContainer.append($("<label>").addClass(Menu.Menu.menuLabelClass).text("Layouts"));
        layoutsContainer.append($("<br>"));
        
        layoutsContainer.append($("<input>")
                .attr("class", "layoutButton")
                .attr("id", "forceLayoutButton")
                .attr("type", "button")
                .attr("value", "Force-Directed Layout"));
        layoutsContainer.append($("<br>"));
        
        layoutsContainer.append($("<input>")
                .attr("class", "layoutButton")
                .attr("id", "circleLayoutButton")
                .attr("type", "button")
                .attr("value", "Circle Layout"));
        layoutsContainer.append($("<br>"));
        
        layoutsContainer.append($("<input>")
                .attr("class", "layoutButton")
                .attr("id", "centerLayoutButton")
                .attr("type", "button")
                .attr("value", "Center Layout"));
        layoutsContainer.append($("<br>"));
        
        layoutsContainer.append($("<input>")
            .attr("class", "layoutButton")
            .attr("id", "horizontalTreeLayoutButton")
            .attr("type", "button")
            .attr("value", "Horizontal Tree Layout"));
        layoutsContainer.append($("<br>"));
    
        layoutsContainer.append($("<input>")
            .attr("class", "layoutButton")
            .attr("id", "verticalTreeLayoutButton")
            .attr("type", "button")
            .attr("value", "Vertical Tree Layout"));
        layoutsContainer.append($("<br>"));
    
       layoutsContainer.append($("<input>")
            .attr("class", "layoutButton")
            .attr("id", "radialLayoutButton")
            .attr("type", "button")
            .attr("value", "Radial Layout"));
    
        
        d3.selectAll("#circleLayoutButton").on("click", this.runCircleLayoutLambda());
        d3.selectAll("#forceLayoutButton").on("click", this.runForceLayoutLambda());
        d3.selectAll("#centerLayoutButton").on("click", this.runCenterLayoutLambda());
        d3.selectAll("#horizontalTreeLayoutButton").on("click", this.runHorizontalTreeLayoutLambda());
        d3.selectAll("#verticalTreeLayoutButton").on("click", this.runVerticalTreeLayoutLambda());
        d3.selectAll("#radialLayoutButton").on("click", this.runRadialLayoutLambda());
    
    }
    
    rootIndex(){
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
        var graphLinks = outerThis.graph.graphD3Format.links;
        
        var index = 0;
        var rootId = null;
        var rootFound=false;
        // not the best algorithm. Need to look into improving it
        graphLinks.forEach(function(a){
            if(rootFound==false){
                rootFound=true;
                graphLinks.forEach(function(b){
                    if(a.sourceId==b.targetId){
                        //rootId = b.sourceId;
                        rootFound = false;
                    }
                    
                });
                
                if(rootFound==true){
                    rootId = a.sourceId;
                }
            }
            
        });
        
        graphNodes.forEach(function(n){
            var i = graphNodes.indexOf(n);
           // console.log("index "+i);
    
            if(n.rawConceptUri==rootId){
                index = i;
               // console.log("index "+i);
    
            }
        });
        
        return index;   
    }
    
    runRadialLayoutLambda(){
        var outerThis = this;
        return function(refreshLayout?: boolean){
        	if(refreshLayout){
    			// Act normal, redo the whole layout
    		}
    		
            outerThis.forceLayout.stop();
            var graphNodes = outerThis.graph.graphD3Format.nodes;
            var graphLinks = outerThis.graph.graphD3Format.links;
            
            var tree = d3.layout.tree()
                .size([360,outerThis.graphView.visHeight()/2-100])
                .children(function(d){  
                    var arrayOfNodes = []; 
                    graphLinks.forEach(function(b){
                        if(b.sourceId==d.rawConceptUri){
                            var targetNode= {};
                            graphNodes.forEach(function(c){
                                if(c.rawConceptUri==b.targetId){
                                    targetNode = c;
                                }
                                
                            });
                            arrayOfNodes.push(targetNode);
                        }
                        
                    });
                   
                    return arrayOfNodes;
                });
            
              var treeNodes = tree.nodes(graphNodes[outerThis.rootIndex()]);
          
              
              $.each(graphNodes,
                    function(index, element){
                        var radius = element.y;
                        var angle = element.x/180 * Math.PI;
                        graphNodes[index].x = outerThis.graphView.visWidth()/2 + radius*Math.cos(angle); 
    //                  graphNodes[index].x = 0; 
    //                  graphNodes[index].y = element.y; 
    
                        graphNodes[index].y = outerThis.graphView.visHeight()/2 + radius*Math.sin(angle); 
                    }
                );
              // Adding 150 to y values is probably not the best way of dealing with this
                 d3.selectAll("g.node_g")

                    .transition()
                    .duration(2500)
                    .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
                
                d3.selectAll(GraphView.BaseGraphView.linkSvgClass)
                    .transition()
                    .duration(2500)
                    .attr("points", outerThis.graphView.computePolyLineLinkPointsFunc);
              
        };
    }

    runVerticalTreeLayoutLambda(){
        var outerThis = this;
        return function(refreshLayout?: boolean){
        	if(refreshLayout){
    			// Act normal, redo the whole layout
    		}
    		
            outerThis.forceLayout.stop();
            var graphNodes = outerThis.graph.graphD3Format.nodes;
            var graphLinks = outerThis.graph.graphD3Format.links;
            
            var tree = d3.layout.tree()
                .size([outerThis.graphView.visWidth(), outerThis.graphView.visHeight()-300])
                .children(function(d){  
                    var arrayOfNodes = []; 
                    graphLinks.forEach(function(b){
                        if(b.sourceId==d.rawConceptUri){
                            var targetNode= {};
                            graphNodes.forEach(function(c){
                                if(c.rawConceptUri==b.targetId){
                                    targetNode = c;
                                }
                                
                            });
                            arrayOfNodes.push(targetNode);
                        }
                        
                    });
                    return arrayOfNodes;
                });
            
              var treeNodes = tree.nodes(graphNodes[outerThis.rootIndex()]);
          
              
              $.each(graphNodes,
                    function(index, element){
                        graphNodes[index].x = element.x; 
                        graphNodes[index].y = element.y+150; 
                    }
                );
              // Adding 150 to y values is probably not the best way of dealing with this
                 d3.selectAll("g.node_g")
                    .transition()
                    .duration(2500)
                    .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
                
                d3.selectAll(GraphView.BaseGraphView.linkSvgClass)
                    .transition()
                    .duration(2500)
                    .attr("points", outerThis.graphView.computePolyLineLinkPointsFunc);
              
        };
    }
    
    runHorizontalTreeLayoutLambda(){
        var outerThis = this;
        return function(refreshLayout?: boolean){
        	if(refreshLayout){
    			// Act normal, redo the whole layout
    		}
    		
            outerThis.forceLayout.stop();
            var graphNodes = outerThis.graph.graphD3Format.nodes;
            var graphLinks = outerThis.graph.graphD3Format.links;
            
            var tree = d3.layout.tree()
                .size([outerThis.graphView.visHeight()-100,outerThis.graphView.visWidth()-300])
                .children(function(d){  
                    var arrayOfNodes = []; 
                    graphLinks.forEach(function(b){
                        if(b.sourceId==d.rawConceptUri){
                            var targetNode= {};
                            graphNodes.forEach(function(c){
                               if(c.rawConceptUri==b.targetId){
                                    targetNode = c;
                                }
                            });
                            arrayOfNodes.push(targetNode);
                        }
                        
                    });
                     
                    return arrayOfNodes;
                });
            
                var treeNodes = tree.nodes(graphNodes[outerThis.rootIndex()]);
              
                  
                $.each(graphNodes,
                      function(index, element){
                          var xValue = element.x
                          graphNodes[index].x = element.y+150; 
                          graphNodes[index].y = xValue; 
                      }
                );

                 d3.selectAll("g.node_g")
                    .transition()
                    .duration(2500)
                    .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
                
                d3.selectAll(GraphView.BaseGraphView.linkSvgClass)
                    .transition()
                    .duration(2500)
                    .attr("points", outerThis.graphView.computePolyLineLinkPointsFunc);
              
        };
      
    }
    
    runCircleLayoutLambda(){
        var outerThis = this;
        return function(refreshLayout?: boolean){
        	if(refreshLayout){
    			// Act normal, redo the whole layout
    		}
    		
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
            
            d3.selectAll("g.node_g")
                .transition()
                .duration(2500)
                .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
            
            d3.selectAll(GraphView.BaseGraphView.linkSvgClass)
                .transition()
                .duration(2500)
                .attr("points", outerThis.graphView.computePolyLineLinkPointsFunc)
                ;
    
        };
    }
    
    runCenterLayoutLambda(){
        var outerThis = this;
        return function(refreshLayout?: boolean){
        	if(refreshLayout){
    			// Act normal, redo the whole layout
    		}
    		
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
                    
                    if(node.rawConceptUri!=outerThis.centralConceptUri){
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
            
            d3.selectAll("g.node_g")
                .transition()
                .duration(2500)
                .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
            
            d3.selectAll(GraphView.BaseGraphView.linkSvgClass)
                .transition()
                .duration(2500)
                .attr("points", outerThis.graphView.computePolyLineLinkPointsFunc)
                ;
    
        };
    }
    
    
    runForceLayoutLambda(){
        var outerThis = this;
        return function(refreshLayout?: boolean): void{
            if(refreshLayout){
                // If we add a node to force layout, reheat it slightly
                outerThis.forceLayout.resume();
                return;
            }
            
            outerThis.forceLayout.friction(0.3) // use 0.2 friction to get a very circular layout
            .gravity(0.05) // 0.5
            .charge(-30) // -100
            ;
            outerThis.forceLayout.on("tick", outerThis.graphView.onLayoutTick());
            outerThis.forceLayout.start();
    
        };
    }
    
}