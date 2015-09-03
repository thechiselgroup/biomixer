///<amd-dependency path="Ontologies/NodeAreaToggleWidget" />
///<amd-dependency path="Utils" />
///<amd-dependency path="GraphView" />

import NodeAreaToggler = require("./NodeAreaToggleWidget");
import Utils = require("../Utils");
import GraphView = require("../GraphView");

export class OntologyRenderScaler {
        
    constructor(
        private vis: D3.Selection
    ){
        
    }

    // Maintaining relative scaled sizes of arcs and nodes depends on updating
    // the raw size range, which in this implementation, loops over all entities.
    // Only update the ranges when appropriate.
    // BioMixer used a 500 ms delay on re-doing things.
    
    // 20 * 7 seems too big. Got 20 from other transformers.
    private NODE_MAX_ON_SCREEN_SIZE: number = 20 * 5;
    private NODE_MIN_ON_SCREEN_SIZE: number = 4;
    private minNodeRawSize: number = -1;
    private maxNodeRawSize: number = -1;
    private LINK_MAX_ON_SCREEN_SIZE: number = 9; // 6 looks good...but if I change colors it may not.
    private LINK_MIN_ON_SCREEN_SIZE: number = 1;
    private minLinkRawSize: number = -1;
    private maxLinkRawSize: number = -1;
    private REFRESH_LOOP_DELAY_MS: number = 500;
    
    private defaultNumOfTermsForSize: number = 10;
    
    updateNodeScalingFactor(){
        // Call this prior to redrawing. The alternative is to track on every size
        // modification. That worked well for BioMixer, but perhaps we're better
        // off doing a bulk computation per size-refreshing redraw that we want to make.
        var outerThis = this;
        var circles = this.vis.selectAll(GraphView.BaseGraphView.nodeSvgClass);
        circles.each(function(d){
                    var basis = parseInt(this.getAttribute("data-radius_basis"));
                    if(-1 == outerThis.maxNodeRawSize || basis > outerThis.maxNodeRawSize){
                        outerThis.maxNodeRawSize = basis;
                    }
                    if(-1 == outerThis.minNodeRawSize || basis < outerThis.minNodeRawSize){
                        outerThis.minNodeRawSize = basis;
                    }
            });

        circles.transition().attr("r", function(d) { return outerThis.ontologyOuterNodeScalingFunc(this.getAttribute("data-radius_basis"), this.getAttribute("id"));});
        
        // Inner circles use the same scaling factor.
        var innerCircles = this.vis.selectAll(GraphView.BaseGraphView.nodeInnerSvgClass);
        innerCircles.transition().attr("r", function(d) { return outerThis.ontologyInnerNodeScalingFunc(this.getAttribute("data-inner_radius_basis"), this.getAttribute("data-outer_radius_basis"), this.getAttribute("id"));});
    
    
        
    }
    
    updateLinkScalingFactor(){
        var outerThis = this;
        // TODO This may not ever need to be called multiple times, but it would take some time to run.
        // Make sure it actually needs to be run if it is indeed called. 
        console.log("Ran update link "+Utils.getTime());
        // Call this prior to redrawing. The alternative is to track on every size
        // modification. That worked well for BioMixer, but perhaps we're better
        // off doing a bulk computation per size-refreshing redraw that we want to make.
        $.each(this.vis.selectAll(GraphView.BaseGraphView.linkSvgClass)[0], function(i, link){
            link = $(link);
            var basis = parseInt(link.attr("data-thickness_basis"));
            if(-1 == outerThis.maxLinkRawSize || basis > outerThis.maxLinkRawSize){
                outerThis.maxLinkRawSize =  basis;
            }
            if(-1 == outerThis.minLinkRawSize || basis < outerThis.minLinkRawSize){
                outerThis.minLinkRawSize =  basis;
            }
        });
        
        // Actually, this is done by the caller after the update occurs.
        /*
        // Dynamic scoping of "this" required for the D3 function,
        // but we need an object reference closured into it as well. So....outerThis!
        $.each(this.vis.selectAll(GraphView.BaseGraphView.linkSvgClass)[0], function(i, link){
            // Given a json encoded graph element, update all of the nested elements associated with it
            // cherry pick elements that we might otherwise get by class "node"
            link = $(link);
            // Using double-backed polyline with variable width of fill instead of thickness of line
            // link.css("stroke-width", function(d) { return outerThis.ontologyLinkScalingFunc(link.attr("data-thickness_basis")); });
            // Actually, this is done by the caller after the update occurs.
            // link.attr("points", function(e){ return outerThis.updateArcLineFunc(e); })
            
        });
        */
    }
    
    ontologyOuterNodeScalingFunc = (rawValue, acronym, outerRawValue = null ) => {
        if(NodeAreaToggler.NodeAreaToggleWidgets.usePercentile){
            return this.ontologyProportionalCountsNodeScalingFunc(rawValue, acronym);
        } else {
            return this.ontologyConstantPercentileNodeScalingFunc(rawValue, acronym, outerRawValue);
        }
    }
    
    /**
     * Scales nodes so that all of them have the same outer size, and so that the inner
     * circle corresponds to the percentage of mappings in that ontology.
     */
    ontologyConstantPercentileNodeScalingFunc = (rawValue, acronym, outerRawValue = null ) => {
        rawValue = parseInt(rawValue);
        if(null === outerRawValue){
            outerRawValue = rawValue;
        } else {
            outerRawValue = parseInt(outerRawValue);
        }
        
        var diameter = 20;
        if(rawValue !== outerRawValue){
            // Steven's Power Law: area is perceived as area to the power of 0.8, times a scaling factor.
            // So, raise this to power of 0.8
            diameter = this.linearAreaRelativeScaledRangeValue((rawValue+1 / outerRawValue+1), 0, 20);
            // power of diameter sort of works.
            // Best results were applying exponent to radius, not diameter, not area.
            diameter = 2*Math.pow(diameter/2, 0.8);
            if(isNaN(diameter)){
                return 0;
            }
        }
        return diameter;
    }
    
    /**
     * Scales nodes so that the largest ontology is set as the largest allowed node size, and so that
     * the inner circle corresponds to the number of mappings in that ontology.
     */
    ontologyProportionalCountsNodeScalingFunc = (rawValue, acronym) => {
        rawValue = parseInt(rawValue);
            
        if(rawValue == 0){
            return this.defaultNumOfTermsForSize;
        }
        
        if(this.maxNodeRawSize == this.minNodeRawSize){
            return this.defaultNumOfTermsForSize;
        }
        
        var factor = this.computeFactorOfRange(rawValue, this.minNodeRawSize, this.maxNodeRawSize);
        var diameter = this.linearAreaRelativeScaledRangeValue(factor, this.NODE_MIN_ON_SCREEN_SIZE, this.NODE_MAX_ON_SCREEN_SIZE);
        if(isNaN(diameter)){
            return 0;
        }
        return diameter/2; // need radius for SVG
    }
        
    ontologyInnerNodeScalingFunc = (rawValue, outerRawValue, acronym) => {
        rawValue = parseInt(rawValue);
        outerRawValue = parseInt(outerRawValue);
        if(rawValue == 0 || this.maxNodeRawSize == this.minNodeRawSize || rawValue > outerRawValue){
            // If there is no mapping, I want no dot. This applies to the central node specifically.
            // I also don't want a teeny weeny inner circle completely covering the outer circle,
            // so let's scale away those that match the minimum render size.
            // Otherwise we'll scale exactly the same as the outer circle.
            return 0;
        }
        if(outerRawValue == this.minNodeRawSize){
            return (rawValue/outerRawValue) * this.ontologyOuterNodeScalingFunc(outerRawValue, acronym, outerRawValue);
        }
        
        return this.ontologyOuterNodeScalingFunc(rawValue, acronym, outerRawValue);
    }
    
    ontologyLinkScalingFunc = (rawValue) => {
        // Used to be used for stroke-width, but now we use invisible stroke to have a larger mousable area for thin arcs.
        // Now this is used to define how wide the fill region for each arc's polyine drawn rectangle.
        rawValue = parseInt(rawValue);
        if(this.maxLinkRawSize == this.minLinkRawSize){
			// Used to retrun rawValue here, but that led to problems in loading. Re-assess if needed.
            return 1;
        }
        var factor = this.computeFactorOfRange(rawValue, this.minLinkRawSize, this.maxLinkRawSize);
        // The linear area algorithm used for nodes happens to work really well for the edges thickness too.
        var thickness = this.linearAreaRelativeScaledRangeValue(factor, this.LINK_MIN_ON_SCREEN_SIZE, this.LINK_MAX_ON_SCREEN_SIZE);
        return thickness/2;
    }
    
    computeRangeRawSize(minRawSize, maxRawSize) {
        return Math.max(1, maxRawSize - minRawSize);
    }
    
    computeFactorOfRange(rawValue, minRawSize, maxRawSize) {
        return 1.0 - (maxRawSize - rawValue) / this.computeRangeRawSize(minRawSize, maxRawSize);
    }
    
    linearAreaRelativeScaledRangeValue(factor, minOnScreenSize, maxOnScreenSize) {
        var factor = (factor > 1) ? 1.0 : factor;
        var linearArea = Math.PI * Math.pow(minOnScreenSize, 2) + factor
              * Math.PI * Math.pow(maxOnScreenSize, 2);
        // power of the linear area doesn't work so well
        // linearArea = Math.pow(linearArea, 0.8);
        var diameter = Math.sqrt(linearArea / Math.PI);
        return diameter;
    }
    
    linearWidthRelativeScaledRangeValue(factor, minOnScreenSize, maxOnScreenSize) {
        var linearWidth = minOnScreenSize + factor * (maxOnScreenSize - minOnScreenSize);
        return linearWidth;
    }
    
}