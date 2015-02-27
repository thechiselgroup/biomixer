///<reference path="headers/require.d.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../Utils", "./ConceptNodeFilterWidget", "./ExpansionSetFilter", "./CherryPickConceptFilter", "../Menu", "../Utils", "../NodeFilterWidget", "./ConceptNodeFilterWidget", "./CherryPickConceptFilter", "./ConceptPathsToRoot", "./ConceptGraph", "../ExpansionSets", "../Menu"], function(require, exports, Utils, ConceptFilterWidget, ExpansionSetFilter, ConceptFilter, Menu) {
    /**
    * This forms a widget that is equivalent to the concept filter and the expansion set filter combined, with nested tree representation
    * of the filtering checkboxes. That is, under each expansion checkbox are the checkboxes for the concepts therein, which may be
    * hidden and revealed with a +/- collapse button.
    *
    * It is implemented as a composition, driving the different behavior for the two checkbox types (concept and expansion set)
    * from classes that already implemented those as separate sets of widgets.
    */
    var NestedExpansionSetConceptFilter = (function (_super) {
        __extends(NestedExpansionSetConceptFilter, _super);
        function NestedExpansionSetConceptFilter(conceptGraph, graphView, centralConceptUri) {
            _super.call(this, NestedExpansionSetConceptFilter.SUB_MENU_TITLE, graphView, conceptGraph);
            this.centralConceptUri = centralConceptUri;
            this.implementation = this;
            this.pathToRootView = graphView;
            this.expansionsFilter = new ExpansionSetFilter.ExpansionSetFilter(conceptGraph, graphView);
            this.conceptFilter = new ConceptFilter.CherryPickConceptFilter(conceptGraph, graphView, centralConceptUri);

            this.expansionsFilter.modifyClassName(NestedExpansionSetConceptFilter.NESTED_FILTER_CLASSNAME_PREFIX + NestedExpansionSetConceptFilter.NESTED_FILTER_CLASSNAME_PARENT_SUFFIX); //_"+this.expansionFilter.getClassName());
            this.conceptFilter.modifyClassName(NestedExpansionSetConceptFilter.NESTED_FILTER_CLASSNAME_PREFIX + NestedExpansionSetConceptFilter.NESTED_FILTER_CLASSNAME_CHILD_SUFFIX); //_"+this.conceptFilter.getClassName());
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

            // We need to update the expansion or concept composite checkbox widgets depending on which one was toggled.
            if (Utils.getClassName(checkboxContextData) === "ExpansionSet") {
                result = this.expansionsFilter.checkboxChanged(checkboxContextData, setOfHideCandidates, checkbox);
                this.conceptFilter.updateCheckboxStateFromView(result);
            } else {
                result = this.conceptFilter.checkboxChanged(checkboxContextData, setOfHideCandidates, checkbox);
                this.expansionsFilter.updateCheckboxStateFromView(result);
            }

            return result;
        };

        /**
        * Synchronize checkboxes with changes made via other checkboxes.
        * Will make the expansion set checkboxes less opaque if any of the individual
        * nodes in the expansion set differ in their state from the most recent toggled
        * state of this checkbox. That is, if all were hidden or shown, then one
        * was shown or hidden, the expansion set checkbox will be changed visually
        * to indicate inconsistent state.
        */
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

        // Override for nesting
        NestedExpansionSetConceptFilter.prototype.updateFilterUI = function () {
            var _this = this;
            // Remove missing ones, whatever is left over in this collection
            var checkboxSpanClass = this.getCheckboxSpanClass();
            var preExistingCheckboxes = $("." + checkboxSpanClass);
            var checkboxesPopulatedOrReUsed = $("");
            var outerThis = this;

            // Can I generalize this sorting and node group for when we will have expansion sets? Maybe...
            var expansionFilterTargets = this.expansionsFilter.getFilterTargets();
            var conceptFilterTargets = this.conceptFilter.getFilterTargets();

            // Add new expansion set checkboxes.
            // Differs from parent class version at least because there is a +/- expander button preceding the checkbox
            $.each(expansionFilterTargets, function (i, target) {
                var checkId = _this.implementation.computeCheckId(target);
                var spanId = "span_" + checkId;
                if (0 === $("#" + spanId).length) {
                    // We store some arbitrary containers of nodes to hide for each checkbox. Seems data consumptive.
                    var checkboxLabel = _this.implementation.generateCheckboxLabel(target);
                    var checkboxColoredSquare = _this.implementation.generateColoredSquareIndicator(target);

                    var labelExpanderIcon = $("<label>").addClass(Menu.Menu.menuItemExpanderLabelClass).addClass("unselectable").attr("unselectable", "on").text("+");

                    var innerHidingContainer = $("<div>").addClass(NestedExpansionSetConceptFilter.NESTED_CONTAINER_CLASS).attr("id", _this.computeExpansionConceptDivId(target)).css("display", "none");

                    var expanderClickFunction = function (open) {
                        // Used for the button, as well as for a programmatic callback for when we want to display the submenu
                        // for special purposes.
                        var expanderIndicatorUpdate = function () {
                            labelExpanderIcon.text($(innerHidingContainer).css("display") === "none" ? "+" : "-");
                        };
                        if (undefined !== open) {
                            if (open) {
                                $(innerHidingContainer).slideDown('fast', expanderIndicatorUpdate);
                            } else {
                                $(innerHidingContainer).slideUp('fast', expanderIndicatorUpdate);
                            }
                        } else {
                            // Don't have a preference of what to do? Toggle it.
                            $(innerHidingContainer).slideToggle('fast', expanderIndicatorUpdate);
                        }
                    };

                    var checkbox = $("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "").addClass(_this.expansionsFilter.getCheckboxClass()).click(function (event) {
                        // I made the span control the +/- toggle, but clicks were going through the
                        // checkbox, toggling both the checkbox and triggering my +/- toggle function.
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

                    _this.filterContainer.append($("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox").addClass("expSet_" + checkboxSpanClass).mouseenter(outerThis.implementation.checkboxHoveredLambda(target)).mouseleave(outerThis.implementation.checkboxUnhoveredLambda(target)).append(spanOfExpanderAndCheckbox).append($("<label>").attr("for", checkId).append(checkboxColoredSquare + "&nbsp;" + checkboxLabel)).append(innerHidingContainer));
                }
                checkboxesPopulatedOrReUsed = checkboxesPopulatedOrReUsed.add("#" + spanId);
            });

            var expansionSetsWithConceptsPresent = $("");

            // Add new concept checkboxes, nested below corresponding expansion set checkbox
            $.each(expansionFilterTargets, function (i, expSet) {
                // Originally I iterated over the concept filter targets, but it was more sensible
                // to collect the expansion sets and iterate over those, knowing that the concepts
                // would reliably connect with the filter.
                // $.each(conceptFilterTargets, (i, target: ConceptGraph.Node) =>
                var sortedExpSetNodes = _this.graphView.sortConceptNodesCentralOntologyName(expSet.getNodes());
                $.each(sortedExpSetNodes, function (i, target) {
                    // Do not make checkboxes for nodes that have been deleted.
                    if (!_this.graphView.conceptGraph.containsNode(target)) {
                        return;
                    }

                    var checkId = _this.implementation.computeCheckId(target);
                    var spanId = "span_" + checkId;

                    // TODO Find expansion set on the basis of a single node.
                    // Am I 100% sure that there is never overlap?
                    var correspondingExpansionInnerHidingContainer = $("#" + _this.computeExpansionConceptDivId(expSet));

                    if (0 === $("#" + spanId).length) {
                        // We store some arbitrary containers of nodes to hide for each checkbox. Seems data consumptive.
                        var checkboxLabel = _this.implementation.generateCheckboxLabel(target);
                        var checkboxColoredSquare = _this.implementation.generateColoredSquareIndicator(target);

                        correspondingExpansionInnerHidingContainer.append($("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox").addClass("concept_" + checkboxSpanClass).css("padding-left", "2em").mouseenter(outerThis.implementation.checkboxHoveredLambda(target)).mouseleave(outerThis.implementation.checkboxUnhoveredLambda(target)).append($("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "").addClass(_this.conceptFilter.getCheckboxClass()).change(function () {
                            var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(target);
                            outerThis.implementation.checkboxChanged(target, nodeHideCandidates, $(this));
                        })).append($("<label>").attr("for", checkId).append(checkboxColoredSquare + "&nbsp;" + checkboxLabel)));
                    } else {
                        // Puts them into the order that the data structure uses
                        correspondingExpansionInnerHidingContainer.append($("#" + spanId));
                    }
                    checkboxesPopulatedOrReUsed = checkboxesPopulatedOrReUsed.add("#" + spanId);
                    expansionSetsWithConceptsPresent = expansionSetsWithConceptsPresent.add($("#" + "span_" + _this.implementation.computeCheckId(expSet)));
                });
            });

            // Keep only those checkboxes for which we looped over a node
            preExistingCheckboxes.not(checkboxesPopulatedOrReUsed).remove();

            // Also keep only those expansion set checkboxes for which there are some nodes beneath
            var removed = $(".expSet_" + checkboxSpanClass).not(expansionSetsWithConceptsPresent).remove();
        };

        /**
        * Sets all checkboxes to be checked. Does not (appear!) to *trigger* the checkboxes though; this affects
        * the view only.
        */
        NestedExpansionSetConceptFilter.prototype.checkmarkAllCheckboxes = function () {
            // $("."+this.getCheckboxClass()).prop("checked", "checked").removeClass(AbstractNodeFilterWidget.SOME_SELECTED_CSS);
            this.conceptFilter.checkmarkAllCheckboxes();
            // Not needed?
            // this.expansionsFilter.checkmarkAllCheckboxes();
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
