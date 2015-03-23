define(["require", "exports", './OntologyMappingOverview', "Ontologies/OntologyMappingOverview"], function (require, exports, Overview) {
    var softNodeCap = 19;
    var centralOntologyAcronym = purl().param("ontology_acronym");
    var graphView = new Overview.OntologyMappingOverview(centralOntologyAcronym, softNodeCap);
    graphView.initAndPopulateGraph();
});
