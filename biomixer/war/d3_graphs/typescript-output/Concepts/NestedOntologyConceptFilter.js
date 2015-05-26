///<amd-dependency path="../Utils" />
///<amd-dependency path="../NodeFilterWidget" />
///<amd-dependency path="./ConceptNodeFilterWidget" />
///<amd-dependency path="./OntologyConceptFilter" />
///<amd-dependency path="./CherryPickConceptFilter" />
///<amd-dependency path="./ConceptPathsToRoot" />
///<amd-dependency path="./ConceptGraph" />
///<amd-dependency path="../Menu" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../Utils", "./ConceptNodeFilterWidget", "./OntologyConceptFilter", "./CherryPickConceptFilter", "../Menu", "../Utils", "../NodeFilterWidget", "./ConceptNodeFilterWidget", "./OntologyConceptFilter", "./CherryPickConceptFilter", "./ConceptPathsToRoot", "./ConceptGraph", "../Menu"], function (require, exports, Utils, ConceptFilterWidget, OntologyFilter, ConceptFilter, Menu) {
    /**
     * This forms a widget that is equivalent to the concept filter and the ontology filter combined, with nested tree representation
     * of the filtering checkboxes. That is, under each ontology checkbox are the checkboxes for the concepts therein, which may be
     * hidden and revealed with a +/- collapse button.
     *
     * It is implemented as a composition, driving the different behavior for the two checkbox types (cocnept and ontology)
     * from classes that already implemented those as separate sets of widgets.
     */
    var NestedOntologyConceptFilter = (function (_super) {
        __extends(NestedOntologyConceptFilter, _super);
        function NestedOntologyConceptFilter(conceptGraph, graphView, centralConceptUri) {
            _super.call(this, NestedOntologyConceptFilter.SUB_MENU_TITLE, graphView, conceptGraph);
            this.centralConceptUri = centralConceptUri;
            this.implementation = this;
            this.pathToRootView = graphView;
            this.ontologyFilter = new OntologyFilter.OntologyConceptFilter(conceptGraph, graphView, centralConceptUri);
            this.conceptFilter = new ConceptFilter.CherryPickConceptFilter(conceptGraph, graphView, centralConceptUri);
            this.ontologyFilter.modifyClassName(NestedOntologyConceptFilter.NESTED_FILTER_CLASSNAME_PREFIX + NestedOntologyConceptFilter.NESTED_FILTER_CLASSNAME_PARENT_SUFFIX); //_"+this.ontologyFilter.getClassName());
            this.conceptFilter.modifyClassName(NestedOntologyConceptFilter.NESTED_FILTER_CLASSNAME_PREFIX + NestedOntologyConceptFilter.NESTED_FILTER_CLASSNAME_CHILD_SUFFIX); //_"+this.conceptFilter.getClassName());
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
            // We need to update the ontology or concept composite checkbox widgets depending on which one was toggled.
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
        /**
         * Synchronize checkboxes with changes made via other checkboxes.
         * Will make the ontology checkboxes less opaque if any of the individual
         * nodes in the ontology differ in their state from the most recent toggled
         * state of this checkbox. That is, if all were hidden or shown, then one
         * was shown or hidden, the ontology checkbox will be changed visually
         * to indicate inconsistent state.
         */
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
        // Override for nesting
        NestedOntologyConceptFilter.prototype.updateFilterUI = function () {
            var _this = this;
            // Remove missing ones, whatever is left over in this collection
            var checkboxSpanClass = this.getCheckboxSpanClass();
            var preExistingCheckboxes = $("." + checkboxSpanClass);
            var checkboxesPopulatedOrReUsed = $("");
            var outerThis = this;
            // Can I generalize this sorting and node group for when we will have expansion sets? Maybe...
            var ontologyFilterTargets = this.ontologyFilter.getFilterTargets();
            var conceptFilterTargets = this.conceptFilter.getFilterTargets();
            // Add new ontology checkboxes.
            // Differs from parent class version at least because there is a +/- expander button preceding the checkbox
            $.each(ontologyFilterTargets, function (i, target) {
                var checkId = _this.implementation.computeCheckId(target);
                var spanId = "span_" + checkId;
                if (0 === $("#" + spanId).length) {
                    // We store some arbitrary containers of nodes to hide for each checkbox. Seems data consumptive.
                    var checkboxLabel = _this.implementation.generateCheckboxLabel(target);
                    var checkboxColoredSquare = _this.implementation.generateColoredSquareIndicator(target);
                    var labelExpanderIcon = $("<label>").addClass(Menu.Menu.menuItemExpanderLabelClass).addClass("unselectable").attr("unselectable", "on").addClass(NestedOntologyConceptFilter.NESTED_EXPANDER_CLASS);
                    var innerHidingContainer = $("<div>").addClass(NestedOntologyConceptFilter.NESTED_CONTAINER_CLASS).attr("id", _this.computeOntologyConceptDivId(target)).css("display", "none");
                    if (_this.filterContainer.children().length === 0) {
                        innerHidingContainer.css("display", "block");
                    }
                    var expanderIndicatorUpdate = function () {
                        // labelExpanderIcon.text( $(innerHidingContainer).css("display") === "none" ? "+" : "-");
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
                        // Used for the button, as well as for a programmatic callback for when we want to display the submenu
                        // for special purposes.
                        if (undefined !== open) {
                            if (open) {
                                $(innerHidingContainer).slideDown('fast', expanderIndicatorUpdate);
                            }
                            else {
                                $(innerHidingContainer).slideUp('fast', expanderIndicatorUpdate);
                            }
                        }
                        else {
                            // Don't have a preference of what to do? Toggle it.
                            $(innerHidingContainer).slideToggle('fast', expanderIndicatorUpdate);
                        }
                    };
                    var checkbox = $("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "").addClass(_this.ontologyFilter.getCheckboxClass()).click(function (event) {
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
                    expanderIndicatorUpdate();
                    _this.filterContainer.append($("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox").mouseenter(outerThis.implementation.checkboxHoveredLambda(target)).mouseleave(outerThis.implementation.checkboxUnhoveredLambda(target)).append(spanOfExpanderAndCheckbox).append($("<label>").attr("for", checkId).append(checkboxColoredSquare + "&nbsp;" + checkboxLabel)).append(innerHidingContainer));
                }
                checkboxesPopulatedOrReUsed = checkboxesPopulatedOrReUsed.add("#" + spanId);
            });
            // Add new concept checkboxes, nested below corresponding ontology checkbox
            $.each(conceptFilterTargets, function (i, target) {
                var checkId = _this.implementation.computeCheckId(target);
                var spanId = "span_" + checkId;
                var correspondingOntologyInnerHidingContainer = $("#" + _this.computeOntologyConceptDivId(target.ontologyAcronym));
                if (0 === $("#" + spanId).length) {
                    // We store some arbitrary containers of nodes to hide for each checkbox. Seems data consumptive.
                    var checkboxLabel = _this.implementation.generateCheckboxLabel(target);
                    var checkboxColoredSquare = _this.implementation.generateColoredSquareIndicator(target);
                    correspondingOntologyInnerHidingContainer.append($("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox").css("padding-left", "2em").mouseenter(outerThis.implementation.checkboxHoveredLambda(target)).mouseleave(outerThis.implementation.checkboxUnhoveredLambda(target)).append($("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "").addClass(_this.conceptFilter.getCheckboxClass()).change(function () {
                        var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(target);
                        outerThis.implementation.checkboxChanged(target, nodeHideCandidates, $(this));
                    })).append($("<label>").attr("for", checkId).append(checkboxColoredSquare + "&nbsp;" + checkboxLabel)));
                }
                else {
                    // Puts them into the order that the data structure uses
                    correspondingOntologyInnerHidingContainer.append($("#" + spanId));
                }
                checkboxesPopulatedOrReUsed = checkboxesPopulatedOrReUsed.add("#" + spanId);
            });
            // Keep only those checkboxes for which we looped over a node
            preExistingCheckboxes.not(checkboxesPopulatedOrReUsed).remove();
        };
        /**
         * Sets all checkboxes to be checked. Does not (appear!) to *trigger* the checkboxes though; this affects
         * the view only.
         */
        NestedOntologyConceptFilter.prototype.checkmarkAllCheckboxes = function () {
            // $("."+this.getCheckboxClass()).prop("checked", "checked").removeClass(AbstractNodeFilterWidget.SOME_SELECTED_CSS);
            this.conceptFilter.checkmarkAllCheckboxes();
            // Not needed?
            // this.ontologyFilter.checkmarkAllCheckboxes();
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
