///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="Concepts/ConceptPathsToRoot" />
///<amd-dependency path="Concepts/ConceptGraph" />

import PathsToRoot = require('./ConceptPathsToRoot');
import ConceptGraph = require('./ConceptGraph');

// Simplest way to include non-TypeScript defined libraries. Like usual, will need to
// be served to browser to function.
declare var purl;

var centralOntologyAcronym: ConceptGraph.RawAcronym = purl().param("ontology_acronym");
var centralConceptUri: ConceptGraph.ConceptURI = purl().param("full_concept_id");
var initialVis = purl().param("initial_vis");
var softNodeCap = 20;

$("#visualization_selector")
.append('<option value="paths_to_root">path to root</option>')
.append('<option value="term_neighborhood">term neighborhood</option>')
.append('<option value="mappings_neighborhood">mappings neighborhood</option>')
;

$("#visualization_selector option").each(
        function(){
            // Note we use the values not text, to facilitate URL parameters without having to encode spaces.
            if($(this).val() == initialVis){
                $(this).attr("selected", "selected");
            }
        }
);

// Run the graph! Don't need the json really, though...
// d3.json("force_files/set_data.json", initAndPopulateGraph);
var graphView = new PathsToRoot.ConceptPathsToRoot(centralOntologyAcronym, centralConceptUri, softNodeCap);
graphView.initAndPopulateGraph();