define(["require", "exports"], function(require, exports) {
    var Menu = (function () {
        function Menu() {
            this.menuName = "";
        }
        Menu.prototype.initializeMenu = function (menuName) {
            if (typeof menuName === "undefined") { menuName = Menu.defaultMenuName; }
            this.menuName = menuName;

            // Append the pop-out panel. It will stay hidden except when moused over.
            var trigger = $("<div>").attr("id", Menu.menuTriggerContainerId).addClass(Menu.topBarButtonClass);
            $(Menu.menuBarSelector).append(trigger);
            trigger.append($("<div>").attr("id", Menu.triggerId).addClass("unselectable").text("Menu").append($("<div>").css("float", "right").addClass("unselectable").addClass(Menu.mainMenuButtonClass)));
            trigger.append($("<div>").attr("id", Menu.menuId));

            // Opted for click control only
            //$(Menu.triggerId).hover(
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
            $("#" + Menu.triggerId).click(function (event) {
                event.stopPropagation();
                outerThis.toggleMenu();
            });

            this.updateMenuText();
        };

        Menu.prototype.updateMenuText = function () {
            if ($("#" + Menu.menuId).css("display") === "none") {
                $("#" + Menu.triggerId).removeClass("pressedMenuButton");
                $("#" + Menu.triggerId).attr("title", Menu.menuClosedPrefix + this.menuName);
            } else {
                $("#" + Menu.triggerId).addClass("pressedMenuButton");
                $("#" + Menu.triggerId).attr("title", Menu.menuOpenPrefix + this.menuName);
            }
        };

        Menu.prototype.toggleMenu = function () {
            var _this = this;
            $("#" + Menu.menuId).slideToggle({ duration: "fast", complete: function () {
                    _this.updateMenuText();
                } });
        };

        Menu.prototype.openMenu = function () {
            var _this = this;
            $("#" + Menu.menuId).slideDown({ duration: 0, complete: function () {
                    _this.updateMenuText();
                } });
        };

        Menu.prototype.closeMenuLambda = function () {
            var _this = this;
            return function () {
                $("#" + Menu.menuId).slideUp({ duration: 0, complete: function () {
                        _this.updateMenuText();
                    } });
            };
        };

        Menu.prototype.getMenuSelector = function () {
            return "#" + Menu.menuId;
        };

        Menu.prototype.getMenuBarSelector = function () {
            return Menu.menuBarSelector;
        };

        /**
        * Creates a menu panel that has an outer visible div with a header, that when clicked, shows or hides an inner div.
        * To use, call with appropriate arguments, then use the returned object as follows:
        * 1) attach the outer element to the menu or other html container of your choice. This outer element is always visible.
        * 2) attach your menu's elements to the inner element. They will be shown or hidden.
        */
        Menu.slideToggleHeaderContainer = function (outerContainerId, innerContainerId, labelText, defaultHideContainer) {
            var outerContainer = $("<div>").attr("id", outerContainerId);
            var innerHidingContainer = $("<div>").attr("id", innerContainerId).addClass(Menu.hidingMenuContainerClass);

            if (defaultHideContainer) {
                innerHidingContainer.css("display", "none");
            }

            // This only indicates collapsability and status
            var labelExpanderIcon = $("<label>").addClass(Menu.menuItemExpanderLabelClass).addClass(Menu.menuExpanderButtonClass).addClass("unselectable").attr("unselectable", "on");

            var expanderIndicatorUpdateLambda = function (whenComplete) {
                return function () {
                    if ($(innerHidingContainer).css("display") === "none") {
                        labelExpanderIcon.addClass(Menu.openActionClass);
                        labelExpanderIcon.removeClass(Menu.closeActionClass);
                    } else {
                        labelExpanderIcon.removeClass(Menu.openActionClass);
                        labelExpanderIcon.addClass(Menu.closeActionClass);
                    }
                    if (null != whenComplete) {
                        whenComplete();
                    }
                };
            };

            // The label labels the section, and acts as a huge collapse button
            var label = $("<label>").addClass(Menu.menuLabelClass).addClass("unselectable").attr("unselectable", "on").text(labelText);

            var expanderClickFunction = function (open, whenComplete) {
                // Used for the button, as well as for a programmatic callback for when we want to display the submenu
                // for special purposes.
                if (null != open) {
                    if (open) {
                        $(innerHidingContainer).slideDown('fast', expanderIndicatorUpdateLambda(whenComplete));
                    } else {
                        $(innerHidingContainer).slideUp('fast', expanderIndicatorUpdateLambda(whenComplete));
                    }
                } else {
                    // Don't have a preference of what to do? Toggle it.
                    $(innerHidingContainer).slideToggle('fast', expanderIndicatorUpdateLambda(whenComplete));
                }
            };

            labelExpanderIcon.click(function (event, whenComplete) {
                expanderClickFunction(undefined, whenComplete);
            });
            label.click(function (event, whenComplete) {
                expanderClickFunction(undefined, whenComplete);
            });

            outerContainer.append(labelExpanderIcon);
            outerContainer.append(label);

            // We don't know the default necessarily, so set the icon here.
            expanderIndicatorUpdateLambda()();

            outerContainer.append(innerHidingContainer);

            return { outer: outerContainer, inner: innerHidingContainer, expanderCallback: expanderClickFunction };
        };
        Menu.defaultMenuName = "Menu";
        Menu.menuClosedPrefix = "Show ";
        Menu.menuOpenPrefix = "Hide ";

        Menu.mainMenuButtonClass = "mainMenuButtonIcon";

        Menu.menuExpanderButtonClass = "menuExpanderButton";

        Menu.openActionClass = "menuLabelIconOpenAction";

        Menu.closeActionClass = "menuLabelIconCloseAction";

        Menu.menuLabelClass = "menuLabel";

        Menu.menuItemExpanderLabelClass = "mainMenuLabelExpander";

        Menu.menuId = 'hoveringGraphMenu';

        Menu.menuBarSelector = "#top_menu_bar";

        Menu.topBarButtonClass = "topBarButton";

        Menu.menuTriggerContainerId = "menuTriggerContainer";

        Menu.triggerId = "trigger";

        Menu.hidingMenuContainerClass = "hidingMenu";
        return Menu;
    })();
    exports.Menu = Menu;
});
