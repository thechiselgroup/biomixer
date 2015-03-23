define(["require", "exports"], function (require, exports) {
    var Menu = (function () {
        function Menu() {
            this.menuName = "";
        }
        Menu.prototype.initializeMenu = function (menuName) {
            if (menuName === void 0) { menuName = Menu.defaultMenuName; }
            this.menuName = menuName;
            var trigger = $("<div>").attr("id", Menu.menuTriggerContainerId).addClass(Menu.topBarButtonClass);
            $(Menu.menuBarSelector).append(trigger);
            trigger.append($("<div>").attr("id", Menu.triggerId).addClass("unselectable").text("Menu").append($("<div>").css("float", "right").addClass("unselectable").addClass(Menu.mainMenuButtonClass)));
            trigger.append($("<div>").attr("id", Menu.menuId));
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
            }
            else {
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
        Menu.slideToggleHeaderContainer = function (outerContainerId, innerContainerId, labelText, defaultHideContainer) {
            var outerContainer = $("<div>").attr("id", outerContainerId);
            var innerHidingContainer = $("<div>").attr("id", innerContainerId).addClass(Menu.hidingMenuContainerClass);
            if (defaultHideContainer) {
                innerHidingContainer.css("display", "none");
            }
            var labelExpanderIcon = $("<label>").addClass(Menu.menuItemExpanderLabelClass).addClass(Menu.menuExpanderButtonClass).addClass("unselectable").attr("unselectable", "on");
            var expanderIndicatorUpdateLambda = function (whenComplete) {
                return function () {
                    if ($(innerHidingContainer).css("display") === "none") {
                        labelExpanderIcon.addClass(Menu.openActionClass);
                        labelExpanderIcon.removeClass(Menu.closeActionClass);
                    }
                    else {
                        labelExpanderIcon.removeClass(Menu.openActionClass);
                        labelExpanderIcon.addClass(Menu.closeActionClass);
                    }
                    if (null != whenComplete) {
                        whenComplete();
                    }
                };
            };
            var label = $("<label>").addClass(Menu.menuLabelClass).addClass("unselectable").attr("unselectable", "on").addClass(Menu.expandableMenuLabelClass).text(labelText);
            var expanderClickFunction = function (open, whenComplete) {
                if (null != open) {
                    if (open) {
                        $(innerHidingContainer).slideDown('fast', expanderIndicatorUpdateLambda(whenComplete));
                    }
                    else {
                        $(innerHidingContainer).slideUp('fast', expanderIndicatorUpdateLambda(whenComplete));
                    }
                }
                else {
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
        Menu.expandableMenuLabelClass = "expandableMenuLabel";
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
