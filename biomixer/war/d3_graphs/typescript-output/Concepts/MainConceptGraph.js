///<amd-dependency path="Concepts/ConceptPathsToRoot" />
///<amd-dependency path="Concepts/ConceptGraph" />
///<amd-dependency path="MouseSpinner" />
///<amd-dependency path="FetchFromApi" />
define(["require", "exports", './ConceptPathsToRoot', './ConceptGraph', '../MouseSpinner', '../FetchFromApi', "Concepts/ConceptPathsToRoot", "Concepts/ConceptGraph", "MouseSpinner", "FetchFromApi"], function (require, exports, PathsToRoot, ConceptGraph, MouseSpinner, FetchFromApi) {
    MouseSpinner.MouseSpinner.applyMouseSpinner("ConceptMain");
    var centralOntologyAcronym = purl().param("ontology_acronym");
    var centralConceptSimpleUri = purl().param("full_concept_id");
    var userapikey = purl().param("userapikey");
    FetchFromApi.RetryingJsonFetcher.userapikey = userapikey;
    var initialVis = purl().param("initial_vis");
    var softNodeCap = 20;
    $(PathsToRoot.ConceptPathsToRoot.VIZ_SELECTOR_ID)
        .append('<option value="paths_to_root">' + ConceptGraph.PathOptionConstants.pathsToRootConstant + '</option>') //Path to Root
        .append('<option value="term_neighborhood">' + ConceptGraph.PathOptionConstants.termNeighborhoodConstant + '</option>') //Term Neighborhood
        .append('<option value="mappings_neighborhood">' + ConceptGraph.PathOptionConstants.mappingsNeighborhoodConstant + '</option>') //Mappings Neighborhood
    ;
    $("#visualization_selector option").each(function () {
        // Convert old version Choosel values to the ones we have above, in case old URL is
        // used to access this visualization.
        if (initialVis === "paths_to_root") {
            initialVis = "paths_to_root";
        }
        else if (initialVis === "mapping_neighbourhood") {
            initialVis = "mappings_neighborhood";
        }
        else if (initialVis === "concept_neighbourhood") {
            initialVis = "term_neighbourhood";
        }
        // Note we use the values not text, to facilitate URL parameters without having to encode spaces.
        if ($(this).val() == initialVis) {
            $(this).attr("selected", "selected");
        }
    });
    // Run the graph! Don't need the json really, though...
    // d3.json("force_files/set_data.json", initAndPopulateGraph);
    var graphView = new PathsToRoot.ConceptPathsToRoot(centralOntologyAcronym, centralConceptSimpleUri, softNodeCap);
    graphView.initAndPopulateGraph();
});
