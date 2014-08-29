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
    
    getAllOntologyAcronyms(){
       // console.log("returning ontologies");
        var ontologies = [];
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
        graphNodes.forEach(function(node){
            //console.log(node.ontologyAcronym);
            if($.inArray(node.ontologyAcronym, ontologies)==-1){
                ontologies.push(node.ontologyAcronym);
              //  console.log("adding ontology");
            }
        });
           
        return ontologies;
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
    
    getOntologyAcronym(conceptUri){
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
        var ontologyAcronym;
        graphNodes.forEach(function(node){
            if(node.rawConceptUri===conceptUri){
                ontologyAcronym = node.ontologyAcronym;    
            }
        });
        return ontologyAcronym;
    }
  
    
    getChildren(rootIndex){
        var children: ConceptGraph.Node[];
        children = [];
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
        var graphLinks = outerThis.graph.graphD3Format.links;
        var rootNode = graphNodes[rootIndex];
        graphLinks.forEach(function(b){
            if(b.sourceId==rootNode.rawConceptUri&&b.relationType!="maps to"){
                var child: ConceptGraph.Node;
                child = new ConceptGraph.Node;
                graphNodes.forEach(function(c){
                    if(c.rawConceptUri==b.targetId){
                        child = c;
                    }
                });
                children.push(child);
            }
        });
    
        return children;
    }
    
    calculateDepth(rootIndex){
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
        var graphLinks = outerThis.graph.graphD3Format.links;
        var children = outerThis.getChildren(rootIndex);
        //console.log(children);
        if(children.length<=0){
            return;
        }else{
            children.forEach(function(child){
                if(child.depth<=graphNodes[rootIndex].depth){
                    child.depth = graphNodes[rootIndex].depth+1;
                }
                outerThis.calculateDepth(graphNodes.indexOf(child));
            });
        }
    }
    
    
    getRoots(ontology){
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
        var graphLinks = outerThis.graph.graphD3Format.links;
        var roots = [];       
        var isRoot = true;    
        var isaLinks = graphLinks.filter(function(l){return l.relationType!="maps to"});
        graphNodes = graphNodes.filter(function(n){return n.ontologyAcronym==ontology});
        graphNodes.forEach(function(node){
            isaLinks.forEach(function(link){
               if (link.targetId===node.rawConceptUri){
                   isRoot = false;
               }
            });
            if(isRoot){
               roots.push(node);       
            }
            isRoot = true;
        });
        console.log("roots");
        console.log(roots);
        return roots;
    }
    
    buildTree(width, height, ontologies){
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
        var graphLinks = outerThis.graph.graphD3Format.links;
        
        var trees = [];
        console.log("updated roots function");
        for (var i=0; i< ontologies.length; i++){
           var root = new ConceptGraph.Node();
           root.name = "phantom_root"; //temporary indentifier for the root
           
           //find how many roots here and store them into roots
           var roots = outerThis.getRoots(ontologies[i]);   
           
           //calculate depth for all roots here 
           roots.forEach(function(root: ConceptGraph.Node){
                outerThis.calculateDepth(graphNodes.indexOf(root));
           });

           trees[i] = d3.layout.tree()
                .size([width/ontologies.length, height])
                .children(function(d: ConceptGraph.Node){ 
                    if(d.name=="phantom_root"){  
                        return roots;
                    }else{
                        var actualChildren = outerThis.getChildren(graphNodes.indexOf(d)); 
                        var treeChildren: ConceptGraph.Node[];
                        treeChildren = []; 
                        actualChildren.forEach(function(b){
                            if(b.depth==d.depth+1){
                                treeChildren.push(b);     
                            }
                        });
                        return treeChildren;
                    }
            });

            //build a tree starting with the phantom root
            trees[i].nodes(root);
            
            //reset depth for next layout
            graphNodes.forEach(function (node){
                node.depth = 0;
            });
            
        }
                            
    }
    
    runRadialLayoutLambda(){
        var outerThis = this;
        return function(){
            outerThis.forceLayout.stop();
            var graphNodes = outerThis.graph.graphD3Format.nodes;
            var graphLinks = outerThis.graph.graphD3Format.links;
            var ontologyAcronym = outerThis.getOntologyAcronym(outerThis.centralConceptUri);
            var treeWidth = 360;
            var treeHeight = outerThis.graphView.visHeight()/2-100; 
            var ontologies = outerThis.getAllOntologyAcronyms();

            outerThis.buildTree(treeWidth, treeHeight, ontologies);
            for (var j=0; j<ontologies.length; j++){
                var increment= treeWidth/ontologies.length*j;
                var ontologyNodes = graphNodes.filter(function (d, i){return d.ontologyAcronym==ontologies[j]});
            
                $.each(ontologyNodes, function(index, element){
                        var radius = element.y+20*(ontologies.length-1);//make an offset if more than one ontology
                        var angle = (element.x+increment)/180 * Math.PI;
                        ontologyNodes[index].x = outerThis.graphView.visWidth()/2 + radius*Math.cos(angle); 
                        ontologyNodes[index].y = outerThis.graphView.visHeight()/2 + radius*Math.sin(angle); 
                    }
                );
            }
            
            outerThis.transitionNodes();
        };
    }

    runVerticalTreeLayoutLambda(){
        var outerThis = this;
        return function(){
            outerThis.forceLayout.stop();
            var graphNodes = outerThis.graph.graphD3Format.nodes;
            var graphLinks = outerThis.graph.graphD3Format.links;
            var treeWidth = outerThis.graphView.visWidth();
            var treeHeight = outerThis.graphView.visHeight()-300; 
            
            var ontologies = outerThis.getAllOntologyAcronyms();
            outerThis.buildTree(treeWidth, treeHeight, ontologies);
            
            for (var j=0; j<ontologies.length; j++){
                var increment= treeWidth/ontologies.length*j;
                var ontologyNodes = graphNodes.filter(function (d, i){return d.ontologyAcronym==ontologies[j]});
               

                $.each(ontologyNodes, function(index, element){
                      ontologyNodes[index].x = element.x+increment; 
                      ontologyNodes[index].y = element.y+150; 

                    }
                );
            }
            
            outerThis.transitionNodes();
        };
    }
    
    runHorizontalTreeLayoutLambda(){
        var outerThis = this;
        return function(){
            outerThis.forceLayout.stop();
            var graphNodes = outerThis.graph.graphD3Format.nodes;
            var graphLinks = outerThis.graph.graphD3Format.links;
            var treeWidth = outerThis.graphView.visHeight()-100;
            var treeHeight = outerThis.graphView.visWidth()-300;          
            var ontologies = outerThis.getAllOntologyAcronyms();
            outerThis.buildTree(treeWidth, treeHeight, ontologies);  
            for (var j=0; j<ontologies.length; j++){
                var increment= treeWidth/ontologies.length*j;
                var ontologyNodes = graphNodes.filter(function (d, i){return d.ontologyAcronym==ontologies[j]});
                $.each(ontologyNodes, function(index, element){
                      var xValue = element.x;
                      ontologyNodes[index].x = element.y + 150; 
                      ontologyNodes[index].y = xValue + increment; 
                });
            }
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