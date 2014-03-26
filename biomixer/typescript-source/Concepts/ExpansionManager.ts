///<reference path="headers/require.d.ts" />

///<amd-dependency path="ConceptGraph" />

import ConceptGraph = require('ConceptGraph');

export class ExpansionManager{

    constructor(
        
    ){
        
    }
    
    // To track nodes for which we want their neighbours expanded (by id and expansion type):
    conceptsToExpand = undefined;
    addConceptIdToExpansionRegistry(centralConceptUri, expansionType){
        this.conceptsToExpand[centralConceptUri] = {};
        this.conceptsToExpand[centralConceptUri][expansionType] = true;
    }
    
    // Maps conceptIds not present in the graph to concept ids in the graph for which an edge exists.:
    // To track edges that we know about that haven't yet been added to the graph (by node not yet in graph and node in graph and type):
    edgeRegistry = undefined;
    addEdgeToRegistry(conceptIdNotInGraph, conceptInGraph, edge: ConceptGraph.Link){
        if(!(conceptIdNotInGraph in this.edgeRegistry)){
            this.edgeRegistry[conceptIdNotInGraph] = {};
        }
        if(!(conceptInGraph in this.edgeRegistry[conceptIdNotInGraph])){
            this.edgeRegistry[conceptIdNotInGraph][conceptInGraph] = {};
        }
        // Need type as an index as well because some ontologies could have multiple edge types between entities.
        this.edgeRegistry[conceptIdNotInGraph][conceptInGraph][edge.relationType] = edge;
    }
    
    clearEdgeFromRegistry(matchIdInRegistry, otherIdInGraph, edge: ConceptGraph.Link){
        delete this.edgeRegistry[matchIdInRegistry][otherIdInGraph][edge.relationType];
        if(Object.keys(this.edgeRegistry[matchIdInRegistry][otherIdInGraph]).length == 0){
            delete this.edgeRegistry[matchIdInRegistry][otherIdInGraph];
        }
        if(Object.keys(this.edgeRegistry[matchIdInRegistry]).length == 0){
            delete this.edgeRegistry[matchIdInRegistry];
        }
    }
    
}