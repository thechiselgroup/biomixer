///<reference path="headers/require.d.ts" />

///<reference path="headers/jquery.d.ts" />

///<amd-dependency path="GraphView" />

import GraphView = require('./GraphView');

 // TODO This creates a timer per popup, which is sort of silly. Figure out another way.
export function closeOtherTipsyTooltips(event?: JQueryMouseEventObject) {
    $(".tipsy").each(
        function(index: number, tipsyItem){
            var tipsyOwner = $("#"+$(tipsyItem).attr("id").replace("_tipsy", ""));
            // Only undefined when coming from canvas
            if(event === undefined){
                tipsyOwner.tipsy("hide");
            }  else if(
                    $(event.target).attr("id") !== tipsyOwner.attr("id")
                    && $(event.target).parent().attr("id") !== tipsyOwner.attr("id")
                    && event.target !== tipsyItem
                ){
                tipsyOwner.tipsy("hide"); // $(me) down below
            }
        }
    );
}

export function nodeTooltipOnClickLambda(outerThis: GraphView.GraphView<any, any>){
    return function(d){
        var me = this;
        var meData = d;
        var waitingToShowForData = undefined;
        var tipsyId = $(me).attr("id")+"_tipsy";
        var showDelayTimer = undefined;
    
        function attachTipsy(){
            if($("#"+tipsyId).length !== 0){
                return;
            }
            
            $(me).tipsy({
                html: true,
                fade: true,
                // offset: parseInt($(me).attr("r")), // works better without this!
                offset: 15, // need this for the gravity south-east cases. It makes it quite far for the other cases though...
                fallback: "Fetching data...",
                title: function() {
                  // var d = this.__data__, c = d.i; //colors(d.i);
                  // return 'Hi there! My color is <span style="color:' + c + '">' + c + '</span>';
                  return outerThis.createNodePopupTable(me, meData);
                },
                trigger: 'manual',
                gravity: function() {
                    var location = "";
                    
                    if($(me).offset().top > ($(document).scrollTop() + $(window).height() / 2)){
                        location += "s";
                    } else {
                        location += "n";
                    }
                    
                    if($(me).offset().left > ($(document).scrollLeft() + $(window).width() / 2)){
                        location += "e";
                    } else {
                        location += "w";
                    }
                    // console.log("Location "+location);
                    return location;
                },
            });
        }

        var mostRecentNodePosition;
        function mouseDownRecPosition(event: JQueryMouseEventObject) {
            // There's a known bug in JQuery where click events are fired on mouse up events after dragging
            //This click handler will let us inspect to see if the node has moved, and if so, not trigger
            // the popup.
            mostRecentNodePosition = event.pageX+" "+event.pageY;
        }
        
        function clickedNode(event: JQueryMouseEventObject) {
            event.stopPropagation();
            closeOtherTipsyTooltips(event);

            if(outerThis.dragging){
                return;
            }

            var currentPosition = event.pageX+" "+event.pageY;
            if(currentPosition != mostRecentNodePosition){
                // don't trigger popup if we dragged
                return;
            }

            if($("#"+tipsyId).length !== 0){
                return;
            } else {
                attachTipsy();
                if(waitingToShowForData !== meData){
                    clearTimeout(showDelayTimer);
                }
                waitingToShowForData = meData;
                
                // this whole whack is in a delaying timer for hover version
                $(me).tipsy('show');
                // The .tipsy object is destroyed every time it is hidden,
                // so we need to add our listener every time its shown
                var tipsy = $(me).tipsy("tip");
                tipsy.attr("id", tipsyId);
                outerThis.lastDisplayedTipsy = tipsy;
                outerThis.lastDisplayedTipsyData = meData;
                outerThis.lastDisplayedTipsySvg = me;
                
                // For the tipsy specific listeners, change opacity.
                // enter and leave functions used to be triggered, but with clicking it is different.
                tipsy.mouseenter(function(){tipsy.css("opacity",1.0);  }).mouseleave(function(){tipsy.css("opacity",0.8); });
                tipsy.mouseover(function(){
                    tipsy.css("opacity",1.0);
                });
                waitingToShowForData = undefined;
            }
        }
        
        // Finally, bind the mouse handlers defined above.
        $(this).mousedown(mouseDownRecPosition);
        $(this).click(clickedNode);
    }
}