///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="../Utils" />
///<amd-dependency path="../FilterWidget" />
///<amd-dependency path="../GraphView" />
///<amd-dependency path="Concepts/ConceptGraph" />

import Utils = require("../Utils");
import FilterWidget = require("../FilterWidget");
import GraphView = require("./ConceptPathsToRoot");
import ConceptGraph = require("./ConceptGraph");
    
export class OntologyConceptFilter extends FilterWidget.FilterWidget {

    constructor(
        private graph: ConceptGraph.ConceptGraph,
        private graphView: GraphView.ConceptPathsToRoot,
        private centralConceptUri: ConceptGraph.ConceptURI
        ){
        super();
    }

    
    addMenuComponents(menuSelector: string){
    }
    
    updateFilterUI(){
    }
    
    rangeSliderSlideEvent = (event, ui)=>{
    };
    
}