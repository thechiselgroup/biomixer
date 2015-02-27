///<reference path="headers/require.d.ts" />
define(["require", "exports", "./Utils", "./Menu", "./Utils", "./Menu"], function(require, exports, Utils, Menu) {
    var AbstractFilterWidget = (function () {
        function AbstractFilterWidget(subMenuTitle) {
            this.subMenuTitle = subMenuTitle;
        }
        AbstractFilterWidget.prototype.modifyClassName = function (newClassName) {
            this.className = newClassName;
        };

        AbstractFilterWidget.prototype.getClassName = function () {
            if (undefined == this.className) {
                this.className = Utils.getClassName(this);
            }
            return this.className;
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

        AbstractFilterWidget.prototype.addResetAndDeleteButtonsToMenuComponents = function (resetHandler, deleteHandler) {
            var menuHeaderContainer = $("#" + this.getClassName() + "OuterContainer");
            menuHeaderContainer.children(".menuLabel").first().before($("<div>").attr("id", this.getClassName() + "ButtonGapper").addClass(AbstractFilterWidget.menuExpanderButtonIconClass).addClass(AbstractFilterWidget.gapperButtonIconClass)).before($("<div>").attr("id", this.getClassName() + "CheckboxDeleteButton").click(deleteHandler).addClass(AbstractFilterWidget.menuExpanderButtonIconClass).addClass(AbstractFilterWidget.deleteNodesButtonIconClass).attr("title", AbstractFilterWidget.deleteNodesButtonText)).before($("<label>").attr("id", this.getClassName() + "CheckboxResetButton").click(resetHandler).addClass(AbstractFilterWidget.menuExpanderButtonIconClass).addClass(AbstractFilterWidget.resetCheckboxesButtonIconClass).attr("title", AbstractFilterWidget.resetCheckboxesButtonText));
        };
        AbstractFilterWidget.menuExpanderButtonIconClass = "menuExpanderUtilityButton";
        AbstractFilterWidget.resetCheckboxesButtonIconClass = "resetCheckboxesButton";
        AbstractFilterWidget.deleteNodesButtonIconClass = "deleteNodesButton";
        AbstractFilterWidget.gapperButtonIconClass = "menuExpanderButtonGapper";
        AbstractFilterWidget.resetCheckboxesButtonText = "Re-check all of these checkboxes";
        AbstractFilterWidget.deleteNodesButtonText = "Delete all unchecked and dimmed nodes from graph";
        return AbstractFilterWidget;
    })();
    exports.AbstractFilterWidget = AbstractFilterWidget;
});
