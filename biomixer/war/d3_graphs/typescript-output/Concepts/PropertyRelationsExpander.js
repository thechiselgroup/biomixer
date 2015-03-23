var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../Utils", "../FetchFromApi", "Utils", "FetchFromApi", "Concepts/ConceptGraph"], function (require, exports, Utils, Fetcher) {
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
        OntologyPropertyRelationsRegistry.matchedAvailableRelations = function (ontologyAcronym, propertyId) {
            var relationSet = OntologyPropertyRelationsRegistry.ontologyQueries[String(ontologyAcronym)];
            var escapedPropertyId = Utils.escapeIdentifierForId(propertyId);
            return relationSet.relations[escapedPropertyId];
        };
        OntologyPropertyRelationsRegistry.fetchOntologyPropertyRelations = function (conceptNode, conceptWrappedCallback) {
            if (OntologyPropertyRelationsRegistry.contains(conceptNode.ontologyAcronym)) {
                conceptWrappedCallback();
                return;
            }
            if (OntologyPropertyRelationsRegistry.ontologyQueries[String(conceptNode.ontologyAcronym)] === undefined) {
                OntologyPropertyRelationsRegistry.ontologyQueries[String(conceptNode.ontologyAcronym)] = null;
                OntologyPropertyRelationsRegistry.queuedCallbacks[String(conceptNode.ontologyAcronym)] = new Array();
                var ontologyPropertyRelationsUrl = this.buildOntologyPropertyRelationsUrl(conceptNode.ontologyAcronym);
                var ontologyPropertyRelationsCallback = new OntologyPropertyRelationsCallback(ontologyPropertyRelationsUrl, conceptNode);
                var fetcher = new Fetcher.RetryingJsonFetcher(ontologyPropertyRelationsUrl);
                fetcher.fetch(ontologyPropertyRelationsCallback, false);
            }
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
        OntologyPropertyRelationsRegistry.queuedCallbacks = {};
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
                if (jqXHR != null) {
                    if (relationsDataRaw.errors != null) {
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
