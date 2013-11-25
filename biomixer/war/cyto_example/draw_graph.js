/**
 * Sample HTML5 page for network visualization with cytoscape.js
 *
 * @type {string}
 */
(function () {
    "use strict";

    var NETWORK_DATA_URI = 'http://localhost:9988/v1/networks/views';
//    var NETWORK_LOCAL_DATA_URI = 'data/galFiltered2.json';
    var VISUAL_STYLE_URI = 'cyto_example/data/vs.json';
    var NETWORK_LOCAL_DATA_URI = 'cyto_example/data/nexo.json';
//    var VISUAL_STYLE_URI = 'data/vs.json';

    var NETWORK_WINDOW_TAG = '#network-view';

    $(function () {

        var originalPositions = {}, networkData = null, visualStyleDict = {}, requests = [];

        function getPositions(nodes) {
            var nodeCount = nodes.length;
            for (var i = 0; i < nodeCount; i++) {
                var node = nodes[i];
                var position = node.position;
                originalPositions[node.data.id] = {x: position.x, y: position.y};

            }

            console.log('Position = ' + JSON.stringify(originalPositions));
            return originalPositions;
        }

        requests.push(
            $.getJSON(NETWORK_LOCAL_DATA_URI, function (json) {
                networkData = json;
            })
        );

        requests.push(
            $.getJSON(VISUAL_STYLE_URI, function (json) {
                var visualStyles = json;
                for(var i=0; i<visualStyles.length; i++) {
                    visualStyleDict[visualStyles[i].title] = visualStyles[i].style;
                }
            })
        );

        $.when.apply($, requests).done(function () {
            originalPositions = getPositions(networkData.elements.nodes);
            drawNetwork(networkData, visualStyleDict);
        });

        function drawNetwork(cyNetwork, vsDict) {

            var defaultStyle = vsDict['default'];

            console.log('Drawing Network: Style = ' + defaultStyle);

            var elementCount = defaultStyle.length;

            $(NETWORK_WINDOW_TAG)
                .cytoscape({
                    elements: {
                        nodes: cyNetwork.elements.nodes,
                        edges: cyNetwork.elements.edges
                    },

                    style: (function () {
                        var curStyleObj = cytoscape.stylesheet();

                        for (var i = 0; i < elementCount; i++) {
                            console.log('# Selector = ' + defaultStyle[i].selector);
                            console.log('# CSS = ' + JSON.stringify(defaultStyle[i].css));
                            curStyleObj.selector(defaultStyle[i].selector).css(defaultStyle[i].css);
                        }

                        // Set some js-dependent props
                        return curStyleObj.selector(".ui-cytoscape-edgehandles-source")
                            .css({
                                "border-color": "#5CC2ED",
                                "border-width": 3
                            })
                            .selector(".ui-cytoscape-edgehandles-target, node.ui-cytoscape-edgehandles-preview")
                            .css({
                                "background-color": "#5CC2ED"
                            })
                            .selector("edge.ui-cytoscape-edgehandles-preview")
                            .css({
                                "line-color": "#5CC2ED"
                            })
                            .selector("node.ui-cytoscape-edgehandles-preview, node.intermediate")
                            .css({
                                "shape": "rectangle",
                                "width": 15,
                                "height": 15
                            });
                    })(),

                    ready: function () {
                        window.cy = this;

                        cy.layout({
                            name: 'preset',
                            positions: originalPositions
                        });
                    }
                });
        }
    });

}).call(this);

