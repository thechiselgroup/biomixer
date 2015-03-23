define(["require", "exports", "../Menu", "./ConceptPathsToRoot", "./ConceptLayouts", "./GraphImporterExporter", "JQueryExtension", "Utils", "Menu", "Concepts/ConceptPathsToRoot", "Concepts/ConceptLayouts", "Concepts/GraphImporterExporter"], function (require, exports, Menu, PathsToRoot, ConceptLayouts, GraphImporterExporter) {
    var Tour = (function () {
        function Tour(pathsToRoot, menu) {
            this.pathsToRoot = pathsToRoot;
            this.menu = menu;
            this.tourButtonId = "tourButton";
            this.introJsSettings = {
                nextLabel: 'Next &rarr;',
                prevLabel: '&larr; Back',
                skipLabel: 'Stop',
                doneLabel: 'Done',
                tooltipPosition: 'auto',
                tooltipClass: '',
                highlightClass: 'nonVisibleIntroJsHelperLayer',
                exitOnEsc: true,
                exitOnOverlayClick: false,
                showStepNumbers: false,
                keyboardNavigation: true,
                showButtons: true,
                showBullets: false,
                showProgress: true,
                scrollToElement: false,
                overlayOpacity: 0.8,
                positionPrecedence: ["bottom", "top", "right", "left"],
                disableInteraction: false,
                dragByLowerLeftHandle: true
            };
        }
        Tour.prototype.initializeMenu = function () {
            var _this = this;
            var introJsTourButton = $("<label>").attr("id", this.tourButtonId).attr("class", "nodeCommandButton").addClass("unselectable").addClass(Menu.Menu.topBarButtonClass).text("Take a Tour!");
            $(Menu.Menu.menuBarSelector).append(introJsTourButton);
            introJsTourButton.click(function (event) {
                event.stopPropagation();
                _this.startIntro();
            });
        };
        Tour.prototype.closeMenuIfVisible = function () {
            this.menu.closeMenuLambda()();
        };
        Tour.prototype.showMenuIfNotVisible = function () {
            this.menu.openMenu();
        };
        Tour.prototype.showSubMenuIfNotVisible = function (childMenuItem, collapseOthers) {
            var _this = this;
            if (collapseOthers === void 0) { collapseOthers = false; }
            this.showMenuIfNotVisible();
            if (collapseOthers) {
                var panels = $("." + Menu.Menu.hidingMenuContainerClass);
                panels.each(function (i, menuItem) {
                    if (childMenuItem[0] === menuItem) {
                        return;
                    }
                    var label = $(menuItem).siblings("." + Menu.Menu.menuItemExpanderLabelClass);
                    if (label.hasClass(Menu.Menu.closeActionClass)) {
                        label.click();
                    }
                });
            }
            var label = childMenuItem.siblings("." + Menu.Menu.menuItemExpanderLabelClass);
            if (!label.hasClass(Menu.Menu.closeActionClass)) {
                label.trigger("click", function () {
                    _this.refreshIntro();
                });
            }
        };
        Tour.prototype.startIntro = function () {
            var tourSteps = this.getSteps();
            this.intro = introJs();
            this.intro.setOptions(this.introJsSettings);
            var tour = {
                steps: tourSteps
            };
            this.intro.setOptions(tour);
            this.intro.start();
        };
        Tour.prototype.refreshIntro = function () {
            if (null != this.intro) {
                this.intro.refresh();
            }
        };
        Tour.prototype.getSteps = function () {
            var _this = this;
            return [
                {
                    intro: "<h1 class='introjs-header'>Welcome</h1> This will take you on an interactive tour of the visualization tools. We recommend you start the tour in a <a href='#' onclick='location.reload(); return false;' > freshly loaded visualization </a> that you haven't manipulated. <br/><br/> More documentation is available on the <a target='_blank' href='http://www.bioontology.org/wiki/index.php/Visualizing_Concepts_and_Mappings'>Bioportal Wiki</a>.",
                    onbeforechange: function () {
                        _this.closeMenuIfVisible();
                    },
                    position: "bottom"
                },
                {
                    element: $(PathsToRoot.ConceptPathsToRoot.VIZ_SELECTOR_ID)[0],
                    intro: "<h1 class='introjs-header'>Graph Modes</h1> The initial display of a visualization varies depending on which mode it starts in. Additional terms may be added by interacting with the graph, but we will explore these starting modes first.",
                    position: "bottom"
                },
                {
                    element: $(PathsToRoot.ConceptPathsToRoot.VIZ_SELECTOR_ID)[0],
                    intro: "<h1 class='introjs-header'>Graph Modes: Term Neighborhood</h1> Try clicking and selecting 'Term Neighborhood'.<br/><br/> This mode initializes the graph with the terms that are directly related to the target term, and which occur in the same ontology.",
                    position: "bottom"
                },
                {
                    element: $(PathsToRoot.ConceptPathsToRoot.VIZ_SELECTOR_ID)[0],
                    intro: "<h1 class='introjs-header'>Graph Modes: Mapping Neighborhood</h1> Try clicking and selecting 'Mapping Neighborhood'.<br/><br/> This mode initializes the graph with terms from other ontologies which have been mapped together; they represent the same concept in separate ontologies.",
                    position: "bottom"
                },
                {
                    element: $(PathsToRoot.ConceptPathsToRoot.VIZ_SELECTOR_ID)[0],
                    intro: "<h1 class='introjs-header'>Graph Modes: Paths to Root</h1> Try clicking and selecting 'Path to Root'.<br/><br/> This mode shows all terms between the target node and the root node of its ontology.",
                    position: "bottom"
                },
                {
                    element: $("#" + Menu.Menu.triggerId)[0],
                    intro: "<h1 class='introjs-header'>Main Menu</h1> This menu provides access to a variety of more advanced visualization tools. We will explore this later. Let's cover some basics first.",
                    onbeforechange: function () {
                        _this.closeMenuIfVisible();
                    },
                    position: "left",
                },
                {
                    element: function () {
                        return $(".centralNode").parent()[0];
                    },
                    intro: "<h1 class='introjs-header'>Node Details</h1> Click a node to display more information about a concept or mapping. The popup remains visible until you click somewhere else. Try it!",
                    position: "bottom"
                },
                {
                    intro: "If the popup is still open, try clicking the white space. This will make the popup disappear.",
                },
                {
                    intro: "<h1 class='introjs-header'>Hovering over Nodes</h1> Hover over a node to highlight the concept or mapping's immediate parent and children.",
                    position: "bottom"
                },
                {
                    intro: "<h1 class='introjs-header'>Hovering over Links</h1> Hover (carefully) over the line or arrow between two nodes to display more details about their relationship.",
                },
                {
                    intro: "Drag nodes to reposition them.",
                },
                {
                    element: $("#findNodeInputBox")[0],
                    intro: "<h1 class='introjs-header'>Node Radar</h1> Highlight a concept or mapping in a visualization by searching for part of its name or synonym using the Locate Node tool.",
                    onbeforechange: function () {
                        _this.showSubMenuIfNotVisible($("#nodeFinderMenuContainerScrollContainer"), true);
                    },
                    position: "left"
                },
                {
                    element: function () {
                        return $("rect[id^='node_expander_indicator']")[0];
                    },
                    intro: "<h1 class='introjs-header'>Node Expansion Menus</h1> Display other related concepts and mappings by clicking the drop-down arrow beneath a node.",
                    position: "top"
                },
                {
                    element: $("#singleNodeImportMessageBoxTextArea")[0],
                    intro: "<h1 class='introjs-header'>Single Node Import</h1> You can add a specific concept or mapping based on its BioPortal ID, even ones that are unrelated to those already in the visualization. These are easily found through the main Bioportal search interface. For example, try adding http://purl.org/stemnet/MHC#Mouse (if not already in the visualization).",
                    onbeforechange: function () {
                        _this.showSubMenuIfNotVisible($("#nodeFinderMenuContainerScrollContainer"), true);
                    },
                    position: "left"
                },
                {
                    element: $("#" + ConceptLayouts.ConceptLayouts.layoutMenuContainerId)[0],
                    intro: "<h1 class='introjs-header'>Layouts</h1> There are six layout algorithms that can help untangle relationships. Try them out.",
                    onbeforechange: function () {
                        _this.showMenuIfNotVisible();
                    },
                    position: "bottom"
                },
                {
                    element: $("#undo_redo_breadcrumb_trail")[0],
                    intro: "<h1 class='introjs-header'>Undo/Redo</h1> Changes to a visualization can be undone and reapplied. However, you cannot undo node movements (nodes you dragged to a new location). Try playing with both the undo/redo arrows, as well as the drop-down arrows (which display a list of the changes you've made).",
                    position: "bottom"
                },
                {
                    intro: "<h1 class='introjs-header'>Adding Nodes from Other Ontologies</h1> For the next step, click a node's drop-down arrow and select Expand Mappings (if there are about 5 available--it shows you the number of available mappings in parentheses). When you have done that, click Next.",
                    position: "bottom"
                },
                {
                    element: $("#NestedOntologyConceptFilterOuterContainer")[0],
                    intro: "<h1 class='introjs-header'>Filtering and Dimming Nodes</h1> When a visualization contains concepts from multiple ontologies, you can hide nodes based on their ontology. Try hiding all the nodes in an ontology. Also try clicking the check boxes with concept names to hide only a few concepts.",
                    onbeforechange: function () {
                        _this.showSubMenuIfNotVisible($("#NestedOntologyConceptFilterScrollContainer"), true);
                    },
                    position: "left"
                },
                {
                    element: $("#NestedOntologyConceptFilterCheckboxResetButton")[0],
                    intro: "You can re-select all those check boxes at once with this button.",
                    onbeforechange: function () {
                        _this.showSubMenuIfNotVisible($("#NestedOntologyConceptFilterScrollContainer"), true);
                    },
                    position: "top"
                },
                {
                    element: $("#NestedOntologyConceptFilterCheckboxDeleteButton")[0],
                    intro: "<h1 class='introjs-header'>Deleting Nodes</h1> And you can delete all the hidden nodes with this button. Try hiding one node, then pressing the button. This may be undone with the undo tools previously covered in this tour.",
                    onbeforechange: function () {
                        _this.showSubMenuIfNotVisible($("#NestedOntologyConceptFilterScrollContainer"), true);
                    },
                    position: "top"
                },
                {
                    intro: "<h1 class='introjs-header'>More Node Filtering Controls: Tool Tip</h1> Nodes can also be filtered from the tool tip. Click a node to see the tool tip and toggle the check box there.",
                    position: "bottom"
                },
                {
                    intro: "<h1 class='introjs-header'>More Node Filtering Controls: Node Menu</h1> In addition, nodes can also be filtered from the node menu. Click the drop-down arrow beneath a node to see the 'Hide Node' option.",
                    position: "bottom"
                },
                {
                    intro: "<h1 class='introjs-header'>Creating Expansion Sets</h1> For the next step, click a node's drop-down arrow and select Expand Concepts (if there are about 5 available--it shows you the number of available concepts in parentheses). When you have done that, click Next.",
                    position: "bottom"
                },
                {
                    element: $("#NestedExpansionSetConceptFilterOuterContainer")[0],
                    intro: "<h1 class='introjs-header'>Filtering Expansion Sets</h1> You just expanded a concept and the system added a number of related nodes to the visualization. This is called an Expansion Set. You can see these groupings in the menu on the right-hand side. You can hide entire sets or individual nodes--the same way you can work with ontology filters.",
                    onbeforechange: function () {
                        _this.showSubMenuIfNotVisible($("#NestedExpansionSetConceptFilterScrollContainer"), true);
                    },
                    position: "left"
                },
                {
                    element: $("#ConceptEdgeTypeFilterOuterContainer")[0],
                    intro: "<h1 class='introjs-header'>Edge Filters</h1> To focus on certain types of relationships, or to visually simplify a visualization, try filtering out the edges by toggling the check boxes.",
                    onbeforechange: function () {
                        _this.showSubMenuIfNotVisible($("#ConceptEdgeTypeFilterScrollContainer"), true);
                    },
                    position: "left"
                },
                {
                    element: $("#ConceptEdgeTypeFilterOuterContainer")[0],
                    intro: "Click the arrow (beside the check box) to assign a new color to that arc type.",
                    onbeforechange: function () {
                        _this.showSubMenuIfNotVisible($("#ConceptEdgeTypeFilterScrollContainer"), true);
                    },
                    position: "left"
                },
                {
                    element: $("#" + GraphImporterExporter.Widget.outerContainerId)[0],
                    intro: "<h1 class='introjs-header'>Sharing</h1> You can share a visualization that includes all the changes you've made. The system provides you with  data that you can copy and then paste into an email to collaborators.<br/><br/> Try it! Click Export and copy the presented data.<br/><br/> Then, in the new window that opens when you click <a target='_blank' href='" + GraphImporterExporter.SavedGraph.getUrlIFrameOrNot() + "'>here</a>, paste the data into the box provided there.",
                    onbeforechange: function () {
                        _this.showSubMenuIfNotVisible($("#" + GraphImporterExporter.Widget.innerContainerId), true);
                    },
                    position: "left"
                },
                {
                    intro: "<h1 class='introjs-header'>All Done! Questions?</h1> If you have suggestions or comments, contact us at biomixer.chisel@gmail.com  <br/><br/> More documentation is available on the <a target='_blank' href='http://www.bioontology.org/wiki/index.php/Visualizing_Concepts_and_Mappings'>Bioportal Wiki</a>.",
                    position: "bottom"
                },
            ];
        };
        return Tour;
    })();
    exports.Tour = Tour;
});
