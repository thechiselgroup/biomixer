import Menu = require("./Menu");
import ExportSvgToImage = require("./ExportSvgToImage");
import GraphView = require("./GraphView");
import MouseSpinner = require("./MouseSpinner");

declare var Pablo;

export class MiniMap {
    
    static minimapContainerOuterContainer = "minimapContainerOuterContainer";
    static menuContainerScrollContainerId = "minimapMenuContainerScrollContainer";
    
    addMenuComponents(menuSelector: string, defaultHideContainer: boolean){
        
        var containers = Menu.Menu.slideToggleHeaderContainer(MiniMap.minimapContainerOuterContainer, MiniMap.menuContainerScrollContainerId, "Minimap", false, this.menuMadeVisibleLambda());
        var layoutsContainer = containers.inner;

        $(menuSelector).append(containers.outer);
        
        containers.inner.append(this.outerCanvas.node());
        
        $("#"+MiniMap.menuContainerScrollContainerId).css("background-color", "white").css("overflow", "hidden");
        $("#"+MiniMap.menuContainerScrollContainerId).css("background-color", "rgb(193, 217, 241)");
        $("#"+MiniMap.menuContainerScrollContainerId).css("min-width", "inherit");
        $("#"+MiniMap.menuContainerScrollContainerId).css("min-height", "inherit");
        $("#"+MiniMap.menuContainerScrollContainerId).css("width", "inherit");
        $("#"+MiniMap.menuContainerScrollContainerId).css("height", "inherit");
        
        this.attachZoomHandlers();
    }
    
    menuMadeVisibleLambda(){
        return (): void=>{
            var outerBbox = this.getMaxMiniMapSize();
            // Use width for everything, because it is not initialized properly under certain circumstances.
            // We get height by checking the aspect ratio of the window.
            var heightScale = parseFloat(d3.select("#graphSvg").attr("height"))/parseFloat(d3.select("#graphSvg").attr("width"));
            
            this.outerMMSVG().attr("width", outerBbox.width);
            this.outerMMSVG().attr("height", outerBbox.width * heightScale);
            
            this.outerMMSVG().attr("viewBox", "0 0 "+outerBbox.width+" "+outerBbox.width * heightScale);
            this.outerMMSVG().attr("width", outerBbox.width);
            this.outerMMSVG().attr("height", outerBbox.width * heightScale);
            
            this.oldViewbox = this.outerMMSVG().attr("viewBox");
            this.oldmmsvgWidth = this.outerMMSVG().attr("width");
            this.oldmmsvgHeight = this.outerMMSVG().attr("height");
            
            this.render(true);
        }
    }
    
    outerMMSVG(){
        return d3.select("#outerMMSVG");
    }
    
    attachZoomHandlers(){
        // In order to control when the user may zoom and drag the minimap, we must add
        // and remove the zoom handler when the mosue goes voer the minimap. There wasn't
        // another way I could see to control zooming applicability.
        var fakeZoomHandler = d3.behavior.zoom();
        d3.select("#"+MiniMap.menuContainerScrollContainerId).on("mousemove", ()=>{
            var currentTime = new Date().getTime();
            if(this.parentGraph.getTimeStampLastLayoutModification() + 500 > currentTime){
                this.outerMMSVG().call(fakeZoomHandler);
                MouseSpinner.MouseSpinner.applyMouseSpinner("MiniMapNoDrag");
            } else {
                MouseSpinner.MouseSpinner.haltSpinner("MiniMapNoDrag");
                // This gives us instant zoom behavior when scrolling on minimap, when we have
                // also done the container.call(parentZoom).
                this.outerMMSVG().call(this._zoom);
                this._zoom.on("zoom.minimap", ()=>{
                    var zoomTime = new Date().getTime();
                    if(this.parentGraph.getTimeStampLastLayoutModification() + 500 > zoomTime){
                        return;
                    }
                    this._scale = d3.event.scale;
                    this.renderImplementation();
                    return;
                });
                this._zoom.on("zoomstart.minimap", ()=>{
                    this.oldViewbox = this.outerMMSVG().attr("viewBox");
                    this.oldmmsvgWidth = this.outerMMSVG().attr("width");
                    this.oldmmsvgHeight = this.outerMMSVG().attr("height");
                });
            }
        });
        
        d3.select("#"+MiniMap.menuContainerScrollContainerId).on("mouseleave", ()=>{
            MouseSpinner.MouseSpinner.haltSpinner("MiniMapNoDrag");
            this.outerMMSVG().call(fakeZoomHandler);
            this._zoom.on("zoom.minimap", null);
        });   
    }

    private parentVisualization     = null;
    private parentGraph     = null;
    private outerCanvas     = null;
    private miniMap       = null;
    private minimapKiddle         = null;
    private _minimapPadding  = 20;
    private _minimapScale    = 0.25;
    private _scale           = 1;
    private _zoom: D3.Behavior.Zoom = null;
    private _base            = null;
    private _target          = null;
    private _mmwidth         = 40;
    private _mmheight        = 40;
    private _x               = 0;
    private _y               = 0;
    private _frameX          = 0;
    private _frameY          = 0;
    
    private frame;
    private container;
    private base;
    
    private svgDefs;
    

    constructor(parentVisualization: D3.Selection, parentGraph: GraphView.GraphView<any, any>, parentZoom: D3.Behavior.Zoom){
        
        this.parentVisualization = parentVisualization;
        this.parentGraph = parentGraph;

        var pc = document.createElementNS(d3.ns.prefix.svg, 'svg');
        pc.setAttribute("id", "outerMMSVG");
        // Avoids some IE and Firefox errors in Pablo, with the getBBox() method.
        d3.select("#outerMMSVG").attr("width", "0").attr("height", "0").attr("x", "0").attr("y", "0");
        this.outerCanvas = d3.select(pc);
        this.addDefs();
        this.miniMap = this.outerCanvas.append("g")
            .attr("class", "minimap")
        ;
        
        
        this.minimapKiddle = this.miniMap.append("g")
            .attr("class", "panCanvas")
        .attr("width", 700)
        .attr("height", 600)
        ;

        this.minimapKiddle.append("rect")
            .attr("id", "framerect")
            .attr("width", this._mmwidth)
            .attr("height", this._mmheight);
        
        this.container = this.miniMap;
        
        var xScale = d3.scale.linear()
            .domain([-this._mmwidth / 2, this._mmwidth / 2])
            .range([0, this._mmwidth]);

        var yScale = d3.scale.linear()
            .domain([-this._mmheight / 2, this._mmheight / 2])
            .range([this._mmheight, 0]);
              
        this._zoom = parentZoom;
        this._target = parentVisualization;
        this._mmwidth  = parseInt(this._target.attr("width"),  10);
        this._mmheight = parseInt(this._target.attr("height"), 10);
        this._minimapScale = this._minimapScale;
        this._x = this._mmwidth + this._minimapPadding;
        this._y = this._mmheight + this._minimapPadding;

        this.frame = this.container.append("g")
            .attr("class", "frame");

        this.frame.append("rect")
            .attr("id", "frameRect")
            .attr("width", 30)
            .attr("height", 30)
            .attr("fill", "url(#frameGradient)")
        ;
        
        var dragstarted = false;
        var drag = d3.behavior.drag()
            .on("dragstart.minimap", ()=>{
                var currentTime = new Date().getTime();
                if(this.parentGraph.getTimeStampLastLayoutModification() + 1000 > currentTime){
                    return;
                }
                dragstarted = true;
                var frameTranslate = this.getXYFromTranslate(this.frame.attr("transform"));
                this._frameX = frameTranslate[0];
                this._frameY = frameTranslate[1];
            })
            .on("drag.minimap", ()=>{
                d3.event.sourceEvent.stopImmediatePropagation();
                var currentTime = new Date().getTime();
                if(!dragstarted || this.parentGraph.getTimeStampLastLayoutModification() + 1000 > currentTime){
                    return;
                }
                this._frameX += d3.event.dx;
                this._frameY += d3.event.dy;
                
                this.frame.attr("transform", "translate(" + this._frameX + "," + this._frameY + ")");
                var translate =  [(-this._frameX*this._scale),(-this._frameY*this._scale)];
                
                var gRect = d3.select("#graph_g"); // .select("rect");
                var xy = this.getXYFromTranslate(gRect.attr("transform"));
                var graphX = xy[0];
                var graphY = xy[1];
                
                graphX -= d3.event.dx * this._scale;
                graphY -= d3.event.dy * this._scale;
                $("#graph_g").attr("transform", "translate(" + graphX + "," + graphY + ") scale(" + this._scale + ")");

                this._zoom.translate(translate); // got rid of in original and it didn't affect anything
            })
            .on("dragend.minimap", ()=>{
                dragstarted = false;
            })
            ;
        
        
        this.frame.call(drag);
            
        // startoff zoomed in a bit to show pan/zoom rectangle
        this._zoom.scale(1.5);
        this.zoomHandlerF(1, null);
        this.renderImplementation();
    }
    
    getMaxMiniMapSize(){
        return d3.select("#"+MiniMap.menuContainerScrollContainerId).node().getBoundingClientRect();   
    }
    
    addDefs(){
        // used to be svg from original code
     this.svgDefs = this.outerCanvas.append("defs");
        
        var grad = this.svgDefs
            .append("radialGradient")
            .attr("id", "frameGradient")
            .attr("fx", "50%")
            .attr("fy", "50%")
            .attr("r", "80%")
            .attr("spreadMethod", "pad");
        grad.append("stop")
            .attr("offset", "0%")
            .attr("stop-color", "white")
            .attr("stop-opacity", "0.1")
        ;
        grad.append("stop")
            .attr("offset", "100%")
            .attr("stop-color", "black")
            .attr("stop-opacity", "0.6")
        ;


        this.svgDefs.append("clipPath")
            .attr("id", "wrapperClipPathDemo01")
            .attr("class", "wrapper clipPath")
            .append("rect")
            .attr("class", "background")
            .attr("width", this._mmwidth)
            .attr("height", this._mmheight);
            
        this.svgDefs.append("clipPath")
            .attr("id", "minimapClipPath")
            //.attr("class", "minimap clipPath")
            .attr("width", this._mmwidth)
            .attr("height", this._mmheight)
            .attr("transform", "translate(" + (this._mmwidth + this._minimapPadding) + "," + (this._minimapPadding/2) + ")")
            .append("rect")
            .attr("class", "background")
            .attr("width", this._mmwidth)
            .attr("height", this._mmheight);
            
        var filter = this.svgDefs.append("svg:filter")
            .attr("id", "minimapDropShadow")
            .attr("x", "-20%")
            .attr("y", "-20%")
            .attr("width", "150%")
            .attr("height", "150%");

        filter.append("svg:feOffset")
            .attr("result", "offOut")
            .attr("in", "SourceGraphic")
            .attr("dx", "1")
            .attr("dy", "1");

        filter.append("svg:feColorMatrix")
            .attr("result", "matrixOut")
            .attr("in", "offOut")
            .attr("type", "matrix")
            .attr("values", "0.1 0 0 0 0 0 0.1 0 0 0 0 0 0.1 0 0 0 0 0 0.5 0");

        filter.append("svg:feGaussianBlur")
            .attr("result", "blurOut")
            .attr("in", "matrixOut")
            .attr("stdDeviation", "10");

        filter.append("svg:feBlend")
            .attr("in", "SourceGraphic")
            .attr("in2", "blurOut")
            .attr("mode", "normal");
            
        var minimapRadialFill = this.svgDefs
            .append("radialGradient")
            .attr({
                id:"minimapGradient",
                gradientUnits:"userSpaceOnUse",
                cx:"500",
                cy:"500",
                r:"400",
                fx:"500",
                fy:"500"
            });
        minimapRadialFill.append("stop")
            .attr("offset", "0%")
            .attr("stop-color", "#FFFFFF");
        minimapRadialFill.append("stop")
            .attr("offset", "40%")
            .attr("stop-color", "#EEEEEE");
        minimapRadialFill.append("stop")
            .attr("offset", "100%")
            .attr("stop-color", "#E0E0E0");   
    }
    
    zoomHandlerF(newScale, parentProperties) {
        console.log("zoomhandlerF");
            var scale;
            if (d3.event) {
                scale = d3.event.scale;
            } else {
                scale = newScale;
            }
        
            var tbound = -this._mmheight * scale,
                bbound = this._mmheight  * scale,
                lbound = -this._mmwidth  * scale,
                rbound = this._mmwidth   * scale;
            // limit translation to thresholds
            var translation = d3.event ? d3.event.translate : [0, 0];
            translation = [
                Math.max(Math.min(translation[0], rbound), lbound),
                Math.max(Math.min(translation[1], bbound), tbound)
            ];

            d3.select(".panCanvas, .panCanvas .bg")
                .attr("transform", "translate(" + translation + ")" + " scale(" + scale + ")");

            this._scale = scale;
        
            this.render();
        } // startoff zoomed in a bit to show pan/zoom rectangle
    
    getXYFromTranslate(translateString) {
        if(null == translateString){
            return [0, 0];
        }
        var split = translateString.split(",");
        // Used to use ~~ instead of parseFloat(), as an obscure parseInt() variant.
        var x = split[0] ? parseFloat(split[0].split("translate(")[1]) : 0;
        var y = split[1] ? parseFloat(split[1].split(")")[0]) : 0;
        return [x, y];
    }
    
    minimapRefreshLastCallTime = 0;
    longTimerWait = 1200;
    timerWait = 500;
    nextTimerDue = 0;
    outerLayoutShortTimer = null;
    outerLayoutLongTimer = null;
    render(immediate: boolean = false, force: boolean = false, slow: boolean = false) {
        var currentTime = new Date().getTime();
        var longEnoughSinceLastRender = this.minimapRefreshLastCallTime + this.longTimerWait < currentTime;
        var longEnoughAfterGraphChange = this.parentGraph.getTimeStampLastGraphModification() + this.timerWait < currentTime;
        if(immediate || (longEnoughSinceLastRender && longEnoughAfterGraphChange)){
            this.renderImplementation(force || slow);
        } else if (slow){
             if(this.outerLayoutLongTimer == null && (this.minimapRefreshLastCallTime + this.longTimerWait > currentTime)){
                // Only use timer when there is actual change to graph, not when it is pan and zoom
                if(currentTime > this.nextTimerDue){
                    this.outerLayoutLongTimer = setTimeout(
                        ()=> {
                            clearTimeout(this.outerLayoutLongTimer);
                            this.outerLayoutLongTimer = null;
                            this.renderImplementation(force || slow);
                        }
                        , this.longTimerWait
                        );
                    this.nextTimerDue = currentTime + this.longTimerWait;
                }
            }
        } else {
            // if we called this within .2 seconds, defer for a bit
            // The minimap render can be called very very often, but we only need it to refresh at perhaps 60HZ
            if(this.outerLayoutShortTimer == null && (this.minimapRefreshLastCallTime + this.timerWait > currentTime)){
                // Only use timer when there is actual change to graph, not when it is pan and zoom
                if(currentTime > this.nextTimerDue){
                    this.outerLayoutShortTimer = setTimeout(
                        ()=> {
                            clearTimeout(this.outerLayoutShortTimer);
                            this.outerLayoutShortTimer = null;
                            this.renderImplementation(force || slow);
                        }
                        , this.timerWait);
                    this.nextTimerDue = currentTime + this.timerWait;
                }
            }
        }
    }
        
    private firstTimeRendering = true;
    private oldViewbox = null;
    private oldmmsvgWidth;
    private oldmmsvgHeight;
    private renderImplementation(force: boolean = false) {
    
        this.svgDefs
        .select("#frameGradient")
        .attr("width", this._mmwidth)
        .attr("height", this._mmheight);
        
        this._scale = this._zoom.scale();
        this.container.attr("transform", "scale(" + this._minimapScale + ")");

        // When the menu hasn't been shown, and in early processing, this doesn't exist yet:
        if($("#graphSvg").length == 0){
            return;
        }
        
        var graphChanged = true;
        if(this.parentGraph.getTimeStampLastGraphModification() < this.minimapRefreshLastCallTime
            && this.parentGraph.getTimeStampLastLayoutModification() < this.minimapRefreshLastCallTime){
            graphChanged = false;
        }
        
        // We will make a clone of the graph and miniaturize it.
        if(force || graphChanged || this.firstTimeRendering){
            this.firstTimeRendering = false;
            this.minimapRefreshLastCallTime = new Date().getTime();
            // Update the SVG in the minimap
            var pabloClone = ExportSvgToImage.ExportSvgToImage.getPabloSvgClone("graphSvg", "minimapClone", true); //this.parentVisualization.
            
            var node = d3.select(pabloClone.children()[0]).node();
            // Also, I want to remove the background that came from the graph, because it has sizing that
            // makes it hard to work with
            $(node).children("rect").remove();
            
            // This remove doesn't seem to work so well for me...
            this.minimapKiddle.selectAll("*").remove(); // d3.selectAll(".minimap .panCanvas").remove();
            this.minimapKiddle.node().appendChild(node);
            
            // I need that white space shrunk down
            if(null !== this.outerMMSVG()[0][0]){
                Pablo("#outerMMSVG").crop();
                if(null != this.oldViewbox){
                    // Make the viewport start at origin always
                    this.outerMMSVG().attr("viewBox", this.oldViewbox);
                    this.outerMMSVG().attr("width", this.oldmmsvgWidth);
                    this.outerMMSVG().attr("height", this.oldmmsvgHeight);
                }
            }
            
            this.frame.node().parentNode.appendChild(this.frame.node());
        
            $("#svgHtmlContainer").remove();
            $("#minimapClone").remove();
        }
        
        var width;
        var height;
        var gRect = d3.select("#graph_g").select("rect");
        if(gRect[0][0] != null){
            width = Math.max(0, parseFloat(gRect.attr("width")))/this._zoom.scale();
            height = Math.max(0, parseFloat(gRect.attr("height")))/this._zoom.scale();
        }
    
        var x = -this._zoom.translate()[0]/this._zoom.scale(); // + width/this._zoom.scale()/2; // 
        var y = -this._zoom.translate()[1]/this._zoom.scale(); // + height/this._zoom.scale()/2; // 
        
        this.frame
            .attr("transform", "translate(" + x + "," + y + ")") 
            .select(".background, #frameRect")
            .attr("width", width)
            .attr("height", height)
        ;
    }

}