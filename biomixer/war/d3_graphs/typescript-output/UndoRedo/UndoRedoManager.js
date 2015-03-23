define(["require", "exports", "./BreadcrumbTrail", "./BackForwardBreadcrumbButtons", "UndoRedo/BreadcrumbTrail", "UndoRedo/BackForwardBreadcrumbButtons"], function (require, exports, BreadcrumbTrail, BackForwardBreadcrumbButtons) {
    var UndoRedoManager = (function () {
        function UndoRedoManager(initGui, useBackForwardButtons) {
            this.trail = new Array();
            this.currentTrailIndex = -1;
            if (useBackForwardButtons) {
                this.crumblez = new BackForwardBreadcrumbButtons.BackForwardBreadcrumbButtons();
            }
            else {
                this.crumblez = new BreadcrumbTrail.BreadcrumbTrail();
            }
            this.crumblez.undoRedoModel = this;
            if (initGui) {
                this.initGui();
            }
        }
        UndoRedoManager.prototype.initGui = function () {
            this.crumblez.initGui();
        };
        UndoRedoManager.prototype.addCommand = function (newCommand) {
            if (this.trail.indexOf(newCommand) !== -1) {
                return;
            }
            var removed = this.trail.splice(this.currentTrailIndex + 1, (this.trail.length - 1 - this.currentTrailIndex), newCommand);
            this.currentTrailIndex = this.trail.length - 1;
            if (undefined !== this.crumblez) {
                var active = this.crumblez.getActiveCrumb();
                if (undefined !== active) {
                    active.getCommand().snapshotLayout(true);
                }
                this.crumblez.updateView(this.trail, newCommand);
            }
        };
        UndoRedoManager.prototype.removeCommand = function (commandToRemove) {
            var index = this.trail.indexOf(commandToRemove);
            if (-1 === index) {
                return;
            }
            if (this.currentTrailIndex <= index) {
                commandToRemove.executeUndo();
            }
            console.log(this.currentTrailIndex);
            var newTrailIndex = (index > this.currentTrailIndex) ? this.currentTrailIndex : this.trail.length - 2;
            console.log(newTrailIndex);
            this.changeCurrentTrailPosition(this.trail[newTrailIndex]);
            this.trail.splice(index, 1);
            this.crumblez.updateView(this.trail, this.trail[newTrailIndex]);
        };
        UndoRedoManager.prototype.undoOneStep = function () {
            var index = Math.max(0, this.currentTrailIndex - 1);
            this.changeCurrentTrailPosition(this.trail[index]);
        };
        UndoRedoManager.prototype.redoOneStep = function () {
            var index = Math.min(this.currentTrailIndex + 1, this.trail.length - 1);
            this.changeCurrentTrailPosition(this.trail[index]);
        };
        UndoRedoManager.prototype.changeCurrentTrailPosition = function (command) {
            if (null == command) {
                return;
            }
            var commandIndex = this.getCommandIndex(command);
            if (commandIndex === this.currentTrailIndex) {
                return;
            }
            var oldIndex = this.currentTrailIndex;
            this.currentTrailIndex = commandIndex;
            if (undefined !== this.crumblez) {
                this.crumblez.updateActiveCommand(command);
            }
            var increment;
            var undo;
            var stopAtIndex;
            var startAtIndex;
            if (commandIndex < oldIndex) {
                increment = -1;
                undo = true;
                startAtIndex = oldIndex;
                stopAtIndex = commandIndex;
            }
            else {
                increment = +1;
                undo = false;
                startAtIndex = oldIndex + 1;
                stopAtIndex = commandIndex + 1;
            }
            var finalCommand = this.trail[startAtIndex];
            for (var i = startAtIndex; i !== stopAtIndex; i += increment) {
                var anotherCommand = this.trail[i];
                if (undo) {
                    anotherCommand.snapshotLayout(false);
                    anotherCommand.executeUndo();
                    finalCommand = this.trail[stopAtIndex];
                }
                else {
                    anotherCommand.executeRedo();
                    finalCommand = anotherCommand;
                }
            }
            finalCommand.callActiveStepCallback();
            this.crumblez.getActiveCrumb().getCommand().applyLayout();
        };
        UndoRedoManager.prototype.getCommandIndex = function (command) {
            return this.trail.indexOf(command);
        };
        UndoRedoManager.prototype.getCrumbHistory = function () {
            return this.trail.slice(0, this.currentTrailIndex + 1);
        };
        return UndoRedoManager;
    })();
    exports.UndoRedoManager = UndoRedoManager;
});
