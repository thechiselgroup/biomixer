///<reference path="headers/require.d.ts" />
define(["require", "exports", './MouseSpinner', "MouseSpinner"], function (require, exports, MouseSpinner) {
    var ExportSvgToImage = (function () {
        function ExportSvgToImage() {
        }
        ExportSvgToImage.exportSvgAsPng = function (svgId) {
            var instance = new ExportSvgToImage();
            MouseSpinner.MouseSpinner.applyMouseSpinner("screenshot");
            var svgStr = instance.getSvgStringClone(svgId, "cloneGraph");
            var testCanvas = document.createElement("canvas");
            if (null != testCanvas.msToBlob) {
                // IE way
                var svgGraph = d3.select("#" + svgId); // e.g. "#graphSvg"
                instance.ieApiWay(svgStr, svgGraph); //, svgHtmlClone);
            }
            else {
                // Chrome, FF way
                instance.pabloDownloadWay(svgStr, "exportImageClone");
            }
            $(testCanvas).remove();
        };
        ExportSvgToImage.prototype.getSvgStringClone = function (svgId, cloneId) {
            // Make a full copy of the SVG, then copy the styles from
            // the original to the new one
            // This is necessary, because the CSS defined styles are not
            // showing up when I export the SVG to an image. Things like label
            // stylings and arc stylings do not render.
            // By copying the structure then the properties, I should get those,
            // and also be able to delete the copy later, to maintain a clean page.
            // Copy all the styles (works exactly the same as before if I skip this part)
            // This CSS copy does not appear to hard-code styles down into the markup
            // var svgClone = d3.select(document.createElementNS('http://www.w3.org/2000/svg', 'svg'));
            // Trying to make the SVG without attaching it to DOM leads to an entirely black exported image.
            d3.select("body").append("div").attr("id", "svgHtmlContainer"); //.append("svg:svg");
            var svgGraph = d3.select("#" + svgId); // e.g. "#graphSvg"
            var origSvgHtml = d3.select(svgGraph.node().parentNode).html();
            origSvgHtml = origSvgHtml.replace('id="' + svgId + '"', 'id="' + cloneId + '"');
            // innerHTML of SVG reached via svgpolyfill library called innersvg.js (for IE in particular)
            document.getElementById("svgHtmlContainer").innerHTML = origSvgHtml;
            // Styles need to be pushed down into the elements, so that when
            // we put it all into the canvas, it renders properly. Canvas ignores
            // CSS classes.
            var svgHtmlClone = d3.select("#" + cloneId);
            this.setInlineStyles(svgHtmlClone[0][0], true);
            var svgHtmlCloneContainer = d3.select("#svgHtmlContainer");
            var svgForSerializing = $(svgHtmlCloneContainer[0][0]).children().first()[0];
            return (new XMLSerializer()).serializeToString(svgForSerializing);
        };
        ExportSvgToImage.prototype.pabloDownloadWay = function (svgStr, cloneId) {
            var pabloCollection = this.pabloGetSvgClone(svgStr, cloneId);
            pabloCollection.download('png', 'biomixer_export_' + Date.now() + '.png', function (result) {
                // console.log(result.error ? 'Failed to export image :(' : 'Successfully exported image :)');
                $("#svgHtmlContainer").remove();
                MouseSpinner.MouseSpinner.haltSpinner("screenshot");
            });
        };
        ExportSvgToImage.prototype.pabloGetSvgClone = function (svgStr, cloneId) {
            var pabloSvg = Pablo(svgStr);
            return Pablo(pabloSvg).crop();
        };
        // More general utility version of things.
        ExportSvgToImage.getPabloSvgClone = function (svgId, cloneId, fullView) {
            var instance = new ExportSvgToImage();
            var clone = instance.pabloGetSvgClone(instance.getSvgStringClone(svgId, cloneId), cloneId);
            if (fullView) {
                // Very important when things are panned and scaled and we want a full view
                d3.select(clone.children()[0]).attr("transform", "translate(0, 0)scale(1)");
            }
            return clone;
        };
        ExportSvgToImage.prototype.ieApiWay = function (svg, svgGraph) {
            var svgStr = (typeof svg === "string") ? svg : null;
            var canvas = document.createElement("canvas");
            var w = parseInt(svgGraph.attr("width"), 10);
            var h = parseInt(svgGraph.attr("height"), 10);
            canvas.width = w;
            canvas.height = h;
            if (svgStr.indexOf('<?xml version=') == -1) {
                svgStr = '<?xml version="1.0"?><!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">' + svgStr;
            }
            canvg(canvas, svgStr, { log: true, useCORS: true, renderCallback: function (dom) {
                var dataURL = canvas.toDataURL('image/png');
                var data = atob(dataURL.substring('data:image/png;base64,'.length));
                var asArray = new Uint8Array(data.length);
                for (var i = 0, len = data.length; i < len; ++i) {
                    asArray[i] = data.charCodeAt(i);
                }
                var blob = new Blob([asArray.buffer], { type: 'image/png' });
                window.navigator.msSaveOrOpenBlob(blob, 'biomixer_export_' + Date.now() + '.png');
                $(canvas).remove();
                $("#svgHtmlContainer").remove();
                MouseSpinner.MouseSpinner.haltSpinner("screenshot");
            } });
        };
        // Modified from Crowbar 2 library:
        ExportSvgToImage.prototype.setInlineStyles = function (svg, stripClasses) {
            var prefix = {
                xmlns: "http://www.w3.org/2000/xmlns/",
                xlink: "http://www.w3.org/1999/xlink",
                svg: "http://www.w3.org/2000/svg"
            };
            var explicitlySetStyle = function (element, parentSvg) {
                var cSSStyleDeclarationComputed = getComputedStyle(element);
                var parentSvgDeclarationComputed = null;
                if (null !== parentSvg) {
                    parentSvgDeclarationComputed = getComputedStyle(parentSvg);
                }
                var i, len, key, value, parentValue;
                // initialize to be the hard coded style of the element
                var computedStyleStr = element.getAttribute('style') || '';
                for (i = 0, len = cSSStyleDeclarationComputed.length; i < len; i++) {
                    key = cSSStyleDeclarationComputed[i];
                    value = cSSStyleDeclarationComputed.getPropertyValue(key);
                    if (null != parentSvg) {
                        parentValue = parentSvgDeclarationComputed.getPropertyValue(key);
                    }
                    else {
                        parentValue = null;
                    }
                    if (parentValue != null && parentValue !== undefined && parentValue !== value && ("" !== value && null !== value && undefined !== value)) {
                        // Get rid of things that will be inherited
                        computedStyleStr += key + ":" + value + ";";
                    }
                }
                element.setAttribute('style', computedStyleStr);
            };
            var traverse = function (obj) {
                var parents = [];
                var tree = [];
                parents.push(null);
                tree.push(obj);
                visit(obj);
                function visit(node) {
                    if (null != node && node.hasChildNodes()) {
                        var child = node.firstChild;
                        while (child) {
                            if (child.nodeType === 1 && child.nodeName != 'SCRIPT') {
                                tree.push(child);
                                parents.push(node);
                                visit(child);
                            }
                            child = child.nextSibling;
                        }
                    }
                }
                return { tree: tree, parents: parents };
            };
            // hardcode computed css styles inside svg
            var allElements = traverse(svg);
            var i = allElements.tree.length;
            while (i--) {
                explicitlySetStyle(allElements.tree[i], allElements.parents[i]);
            }
            if (stripClasses) {
                // Go over them again, strip off classes
                i = allElements.tree.length;
                while (i--) {
                    allElements.tree[i].setAttribute("class", "");
                    allElements.tree[i].removeAttribute("id"); // clones don't need ids, right?
                }
            }
        };
        return ExportSvgToImage;
    })();
    exports.ExportSvgToImage = ExportSvgToImage;
});
