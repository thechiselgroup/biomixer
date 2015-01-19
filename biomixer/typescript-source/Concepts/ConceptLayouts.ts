///<reference path="headers/require.d.ts" />
///<reference path="headers/d3.d.ts" />

///<amd-dependency path="../JQueryExtension" />

///<amd-dependency path="LayoutProvider" />
///<amd-dependency path="GraphView" />
///<amd-dependency path="Menu" />
///<amd-dependency path="Concepts/ConceptPathsToRoot" />
///<amd-dependency path="Concepts/ConceptGraph" />

import LayoutProvider = require("../LayoutProvider");
import GraphView = require("../GraphView");
import Menu = require("../Menu");
import ConceptGraphView = require("./ConceptPathsToRoot");
import ConceptGraph = require("./ConceptGraph");

export class ConceptLayouts implements LayoutProvider.ILayoutProvider {

    constructor(
        public forceLayout: D3.Layout.ForceLayout,
        public graph: ConceptGraph.ConceptGraph,
        public graphView: ConceptGraphView.ConceptPathsToRoot,
        public centralConceptUri: ConceptGraph.ConceptURI
    ){
        
    }
    
    
    addMenuComponents(menuSelector: string){
        // Add the butttons to the pop-out panel
        var layoutsContainer = $("<div>").attr("id", "layoutMenuContainer");
        $(menuSelector).append(layoutsContainer);
                
        layoutsContainer.append($("<label>").addClass(Menu.Menu.menuLabelClass).text("Layouts"));
        layoutsContainer.append($("<br>"));
        
        var forceButton = $("<div>")
            .attr("id", "forceLayoutButton")
            .addClass("unselectable")
            .text("Force")
            .append(
                $("<div>")
                .attr("id", "forceLayoutButtonIcon")
                .css("float", "left")
                .addClass("unselectable")
                .addClass("iconLayoutButton")
                .attr("title", "Force-Directed Layout")
            )
            ;
        
        var circleButton = $("<div>")
            .attr("id", "circleLayoutButton")
            .addClass("unselectable")
            .text("Circle")
            .append(
                $("<div>")
                .attr("id", "circleLayoutButtonIcon")
                .css("float", "left")
                .addClass("unselectable")
                .addClass("iconLayoutButton")
                .attr("title", "Circle Layout")
            )
            ;
        
        var centerButton = $("<div>")
            .attr("id", "centerLayoutButton")
            .addClass("unselectable")
            .text("Center")
            .append(
                $("<div>")
                .attr("id", "centerLayoutButtonIcon")
                .css("float", "left")
                .addClass("unselectable")
                .addClass("iconLayoutButton")
                .attr("title", "Center Layout")
            )
            ;
        
        var horizTreeButton = $("<div>")
            .attr("id", "horizontalTreeLayoutButton")
            .addClass("unselectable")
            .text("Horizontal")
            .append(
                $("<div>")
                .attr("id", "horizontalTreeLayoutButtonIcon")
                .css("float", "left")
                .addClass("unselectable")
                .addClass("iconLayoutButton")
                .attr("title", "Horizontal Tree Layout")
            )
            ;
    
        var vertTreeButton = $("<div>")
            .attr("id", "verticalTreeLayoutButton")
            .addClass("unselectable")
            .text("Vertical")
            .append(
                $("<div>")
                .attr("id", "verticalTreeLayoutButtonIcon")
                .css("float", "left")
                .addClass("unselectable")
                .addClass("iconLayoutButton")
                .attr("title", "Vertical Tree Layout")
            )
            ;
    
        var radialButton = $("<div>")
            .attr("id", "radialLayoutButton")
            .addClass("unselectable")
            .text("Radial")
            .append(
                $("<div>")
                .attr("id", "radialLayoutButtonIcon")
                .css("float", "left")
                .addClass("unselectable")
                .addClass("iconLayoutButton")
                .attr("title", "Radial Layout")
            )
            ;

        var importButton = $("<input>")
            .attr("id", "importedLayoutButton")
            .addClass("nonIconLayoutButton")
            .attr("type", "button")
            .attr("value", "Imported Layout");
        
        
        var firstCol = $("<div>").css("float", "left");
        var secondCol = $("<div>").css("float", "left");
        var footer = $("<div>").css("clear", "both");
        layoutsContainer.append(firstCol);
        layoutsContainer.append(secondCol);
        layoutsContainer.append(footer);
        
        firstCol.append(centerButton);
        firstCol.append($("<br>"));
        firstCol.append(circleButton);
        firstCol.append($("<br>"));
        firstCol.append(forceButton);
        firstCol.append($("<br>"));
        firstCol.append(importButton);
        
        secondCol.append(vertTreeButton);
        secondCol.append($("<br>"));
        secondCol.append(horizTreeButton);
        secondCol.append($("<br>"));
        secondCol.append(radialButton);
        secondCol.append($("<br>"));
        
        d3.selectAll("#circleLayoutButton").on("click", this.applyNewLayoutLambda(this.runCircleLayoutLambda()));
        d3.selectAll("#forceLayoutButton").on("click", this.applyNewLayoutLambda(this.runForceLayoutLambda()));
        d3.selectAll("#centerLayoutButton").on("click", this.applyNewLayoutLambda(this.runCenterLayoutLambda()));
        d3.selectAll("#horizontalTreeLayoutButton").on("click", this.applyNewLayoutLambda(this.runHorizontalTreeLayoutLambda()));
        d3.selectAll("#verticalTreeLayoutButton").on("click", this.applyNewLayoutLambda(this.runVerticalTreeLayoutLambda()));
        d3.selectAll("#radialLayoutButton").on("click", this.applyNewLayoutLambda(this.runRadialLayoutLambda()));
        d3.selectAll("#importedLayoutButton").on("click", this.applyNewLayoutLambda(this.runFixedPositionLayoutLambda()));
        $("#importedLayoutButton").slideUp();
        
    }
    
    /**
     * The fixed layout currently allows for storing of only a single layout, but interacts with undo/redo.
     */
    private currentFixedLayoutData: {[nodeUri: string]: {x: number; y: number}} = {};
    
    getLayoutPositionSnapshot(): {[nodeUri: string]: {x: number; y: number}} {
        var positions: {[nodeUri: string]: {x: number; y: number}} = {};
        var graphNodes = this.graph.graphD3Format.nodes;
        $.each(graphNodes, (index, node)=>{ positions[String(node.rawConceptUri)] = {x: node.x, y: node.y}; } );
        return positions;
    }
    
    setLayoutFixedCoordinates(layout: {[nodeUri: string]: {x: number; y: number}}){
        if(undefined == layout){
            return;
        }
        this.currentFixedLayoutData = layout;
    }
    
    applyFixedLayout(){
        this.runFixedPositionLayoutLambda()(false);
    }
    
    applyNewLayoutLambda(layoutLambda: LayoutProvider.LayoutRunner){
        var outerThis = this;
        return ()=>{
            outerThis.graphView.setCurrentLayout(layoutLambda);
            outerThis.graphView.runCurrentLayout();
        };
    }
    
    setNewLayoutWithoutRunning(layoutLambda: LayoutProvider.LayoutRunner){
        this.graphView.setCurrentLayout(layoutLambda);
    }
    
    getLayoutRunner(): LayoutProvider.LayoutRunner{
        return this.graphView.currentLambda; // Not runCurrentLayout, because that's a wrapped version
    }
    
    private lastTransition = null;
    private staleTimerThreshold = 4000;
    private desiredDuration = 500;
    private lastRefreshArg = false;
    /**
     * If refresh, we use a timer to prevent stuttering.
     * If translateOnlyFixedNodes is true, then we have other nodes that will be unaffected
     * during the transition, probably because they are being animated in another way (such
     * as running a force layout concurrently that is meant to only apply to non-fixed nodes,
     * while we animate all of the fixed position nodes into their fixed positions).
     */
    private transitionNodes(refresh?: boolean, transitionOnlyFixedNodes: boolean = false){
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
        var graphLinks = outerThis.graph.graphD3Format.links;
        
        // This part is involved. To know if we have to smooth the time for the transition
        // as seen further down, we need to account for autoamtic layout refreshes and manual
        // triggers separately. In particular, we have to treat the first refresh in a series
        // of automatic refreshes as special and essentially equivalent to a manual non-refreshing
        // layout triggered by the user. Like I said, involved.
        var allowForDurationAdjustment = false;
        if(refresh === false){
            // For non-refreshign manual layout triggers, do not allow time adjustment,
            // always use the maximal transition animation duration.
            this.lastRefreshArg = false;
        } else if(refresh === true){
            // First refresh in series does not allow for time adjustment, subsequent do. 
            allowForDurationAdjustment = this.lastRefreshArg;
            this.lastRefreshArg = true;
        }

        var now = new Date().getTime();

        // When the graph is still growing, calls to the layout occur. This causes
        // a stuttering of movement if we do transitions, and without, the node
        // teleportation is confusing.
        // So, when we are refreshing a layout (node/edge addition), we need the
        // layout duration to continue along the path it was on. This *might* backfire
        // when we have an extremely long set of node or edge additions though.
        // If so, try having a minimum transition duration.
        var reduceDurationBy = 0;
        if(null !== this.lastTransition && allowForDurationAdjustment && (now - this.lastTransition) <= this.staleTimerThreshold){
            reduceDurationBy = now - this.lastTransition;
            // console.log("SHORTEN! By: "+reduceDurationBy);
        }
        var duration = this.desiredDuration - reduceDurationBy;

        d3.selectAll("g.node_g")
            .filter((node: ConceptGraph.Node, i: number)=>{ return transitionOnlyFixedNodes ? node.fixed : true; })
            .transition()
            .duration(duration)
            .ease("linear")
            .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });

        // NB If we are doing translateOnlyFixedNodes, will transitioning unfixed arcs break things?
        d3.selectAll(GraphView.BaseGraphView.linkSvgClass)
            .transition()
            .duration(duration)
            .ease("linear")
            .attr("points", outerThis.graphView.updateArcLineFunc);
        
        d3.selectAll(GraphView.BaseGraphView.linkMarkerSvgClass)
            .transition()
            .duration(duration)
            .ease("linear")
            .attr("points", outerThis.graphView.updateArcMarkerFunc);
    
       if(this.lastTransition === null || !refresh || (now - this.lastTransition) > this.staleTimerThreshold){
            this.lastTransition = new Date().getTime();
       }

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
    
    
    
    getChildren(parentNode: ConceptGraph.Node, graphLinks){
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
//        var graphLinks = outerThis.graph.graphD3Format.links;
        var children: ConceptGraph.Node[] = [];
        
        
        graphLinks.forEach(function(link){
            if(link.sourceId==parentNode.rawConceptUri&&link.relationType!="maps_to"){
                graphNodes.forEach(function(node){
                    if(node.rawConceptUri == link.targetId && $.inArray(node, children) === -1){
                        children.push(node);
                    }
                });               
            }
        });
    
        return children;
    }
    
    calculateDepth(parentNode: ConceptGraph.Node, depth, graphLinks){
        var outerThis = this;

        var children = outerThis.getChildren(parentNode, graphLinks);
            
//            .filter(function(c){
//            return !($.inArray(parentNode, outerThis.getChildren(c)) != -1);
//        });
        //console.log(children);
        if(children.length<=0){
            return depth;
        }else{
            children.forEach(function(child){
                if(child.tempDepth <= parentNode.tempDepth){
                    child.tempDepth = parentNode.tempDepth+1;
                    
                }
                if(child.tempDepth>depth) {depth++;}
                depth = outerThis.calculateDepth(child, depth, graphLinks);
                
            });
            return depth;
        }
    }
    
    getRoots(ontologyAcronym, graphLinks){
        var outerThis = this;
        var graphNodes = outerThis.graph.graphD3Format.nodes;
//        var graphLinks = outerThis.graph.graphD3Format.links;
        var roots: ConceptGraph.Node[] = [];       
        var isRoot = true;    
        var graphLinks = graphLinks.filter(function(l){return l.relationType!="maps_to"});
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
        var graphLinks = outerThis.graph.graphD3Format.links;
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
        
        var tempGraphLinks: ConceptGraph.Link[] = [];
        
        graphLinks.forEach(function(link){
            var cycleLink = false;
            tempGraphLinks.forEach(function(tempLink){
                if((link.sourceId==tempLink.targetId&&link.targetId==tempLink.sourceId)){
                    cycleLink = true;
                }
                
            });
            
            if(!cycleLink)tempGraphLinks.push(link);
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
            
            roots = outerThis.getRoots(ontologyName, tempGraphLinks);   
                    
            roots.forEach(function(root){
                var ontologyDepth = outerThis.calculateDepth(root, 0, tempGraphLinks);
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
                    roots = outerThis.getRoots(parent.name, tempGraphLinks);   
                    
                    roots.forEach(function(root){
                        if($.inArray(root, allChildren) === -1){ allChildren.push(root); }
                    });

                    return roots;
                }else{
                    var graphChildren = outerThis.getChildren(parent, tempGraphLinks); 
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
    
    runRadialLayoutLambda(): LayoutProvider.LayoutRunner{
        var outerThis = this;
        return function(refreshLayout?: boolean){
        	if(refreshLayout){
    			// Act normal, redo the whole layout
    		}
    		
            outerThis.forceLayout.stop();
            var graphNodes = outerThis.graph.graphD3Format.nodes;

            var ontologies = outerThis.getAllOntologyAcronyms();

            var tempGraphLinks: ConceptGraph.Link[] = [];
            var graphLinks = outerThis.graph.graphD3Format.links;

            graphLinks.forEach(function(link){
                var cycleLink = false;
                tempGraphLinks.forEach(function(tempLink){
                    if((link.sourceId==tempLink.targetId&&link.targetId==tempLink.sourceId)){
                        cycleLink = true;
                    }
                    
                });
                
                if(!cycleLink)tempGraphLinks.push(link);
            });
            
            var numOfRoots = 0;
            ontologies.forEach(function(o){
                var roots = outerThis.getRoots(o, tempGraphLinks);
                numOfRoots+=roots.length;   
            });
            console.log(numOfRoots);
            
            var minShift = 100;
            var maxShift = outerThis.graphView.visHeight()/2-100;  
            var yShift = numOfRoots*20;
            
            if( yShift < minShift ){ yShift = minShift; }
            if( yShift > maxShift ){ yShift = maxShift; }

            var treeWidth = 360;
            var treeHeight = (outerThis.graphView.visHeight()-yShift-100)/2; 
 
            outerThis.buildTree(treeWidth, treeHeight);

            $.each(graphNodes, function(index, element){
                 var radius = element.y+yShift/2; 
                 var angle = (element.x)/180 * Math.PI;
                 graphNodes[index].x = outerThis.graphView.visWidth()/2 + radius*Math.cos(angle); 
                 graphNodes[index].y = outerThis.graphView.visHeight()/2 + radius*Math.sin(angle); 
            }); 
            outerThis.transitionNodes(refreshLayout);
        };
    }

    runVerticalTreeLayoutLambda(): LayoutProvider.LayoutRunner{
        var outerThis = this;
        return function(refreshLayout?: boolean){
        	if(refreshLayout){
    			// Act normal, redo the whole layout
    		}
    		
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
              
            
            
            
            outerThis.transitionNodes(refreshLayout);
        };
    }
    
    runHorizontalTreeLayoutLambda(): LayoutProvider.LayoutRunner{
        var outerThis = this;
        return function(refreshLayout?: boolean){
        	if(refreshLayout){
    			// Act normal, redo the whole layout
    		}
    		
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
            
            outerThis.transitionNodes(refreshLayout);
        };
    }
    
    runCircleLayoutLambda(): LayoutProvider.LayoutRunner{
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
                    var angleForNode = i * anglePerNode; 
                    i++;
                    graphNodes[index].x = outerThis.graphView.visWidth()/2 + arcLength*Math.cos(angleForNode); // start in middle and let them fly outward
                    graphNodes[index].y = outerThis.graphView.visHeight()/2 + arcLength*Math.sin(angleForNode); // start in middle and let them fly outward
                }
            );
            
            outerThis.transitionNodes(refreshLayout);
    
        };
    }
    
    runCenterLayoutLambda(): LayoutProvider.LayoutRunner{
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
                function(index, node){
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
            outerThis.transitionNodes(refreshLayout);
        };
    }
    
    
    runForceLayoutLambda(): LayoutProvider.LayoutRunner{
        var outerThis = this;
        return function(refreshLayout?: boolean): void{
            if(refreshLayout){
                // If we add a node to force layout, reheat it slightly
                outerThis.forceLayout.resume();
                return;
            }
            
            var graphNodes = outerThis.graph.graphD3Format.nodes;

            // The nodes may have been fixed in the fixed layout, or when dragging them.
            // If we are not merely refreshing, let them all be free to move.
            $.each(graphNodes, (index, node)=>{ node.fixed = false; } );
            
            outerThis.forceLayout
            .friction(0.3) // use 0.2 friction to get a very circular layout
            .gravity(0.05) // 0.5
            .linkStrength(0.1)
        	.charge(-800) // If we use collision, we may not want repulsion, so set to 0 in that case.
            ;

            outerThis.forceLayout.on("tick", outerThis.graphView.onLayoutTick());
            outerThis.forceLayout.start();
        };
    }
    
    runFixedPositionLayoutLambda(): LayoutProvider.LayoutRunner{
        var outerThis = this;
        return function(refreshLayout?: boolean): void{
            if(refreshLayout){
                // Act normal, redo the whole layout
            }
            
            outerThis.forceLayout.stop();
            var graphNodes = outerThis.graph.graphD3Format.nodes;
            
            $.each(graphNodes,
                function(index, node){
                    if(undefined !== outerThis.currentFixedLayoutData[String(node.rawConceptUri)]){
                        node.x = outerThis.currentFixedLayoutData[String(node.rawConceptUri)].x;
                        node.y = outerThis.currentFixedLayoutData[String(node.rawConceptUri)].y;
                        node.fixed = true;
                    } else {
                        // Use whatever position is on the node already? Assign a random position??
                        // If using previous position, then shall we use a circle layout to do so (quite quick)?
                        // No, we will run the force layout while keeping all the nodes herein fixed!
                    }
                }
            );
            
            // The transition is needed for the fixed positions, but not the force layout ones...
            outerThis.transitionNodes(refreshLayout, true);
        }
    }
    
    public updateFixedLayoutDatum(nodeId: ConceptGraph.ConceptURI, coordinates: {x: number; y: number}){
        this.currentFixedLayoutData[String(nodeId)] = coordinates;
        $("#importedLayoutButton").slideDown();
    }
    
    public updateFixedLayoutData(newPositions: {[nodeId: string]: {x: number; y: number}}){
        for(var nodeId in newPositions){
            this.currentFixedLayoutData[nodeId] = newPositions[nodeId];
        }
        
        if(this.currentFixedLayoutData !== undefined){
            $("#importedLayoutButton").slideDown();
        } else {
            $("#importedLayoutButton").slideUp();
        }
    }
    
}