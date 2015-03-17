///<reference path="headers/require.d.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", "./BreadcrumbTrail", "UndoRedo/BreadcrumbTrail", "UndoRedo/UndoRedoManager"], function(require, exports, BreadcrumbTrail) {
    /**
    * This class wraps the spatially wide breadcrumb trail into a forward and back button pair, with drop down menus.
    */
    var BackForwardBreadcrumbButtons = (function (_super) {
        __extends(BackForwardBreadcrumbButtons, _super);
        function BackForwardBreadcrumbButtons() {
            _super.call(this);
        }
        BackForwardBreadcrumbButtons.prototype.initGui = function () {
            // The undo and redo button do what they sound like. The undo and redo list are drop down lists
            // of the available undo and redo checkpoints. Openign the list and clicking one of these will also
            // undo or redo the command.
            var _this = this;
            var undoButton = $("<div>").attr("id", BackForwardBreadcrumbButtons.undoButtonId).addClass(BackForwardBreadcrumbButtons.undoRedoButtonClass).addClass(BackForwardBreadcrumbButtons.undoButtonIconClass).attr("title", BackForwardBreadcrumbButtons.undoButtonText);
            var redoButton = $("<div>").attr("id", BackForwardBreadcrumbButtons.redoButtonId).addClass(BackForwardBreadcrumbButtons.undoRedoButtonClass).addClass(BackForwardBreadcrumbButtons.redoButtonIconClass).attr("title", BackForwardBreadcrumbButtons.redoButtonText);

            var undoDropDownButton = $("<div>").addClass(BackForwardBreadcrumbButtons.undoListButtonIconClass).attr("title", BackForwardBreadcrumbButtons.undoListButtonText).addClass(BackForwardBreadcrumbButtons.crumbTextClass);
            var undoList = $("<div>").attr("id", BackForwardBreadcrumbButtons.undoListButtonId).addClass(BackForwardBreadcrumbButtons.undoRedoListButtonClass).append(undoDropDownButton).append($("<div>").attr("id", BackForwardBreadcrumbButtons.undoListCrumbContainerId).addClass(BackForwardBreadcrumbButtons.undoredoVerticalListContainer));
            var redoDropDownButton = $("<div>").addClass(BackForwardBreadcrumbButtons.undoListButtonIconClass).attr("title", BackForwardBreadcrumbButtons.redoListButtonText).addClass(BackForwardBreadcrumbButtons.crumbTextClass);
            var redoList = $("<div>").attr("id", BackForwardBreadcrumbButtons.redoListButtonId).addClass(BackForwardBreadcrumbButtons.undoRedoListButtonClass).append(redoDropDownButton).append($("<div>").attr("id", BackForwardBreadcrumbButtons.redoListCrumbContainerId).addClass(BackForwardBreadcrumbButtons.undoredoVerticalListContainer));

            undoButton.click(function () {
                _this.undoRedoModel.undoOneStep();
            });
            redoButton.click(function () {
                _this.undoRedoModel.redoOneStep();
            });

            undoDropDownButton.click(this.dropdownClickLambda("#" + BackForwardBreadcrumbButtons.undoListCrumbContainerId));
            redoDropDownButton.click(this.dropdownClickLambda("#" + BackForwardBreadcrumbButtons.redoListCrumbContainerId));

            $("#" + BackForwardBreadcrumbButtons.breadcrumbMenuId).append(undoList).append(undoButton).append($("<div>").addClass("undo_redo_spacer")).append(redoButton).append(redoList);
        };

        BackForwardBreadcrumbButtons.prototype.dropdownClickLambda = function (dropdownSelector) {
            $(document).click(function (event) {
                $(dropdownSelector).slideUp();
            });
            return function (event) {
                event.stopPropagation();
                $(dropdownSelector).slideToggle();
            };
        };

        BackForwardBreadcrumbButtons.prototype.addCrumbElement = function (command) {
            var finalCrumb = this.getFinalCrumb();

            var newCrumb = new BreadcrumbTrail.Breadcrumb(command, this);

            // Make it
            var newCrumbElement = $("<div>").attr("id", this.generateCrumbElementId(command)).addClass(BackForwardBreadcrumbButtons.crumbIdPrefixAndClassName).addClass(BackForwardBreadcrumbButtons.verticalCrumbDivClass).click(newCrumb.breadcrumbClickedLambda(newCrumb)).hover(newCrumb.breadcrumbHoveredLambda(newCrumb), newCrumb.breadcrumbUnhoveredLambda(newCrumb));
            var crumbName = $("<p>").text(command.getDisplayName()).addClass(BackForwardBreadcrumbButtons.crumbTextClass);
            newCrumbElement.append(crumbName);
            command.addNameUpdateListener(this.generateCrumbElementId(command), function () {
                crumbName.text(command.getDisplayName());
            });

            // Use it
            // Note that whenever we add a crumb, it is modifying redo, and thus is always going on the
            // top of the undo stack. For the UI, this means we only have things in redo when they have
            // been undone.
            if (null === finalCrumb) {
                // No prev breadcrumb? Use label as parent.
                var crumbElementPredecessor = $("#" + BackForwardBreadcrumbButtons.undoListButtonId);
                crumbElementPredecessor.append(newCrumbElement);
            } else {
                // Use preceding crumb as sibling. This is always in the undo stack, so we put them before,
                // not after, the sibling.
                var crumbElementPredecessor = this.selectCrumbElement(finalCrumb.getCommand());
                crumbElementPredecessor.before(newCrumbElement);
            }

            // Sort it
            this.trailOfCrumbs.push(command.getUniqueId());

            // Store it
            this.trailMap[command.getUniqueId()] = newCrumb;
        };

        BackForwardBreadcrumbButtons.prototype.removeCrumbElement = function (command) {
            // Note that for the back and forward undo lists, we only ever
            // remove from the undo list (when creating a new command head for the undo list)
            // Remove the crumb's element from the GUI
            this.selectCrumbElement(command).remove();

            // Clean up three containers
            var popped = this.trailOfCrumbs.pop();
            if (popped !== command.getUniqueId()) {
                console.log("Sequence problem in breadcrumbs: popped element does not match expected.");
            }
            var crumbElement = this.selectCrumbElement(this.trailMap[command.getUniqueId()].command);
            var isActiveCrumb = crumbElement.hasClass(BackForwardBreadcrumbButtons.activeCrumbClassName);
            delete this.trailMap[command.getUniqueId()];

            // Activate next crumb if this popped one was indeed the active one.
            if (isActiveCrumb) {
                this.updateActiveCommand(this.getNthCrumb(this.trailOfCrumbs.length).command);
            }
        };

        BackForwardBreadcrumbButtons.prototype.updateActiveCommand = function (activeCommand) {
            // For the back and forward undo lists, the active crumb is the top one in
            // the undo list. All commands that are later in the command sequence get
            // shunted to the redo list.
            _super.prototype.updateActiveCommand.call(this, activeCommand);
            var activeCommandIndex = this.undoRedoModel.getCommandIndex(activeCommand);

            var undoContainer = $("#" + BackForwardBreadcrumbButtons.undoListCrumbContainerId);
            var redoContainer = $("#" + BackForwardBreadcrumbButtons.redoListCrumbContainerId);

            var undoEmpty = true;
            var redoEmpty = true;

            for (var i = this.trailOfCrumbs.length - 1; i >= 0; i--) {
                var crumb = this.selectCrumbElement(this.trailMap[this.trailOfCrumbs[i]].command);
                if (i <= activeCommandIndex) {
                    crumb.detach();
                    undoContainer.append(crumb);
                    undoEmpty = false;
                } else {
                    crumb.detach();
                    redoContainer.prepend(crumb);
                    crumb.removeClass(BreadcrumbTrail.BreadcrumbTrail.fadedCrumbClassName);
                    redoEmpty = false;
                }
            }

            // Update the GUI to reflect availability of undo and redo
            if (undoContainer.children("." + BackForwardBreadcrumbButtons.crumbIdPrefixAndClassName).length <= 1) {
                // Minimum one element to undo...we list the initial state, but it cannot be undone.
                $("#" + BackForwardBreadcrumbButtons.undoButtonId).addClass(BackForwardBreadcrumbButtons.disabledButtonClass);
                $("#" + BackForwardBreadcrumbButtons.undoListButtonId).addClass(BackForwardBreadcrumbButtons.disabledButtonClass);
            } else {
                $("#" + BackForwardBreadcrumbButtons.undoButtonId).removeClass(BackForwardBreadcrumbButtons.disabledButtonClass);
                $("#" + BackForwardBreadcrumbButtons.undoListButtonId).removeClass(BackForwardBreadcrumbButtons.disabledButtonClass);
            }
            if (redoContainer.children("." + BackForwardBreadcrumbButtons.crumbIdPrefixAndClassName).length < 1) {
                $("#" + BackForwardBreadcrumbButtons.redoButtonId).addClass(BackForwardBreadcrumbButtons.disabledButtonClass);
                $("#" + BackForwardBreadcrumbButtons.redoListButtonId).addClass(BackForwardBreadcrumbButtons.disabledButtonClass);
            } else {
                $("#" + BackForwardBreadcrumbButtons.redoButtonId).removeClass(BackForwardBreadcrumbButtons.disabledButtonClass);
                $("#" + BackForwardBreadcrumbButtons.redoListButtonId).removeClass(BackForwardBreadcrumbButtons.disabledButtonClass);
            }
        };
        BackForwardBreadcrumbButtons.undoButtonText = "Undo Node Expansion";
        BackForwardBreadcrumbButtons.redoButtonText = "Redo Node Expansion";
        BackForwardBreadcrumbButtons.undoListButtonText = "Click to see list of undo options";
        BackForwardBreadcrumbButtons.redoListButtonText = "Click to see list of redo options";

        BackForwardBreadcrumbButtons.undoButtonIconClass = "action-undo-icon";
        BackForwardBreadcrumbButtons.redoButtonIconClass = "action-redo-icon";
        BackForwardBreadcrumbButtons.undoListButtonIconClass = "caret-bottom-icon";

        BackForwardBreadcrumbButtons.undoRedoButtonClass = "undo_redo_button";
        BackForwardBreadcrumbButtons.undoRedoListButtonClass = "undo_redo_list_button";

        BackForwardBreadcrumbButtons.disabledButtonClass = "undo_redo_button_disabled";

        BackForwardBreadcrumbButtons.undoButtonId = "undo_button";
        BackForwardBreadcrumbButtons.redoButtonId = "redo_button";
        BackForwardBreadcrumbButtons.undoListButtonId = "undo_list_button";
        BackForwardBreadcrumbButtons.redoListButtonId = "redo_list_button";
        BackForwardBreadcrumbButtons.undoListCrumbContainerId = "undo_list_crumb_container";
        BackForwardBreadcrumbButtons.redoListCrumbContainerId = "redo_list_crumb_container";

        BackForwardBreadcrumbButtons.verticalCrumbDivClass = "vertical_stack_bread_crumb_div";
        BackForwardBreadcrumbButtons.undoredoVerticalListContainer = "vertical_stack_bread_crumb_container";
        return BackForwardBreadcrumbButtons;
    })(BreadcrumbTrail.BreadcrumbTrail);
    exports.BackForwardBreadcrumbButtons = BackForwardBreadcrumbButtons;
});
