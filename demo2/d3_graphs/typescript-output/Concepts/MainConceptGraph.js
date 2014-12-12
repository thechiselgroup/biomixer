///<reference path="headers/require.d.ts" />
define(["require", "exports", './ConceptPathsToRoot', './ConceptGraph', "Concepts/ConceptPathsToRoot", "Concepts/ConceptGraph"], function(require, exports, PathsToRoot, ConceptGraph) {
    

    var centralOntologyAcronym = purl().param("ontology_acronym");
    var centralConceptUri = purl().param("full_concept_id");
    var initialVis = purl().param("initial_vis");
    var softNodeCap = 20;

    $("#visualization_selector").append('<option value="paths_to_root">' + ConceptGraph.PathOptionConstants.pathsToRootConstant + '</option>').append('<option value="term_neighborhood">' + ConceptGraph.PathOptionConstants.termNeighborhoodConstant + '</option>').append('<option value="mappings_neighborhood">' + ConceptGraph.PathOptionConstants.mappingsNeighborhoodConstant + '</option>');

    $("#visualization_selector option").each(function () {
        // Note we use the values not text, to facilitate URL parameters without having to encode spaces.
        if ($(this).val() == initialVis) {
            $(this).attr("selected", "selected");
        }
    });

    // Run the graph! Don't need the json really, though...
    // d3.json("force_files/set_data.json", initAndPopulateGraph);
    var graphView = new PathsToRoot.ConceptPathsToRoot(centralOntologyAcronym, centralConceptUri, softNodeCap);
    graphView.initAndPopulateGraph();
});
