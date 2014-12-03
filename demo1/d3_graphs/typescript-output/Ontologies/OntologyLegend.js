///<reference path="headers/require.d.ts" />
define(["require", "exports", "Menu"], function(require, exports) {
    var OntologyLegend = (function () {
        function OntologyLegend(menu) {
            this.menu = menu;
        }
        OntologyLegend.prototype.initialize = function () {
            var legend = $("<div>").attr("id", "legend").attr("class", "legend");
            $(this.menu.getMenuBarSelector()).append(legend);

            legend.append($("<p>").text("Outer circle represents the number of concepts in that ontology."));
            legend.append($("<p>").text("Inner circle represents concepts mapped to the central ontology."));
        };
        return OntologyLegend;
    })();
    exports.OntologyLegend = OntologyLegend;
});
