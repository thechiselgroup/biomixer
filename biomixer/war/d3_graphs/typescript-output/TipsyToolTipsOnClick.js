define(["require", "exports", './GraphView', "GraphView"], function (require, exports, GraphView) {
    function closeOtherTipsyTooltips(event) {
        $(".tipsy").each(function (index, tipsyItem) {
            var tipsyOwner = $("#" + $(tipsyItem).attr("id").replace("_tipsy", ""));
            if (event === undefined) {
                tipsyOwner.tipsy("hide");
            }
            else if ($(event.target).attr("id") !== tipsyOwner.attr("id") && $(event.target).parent().attr("id") !== tipsyOwner.attr("id") && event.target !== tipsyItem) {
                tipsyOwner.tipsy("hide");
            }
        });
    }
    exports.closeOtherTipsyTooltips = closeOtherTipsyTooltips;
    function nodeTooltipOnClickLambda(outerThis) {
        return function (d) {
            var me = this;
            var meData = d;
            var waitingToShowForData = undefined;
            var tipsyId = $(me).attr("id") + "_tipsy";
            var showDelayTimer = undefined;
            function attachTipsy() {
                if ($("#" + tipsyId).length !== 0) {
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
            var mostRecentNodePosition;
            function mouseDownRecPosition(event) {
                mostRecentNodePosition = event.pageX + " " + event.pageY;
            }
            function clickedNode(event) {
                event.stopPropagation();
                closeOtherTipsyTooltips(event);
                if (outerThis.dragging) {
                    return;
                }
                var currentPosition = event.pageX + " " + event.pageY;
                if (currentPosition != mostRecentNodePosition) {
                    return;
                }
                if ($("#" + tipsyId).length !== 0) {
                    return;
                }
                else {
                    attachTipsy();
                    if (waitingToShowForData !== meData) {
                        clearTimeout(showDelayTimer);
                    }
                    waitingToShowForData = meData;
                    $(me).tipsy('show');
                    var tipsy = $(me).tipsy("tip");
                    tipsy.attr("id", tipsyId);
                    outerThis.lastDisplayedTipsy = tipsy;
                    outerThis.lastDisplayedTipsyData = meData;
                    outerThis.lastDisplayedTipsySvg = me;
                    tipsy.draggable({ handle: $("#popups-GrabHandle") });
                    tipsy.mouseenter(function () {
                        tipsy.css("opacity", 1.0);
                    }).mouseleave(function () {
                        tipsy.css("opacity", 0.8);
                    });
                    tipsy.mouseover(function () {
                        tipsy.css("opacity", 1.0);
                    });
                    waitingToShowForData = undefined;
                }
            }
            $(this).mousedown(mouseDownRecPosition);
            $(this).children("." + GraphView.BaseGraphView.nodeSvgClassSansDot).first().click(clickedNode);
        };
    }
    exports.nodeTooltipOnClickLambda = nodeTooltipOnClickLambda;
});
