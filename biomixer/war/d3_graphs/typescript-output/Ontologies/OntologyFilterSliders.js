define(["require", "exports", "../Utils", "../GraphView", "Utils", "GraphView", "Ontologies/OntologyMappingOverview", "Ontologies/OntologyGraph"], function (require, exports, Utils, GraphView) {
    var MappingRangeSliders = (function () {
        function MappingRangeSliders(graph, graphView, centralOntologyAcronym) {
            var _this = this;
            this.graph = graph;
            this.graphView = graphView;
            this.centralOntologyAcronym = centralOntologyAcronym;
            this.sortedLinksByMapping = [];
            this.rangeSliderSlideEvent = function (event, ui) {
                var bottom = $("#top-mappings-slider-range").slider("values", 0) + 1;
                var top = $("#top-mappings-slider-range").slider("values", 1) + 1;
                $("#top-mappings-slider-amount").text("Top " + bottom + " - " + top);
                _this.filterGraphOnMappingCounts();
            };
        }
        MappingRangeSliders.prototype.addMenuComponents = function (menuSelector, softNodeCap) {
            var minSliderAbsolute = 0;
            var maxSliderAbsolute = 0 == softNodeCap ? this.sortedLinksByMapping.length : softNodeCap;
            $(menuSelector).append($("<label>").attr("for", "top-mappings-slider-amount").text("Ranked Mapping Range: "));
            $(menuSelector).append($("<label>").attr("type", "text").attr("id", "top-mappings-slider-amount"));
            $(menuSelector).append($("<div>").attr("id", "top-mappings-slider-range"));
            $("#top-mappings-slider-range").slider({
                range: true,
                min: minSliderAbsolute,
                max: maxSliderAbsolute,
                values: [minSliderAbsolute, maxSliderAbsolute],
                slide: this.rangeSliderSlideEvent,
                change: this.rangeSliderSlideEvent
            });
            this.updateTopMappingsSliderRange();
            $("#top-mappings-slider-amount").text("Top " + minSliderAbsolute + " - " + maxSliderAbsolute);
        };
        MappingRangeSliders.prototype.changeTopMappingSliderValues = function (bottom, top) {
            console.log("Programatically changing node filter cutoff at " + Utils.getTime());
            if (null == bottom) {
                bottom = $("#top-mappings-slider-range").slider('values', 0);
            }
            else if (bottom > 0) {
                bottom = bottom - 1;
            }
            if (null == top) {
                top = $("#top-mappings-slider-range").slider('values', 1);
            }
            else if (top > 0) {
                top = top - 1;
            }
            $("#top-mappings-slider-range").slider('values', [bottom, top]);
        };
        MappingRangeSliders.prototype.updateTopMappingsSliderRange = function () {
            this.sortedLinksByMapping = [];
            var i = 0;
            var outerThis = this;
            d3.selectAll(GraphView.BaseGraphView.linkSvgClass).each(function (d, i) {
                outerThis.sortedLinksByMapping[i] = d;
            });
            this.sortedLinksByMapping.sort(function (a, b) {
                return b.value - a.value;
            });
            var mappingMin = 1;
            var mappingMax = this.sortedLinksByMapping.length;
            $("#top-mappings-slider-range").slider("option", "min", 0);
            $("#top-mappings-slider-range").slider("option", "max", this.sortedLinksByMapping.length - 1);
            $("#top-mappings-slider-amount").text("Top " + mappingMin + " - " + mappingMax);
        };
        MappingRangeSliders.prototype.filterGraphDeprecated = function () {
            alert("Deprecated function called");
        };
        MappingRangeSliders.prototype.filterGraphOnMappingCounts = function () {
            var minNode = this.sortedLinksByMapping[$("#top-mappings-slider-range").slider("values", 1)];
            var maxNode = this.sortedLinksByMapping[$("#top-mappings-slider-range").slider("values", 0)];
            if (undefined === minNode || undefined === maxNode) {
                return;
            }
            var minNodeAbsolute = minNode.value;
            var maxNodeAbsolute = maxNode.value;
            var minArcAbsolute = minNodeAbsolute;
            var maxArcAbsolute = maxNodeAbsolute;
            var topIndex = $("#top-mappings-slider-range").slider("values", 1);
            var bottomIndex = $("#top-mappings-slider-range").slider("values", 0);
            var outerThis = this;
            $.each(this.sortedLinksByMapping, function (i, d) {
                var hideArc = !(bottomIndex <= i && i <= topIndex);
                var hideSourceNodeBecauseOfHiddenArc = hideArc;
                var hideTargetNodeBecauseOfHiddenArc = hideArc;
                if (d.source.rawAcronym == outerThis.centralOntologyAcronym) {
                    hideSourceNodeBecauseOfHiddenArc = false;
                }
                if (d.target.rawAcronym == outerThis.centralOntologyAcronym) {
                    hideTargetNodeBecauseOfHiddenArc = false;
                }
                if (!hideArc) {
                }
                $("#link_line_" + d.source.acronymForIds + "-to-" + d.target.acronymForIds).css("display", (hideArc) ? "none" : "");
                $("#node_g_" + d.source.acronymForIds).find("*").css("display", (hideSourceNodeBecauseOfHiddenArc) ? "none" : "");
                $("#node_g_" + d.target.acronymForIds).find("*").css("display", (hideTargetNodeBecauseOfHiddenArc) ? "none" : "");
                if (!hideSourceNodeBecauseOfHiddenArc) {
                    outerThis.graph.fetchNodeRestData(d.source);
                }
                if (!hideTargetNodeBecauseOfHiddenArc) {
                    outerThis.graph.fetchNodeRestData(d.target);
                }
            });
            $(GraphView.BaseGraphView.nodeLabelSvgClass).attr("x", function () {
                return -this.getComputedTextLength() / 2;
            });
            this.graphView.stampTimeGraphModified();
            this.graphView.runCurrentLayout();
        };
        return MappingRangeSliders;
    })();
    exports.MappingRangeSliders = MappingRangeSliders;
});
