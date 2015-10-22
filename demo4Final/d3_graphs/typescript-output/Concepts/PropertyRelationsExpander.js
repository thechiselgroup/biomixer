///<amd-dependency path="Utils" />
///<amd-dependency path="FetchFromApi" />
///<amd-dependency path="Concepts/ConceptGraph" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../Utils", "../FetchFromApi", "Utils", "FetchFromApi", "Concepts/ConceptGraph"], function (require, exports, Utils, Fetcher) {
    // Responsible for storing and fetching relational properties of each ontology,
    // for production of non-inheritance arcs. Each ontology can have its own defined
    // relations, which show up on concept properties.
    // Access must be unified, and due to asynchronous calls, access must queue up callers
    // for when the data is available.
    var OntologyPropertyRelationsRegistry = (function () {
        function OntologyPropertyRelationsRegistry() {
        }
        OntologyPropertyRelationsRegistry.addRelationsSet = function (relations) {
            OntologyPropertyRelationsRegistry.ontologyQueries[String(relations.ontologyAcronym)] = relations;
        };
        OntologyPropertyRelationsRegistry.contains = function (ontologyAcronym) {
            var entry = OntologyPropertyRelationsRegistry.ontologyQueries[String(ontologyAcronym)];
            return entry !== null && entry !== undefined;
        };
        /**
         * Is there a relation id registered for the ontology that corresponds with the provided property id
         * (and thus the provided id represents a property relation)?
         */
        OntologyPropertyRelationsRegistry.matchedAvailableRelations = function (ontologyAcronym, propertyId) {
            var relationSet = OntologyPropertyRelationsRegistry.ontologyQueries[String(ontologyAcronym)];
            var escapedPropertyId = Utils.escapeIdentifierForId(propertyId);
            return relationSet.relations[escapedPropertyId];
        };
        OntologyPropertyRelationsRegistry.fetchOntologyPropertyRelations = function (conceptNode, conceptWrappedCallback) {
            // Be sure to prevent each concept call from making additional calls into this...perhaps we should put this call in the ConceptCompositionRelationsCallback,
            // and the registry can keep a queue of callbacks that need this satisfied prior to continuing...
            // thinking on it further...
            if (OntologyPropertyRelationsRegistry.contains(conceptNode.ontologyAcronym)) {
                // We shouldn't really get here since caller should guard the same way, but it's valid and the correct response.
                conceptWrappedCallback();
                return;
            }
            // If undefined...if it is null, it means we have made the fetch call from a previous request, and it is
            // pending.
            if (OntologyPropertyRelationsRegistry.ontologyQueries[String(conceptNode.ontologyAcronym)] === undefined) {
                // Null indicates in progress
                OntologyPropertyRelationsRegistry.ontologyQueries[String(conceptNode.ontologyAcronym)] = null;
                // Create the queue, since it doesn't exist until the first request for the ontology properties.
                OntologyPropertyRelationsRegistry.queuedCallbacks[String(conceptNode.ontologyAcronym)] = new Array();
                // Fetch the data
                var ontologyPropertyRelationsUrl = this.buildOntologyPropertyRelationsUrl(conceptNode.ontologyAcronym);
                var ontologyPropertyRelationsCallback = new OntologyPropertyRelationsCallback(ontologyPropertyRelationsUrl, conceptNode);
                var fetcher = new Fetcher.RetryingJsonFetcher(ontologyPropertyRelationsUrl);
                fetcher.fetch(ontologyPropertyRelationsCallback, false);
            }
            // Regardless of whether this is the request that triggers the actual fetch, we add the callback to the queue.
            OntologyPropertyRelationsRegistry.queuedCallbacks[String(conceptNode.ontologyAcronym)].push(conceptWrappedCallback);
        };
        OntologyPropertyRelationsRegistry.dispatchCallbacks = function (ontologyAcronym) {
            var callbacks = OntologyPropertyRelationsRegistry.queuedCallbacks[String(ontologyAcronym)];
            for (var i = 0; i < callbacks.length; i++) {
                callbacks[i]();
            }
            delete OntologyPropertyRelationsRegistry.queuedCallbacks[String(ontologyAcronym)];
        };
        OntologyPropertyRelationsRegistry.buildOntologyPropertyRelationsUrl = function (ontologyAcronym) {
            return "http://data.bioontology.org/ontologies/" + ontologyAcronym + "/properties";
        };
        // With the asynchronous callbacks, there is opportunity for multiple attempts to fetch
        // the ontology properties, from disparate sources. All of these sources must have their
        // own callbacks called once the ontology data has been parsed.
        // Thus, we maintain a queue of outstanding callbacks to take care of. Clear after calling.
        OntologyPropertyRelationsRegistry.queuedCallbacks = {};
        // For each ontology, if we are currently calling for the values, false; if received, true.
        OntologyPropertyRelationsRegistry.ontologyQueries = {};
        return OntologyPropertyRelationsRegistry;
    })();
    exports.OntologyPropertyRelationsRegistry = OntologyPropertyRelationsRegistry;
    var OntologyRelation = (function () {
        function OntologyRelation() {
        }
        return OntologyRelation;
    })();
    exports.OntologyRelation = OntologyRelation;
    var OntologyRelationSet = (function () {
        function OntologyRelationSet(ontologyAcronym) {
            this.relations = {};
            this.ontologyAcronym = ontologyAcronym;
        }
        OntologyRelationSet.prototype.addRelation = function (relation) {
            this.relations[relation.idEscaped] = relation;
        };
        return OntologyRelationSet;
    })();
    exports.OntologyRelationSet = OntologyRelationSet;
    var OntologyPropertyRelationsCallback = (function (_super) {
        __extends(OntologyPropertyRelationsCallback, _super);
        function OntologyPropertyRelationsCallback(url, conceptNode) {
            var _this = this;
            _super.call(this, url, String(conceptNode.nodeId), Fetcher.CallbackVarieties.metaData);
            this.url = url;
            this.callback = function (relationsDataRaw, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                // CORS enabled GET and POST do though!
                if (jqXHR != null) {
                    if (relationsDataRaw.errors != null) {
                        // We had an error. Handle it.
                        // Not much to do if this fails...
                        return;
                    }
                }
                var relationSet = new OntologyRelationSet(_this.ontologyAcronym);
                for (var i = 0; i < relationsDataRaw.length; i++) {
                    var relationsEntry = relationsDataRaw[i];
                    var relation = new OntologyRelation();
                    if (relationsEntry.label.length > 1) {
                    }
                    relation.id = relationsEntry.id;
                    relation.idEscaped = Utils.escapeIdentifierForId(relation.id);
                    relation.label = relationsEntry.label[0];
                    relation.definition = relationsEntry.definition;
                    relation.ontologyAcronym = _this.ontologyAcronym;
                    // relation.parents = relationsEntry["parents"];
                    relationSet.addRelation(relation);
                }
                OntologyPropertyRelationsRegistry.addRelationsSet(relationSet);
                OntologyPropertyRelationsRegistry.dispatchCallbacks(_this.ontologyAcronym);
            };
            this.ontologyAcronym = conceptNode.ontologyAcronym;
        }
        return OntologyPropertyRelationsCallback;
    })(Fetcher.CallbackObject);
});
