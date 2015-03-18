///<reference path="headers/require.d.ts" />
define(["require", "exports", "UndoRedo/UndoRedoManager"], function(require, exports) {
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

        /**
        * Call whenever the stack changes (commands added), and when an undo or redo has happened.
        */
        BreadcrumbTrail.prototype.updateView = function (stack, activeCommand) {
            // Walk stack and see what differs from rendered
            // Locate elements to remove; don't wreck loop by changing container contents, right?
            var toRemove = [];
            for (var i = 0; i < this.trailOfCrumbs.length; i++) {
                // Order doesn't matter, so we can use the unordered map.
                var crumbCommand = this.trailMap[this.trailOfCrumbs[i]];
                if (undefined === crumbCommand) {
                    // Nothing; this one is new and unadded.
                } else if (stack.indexOf(crumbCommand.command) < 0) {
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

            // Set the active rendered breadcrumb
            this.updateActiveCommand(activeCommand);
        };

        BreadcrumbTrail.prototype.addCrumbElement = function (command) {
            var finalCrumb = this.getFinalCrumb();
            var crumbElementPredecessor;
            if (null === finalCrumb) {
                // No prev breadcrumb? Use label as sibling.
                crumbElementPredecessor = $("#" + BreadcrumbTrail.breadcrumbTrailLabelId);
            } else {
                crumbElementPredecessor = this.selectCrumbElement(finalCrumb.getCommand());
            }

            var newCrumb = new Breadcrumb(command, this);

            // Make it
            var newCrumbElement = $("<div>").attr("id", this.generateCrumbElementId(command)).addClass(BreadcrumbTrail.crumbIdPrefixAndClassName).click(newCrumb.breadcrumbClickedLambda(newCrumb)).hover(newCrumb.breadcrumbHoveredLambda(newCrumb), newCrumb.breadcrumbUnhoveredLambda(newCrumb));
            var crumbName = $("<p>").text(command.getDisplayName() + BreadcrumbTrail.undoButtonSuffix).addClass("crumb_text");
            newCrumbElement.append(crumbName);
            command.addNameUpdateListener(this.generateCrumbElementId(command), this.updateCrumbText(crumbName, command));

            // Use it
            crumbElementPredecessor.after(newCrumbElement);

            // Sort it
            this.trailOfCrumbs.push(command.getUniqueId());

            // Store it
            this.trailMap[command.getUniqueId()] = newCrumb;
        };

        BreadcrumbTrail.prototype.removeCrumbElement = function (command) {
            // Remove the crumb's element from the GUI
            this.selectCrumbElement(command).remove();

            // Clean up three containers
            var popped = this.trailOfCrumbs.pop();
            if (popped !== command.getUniqueId()) {
                console.log("Sequence problem in breadcrumbs: popped element does not match expected.");
            }
            var crumbElement = this.selectCrumbElement(this.trailMap[command.getUniqueId()].command);
            var isActiveCrumb = crumbElement.hasClass(BreadcrumbTrail.activeCrumbClassName);
            delete this.trailMap[command.getUniqueId()];

            // Activate next crumb if this popped one was indeed the active one.
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
                } else {
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
        // Just a reminder on why I do Lambda(classInstance)...
        // If we don't, we might misuse the lambda by not *calling* it, but instead providing it.
        // Doing so would rescope "this" to be the calling context rather than the lexical scope.
        // Setting outerThis inside the method but outside the function that is returned will
        // be subject to dynamic rescoping, whereas forcing the caller to explicitly provide a
        // first argument that is the object on which we are operating is safer, though very
        // slightly verbose. It is the safest way.
        Breadcrumb.prototype.breadcrumbClickedLambda = function (outerThis) {
            return function () {
                // TODO How do we guarantee that the command is valid? This isn't tied as tightly as the
                // undo/redo model is internally.
                outerThis.breadcrumbTrail.undoRedoModel.changeCurrentTrailPosition(outerThis.command);
            };
        };

        Breadcrumb.prototype.breadcrumbHoveredLambda = function (outerThis) {
            return function () {
                // Very advanced functionality. Might not be implemented.
                outerThis.command.preview();
            };
        };

        Breadcrumb.prototype.breadcrumbUnhoveredLambda = function (outerThis) {
            return function () {
                // Very advanced functionality. Might not be implemented.
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
