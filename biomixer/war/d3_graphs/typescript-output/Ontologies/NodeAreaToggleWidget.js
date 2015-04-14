///<amd-dependency path="GraphView" />
///<amd-dependency path="Menu" />
define(["require", "exports", "../Menu", "GraphView", "Menu"], function (require, exports, Menu) {
    var NodeAreaToggleWidgets = (function () {
        function NodeAreaToggleWidgets(graphView, graphModel) {
            this.graphView = graphView;
            this.graphModel = graphModel;
        }
        NodeAreaToggleWidgets.prototype.addMenuComponents = function (menuSelector, defaultHideContainer) {
            var _this = this;
            var containers = Menu.Menu.slideToggleHeaderContainer("nodeFinderMenuContainer" + "OuterContainer", "nodeFinderMenuContainer" + "ScrollContainer", "Node Utilities", defaultHideContainer);
            var layoutsContainer = containers.inner;
            $(menuSelector).append(containers.outer);
            var toggleButtonLabel = $("<label>").text("Toggle Percentile").attr("id", NodeAreaToggleWidgets.toggleButtonLabelId).css("padding-top", "2px").css("padding-bottom", "-2px").addClass("unselectable").addClass("plainBoxButton");
            // Update the text once it is attached to the DOM
            var toggleFunc = function () {
                _this.flipProportionalPercentageState(_this.graphModel, _this.graphView);
                _this.updateLabelText();
            };
            var toggleButton = $("<div>").attr("id", "proportionalAreaToggleButton").addClass("unselectable").addClass("boxButton").attr("title", NodeAreaToggleWidgets.locateNodesButtonText).on("click", toggleFunc).append($("<div>").addClass("unselectable")).append(toggleButtonLabel);
            var searchDiv = $("<div>").addClass(NodeAreaToggleWidgets.nodeUtilityContainer).addClass("clearfix");
            searchDiv.append(toggleButton);
            layoutsContainer.append(searchDiv);
            // Set up label text now that it is attached to DOM
            this.updateLabelText();
        };
        NodeAreaToggleWidgets.prototype.updateLabelText = function () {
            if (NodeAreaToggleWidgets.usePercentile) {
                $("#" + NodeAreaToggleWidgets.toggleButtonLabelId).text("Toggle Nodes To Percentile Area");
            }
            else {
                $("#" + NodeAreaToggleWidgets.toggleButtonLabelId).text("Toggle Nodes To Absolute Area");
            }
        };
        NodeAreaToggleWidgets.prototype.flipProportionalPercentageState = function (graphModel, graphView) {
            NodeAreaToggleWidgets.usePercentile = !NodeAreaToggleWidgets.usePercentile;
            graphView.renderScaler.updateNodeScalingFactor();
        };
        NodeAreaToggleWidgets.usePercentile = true;
        NodeAreaToggleWidgets.toggleButtonLabelId = "percentileToggleButtonLabel";
        NodeAreaToggleWidgets.singleNodeImportButtonClass = "nodeAreaToggleBoxButton";
        NodeAreaToggleWidgets.nodeAdditionText = "To change the node areas from percentage to ";
        NodeAreaToggleWidgets.nodeUtilityContainer = "nodeUtilityContainer";
        NodeAreaToggleWidgets.locateNodesButtonText = "Toggle node areas from counts to percentage and back";
        return NodeAreaToggleWidgets;
    })();
    exports.NodeAreaToggleWidgets = NodeAreaToggleWidgets;
});
