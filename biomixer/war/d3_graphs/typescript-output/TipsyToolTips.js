///<reference path="headers/require.d.ts" />
define(["require", "exports", "GraphView"], function (require, exports) {
    function nodeTooltipOnHoverLambda(outerThis) {
        return function (d) {
            var me = this;
            var meData = d;
            var leaveDelayTimer = null;
            var visible = false;
            var waitingToShowForData = undefined;
            var tipsyId = $(me).attr("id") + "_tipsy";
            // TODO This creates a timer per popup, which is sort of silly. Figure out another way.
            var leaveMissedTimer = undefined;
            var showDelayTimer = undefined;
            function missedEventTimer() {
                leaveMissedTimer = setTimeout(missedEventTimer, 1000);
                // This timer doesn't really work...replacing with click rather than hover popups, so
                // it's not important to fix unless we come back to hovering.
                // The hover check doesn't work when we are over children it seems, and the tipsy has plenty of children...
                if ($("#" + me.id + ":hover").length !== 0 && $(tipsyId + ":hover").length !== 0) {
                    console.log("Not in thing " + me.id + " and tipsyId " + tipsyId);
                    leave();
                }
            }
            function leave() {
                // We add a 100 ms timeout to give the user a little time
                // moving the cursor to/from the tipsy object
                leaveDelayTimer = setTimeout(function () {
                    $(me).tipsy('hide');
                    visible = false;
                    waitingToShowForData = undefined;
                    clearTimeout(showDelayTimer);
                    clearTimeout(leaveMissedTimer);
                }, 100);
            }
            function attachTipsy() {
                if (visible) {
                    return;
                }
                $(me).tipsy({
                    html: true,
                    fade: true,
                    // offset: parseInt($(me).attr("r")), // works better without this!
                    offset: 15,
                    fallback: "Fetching data...",
                    title: function () {
                        // var d = this.__data__, c = d.i; //colors(d.i);
                        // return 'Hi there! My color is <span style="color:' + c + '">' + c + '</span>';
                        return outerThis.createNodePopupTable(me, meData);
                    },
                    trigger: 'manual',
                    gravity: function () {
                        var location = "";
                        if ($(me).offset().top > ($(document).scrollTop() + $(window).height() / 2)) {
                            location += "s";
                        }
                        else {
                            location += "n";
                        }
                        if ($(me).offset().left > ($(document).scrollLeft() + $(window).width() / 2)) {
                            location += "e";
                        }
                        else {
                            location += "w";
                        }
                        // console.log("Location "+location);
                        return location;
                    },
                });
            }
            function enter() {
                if (outerThis.dragging) {
                    return;
                }
                if (visible) {
                    clearTimeout(leaveDelayTimer);
                }
                else {
                    attachTipsy();
                    if (waitingToShowForData !== meData) {
                        clearTimeout(showDelayTimer);
                    }
                    waitingToShowForData = meData;
                    showDelayTimer = setTimeout(function () {
                        missedEventTimer();
                        $(me).tipsy('show');
                        // The .tipsy object is destroyed every time it is hidden,
                        // so we need to add our listener every time its shown
                        var tipsy = $(me).tipsy("tip");
                        tipsy.attr("id", tipsyId);
                        outerThis.lastDisplayedTipsy = tipsy;
                        outerThis.lastDisplayedTipsyData = meData;
                        outerThis.lastDisplayedTipsySvg = me;
                        // For the tipsy specific listeners, change opacity.
                        tipsy.mouseenter(function () {
                            tipsy.css("opacity", 1.0);
                            enter();
                        }).mouseleave(function () {
                            tipsy.css("opacity", 0.8);
                            leave();
                        });
                        tipsy.mouseover(function () {
                            tipsy.css("opacity", 1.0);
                            clearTimeout(leaveMissedTimer);
                        });
                        visible = true;
                        waitingToShowForData = undefined;
                    }, 600);
                }
            }
            $(this).hover(enter, leave);
            //        $(this).mouseover(function(){
            //            console.log("clearing time out in mouse over");
            //            clearTimeout(leaveMissedTimer);
            //        });
            // TODO Use a timer, poll style, to prevent cases where mouse events are missed by browser.
            // That happens commonly. We'll want to hide stale open tipsy panels when this happens.
            // d3.timer(function(){}, -4 * 1000 * 60 * 60, +new Date(2012, 09, 29));
        };
    }
    exports.nodeTooltipOnHoverLambda = nodeTooltipOnHoverLambda;
});
