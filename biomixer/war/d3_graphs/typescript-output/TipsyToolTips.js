define(["require", "exports", "GraphView"], function (require, exports) {
    function nodeTooltipOnHoverLambda(outerThis) {
        return function (d) {
            var me = this;
            var meData = d;
            var leaveDelayTimer = null;
            var visible = false;
            var waitingToShowForData = undefined;
            var tipsyId = $(me).attr("id") + "_tipsy";
            var leaveMissedTimer = undefined;
            var showDelayTimer = undefined;
            function missedEventTimer() {
                leaveMissedTimer = setTimeout(missedEventTimer, 1000);
                if ($("#" + me.id + ":hover").length !== 0 && $(tipsyId + ":hover").length !== 0) {
                    console.log("Not in thing " + me.id + " and tipsyId " + tipsyId);
                    leave();
                }
            }
            function leave() {
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
                    offset: 15,
                    fallback: "Fetching data...",
                    title: function () {
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
                        var tipsy = $(me).tipsy("tip");
                        tipsy.attr("id", tipsyId);
                        outerThis.lastDisplayedTipsy = tipsy;
                        outerThis.lastDisplayedTipsyData = meData;
                        outerThis.lastDisplayedTipsySvg = me;
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
        };
    }
    exports.nodeTooltipOnHoverLambda = nodeTooltipOnHoverLambda;
});
