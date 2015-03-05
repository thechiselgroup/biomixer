///<reference path="headers/require.d.ts" />
define(["require", "exports", "../Menu", "./ConceptPathsToRoot", "./ConceptLayouts", "./GraphImporterExporter", "JQueryExtension", "Utils", "Menu", "Concepts/ConceptPathsToRoot", "Concepts/ConceptLayouts", "Concepts/GraphImporterExporter"], function(require, exports, Menu, PathsToRoot, ConceptLayouts, GraphImporterExporter) {
    

    /**
    * Using intro.js, but keepign hopscotch around for a while in case we need to re-investigate it as
    * an alternative. Feel free to get rid of it later though.
    */
    var Tour = (function () {
        //    static hopscotchSettings = {
        //        id: "hopscotchTour",
        //        bubbleWidth: 280,
        //        bubblePadding: 15,
        //        unsafe: true, // whether to run through HTML escapes or not
        //        smoothScroll: true,
        //        scrollDuration: 1000,
        //        scrollTopMargin: 200,
        //        showCloseButton: true,
        //        showPrevButton: false,
        //        showNextButton: true,
        //        arrowWidth: 20,
        //        skipIfNoElement: true,
        //        nextOnTargetClick: false,
        //        onNext: null,
        //        onPrev: null,
        //        onStart: null,
        //        onEnd: ()=>{ hopscotch.endTour(true) }, // null,
        //        onClose: null,
        //        onError: null,
        //        //i18n [OBJECT] - For i18n purposes. Allows you to change the text of button labels and step numbers.
        //        //i18n.nextBtn [STRING] - Label for next button
        //        //i18n.prevBtn [STRING] - Label for prev button
        //        //i18n.doneBtn [STRING] - Label for done button
        //        //i18n.skipBtn [STRING] - Label for skip button
        //        //i18n.closeTooltip [STRING] - Text for close button tooltip
        //        //i18n.stepNums [ARRAY] - Provide a list of strings to be shown as the step number, based on index of array. Unicode characters are supported. (e.g., Japanese kanji numerals) If there are more steps than provided numbers, Arabic numerals ('4', '5', '6', etc.) will be used as default.
        //    };
        function Tour(pathsToRoot, menu) {
            this.pathsToRoot = pathsToRoot;
            this.menu = menu;
            this.tourButtonId = "tourButton";
            // This is really just self documentation. Don't bother using this object for anything.
            // Make an array of objects like this to define all the steps for a given tour.
            this.introElementAttributes = {
                element: "htmlElementId",
                intro: "Our tour step text",
                step: 0,
                tooltipClass: "cssClassForTip",
                highlightClass: "cssClassForHighlightedTargetElement",
                position: "bottom"
            };
            this.introJsSettings = {
                /* Next button label in tooltip box */
                nextLabel: 'Next &rarr;',
                /* Previous button label in tooltip box */
                prevLabel: '&larr; Back',
                /* Skip button label in tooltip box */
                skipLabel: 'Stop',
                /* Done button label in tooltip box */
                doneLabel: 'Done',
                /* Default tooltip box position */
                tooltipPosition: 'auto',
                /* Next CSS class for tooltip boxes */
                tooltipClass: '',
                /* CSS class that is added to the helperLayer */
                highlightClass: 'nonVisibleIntroJsHelperLayer',
                /* Close introduction when pressing Escape button? */
                exitOnEsc: true,
                /* Close introduction when clicking on overlay layer? */
                exitOnOverlayClick: false,
                /* Show step numbers in introduction? */
                showStepNumbers: false,
                /* Let user use keyboard to navigate the tour? */
                keyboardNavigation: true,
                /* Show tour control buttons? */
                showButtons: true,
                /* Show tour bullets? */
                showBullets: false,
                /* Show tour progress? */
                showProgress: true,
                /* Scroll to highlighted element? */
                scrollToElement: false,
                /* Set the overlay opacity */
                overlayOpacity: 0.8,
                /* Precedence of positions, when auto is enabled */
                positionPrecedence: ["bottom", "top", "right", "left"],
                /* Disable an interaction with element? */
                disableInteraction: false,
                /* Drag by handle on lower left corner */
                dragByLowerLeftHandle: true
            };
        }
        Tour.prototype.initializeMenu = function () {
            //        var hopscotchTourButton = $("<label>")
            //            .attr("id", this.tourButtonId)
            //            .attr("class", "nodeCommandButton")
            //            .addClass("unselectable")
            //            .addClass(Menu.Menu.topBarButtonClass)
            //            .text("Take a Hopscotch Tour!")
            //        ;
            var _this = this;
            var introJsTourButton = $("<label>").attr("id", this.tourButtonId).attr("class", "nodeCommandButton").addClass("unselectable").addClass(Menu.Menu.topBarButtonClass).text("Take a Tour!");

            //        $(Menu.Menu.menuBarSelector).append(hopscotchTourButton);
            $(Menu.Menu.menuBarSelector).append(introJsTourButton);

            //        hopscotchTourButton.click(
            //            (event)=>{
            //                event.stopPropagation();
            //                this.startHopscotch();
            //            }
            //        );
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
            if (typeof collapseOthers === "undefined") { collapseOthers = false; }
            this.showMenuIfNotVisible();
            if (collapseOthers) {
                var panels = $("." + Menu.Menu.hidingMenuContainerClass);
                panels.each(function (i, menuItem) {
                    if (childMenuItem[0] === menuItem) {
                        return;
                    }
                    var label = $(menuItem).siblings("." + Menu.Menu.menuItemExpanderLabelClass);
                    if (label.hasClass(Menu.Menu.closeActionClass)) {
                        label.click(); // label[class$='']
                    }
                });
            }

            // Find the parent menu collapsible section for the element,
            // and ensure it is expanded. Perhaps collapse the others?
            var label = childMenuItem.siblings("." + Menu.Menu.menuItemExpanderLabelClass);
            if (!label.hasClass(Menu.Menu.closeActionClass)) {
                // The animation for the menu takes a moment, so after a delay, refresh the tour toget the outline positioned
                // more accurately.
                label.trigger("click", function () {
                    _this.refreshIntro();
                });
            }
        };

        Tour.prototype.getLibrarySpecificSteps = function (elementKey, textKey, positionKey, nextMethodKey, optionalCustom) {
            var steps = [];
            var generalSteps = this.getSteps();

            var first = 0;
            var max = 99;

            for (var i = 0; i < generalSteps.length; i++) {
                if (i < first - 1) {
                    continue;
                }
                if (i > max - 1) {
                    break;
                }
                var step = generalSteps[i];
                var newStep = {};
                if (null != step.target) {
                    newStep[elementKey] = step.target;
                }
                if (null != step.text) {
                    newStep[textKey] = step.text;
                }
                if (null != step.position) {
                    newStep[positionKey] = step.position;
                }
                if (null != step.nextMethod) {
                    newStep[nextMethodKey] = step.nextMethod;
                }
                if (optionalCustom != null) {
                    for (var j = 0; j < optionalCustom.length; j++) {
                        var key = optionalCustom[j];
                        if (null != step[key]) {
                            newStep[key] = step[key];
                        }
                    }
                }
                steps.push(newStep);
            }
            return steps;
        };

        Tour.prototype.startIntro = function () {
            // I needed to modify introjs to allow per-event next step callbacks. I also needed to get rid of the
            // shading and spotlight layers to allow clicking on objects.
            var tourSteps = this.getLibrarySpecificSteps("element", "intro", "position", "onbeforechange", ["overlayOpacity"]);

            this.intro = introJs();
            this.intro.setOptions(this.introJsSettings);

            // We can probably always use the presets for the main settings,
            // then provide the step settings afterwards. Makes it easier to
            // have multiple tour types or stages of tours.
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
            // hopscotch is content for target for element selection, text, placement for positioning, and onNext for methods.
            // intro.js is element for element selection, intro for text, position for positioning, and onbeforechange for methods.
            return [
                {
                    text: "<h1 class='introjs-header'>Welcome</h1> This will take you on an interactive tour of the visualization tools. We recommend you start the tour in a <a href='#' onclick='location.reload(); return false;' > freshly loaded visualization </a> that you haven't manipulated. <br/><br/> More documentation is available on the <a target='_blank' href='http://www.bioontology.org/wiki/index.php/Visualizing_Concepts_and_Mappings'>Bioportal Wiki</a>.",
                    nextMethod: function () {
                        _this.closeMenuIfVisible();
                    },
                    position: "bottom"
                },
                //- three base modes: Path to Root, Term Neighborhood, and Mapping Neighborhood
                {
                    target: $(PathsToRoot.ConceptPathsToRoot.VIZ_SELECTOR_ID)[0],
                    text: "<h1 class='introjs-header'>Graph Modes</h1> The initial display of a visualization varies depending on which mode it starts in. Additional terms may be added by interacting with the graph, but we will explore these starting modes first.",
                    position: "bottom"
                },
                {
                    target: $(PathsToRoot.ConceptPathsToRoot.VIZ_SELECTOR_ID)[0],
                    text: "<h1 class='introjs-header'>Graph Modes: Term Neighborhood</h1> Try clicking and selecting 'Term Neighborhood'.<br/><br/> This mode initializes the graph with the terms that are directly related to the target term, and which occur in the same ontology.",
                    position: "bottom"
                },
                {
                    target: $(PathsToRoot.ConceptPathsToRoot.VIZ_SELECTOR_ID)[0],
                    text: "<h1 class='introjs-header'>Graph Modes: Mapping Neighborhood</h1> Try clicking and selecting 'Mapping Neighborhood'.<br/><br/> This mode initializes the graph with terms from other ontologies which have been mapped together; they represent the same concept in separate ontologies.",
                    position: "bottom"
                },
                {
                    target: $(PathsToRoot.ConceptPathsToRoot.VIZ_SELECTOR_ID)[0],
                    text: "<h1 class='introjs-header'>Graph Modes: Paths to Root</h1> Try clicking and selecting 'Path to Root'.<br/><br/> This mode shows all terms between the target node and the root node of its ontology.",
                    position: "bottom"
                },
                //- a menu button in the top right corner toggles the side bar menu visibility
                {
                    target: $("#" + Menu.Menu.triggerId)[0],
                    text: "<h1 class='introjs-header'>Main Menu</h1> This menu provides access to a variety of more advanced visualization tools. We will explore this later. Let's cover some basics first.",
                    nextMethod: function () {
                        _this.closeMenuIfVisible();
                    },
                    position: "left"
                },
                //- node details are accessible by clicking on the node. A single popup is displayed at a time, until the user clicks anywhere else in the graph.
                {
                    target: $(".centralNode").parent()[0],
                    text: "<h1 class='introjs-header'>Node Details</h1> Click a node to display more information about a concept or mapping. The popup remains visible until you click somewhere else. Try it!",
                    position: "bottom"
                },
                {
                    //            target: $(".node_container")[0],
                    text: "If the popup is still open, try clicking the white space. This will make the popup disappear."
                },
                //- hovering over a node in the graph will highlight it and the nodes that are connected to it
                {
                    text: "<h1 class='introjs-header'>Hovering over Nodes</h1> Hover over a node to highlight the concept or mapping's immediate parent and children.",
                    position: "bottom"
                },
                //- hovering over arcs triggers a popup indicating the endpoint node names and the relation type
                {
                    //            target: $(".link_container")[0],
                    text: "<h1 class='introjs-header'>Hovering over Links</h1> Hover (carefully) over the line or arrow between two nodes to display more details about their relationship."
                },
                //- node dragging to reposition individual nodes
                {
                    //            target: $(ConceptLayouts.ConceptLayouts.layoutMenuContainerId)[0],
                    text: "Drag nodes to reposition them."
                },
                //- search for nodes in a dense graph, via field in menu. Will trigger radar blips around each matching node
                {
                    target: $("#findNodeInputBox")[0],
                    text: "<h1 class='introjs-header'>Node Radar</h1> Highlight a concept or mapping in a visualization by searching for part of its name or synonym using the Locate Node tool.",
                    nextMethod: function () {
                        _this.showSubMenuIfNotVisible($("#nodeFinderMenuContainerScrollContainer"), true);
                    },
                    position: "left"
                },
                //- node expansions to add related nodes. Each node has a drop down menu attached, allowing the user to add either related terms in the same ontology, or mapped terms in other ontologies. Node expansion is optionally capped when many nodes already exist in the graph. An estimate of the number of available related nodes is given.
                {
                    target: $("rect[id^='node_expander_indicator']")[0],
                    text: "<h1 class='introjs-header'>Node Expansion Menus</h1> Display other related concepts and mappings by clicking the drop-down arrow beneath a node.",
                    position: "top"
                },
                //             {
                //            target: $($("#expanderMenuItemConceptExpander"),
                //            text: "",
                //            nextMethod: ()=>{}
                //            },
                // TODO Make this a two step portion, getting user to expand menu, then we focus on it, and they click the expander
                //- individual nodes from any ontology may be added if their unique URI is provided to a field in the menu
                {
                    target: $("#singleNodeImportMessageBoxTextArea")[0],
                    text: "<h1 class='introjs-header'>Single Node Import</h1> You can add a specific concept or mapping based on its BioPortal ID, even ones that are unrelated to those already in the visualization. These are easily found through the main Bioportal search interface. For example, try adding http://purl.org/stemnet/MHC#Mouse (if not already in the visualization).",
                    nextMethod: function () {
                        _this.showSubMenuIfNotVisible($("#nodeFinderMenuContainerScrollContainer"), true);
                    },
                    position: "left"
                },
                //           {
                //            target: haven't added thsi feature yet),
                //            text: "You can also try finding a distant node by searching by name, within a specific ontology.",
                //            position: "bottom"
                //            },
                //- six layout algorithms available in menu (Center, Circle, Force-Directed, Vertical Tree, Horizontal Tree, and Radial layouts)
                {
                    target: $("#" + ConceptLayouts.ConceptLayouts.layoutMenuContainerId)[0],
                    text: "<h1 class='introjs-header'>Layouts</h1> There are six layout algorithms that can help untangle relationships. Try them out.",
                    nextMethod: function () {
                        _this.showMenuIfNotVisible();
                    },
                    position: "bottom"
                },
                //- undo and redo: both buttons and drop down lists. Can step between any set of changes to the graph's node population, but not things like layouts or display customization
                {
                    target: $("#undo_redo_breadcrumb_trail")[0],
                    text: "<h1 class='introjs-header'>Undo/Redo</h1> Changes to a visualization can be undone and reapplied. However, you cannot undo node movements (nodes you dragged to a new location). Try playing with both the undo/redo arrows, as well as the drop-down arrows (which display a list of the changes you've made).",
                    position: "bottom"
                },
                //- filter/dim nodes by ontology; can dim out nodes based on their ontology
                {
                    text: "<h1 class='introjs-header'>Adding Nodes from Other Ontologies</h1> For the next step, click a node's drop-down arrow and select Expand Mappings (if there are about 5 available--it shows you the number of available mappings in parentheses). When you have done that, click Next.",
                    position: "bottom"
                },
                {
                    target: $("#NestedOntologyConceptFilterOuterContainer")[0],
                    text: "<h1 class='introjs-header'>Filtering and Dimming Nodes</h1> When a visualization contains concepts from multiple ontologies, you can hide nodes based on their ontology. Try hiding all the nodes in an ontology. Also try clicking the check boxes with concept names to hide only a few concepts.",
                    nextMethod: function () {
                        _this.showSubMenuIfNotVisible($("#NestedOntologyConceptFilterScrollContainer"), true);
                    },
                    position: "left"
                },
                //- remove all dimmed/filtered nodes from the graph view (can undo/redo this)
                {
                    target: $("#NestedOntologyConceptFilterCheckboxResetButton")[0],
                    text: "You can re-select all those check boxes at once with this button.",
                    nextMethod: function () {
                        _this.showSubMenuIfNotVisible($("#NestedOntologyConceptFilterScrollContainer"), true);
                    },
                    position: "top"
                },
                //- remove all node filters with button in menu
                {
                    target: $("#NestedOntologyConceptFilterCheckboxDeleteButton")[0],
                    text: "<h1 class='introjs-header'>Deleting Nodes</h1> And you can delete all the hidden nodes with this button. Try hiding one node, then pressing the button. This may be undone with the undo tools previously covered in this tour.",
                    nextMethod: function () {
                        _this.showSubMenuIfNotVisible($("#NestedOntologyConceptFilterScrollContainer"), true);
                    },
                    position: "top"
                },
                //- filter/dim individual nodes; can dim out individual nodes arbitrarily. can also be accessed via each node's drop down menu, as well as in each node's detailed popup box.
                //            {
                //            target: $(".centralNode").children("rect[id^='"+PathsToRoot.ConceptPathsToRoot.NODE_EXPANDER_INDICATOR_ID_PREFIX+"']")[0],
                //            text: "Nodes have individual node menus. Click one now, but don't click the options in it yet.",
                //            position: "bottom"
                //            },
                {
                    text: "<h1 class='introjs-header'>More Node Filtering Controls: Tool Tip</h1> Nodes can also be filtered from the tool tip. Click a node to see the tool tip and toggle the check box there.",
                    position: "bottom"
                },
                {
                    text: "<h1 class='introjs-header'>More Node Filtering Controls: Node Menu</h1> In addition, nodes can also be filtered from the node menu. Click the drop-down arrow beneath a node to see the 'Hide Node' option.",
                    position: "bottom"
                },
                //- filter/dim expansion sets, groups of nodes that were added to the graph together, usually via a node expansion
                {
                    text: "<h1 class='introjs-header'>Creating Expansion Sets</h1> For the next step, click a node's drop-down arrow and select Expand Concepts (if there are about 5 available--it shows you the number of available concepts in parentheses). When you have done that, click Next.",
                    position: "bottom"
                },
                {
                    target: $("#NestedExpansionSetConceptFilterOuterContainer")[0],
                    text: "<h1 class='introjs-header'>Filtering Expansion Sets</h1> You just expanded a concept and the system added a number of related nodes to the visualization. This is called an Expansion Set. You can see these groupings in the menu on the right-hand side. You can hide entire sets or individual nodes--the same way you can work with ontology filters.",
                    nextMethod: function () {
                        _this.showSubMenuIfNotVisible($("#NestedExpansionSetConceptFilterScrollContainer"), true);
                    },
                    position: "left"
                },
                //- filter edge types. Concepts can have different types of relationships between them, depending on the ontology, and these may be hidden or re-colored via the menu.
                {
                    target: $("#ConceptEdgeTypeFilterOuterContainer")[0],
                    text: "<h1 class='introjs-header'>Edge Filters</h1> To focus on certain types of relationships, or to visually simplify a visualization, try filtering out the edges by toggling the check boxes.",
                    nextMethod: function () {
                        _this.showSubMenuIfNotVisible($("#ConceptEdgeTypeFilterScrollContainer"), true);
                    },
                    position: "left"
                },
                {
                    target: $("#ConceptEdgeTypeFilterOuterContainer")[0],
                    text: "Click the arrow (beside the check box) to assign a new color to that arc type.",
                    nextMethod: function () {
                        _this.showSubMenuIfNotVisible($("#ConceptEdgeTypeFilterScrollContainer"), true);
                    },
                    position: "left"
                },
                //- sharing of graphs via export and import, via the menu. Export provides json code that another user may paste into the import field, and receive the nodes, their positions and any custom edge colors, that existed at the time of export. Undo/redo state is not transferred.
                {
                    target: $("#" + GraphImporterExporter.Widget.outerContainerId)[0],
                    text: "<h1 class='introjs-header'>Sharing</h1> You can share a visualization that includes all the changes you've made. The system provides you with  data that you can copy and then paste into an email to collaborators.<br/><br/> Try it! Click Export and copy the presented data.<br/><br/> Then, in the new window that opens when you click <a target='_blank' href='" + GraphImporterExporter.SavedGraph.getUrlIFrameOrNot() + "'>here</a>, paste the data into the box provided there.",
                    nextMethod: function () {
                        _this.showSubMenuIfNotVisible($("#" + GraphImporterExporter.Widget.innerContainerId), true);
                    },
                    position: "left"
                },
                //
                ////- hovering over any of the node filtering check boxes will highlight the corresponding nodes in the graph
                //             {
                //            target: $("#NestedOntologyConceptFilterOuterContainer")[0],
                //            text: "You can also hover over check boxes to highlight all the nodes corresponding to them.",
                //            nextMethod: ()=>{ this.showSubMenuIfNotVisible($("#NestedOntologyConceptFilterOuterContainer")); },
                //            position: "bottom"
                //            },
                ////- since mapping arcs generally form densely connected webs, it only displays mapping arcs leading to a node for which a mapping expansion was triggered. Other mapping arcs are made visible whenever one of their endpoint nodes is hovered over.
                //             {
                //            target: $(ConceptLayouts.ConceptLayouts.layoutMenuContainerId)[0],
                //            text: "For the next step, if you don't have a few ontologies present, I need you to click some node's menu, and do a Mapping Expansion. When you have done that, click next.",
                //            position: "bottom"
                //            },
                //             {
                //            target: $(ConceptLayouts.ConceptLayouts.layoutMenuContainerId)[0],
                //            text: "Note that there are arcs drawn between the expanded mapped concepts and the node they were expanded from. If you hover over one of them, it will probably have arcs appear connecting it to the other mapped nodes. Hiding and revealing the arcs this way keeps the graph tidy.",
                //            position: "bottom"
                //            },
                ////- refocus the graph on a different node; this reloads the visualization in its current mode starting with the specified node (accessed via dropdown menu on each node)
                //            {
                //            target: $(ConceptLayouts.ConceptLayouts.layoutMenuContainerId)[0],
                //            text: "If you want to do a more drastic exploration or traversal of the graph, you can start the whole thing over using a different node. Try clicking a node menu, and selecting '"+PathsToRoot.ConceptPathsToRoot.REFOCUS_NODE_TEXT+"'.",
                //            position: "bottom"
                //            },
                {
                    text: "<h1 class='introjs-header'>All Done! Questions?</h1> If you have suggestions or comments, contact us at biomixer.chisel@gmail.com  <br/><br/> More documentation is available on the <a target='_blank' href='http://www.bioontology.org/wiki/index.php/Visualizing_Concepts_and_Mappings'>Bioportal Wiki</a>.",
                    position: "bottom"
                }
            ];
        };
        return Tour;
    })();
    exports.Tour = Tour;
});
