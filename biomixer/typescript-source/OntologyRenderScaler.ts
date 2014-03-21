///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="Utils" />

import Utils = require('./Utils');

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
    private LINK_MAX_ON_SCREEN_SIZE: number = 7; // 6 looks good...but if I change colors it may not.
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
        var circles = this.vis.selectAll(".circle");
        circles.each(function(d){
                    var basis = parseInt(this.getAttribute("data-radius_basis"));
                    if(-1 == outerThis.maxNodeRawSize || basis > outerThis.maxNodeRawSize){
                        this.maxNodeRawSize = basis;
                    }
                    if(-1 == outerThis.minNodeRawSize || basis < outerThis.minNodeRawSize){
                        this.minNodeRawSize = basis;
                    }
            });

        circles.transition().attr("r", function(d) { return outerThis.ontologyNodeScalingFunc(this.getAttribute("data-radius_basis"), this.getAttribute("id"));});
        
        // Inner circles use the same scaling factor.
        var innerCircles = this.vis.selectAll(".inner_circle");
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
        $.each(this.vis.selectAll("line.link")[0], function(i, link){
            link = $(link);
            var basis = parseInt(link.attr("data-thickness_basis"));
            if(-1 == outerThis.maxLinkRawSize || basis > outerThis.maxLinkRawSize){
                outerThis.maxLinkRawSize =  basis;
            }
            if(-1 == outerThis.minLinkRawSize || basis < outerThis.minLinkRawSize){
                outerThis.minLinkRawSize =  basis;
            }
        });
        
        // Dynamic scoping of "this" required for the D3 function,
        // but we need an object reference closured into it as well. So....outerThis!
        $.each(this.vis.selectAll("line.link")[0], function(i, link){
            // Given a json encoded graph element, update all of the nested elements associated with it
            // cherry pick elements that we might otherwise get by class "node"
            link = $(link);
            link.css("stroke-width", function(d) { return outerThis.ontologyLinkScalingFunc(link.attr("data-thickness_basis")); });
        });
    }
    
    ontologyNodeScalingFunc(rawValue, acronym){
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
        
    ontologyInnerNodeScalingFunc(rawValue, outerRawValue, acronym){
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
            return (rawValue/outerRawValue) * this.ontologyNodeScalingFunc(outerRawValue, acronym);
        }
        
        return this.ontologyNodeScalingFunc(rawValue, acronym);
        
        // var outerRadius = ontologyNodeScalingFunc(rawValue, acronym);
        // var outerArea = Math.PI*(outerRadius*outerRadius);
        // var innerArea = outerArea * (rawValue / outerRawValue);
        // var innerRadius = outerRadius * (rawValue / outerRawValue);
        // // var innerRadius = Math.sqrt(innerArea/Math.PI);
        //  console.log([acronym, "raw", rawValue / outerRawValue, rawValue, outerRawValue, "area", outerArea/innerArea, outerArea, innerArea, "radius", outerRadius/innerRadius, outerRadius, innerRadius]);
        // return innerRadius;
    }
    
    ontologyLinkScalingFunc(rawValue){
        rawValue = parseInt(rawValue);
        if(this.maxLinkRawSize == this.minLinkRawSize){
            return rawValue;
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
        var linearArea = Math.PI * Math.pow(minOnScreenSize, 2) + factor
              * Math.PI * Math.pow(maxOnScreenSize, 2);
        var diameter = Math.sqrt(linearArea / Math.PI);
        return diameter;
    }
    
    /*
        private double linearFunction(double value) {
            // Ha! A sqrt makes this not linear. Mis-named now...
            return 2 * (4 + Math.sqrt((value) / 10));
            return (1 + Math.sqrt((value)));
        }
    
        private double logFunction(double value) {
            return 4 + Math.log(value) * 10;
        }
     */
    
}