///<amd-dependency path="GraphView" />
///<amd-dependency path="Menu" />
define(["require", "exports", "../Menu", "GraphView", "Menu"], function (require, exports, Menu) {
    var NodeAreaToggleWidgets = (function () {
        function NodeAreaToggleWidgets(graphView, graphModel) {
            this.graphView = graphView;
            this.graphModel = graphModel;
        }
        NodeAreaToggleWidgets.prototype.addMenuComponents = function (menuSelector, defaultHideContainer) {
            var containers = Menu.Menu.slideToggleHeaderContainer("nodeFinderMenuContainer" + "OuterContainer", "nodeFinderMenuContainer" + "ScrollContainer", "Node Utilities", defaultHideContainer);
            this.addNodeTogglePercentileButton(menuSelector, containers);
            this.addNodeToggleSortButton(menuSelector, containers);
            // Set up label text now that it is attached to DOM
            this.updateLabelText();
        };
        NodeAreaToggleWidgets.prototype.addNodeTogglePercentileButton = function (menuSelector, containers) {
            var _this = this;
            var layoutsContainer = containers.inner;
            $(menuSelector).append(containers.outer);
            var toggleButtonLabel = $("<label>").text("Toggle Percentile").attr("id", NodeAreaToggleWidgets.toggleButtonLabelId).css("padding-top", "2px").css("padding-bottom", "-2px").addClass("unselectable").addClass("plainBoxButton");
            // Update the text once it is attached to the DOM
            var toggleFunc = function () {
                _this.flipProportionalPercentageState(_this.graphModel, _this.graphView);
                _this.updateLabelText();
            };
            var toggleButton = $("<div>").attr("id", "proportionalAreaToggleButton").addClass("unselectable").addClass("boxButton").attr("title", NodeAreaToggleWidgets.toggleNodePercentileButtonText).on("click", toggleFunc).append($("<div>").addClass("unselectable")).append(toggleButtonLabel);
            var togglePercentileDiv = $("<div>").addClass(NodeAreaToggleWidgets.nodeUtilityContainer).addClass("clearfix");
            togglePercentileDiv.append(toggleButton);
            layoutsContainer.append(togglePercentileDiv);
            //        // Set up label text now that it is attached to DOM
            //        this.updateLabelText();
        };
        NodeAreaToggleWidgets.prototype.addNodeToggleSortButton = function (menuSelector, containers) {
            var _this = this;
            var layoutsContainer = containers.inner;
            $(menuSelector).append(containers.outer);
            var toggleButtonLabel = $("<label>").text("Toggle Sort").attr("id", NodeAreaToggleWidgets.sortToggleButtonLabelId).css("padding-top", "2px").css("padding-bottom", "-2px").addClass("unselectable").addClass("plainBoxButton");
            // Update the text once it is attached to the DOM
            var toggleFunc = function () {
                _this.flipNodeSortState(_this.graphModel, _this.graphView);
                _this.updateLabelText();
            };
            var toggleButton = $("<div>").attr("id", "proportionalAreaToggleButton").addClass("unselectable").addClass("boxButton").attr("title", NodeAreaToggleWidgets.sortToggleNodePercentileButtonText).on("click", toggleFunc).append($("<div>").addClass("unselectable")).append(toggleButtonLabel);
            var togglePercentileDiv = $("<div>").addClass(NodeAreaToggleWidgets.nodeUtilityContainer).addClass("clearfix");
            togglePercentileDiv.append(toggleButton);
            layoutsContainer.append(togglePercentileDiv);
            //        // Set up label text now that it is attached to DOM
            //        this.updateLabelText();
        };
        NodeAreaToggleWidgets.prototype.updateLabelText = function () {
            if (NodeAreaToggleWidgets.usePercentile) {
                $("#" + NodeAreaToggleWidgets.toggleButtonLabelId).text("Toggle Nodes To Percentile Area");
            }
            else {
                $("#" + NodeAreaToggleWidgets.toggleButtonLabelId).text("Toggle Nodes To Absolute Area");
            }
            if (NodeAreaToggleWidgets.sortPercentile) {
                $("#" + NodeAreaToggleWidgets.sortToggleButtonLabelId).text("Sort Nodes By Absolute Area");
            }
            else {
                $("#" + NodeAreaToggleWidgets.sortToggleButtonLabelId).text("Sort Nodes By Percentile Area");
            }
        };
        NodeAreaToggleWidgets.prototype.flipProportionalPercentageState = function (graphModel, graphView) {
            NodeAreaToggleWidgets.usePercentile = !NodeAreaToggleWidgets.usePercentile;
            graphView.renderScaler.updateNodeScalingFactor();
        };
        NodeAreaToggleWidgets.prototype.flipNodeSortState = function (graphModel, graphView) {
            var _this = this;
            var newValue = !NodeAreaToggleWidgets.sortPercentile;
            var basicSortCallback = function () {
                NodeAreaToggleWidgets.sortPercentile = newValue;
                var sortFunc = graphModel.getOntologySortFunction();
                graphModel.sortedAcronymsByMappingCount = graphModel.sortedAcronymsByMappingCount.sort(sortFunc);
                graphView.filterSliders.updateTopMappingsSliderRange();
                graphView.filterSliders.filterGraphOnMappingCounts();
                //        graphView.renderScaler.updateNodeScalingFactor();
                graphView.runCurrentLayout();
            };
            if (!NodeAreaToggleWidgets.sortPercentile) {
                var sortTimerCallback;
                sortTimerCallback = function () {
                    console.log("Sort timer");
                    // Sort
                    basicSortCallback();
                    // Shall we sort again? Yes if we don't have all of the data yet...
                    if (0 < _this.getOutstandingFetchCallCount()) {
                        // set the timer again.
                        window.setTimeout(sortTimerCallback, 300);
                    }
                };
                // As fetches are returned for this percentile sort, we need to update the view,
                // For percentile sorting, we actually do need to be done all of the REST calls.
                // Therefore, trigger them all, and re-sort occassionally as results come in.
                if (0 < this.getOutstandingFetchCallCount()) {
                    var fetchAndSortCallback = function () {
                        // Get fetching going, and proceed with the callback that has the timer for re-sorting
                        // as results come in.
                        sortTimerCallback();
                        for (var nodeIndex in _this.graphModel.graphD3Format.nodes) {
                            graphModel.fetchNodeRestData(graphModel.graphD3Format.nodes[nodeIndex]);
                        }
                    };
                    this.showNodeSortDialog(fetchAndSortCallback);
                }
                else {
                    // We already have all the stuff, so we needn't bother the user with the dialog,
                    // and we can execute the sort once. the timer should not be triggered either.
                    basicSortCallback();
                }
            }
            else {
                // Sorting by absolute
                basicSortCallback();
            }
        };
        NodeAreaToggleWidgets.prototype.getOutstandingFetchCallCount = function () {
            var incompleteFetchCalls = 0;
            for (var n in this.graphModel.graphD3Format.nodes) {
                var nodeCompleted = this.graphModel.graphD3Format.nodes[n];
                if (undefined === nodeCompleted.metricsFetched) {
                    incompleteFetchCalls++;
                }
            }
            return incompleteFetchCalls;
        };
        NodeAreaToggleWidgets.prototype.showNodeSortDialog = function (callback) {
            var outerThis = this;
            $('#confirm').modal({
                closeHTML: "<a href='#' title='Close' class='modal-close'>x</a>",
                position: ["20%",],
                overlayId: 'confirm-overlay',
                containerId: 'confirm-container',
                onShow: function (dialog) {
                    var modal = this;
                    var message = $("<div>").attr("id", "nodeSortDialogMessage").css("white-space", "pre-wrap");
                    // Tired of fighting with CSS
                    $("#confirm-container").css("height", "auto");
                    $('.message', dialog.data[0]).append(message);
                    $("div.buttons").css("width", "auto");
                    $("#nodeSortDialogMessage").empty().append($("<span>").text("You are about sort the nodes by the percentage of mappings to the central ontology." + "\n" + "This could take a few seconds the first time."));
                    // if the user clicks "yes"
                    $('.yes', dialog.data[0]).click(function () {
                        // close the dialog
                        modal.close(); // or $.modal.close();
                        // call the callback
                        callback();
                    });
                    $('.no', dialog.data[0]).click(function () {
                        // close the dialog
                        modal.close(); // or $.modal.close();
                        // Do nothing else.
                    });
                    $(".yes").text("Proceed");
                    $(".no").text("Stop");
                    $("div.buttons").css("padding", "0px 5px 5px 0px");
                    // http://jqueryui.com/draggable/#handle
                    $($("#confirm-container")).draggable({ handle: $("#simplemodal-GrabHandle") });
                }
            });
        };
        NodeAreaToggleWidgets.nodeUtilityContainer = "nodeUtilityContainer";
        NodeAreaToggleWidgets.usePercentile = true;
        NodeAreaToggleWidgets.toggleButtonLabelId = "percentileToggleButtonLabel";
        NodeAreaToggleWidgets.nodeTogglePercentageButtonClass = "nodeAreaToggleBoxButton";
        NodeAreaToggleWidgets.toggleNodePercentileButtonText = "Toggle node areas from counts to percentage and back";
        NodeAreaToggleWidgets.sortPercentile = false;
        NodeAreaToggleWidgets.sortToggleButtonLabelId = "sortPercentileToggleButtonLabel";
        NodeAreaToggleWidgets.sortNodeTogglePercentageButtonClass = "sortPercentileToggleBoxButton";
        NodeAreaToggleWidgets.sortToggleNodePercentileButtonText = "Toggle node sort order from counts to percentage and back";
        return NodeAreaToggleWidgets;
    })();
    exports.NodeAreaToggleWidgets = NodeAreaToggleWidgets;
});
