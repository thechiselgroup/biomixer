///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />
///<reference path="headers/introjs.d.ts" />

///<amd-dependency path="JQueryExtension" />

///<amd-dependency path="Utils" />
///<amd-dependency path="Menu" />
///<amd-dependency path="Concepts/ConceptPathsToRoot" />
///<amd-dependency path="Concepts/ConceptLayouts" />
///<amd-dependency path="Concepts/GraphImporterExporter" />


import Utils = require("../Utils");
import Menu = require("../Menu");
import PathsToRoot = require("./ConceptPathsToRoot");
import ConceptLayouts = require("./ConceptLayouts");
import GraphImporterExporter = require("./GraphImporterExporter");

declare var introJs;
//declare var hopscotch;

interface GeneralTourStep {
    target?: Element;
    text: string;
    position?: string;
    nextMethod?: ()=>void;
}

/**
 * Using intro.js, but keepign hopscotch around for a while in case we need to re-investigate it as
 * an alternative. Feel free to get rid of it later though.
 */
export class Tour {

    private tourButtonId = "tourButton";
    
    // This is really just self documentation. Don't bother using this object for anything.
    // Make an array of objects like this to define all the steps for a given tour.
    private introElementAttributes = {
        element: "htmlElementId",
        intro: "Our tour step text",
        step: 0, // step index number, likely reflected in order of steps in array
        tooltipClass: "cssClassForTip",
        highlightClass: "cssClassForHighlightedTargetElement",
        position: "bottom", // Position relative to target top, left, right, bottom-left-aligned, bottom-right-aligned, bottom-middle-aligned
    };
    
    private introJsSettings = {
        /* Next button label in tooltip box */
        nextLabel: 'Next &rarr;',
        /* Previous button label in tooltip box */
        prevLabel: '&larr; Back',
        /* Skip button label in tooltip box */
        skipLabel: 'Skip',
        /* Done button label in tooltip box */
        doneLabel: 'Done',
        /* Default tooltip box position */
        tooltipPosition: 'auto', // bottom
        /* Next CSS class for tooltip boxes */
        tooltipClass: '',
        /* CSS class that is added to the helperLayer */
        highlightClass: 'nonVisibleIntroJsHelperLayer', // '',
        /* Close introduction when pressing Escape button? */
        exitOnEsc: true,
        /* Close introduction when clicking on overlay layer? */
        exitOnOverlayClick: false, // true
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
        disableInteraction: false
    };
    
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
    
    constructor(
        public pathsToRoot: PathsToRoot.ConceptPathsToRoot,
        public menu: Menu.Menu
    ){
        
    }
    
    initializeMenu(){
//        var hopscotchTourButton = $("<label>")
//            .attr("id", this.tourButtonId)
//            .attr("class", "nodeCommandButton")
//            .addClass("unselectable")
//            .addClass(Menu.Menu.topBarButtonClass)  
//            .text("Take a Hopscotch Tour!")
//        ;
        
        var introJsTourButton = $("<label>")
            .attr("id", this.tourButtonId)
            .attr("class", "nodeCommandButton")
            .addClass("unselectable")
            .addClass(Menu.Menu.topBarButtonClass)  
            .text("Take an Intro.js Tour!")
        ;
        
//        $(Menu.Menu.menuBarSelector).append(hopscotchTourButton);
        $(Menu.Menu.menuBarSelector).append(introJsTourButton);
        
//        hopscotchTourButton.click(
//            (event)=>{
//                event.stopPropagation();
//                this.startHopscotch();
//            }
//        );
        
        introJsTourButton.click(
            (event)=>{
                event.stopPropagation();
                this.startIntro();
            }
        );
    }
    
    closeMenuIfVisible(){
        this.menu.closeMenuLambda()();
    }
    
    showMenuIfNotVisible(){
        this.menu.openMenu();
    }
    
    showSubMenuIfNotVisible(childMenuItem: JQuery, collapseOthers: boolean = false){
        this.showMenuIfNotVisible();
        if(collapseOthers){
            var panels = $("."+Menu.Menu.hidingMenuContainerClass);
            panels.each(
                (i: number, menuItem: Element)=>{
                    if(childMenuItem[0] === menuItem){
                        return;
                    }
                    var label = $(menuItem).siblings("."+Menu.Menu.menuItemExpanderLabelClass);
                    if(label.hasClass("menuLabelIconCloseAction")){
                        label.click(); // label[class$='']
                    }            
                }
            );
        }
        // Find the parent menu collapsible section for the element,
        // and ensure it is expanded. Perhaps collapse the others?
        var label = childMenuItem.siblings("."+Menu.Menu.menuItemExpanderLabelClass);
        if(!label.hasClass("menuLabelIconCloseAction")){
            label.click(); // label[class$='']
        }
    }
    
    private getLibrarySpecificSteps(elementKey: string, textKey: string, positionKey: string, nextMethodKey: string, optionalCustom?: Array<string>){
        var steps = [];
        var generalSteps = this.getSteps();
        
        var first = 0;
        var max = 99;
        
        for(var i = 0; i < generalSteps.length; i++){
            if(i < first - 1){
                continue;
            }
            if(i > max - 1){
                break;
            }
            var step: GeneralTourStep = generalSteps[i];
            var newStep = {};
            if(null != step.target){
                newStep[elementKey] = step.target;
            }
            if(null != step.text){
                newStep[textKey] = step.text;
            }
            if(null != step.position){
                newStep[positionKey] = step.position;
            }
            if(null != step.nextMethod){
                newStep[nextMethodKey] = step.nextMethod;
            }
            if(optionalCustom != null){
                for(var j = 0; j < optionalCustom.length; j++){
                    var key = optionalCustom[j];
                    if(null != step[key]){
                        newStep[key] = step[key];
                    }
                }
            }
            steps.push(newStep);
        }
        return steps;
    }
    
//    startHopscotch(){
//        var tourSteps = this.getLibrarySpecificSteps("target", "content", "placement", "onNext");
//        
//        // Ok, I will hack it here, grabbing the next element's onNext, and making it the previous element's
//        // onNext. It makes sense, and I don't have to maintain independent changes from the library.
//        // It is also far more convenient than defining each step's onNext inside the data element of the preceding
//        // step, since that would create dependencies in the definition and complicate maintenance.
//        for(var i = 0; i < tourSteps.length - 1; i++){
//            var aStep = tourSteps[i];
//            var bStep = tourSteps[i+1];
//            if(bStep.onNext !== null){
//                aStep.onNext =bStep.onNext;
//                bStep.onNext = null; 
//            }
//            if(aStep.target == null){
//                aStep.target = $("body")[0];
//            }
//        }
//        
//        // Define the tour!
//        var tour = {
//          id: "hello-hopscotch",
//          steps: tourSteps
//        };
//        
//        // Is this actually necessary? It appeared to fix some problems with steps not occurring depending
//        // on the previous run, even when the browser was refreshed! Cookies business...
//        hopscotch.endTour(true);
//        
//        // Start the tour!
//        hopscotch.configure(Tour.hopscotchSettings);
//        
//        hopscotch.startTour(tour);
//        
//    }
    
    startIntro(){
    	// I needed to modify introjs to allow per-event next step callbacks. I also needed to get rid of the 
    	// shading and spotlight layers to allow clicking on objects.
        var tourSteps = this.getLibrarySpecificSteps("element", "intro", "position", "onbeforechange", ["overlayOpacity"]);
        
        var intro = introJs();
        intro.setOptions(this.introJsSettings);
          
        // We can probably always use the presets for the main settings,
        // then provide the step settings afterwards. Makes it easier to
        // have multiple tour types or stages of tours.
        var tour = {
            steps: tourSteps
        };
        intro.setOptions(tour);
        
        intro.start();
        
    }
    
    private getSteps(): Array<GeneralTourStep>{
        // hopscotch is content for target for element selection, text, placement for positioning, and onNext for methods.
        // intro.js is element for element selection, intro for text, position for positioning, and onbeforechange for methods.
        return [
            {
                
            text: "Welcome to the visualization tour. We advise that you use the tour in a freshly loaded visualization.",
            nextMethod: ()=>{ this.closeMenuIfVisible(); },
            position: "bottom"
            },
            
//- three base modes: Path to Root, Term Neighborhood, and Mapping Neighborhood
            {
            target: $(PathsToRoot.ConceptPathsToRoot.VIZ_SELECTOR_ID)[0],
            text: "The visualization begins differently depending on which mode it starts in. Feel free to click and see what is available. Click Next when you are ready.",
            position: "bottom"
            },
//- node expansions to add related nodes. Each node has a drop down menu attached, allowing the user to add either related terms in the same ontology, or mapped terms in other ontologies. Node expansion is optionally capped when many nodes already exist in the graph. An estimate of the number of available related nodes is given.
             {
            target: $("rect[id^='node_expander_indicator']")[0],
            text: "New concepts that are related to a concept in the graph can be added by clicking the menu on any node. Please click the highlighted menu to proceed.",
            position: "top"
            },
//             {
//            target: $($("#expanderMenuItemConceptExpander"),
//            text: "",
//            nextMethod: ()=>{}
//            },
                // TODO Make this a two step portion, getting user to expand menu, then we focus on it, and they click the expander
                
//- a menu button in the top right corner toggles the side bar menu visibility
             {
            target: $("#"+Menu.Menu.triggerId)[0],
            text: "The menu allows access to most other functionality. Click it to see more.",
            nextMethod: ()=>{ this.closeMenuIfVisible(); },
            position: "left",
            },
//- individual nodes from any ontology may be added if their unique URI is provided to a field in the menu
             {
            target: $("#singleNodeImportMessageBoxTextArea")[0],
            text: "You can add a specific node based on its ID, possibly unrelated to those already in the graph. These are easily found through the main Bioportal search interface. If it's not already in the graph, try adding http://purl.org/stemnet/MHC#Mouse",
            nextMethod: ()=>{ this.showSubMenuIfNotVisible($("#nodeFinderMenuContainerScrollContainer"), true); },
            position: "left"
            },
//           {
//            target: haven't added thsi feature yet),
//            text: "You can also try finding a distant node by searching by name, within a specific ontology.",
//            position: "bottom"
//            },
//- six layout algorithms available in menu (Center, Circle, Force-Directed, Vertical Tree, Horizontal Tree, and Radial layouts)
             {
            target: $("#"+ConceptLayouts.ConceptLayouts.layoutMenuContainerId)[0],
            text: "There are six layout algorithms that can help untangle relationships. Try them out.",
            nextMethod: ()=>{ this.showMenuIfNotVisible(); },
            position: "bottom"
            },
            
//- undo and redo: both buttons and drop down lists. Can step between any set of changes to the graph's node population, but not things like layouts or display customization
             {
            target: $("#redo_list_button")[0],
            text: "Changes to the graph's composition can be undone, and redone. You cannot undo node movements, though. Try going back and forth a bit.",
            position: "bottom"
            },
            
//- node details are accessible by clicking on the node. A single popup is displayed at a time, until the user clicks anywhere else in the graph.
             {
            target: $(".centralNode").parent()[0],
            text: "Node details can be seen if you click on a node. The popup stays visible until you click somewhere else. Try it!",
            position: "bottom"
            },
             {
//            target: $(".node_container")[0],
            text: "Try clicking in some white space to make the info box disappear, if you have not already.",
//            position: "bottom"
            },
//- hovering over arcs triggers a popup indicating the endpoint node names and the relation type
             {
//            target: $(".link_container")[0],
            text: "Hovering the mouse carefully over the links between nodes will provide more details about their relationship. Try it out.",
//            position: "bottom"
            },
//- node dragging to reposition individual nodes
             {
//            target: $(ConceptLayouts.ConceptLayouts.layoutMenuContainerId)[0],
            text: "Try dragging nodes around to reposition them.",
//            position: "bottom"
            },

//- search for nodes in a dense graph, via field in menu. Will trigger radar blips around each matching node
             {
            target: $("#nodeNameSearchButton")[0],
            text: "A node included in the graph can be visually located by searching for part of its name or synonym in this box. Try it out.",
            nextMethod: ()=>{ console.log("here though"); this.showSubMenuIfNotVisible($("#nodeFinderMenuContainerScrollContainer"), true); },
            position: "left"
            },
//- sharing of graphs via export and import, via the menu. Export provides json code that another user may paste into the import field, and receive the nodes, their positions and any custom edge colors, that existed at the time of export. Undo/redo state is not transferred.
             {
            target: $("#"+GraphImporterExporter.Widget.outerContainerId)[0],
            text: "You can share a graph and layout you have constructed. The export data provided to you can be emailed or otherwise sent to collaborators. Try Exporting the current graph, and Importing it into the new window that opens when you click <a href='"+GraphImporterExporter.SavedGraph.getUrlIFrameOrNot()+"'>here</a>",
            nextMethod: ()=>{ console.log("before or after anchor one?"); this.showSubMenuIfNotVisible($("#"+GraphImporterExporter.Widget.innerContainerId), true); },
            position: "left"
            },
//- filter edge types. Concepts can have different types of relations between them, depending on the ontology, and these may be hidden or re-colored via the menu.
             {
            target: $("#ConceptEdgeTypeFilterOuterContainer")[0],
            text: "To focus on certain types of relationships or to visually simplify a graph, try filtering edges out.",
            nextMethod: ()=>{ this.showSubMenuIfNotVisible($("#ConceptEdgeTypeFilterScrollContainer"), true); },
            position: "left"
            },
//- filter/di nodes by ontology; can dim out nodes based on their ontology
             {
            
            text: "For the next step, I need you to click some node's menu, and do a Mapping Expansion if there are about 5 available. It shows you the number to expand in parentheses. When you have done that, click next.",
            position: "bottom"
            },
            {
            target: $("#NestedOntologyConceptFilterOuterContainer")[0],
            text: "When there are concepts from multiple ontologies present, you can hide them based on their ontology. Try hiding all the nodes in an ontology. Also try clicking the checkboxes with concept names to hide only a few concepts.",
            nextMethod: ()=>{ this.showSubMenuIfNotVisible($("#NestedOntologyConceptFilterScrollContainer"), true); },
            position: "left"
            },
//- remove all dimmed/filtered nodes from the graph view (can undo/redo this)
             {
            target: $("#NestedOntologyConceptFilterCheckboxResetButton")[0],
            text: "You can re-check all those checkboxes at once with this button",
            nextMethod: ()=>{ this.showSubMenuIfNotVisible($("#NestedOntologyConceptFilterScrollContainer"), true); },
            position: "top"
            },
//- remove all node filters with button in menu
             {
            target: $("#NestedOntologyConceptFilterCheckboxDeleteButton")[0],
            text: "And you can delete all the hidden nodes with this button. Try hiding one node, then pressing the button. This may be undone with the undo button visited at another step in this tour.",
            nextMethod: ()=>{ this.showSubMenuIfNotVisible($("#NestedOntologyConceptFilterScrollContainer"), true); },
            position: "top"
            },
//- filter/dim individual nodes; can dim out individual nodes arbitrarily. can also be accessed via each node's drop down menu, as well as in each node's detailed popup box.
//            {
//            target: $(".centralNode").children("rect[id^='"+PathsToRoot.ConceptPathsToRoot.NODE_EXPANDER_INDICATOR_ID_PREFIX+"']")[0],
//            text: "Nodes have individual node menus. Click one now, but don't click the options in it yet.",
//            position: "bottom"
//            },
             {
            
            text: "Nodes can also be filtered from their node menus, or from their tool tips. Click a node to see the tool tip, and toggle the checkbox there. Thenm click the menu beneath a node to see a 'hide' option. ",
            position: "bottom"
            },
//- filter/dim expansion sets, groups of nodes that were added to the graph together, usually via a node expansion
             {
            
            text: "For the next step, I need you to click some node's menu, and do an expansion if there are about 5 available. It shows you the number to expand in parentheses. When you have done that, click next.",
            position: "bottom"
            },
             {
            target: $("#NestedExpansionSetConceptFilterOuterContainer")[0],
            text: "You just expanded a node, and a bunch of related nodes were added to the graph. This is called an Expansion Set. You can see these groupings here in the menu. You can hide entire sets, or individual nodes, the same way you can work with ontology filters.",
            nextMethod: ()=>{ this.showSubMenuIfNotVisible($("#NestedExpansionSetConceptFilterScrollContainer", true)); },
            position: "left"
            },

//- hovering over a node in the graph will highlight it and the nodes that are connected to it
             {
            
            text: "You may have noticed, but hovering over any node highlights the nodes related to it.",
            position: "bottom"
            },
//            
////- hovering over any of the node filtering checkboxes will highlight the corresponding nodes in the graph
//             {
//            target: $("#NestedOntologyConceptFilterOuterContainer")[0],
//            text: "You can also hover over checkboxes to highlight all the nodes corresponding to them.",
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
            
            text: "If you have suggestions or comments, contact us at biomixer.chisel@gmail.com",
                position: "bottom"
            },
            ]
        ;   
    }
}