var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../FilterWidget", "../GraphView", "./ConceptGraph", "../Utils", "../FilterWidget", "../GraphView", "Concepts/ConceptPathsToRoot", "Concepts/ConceptGraph", "Concepts/PropertyRelationsExpander"], function (require, exports, FilterWidget, GraphView, ConceptGraph) {
    var ConceptEdgeTypeFilter = (function (_super) {
        __extends(ConceptEdgeTypeFilter, _super);
        function ConceptEdgeTypeFilter(conceptGraph, graphView, centralConceptUri) {
            _super.call(this, ConceptEdgeTypeFilter.SUB_MENU_TITLE);
            this.conceptGraph = conceptGraph;
            this.graphView = graphView;
            this.centralConceptUri = centralConceptUri;
        }
        ConceptEdgeTypeFilter.prototype.updateFilterUI = function () {
            var _this = this;
            var checkboxSpanClass = this.getCheckboxSpanClass();
            var preExistingCheckboxes = $("." + checkboxSpanClass);
            var checkboxesPopulatedOrReUsed = $("");
            var outerThis = this;
            var linkTypes = {};
            var linkTypeToOntology = {};
            d3.selectAll("." + GraphView.BaseGraphView.linkSvgClassSansDot).each(function (d) {
                linkTypes[d.relationType] = d.relationLabel;
                linkTypeToOntology[d.relationType] = d.relationSpecificToOntologyAcronym;
            });
            if (0 === $("#" + "span_color_instructions").length) {
                var inst = $("<span>").attr("id", "span_color_instructions").text("(Click sample arc for color change)").addClass("unselectable").css("font-style", "italic");
                this.filterContainer.append(inst);
            }
            $.each(linkTypes, function (linkTypeName, linkTypeLabel) {
                var checkId = _this.computeCheckId(linkTypeName);
                var spanId = "span_" + checkId;
                if (0 === $("#" + spanId).length) {
                    var checkboxLabel = _this.generateCheckboxLabel(linkTypeLabel);
                    var checkboxColoredSquare = _this.generateColoredSquareIndicator(linkTypeLabel, linkTypeToOntology[linkTypeName]);
                    var checkboxSampleArc = _this.generateSampleArcIndicator(linkTypeName, linkTypeToOntology[linkTypeName]);
                    _this.filterContainer.append($("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox").append($("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "").change(function () {
                        var linkHideCandidates = outerThis.computeCheckboxElementDomain(linkTypeName);
                        outerThis.checkboxChanged(linkHideCandidates, $(this));
                    })).append(checkboxSampleArc).append($("<label>").attr("for", checkId).append("&nbsp;" + checkboxLabel)));
                }
                checkboxesPopulatedOrReUsed = checkboxesPopulatedOrReUsed.add("#" + spanId);
            });
            $("#" + "span_" + this.computeCheckId(this.conceptGraph.relationLabelConstants.mapping)).prependTo(this.filterContainer);
            $("#" + "span_" + this.computeCheckId(this.conceptGraph.relationLabelConstants.composition)).prependTo(this.filterContainer);
            $("#" + "span_" + this.computeCheckId(this.conceptGraph.relationLabelConstants.inheritance)).prependTo(this.filterContainer);
            $("#" + "span_color_instructions").prependTo(this.filterContainer);
            preExistingCheckboxes.not(checkboxesPopulatedOrReUsed).remove();
        };
        ConceptEdgeTypeFilter.prototype.generateCheckboxLabel = function (linkTypeLabel) {
            return "\"" + linkTypeLabel + "\"";
        };
        ConceptEdgeTypeFilter.prototype.generateColoredSquareIndicator = function (linkTypeLabel, ontologyAcronym) {
            if (undefined !== this.conceptGraph.relationTypeCssClasses[linkTypeLabel]) {
                return "<span style='font-size: large;' class='" + this.conceptGraph.relationTypeCssClasses[linkTypeLabel] + "'>\u25A0</span>";
            }
            else {
                return "<span style='font-size: large; color: " + this.conceptGraph.nextNodeColor(ontologyAcronym) + ";'>\u25A0</span>";
            }
        };
        ConceptEdgeTypeFilter.prototype.generateSampleArcIndicator = function (relationType, relationSpecificToOntologyAcronym) {
            var _this = this;
            var linkContainer = document.createElementNS(d3.ns.prefix.svg, 'svg');
            var markerCompensation = relationType === this.conceptGraph.relationLabelConstants.inheritance ? 15 : 0;
            var finalTargetXCoordinate = 50;
            var initialTargetXCoordinate = finalTargetXCoordinate + markerCompensation;
            var arcHeight = 7;
            var linkData = new ConceptGraph.Link();
            linkData.relationType = relationType;
            linkData.source = { x: 0, y: arcHeight };
            linkData.target = { x: initialTargetXCoordinate, y: arcHeight };
            linkData.relationSpecificToOntologyAcronym = relationSpecificToOntologyAcronym;
            var outerThis = this;
            d3.select(linkContainer).attr("width", finalTargetXCoordinate).attr("height", 2 * arcHeight).attr("class", function () {
                return ConceptEdgeTypeFilter.sampleEdgeClassSansDot + " " + GraphView.BaseGraphView.linkClassSelectorPrefix + relationType + " " + _this.graphView.getLinkCssClass(relationType, relationSpecificToOntologyAcronym) + " " + "unselectable";
            }).attr("id", function () {
                return "filter_link_g_" + relationType;
            });
            d3.select(linkContainer).append("svg:polyline").attr("class", function () {
                return ConceptEdgeTypeFilter.sampleMarkerClassSansDot + " " + GraphView.BaseGraphView.linkClassSelectorPrefix + relationType + " " + _this.graphView.getLinkCssClass(relationType, relationSpecificToOntologyAcronym);
            }).attr("id", function (d) {
                return "filter_link_marker_" + relationType;
            }).attr("points", this.graphView.updateArcMarkerFunc(linkData, true));
            linkData.target.x = finalTargetXCoordinate;
            d3.select(linkContainer).append("svg:polyline").attr("class", function () {
                return ConceptEdgeTypeFilter.sampleEdgeClassSansDot + " " + GraphView.BaseGraphView.linkClassSelectorPrefix + relationType + " " + _this.graphView.getLinkCssClass(relationType, relationSpecificToOntologyAcronym);
            }).attr("id", function () {
                return "filter_link_line_" + relationType;
            }).attr("points", this.graphView.updateArcLineFunc(linkData, true)).append("title").text("Click to select color");
            d3.select(linkContainer).each(function (d, i) {
                var spectrumOwner = $(this);
                var proxyId = "filter_link_g_color_box_proxy_" + relationType;
                var exists = false;
                d3.select(this).append("svg:rect").attr("id", proxyId).attr("width", finalTargetXCoordinate).attr("height", 2 * arcHeight).style("fill-opacity", "0.0").style("stroke-opacity", "0.0").each(function () {
                    var spectrumActivator = $(this);
                    spectrumActivator.on("click", function (e) {
                        var sampleArc = $("#filter_link_marker_" + relationType);
                        if (exists) {
                            return;
                        }
                        exists = true;
                        spectrumOwner.spectrum({
                            color: $.stylesheet("." + outerThis.graphView.getLinkCssClass(relationType, relationSpecificToOntologyAcronym)).css("fill"),
                            change: function () {
                                outerThis.updateArcColor(spectrumOwner, sampleArc, relationType, relationSpecificToOntologyAcronym);
                            },
                            beforeShow: function () {
                            },
                            show: function () {
                            },
                            hide: function () {
                                outerThis.updateArcColor(spectrumOwner, sampleArc, relationType, relationSpecificToOntologyAcronym);
                                spectrumOwner.spectrum("destroy");
                                exists = false;
                            },
                            showInitial: true,
                            showPalette: true,
                            palette: [
                                ["#000", "#444", "#666", "#999", "#ccc", "#eee", "#f3f3f3", "#fff"],
                                ["#f00", "#f90", "#ff0", "#0f0", "#0ff", "#00f", "#90f", "#f0f"],
                                ["#ea9999", "#f9cb9c", "#ffe599", "#b6d7a8", "#a2c4c9", "#9fc5e8", "#b4a7d6", "#d5a6bd"],
                                ["#e06666", "#f6b26b", "#ffd966", "#93c47d", "#76a5af", "#6fa8dc", "#8e7cc3", "#c27ba0"],
                                ["#c00", "#e69138", "#f1c232", "#6aa84f", "#45818e", "#3d85c6", "#674ea7", "#a64d79"],
                                ["#900", "#b45f06", "#bf9000", "#38761d", "#134f5c", "#0b5394", "#351c75", "#741b47"],
                                ["#600", "#783f04", "#7f6000", "#274e13", "#0c343d", "#073763", "#20124d", "#4c1130"]
                            ]
                        });
                        spectrumOwner.spectrum("show");
                        e.stopPropagation();
                        return false;
                    });
                });
            });
            return linkContainer;
        };
        ConceptEdgeTypeFilter.prototype.updateArcColor = function (spectrumHolder, elemForColor, relationType, relationSpecificToOntologyAcronym) {
            var newColor = spectrumHolder.spectrum("get").toHexString();
            var sheet = $.stylesheet("." + this.graphView.getLinkCssClass(relationType, relationSpecificToOntologyAcronym));
            sheet.css("stroke", newColor);
            sheet.css("fill", newColor);
            sheet.css("color ", newColor);
        };
        ConceptEdgeTypeFilter.prototype.computeCheckId = function (linkName) {
            return this.getClassName() + "_for_" + linkName;
        };
        ConceptEdgeTypeFilter.prototype.computeCheckboxElementDomain = function (linkTypeName) {
            return d3.selectAll("." + GraphView.BaseGraphView.linkClassSelectorPrefix + linkTypeName + ":not(." + ConceptEdgeTypeFilter.sampleEdgeClassSansDot + ")" + ":not(." + ConceptEdgeTypeFilter.sampleMarkerClassSansDot + ")");
        };
        ConceptEdgeTypeFilter.prototype.checkboxChanged = function (setOfHideCandidates, checkbox) {
            if (checkbox.is(':checked')) {
                this.graphView.unhideLinks(setOfHideCandidates);
            }
            else {
                this.graphView.hideLinks(setOfHideCandidates);
            }
        };
        ConceptEdgeTypeFilter.SUB_MENU_TITLE = "Edge Types Displayed";
        ConceptEdgeTypeFilter.sampleEdgeClassSansDot = "filter_link_sample";
        ConceptEdgeTypeFilter.sampleMarkerClassSansDot = "filter_link_sample_marker";
        return ConceptEdgeTypeFilter;
    })(FilterWidget.AbstractFilterWidget);
    exports.ConceptEdgeTypeFilter = ConceptEdgeTypeFilter;
});
