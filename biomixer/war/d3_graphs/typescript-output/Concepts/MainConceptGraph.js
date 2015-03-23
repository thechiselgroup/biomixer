define(["require", "exports", './ConceptPathsToRoot', './ConceptGraph', '../MouseSpinner', "Concepts/ConceptPathsToRoot", "Concepts/ConceptGraph", "MouseSpinner"], function (require, exports, PathsToRoot, ConceptGraph, MouseSpinner) {
    MouseSpinner.MouseSpinner.applyMouseSpinner("ConceptMain");
    var centralOntologyAcronym = purl().param("ontology_acronym");
    var centralConceptSimpleUri = purl().param("full_concept_id");
    var initialVis = purl().param("initial_vis");
    var softNodeCap = 20;
    $(PathsToRoot.ConceptPathsToRoot.VIZ_SELECTOR_ID).append('<option value="paths_to_root">' + ConceptGraph.PathOptionConstants.pathsToRootConstant + '</option>').append('<option value="term_neighborhood">' + ConceptGraph.PathOptionConstants.termNeighborhoodConstant + '</option>').append('<option value="mappings_neighborhood">' + ConceptGraph.PathOptionConstants.mappingsNeighborhoodConstant + '</option>');
    $("#visualization_selector option").each(function () {
        if (initialVis === "paths_to_root") {
            initialVis = "paths_to_root";
        }
        else if (initialVis === "mapping_neighbourhood") {
            initialVis = "mappings_neighborhood";
        }
        else if (initialVis === "concept_neighbourhood") {
            initialVis = "term_neighbourhood";
        }
        if ($(this).val() == initialVis) {
            $(this).attr("selected", "selected");
        }
    });
    var graphView = new PathsToRoot.ConceptPathsToRoot(centralOntologyAcronym, centralConceptSimpleUri, softNodeCap);
    graphView.initAndPopulateGraph();
});
