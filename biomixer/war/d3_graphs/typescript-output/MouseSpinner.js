///<reference path="headers/require.d.ts" />
define(["require", "exports"], function(require, exports) {
    ///<reference path="headers/jquery.d.ts" />
    ///<reference path="headers/spin.d.ts" />
    var MouseSpinner = (function () {
        function MouseSpinner() {
        }
        MouseSpinner.applyMouseSpinner = function (callbackName) {
            if (MouseSpinner.spinner == null) {
                MouseSpinner.spinnerDiv = $("#mouseSpinnerDiv");
                MouseSpinner.spinnerDiv.css("position", "absolute").hide();
                MouseSpinner.spinner = new Spinner(MouseSpinner.opts);
                $(document).bind("mousemove", function (e) {
                    MouseSpinner.spinnerDiv.css({
                        left: e.pageX + 20,
                        top: e.pageY
                    });
                });
            }

            if (null != MouseSpinner.outstandingSpinnerRegistry[callbackName]) {
                // Multiple requests are ok, since the same related URL might be requested from multiple concepts
                // console.log("Multiple spinner request from callback named: "+callbackName);
            } else {
                MouseSpinner.outstandingSpinnerRegistry[callbackName] = true;
                MouseSpinner.outstandingCount++;
            }
            MouseSpinner.spinnerDiv.show();
            MouseSpinner.spinner.spin(MouseSpinner.spinnerDiv[0]);
        };

        MouseSpinner.haltSpinner = function (callbackName) {
            if (MouseSpinner.outstandingCount === 0) {
                return;
            }
            if (null != MouseSpinner.outstandingSpinnerRegistry[callbackName]) {
                MouseSpinner.outstandingSpinnerRegistry[callbackName] = undefined;
                delete MouseSpinner.outstandingSpinnerRegistry[callbackName];
                MouseSpinner.outstandingCount--;
            }
            if (MouseSpinner.outstandingCount === 0) {
                MouseSpinner.spinner.stop();
                MouseSpinner.spinnerDiv.hide();
            } else {
                // Problems with outstanding spinners? Start by checking here.
                // console.log(MouseSpinner.outstandingSpinnerRegistry);
            }
        };
        MouseSpinner.opts = {
            lines: 5,
            length: 0,
            width: 10,
            radius: 4,
            corners: 1,
            rotate: 0,
            direction: 1,
            color: '#000',
            speed: 1,
            trail: 60,
            shadow: false,
            hwaccel: false,
            className: 'spinner',
            zIndex: 2e9,
            top: '50%',
            left: '50%'
        };

        MouseSpinner.spinner = null;
        MouseSpinner.spinnerDiv = null;
        MouseSpinner.outstandingSpinnerRegistry = {};
        MouseSpinner.outstandingCount = 0;
        return MouseSpinner;
    })();
    exports.MouseSpinner = MouseSpinner;
});
