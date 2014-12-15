///<reference path="headers/require.d.ts" />
define(["require", "exports", "./Utils", "./Menu", "./Utils", "./Menu"], function(require, exports, Utils, Menu) {
    var AbstractFilterWidget = (function () {
        function AbstractFilterWidget(subMenuTitle) {
            this.subMenuTitle = subMenuTitle;
        }
        AbstractFilterWidget.prototype.getClassName = function () {
            if (undefined !== this.className) {
                return this.className;
            } else {
                this.className = Utils.getClassName(this);
            }
        };

        AbstractFilterWidget.prototype.getCheckboxSpanClass = function () {
            return this.getClassName() + "_filterCheckboxSpan";
        };

        AbstractFilterWidget.prototype.getCheckboxClass = function () {
            return this.getClassName() + "_filterCheckbox";
        };

        AbstractFilterWidget.prototype.addMenuComponents = function (menuSelector, defaultHideContainer) {
            // This container holds the checkbox widgets
            var containers = Menu.Menu.slideToggleHeaderContainer(this.getClassName() + "OuterContainer", this.className + "ScrollContainer", this.subMenuTitle, defaultHideContainer);
            var outerContainer = containers.outer;
            this.filterContainer = containers.inner;
            this.filterContainer.addClass("scroll-div").css("height", 100);

            // This container is the encapsulating one for this entire widget item.
            $(menuSelector).append(outerContainer);
        };
        return AbstractFilterWidget;
    })();
    exports.AbstractFilterWidget = AbstractFilterWidget;
});
