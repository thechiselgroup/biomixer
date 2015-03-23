var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../Utils", "./ConceptNodeFilterWidget", "./ExpansionSetFilter", "./CherryPickConceptFilter", "../Menu", "../Utils", "../NodeFilterWidget", "./ConceptNodeFilterWidget", "./CherryPickConceptFilter", "./ConceptPathsToRoot", "./ConceptGraph", "../ExpansionSets", "../Menu"], function (require, exports, Utils, ConceptFilterWidget, ExpansionSetFilter, ConceptFilter, Menu) {
    var NestedExpansionSetConceptFilter = (function (_super) {
        __extends(NestedExpansionSetConceptFilter, _super);
        function NestedExpansionSetConceptFilter(conceptGraph, graphView, centralConceptUri) {
            _super.call(this, NestedExpansionSetConceptFilter.SUB_MENU_TITLE, graphView, conceptGraph);
            this.centralConceptUri = centralConceptUri;
            this.implementation = this;
            this.pathToRootView = graphView;
            this.expansionsFilter = new ExpansionSetFilter.ExpansionSetFilter(conceptGraph, graphView);
            this.conceptFilter = new ConceptFilter.CherryPickConceptFilter(conceptGraph, graphView, centralConceptUri);
            this.expansionsFilter.modifyClassName(NestedExpansionSetConceptFilter.NESTED_FILTER_CLASSNAME_PREFIX + NestedExpansionSetConceptFilter.NESTED_FILTER_CLASSNAME_PARENT_SUFFIX);
            this.conceptFilter.modifyClassName(NestedExpansionSetConceptFilter.NESTED_FILTER_CLASSNAME_PREFIX + NestedExpansionSetConceptFilter.NESTED_FILTER_CLASSNAME_CHILD_SUFFIX);
        }
        NestedExpansionSetConceptFilter.prototype.generateCheckboxLabel = function (arg) {
            return (Utils.getClassName(arg) === "ExpansionSet") ? this.expansionsFilter.generateCheckboxLabel(arg) : this.conceptFilter.generateCheckboxLabel(arg);
        };
        NestedExpansionSetConceptFilter.prototype.generateColoredSquareIndicator = function (arg) {
            return (Utils.getClassName(arg) === "ExpansionSet") ? this.expansionsFilter.generateColoredSquareIndicator(arg) : this.conceptFilter.generateColoredSquareIndicator(arg);
        };
        NestedExpansionSetConceptFilter.prototype.computeCheckId = function (arg) {
            return ((Utils.getClassName(arg) === "ExpansionSet") ? this.expansionsFilter.computeCheckId(arg) : this.conceptFilter.computeCheckId(arg));
        };
        NestedExpansionSetConceptFilter.prototype.computeCheckboxElementDomain = function (arg) {
            return (Utils.getClassName(arg) === "ExpansionSet") ? this.expansionsFilter.computeCheckboxElementDomain(arg) : this.conceptFilter.computeCheckboxElementDomain(arg);
        };
        NestedExpansionSetConceptFilter.prototype.getFilterTargets = function () {
            var concepts = this.conceptFilter.getFilterTargets();
            var ontologies = this.expansionsFilter.getFilterTargets();
            var both = new Array();
            both = both.concat(ontologies);
            both = both.concat(concepts);
            return both;
        };
        NestedExpansionSetConceptFilter.prototype.checkboxChanged = function (checkboxContextData, setOfHideCandidates, checkbox) {
            var result;
            if (Utils.getClassName(checkboxContextData) === "ExpansionSet") {
                result = this.expansionsFilter.checkboxChanged(checkboxContextData, setOfHideCandidates, checkbox);
                this.conceptFilter.updateCheckboxStateFromView(result);
            }
            else {
                result = this.conceptFilter.checkboxChanged(checkboxContextData, setOfHideCandidates, checkbox);
                this.expansionsFilter.updateCheckboxStateFromView(result);
            }
            return result;
        };
        NestedExpansionSetConceptFilter.prototype.updateCheckboxStateFromView = function (affectedNodes) {
            this.expansionsFilter.updateCheckboxStateFromView(affectedNodes);
            this.conceptFilter.updateCheckboxStateFromView(affectedNodes);
        };
        NestedExpansionSetConceptFilter.prototype.getHoverNeedsAdjacentHighlighting = function () {
            return false;
        };
        NestedExpansionSetConceptFilter.prototype.computeExpansionConceptDivId = function (expansionSet) {
            return "conceptDiv_" + this.expansionsFilter.computeCheckId(expansionSet);
        };
        NestedExpansionSetConceptFilter.prototype.updateFilterLabelLambda = function () {
            var _this = this;
            return function (target) {
                var checkId = _this.implementation.computeCheckId(target);
                var checkboxLabel = _this.implementation.generateCheckboxLabel(target);
                var checkboxColoredSquare = _this.implementation.generateColoredSquareIndicator(target);
                var label = $("label[for=" + checkId + "]");
                label.empty();
                label.append(checkboxColoredSquare + "&nbsp;" + checkboxLabel);
            };
        };
        NestedExpansionSetConceptFilter.prototype.updateFilterUI = function () {
            var _this = this;
            var checkboxSpanClass = this.getCheckboxSpanClass();
            var preExistingCheckboxes = $("." + checkboxSpanClass);
            var checkboxesPopulatedOrReUsed = $("");
            var outerThis = this;
            var expansionFilterTargets = this.expansionsFilter.getFilterTargets();
            var conceptFilterTargets = this.conceptFilter.getFilterTargets();
            $.each(expansionFilterTargets, function (i, target) {
                var checkId = _this.implementation.computeCheckId(target);
                var spanId = "span_" + checkId;
                if (0 === $("#" + spanId).length) {
                    var checkboxLabel = _this.implementation.generateCheckboxLabel(target);
                    var checkboxColoredSquare = _this.implementation.generateColoredSquareIndicator(target);
                    var labelExpanderIcon = $("<label>").addClass(Menu.Menu.menuItemExpanderLabelClass).addClass("unselectable").attr("unselectable", "on").text("+");
                    var innerHidingContainer = $("<div>").addClass(NestedExpansionSetConceptFilter.NESTED_CONTAINER_CLASS).attr("id", _this.computeExpansionConceptDivId(target)).css("display", "none");
                    var expanderClickFunction = function (open) {
                        var expanderIndicatorUpdate = function () {
                            labelExpanderIcon.text($(innerHidingContainer).css("display") === "none" ? "+" : "-");
                        };
                        if (undefined !== open) {
                            if (open) {
                                $(innerHidingContainer).slideDown('fast', expanderIndicatorUpdate);
                            }
                            else {
                                $(innerHidingContainer).slideUp('fast', expanderIndicatorUpdate);
                            }
                        }
                        else {
                            $(innerHidingContainer).slideToggle('fast', expanderIndicatorUpdate);
                        }
                    };
                    var checkbox = $("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "").addClass(_this.expansionsFilter.getCheckboxClass()).click(function (event) {
                        event.stopPropagation();
                    }).change(function (event) {
                        var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(target);
                        outerThis.implementation.checkboxChanged(target, nodeHideCandidates, $(this));
                    });
                    var spanOfExpanderAndCheckbox = $("<span>").append(labelExpanderIcon).append(checkbox).click(function () {
                        expanderClickFunction();
                    });
                    ;
                    labelExpanderIcon.text($(innerHidingContainer).css("display") === "none" ? "+" : "-");
                    var label = $("<label>").attr("for", checkId).append(checkboxColoredSquare + "&nbsp;" + checkboxLabel);
                    _this.filterContainer.append($("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox").addClass("expSet_" + checkboxSpanClass).mouseenter(outerThis.implementation.checkboxHoveredLambda(target)).mouseleave(outerThis.implementation.checkboxUnhoveredLambda(target)).append(spanOfExpanderAndCheckbox).append(label).append(innerHidingContainer));
                    target.getGraphModifier().addNameUpdateListener(checkId, function () {
                        _this.updateFilterLabelLambda()(target);
                    });
                }
                checkboxesPopulatedOrReUsed = checkboxesPopulatedOrReUsed.add("#" + spanId);
            });
            var expansionSetsWithConceptsPresent = $("");
            $.each(expansionFilterTargets, function (i, expSet) {
                var sortedExpSetNodes = _this.graphView.sortConceptNodesCentralOntologyName(expSet.getNodes());
                $.each(sortedExpSetNodes, function (i, target) {
                    if (!_this.graphView.conceptGraph.containsNode(target)) {
                        return;
                    }
                    var checkId = _this.implementation.computeCheckId(target);
                    var spanId = "span_" + checkId;
                    var correspondingExpansionInnerHidingContainer = $("#" + _this.computeExpansionConceptDivId(expSet));
                    if (0 === $("#" + spanId).length) {
                        var checkboxLabel = _this.implementation.generateCheckboxLabel(target);
                        var checkboxColoredSquare = _this.implementation.generateColoredSquareIndicator(target);
                        correspondingExpansionInnerHidingContainer.append($("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox").addClass("concept_" + checkboxSpanClass).css("padding-left", "2em").mouseenter(outerThis.implementation.checkboxHoveredLambda(target)).mouseleave(outerThis.implementation.checkboxUnhoveredLambda(target)).append($("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "").addClass(_this.conceptFilter.getCheckboxClass()).change(function () {
                            var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(target);
                            outerThis.implementation.checkboxChanged(target, nodeHideCandidates, $(this));
                        })).append($("<label>").attr("for", checkId).append(checkboxColoredSquare + "&nbsp;" + checkboxLabel)));
                    }
                    else {
                        correspondingExpansionInnerHidingContainer.append($("#" + spanId));
                    }
                    checkboxesPopulatedOrReUsed = checkboxesPopulatedOrReUsed.add("#" + spanId);
                    expansionSetsWithConceptsPresent = expansionSetsWithConceptsPresent.add($("#" + "span_" + _this.implementation.computeCheckId(expSet)));
                });
            });
            preExistingCheckboxes.not(checkboxesPopulatedOrReUsed).remove();
            var removed = $(".expSet_" + checkboxSpanClass).not(expansionSetsWithConceptsPresent).remove();
        };
        NestedExpansionSetConceptFilter.prototype.checkmarkAllCheckboxes = function () {
            this.conceptFilter.checkmarkAllCheckboxes();
        };
        NestedExpansionSetConceptFilter.SUB_MENU_TITLE = "Node Expansions Displayed";
        NestedExpansionSetConceptFilter.NESTED_CONTAINER_CLASS = "nestedExpansionSetConceptContainer";
        NestedExpansionSetConceptFilter.NESTED_FILTER_CLASSNAME_PREFIX = "NestedExpansionSetConceptFilter";
        NestedExpansionSetConceptFilter.NESTED_FILTER_CLASSNAME_PARENT_SUFFIX = "Parent";
        NestedExpansionSetConceptFilter.NESTED_FILTER_CLASSNAME_CHILD_SUFFIX = "Child";
        return NestedExpansionSetConceptFilter;
    })(ConceptFilterWidget.AbstractConceptNodeFilterWidget);
    exports.NestedExpansionSetConceptFilter = NestedExpansionSetConceptFilter;
});
