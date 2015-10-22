///<reference path="headers/require.d.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "./FilterWidget", "./Utils", "./FilterWidget", "./Menu", "./GraphView", "./Concepts/ConceptPathsToRoot", "./Concepts/ConceptGraph"], function (require, exports, FilterWidget) {
    var AbstractNodeFilterWidget = (function (_super) {
        __extends(AbstractNodeFilterWidget, _super);
        function AbstractNodeFilterWidget(subMenuTitle, graphView) {
            _super.call(this, subMenuTitle);
            this.graphView = graphView;
        }
        /*
         * Override to add the checkbox reset and node delete buttons
         */
        AbstractNodeFilterWidget.prototype.addMenuComponents = function (menuSelector, defaultHideContainer) {
            var _this = this;
            _super.prototype.addMenuComponents.call(this, menuSelector, defaultHideContainer);
            // Important to use implementation, since it might override...
            this.addResetAndDeleteButtonsToMenuComponents(function () {
                _this.implementation.checkmarkAllCheckboxes();
            }, this.implementation.deleteSelectedCheckboxesLambda(function () {
                return _this.getHiddenAssociatedNodes();
            }));
        };
        /**
         * Get all the nodes associated with the filter, that are currently hidden.
         */
        AbstractNodeFilterWidget.prototype.getHiddenAssociatedNodes = function () {
            var targets = this.implementation.getFilterTargets();
            var nodes = [];
            for (var i in targets) {
                var targ = targets[i];
                var node = this.implementation.computeCheckboxElementDomain(targ);
                for (var j in node) {
                    var n = node[j];
                    if (this.graphView.isNodeHidden(n)) {
                        nodes.push(n);
                    }
                }
            }
            return nodes;
        };
        AbstractNodeFilterWidget.prototype.updateFilterUI = function () {
            var _this = this;
            // Remove missing ones, whatever is left over in this collection
            var checkboxSpanClass = this.getCheckboxSpanClass();
            var preExistingCheckboxes = $("." + checkboxSpanClass);
            var checkboxesPopulatedOrReUsed = $("");
            var outerThis = this;
            // Can I generalize this sorting and node group for when we will have expansion sets? Maybe...
            var filterTargets = this.implementation.getFilterTargets();
            // Add new ones
            $.each(filterTargets, function (i, target) {
                var checkId = _this.implementation.computeCheckId(target);
                var spanId = "span_" + checkId;
                if (0 === $("#" + spanId).length) {
                    // We store some arbitrary containers of nodes to hide for each checkbox. Seems data consumptive.
                    var checkboxLabel = _this.implementation.generateCheckboxLabel(target);
                    var checkboxColoredSquare = _this.implementation.generateColoredSquareIndicator(target);
                    _this.filterContainer.append($("<span>").attr("id", spanId).addClass(checkboxSpanClass).addClass("filterCheckbox").mouseenter(outerThis.implementation.checkboxHoveredLambda(target)).mouseleave(outerThis.implementation.checkboxUnhoveredLambda(target)).append($("<input>").attr("id", checkId).attr("type", "checkbox").attr("value", "on").attr("tabindex", "0").attr("checked", "").addClass(_this.getCheckboxClass()).change(function () {
                        var nodeHideCandidates = outerThis.implementation.computeCheckboxElementDomain(target);
                        outerThis.implementation.checkboxChanged(target, nodeHideCandidates, $(this));
                    })).append($("<label>").attr("for", checkId).append(checkboxColoredSquare + "&nbsp;" + checkboxLabel)));
                }
                else {
                    // Puts them into the order that the data structure uses
                    _this.filterContainer.append($("#" + spanId));
                }
                checkboxesPopulatedOrReUsed = checkboxesPopulatedOrReUsed.add("#" + spanId);
            });
            // Keep only those checkboxes for which we looped over a node
            preExistingCheckboxes.not(checkboxesPopulatedOrReUsed).remove();
        };
        /**
         * Intended to trigger each un-checked checkbox in order to undo its action and change its state to checked.
         */
        AbstractNodeFilterWidget.prototype.checkmarkAllCheckboxes = function () {
            $("." + this.getCheckboxClass() + ":not(:checked)").trigger("click");
        };
        AbstractNodeFilterWidget.SOME_SELECTED_CSS = "filterCheckboxSomeSelected";
        return AbstractNodeFilterWidget;
    })(FilterWidget.AbstractFilterWidget);
    exports.AbstractNodeFilterWidget = AbstractNodeFilterWidget;
});
