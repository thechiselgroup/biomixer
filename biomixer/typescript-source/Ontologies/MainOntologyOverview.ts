///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="Ontologies/OntologyMappingOverview" />

import Overview = require('./OntologyMappingOverview');

// Simplest way to include non-TypeScript defined libraries. Like usual, will need to
// be served to browser to function.
declare var purl;

// This is using the new API that is stable in September 2013.

// I eventually came across the post that sort of discusses our update problem, of
// having new attributes for nodes from late coming JSON:
// https://groups.google.com/forum/#!msg/d3-js/ENMlOyUGGjk/YiPc8AUKCOwJ
// http://grokbase.com/t/gg/d3-js/12cjmqc2cx/dynamically-updating-nodes-links-in-a-force-layout-diagram
// Bostock confirms that we shouldn't bind things that aren't truly new, and instead we must
// update element properties without binding.

// Some ontologies now have bad names with dots in them. May need to change out id matching with:
// '[id="node_g_'+centralOntologyAcronym+'"]'

// This cap only affects API dispatch and rendering for nodes past the cap. It is used during
// initialization only. Set to 0 means all nodes will be used.
var softNodeCap = 19; 


var centralOntologyAcronym = purl().param("ontology_acronym");

// Run the graph! Don't need the json really, though...
// d3.json("force_files/set_data.json", initAndPopulateGraph);
var graphView = new Overview.OntologyMappingOverview(centralOntologyAcronym, softNodeCap);
graphView.initAndPopulateGraph();