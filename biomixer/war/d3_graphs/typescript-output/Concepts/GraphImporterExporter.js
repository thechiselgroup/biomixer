///<reference path="headers/require.d.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "../FetchFromApi", "../GraphView", "./ConceptGraph", "../Menu", "../ExpansionSets", "Utils", "FetchFromApi", "GraphView", "Concepts/ConceptGraph", "Menu", "ExpansionSets"], function(require, exports, Fetcher, GraphView, ConceptGraph, Menu, ExpansionSets) {
    /**
    * This format is used for exporting and importing graphs and their layouts.
    * This can be transmitted across browsing sessions or between different people
    * using the visualization.
    * It is a relatively simple but self contained format that requires expansion
    * of each node included.
    *
    * I opted for shorter property names, expecting that this would likely be transmitted
    * as raw JSON text without compression.
    *
    * Each node indexed by its own, id, data including its ontology, x and y coordinate,
    *
    */
    var SavedGraphSeed = (function () {
        function SavedGraphSeed() {
        }
        return SavedGraphSeed;
    })();
    exports.SavedGraphSeed = SavedGraphSeed;

    var SavedGraph = (function () {
        function SavedGraph() {
            this.NB = "Greetings! This is a BioMixer portable graph." + " Paste this entire data structure into the Import dialog box, accessible via the menu." + " Use the url: " + SavedGraph.getUrlIFrameOrNot() + " .";
            this.n = [];
            this.s = {};
        }
        SavedGraph.prototype.addNode = function (nodeData, graph) {
            // Would make the graph an instance variable, but then it would get serialized later.
            var nodeSeed = {
                c: nodeData.simpleConceptUri, o: nodeData.ontologyAcronym, x: nodeData.x, y: nodeData.y
            };
            if (graph.expMan.wasConceptClearedForExpansion(nodeData.nodeId, ConceptGraph.PathOptionConstants.mappingsNeighborhoodConstant)) {
                nodeSeed.m = true;
            }
            this.n.push(nodeSeed);
        };

        SavedGraph.prototype.addLinkStyle = function (cssName, color) {
            this.s[cssName] = color;
        };

        SavedGraph.getUrlIFrameOrNot = function () {
            return document.location.protocol + '//' + document.location.host + document.location.pathname;
        };
        return SavedGraph;
    })();
    exports.SavedGraph = SavedGraph;

    var Widget = (function () {
        function Widget(pathsToRoot) {
            this.pathsToRoot = pathsToRoot;
            this.menuSelector = "";
        }
        Widget.prototype.addMenuComponents = function (menuSelector) {
            this.containers = Menu.Menu.slideToggleHeaderContainer(Widget.outerContainerId, Widget.innerContainerId, "Sharing", true);
            var outerContainer = this.containers.outer;
            var innerContainer = this.containers.inner;
            this.menuSelector = menuSelector; // store for later
            $(menuSelector).append(outerContainer);

            var exportButton = $("<div>").addClass("unselectable").addClass("importExportButton").addClass("boxButton").attr("id", "exportButton").attr("type", "button").text("Export").attr("value", "Export");

            var importButton = $("<div>").addClass("unselectable").addClass("importExportButton").addClass("boxButton").attr("id", "importButton").attr("type", "button").text("Import").attr("value", "Import");

            var firstCol = $("<div>").css("float", "left");
            var secondCol = $("<div>").css("float", "left");
            var footer = $("<div>").attr("id", Widget.exportImportFooterDiv).css("clear", "both");
            innerContainer.append($("<br>"));
            innerContainer.append(firstCol);
            innerContainer.append(secondCol);
            innerContainer.append($("<br>"));
            innerContainer.append(footer);

            firstCol.append(exportButton);
            secondCol.append(importButton);

            d3.selectAll("#exportButton").on("click", this.showExportDialogLambda());
            d3.selectAll("#importButton").on("click", this.showImportDialogLambda());
        };

        /**
        * When we are opening an empty page, we like to assume the user will be importing, so we can open all the required elements
        * for them.
        */
        Widget.prototype.openShareAndImportMenu = function () {
            $('#trigger').trigger("click");
            this.containers.expanderCallback();
            this.showImportDialogLambda()();
        };

        Widget.prototype.showExportDialogLambda = function () {
            var _this = this;
            return function () {
                var message = "To share this graph view, copy the text below, and share it via email." + "\nThe receiver can then click the import button, paste the text there, and see this current view.";
                var exporter = new GraphExporter(_this.pathsToRoot.conceptGraph, _this.pathsToRoot);
                var exportJson = exporter.getExportData();
                _this.messagePrompt(message, JSON.stringify(exportJson), null);
            };
        };

        Widget.prototype.showImportDialogLambda = function () {
            var _this = this;
            return function () {
                var message = "To import a graph, paste the JSON-format text that you received from your collaborator, and click OK.";
                _this.messagePrompt(message, "", _this.importCallbackLambda());
            };
        };

        Widget.prototype.importCallbackLambda = function () {
            var _this = this;
            return function (event) {
                event.stopPropagation();
                var dialog = $("#" + Widget.messageDivId);
                var messageField = $("#" + Widget.messageTextId);
                var importData = messageField.first().val();
                dialog.slideUp(200, function () {
                    dialog.detach();
                });

                if (importData.length === 0) {
                    return;
                }
                var importer = new GraphImporter(_this.pathsToRoot, JSON.parse(importData));
                importer.loadGraph();
            };
        };

        Widget.prototype.closeDialogLambda = function () {
            return function (event) {
                var dialog = $("#" + Widget.messageDivId);
                dialog.slideUp(200, function () {
                    dialog.detach();
                });
            };
        };

        Widget.prototype.messagePrompt = function (message, fieldContent, okCallback) {
            // Remove any existing version of this panel. It is an embedded modal singleton unique as a unicorn.
            var dialog = $("#" + Widget.messageDivId);
            if (undefined !== dialog) {
                dialog.detach();
            }

            // Create the new one.
            dialog = $("<div>").attr("id", Widget.messageDivId).addClass(Widget.messageDivClass).addClass("opaqueMenu");
            var messageParagraph = $("<p>").addClass(Widget.messageParagraphId);
            messageParagraph.text(message);
            var messageField = $("<textarea>").attr("id", Widget.messageTextId).addClass(Widget.messageTextId);
            messageField.text(fieldContent);
            messageField.select();

            // Default the ok button to close the box. If it is to something more useful, then create a cancel button
            // to allow the user to simply close the box.
            var cancelButton = undefined;
            var okButtonText;
            if (null === okCallback) {
                okCallback = this.closeDialogLambda();
                okButtonText = "Close";
            } else {
                cancelButton = $("<div>").addClass("boxButton").addClass("unselectable").addClass(Widget.messageBoxButtonClass).addClass("importExportButton").text("Cancel").click(this.closeDialogLambda());
                okButtonText = "Apply";
            }
            var okButton = $("<div>").addClass("boxButton").addClass("unselectable").addClass(Widget.messageBoxButtonClass).addClass("importExportButton").text(okButtonText).click(okCallback);
            dialog.append(messageParagraph).append(messageField).append(okButton);
            if (undefined !== cancelButton) {
                dialog.append(cancelButton);
            }

            dialog.css("display", "none");
            $("#" + Widget.exportImportFooterDiv).append(dialog);
            dialog.slideDown("fast");
        };
        Widget.exportImportFooterDiv = "exportImportFooterDiv";

        Widget.outerContainerId = "importerExporterMenuContainer";

        Widget.innerContainerId = "importerExporterInnerContainer";

        Widget.messageTextId = "messageBoxTextArea";
        Widget.messageParagraphId = "messageBoxMessage";
        Widget.messageDivId = "messageBox";
        Widget.messageDivClass = "messageBoxWithField";
        Widget.messageBoxButtonClass = "messageBoxButton";
        return Widget;
    })();
    exports.Widget = Widget;

    var GraphExporter = (function () {
        function GraphExporter(graph, pathsToRoot) {
            this.graph = graph;
            this.pathsToRoot = pathsToRoot;
        }
        GraphExporter.prototype.getExportData = function () {
            var _this = this;
            var savedGraph = new SavedGraph();

            // Fill it up!
            var nodes = this.graph.graphView.getUnhiddenNodes();
            nodes.each(function (index, node) {
                var nodeId = node.getAttribute("id").replace(GraphView.BaseGraphView.nodeGSvgClassSansDot + "_", "");
                var nodeData = _this.graph.getNodeByIdUri(nodeId);
                savedGraph.addNode(nodeData, _this.graph);
            });

            for (var i = 0; i < this.pathsToRoot.propertyRelationClassNames.length; i++) {
                var className = this.pathsToRoot.propertyRelationClassNames[i];
                var sheet = $.stylesheet("." + className);
                var color = sheet.css("fill");
                savedGraph.addLinkStyle(className, color);
            }

            return savedGraph;
        };
        return GraphExporter;
    })();
    exports.GraphExporter = GraphExporter;

    var GraphImporter = (function () {
        function GraphImporter(pathsToRoot, importData) {
            this.pathsToRoot = pathsToRoot;
            this.importData = importData;
            this.conceptGraph = pathsToRoot.conceptGraph;
        }
        GraphImporter.prototype.loadGraph = function () {
            if (0 >= Object.keys(this.importData).length) {
                console.log("No keys available in data that was imported.");
                return;
            }

            // Set the graph's layout to be agnostic. We are presumably getting all of the layout data from the imported
            // data, and we don't want any other layout algorithm overriding that when calls to refresh the layout occurs.
            // I expected to need to set the layout here, but it didn't work. Had to do it after adding all the data.
            // this.conceptGraph.graphView.setCurrentLayout(this.pathsToRoot.layouts.runFixedPositionLayoutLambda());
            // It's ok if the id for this expansion set is simplistic. We do want to allow for multiple imports within a page though...
            // Maybe...
            var expId = new ExpansionSets.ExpansionSetIdentifer("importedGraphInitialExpansion_" + GraphImporter.importNumber, "Imported Graph " + GraphImporter.importNumber);
            var initSet = this.pathsToRoot.prepareForExpansionFromScratch(expId, null, null);
            var expansionSet = initSet.expansionSet;

            // The init set has the expansion set I would otherwise have made, but is the right way to delete and add at the same time.
            // var expansionSet = new ExpansionSets.ExpansionSet<ConceptGraph.Node>(expId, null, this.conceptGraph, this.pathsToRoot.undoRedoBoss, null);
            GraphImporter.importNumber++;

            for (var className in this.importData.s) {
                var color = this.importData.s[className];
                var sheet = $.stylesheet("." + className);
                sheet.css("fill", color);
                sheet.css("stroke", color);
            }

            for (var i = 0; i < this.importData.n.length; i++) {
                // Verify the structure's contents. It was imported via casting, not parsing.
                var nodeData = this.importData.n[i];
                var conceptUri = ConceptGraph.ConceptGraph.computeNodeId(nodeData.c, nodeData.o);
                if (nodeData.m) {
                    expansionSet.graphModifier.addExtraInteraction(String(conceptUri), ConceptGraph.PathOptionConstants.mappingsNeighborhoodConstant);
                }

                // Casting to prevent need for re-boxing data. Would need to remove elements and leave just x and y.
                this.pathsToRoot.layouts.updateFixedLayoutDatum(conceptUri, nodeData);
                this.loadNode(conceptUri, nodeData, expansionSet);
            }

            this.conceptGraph.graphView.setCurrentLayout(this.pathsToRoot.layouts.runFixedPositionLayoutLambda());
        };

        GraphImporter.prototype.loadNode = function (conceptUri, nodeData, expansionSet) {
            // Dumb trick. Only way to do it. Minimizes casting, while allowing typing.
            var ontologyAcronym = nodeData.o;
            var simpleConceptUri = nodeData.c;
            if (!(String(conceptUri) in this.conceptGraph.conceptIdNodeMap)) {
                var url = this.conceptGraph.buildConceptUrlNewApi(ontologyAcronym, simpleConceptUri);
                var callback = new FetchAndApplyLayoutCallback(this.conceptGraph, this.pathsToRoot, nodeData, url, conceptUri, expansionSet);
                var fetcher = new Fetcher.RetryingJsonFetcher(url);
                fetcher.fetch(callback, true);
            }
        };
        GraphImporter.importNumber = 0;
        return GraphImporter;
    })();
    exports.GraphImporter = GraphImporter;

    /**
    * We need to set the node positions, but only after we have actually parsed and built the node.
    * This callback does just that. If D3 used layouts that did not embed position data into node
    * data, then we could do this very differently.
    */
    var FetchAndApplyLayoutCallback = (function (_super) {
        __extends(FetchAndApplyLayoutCallback, _super);
        function FetchAndApplyLayoutCallback(graph, pathsToRoot, nodeData, url, conceptUri, expansionSet) {
            var _this = this;
            _super.call(this, url, String(conceptUri), 0 /* nodeSingle */); //+":"+directCallForExpansionType);
            this.graph = graph;
            this.pathsToRoot = pathsToRoot;
            this.nodeData = nodeData;
            this.conceptUri = conceptUri;
            this.expansionSet = expansionSet;
            this.callback = function (conceptPropertiesData, textStatus, jqXHR) {
                // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.
                _this.wrappedCallback.callback(conceptPropertiesData, textStatus, jqXHR);

                // Casting to prevent need for re-boxing data. Would need to remove elements and leave just x and y.
                _this.pathsToRoot.layouts.updateFixedLayoutDatum(_this.conceptUri, _this.nodeData);
            };
            this.wrappedCallback = new ConceptGraph.FetchOneConceptCallback(graph, url, conceptUri, expansionSet, true);
        }
        return FetchAndApplyLayoutCallback;
    })(Fetcher.CallbackObject);
});
