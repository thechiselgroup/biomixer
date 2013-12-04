var labelType, useGradients, nativeTextSupport, animate;

function visWidth(){ return $("#infovis").width(); }
function visHeight(){ return $("#infovis").height(); }
function linkMaxDesiredLength(){ return Math.min(visWidth(), visHeight())/2 - 50; }

var centralOntologyAcronym = purl().param("ontology_acronym");

var defaultNodeColor = "#000000";
var defaultLinkColor = "#999";
var defaultArcWidth = 1;
var defaultNodeSize = 10;

(function() {
  var ua = navigator.userAgent,
      iStuff = ua.match(/iPhone/i) || ua.match(/iPad/i),
      typeOfCanvas = typeof HTMLCanvasElement,
      nativeCanvasSupport = (typeOfCanvas == 'object' || typeOfCanvas == 'function'),
      textSupport = nativeCanvasSupport 
        && (typeof document.createElement('canvas').getContext('2d').fillText == 'function');
  //I'm setting this based on the fact that ExCanvas provides text support for IE
  //and that as of today iPhone/iPad current text support is lame
  labelType = (!nativeCanvasSupport || (textSupport && !iStuff))? 'Native' : 'HTML';
  nativeTextSupport = labelType == 'Native';
  useGradients = nativeCanvasSupport;
  animate = !(iStuff || !nativeCanvasSupport);
})();

var Log = {
  elem: false,
  write: function(text){
    if (!this.elem) 
      this.elem = document.getElementById('log');
    this.elem.innerHTML = text;
    this.elem.style.left = (500 - this.elem.offsetWidth / 2) + 'px';
  }
};


function getDefaultJson(){
  // init data
  return [
      {
        "adjacencies": [
            "graphnode21", // implicit node. it has no definition in the data
            {
              "nodeTo": "graphnode1",
              "nodeFrom": "graphnode0",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode13",
              "nodeFrom": "graphnode0",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode14",
              "nodeFrom": "graphnode0",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode15",
              "nodeFrom": "graphnode0",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode16",
              "nodeFrom": "graphnode0",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode17",
              "nodeFrom": "graphnode0",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }
        ],
        "data": {
          "$color": "#83548B",
          "$type": "circle",
          "$dim": 10,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode0",
        "name": "graphnode0"
      }, {
        "adjacencies": [
            {
              "nodeTo": "graphnode2",
              "nodeFrom": "graphnode1",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode4",
              "nodeFrom": "graphnode1",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode5",
              "nodeFrom": "graphnode1",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode6",
              "nodeFrom": "graphnode1",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode7",
              "nodeFrom": "graphnode1",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode8",
              "nodeFrom": "graphnode1",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode10",
              "nodeFrom": "graphnode1",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode11",
              "nodeFrom": "graphnode1",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode12",
              "nodeFrom": "graphnode1",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode13",
              "nodeFrom": "graphnode1",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode14",
              "nodeFrom": "graphnode1",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode15",
              "nodeFrom": "graphnode1",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode16",
              "nodeFrom": "graphnode1",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode17",
              "nodeFrom": "graphnode1",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }
        ],
        "data": {
          "$color": "#EBB056",
          "$type": "circle",
          "$dim": 11,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode1",
        "name": "graphnode1"
      }, {
        "adjacencies": [
            {
              "nodeTo": "graphnode5",
              "nodeFrom": "graphnode2",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode9",
              "nodeFrom": "graphnode2",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode18",
              "nodeFrom": "graphnode2",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }
        ],
        "data": {
          "$color": "#416D9C",
          "$type": "circle",
          "$dim": 7,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode2",
        "name": "graphnode2"
      }, {
        "adjacencies": [
            {
              "nodeTo": "graphnode5",
              "nodeFrom": "graphnode3",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode9",
              "nodeFrom": "graphnode3",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode10",
              "nodeFrom": "graphnode3",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode12",
              "nodeFrom": "graphnode3",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }
        ],
        "data": {
          "$color": "#416D9C",
          "$type": "square",
          "$dim": 10,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode3",
        "name": "graphnode3"
      }, {
        "adjacencies": [],
        "data": {
          "$color": "#83548B",
          "$type": "square",
          "$dim": 11,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode4",
        "name": "graphnode4"
      }, {
        "adjacencies": [
          {
            "nodeTo": "graphnode9",
            "nodeFrom": "graphnode5",
            "data": {
              "$color": defaultLinkColor,
              '$arcWidthBasis': defaultArcWidth,
            }
          }
        ],
        "data": {
          "$color": "#C74243",
          "$type": "triangle",
          "$dim": 8,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode5",
        "name": "graphnode5"
      }, {
        "adjacencies": [
            {
              "nodeTo": "graphnode10",
              "nodeFrom": "graphnode6",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode11",
              "nodeFrom": "graphnode6",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }
        ],
        "data": {
          "$color": "#83548B",
          "$type": "circle",
          "$dim": 11,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode6",
        "name": "graphnode6"
      }, {
        "adjacencies": [],
        "data": {
          "$color": "#EBB056",
          "$type": "triangle",
          "$dim": 12,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode7",
        "name": "graphnode7"
      }, {
        "adjacencies": [],
        "data": {
          "$color": "#C74243",
          "$type": "star",
          "$dim": 10,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode8",
        "name": "graphnode8"
      }, {
        "adjacencies": [],
        "data": {
          "$color": "#83548B",
          "$type": "circle",
          "$dim": 12,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode9",
        "name": "graphnode9"
      }, {
        "adjacencies": [
          {
            "nodeTo": "graphnode11",
            "nodeFrom": "graphnode10",
            "data": {
              "$color": defaultLinkColor,
              '$arcWidthBasis': defaultArcWidth,
            }
          }
        ],
        "data": {
          "$color": "#70A35E",
          "$type": "triangle",
          "$dim": 11,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode10",
        "name": "graphnode10"
      }, {
        "adjacencies": [],
        "data": {
          "$color": "#70A35E",
          "$type": "circle",
          "$dim": 11,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode11",
        "name": "graphnode11"
      }, {
        "adjacencies": [],
        "data": {
          "$color": "#83548B",
          "$type": "triangle",
          "$dim": 10,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode12",
        "name": "graphnode12"
      }, {
        "adjacencies": [
          {
            "nodeTo": "graphnode14",
            "nodeFrom": "graphnode13",
            "data": {
              "$color": defaultLinkColor,
              '$arcWidthBasis': defaultArcWidth,
            }
          }
        ],
        "data": {
          "$color": "#EBB056",
          "$type": "star",
          "$dim": 7,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode13",
        "name": "graphnode13"
      }, {
        "adjacencies": [],
        "data": {
          "$color": "#EBB056",
          "$type": "triangle",
          "$dim": 12,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode14",
        "name": "graphnode14"
      }, {
        "adjacencies": [
            {
              "nodeTo": "graphnode16",
              "nodeFrom": "graphnode15",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode17",
              "nodeFrom": "graphnode15",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }
        ],
        "data": {
          "$color": "#83548B",
          "$type": "triangle",
          "$dim": 11,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode15",
        "name": "graphnode15"
      }, {
        "adjacencies": [
          {
            "nodeTo": "graphnode17",
            "nodeFrom": "graphnode16",
            "data": {
              "$color": defaultLinkColor,
              '$arcWidthBasis': defaultArcWidth,
            }
          }
        ],
        "data": {
          "$color": "#C74243",
          "$type": "star",
          "$dim": 7,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode16",
        "name": "graphnode16"
      }, {
        "adjacencies": [],
        "data": {
          "$color": "#416D9C",
          "$type": "circle",
          "$dim": 7,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode17",
        "name": "graphnode17"
      }, {
        "adjacencies": [
            {
              "nodeTo": "graphnode19",
              "nodeFrom": "graphnode18",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }, {
              "nodeTo": "graphnode20",
              "nodeFrom": "graphnode18",
              "data": {
                "$color": defaultLinkColor,
                '$arcWidthBasis': defaultArcWidth,
              }
            }
        ],
        "data": {
          "$color": "#EBB056",
          "$type": "triangle",
          "$dim": 9,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode18",
        "name": "graphnode18"
      }, {
        "adjacencies": [],
        "data": {
          "$color": "#70A35E",
          "$type": "circle",
          "$dim": 8,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode19",
        "name": "graphnode19"
      }, {
        "adjacencies": [],
        "data": {
          "$color": "#C74243",
          "$type": "star",
          "$dim": 8,
          '$nodeDiameterBasis': defaultNodeSize,
        },
        "id": "graphnode20",
        "name": "graphnode20"
      }
  ];
  // end
}

function init(){
	var fd = initForceDirected();
//	loadData(getDefaultJson(), fd);
	fetchOntologyNeighbourhood(centralOntologyAcronym, fd);

	
}

function loadData(json, fd){
	//load JSON data.
	  fd.loadJSON(json);
	  // compute positions incrementally and animate.
	  fd.computeIncremental({
	    iter: 40,
	    property: 'end',
	onStep: function(perc){
	  Log.write(perc + '% loaded...');
	},
	onComplete: function(){
	  Log.write('done');
	  fd.animate({
	    modes: ['linear'],
	        transition: $jit.Trans.Elastic.easeOut,
	        duration: 2500
	      });
	    }
	  });
	  // end
}

function initForceDirected(){
  // init ForceDirected
  var fd = new $jit.ForceDirected({
    //id of the visualization container
    injectInto: 'infovis',
    //Enable zooming and panning
    //with scrolling and DnD
    Navigation: {
      enable: true,
      type: 'Native',
      //Enable panning events only if we're dragging the empty
      //canvas (and not a node).
      panning: 'avoid nodes',
      zooming: 10 //zoom speed. higher is more sensible
    },
    // Change node and edge styles such as
    // color and width.
    // These properties are also set per node
    // with dollar prefixed data-properties in the
    // JSON structure.
    Node: {
      overridable: true,
      // dim: 7 // cosntant size for all nodes this way. Can't map like in cytoscape.
    },
    Edge: {
      overridable: true,
      color: '#23A4FF',
      lineWidth: 0.4
    },
    // From sample 1
    //Add Tips
    Tips: {
      enable: true,
      onShow: function(tip, node) {
        //count connections
        var count = 0;
        node.eachAdjacency(function() { count++; });
        //display node info in tooltip
        tip.innerHTML = "<div class=\"tip-title\">" + node.name + "</div>"
          + "<div class=\"tip-text\"><b>connections:</b> " + count + "</div>";
      }
    },
    // Add node events
    Events: {
      enable: true,
      type: 'Native',
      //Change cursor style when hovering a node
      onMouseEnter: function() {
        fd.canvas.getElement().style.cursor = 'move';
      },
      onMouseLeave: function() {
        fd.canvas.getElement().style.cursor = '';
      },
      //Update node positions when dragged
      onDragMove: function(node, eventInfo, e) {
        var pos = eventInfo.getPos();
        node.pos.setc(pos.x, pos.y);
        fd.plot();
      },
      //Implement the same handler for touchscreens
      onTouchMove: function(node, eventInfo, e) {
        $jit.util.event.stop(e); //stop default touchmove event
        this.onDragMove(node, eventInfo, e);
      },
      // From sample 1
      //Add also a click handler to nodes
      onClick: function(node) {
        if(!node) return;
        // Build the right column relations list.
        // This is done by traversing the clicked node connections.
        var html = "<h4>" + node.name + "</h4><b> connections:</b><ul><li>",
            list = [];
        node.eachAdjacency(function(adj){
          list.push(adj.nodeTo.name);
        });
        //append connections information
        $jit.id('inner-details').innerHTML = html + list.join("</li><li>") + "</li></ul>";
      }
    },
    //Number of iterations for the FD algorithm
    iterations: 200,
    //Edge length
    levelDistance: 130,
    // This method is only triggered
    // on label creation and only for DOM labels (not native canvas ones).
    onCreateLabel: function(domElement, node){
      // Create a 'name' and 'close' buttons and add them
      // to the main node label
      var nameContainer = document.createElement('span'),
          closeButton = document.createElement('span'),
          style = nameContainer.style;
      nameContainer.className = 'name';
      nameContainer.innerHTML = node.name;
      closeButton.className = 'close';
      closeButton.innerHTML = 'x';
      domElement.appendChild(nameContainer);
      domElement.appendChild(closeButton);
      style.fontSize = "0.8em";
      style.color = "#ddd";
      //Fade the node and its connections when
      //clicking the close button
      closeButton.onclick = function() {
        node.setData('alpha', 0, 'end');
        node.eachAdjacency(function(adj) {
          adj.setData('alpha', 0, 'end');
        });
        fd.fx.animate({
          modes: ['node-property:alpha',
                  'edge-property:alpha'],
          duration: 500
        });
      };
      //Toggle a node selection when clicking
      //its name. This is done by animating some
      //node styles like its dimension and the color
      //and lineWidth of its adjacencies.
      nameContainer.onclick = function() {
        //set final styles (also shrinks previously selected nodes, and all adjacent nodes)
        fd.graph.eachNode(function(n) {
          if(n.id != node.id) delete n.selected;
          n.setData('dim', n.getData('nodeDiameterBasis'), 'end'); // clobbers original node size.
          n.eachAdjacency(function(adj) {
            adj.setDataset('end', {
              lineWidth: adj.getData('arcWidthBasis'),
              color: '#23a4ff'
            });
          });
        });
        // Select a node, setting the node and arcs to be larger
        if(!node.selected) {
          node.selected = true;
          node.setData('dim', 17, 'end');
          node.eachAdjacency(function(adj) {
            adj.setDataset('end', {
              lineWidth: 3,
              color: '#36acfb'
            });
          });
        } else {
          delete node.selected;
        }
        //trigger animation to final styles
        fd.fx.animate({
          modes: ['node-property:dim',
                  'edge-property:lineWidth:color'],
          duration: 500
        });
        // Build the right column relations list.
        // This is done by traversing the clicked node connections.
        var html = "<h4>" + node.name + "</h4><b> connections:</b><ul><li>",
            list = [];
        node.eachAdjacency(function(adj){
          if(adj.getData('alpha')) list.push(adj.nodeTo.name);
        });
        //append connections information
        $jit.id('inner-details').innerHTML = html + list.join("</li><li>") + "</li></ul>";
      };
    },
    // Change node styles when DOM labels are placed
    // or moved.
    onPlaceLabel: function(domElement, node){
      var style = domElement.style;
      var left = parseInt(style.left);
      var top = parseInt(style.top);
      var w = domElement.offsetWidth;
      style.left = (left - w / 2) + 'px';
      style.top = (top + 10) + 'px';
      style.display = '';
    }
  });
 return fd; 
}


function fetchOntologyNeighbourhood(centralOntologyAcronym, fd){
	// I have confirmed that this is faster than BioMixer. Without removing
	// network latency in REST calls, it is approximately half as long from page load to
	// graph completion (on the order of 11 sec vs 22 sec)
	// TODO XXX Then try adding web workers around things to see if it affects it further.
	
	// TODO XXX I lose all the error handling and retry handling that I set up in BioMixer.
	// This is our first loss, that we have to futz with that again. It can be recreated, or if this
	// is fast enough, we can adapt things so that some of the Java work in BioMixer can be used here too
	// I mostly need to bypass the overall architecture of BioMixer to see how it affects loading speed
	// and responsivity, as well as to try using web workers (which don't work with GWT 2.5 right now)
	
	/* Adding BioPortal data for ontology overview graph (mapping neighbourhood of a single ontology node)
	1) Get the mapped ontology ids from the target ontology id [starts at line 126 in OntologyMappingNeighbourhood]
	   http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fvirtual%2Fmappings%2Fstats%2Fontologies%2F1033&callback=__gwt_jsonp__.P0.onSuccess
	   - can create nodes and links with sparse meta-data now if we want, or we can wait for more data
	2) Get ontology details, which is one big json return [passed to line 167 for class OntologyMappingNeighbourhoodLoader nested class OntologyDetailsCallback]
	   http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fontologies%2F&callback=__gwt_jsonp__.P1.onSuccess
	   - fill in nodes with details from this data
	3) Get ontology metrics for each ontology [line 82 in AutomaticOntologyExpander]
	   - set node size (# of concepts), and tool tip properties of classes, individuals, properties, and notes
	   http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fontologies%2Fmetrics%2F45254&callback=__gwt_jsonp__.P7.onSuccess
	*/
	
//	console.log("Begun fetchingOntologyNeighbourhood");
	
	// 1) Get mappings to central ontology
	var ontologyMappingUrl = buildOntologyMappingUrlNewApi(centralOntologyAcronym);
	var ontologyMappingCallback = new OntologyMappingCallback(ontologyMappingUrl, centralOntologyAcronym, fd);
//	var fetcher = new RetryingJsonpFetcher(ontologyMappingCallback);
//	fetcher.retryFetch();
	var fetcher = closureRetryingJsonpFetcher(ontologyMappingCallback);
	fetcher();
}

function OntologyMappingCallback(url, centralOntologyAcronym, fd){
	this.url = url;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	var self = this;
	
	this.callback = function (mappingData, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
//		console.log("Begun OntologyMappingCallback");
//		var errorOrRetry = self.fetcher.retryFetch(mappingData);
		var errorOrRetry = self.fetcher(mappingData);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		
		var numberOfMappedOntologies = Object.keys(mappingData).length;
		
		var defaultNumOfTermsForSize = 10;
		
		// New API example: http://stagedata.bioontology.org/mappings/statistics/ontologies/SNOMEDCT/?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a
		
		// Create the central node
		var centralOntologyNode = {"id": centralOntologyAcronym,
    		    	"data": {
    			          "$color": nextNodeColor(),
    			          "$dim": defaultNodeSize,
    			          '$nodeDiameterBasis': defaultNodeSize,
    			        },
    		        "adjacencies": [
    		            
    		        ]};
		centralOntologyNode.name = "fetching";
		centralOntologyNode.data.description = "fetching description";
		// Fixed and position...do they work in JIT?
		centralOntologyNode.data.fixed = true; // lock central node
		// Cyto needs p instead of naked x and y
		centralOntologyNode.data.p = {
				x: visWidth()/2,
				y: visHeight()/2
				};
//		globalNodePositionMap[centralOntologyAcronym] = centralOntologyNode.p;
		centralOntologyNode.data.weight = numberOfMappedOntologies; // will increment as we loop
		centralOntologyNode.data.acronym = centralOntologyAcronym;
		
		// TODO In the example, 'weight' is added nested within 'data':
		//	{
		//	    group: "nodes",
		//	    data: { weight: 75 },
		//	    position: { x: 200, y: 200 }
		//	}
		// If things get funky, look into that. It might be required, or we might clobber cyto properties accidentally.
		// It also had id nested in data, and source, target, but *not* position or group.
		
		
		// Cytoscape specific stuff (x and y above work for Cytoscape)
//		centralOntologyNode.data = {};
//		centralOntologyNode.data.id = centralOntologyAcronym;
//		centralOntologyNode.data.nodeColor = nextNodeColor();
		centralOntologyNode.data.nodeDiameterBasis = defaultNumOfTermsForSize; // number of terms
		// Node size is controlled by a css function map(nodeDiameter) or dataMap(nodeDiameter).
		centralOntologyNode.data.dim = ontologyNodeScalingFunc(centralOntologyNode.data.nodeDiameterBasis);
		
//		ontologyNeighbourhoodJsonForGraph.nodes.push(centralOntologyNode);
		
		// Add central node to JIT later on with the rest
		// Needed because of our flow through multiple REST responses...easiest and fastest to index in with id
		// rather than using JQuery
		var ontologyAcronymNodeMap = new Object(); 
		$(ontologyAcronymNodeMap).attr(centralOntologyAcronym, centralOntologyNode);
		
		jitNodeCollection = [];
		jitNodeCollection.push(centralOntologyNode);
		
		// TODO XXX Either the parsing or the looping here causes a visible glitch in rendering,
		// so this is the first place to try a web worker out.

		// Make some graph parts!
		// I used degrees here and it gave me a strange mandala with gaps between groups of nodes.
		var anglePerNode = 2*Math.PI / numberOfMappedOntologies; // 360/numberOfMappedOntologies;
		var arcLength = linkMaxDesiredLength();
		var i = 0;
		
		// This randomizer is to keep nodes at the top and bottom of the circular layout
		// from completely overlapping their labels. The overall effect was a replication of
		// the resulting D3 force layout with circular seeding.
		var distanceSeed = 0;
		var distanceRandomizer = function(){
			// vary it between +0.1 and -0.1 in
			// return (Math.random() - 0.5) / 5;
			// cycle it through as sine wave
			distanceSeed += 2*Math.PI * 0.05;
			return Math.sin(distanceSeed % 2*Math.PI)/10
		};  
		
		$.each(mappingData,
			function(index, element){
				var acronym = index;

				if(typeof acronym === "undefined"){
					console.log("Undefined ontology entry");
				}
				
				// TODO For Cytoscape.JS, I don't like using arbor.js, and the circle layout
				// doesn't have a central node. For D3, I was using the force layout with initialization
				// in a circle.
				// For Cytoscape.JS, I don't think I can initialize and get results like I want.
				// In either case, tree layouts are still required later. Cytoscape has a native
				// layout called 'breadthfirst' that might work out, and perhaps we can tweak arbor.
				// TODO Add a positions node-id: {x, y} map for use with the 'preset' Cyto layout. 
				
				// Create the neighbouring nodes
				var ontologyNode = {"id": acronym,
		    		    	"data": {
		    			          "$color": nextNodeColor(),
		    			          "$dim": defaultNodeSize,
		    			          '$nodeDiameterBasis': defaultNodeSize,
		    			        },
		    		        "adjacencies": [
		    		            
		    		        ]};

				ontologyNode.data.name = "fetching";
				ontologyNode.data.description = "fetching description";
				ontologyNode.data.weight = 1;
				ontologyNode.data.fixed = false; // lock central node
				// Compute starting positions to be in a circle for faster layout
				var angleForNode = i * anglePerNode; i++;
				 // Cyto needs p instead of naked x and y
				var randomFactor = distanceRandomizer();
				ontologyNode.data.p = {
						x: visWidth()/2 + arcLength*Math.cos(angleForNode) + arcLength*randomFactor, // start in middle and let them fly outward
						y: visHeight()/2 + arcLength*Math.sin(angleForNode) + arcLength*randomFactor // start in middle and let them fly outward
				};
//				globalNodePositionMap[acronym] = ontologyNode.p;
				ontologyNode.data.acronym = acronym;
				
				
				// Cytoscape specific stuff (x and y above work for Cytoscape)
//				ontologyNode.data = {};
//				ontologyNode.data.id = acronym;
//				ontologyNode.data.nodeColor = nextNodeColor();
				ontologyNode.data.nodeDiameterBasis = defaultNumOfTermsForSize; // number of terms
				// Node size is controlled by a css function map(nodeDiameter) or dataMap(nodeDiameter).
				ontologyNode.data.dim = ontologyNodeScalingFunc(ontologyNode.data.nodeDiameterBasis);
				
				// TODO Cyto doesn't need the structure here...ontologyNeighbourhoodJsonForGraph
//				var targetIndex = ontologyNeighbourhoodJsonForGraph.nodes.push(ontologyNode) - 1;
				
				// But it does like this mapping of ids to edges and nodes...
				$(ontologyAcronymNodeMap).attr(acronym, ontologyNode);
				
				jitNodeCollection.push(ontologyNode);
				

				// Make the links at the same time; they are done now!
				var ontologyLink = {
			              "nodeTo": centralOntologyNode.data.acronym,
			              "nodeFrom": ontologyNode.data.acronym,
			              "data": {
			            	"$id": centralOntologyNode.acronym+"->"+ontologyNode.acronym,
			                "$color": defaultLinkColor,
			                '$arcWidthBasis': element,
			                '$linkThickness': 1,
			              }
			            };
				ontologyLink.data.linkThickness = ontologyLinkScalingFunc(ontologyLink.data.arcWidthBasis);
				
//				ontologyLink.source = centralOntologyNode; // TODO This is in data for Cyto, remove this, right?
//				ontologyLink.target = ontologyNode; // TODO This is in data for Cyto, remove this, right?
				
				// Cytoscape specific
				ontologyLink.data = {};
//				ontologyLink.data.id = centralOntologyNode.acronym+"->"+ontologyNode.acronym;
//				ontologyLink.data.source = centralOntologyNode.data.id; //centralOntologyNode;
//				ontologyLink.data.target = ontologyNode.data.id; //ontologyNode;
				ontologyLink.data.linkThicknessBasis = element; // This gets used for link stroke thickness later.
				ontologyLink.data.linkThickness = ontologyLinkScalingFunc(ontologyLink.data.linkThicknessBasis);
				ontologyLink.data.numMappings = element;
				ontologyLink.group = "edges"; // Cytoscape specific
				
				// TODO Cyto doesn't need the structure here...ontologyNeighbourhoodJsonForGraph
//				ontologyNeighbourhoodJsonForGraph.links.push(ontologyLink);
				
				// For JIT
				ontologyNode.adjacencies.push(ontologyLink);
				
				// But it does like this mapping of ids to edges and nodes...
				$(ontologyAcronymNodeMap).attr(ontologyLink.data.id, ontologyLink);
				
				// Add the node and link for JIT later with the rest.
//				cy.add(ontologyNode);
//				cy.add(ontologyLink);
			}
		);
		
		console.log(jitNodeCollection);

		// Not sure about whether to do this here or not...
		// console.log("ontologyMappingCallback");
		// populateGraph(ontologyNeighbourhoodJsonForGraph, true);
		// For JIT, might want to initialize graph here rather than outside, if it
		// requires any information from the data at the time of creation.
		
//		json format like so (http://philogb.github.io/jit/static/v20/Docs/files/Loader/Loader-js.html#Loader):
	                   
	                   // It appears that calling graph.getNode(id) gets us a node object which contains a property,
	                   // node.data, that corresponds to our custom node data (can put in line thickness, description, name, etc).
	                   // what about labels? will thicknesses and node size have to be redrawn manually too? Looking at the click listener...
		
//		LEFTOFF TODO Get data formatted for JIT, ensure this is the way to load.
//		Find out how to update once loaded.
//		JIT accepts data with unique id when loading. Updating might use the same method...
//		which means inefficient updates.
		loadData(jitNodeCollection, fd); // ontologyNeighbourhoodJsonForGraph
		
		return;
		
		//----------------------------------------------------------
		
		// 2) Get details for all the ontologies (and either create or update the nodes)
		var ontologyDetailsUrl = buildOntologyDetailsUrlNewApi();
		var ontologyDetailsCallback = new OntologyDetailsCallback(ontologyDetailsUrl, ontologyAcronymNodeMap, fd);
//		var fetcher = new RetryingJsonpFetcher(ontologyDetailsCallback);
//		fetcher.retryFetch();
		var fetcher = closureRetryingJsonpFetcher(ontologyDetailsCallback);
		fetcher();
	}
	
}

function OntologyDetailsCallback(url, ontologyAcronymNodeMap, fd){
	this.url = url;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	// Need to fetch existing node objects from this
	this.ontologyAcronymNodeMap = ontologyAcronymNodeMap;
	var self = this;

	this.callback  = function ontologyDetailsCallback(detailsDataRaw, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
//		console.log("Begun OntologyDetailsCallback");
//		var errorOrRetry = self.fetcher.retryFetch(detailsDataRaw);
		var errorOrRetry = self.fetcher(detailsDataRaw);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		
		//TODO For Cytoscape, to update element properties, use things like cy.batchData(<id to <prop to value>>).
		// If things with the provided ids exist already, it modifies their properties (or clobber replaces them??)
		// So...after we update our stored dat,a we can pass that on in, because the stored stuff is stored in this
		// exact way in our 'ontologyAcronymNodeMap'! Delightful!
		// We can also use cy.$(<selector>), or cy.elements(), cy.nodes() cy.edges(), cy.filter().
		
		// Loop over ontologies and add their additional properties to the nodes
		// Recall that getting *all* ontology details is the easiest (only) way,
		// so we have to skip anything that is not defined.
		var ontologiesSkipped = 0;
		var ontologyUpdateMap = {};
		$.each(detailsDataRaw,
				function(index, ontologyDetails){
					// I can't cherry pick, because this involves iterating
					// through the entire set of ontologies to find each ontology entry.
					// So, I will do a separate loop, and only use data for which there
					// exists in the graph a corresponding ontology.
					// Make use of details to add info to ontologies
					var ontologyAcronym = ontologyDetails.acronym;
					
					// TODO LEFTOFF Updating for Cytoscape
					
					// TODO For cytoscape, do we want to retrieve from cy.nodes()?
					// Batch update might make us want to keep separate data.
					// TODO It might be better to *not* keep around this ontologoy map.
					// Cytoscape updates any properties provided, which means we could make smaller
					// containers with just the new values and not the whole whack of graph data.
					// This seems like it would speed updating. I will try it that way.
					var node = $(self.ontologyAcronymNodeMap).attr(ontologyAcronym);
					
					if(typeof node === "undefined"){
						// Skip node details that aren't in our graph
						ontologiesSkipped += 1;
						return;
					}
					
					var nodeUpdate = {};
//					ontologyUpdateMap[node.data.id] = nodeUpdate;
					
					nodeUpdate.name = ontologyDetails.name;
//					nodeUpdate.ONTOLOGY_VERSION_ID = ontologyDetails.id;
					nodeUpdate.uriId = ontologyDetails["@id"]; // Use the URI instead of virtual id
					// In Cytoscape, use 'content' as label
					nodeUpdate.content = ontologyDetails.name;
					// nodeUpdate.description = ontologyDetails.description; // Unavailable in details call
//					nodeUpdate.VIEWING_RESTRICTIONS = ontologyDetails.viewingRestrictions; // might be missing

					// TODO XXX If we want Description, I think we need to grab the most recent submission
					// and take it fromt here. This is another API call per ontology.
					// /ontologies/:acronym:/lastest_submission
					
					// --------------------------------------------------------------
					// Do this in the details callback, then? Do we need anything from details in
					// order to get metrics? Do we need the ontology id?
					// 3) Get metric details for each ontology
					{
						// The metric call has much of the info we need
						var ontologyMetricsUrl = buildOntologyMetricsUrlNewApi(node.data.acronym);
						var ontologyMetricsCallback = new OntologyMetricsCallback(ontologyMetricsUrl, node);
	//					var fetcher = new RetryingJsonpFetcher(ontologyMetricsCallback);
	//					fetcher.retryFetch();
						var fetcher = closureRetryingJsonpFetcher(ontologyMetricsCallback);
						fetcher();
					}
					
					{
						// Details are in the submissions, so we need an additional call.
						var ontologyDescriptionUrl = buildOntologyLatestSubmissionUrlNewApi(node.data.acronym);
						var ontologyDescriptionCallback = new OntologyDescriptionCallback(ontologyDescriptionUrl, node);
						var fetcher = closureRetryingJsonpFetcher(ontologyDescriptionCallback);
						fetcher();
					}
				}
		);

		// We usually use very many of the ontologies, so it is likely cheaper to make the one
		// big call with no ontology acronym arguments than to cherry pick the ones we want details for.
		console.log("ontologyDetailsCallback, skipped "+ontologiesSkipped+" of total "+detailsDataRaw.length);
		
//		updateDataForNodesAndLinks(ontologyUpdateMap);
			
	}
}

function OntologyMetricsCallback(url, node){
	this.url = url;
	this.node = node;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	var self = this;
	
	this.callback = function (metricDataRaw, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
//		console.log("Begun OntologyMetricsCallback");
//		var errorOrRetry = 	self.fetcher.retryFetch(metricDataRaw);
		var errorOrRetry = 	self.fetcher(metricDataRaw);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		
		var metricData = metricDataRaw;
		
		var nodeSizeBasis = 100;
		var numClasses=0, numIndividuals=0, numProperties=0;
	    if (typeof metricData !== "undefined") {
	        if (metricData.classes != null) {
	            numClasses = metricData.classes;
	            nodeSizeBasis = numClasses;
	        }
	        if (metricData.individuals != null) {
	            numIndividuals = metricData.individuals;
	        }
	        if (metricData.properties != null) {
	            numProperties = metricData.properties;
	        }
	    }
	    
	    
//		self.node.weight = 1;
//		self.node.numberOfClasses = numClasses;
//		self.node.numberOfIndividuals = numIndividuals;
//		self.node.numberOfProperties = numProperties;
//		self.node.number = nodeSizeBasis;
		
	    var ontologyUpdateMap = {};
	    var nodeUpdate = {};
	    ontologyUpdateMap[node.data.id] = nodeUpdate;
	    
//	    nodeUpdate.weight = 1;
	    nodeUpdate.numberOfClasses = numClasses;
	    nodeUpdate.numberOfIndividuals = numIndividuals;
	    nodeUpdate.numberOfProperties = numProperties;
	    nodeUpdate.nodeDiameterBasis = nodeSizeBasis;
		// Node size is controlled by a css function map(nodeDiameter) or dataMap(nodeDiameter).
	    // This gets updated en masse later.
//	    nodeUpdate.nodeDiameter = ontologyNodeScalingFunc(nodeUpdate.nodeDiameterBasis);
	    
	    
		// console.log("ontologyMetricsCallback");
//		updateDataForNodesAndLinks({nodes:[self.node], links:[]});
//		updateDataForNodesAndLinks(ontologyUpdateMap);
	}
}

function OntologyDescriptionCallback(url, node){
	this.url = url;
	this.node = node;
	// Define this fetcher when one is instantiated (circular dependency)
	this.fetcher = undefined;
	var self = this;
	
	this.callback = function (latestSubmissionData, textStatus, jqXHR){
		// textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
//		console.log("Begun OntologyDescriptionCallback");
//		var errorOrRetry = 	self.fetcher.retryFetch(metricDataRaw);
		var errorOrRetry = 	self.fetcher(latestSubmissionData);
		if(0 == errorOrRetry){
			return;
		} else if(-1 == errorOrRetry){
			// have an error. Done?
			return;
		}
		
		var description="";
	    if (typeof latestSubmissionData !== "undefined") {
	        if (latestSubmissionData.description != null) {
	            description = latestSubmissionData.description;
	        } else if(typeof latestSubmissionData.error != null){
	        	description = latestSubmissionData.error;
	        }
	    }
	    
		self.node.description = description;
		
		// console.log("ontologyDescriptionCallback");
//		updateDataForNodesAndLinks({nodes:[self.node], links:[]});
//		var ontologyUpdateMap = {};
//		updateDataForNodesAndLinks(ontologyUpdateMap);
	}
}


//function buildOntologyMappingUrl(centralOntologyVirtualId){
//	return "http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fvirtual%2Fmappings%2Fstats%2Fontologies%2F"+centralOntologyVirtualId+"&callback=?";
//}

function buildOntologyMappingUrlNewApi(centralOntologyAcronym){
	return "http://stagedata.bioontology.org/mappings/statistics/ontologies/"+centralOntologyAcronym+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?";
}

//function buildOntologyDetailsUrl(){
//	return "http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fontologies%2F"+"&callback=?";
//}

function buildOntologyDetailsUrlNewApi(){
	return "http://stagedata.bioontology.org/ontologies"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?";
}

//function buildOntologyMetricsUrl(ontologyVersionId){
//	return "http://bioportal.bioontology.org/ajax/jsonp?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&userapikey=&path=%2Fontologies%2Fmetrics%2F"+ontologyVersionId+"&callback=?";
//}

function buildOntologyMetricsUrlNewApi(ontologyAcronym){
	return "http://stagedata.bioontology.org/ontologies/"+ontologyAcronym+"/metrics"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?"
}

function buildOntologyLatestSubmissionUrlNewApi(ontologyAcronym){
	return "http://stagedata.bioontology.org/ontologies/"+ontologyAcronym+"/latest_submission"+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?"
}

//function RetryingJsonpFetcher(callbackObject){
//	this.callbackObject = callbackObject;
//	// Has circular dependency with the callback
//	this.callbackObject.fetcher = this;
//	this.previousRetriesMade = 0;
//	var self = this;
//
//		/*
//		 * Return values: -1 is non-retry due to error, 0 is retry, 1 is success, no error.
//		 */
//		RetryingJsonpFetcher.prototype.retryFetch = function(resultData){
//			// console.log("retryFetch for "+self.callbackObject.url);
//			if(typeof resultData === "undefined"){
//				// If not error, call for first time
//				jQuery.getJSON(self.callbackObject.url, null, self.callbackObject.callback);
//				return 0;
//			}
//			
//			if(typeof resultData.success === "undefined") {
//				if(resultData.status == "403" && resultData.body.indexOf("Forbidden") >= 0){
//					console.log("No retry, Forbidden Error: "+self.callbackObject.url);
//					console.log("No retry, Forbidden Error: "+resultData.body);
//		    		return -1;
//				} else if(resultData.status == "500" || resultData.status == "403"){
//		    		if(self.previousRetriesMade < 4){
//		    			self.previousRetriesMade++;
//		    			jQuery.getJSON(self.callbackObject.url, null, self.callbackObject.callback);
//		    			return 0;
//		    		} else {
//			    		// Error, but we are done retrying.
//			    		console.log("No retry, Error: "+resultData);
//			    		return -1;
//		    		}
//		    	} else {
//			    	// Don't retry for other errors
//		    		console.log("Error: "+self.callbackObject.url+" --> Data: "+resultData.status);
//			    	return -1;
//		    	}
//		    } else {
//		    	// Success, great!
//		    	return 1;
//		    }
//		}
//}

/*
* This fetcher system allows the success receiver to call it to see if there has been an error that
* allows for a retry. It is fairly clean on the user side, though it does require checking of
* return values.
* 
* Tried to implement as a class object and failed...see above this if you want to try again...
*/
function closureRetryingJsonpFetcher(callbackObject){
	var callbackObject = callbackObject;
	// Has circular dependency with the callback
	var previousRetriesMade = 0;

	/*
	 * Return values: -1 is non-retry due to error, 0 is retry, 1 is success, no error.
	 */
	callbackObject.fetcher = function(resultData){
			// console.log("retryFetch for "+callbackObject.url);
			if(typeof resultData === "undefined"){
				// If not error, call for first time
				jQuery.getJSON(callbackObject.url, null, callbackObject.callback);
				return 0;
			}
			
			if(typeof resultData.errors !== "undefined") {
				if(resultData.status == "404"){
					// 404 Error should fill in some popup data points, so let through...
					console.log("Error: "+callbackObject.url+" --> Data: "+resultData.error);
			    	return 1;
				} else if(resultData.status == "403" && resultData.error.indexOf("Forbidden") >= 0){
					console.log("Forbidden Error, no retry: "
							+"\nURL: "+callbackObject.url
							+"\nReply: "+resultData.error);
		    		return -1;
				} else if(resultData.status == "500" || resultData.status == "403"){
		    		if(previousRetriesMade < 4){
		    			previousRetriesMade++;
		    			console.log("Retrying: "+callbackObject.url);
		    			jQuery.getJSON(callbackObject.url, null, callbackObject.callback);
		    			return 0;
		    		} else {
			    		// Error, but we are done retrying.
			    		console.log("No retry, Error: "+resultData);
			    		return -1;
		    		}
		    	} else {
			    	// Don't retry for other errors
		    		console.log("Error: "+callbackObject.url+" --> Data: "+resultData.error);
			    	return -1;
		    	}
		    } else {
		    	// Success, great!
		    	return 1;
		    }
		}
	
	return callbackObject.fetcher;
}


//20 * 7 seems too big. Got 20 from other transformers.
var NODE_MAX_ON_SCREEN_SIZE = 20 * 5;
var NODE_MIN_ON_SCREEN_SIZE = 4;
var minNodeRawSize = -1;
var maxNodeRawSize = -1;
var LINK_MAX_ON_SCREEN_SIZE = 7; // 6 looks good...but if I change colors it may not.
var LINK_MIN_ON_SCREEN_SIZE = 1;
var minLinkRawSize = -1;
var maxLinkRawSize = -1;
var REFRESH_LOOP_DELAY_MS = 500;

//TODO Update for Cyto
function updateNodeScalingFactor(){
	// Call this prior to redrawing. The alternative is to track on every size
	// modification. That worked well for BioMixer, but perhaps we're better
	// off doing a bulk computation per size-refreshing redraw that we want to make.
//	var circles = vis.selectAll(".circle");
	var cy = $("#network-view").cytoscape("get"); // now we have a global reference to `cy`
	var circles = cy.nodes();
	
//	console.log("Rescaling from:"+minNodeRawSize+" and "+maxNodeRawSize);
	$.each(circles, function(index, node){
//				var basis = parseInt(this.getAttribute("data-radius_basis"));
				var basis = node.data('nodeDiameterBasis');
				if(-1 == maxNodeRawSize || basis > maxNodeRawSize){
					maxNodeRawSize = basis;
				}
				if(-1 == minNodeRawSize || basis < minNodeRawSize){
					minNodeRawSize = basis;
				}
		});
//	console.log("Rescaling to:"+minNodeRawSize+" and "+maxNodeRawSize);

	var newDiameters = {};
	
	$.each(circles, function(index, node){
		// TODO I think we need to avoid updating nodeDiameter elsewhere, and that we
		// only want to do it here, when we have properly updated the range.
		newDiameters[node.data('id')] = {'nodeDiameter': ontologyNodeScalingFunc(node.data('nodeDiameterBasis'))};
	});
	updateDataForNodesAndLinks(newDiameters, true);
	
	// Couldn't get the linear mapData approach to work at all, and the animation is incompatible with my approach.
//	circles.animate(
//			{css: {
//				width: 'data(nodeDiameter)', //'mapData(nodeDiameter, 30, 80, 20, 50)',
//				height: 'data(nodeDiameter)', //'mapData(nodeDiameter, 0, 200, 10, 45)',})
//				}},
//			{
//				duration: '700',
//				queue: 'true',
//			}
//		);
	
}

// TODO Update for Cyto
function updateLinkScalingFactor(){
//	// TODO This may not ever need to be called multiple times, but it would take some time to run.
//	// Make sure it actually needs to be run if it is indeed called. 
//	console.log("Ran update link");
//	// Call this prior to redrawing. The alternative is to track on every size
//	// modification. That worked well for BioMixer, but perhaps we're better
//	// off doing a bulk computation per size-refreshing redraw that we want to make.
	var cy = $("#network-view").cytoscape("get"); // now we have a global reference to `cy`
	var arcs = cy.edges();
	
//	console.log("Rescaling from:"+minNodeRawSize+" and "+maxNodeRawSize);
	$.each(arcs, function(index, link){
//			var basis = parseInt(link.attr("data-thickness_basis"));
			var basis = parseInt(link.data('linkThicknessBasis'));
			if(-1 == maxLinkRawSize || basis > maxLinkRawSize){
				maxLinkRawSize =  basis;
			}
			if(-1 == minLinkRawSize || basis < minLinkRawSize){
				minLinkRawSize =  basis;
			}
		});
	
	var newThicknesses = {};
	$.each(arcs, function(index, link){
		// TODO I think we need to avoid updating nodeDiameter elsewhere, and that we
		// only want to do it here, when we have properly updated the range.
		newThicknesses[link.data('id')] = {'linkThickness': ontologyLinkScalingFunc(link.data('linkThicknessBasis'))};
	});
	updateDataForNodesAndLinks(newThicknesses, true);
	
}


function ontologyNodeScalingFunc(rawValue){
	// return Math.sqrt((rawValue)/10);
	if(maxNodeRawSize == minNodeRawSize){
		return rawValue;
	}
	var factor = computeFactorOfRange(rawValue, minNodeRawSize, maxNodeRawSize);
    var diameter = linearAreaRelativeScaledRangeValue(factor, NODE_MIN_ON_SCREEN_SIZE, NODE_MAX_ON_SCREEN_SIZE);
    return diameter; // needed radius for D3 SVG, but diameter for Cytoscape.JS canvas.
}


function ontologyLinkScalingFunc(rawValue){
	if(maxLinkRawSize == minLinkRawSize){
		return rawValue;
	}
	var factor = computeFactorOfRange(rawValue, minLinkRawSize, maxLinkRawSize);
	// The linear area algorithm used for nodes happens to work really well for the edges thickness too.
    var thickness = linearAreaRelativeScaledRangeValue(factor, LINK_MIN_ON_SCREEN_SIZE, LINK_MAX_ON_SCREEN_SIZE);
    return thickness/2;
}

function computeRangeRawSize(minRawSize, maxRawSize) {
	return Math.max(1, maxRawSize - minRawSize);
}

function computeFactorOfRange(rawValue, minRawSize, maxRawSize) {
	return 1.0 - (maxRawSize - rawValue) / computeRangeRawSize(minRawSize, maxRawSize);
}

function linearAreaRelativeScaledRangeValue(factor, minOnScreenSize, maxOnScreenSize) {
	var linearArea = Math.PI * Math.pow(minOnScreenSize, 2) + factor
	      * Math.PI * Math.pow(maxOnScreenSize, 2);
	var diameter = Math.sqrt(linearArea / Math.PI);
	return diameter;
}

/*
    private double linearFunction(double value) {
        // Ha! A sqrt makes this not linear. Mis-named now...
        return 2 * (4 + Math.sqrt((value) / 10));
        return (1 + Math.sqrt((value)));
    }

    private double logFunction(double value) {
        return 4 + Math.log(value) * 10;
    }
 */

var currentNodeColor = -1;
//var nodeOrderedColors = d3.scale.category20().domain([0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19]);
// Hard coded, taken from D3 generated colors.
var nodeOrderedColors = [
"#1f77b4",
"#aec7e8",
"#ff7f0e",
"#ffbb78",
"#2ca02c",
"#98df8a",
"#d62728",
"#ff9896",
"#9467bd",
"#c5b0d5",
"#8c564b",
"#c49c94",
"#e377c2",
"#f7b6d2",
"#7f7f7f",
"#c7c7c7",
"#bcbd22",
"#dbdb8d",
"#17becf", 
"#9edae5", 
];
function nextNodeColor(){
	currentNodeColor = currentNodeColor == 19 ? 0 : currentNodeColor + 1;
	return nodeOrderedColors[currentNodeColor];
}
