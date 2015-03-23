define(["require", "exports", "UndoRedo/UndoRedoManager"], function (require, exports) {
    var BreadcrumbTrail = (function () {
        function BreadcrumbTrail() {
            this.trailOfCrumbs = new Array();
            this.trailMap = {};
        }
        BreadcrumbTrail.prototype.initGui = function () {
            $("#" + BreadcrumbTrail.breadcrumbMenuId).append($("<div>").attr("id", BreadcrumbTrail.breadcrumbTrailLabelId).append($("<p>").text(BreadcrumbTrail.undoMenuText).addClass(BreadcrumbTrail.crumbTextClass)));
        };
        BreadcrumbTrail.prototype.updateCrumbText = function (crumbNameDisplay, command) {
            return function () {
                crumbNameDisplay.text(command.getDisplayName() + BreadcrumbTrail.undoButtonSuffix);
            };
        };
        BreadcrumbTrail.prototype.updateView = function (stack, activeCommand) {
            var toRemove = [];
            for (var i = 0; i < this.trailOfCrumbs.length; i++) {
                var crumbCommand = this.trailMap[this.trailOfCrumbs[i]];
                if (undefined === crumbCommand) {
                }
                else if (stack.indexOf(crumbCommand.command) < 0) {
                    toRemove.push(crumbCommand);
                }
            }
            for (var i = 0; i < toRemove.length; i++) {
                this.removeCrumbElement(toRemove[i].command);
            }
            for (var i = 0; i < stack.length; i++) {
                var crumbElement = this.selectCrumbElement(stack[i]);
                if (0 === crumbElement.length || undefined === this.trailMap[stack[i].getUniqueId()]) {
                    this.addCrumbElement(stack[i]);
                }
            }
            this.updateActiveCommand(activeCommand);
        };
        BreadcrumbTrail.prototype.addCrumbElement = function (command) {
            var finalCrumb = this.getFinalCrumb();
            var crumbElementPredecessor;
            if (null === finalCrumb) {
                crumbElementPredecessor = $("#" + BreadcrumbTrail.breadcrumbTrailLabelId);
            }
            else {
                crumbElementPredecessor = this.selectCrumbElement(finalCrumb.getCommand());
            }
            var newCrumb = new Breadcrumb(command, this);
            var newCrumbElement = $("<div>").attr("id", this.generateCrumbElementId(command)).addClass(BreadcrumbTrail.crumbIdPrefixAndClassName).click(newCrumb.breadcrumbClickedLambda(newCrumb)).hover(newCrumb.breadcrumbHoveredLambda(newCrumb), newCrumb.breadcrumbUnhoveredLambda(newCrumb));
            var crumbName = $("<p>").text(command.getDisplayName() + BreadcrumbTrail.undoButtonSuffix).addClass("crumb_text");
            newCrumbElement.append(crumbName);
            command.addNameUpdateListener(this.generateCrumbElementId(command), this.updateCrumbText(crumbName, command));
            crumbElementPredecessor.after(newCrumbElement);
            this.trailOfCrumbs.push(command.getUniqueId());
            this.trailMap[command.getUniqueId()] = newCrumb;
        };
        BreadcrumbTrail.prototype.removeCrumbElement = function (command) {
            this.selectCrumbElement(command).remove();
            var popped = this.trailOfCrumbs.pop();
            if (popped !== command.getUniqueId()) {
                console.log("Sequence problem in breadcrumbs: popped element does not match expected.");
            }
            var crumbElement = this.selectCrumbElement(this.trailMap[command.getUniqueId()].command);
            var isActiveCrumb = crumbElement.hasClass(BreadcrumbTrail.activeCrumbClassName);
            delete this.trailMap[command.getUniqueId()];
            if (isActiveCrumb) {
                this.updateActiveCommand(this.getNthCrumb(this.trailOfCrumbs.length).command);
            }
        };
        BreadcrumbTrail.prototype.getActiveCrumb = function () {
            return this.getNthCrumb(this.activeCommandIndex);
        };
        BreadcrumbTrail.prototype.getNthCrumb = function (n) {
            if (n > this.trailOfCrumbs.length || n < 0) {
                return null;
            }
            return this.trailMap[this.trailOfCrumbs[n]];
        };
        BreadcrumbTrail.prototype.getFinalCrumb = function () {
            return this.getNthCrumb(this.trailOfCrumbs.length - 1);
        };
        BreadcrumbTrail.prototype.updateActiveCommand = function (activeCommand) {
            this.selectAllCrumbElements().removeClass(BreadcrumbTrail.activeCrumbClassName);
            var activeCrumb = this.selectCrumbElement(activeCommand);
            this.activeCommandIndex = this.undoRedoModel.getCommandIndex(activeCommand);
            if (activeCommand != null) {
                this.selectCrumbElement(activeCommand).addClass(BreadcrumbTrail.activeCrumbClassName);
            }
            for (var i = this.trailOfCrumbs.length - 1; i >= 0; i--) {
                var crumb = this.selectCrumbElement(this.trailMap[this.trailOfCrumbs[i]].command);
                if (i <= this.activeCommandIndex) {
                    crumb.removeClass(BreadcrumbTrail.fadedCrumbClassName);
                }
                else {
                    crumb.addClass(BreadcrumbTrail.fadedCrumbClassName);
                }
            }
        };
        BreadcrumbTrail.prototype.selectAllCrumbElements = function () {
            return $("." + BreadcrumbTrail.crumbIdPrefixAndClassName);
        };
        BreadcrumbTrail.prototype.selectCrumbElement = function (crumbCommand) {
            return $("#" + this.generateCrumbElementId(crumbCommand));
        };
        BreadcrumbTrail.prototype.generateCrumbElementId = function (crumbCommand) {
            return BreadcrumbTrail.crumbIdPrefixAndClassName + crumbCommand.getUniqueId();
        };
        BreadcrumbTrail.prototype.getCrumb = function (activeCommand) {
            var crumbId = this.computeCrumbId(activeCommand);
            return this.trailOfCrumbs[crumbId];
        };
        BreadcrumbTrail.prototype.computeCrumbId = function (command) {
            return command.getUniqueId();
        };
        BreadcrumbTrail.breadcrumbMenuId = "undo_redo_breadcrumb_trail";
        BreadcrumbTrail.breadcrumbTrailLabelId = "undo_redo_breadcrumb_label";
        BreadcrumbTrail.crumbIdPrefixAndClassName = "crumb_for_";
        BreadcrumbTrail.activeCrumbClassName = "active_crumb";
        BreadcrumbTrail.fadedCrumbClassName = "faded_crumb";
        BreadcrumbTrail.crumbTextClass = "crumb_text";
        BreadcrumbTrail.undoMenuText = "Undo/Redo >> ";
        BreadcrumbTrail.undoButtonSuffix = " >";
        return BreadcrumbTrail;
    })();
    exports.BreadcrumbTrail = BreadcrumbTrail;
    var Breadcrumb = (function () {
        function Breadcrumb(command, breadcrumbTrail) {
            this.command = command;
            this.breadcrumbTrail = breadcrumbTrail;
        }
        Breadcrumb.prototype.breadcrumbClickedLambda = function (outerThis) {
            return function () {
                outerThis.breadcrumbTrail.undoRedoModel.changeCurrentTrailPosition(outerThis.command);
            };
        };
        Breadcrumb.prototype.breadcrumbHoveredLambda = function (outerThis) {
            return function () {
                outerThis.command.preview();
            };
        };
        Breadcrumb.prototype.breadcrumbUnhoveredLambda = function (outerThis) {
            return function () {
                outerThis.command.preview();
            };
        };
        Breadcrumb.prototype.getCommand = function () {
            return this.command;
        };
        return Breadcrumb;
    })();
    exports.Breadcrumb = Breadcrumb;
});
