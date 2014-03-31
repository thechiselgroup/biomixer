///<reference path="headers/require.d.ts" />

///<amd-dependency path="Concepts/ConceptGraph" />

import ConceptGraph = require('./ConceptGraph');

export class ExpansionManager{
    
    // To track nodes for which we want their neighbours expanded (by id and expansion type):
    private conceptsToExpand = {};
    
       // Maps conceptIds not present in the graph to concept ids in the graph for which an edge exists.:
    // To track edges that we know about that haven't yet been added to the graph (by node not yet in graph and node in graph and type):
    private edgeRegistry = {};

    constructor(
        
    ){
        
    }
    
    whiteListConcept(conceptUri, expansionType){
        return conceptUri in this.conceptsToExpand && expansionType in this.conceptsToExpand[conceptUri];
    }
    
    addConceptIdToExpansionRegistry(centralConceptUri, expansionType){
        // Weakly typed maps are much more pleasant than string and number indexed maps when using objects
        this.conceptsToExpand[centralConceptUri] = {};
        this.conceptsToExpand[centralConceptUri][expansionType] = true;
    }
    
    addEdgeToRegistry(conceptIdNotInGraph, conceptInGraph, edge: ConceptGraph.Link){
        // Weakly typed maps are much more pleasant than string and number indexed maps when using objects
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
        // Weakly typed maps are much more pleasant than string and number indexed maps when using objects
        delete this.edgeRegistry[matchIdInRegistry][otherIdInGraph][edge.relationType];
        if(Object.keys(this.edgeRegistry[matchIdInRegistry][otherIdInGraph]).length == 0){
            delete this.edgeRegistry[matchIdInRegistry][otherIdInGraph];
        }
        if(Object.keys(this.edgeRegistry[matchIdInRegistry]).length == 0){
            delete this.edgeRegistry[matchIdInRegistry];
        }
    }
    
}