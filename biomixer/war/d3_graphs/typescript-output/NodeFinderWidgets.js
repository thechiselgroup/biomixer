///<reference path="headers/require.d.ts" />
///<reference path="headers/d3.d.ts" />
define(["require", "exports", "./Menu", "GraphView", "Menu"], function(require, exports, Menu) {
    var NodeFinder = (function () {
        function NodeFinder(graphView, graphModel) {
            this.graphView = graphView;
            this.graphModel = graphModel;
        }
        NodeFinder.prototype.addMenuComponents = function (menuSelector, defaultHideContainer) {
            var containers = Menu.Menu.slideToggleHeaderContainer("nodeFinderMenuContainer" + "OuterContainer", "nodeFinderMenuContainer" + "ScrollContainer", "Node Utilities", defaultHideContainer);
            var layoutsContainer = containers.inner;

            $(menuSelector).append(containers.outer);

            var searchInput = $("<input>").addClass(NodeFinder.locateNodesInputClass).attr("title", NodeFinder.locateNodesButtonText).attr("id", "findNodeInputBox");

            var findFunc = this.highlightNodeNameMatches(this.graphModel, this.graphView, searchInput);

            var searchButton = $("<div>").attr("id", "nodeNameSearchButton").addClass("unselectable").addClass("boxButton").attr("title", NodeFinder.locateNodesButtonText).append($("<div>").addClass("unselectable").addClass(NodeFinder.locateNodesButtonClass).addClass(NodeFinder.iconButton)).append($("<label>").text("Locate Node").css("padding-top", "2px").css("padding-bottom", "-2px").addClass("unselectable").addClass("plainBoxButton"));

            var searchDiv = $("<div>").addClass(NodeFinder.nodeUtilityContainer).addClass("clearfix");
            searchDiv.append(searchInput);
            searchDiv.append(searchButton);
            layoutsContainer.append(searchDiv);

            var addUriInput = $("<input>").addClass(NodeFinder.locateNodesInputClass).attr("id", NodeFinder.singleNodeImportFieldId).addClass(NodeFinder.singleNodeImportFieldId);

            var addUriButton = $("<label>").addClass("unselectable").addClass("addSingleConceptButton").addClass(NodeFinder.singleNodeImportButtonClass).addClass("plainBoxButton").addClass("boxButton").text("Add Concept Using URI");

            var addUriDiv = $("<div>").addClass(NodeFinder.nodeUtilityContainer).addClass("clearfix").attr("title", NodeFinder.nodeAdditionText);
            addUriDiv.append(addUriInput);
            addUriDiv.append(addUriButton);
            layoutsContainer.append(addUriDiv);

            addUriButton.click(this.importSingleNodeCallbackLambda());

            searchInput.on("keydown", function (event) {
                if (event.which === 13) {
                    event.preventDefault();
                    findFunc();
                }
                // console.log(event.keyCode);
            });

            // for the button, after click or Enter key press, move focus back to the input box for nicer interactions.
            searchButton.on("click", function () {
                findFunc();
                searchInput.focus();
            }).on("keydown", function (event) {
                if (event.which === 13) {
                    event.preventDefault();
                    findFunc();
                    searchInput.focus();
                }
                // console.log(event.keyCode);
            });
        };

        NodeFinder.prototype.highlightNodeNameMatches = function (graphModel, graphView, textInput) {
            return function () {
                graphView.animateHighlightNodesDeactivate();
                var trimmed = $.trim(textInput.val());
                if ("" === trimmed) {
                    return;
                }
                var matchingNodes = graphModel.findNodesByName(trimmed);
                graphView.animateHighlightNodesActivate(matchingNodes);
            };
        };

        NodeFinder.prototype.importSingleNodeCallbackLambda = function () {
            var _this = this;
            return function (event) {
                event.stopPropagation();
                var messageField = $("#" + NodeFinder.singleNodeImportFieldId);
                var importData = messageField.first().val();

                if (importData.length === 0) {
                    return;
                }
                _this.graphModel.addNodeToGraph(importData);
                messageField.first().val("");
            };
        };
        NodeFinder.singleNodeImportFieldId = "singleNodeImportMessageBoxTextArea";
        NodeFinder.singleNodeImportButtonClass = "singleNodeImportMessageBoxButton";
        NodeFinder.nodeAdditionText = "To import a single node, paste the URI id (found via BioPortal) into the field.";

        NodeFinder.nodeUtilityContainer = "nodeUtilityContainer";
        NodeFinder.locateNodesInputClass = "locateNodeByNameInput";
        NodeFinder.locateNodesButtonClass = "locateNodeByNameButtonIcon";
        NodeFinder.iconButton = "boxButtonIconSegment";
        NodeFinder.locateNodesButtonText = "Locate nodes in graph based on Name/Synonyms";
        return NodeFinder;
    })();
    exports.NodeFinder = NodeFinder;
});
