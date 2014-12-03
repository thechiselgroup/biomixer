define(["require", "exports"], function(require, exports) {
    var Menu = (function () {
        function Menu() {
            this.menuSelector = 'div#hoveringGraphMenu';
            this.menuBarSelector = "#top_menu_bar";
            this.menuName = "";
        }
        Menu.prototype.initializeMenu = function (menuName) {
            var _this = this;
            if (typeof menuName === "undefined") { menuName = Menu.defaultMenuName; }
            this.menuName = menuName;

            // Append the pop-out panel. It will stay hidden except when moused over.
            var trigger = $("<div>").attr("id", "menuTriggerContainer");

            //        $("#chart").append(trigger);
            $(this.menuBarSelector).append(trigger);
            trigger.append($("<div>").attr("id", "trigger").addClass("unselectable").text(Menu.menuClosedPrefix + menuName)); // "<< Menu" by default
            trigger.append($("<div>").attr("id", "hoveringGraphMenu"));

            // Opted for click control only
            //$('#trigger').hover(
            //        (e) => {
            //            $(this.menuSelector).show(); //.css('top', e.pageY).css('left', e.pageX);
            //             // Looks bad when it's not fully visible, due to children inheriting transparency
            //            $(this.menuSelector).fadeTo(0, 1.0);
            //        },
            //        function() {
            //        //  $(menuSelector).hide();
            //        }
            //);
            var outerThis = this;
            $('#trigger').click(function (event) {
                event.stopPropagation();
                $(_this.menuSelector).slideToggle({ duration: "fast", complete: function () {
                        outerThis.updateMenuText();
                    } });
            });
        };

        Menu.prototype.updateMenuText = function () {
            if ($(this.menuSelector).css("display") === "none") {
                $('#trigger').text(Menu.menuClosedPrefix + this.menuName);
            } else {
                $('#trigger').text(Menu.menuOpenPrefix + this.menuName);
            }
        };

        Menu.prototype.closeMenuLambda = function () {
            var _this = this;
            return function () {
                $(_this.menuSelector).slideUp();
            };
        };

        Menu.prototype.getMenuSelector = function () {
            return this.menuSelector;
        };

        Menu.prototype.getMenuBarSelector = function () {
            return this.menuBarSelector;
        };

        /**
        * Creates a menu panel that has an outer visible div with a header, that when clicked, shows or hides an inner div.
        * To use, call with appropriate arguments, then use the returned object as follows:
        * 1) attach the outer element to the menu or other html container of your choice. This outer element is always visible.
        * 2) attach your menu's elements to the inner element. They will be shown or hidden.
        */
        Menu.slideToggleHeaderContainer = function (outerContainerId, innerContainerId, labelText, defaultHideContainer) {
            var outerContainer = $("<div>").attr("id", outerContainerId);
            var innerHidingContainer = $("<div>").attr("id", innerContainerId);

            if (defaultHideContainer) {
                innerHidingContainer.css("display", "none");
            }

            // This only indicates collapsability and status
            var labelExpanderIcon = $("<label>").addClass(Menu.menuItemExpanderLabelClass).addClass("unselectable").attr("unselectable", "on").text("+");

            // The label labels the section, and acts as a huge collapse button
            var label = $("<label>").addClass(Menu.menuLabelClass).addClass("unselectable").attr("unselectable", "on").text(labelText);

            var expanderClickFunction = function (open) {
                // Used for the button, as well as for a programmatic callback for when we want to display the submenu
                // for special purposes.
                var expanderIndicatorUpdate = function () {
                    labelExpanderIcon.text($(innerHidingContainer).css("display") === "none" ? "+" : "-");
                };
                if (undefined !== open) {
                    if (open) {
                        $(innerHidingContainer).slideDown('fast', expanderIndicatorUpdate);
                    } else {
                        $(innerHidingContainer).slideUp('fast', expanderIndicatorUpdate);
                    }
                } else {
                    // Don't have a preference of what to do? Toggle it.
                    $(innerHidingContainer).slideToggle('fast', expanderIndicatorUpdate);
                }
            };

            labelExpanderIcon.click(function () {
                expanderClickFunction();
            });
            label.click(function () {
                expanderClickFunction();
            });

            outerContainer.append(labelExpanderIcon);
            outerContainer.append(label);

            // innerHidingContainer.css("display", "none");
            // We don't know the default necessarily, so set the text here.
            labelExpanderIcon.text($(innerHidingContainer).css("display") === "none" ? "+" : "-");

            outerContainer.append(innerHidingContainer);

            return { outer: outerContainer, inner: innerHidingContainer, expanderCallback: expanderClickFunction };
        };
        Menu.defaultMenuName = "Menu";
        Menu.menuClosedPrefix = "Show ";
        Menu.menuOpenPrefix = "Hide ";

        Menu.menuLabelClass = "menuLabel";

        Menu.menuItemExpanderLabelClass = "menuLabelExpander";
        return Menu;
    })();
    exports.Menu = Menu;
});
