///<reference path="headers/require.d.ts" />
define(["require", "exports", "../Utils", "../GraphView", "Utils"], function(require, exports, Utils, GraphView) {
    var OntologyRenderScaler = (function () {
        function OntologyRenderScaler(vis) {
            var _this = this;
            this.vis = vis;
            // Maintaining relative scaled sizes of arcs and nodes depends on updating
            // the raw size range, which in this implementation, loops over all entities.
            // Only update the ranges when appropriate.
            // BioMixer used a 500 ms delay on re-doing things.
            // 20 * 7 seems too big. Got 20 from other transformers.
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
                    // If there is no mapping, I want no dot. This applies to the central node specifically.
                    // I also don't want a teeny weeny inner circle completely covering the outer circle,
                    // so let's scale away those that match the minimum render size.
                    // Otherwise we'll scale exactly the same as the outer circle.
                    return 0;
                }
                if (outerRawValue == _this.minNodeRawSize) {
                    return (rawValue / outerRawValue) * _this.ontologyNodeScalingFunc(outerRawValue, acronym);
                }

                return _this.ontologyNodeScalingFunc(rawValue, acronym);
                // var outerRadius = ontologyNodeScalingFunc(rawValue, acronym);
                // var outerArea = Math.PI*(outerRadius*outerRadius);
                // var innerArea = outerArea * (rawValue / outerRawValue);
                // var innerRadius = outerRadius * (rawValue / outerRawValue);
                // // var innerRadius = Math.sqrt(innerArea/Math.PI);
                //  console.log([acronym, "raw", rawValue / outerRawValue, rawValue, outerRawValue, "area", outerArea/innerArea, outerArea, innerArea, "radius", outerRadius/innerRadius, outerRadius, innerRadius]);
                // return innerRadius;
            };
            this.ontologyLinkScalingFunc = function (rawValue) {
                rawValue = parseInt(rawValue);
                if (_this.maxLinkRawSize == _this.minLinkRawSize) {
                    return rawValue;
                }
                var factor = _this.computeFactorOfRange(rawValue, _this.minLinkRawSize, _this.maxLinkRawSize);

                // The linear area algorithm used for nodes happens to work really well for the edges thickness too.
                var thickness = _this.linearAreaRelativeScaledRangeValue(factor, _this.LINK_MIN_ON_SCREEN_SIZE, _this.LINK_MAX_ON_SCREEN_SIZE);
                return thickness / 2;
            };
        }
        OntologyRenderScaler.prototype.updateNodeScalingFactor = function () {
            // Call this prior to redrawing. The alternative is to track on every size
            // modification. That worked well for BioMixer, but perhaps we're better
            // off doing a bulk computation per size-refreshing redraw that we want to make.
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

            // Inner circles use the same scaling factor.
            var innerCircles = this.vis.selectAll(GraphView.BaseGraphView.nodeInnerSvgClass);
            innerCircles.transition().attr("r", function (d) {
                return outerThis.ontologyInnerNodeScalingFunc(this.getAttribute("data-inner_radius_basis"), this.getAttribute("data-outer_radius_basis"), this.getAttribute("id"));
            });
        };

        OntologyRenderScaler.prototype.updateLinkScalingFactor = function () {
            var outerThis = this;

            // TODO This may not ever need to be called multiple times, but it would take some time to run.
            // Make sure it actually needs to be run if it is indeed called.
            console.log("Ran update link " + Utils.getTime());

            // Call this prior to redrawing. The alternative is to track on every size
            // modification. That worked well for BioMixer, but perhaps we're better
            // off doing a bulk computation per size-refreshing redraw that we want to make.
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

            // Dynamic scoping of "this" required for the D3 function,
            // but we need an object reference closured into it as well. So....outerThis!
            $.each(this.vis.selectAll(GraphView.BaseGraphView.linkSvgClass)[0], function (i, link) {
                // Given a json encoded graph element, update all of the nested elements associated with it
                // cherry pick elements that we might otherwise get by class "node"
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
