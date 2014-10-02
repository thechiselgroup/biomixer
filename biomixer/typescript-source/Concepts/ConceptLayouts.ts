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
        $(menuSelector).append($("<br>"));
        
        $(menuSelector).append($("<input>")
            .attr("class", "layoutButton")
            .attr("id", "horizontalTreeLayoutButton")
            .attr("type", "button")
            .attr("value", "Horizontal Tree Layout"));
        $(menuSelector).append($("<br>"));
    
        $(menuSelector).append($("<input>")
            .attr("class", "layoutButton")
            .attr("id", "verticalTreeLayoutButton")
            .attr("type", "button")
            .attr("value", "Vertical Tree Layout"));
        $(menuSelector).append($("<br>"));
    
       $(menuSelector).append($("<input>")
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
    
     transitionNodes(){
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
        var graphLinks = outerThis.graph.graphD3Format.links;
        
        d3.selectAll("g.node_g")
            .transition()
            .duration(2500)
            .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
           
        d3.selectAll(GraphView.BaseGraphView.linkSvgClass)
            .transition()
            .duration(2500)
            .attr("points", outerThis.graphView.computePolyLineLinkPointsFunc);
            
    }
    
    getAllOntologyAcronyms(){
        var ontologies = [];
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
        graphNodes.forEach(function(node){
            if($.inArray(node.ontologyAcronym, ontologies) === -1){
                ontologies.push(node.ontologyAcronym);
            }
        });
           
        return ontologies;
    }
    
     
    getChildren(parentNode: ConceptGraph.Node){
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
        var graphLinks = outerThis.graph.graphD3Format.links;
        var children: ConceptGraph.Node[] = [];

        graphLinks.forEach(function(link){
            if(link.sourceId==parentNode.rawConceptUri&&link.relationType!="maps to"){
                graphNodes.forEach(function(node){
                    if(node.rawConceptUri == link.targetId && $.inArray(node, children) === -1){
                        children.push(node);
                    }
                });               
            }
        });
    
        return children;
    }
    
    calculateDepth(parentNode: ConceptGraph.Node, depth){
        var outerThis = this;

        var children = outerThis.getChildren(parentNode);
        //console.log(children);
        if(children.length<=0){
            return depth;
        }else{
            children.forEach(function(child){
                if(child.tempDepth <= parentNode.tempDepth){
                    child.tempDepth = parentNode.tempDepth+1;
                    
                }
                if(child.tempDepth>depth) {depth++;}
                depth = outerThis.calculateDepth(child, depth);
                
            });
            return depth;
        }
    }
    
    getRoots(ontologyAcronym){
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
        var graphLinks = outerThis.graph.graphD3Format.links;
        var roots: ConceptGraph.Node[] = [];       
        var isRoot = true;    
        var graphLinks = graphLinks.filter(function(l){return l.relationType!="maps to"});
        graphNodes = graphNodes.filter(function(n){return n.ontologyAcronym==ontologyAcronym});
        graphNodes.forEach(function(node){
            graphLinks.forEach(function(link){
               if (link.targetId===node.rawConceptUri) { isRoot = false; }
            });
            if(isRoot) { roots.push(node); }       
            
            isRoot = true;
        });
        return roots;
    }
    
    buildTree(width, height){
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
        var ontologies = outerThis.getAllOntologyAcronyms();

        //reset values for next layout
        graphNodes.forEach(function (node){ 
            node.tempDepth = 0; 
            node.depth = 0;
            node.x = 0;
            node.y = 0;
            node.children = null;
            node.parent = null;
        });
        
        var fullTreeDepth = 0;
        
        var primaryRoot = new ConceptGraph.Node();
        primaryRoot.name = "main_phantom_root"; //temporary identifier for the root
        
        var ontologyRoots: ConceptGraph.Node[] = [];
        
        //create ontology roots
        ontologies.forEach(function(ontologyName){
            var ontologyRoot = new ConceptGraph.Node();
            ontologyRoot.name = ontologyName;
            ontologyRoots.push(ontologyRoot);
            var roots: ConceptGraph.Node[];
            
            roots = outerThis.getRoots(ontologyName);   
                    
            roots.forEach(function(root){
                var ontologyDepth = outerThis.calculateDepth(root, 0);
                if (ontologyDepth > fullTreeDepth) { fullTreeDepth = ontologyDepth; }
            });
            
        });
                
        var allChildren: ConceptGraph.Node[] = [];
        
        //calculate tree height and adjust for phantom nodes
        var oldHeight = height;
        height = height*(fullTreeDepth+2)/(fullTreeDepth)
        
        var mainTree = d3.layout.tree()
            .size([width, height])
            .children(function(parent: ConceptGraph.Node){
                if(parent.name == "main_phantom_root"){  
                    return ontologyRoots;
                }else if($.inArray(parent.name, ontologies) != -1){  
                    var roots: ConceptGraph.Node[];
                    roots = outerThis.getRoots(parent.name);   
                    
                    roots.forEach(function(root){
                        if($.inArray(root, allChildren) === -1){ allChildren.push(root); }
                    });

                    return roots;
                }else{
                    var graphChildren = outerThis.getChildren(parent); 
                    var treeChildren: ConceptGraph.Node[] = [];
 
                    graphChildren = graphChildren.sort(function(a, b){
                        if(a.rawConceptUri>b.rawConceptUri){
                            return -1;    
                        }else if(a.rawConceptUri<b.rawConceptUri){
                            return 1;
                        }else{
                            return 0;    
                        }});
                        
                    graphChildren.forEach(function(child){
                        if(child.tempDepth === parent.tempDepth+1 && $.inArray(child, allChildren) === -1){
                            treeChildren.push(child);
                            allChildren.push(child);
                        }
                    });
                    return treeChildren;
                }
           });
           
           mainTree.nodes(primaryRoot);    
        
           // shift the tree by 2 node distances
           graphNodes.forEach(function(node){
               node.y = node.y-2/(fullTreeDepth+2)*height;
           });
    }
    
    runRadialLayoutLambda(){
        var outerThis = this;
        return function(){
            outerThis.forceLayout.stop();
            var graphNodes = outerThis.graph.graphD3Format.nodes;

            var ontologies = outerThis.getAllOntologyAcronyms();
            var numOfRoots = 0;
            ontologies.forEach(function(o){
                var roots = outerThis.getRoots(o);
                numOfRoots+=roots.length;   
            });
            console.log(numOfRoots);
            var yShift = numOfRoots*20;
            var treeWidth = 360;
            var treeHeight = (outerThis.graphView.visHeight()-yShift-100)/2; 
 
            outerThis.buildTree(treeWidth, treeHeight);
                        
            $.each(graphNodes, function(index, element){
                 var radius = element.y+yShift/2; 
                 var angle = (element.x)/180 * Math.PI;
                 graphNodes[index].x = outerThis.graphView.visWidth()/2 + radius*Math.cos(angle); 
                 graphNodes[index].y = outerThis.graphView.visHeight()/2 + radius*Math.sin(angle); 
            }); 
            outerThis.transitionNodes();
        };
    }

    runVerticalTreeLayoutLambda(){
        var outerThis = this;
        return function(){
            outerThis.forceLayout.stop();
            var graphNodes = outerThis.graph.graphD3Format.nodes;

            var xShift = 100;
            var yShift = 200;
            var treeWidth = outerThis.graphView.visWidth()-xShift;
            var treeHeight = outerThis.graphView.visHeight()-yShift; 
            
            outerThis.buildTree(treeWidth, treeHeight);
            
            $.each(graphNodes, function(index, element){
                    graphNodes[index].x = element.x+xShift/2; 
                    graphNodes[index].y = element.y+yShift/2; 
                }
            );
                     
            outerThis.transitionNodes();
        };
    }
    
    runHorizontalTreeLayoutLambda(){
        var outerThis = this;
        return function(){
            outerThis.forceLayout.stop();
            var graphNodes = outerThis.graph.graphD3Format.nodes;

            var xShift = 300;
            var yShift = 100;
            var treeWidth = outerThis.graphView.visHeight()-yShift;
            var treeHeight = outerThis.graphView.visWidth()-xShift;   
                   
            outerThis.buildTree(treeWidth, treeHeight);  

            $.each(graphNodes, function(index, element){
                 var xValue = element.x;
                 graphNodes[index].x = element.y+xShift/2; 
                 graphNodes[index].y = xValue+yShift/2; 
            });
            
            outerThis.transitionNodes();
        };
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
            
            outerThis.transitionNodes();
    
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
            outerThis.transitionNodes();
        };
    }
    
    
    runForceLayoutLambda(){
        var outerThis = this;
        return function(){
            outerThis.forceLayout.friction(0.3) // use 0.2 friction to get a very circular layout
            .gravity(0.05) // 0.5
            .charge(-30); // -100
            
            outerThis.forceLayout.on("tick", outerThis.graphView.onLayoutTick());
            outerThis.forceLayout.start();
    
        };
    }
    
}