///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />

/**
 * "The Rube Goldberg Machine of SVG Export; or Whiling Away The Spring Days", by Eric Verbeek
 * 
 * When I tried to achieve this, the solutions did not all fit my needs, but they followed
 * the same overall structure, which I used here. I will not completely justify the whole
 * approach, which is found in the SVG Crowbar (2.0) library [http://nytimes.github.io/svg-crowbar/],
 * canvg library, various stackoverflow solutions RasterizeSvg library, and other sources.
 * This soultion is a combination of the same overall approach found in those, plus code modified
 * from SVG Crowbar 2.0 to reify CSS into SVG markup.
 * 
 * Allows export or printing of an SVG structure, that is styled with external CSS, to PNG.
 * Creates a clone of the Svg target, reifies the CSS styles into the clone markup, converts the
 * clone to an image, renders the image to canvas, and passes the canvas rendering to
 * a new browser tab. It removes the cloned SVG, img and canvas after pushing data to
 * the new tab. The SVG in particular does get added to the initial page's <body>; if it is
 * not, a black export image results.
 */
export class  ExportSvgToImage {
    
    constructor(){
    }
    
    static exportSvgAsPng(svgId: string){
        var instance = new ExportSvgToImage();

        // Not my source, but a nice post of the techniques I found elsewhere: http://techslides.com/save-svg-as-an-image
        // Select the first svg element
        var img = new Image();
    
        var svgGraph = d3.select(svgId); // e.g. "#graphSvg"
        
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
        // Trying to make th eSVG without attaching it to DOM leads to an entirely black exported image.
        var svgClone = d3.select("body").append("svg:svg");
        svgClone.attr("id", "graphclone").attr("width", svgGraph.attr("width")).attr("height", svgGraph.attr("height"));
        svgClone.html(svgGraph.html());
        instance.setInlineStyles(svgClone[0][0]);
        var svgCloneMarkup = svgClone[0][0];
        
//        var svgMarkup = svgGraph[0][0];
//        if(svgMarkup !== svgCloneMarkup){
//            console.log(svgMarkup);
//            console.log(svgCloneMarkup);
//        }
        
        var svgStr = (new XMLSerializer()).serializeToString(svgCloneMarkup);
        svgClone.remove();

        // Also worked with this line here, and no onload (just do contents without callback),
        // but onload needed for IE9 support:
        // img.src = 'data:image/svg+xml;base64,'+window.btoa(svgStr);
    
        // You could also use the actual string without base64 encoding it:
        // img.src = "data:image/svg+xml;utf8," + svgStr;
    
        var canvas = document.createElement("canvas");
        // document.body.appendChild(canvas); // Not actually necessary it seems.
    
        var w = parseInt(svgGraph.attr("width"), 10);
        var h = parseInt(svgGraph.attr("height"), 10);
        canvas.width = w;
        canvas.height = h;
        var ctx = canvas.getContext("2d");
        img.onload = ()=>{
            // NOT using the Blob and URL method form here: https://developer.mozilla.org/en-US/docs/Web/API/Canvas_API/Drawing_DOM_objects_into_a_canvas
            // or here http://stackoverflow.com/questions/11567668/svg-to-canvas-with-d3-js
            ctx.drawImage(img,0,0,w,h);
            // Now save as png or whatever
            var imgUrl = canvas.toDataURL("image/png");
            // console.log(imgUrl);
            
            // http://jsfiddle.net/5z3b5y1o/
            var wnd = window.open(imgUrl, '_blank', ''); // last empty string required to get tab rather than popup
            $(canvas).remove();
            $(img).remove();
            // $(svgClone).remove(); // Did earlier, right?
        }
        img.src = 'data:image/svg+xml;base64,'+window.btoa(svgStr);
    }

    // Modified from Crowbar 2 library:
    private setInlineStyles(svg) {
        var prefix = {
            xmlns: "http://www.w3.org/2000/xmlns/",
            xlink: "http://www.w3.org/1999/xlink",
            svg: "http://www.w3.org/2000/svg"
          };
        var emptySvg = window.document.createElementNS(prefix.svg, 'svg');
        
        var emptySvgDeclarationComputed = getComputedStyle(emptySvg);
        
        var explicitlySetStyle = (element)=>{
          var cSSStyleDeclarationComputed = getComputedStyle(element);
          var i, len, key, value;
          var computedStyleStr = "";
          for (i=0, len=cSSStyleDeclarationComputed.length; i<len; i++) {
            key=cSSStyleDeclarationComputed[i];
            value=cSSStyleDeclarationComputed.getPropertyValue(key);
            if (value!==emptySvgDeclarationComputed.getPropertyValue(key)) {
              computedStyleStr+=key+":"+value+";";
            }
          }
          element.setAttribute('style', computedStyleStr);
        };
        var traverse = (obj)=>{
          var tree = [];
          tree.push(obj);
          visit(obj);
          function visit(node) {
            if (null != node && node.hasChildNodes()) {
              var child = node.firstChild;
              while (child) {
                if (child.nodeType === 1 && child.nodeName != 'SCRIPT'){
                  tree.push(child);
                  visit(child);
                }
                child = child.nextSibling;
              }
            }
          }
          return tree;
        };
        // hardcode computed css styles inside svg
        var allElements = traverse(svg);
        var i = allElements.length;
        while (i--){
          explicitlySetStyle(allElements[i]);
        }
  }
    
    private makeStyleObject(rule) {
        var styleDec = rule.style;
        var output = {};
        var s;
        
        for (s = 0; s < styleDec.length; s++) {
            output[styleDec[s]] = styleDec[styleDec[s]];
            if(styleDec[styleDec[s]] === undefined) {
                //firefox being firefoxy
                output[styleDec[s]] = styleDec.getPropertyValue(styleDec[s])
            }
        }
        
        return output;
    }
    
}