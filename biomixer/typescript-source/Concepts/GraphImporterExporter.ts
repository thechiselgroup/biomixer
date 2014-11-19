///<reference path="headers/require.d.ts" />

///<amd-dependency path="Utils" />
///<amd-dependency path="FetchFromApi" />
///<amd-dependency path="GraphView" />
///<amd-dependency path="Concepts/ConceptGraph" />
///<amd-dependency path="Menu" />
///<amd-dependency path="ExpansionSets" />

import Utils = require("../Utils");
import Fetcher = require("../FetchFromApi");
import GraphView = require("../GraphView");
import ConceptGraph = require("./ConceptGraph");
import PathsToRoot = require("./ConceptPathsToRoot");
import Menu = require("../Menu");
import ExpansionSets = require("../ExpansionSets");

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
export class SavedGraphSeed {
    o: string;
    x: number;
    y: number;
}



export class SavedGraph {
    NB: string = "Greetings! This is a BioMixer portable graph."
    +" Paste this entire data structure into the Import dialog box, accessible via the menu."
    +" Use the url: "+ SavedGraph.getUrlIFrameOrNot()+" ."
    ;
    
    n: { [conceptUri: string]: SavedGraphSeed } = {};
    
    addNode(nodeData){
        this.n[String(nodeData.rawConceptUri)] = <SavedGraphSeed><any>{ o: nodeData.ontologyAcronym, x: nodeData.x, y: nodeData.y };
    }
    
    static getUrlIFrameOrNot(): string{
        return document.location.protocol + '//' +document.location.host +document.location.pathname;
    }
}

export class Widget {

    public menuSelector: string = "";
    
    private containers: {outer: JQuery; inner: JQuery; expanderCallback: {(open?: boolean): void} };
    
    constructor(
        private pathsToRoot: PathsToRoot.ConceptPathsToRoot
    ){
    }
    
    static exportImportFooterDiv = "exportImportFooterDiv";
    
    addMenuComponents(menuSelector: string){
        this.containers = Menu.Menu.slideToggleHeaderContainer("importerExporterMenuContainer", "importerExporterInnerContainer", "Sharing", true);
        var outerContainer = this.containers.outer;
        var innerContainer = this.containers.inner;
        this.menuSelector = menuSelector; // store for later
        $(menuSelector).append(outerContainer);
        
        var exportButton = $("<input>")
                .attr("class", "importExportButton")
                .attr("id", "exportButton")
                .attr("type", "button")
                .attr("value", "Export");

        var importButton = $("<input>")
                .attr("class", "importExportButton")
                .attr("id", "importButton")
                .attr("type", "button")
                .attr("value", "Import");
        
        
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

    }
    
    /**
     * When we are opening an empty page, we like to assume the user will be importing, so we can open all the required elements
     * for them.
     */
    openShareAndImportMenu(){
        $('#trigger').trigger("click");
        this.containers.expanderCallback();
        this.showImportDialogLambda()();
    }

    private showExportDialogLambda(){
        return ()=>{
            var message = "To share this graph view, copy the text below, and share it via email."
                +"\nThe receiver can then click the import button, paste the text there, and see this current view."
                ;
            var exporter = new GraphExporter(this.pathsToRoot.conceptGraph);
            var exportJson = exporter.getExportData();
            this.messagePrompt(message, JSON.stringify(exportJson), null);
        };
    }

    private showImportDialogLambda(){
        return ()=>{
            var message = "To import a graph, paste the JSON-format text that you received from your collaborator, and click OK."
                ;
            this.messagePrompt(message, "", this.importCallbackLambda());
        };
    }
    
    static messageTextId = "messageBoxTextArea";
    static messageParagraphId = "messageBoxMessage";
    static messageDivId = "messageBox";
    static messageDivClass  ="messageBoxWithField";
    static messageBoxButtonClass = "messageBoxButton";
    
    private importCallbackLambda(){
        return (event: JQueryEventObject)=>{
            event.stopPropagation();
            var dialog = $("#"+Widget.messageDivId);
            var messageField = $("#"+Widget.messageTextId);
            var importData = messageField.first().val();
            dialog.slideUp(200, ()=>{ dialog.detach() });
            
            if(importData.length === 0){
                return;
            }
            var importer = new GraphImporter(this.pathsToRoot, <SavedGraph><any>JSON.parse(importData));
            importer.loadGraph();
        }  
    }
    
    private closeDialogLambda(){
        return (event: JQueryEventObject)=>{
            var dialog = $("#"+Widget.messageDivId);
            dialog.slideUp(200, ()=>{ dialog.detach() });
        }
    }
    
    private messagePrompt(message: string, fieldContent: string, okCallback){
        // Remove any existing version of this panel. It is an embedded modal singleton unique as a unicorn.
        var dialog = $("#"+Widget.messageDivId);
        if(undefined !== dialog){
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
        if(null === okCallback){
            okCallback = this.closeDialogLambda();
            okButtonText = "Close";
        } else {
            cancelButton =  $("<button>").addClass(Widget.messageBoxButtonClass).addClass("importExportButton")
                .text("Cancel").click(this.closeDialogLambda());
            okButtonText = "Apply";
        }
        var okButton = $("<button>").addClass(Widget.messageBoxButtonClass).addClass("importExportButton")
            .text(okButtonText).click(okCallback);
        dialog
            .append(messageParagraph)
            .append(messageField)
            .append($("<br>"))
            .append(okButton)
        ;
        if(undefined !== cancelButton){
            dialog.append(cancelButton);
        }
        
        
        dialog.css("display", "none");
        $("#"+Widget.exportImportFooterDiv).append(dialog);
        dialog.slideDown("fast");
        
    }
    
}

export class GraphExporter {
    
    constructor(
        public graph: ConceptGraph.ConceptGraph
    ){
        
    }
    
    public getExportData(): SavedGraph {
        var savedGraph = new SavedGraph();
        // Fill it up!
        var nodes = this.graph.graphView.getUnhiddenNodes();
        nodes.each(
            (index: number, node: Element)=>{
                var nodeId = node.getAttribute("id").replace(GraphView.BaseGraphView.nodeGSvgClassSansDot+"_", "");
                var nodeData = this.graph.getNodeByIdUri(nodeId);
                savedGraph.addNode(nodeData);
            }
        );
        
        return savedGraph;
    }
}

export class GraphImporter {
    
    static importNumber = 0;
    
    public conceptGraph: ConceptGraph.ConceptGraph;
    
    constructor(
        public pathsToRoot: PathsToRoot.ConceptPathsToRoot,
        public importData: SavedGraph
    ){
        
        this.conceptGraph = pathsToRoot.conceptGraph;
    }

    loadGraph(){
        
        if(0 >= Object.keys(this.importData).length){
            console.log("No keys available in data that was imported.");
            return;
        }

        // Set the graph's layout to be agnostic. We are presumably getting all of the layout data from the imported
        // data, and we don't want any other layout algorithm overriding that when calls to refresh the layout occurs.
        // I expected to need to set the layout here, but it didn't work. Had to do it after adding all the data.
        // this.conceptGraph.graphView.setCurrentLayout(this.pathsToRoot.layouts.runFixedPositionLayoutLambda());
       
        // It's ok if the id for this expansion set is simplistic. We do want to allow for multiple imports within a page though...
        // Maybe...
        
        var expId = new ExpansionSets.ExpansionSetIdentifer("importedGraphInitialExpansion_"+GraphImporter.importNumber, "Imported Graph "+GraphImporter.importNumber);
        var initSet = this.pathsToRoot.prepareForExpansionFromScratch(expId, null);
        var expansionSet = initSet.expansionSet;

        // The init set has the expansion set I would otherwise have made, but is the right way to delete and add at the same time.
        // var expansionSet = new ExpansionSets.ExpansionSet<ConceptGraph.Node>(expId, null, this.conceptGraph, this.pathsToRoot.undoRedoBoss, null);
        GraphImporter.importNumber++;
        
        
        for(var conceptUri in this.importData.n){
            // Verify the structure's contents. It was imported via casting, not parsing.
            var nodeData: SavedGraphSeed = this.importData.n[conceptUri];
            // Casting to prevent need for re-boxing data. Would need to remove elements and leave just x and y.
            this.pathsToRoot.layouts.updateFixedLayoutDatum(conceptUri, <{x: number; y: number}>nodeData);
            this.loadNode(conceptUri, nodeData, expansionSet);
        }
        
        this.conceptGraph.graphView.setCurrentLayout(this.pathsToRoot.layouts.runFixedPositionLayoutLambda());
    }
    
    loadNode(conceptUri: string, nodeData: SavedGraphSeed, expansionSet: ExpansionSets.ExpansionSet<ConceptGraph.Node>){
        if(!(conceptUri in this.conceptGraph.conceptIdNodeMap)){
            
            // Dumb trick. Only way to do it. Minimizes casting, while allowing typing.
            var nodeUri = <ConceptGraph.ConceptURI><any>conceptUri;
            var ontologyAcronym = <ConceptGraph.RawAcronym><any>nodeData.o;
            
            var url = this.conceptGraph.buildConceptUrlNewApi(ontologyAcronym, nodeUri);
            var callback = new FetchAndApplyLayoutCallback(this.conceptGraph, this.pathsToRoot, nodeData, url, nodeUri, expansionSet);
            var fetcher = new Fetcher.RetryingJsonFetcher(url);
            fetcher.fetch(callback);
        }
    }
    
}

/**
 * We need to set the node positions, but only after we have actually parsed and built the node.
 * This callback does just that. If D3 used layouts that did not embed position data into node
 * data, then we could do this very differently.
 */
class FetchAndApplyLayoutCallback extends Fetcher.CallbackObject {
    
    private wrappedCallback: ConceptGraph.FetchOneConceptCallback;
    
    constructor(
        public graph: ConceptGraph.ConceptGraph,
        public pathsToRoot: PathsToRoot.ConceptPathsToRoot,
        public nodeData: SavedGraphSeed,
        url: string,
        public conceptUri: ConceptGraph.ConceptURI,
        public expansionSet: ExpansionSets.ExpansionSet<ConceptGraph.Node>
        ){
            super(url, String(conceptUri), Fetcher.CallbackVarieties.nodeSingle); //+":"+directCallForExpansionType);
            this.wrappedCallback = new ConceptGraph.FetchOneConceptCallback(graph, url, conceptUri, null, expansionSet);
        }
    
    public checkAgainstNodeCap = () => {
        return true; // Used with export and import, so should let all nodes through.
    }
    
    public callback = (conceptPropertiesData: any, textStatus: string, jqXHR: any) => {
        // textStatus and jqXHR will be undefined, because JSONP and cross domain GET don't use XHR.

        this.wrappedCallback.callback(conceptPropertiesData, textStatus, jqXHR);
        
        // Casting to prevent need for re-boxing data. Would need to remove elements and leave just x and y.
        this.pathsToRoot.layouts.updateFixedLayoutDatum(this.conceptUri, <{x: number; y: number}>this.nodeData);
    }
}