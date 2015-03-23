define(["require", "exports", "../Utils", "../GraphView", "Utils"], function (require, exports, Utils, GraphView) {
    var OntologyRenderScaler = (function () {
        function OntologyRenderScaler(vis) {
            var _this = this;
            this.vis = vis;
            this.NODE_MAX_ON_SCREEN_SIZE = 20 * 5;
            this.NODE_MIN_ON_SCREEN_SIZE = 4;
            this.minNodeRawSize = -1;
            this.maxNodeRawSize = -1;
            this.LINK_MAX_ON_SCREEN_SIZE = 7;
            this.LINK_MIN_ON_SCREEN_SIZE = 1;
            this.minLinkRawSize = -1;
            this.maxLinkRawSize = -1;
            this.REFRESH_LOOP_DELAY_MS = 500;
            this.defaultNumOfTermsForSize = 10;
            this.ontologyNodeScalingFunc = function (rawValue, acronym) {
                rawValue = parseInt(rawValue);
                if (rawValue == 0) {
                    return _this.defaultNumOfTermsForSize;
                }
                if (_this.maxNodeRawSize == _this.minNodeRawSize) {
                    return _this.defaultNumOfTermsForSize;
                }
                var factor = _this.computeFactorOfRange(rawValue, _this.minNodeRawSize, _this.maxNodeRawSize);
                var diameter = _this.linearAreaRelativeScaledRangeValue(factor, _this.NODE_MIN_ON_SCREEN_SIZE, _this.NODE_MAX_ON_SCREEN_SIZE);
                if (isNaN(diameter)) {
                    return 0;
                }
                return diameter / 2;
            };
            this.ontologyInnerNodeScalingFunc = function (rawValue, outerRawValue, acronym) {
                rawValue = parseInt(rawValue);
                outerRawValue = parseInt(outerRawValue);
                if (rawValue == 0 || _this.maxNodeRawSize == _this.minNodeRawSize || rawValue > outerRawValue) {
                    return 0;
                }
                if (outerRawValue == _this.minNodeRawSize) {
                    return (rawValue / outerRawValue) * _this.ontologyNodeScalingFunc(outerRawValue, acronym);
                }
                return _this.ontologyNodeScalingFunc(rawValue, acronym);
            };
            this.ontologyLinkScalingFunc = function (rawValue) {
                rawValue = parseInt(rawValue);
                if (_this.maxLinkRawSize == _this.minLinkRawSize) {
                    return rawValue;
                }
                var factor = _this.computeFactorOfRange(rawValue, _this.minLinkRawSize, _this.maxLinkRawSize);
                var thickness = _this.linearAreaRelativeScaledRangeValue(factor, _this.LINK_MIN_ON_SCREEN_SIZE, _this.LINK_MAX_ON_SCREEN_SIZE);
                return thickness / 2;
            };
        }
        OntologyRenderScaler.prototype.updateNodeScalingFactor = function () {
            var outerThis = this;
            var circles = this.vis.selectAll(GraphView.BaseGraphView.nodeSvgClass);
            circles.each(function (d) {
                var basis = parseInt(this.getAttribute("data-radius_basis"));
                if (-1 == outerThis.maxNodeRawSize || basis > outerThis.maxNodeRawSize) {
                    outerThis.maxNodeRawSize = basis;
                }
                if (-1 == outerThis.minNodeRawSize || basis < outerThis.minNodeRawSize) {
                    outerThis.minNodeRawSize = basis;
                }
            });
            circles.transition().attr("r", function (d) {
                return outerThis.ontologyNodeScalingFunc(this.getAttribute("data-radius_basis"), this.getAttribute("id"));
            });
            var innerCircles = this.vis.selectAll(GraphView.BaseGraphView.nodeInnerSvgClass);
            innerCircles.transition().attr("r", function (d) {
                return outerThis.ontologyInnerNodeScalingFunc(this.getAttribute("data-inner_radius_basis"), this.getAttribute("data-outer_radius_basis"), this.getAttribute("id"));
            });
        };
        OntologyRenderScaler.prototype.updateLinkScalingFactor = function () {
            var outerThis = this;
            console.log("Ran update link " + Utils.getTime());
            $.each(this.vis.selectAll(GraphView.BaseGraphView.linkSvgClass)[0], function (i, link) {
                link = $(link);
                var basis = parseInt(link.attr("data-thickness_basis"));
                if (-1 == outerThis.maxLinkRawSize || basis > outerThis.maxLinkRawSize) {
                    outerThis.maxLinkRawSize = basis;
                }
                if (-1 == outerThis.minLinkRawSize || basis < outerThis.minLinkRawSize) {
                    outerThis.minLinkRawSize = basis;
                }
            });
            $.each(this.vis.selectAll(GraphView.BaseGraphView.linkSvgClass)[0], function (i, link) {
                link = $(link);
                link.css("stroke-width", function (d) {
                    return outerThis.ontologyLinkScalingFunc(link.attr("data-thickness_basis"));
                });
            });
        };
        OntologyRenderScaler.prototype.computeRangeRawSize = function (minRawSize, maxRawSize) {
            return Math.max(1, maxRawSize - minRawSize);
        };
        OntologyRenderScaler.prototype.computeFactorOfRange = function (rawValue, minRawSize, maxRawSize) {
            return 1.0 - (maxRawSize - rawValue) / this.computeRangeRawSize(minRawSize, maxRawSize);
        };
        OntologyRenderScaler.prototype.linearAreaRelativeScaledRangeValue = function (factor, minOnScreenSize, maxOnScreenSize) {
            var linearArea = Math.PI * Math.pow(minOnScreenSize, 2) + factor * Math.PI * Math.pow(maxOnScreenSize, 2);
            var diameter = Math.sqrt(linearArea / Math.PI);
            return diameter;
        };
        return OntologyRenderScaler;
    })();
    exports.OntologyRenderScaler = OntologyRenderScaler;
});
