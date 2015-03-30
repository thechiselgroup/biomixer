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

interface GeneralTourStep {
    element?: Element;
    intro: string;
    position?: string;
    onbeforechange?: ()=>void;
}

/**
 * Using intro.js library for this, customized to have several new features (SVG compatible highlighting, function
 * defined targets).
 */
export class Tour {

    private tourButtonId = "tourButton";
    
    private introJsSettings = {
        /* Next button label in tooltip box */
        nextLabel: 'Next &rarr;',
        /* Previous button label in tooltip box */
        prevLabel: '&larr; Back',
        /* Skip button label in tooltip box */
        skipLabel: 'Stop',
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
        disableInteraction: false,
        /* Drag by handle on lower left corner */
        dragByLowerLeftHandle: true
    };

    
    constructor(
        public pathsToRoot: PathsToRoot.ConceptPathsToRoot,
        public menu: Menu.Menu
    ){
        
    }
    
    initializeMenu(){
        
        var introJsTourButton = $("<label>")
            .attr("id", this.tourButtonId)
            .attr("class", "nodeCommandButton")
            .addClass("unselectable")
            .addClass(Menu.Menu.topBarButtonClass)  
            .text("Take a Tour!")
        ;
        
        $(Menu.Menu.menuBarSelector).append(introJsTourButton);
        
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
                    if(label.hasClass(Menu.Menu.closeActionClass)){
                        label.click(); // label[class$='']
                    }            
                }
            );
        }
        // Find the parent menu collapsible section for the element,
        // and ensure it is expanded. Perhaps collapse the others?
        var label = childMenuItem.siblings("."+Menu.Menu.menuItemExpanderLabelClass);
        if(!label.hasClass(Menu.Menu.closeActionClass)){
            // The animation for the menu takes a moment, so after a delay, refresh the tour toget the outline positioned
            // more accurately.
            label.trigger("click", ()=>{this.refreshIntro();});
        }
        
    }
    
    intro;
    startIntro(){
        var tourSteps = this.getSteps();
        
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
        
    }
    
    refreshIntro(){
        if(null != this.intro){
            this.intro.refresh();  
        } 
    }
    
    private getSteps(): Array<GeneralTourStep>{
        // intro.js is element for element selection, intro for text, position for positioning, and onbeforechange for methods.
        return [
            {
                
            intro: "<h1 class='introjs-header'>Welcome</h1> This will take you on an interactive tour of the visualization tools. We recommend you start the tour in a <a href='#' onclick='location.reload(); return false;' > freshly loaded visualization </a> that you haven't manipulated. <br/><br/> More documentation is available on the <a target='_blank' href='http://www.bioontology.org/wiki/index.php/Visualizing_Concepts_and_Mappings'>Bioportal Wiki</a>.",
            onbeforechange: ()=>{ this.closeMenuIfVisible(); },
            position: "bottom"
            },
            
//- three base modes: Path to Root, Term Neighborhood, and Mapping Neighborhood
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
//- a menu button in the top right corner toggles the side bar menu visibility
             {
            element: $("#"+Menu.Menu.triggerId)[0],
            intro: "<h1 class='introjs-header'>Main Menu</h1> This menu provides access to a variety of more advanced visualization tools. We will explore this later. Let's cover some basics first.",
            onbeforechange: ()=>{ this.closeMenuIfVisible(); },
            position: "left",
            },
//- node details are accessible by clicking on the node. A single popup is displayed at a time, until the user clicks anywhere else in the graph.
             {
            element: ()=>{ return $(".centralNode").parent()[0] },
            intro: "<h1 class='introjs-header'>Node Details</h1> Click a node to display more information about a concept or mapping. The popup remains visible until you click somewhere else. Try it!",
            position: "bottom"
            },
             {
//            element: $(".node_container")[0],
            intro: "If the popup is still open, try clicking the white space. This will make the popup disappear.",
//            position: "bottom"
            },

//- hovering over a node in the graph will highlight it and the nodes that are connected to it
             {
            
            intro: "<h1 class='introjs-header'>Hovering over Nodes</h1> Hover over a node to highlight the concept or mapping's immediate parent and children.",
            position: "bottom"
            },
//- hovering over arcs triggers a popup indicating the endpoint node names and the relation type
             {
//            element: $(".link_container")[0],
            intro: "<h1 class='introjs-header'>Hovering over Links</h1> Hover (carefully) over the line or arrow between two nodes to display more details about their relationship.",
//            position: "bottom"
            },
//- node dragging to reposition individual nodes
             {
//            element: $(ConceptLayouts.ConceptLayouts.layoutMenuContainerId)[0],
            intro: "Drag nodes to reposition them.",
//            position: "bottom"
            },

//- search for nodes in a dense graph, via field in menu. Will trigger radar blips around each matching node
             {
            element: $("#findNodeInputBox")[0],
            intro: "<h1 class='introjs-header'>Node Radar</h1> Highlight a concept or mapping in a visualization by searching for part of its name or synonym using the Locate Node tool.",
            onbeforechange: ()=>{ this.showSubMenuIfNotVisible($("#nodeFinderMenuContainerScrollContainer"), true); },
            position: "left"
            },
//- node expansions to add related nodes. Each node has a drop down menu attached, allowing the user to add either related terms in the same ontology, or mapped terms in other ontologies. Node expansion is optionally capped when many nodes already exist in the graph. An estimate of the number of available related nodes is given.
             {
            element: ()=>{ return $("rect[id^='node_expander_indicator']")[0] },
            intro: "<h1 class='introjs-header'>Node Expansion Menus</h1> Display other related concepts and mappings by clicking the drop-down arrow beneath a node.",
            position: "top"
            },
//             {
//            element: $($("#expanderMenuItemConceptExpander"),
//            intro: "",
//            onbeforechange: ()=>{}
//            },
                // TODO Make this a two step portion, getting user to expand menu, then we focus on it, and they click the expander
                
//- individual nodes from any ontology may be added if their unique URI is provided to a field in the menu
             {
            element: $("#singleNodeImportMessageBoxTextArea")[0],
            intro: "<h1 class='introjs-header'>Single Node Import</h1> You can add a specific concept or mapping based on its BioPortal ID, even ones that are unrelated to those already in the visualization. These are easily found through the main Bioportal search interface. For example, try adding http://purl.org/stemnet/MHC#Mouse (if not already in the visualization).",
            onbeforechange: ()=>{ this.showSubMenuIfNotVisible($("#nodeFinderMenuContainerScrollContainer"), true); },
            position: "left"
            },
//           {
//            element: haven't added thsi feature yet),
//            intro: "You can also try finding a distant node by searching by name, within a specific ontology.",
//            position: "bottom"
//            },
//- six layout algorithms available in menu (Center, Circle, Force-Directed, Vertical Tree, Horizontal Tree, and Radial layouts)
             {
            element: $("#"+ConceptLayouts.ConceptLayouts.layoutMenuContainerId)[0],
            intro: "<h1 class='introjs-header'>Layouts</h1> There are six layout algorithms that can help untangle relationships. Try them out.",
            onbeforechange: ()=>{ this.showMenuIfNotVisible(); },
            position: "bottom"
            },
            
//- undo and redo: both buttons and drop down lists. Can step between any set of changes to the graph's node population, but not things like layouts or display customization
             {
            element: $("#undo_redo_breadcrumb_trail")[0],
            intro: "<h1 class='introjs-header'>Undo/Redo</h1> Changes to a visualization can be undone and reapplied. However, you cannot undo node movements (nodes you dragged to a new location). Try playing with both the undo/redo arrows, as well as the drop-down arrows (which display a list of the changes you've made).",
            position: "bottom"
            },
            

//- filter/dim nodes by ontology; can dim out nodes based on their ontology
             {
            
            intro: "<h1 class='introjs-header'>Adding Nodes from Other Ontologies</h1> For the next step, click a node's drop-down arrow and select Expand Mappings (if there are about 5 available--it shows you the number of available mappings in parentheses). When you have done that, click Next.",
            position: "bottom"
            },
            {
            element: $("#NestedOntologyConceptFilterOuterContainer")[0],
            intro: "<h1 class='introjs-header'>Filtering and Dimming Nodes</h1> When a visualization contains concepts from multiple ontologies, you can dim nodes based on their ontology. Try dimming all the nodes in an ontology. Also try clicking the check boxes with concept names to dim only a few concepts.",
            onbeforechange: ()=>{ this.showSubMenuIfNotVisible($("#NestedOntologyConceptFilterScrollContainer"), true); },
            position: "left"
            },
//- remove all dimmed/filtered nodes from the graph view (can undo/redo this)
             {
            element: $("#NestedOntologyConceptFilterCheckboxResetButton")[0],
            intro: "You can re-select all those check boxes at once with this button.",
            onbeforechange: ()=>{ this.showSubMenuIfNotVisible($("#NestedOntologyConceptFilterScrollContainer"), true); },
            position: "top"
            },
//- remove all node filters with button in menu
             {
            element: $("#NestedOntologyConceptFilterCheckboxDeleteButton")[0],
            intro: "<h1 class='introjs-header'>Excluding Nodes</h1> And you can exclude all the dimmed nodes with this button. Try dimming one node, then pressing the button. This may be undone with the undo tools previously covered in this tour.",
            onbeforechange: ()=>{ this.showSubMenuIfNotVisible($("#NestedOntologyConceptFilterScrollContainer"), true); },
            position: "top"
            },
//- filter/dim individual nodes; can dim out individual nodes arbitrarily. can also be accessed via each node's drop down menu, as well as in each node's detailed popup box.
//            {
//            element: $(".centralNode").children("rect[id^='"+PathsToRoot.ConceptPathsToRoot.NODE_EXPANDER_INDICATOR_ID_PREFIX+"']")[0],
//            intro: "Nodes have individual node menus. Click one now, but don't click the options in it yet.",
//            position: "bottom"
//            },
             {
            
            intro: "<h1 class='introjs-header'>More Node Filtering Controls: Tool Tip</h1> Nodes can also be filtered from the tool tip. Click a node to see the tool tip and toggle the check box there.",
            position: "bottom"
            },
            {
            
            intro: "<h1 class='introjs-header'>More Node Filtering Controls: Node Menu</h1> In addition, nodes can also be filtered from the node menu. Click the drop-down arrow beneath a node to see the 'Dim Node' option.",
            position: "bottom"
            },
//- filter/dim expansion sets, groups of nodes that were added to the graph together, usually via a node expansion
             {
            
            intro: "<h1 class='introjs-header'>Creating Expansion Sets</h1> For the next step, click a node's drop-down arrow and select Expand Concepts (if there are about 5 available--it shows you the number of available concepts in parentheses). When you have done that, click Next.",
            position: "bottom"
            },
             {
            element: $("#NestedExpansionSetConceptFilterOuterContainer")[0],
            intro: "<h1 class='introjs-header'>Filtering Expansion Sets</h1> You just expanded a concept and the system added a number of related nodes to the visualization. This is called an Expansion Set. You can see these groupings in the menu on the right-hand side. You can dim entire sets or individual nodes--the same way you can work with ontology filters.",
            onbeforechange: ()=>{ this.showSubMenuIfNotVisible($("#NestedExpansionSetConceptFilterScrollContainer"), true); },
            position: "left"
            },

            
//- filter edge types. Concepts can have different types of relationships between them, depending on the ontology, and these may be hidden or re-colored via the menu.
             {
            element: $("#ConceptEdgeTypeFilterOuterContainer")[0],
            intro: "<h1 class='introjs-header'>Edge Filters</h1> To focus on certain types of relationships, or to visually simplify a visualization, try filtering out the edges by toggling the check boxes.",
            onbeforechange: ()=>{ this.showSubMenuIfNotVisible($("#ConceptEdgeTypeFilterScrollContainer"), true); },
            position: "left"
            },
            {
            element: $("#ConceptEdgeTypeFilterOuterContainer")[0],
            intro: "Click the arrow (beside the check box) to assign a new color to that arc type.",
            onbeforechange: ()=>{ this.showSubMenuIfNotVisible($("#ConceptEdgeTypeFilterScrollContainer"), true); },
            position: "left"
            },
            
//- sharing of graphs via export and import, via the menu. Export provides json code that another user may paste into the import field, and receive the nodes, their positions and any custom edge colors, that existed at the time of export. Undo/redo state is not transferred.
             {
            element: $("#"+GraphImporterExporter.Widget.outerContainerId)[0],
            intro: "<h1 class='introjs-header'>Sharing</h1> You can share a visualization that includes all the changes you've made. The system provides you with  data that you can copy and then paste into an email to collaborators.<br/><br/> Try it! Click Export and copy the presented data.<br/><br/> Then, in the new window that opens when you click <a target='_blank' href='"+GraphImporterExporter.SavedGraph.getUrlIFrameOrNot()+"'>here</a>, paste the data into the box provided there.",
            onbeforechange: ()=>{ this.showSubMenuIfNotVisible($("#"+GraphImporterExporter.Widget.innerContainerId), true); },
            position: "left"
            },
//            
////- hovering over any of the node filtering check boxes will highlight the corresponding nodes in the graph
//             {
//            element: $("#NestedOntologyConceptFilterOuterContainer")[0],
//            intro: "You can also hover over check boxes to highlight all the nodes corresponding to them.",
//            onbeforechange: ()=>{ this.showSubMenuIfNotVisible($("#NestedOntologyConceptFilterOuterContainer")); },
//            position: "bottom"
//            },
////- since mapping arcs generally form densely connected webs, it only displays mapping arcs leading to a node for which a mapping expansion was triggered. Other mapping arcs are made visible whenever one of their endpoint nodes is hovered over.
//             {
//            element: $(ConceptLayouts.ConceptLayouts.layoutMenuContainerId)[0],
//            intro: "For the next step, if you don't have a few ontologies present, I need you to click some node's menu, and do a Mapping Expansion. When you have done that, click next.",
//            position: "bottom"
//            },
//             {
//            element: $(ConceptLayouts.ConceptLayouts.layoutMenuContainerId)[0],
//            intro: "Note that there are arcs drawn between the expanded mapped concepts and the node they were expanded from. If you hover over one of them, it will probably have arcs appear connecting it to the other mapped nodes. Hiding and revealing the arcs this way keeps the graph tidy.",
//            position: "bottom"
//            },
////- refocus the graph on a different node; this reloads the visualization in its current mode starting with the specified node (accessed via dropdown menu on each node)
//            {
//            element: $(ConceptLayouts.ConceptLayouts.layoutMenuContainerId)[0],
//            intro: "If you want to do a more drastic exploration or traversal of the graph, you can start the whole thing over using a different node. Try clicking a node menu, and selecting '"+PathsToRoot.ConceptPathsToRoot.REFOCUS_NODE_TEXT+"'.",
//            position: "bottom"
//            },
            {
            
            intro: "<h1 class='introjs-header'>All Done! Questions?</h1> If you have suggestions or comments, contact us at biomixer.chisel@gmail.com  <br/><br/> More documentation is available on the <a target='_blank' href='http://www.bioontology.org/wiki/index.php/Visualizing_Concepts_and_Mappings'>Bioportal Wiki</a>.",
                position: "bottom"
            },
            ]
        ;   
    }
}