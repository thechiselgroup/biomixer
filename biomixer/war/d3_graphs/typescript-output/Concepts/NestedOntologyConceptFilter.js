var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../Utils", "./ConceptNodeFilterWidget", "./OntologyConceptFilter", "./CherryPickConceptFilter", "../Menu", "../Utils", "../NodeFilterWidget", "./ConceptNodeFilterWidget", "./OntologyConceptFilter", "./CherryPickConceptFilter", "./ConceptPathsToRoot", "./ConceptGraph", "../Menu"], function (require, exports, Utils, ConceptFilterWidget, OntologyFilter, ConceptFilter, Menu) {
    var NestedOntologyConceptFilter = (function (_super) {
        __extends(NestedOntologyConceptFilter, _super);
        function NestedOntologyConceptFilter(conceptGraph, graphView, centralConceptUri) {
            _super.call(this, NestedOntologyConceptFilter.SUB_MENU_TITLE, graphView, conceptGraph);
            this.centralConceptUri = centralConceptUri;
            this.implementation = this;
            this.pathToRootView = graphView;
            this.ontologyFilter = new OntologyFilter.OntologyConceptFilter(conceptGraph, graphView, centralConceptUri);
            this.conceptFilter = new ConceptFilter.CherryPickConceptFilter(conceptGraph, graphView, centralConceptUri);
            this.ontologyFilter.modifyClassName(NestedOntologyConceptFilter.NESTED_FILTER_CLASSNAME_PREFIX + NestedOntologyConceptFilter.NESTED_FILTER_CLASSNAME_PARENT_SUFFIX);
            this.conceptFilter.modifyClassName(NestedOntologyConceptFilter.NESTED_FILTER_CLASSNAME_PREFIX + NestedOntologyConceptFilter.NESTED_FILTER_CLASSNAME_CHILD_SUFFIX);
        }
        NestedOntologyConceptFilter.prototype.generateCheckboxLabel = function (arg) {
            return (Utils.getClassName(arg) === "String") ? this.ontologyFilter.generateCheckboxLabel(arg) : this.conceptFilter.generateCheckboxLabel(arg);
        };
        NestedOntologyConceptFilter.prototype.generateColoredSquareIndicator = function (arg) {
            return (Utils.getClassName(arg) === "String") ? this.ontologyFilter.generateColoredSquareIndicator(arg) : this.conceptFilter.generateColoredSquareIndicator(arg);
        };
        NestedOntologyConceptFilter.prototype.computeCheckId = function (arg) {
            return ((Utils.getClassName(arg) === "String") ? this.ontologyFilter.computeCheckId(arg) : this.conceptFilter.computeCheckId(arg));
        };
        NestedOntologyConceptFilter.prototype.computeCheckboxElementDomain = function (arg) {
            return (Utils.getClassName(arg) === "String") ? this.ontologyFilter.computeCheckboxElementDomain(arg) : this.conceptFilter.computeCheckboxElementDomain(arg);
        };
        NestedOntologyConceptFilter.prototype.getFilterTargets = function () {
            var concepts = this.conceptFilter.getFilterTargets();
            var ontologies = this.ontologyFilter.getFilterTargets();
            var both = new Array();
            both = both.concat(ontologies);
            both = both.concat(concepts);
            return both;
        };
        NestedOntologyConceptFilter.prototype.checkboxChanged = function (checkboxContextData, setOfHideCandidates, checkbox) {
            var result;
            if (Utils.getClassName(checkboxContextData) === "String") {
                result = this.ontologyFilter.checkboxChanged(checkboxContextData, setOfHideCandidates, checkbox);
                this.conceptFilter.updateCheckboxStateFromView(result);
            }
            else {
                result = this.conceptFilter.checkboxChanged(checkboxContextData, setOfHideCandidates, checkbox);
                this.ontologyFilter.updateCheckboxStateFromView(result);
            }
            return result;
        };
        NestedOntologyConceptFilter.prototype.updateCheckboxStateFromView = function (affectedNodes) {
            this.ontologyFilter.updateCheckboxStateFromView(affectedNodes);
            this.conceptFilter.updateCheckboxStateFromView(affectedNodes);
        };
        NestedOntologyConceptFilter.prototype.getHoverNeedsAdjacentHighlighting = function () {
            return false;
        };
        NestedOntologyConceptFilter.prototype.computeOntologyConceptDivId = function (acronym) {
            return "conceptDiv_" + this.ontologyFilter.computeCheckId(acronym);
        };
        NestedOntologyConceptFilter.prototype.updateFilterUI = function () {
            var _this = this;
            var checkboxSpanClass = this.getCheckboxSpanClass();
            var preExistingCheckboxes = $("." + checkboxSpanClass);
            var checkboxesPopulatedOrReUsed = $("");
            var outerThis = this;
            var ontologyFilterTargets = this.ontologyFilter.getFilterTargets();
            var conceptFilterTargets = this.conceptFilter.getFilterTargets();
            $.each(ontologyFilterTargets, function (i, target) {
                var checkId = _this.implementation.computeCheckId(target);
                var spanId = "span_" + checkId;
                if (0 === $("#" + spanId).length) {
                    var checkboxLabel = _this.implementation.generateCheckboxLabel(target);
                    var checkboxColoredSquare = _this.implementation.generateColoredSquareIndicator(target);
                    var labelExpanderIcon = $("<label>").addClass(Menu.Menu.menuItemExpanderLabelClass).addClass("unselectable").attr("unselectable", "on").addClass(NestedOntologyConceptFilter.NESTED_EXPANDER_CLASS);
                    var innerHidingContainer = $("<div>").addClass(NestedOntologyConceptFilter.NESTED_CONTAINER_CLASS).attr("id", _this.computeOntologyConceptDivId(target)).css("display", "none");
                    if (_this.filterContainer.children().length === 0) {
                        innerHidingContainer.css("display", "block");
                    }
                    var expanderIndicatorUpdate = function () {
                        if ($(innerHidingContainer).css("display") === "none") {
                            labelExpanderIcon.addClass(Menu.Menu.openActionClass);
                            labelExpanderIcon.removeClass(Menu.Menu.closeActionClass);
                        }
                        else {
                            labelExpanderIcon.removeClass(Menu.Menu.openActionClass);
                            labelExpanderIcon.addClass(Menu.Menu.closeActionClass);
                        }
                    };
                    var expanderClickFunction = function (open) {
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
                    var checkbox = $("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "").addClass(_this.ontologyFilter.getCheckboxClass()).click(function (event) {
                        event.stopPropagation();
                    }).change(function (event) {
                        var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(target);
                        outerThis.implementation.checkboxChanged(target, nodeHideCandidates, $(this));
                    });
                    var spanOfExpanderAndCheckbox = $("<span>").append(labelExpanderIcon).append(checkbox).click(function () {
                        expanderClickFunction();
                    });
                    ;
                    expanderIndicatorUpdate();
                    _this.filterContainer.append($("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox").mouseenter(outerThis.implementation.checkboxHoveredLambda(target)).mouseleave(outerThis.implementation.checkboxUnhoveredLambda(target)).append(spanOfExpanderAndCheckbox).append($("<label>").attr("for", checkId).append(checkboxColoredSquare + "&nbsp;" + checkboxLabel)).append(innerHidingContainer));
                }
                checkboxesPopulatedOrReUsed = checkboxesPopulatedOrReUsed.add("#" + spanId);
            });
            $.each(conceptFilterTargets, function (i, target) {
                var checkId = _this.implementation.computeCheckId(target);
                var spanId = "span_" + checkId;
                var correspondingOntologyInnerHidingContainer = $("#" + _this.computeOntologyConceptDivId(target.ontologyAcronym));
                if (0 === $("#" + spanId).length) {
                    var checkboxLabel = _this.implementation.generateCheckboxLabel(target);
                    var checkboxColoredSquare = _this.implementation.generateColoredSquareIndicator(target);
                    correspondingOntologyInnerHidingContainer.append($("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox").css("padding-left", "2em").mouseenter(outerThis.implementation.checkboxHoveredLambda(target)).mouseleave(outerThis.implementation.checkboxUnhoveredLambda(target)).append($("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "").addClass(_this.conceptFilter.getCheckboxClass()).change(function () {
                        var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(target);
                        outerThis.implementation.checkboxChanged(target, nodeHideCandidates, $(this));
                    })).append($("<label>").attr("for", checkId).append(checkboxColoredSquare + "&nbsp;" + checkboxLabel)));
                }
                else {
                    correspondingOntologyInnerHidingContainer.append($("#" + spanId));
                }
                checkboxesPopulatedOrReUsed = checkboxesPopulatedOrReUsed.add("#" + spanId);
            });
            preExistingCheckboxes.not(checkboxesPopulatedOrReUsed).remove();
        };
        NestedOntologyConceptFilter.prototype.checkmarkAllCheckboxes = function () {
            this.conceptFilter.checkmarkAllCheckboxes();
        };
        NestedOntologyConceptFilter.SUB_MENU_TITLE = "Ontologies and Concepts Displayed";
        NestedOntologyConceptFilter.NESTED_CONTAINER_CLASS = "nestedContologyConceptContainer";
        NestedOntologyConceptFilter.NESTED_FILTER_CLASSNAME_PREFIX = "NestedOntologyConceptFilter";
        NestedOntologyConceptFilter.NESTED_EXPANDER_CLASS = "nestedTreeExpander";
        NestedOntologyConceptFilter.NESTED_FILTER_CLASSNAME_PARENT_SUFFIX = "Parent";
        NestedOntologyConceptFilter.NESTED_FILTER_CLASSNAME_CHILD_SUFFIX = "Child";
        return NestedOntologyConceptFilter;
    })(ConceptFilterWidget.AbstractConceptNodeFilterWidget);
    exports.NestedOntologyConceptFilter = NestedOntologyConceptFilter;
});
