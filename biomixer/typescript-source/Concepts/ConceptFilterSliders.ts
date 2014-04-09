///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />
///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="../Utils" />
///<amd-dependency path="../GraphView" />
///<amd-dependency path="Concepts/ConceptGraph" />

import Utils = require('../Utils');
import GraphView = require('../GraphView');
import OntologyGraph = require('./ConceptGraph');
    
export class ConceptRangeSliders {

    addMenuComponents(menuSelector: string, softNodeCap: number){
//        var minSliderAbsolute = 0;
//        var maxSliderAbsolute = 0 == softNodeCap ? this.sortedLinksByMapping.length : softNodeCap; 
//        
//        $(menuSelector).append($("<label>").attr("for", "top-mappings-slider-amount").text("Ranked Mapping Range: "));
//        $(menuSelector).append($("<label>").attr("type", "text").attr("id", "top-mappings-slider-amount")) // .css("border:0; color:#f6931f; font-weight:bold;"));
//        $(menuSelector).append($("<div>").attr("id",  "top-mappings-slider-range" ));
//        
//        $( "#top-mappings-slider-range" ).slider({
//            range: true,
//            min: minSliderAbsolute,
//            max: maxSliderAbsolute,
//            values: [ minSliderAbsolute, maxSliderAbsolute ],
//            slide: this.rangeSliderSlideEvent,
//            change: this.rangeSliderSlideEvent
//            }
//        );
//         
//        this.updateTopMappingsSliderRange();
//        
//        // Need separate initialization for input text
//        $( "#top-mappings-slider-amount" ).text( "Top "+ minSliderAbsolute + " - " + maxSliderAbsolute );
    }
    
    updateTopMappingsSliderRange(){
//        this.sortedLinksByMapping = [];
//        // Fill the sorted set every time in caase we are updating.
//        // This shouldn't get called too often.
//        var i = 0;
//        var outerThis = this;
//        d3.selectAll("line").each( 
//                function(d,i){
//                    outerThis.sortedLinksByMapping[i] = d;
//                }
//        );
//        
//        // Descending sort so we can pick the top n.
//        this.sortedLinksByMapping.sort(function(a,b){return b.value-a.value});
//        
//        var mappingMin = 1;
//        var mappingMax = this.sortedLinksByMapping.length;
//        
//        $( "#top-mappings-slider-range" ).slider("option", "min", 0);
//        $( "#top-mappings-slider-range" ).slider("option", "max", this.sortedLinksByMapping.length - 1);
//    //  $( "#top-mappings-slider-range" ).slider("option", "values", [0, sortedLinksByMapping.length - 1]);
//        $( "#top-mappings-slider-amount" ).text( "Top "+ mappingMin + " - " + mappingMax );
    }
    
     // Callback needs fat arrow or use of Utils.HasCallback.
    // Can use fat arrow instead of lambda closure because we don't need
    // the caller's" this".
    rangeSliderSlideEvent = (event, ui)=>{
//        // Need to make it wider than 100% due to UI bugginess
//        var bottom = $( "#top-mappings-slider-range" ).slider( "values", 0 ) + 1;
//        var top = $( "#top-mappings-slider-range" ).slider( "values", 1 ) + 1;
//        $( "#top-mappings-slider-amount" ).text( "Top "+ bottom + " - " + top );
//        this.filterGraphOnMappingCounts();
    }
    
}