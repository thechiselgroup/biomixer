///<reference path="headers/require.d.ts" />

///<amd-dependency path="Utils" />
///<amd-dependency path="FetchFromApi" />
///<amd-dependency path="Concepts/ConceptGraph" />

import Utils = require("../Utils");
import Fetcher = require("../FetchFromApi");
import ConceptGraph = require("./ConceptGraph");

// Responsible for storing and fetching relational properties of each ontology,
// for production of non-inheritance arcs. Each ontology can have its own defined
// relations, which show up on concept properties.
// Access must be unified, and due to asynchronous calls, access must queue up callers
// for when the data is available.
export class OntologyPropertyRelationsRegistry {
    
    // With the asynchronous callbacks, there is opportunity for multiple attempts to fetch
    // the ontology properties, from disparate sources. All of these sources must have their
    // own callbacks called once the ontology data has been parsed.
    // Thus, we maintain a queue of outstanding callbacks to take care of. Clear after calling.
    private static queuedCallbacks: { [ontologyUri: string]: Array<{()}>} = {};
    
    // For each ontology, if we are currently calling for the values, false; if received, true.
    private static ontologyQueries: { [ontologyUri: string]: OntologyRelationSet } = {};
 
    static addRelationsSet(relations: OntologyRelationSet){
        OntologyPropertyRelationsRegistry.ontologyQueries[String(relations.ontologyAcronym)] = relations;
    }
    
    public static contains(ontologyAcronym: ConceptGraph.RawAcronym): boolean {
        var entry = OntologyPropertyRelationsRegistry.ontologyQueries[String(ontologyAcronym)];
        return entry !== null && entry !== undefined;
    }
    
    /**
     * Is there a relation id registered for the ontology that corresponds with the provided property id
     * (and thus the provided id represents a property relation)?
     */
    public static matchedAvailableRelations(ontologyAcronym: ConceptGraph.RawAcronym, propertyId: string): OntologyRelation {
        var relationSet = OntologyPropertyRelationsRegistry.ontologyQueries[String(ontologyAcronym)];
        var escapedPropertyId =  Utils.escapeIdentifierForId(propertyId);
        return relationSet.relations[escapedPropertyId];
    }

            
    public static fetchOntologyPropertyRelations(conceptNode: ConceptGraph.Node, conceptWrappedCallback: {()}){
        // Be sure to prevent each concept call from making additional calls into this...perhaps we should put this call in the ConceptCompositionRelationsCallback,
        // and the registry can keep a queue of callbacks that need this satisfied prior to continuing...
        // thinking on it further...
        if(OntologyPropertyRelationsRegistry.contains(conceptNode.ontologyAcronym)){
            // We shouldn't really get here since caller should guard the same way, but it's valid and the correct response.
            conceptWrappedCallback();
            return;
        }
        
        // If undefined...if it is null, it means we have made the fetch call from a previous request, and it is
        // pending.
        if(OntologyPropertyRelationsRegistry.ontologyQueries[String(conceptNode.ontologyAcronym)] === undefined){
            // Null indicates in progress
            OntologyPropertyRelationsRegistry.ontologyQueries[String(conceptNode.ontologyAcronym)] = null;
            // Create the queue, since it doesn't exist until the first request for the ontology properties.
            OntologyPropertyRelationsRegistry.queuedCallbacks[String(conceptNode.ontologyAcronym)] = new Array<{()}>();
            // Fetch the data
            var ontologyPropertyRelationsUrl = this.buildOntologyPropertyRelationsUrl(conceptNode.ontologyAcronym);
            var ontologyPropertyRelationsCallback = new OntologyPropertyRelationsCallback(ontologyPropertyRelationsUrl, conceptNode);
            var fetcher = new Fetcher.RetryingJsonFetcher(ontologyPropertyRelationsUrl);
            fetcher.fetch(ontologyPropertyRelationsCallback);
        }
        
        // Regardless of whether this is the request that triggers the actual fetch, we add the callback to the queue.
        OntologyPropertyRelationsRegistry.queuedCallbacks[String(conceptNode.ontologyAcronym)].push(conceptWrappedCallback);

    }
    
    static dispatchCallbacks(ontologyAcronym: ConceptGraph.RawAcronym){
        var callbacks = OntologyPropertyRelationsRegistry.queuedCallbacks[String(ontologyAcronym)];
        for(var i = 0; i < callbacks.length; i++){
            callbacks[i]();
        }
        delete OntologyPropertyRelationsRegistry.queuedCallbacks[String(ontologyAcronym)];
    }
    
    private static buildOntologyPropertyRelationsUrl(ontologyAcronym: ConceptGraph.RawAcronym): string {
        return "http://data.bioontology.org/ontologies/"+ontologyAcronym+"/properties";
    }
    
}

export class OntologyRelation {
    public id: string;
    public idEscaped: string;
    public label: string;
    public definition: string[];
    public parents: string[];
    public ontologyAcronym: ConceptGraph.RawAcronym;
}

export class OntologyRelationSet {
    public ontologyAcronym: ConceptGraph.RawAcronym;
    public relations: { [relationIdEscaped: string]: OntologyRelation } = {};
    
    constructor(ontologyAcronym: ConceptGraph.RawAcronym){
        this.ontologyAcronym = ontologyAcronym;
    }
    
    addRelation(relation: OntologyRelation){
        this.relations[relation.idEscaped] = relation;
    }
}

class OntologyPropertyRelationsCallback extends Fetcher.CallbackObject {

    private ontologyAcronym: ConceptGraph.RawAcronym;

    constructor(
        public url: string,
        conceptNode: ConceptGraph.Node
        ){
            super(url, String(conceptNode.nodeId), Fetcher.CallbackVarieties.metaData);
            this.ontologyAcronym = conceptNode.ontologyAcronym;
        }
    
    public callback = (relationsDataRaw: any, textStatus: string, jqXHR: any) => {
        // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
        // CORS enabled GET and POST do though!
        if(jqXHR != null){
            if(relationsDataRaw.errors != null){
                // We had an error. Handle it.
                // Not much to do if this fails...
                return;
            }
        }
        
        
        var relationSet = new OntologyRelationSet(this.ontologyAcronym);
        
        for(var i = 0; i < relationsDataRaw.length; i++){
            var relationsEntry = relationsDataRaw[i];
            var relation = new OntologyRelation();
            if(relationsEntry.label.length > 1){
                // Multiple labels have a tendency to be nearly identical to each other,
                // based on my investigations.  
                //console.log("Found multiple labels:");
                //console.log(relationsEntry);
            }
            relation.id = relationsEntry.id;
            relation.idEscaped = Utils.escapeIdentifierForId(relation.id);
            relation.label = relationsEntry.label[0];
            relation.definition = relationsEntry.definition;
            relation.ontologyAcronym = this.ontologyAcronym;
            // relation.parents = relationsEntry["parents"];
            relationSet.addRelation(relation);
        }
        
        

        OntologyPropertyRelationsRegistry.addRelationsSet(relationSet);
        OntologyPropertyRelationsRegistry.dispatchCallbacks(this.ontologyAcronym);
        
    }

}