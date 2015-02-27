///<reference path="headers/require.d.ts" />

///<reference path="headers/jquery.d.ts" />
///<reference path="headers/spin.d.ts" />

export class MouseSpinner {

    // Use the widgets here to reconfigure to taste: http://fgnass.github.io/spin.js/
    static opts = {
      lines: 5, // The number of lines to draw
      length: 0, // The length of each line
      width: 10, // The line thickness
      radius: 4, // The radius of the inner circle
      corners: 1, // Corner roundness (0..1)
      rotate: 0, // The rotation offset
      direction: 1, // 1: clockwise, -1: counterclockwise
      color: '#000', // #rgb or #rrggbb or array of colors
      speed: 1, // Rounds per second
      trail: 60, // Afterglow percentage
      shadow: false, // Whether to render a shadow
      hwaccel: false, // Whether to use hardware acceleration
      className: 'spinner', // The CSS class to assign to the spinner
      zIndex: 2e9, // The z-index (defaults to 2000000000)
      top: '50%', // Top position relative to parent
      left: '50%' // Left position relative to parent
    };
    
    static spinner = null;
    static spinnerDiv = null;
    static outstandingSpinnerRegistry = {};
    static outstandingCount = 0;
    
    static applyMouseSpinner(callbackName: string){
        if(MouseSpinner.spinner == null){
            MouseSpinner.spinnerDiv = $("#mouseSpinnerDiv");
            MouseSpinner.spinnerDiv.css("position", "absolute").hide();
            MouseSpinner.spinner = new Spinner(MouseSpinner.opts);
            $(document).bind("mousemove", function(e){
                MouseSpinner.spinnerDiv.css({
                   left:  e.pageX + 20,
                   top:   e.pageY
                });
            });
        }
        
        if(null != MouseSpinner.outstandingSpinnerRegistry[callbackName]){
            // Multiple requests are ok, since the same related URL might be requested from multiple concepts
            // console.log("Multiple spinner request from callback named: "+callbackName);
        } else {
            MouseSpinner.outstandingSpinnerRegistry[callbackName] = true;
            MouseSpinner.outstandingCount++;
        }
        MouseSpinner.spinnerDiv.show();
        MouseSpinner.spinner.spin(MouseSpinner.spinnerDiv[0]);
    }
    
    static haltSpinner(callbackName: string){
        if(MouseSpinner.outstandingCount === 0){
            return;
        }
        if(null != MouseSpinner.outstandingSpinnerRegistry[callbackName]){
            MouseSpinner.outstandingSpinnerRegistry[callbackName] = undefined;
            delete MouseSpinner.outstandingSpinnerRegistry[callbackName];
            MouseSpinner.outstandingCount--;
        }
        if(MouseSpinner.outstandingCount === 0){
            MouseSpinner.spinner.stop();
            MouseSpinner.spinnerDiv.hide();
        } else {
            // Problems with outstanding spinners? Start by checking here.
            // console.log(MouseSpinner.outstandingSpinnerRegistry);
        }
    }
    
    
}