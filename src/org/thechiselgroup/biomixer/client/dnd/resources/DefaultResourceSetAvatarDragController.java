/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.dnd.resources;

import static org.thechiselgroup.biomixer.client.core.configuration.ChooselInjectionConstants.ROOT_PANEL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.Rectangle;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.biomixer.client.core.ui.CSS;
import org.thechiselgroup.biomixer.client.core.ui.shade.ShadeManager;
import org.thechiselgroup.biomixer.client.core.util.RemoveHandle;
import org.thechiselgroup.biomixer.client.core.util.math.MathUtils;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.HighlightingModel;
import org.thechiselgroup.biomixer.client.dnd.windows.Desktop;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowPanel;

import com.allen_sauer.gwt.dnd.client.AbstractDragController;
import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.BoundaryDropController;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
import com.allen_sauer.gwt.dnd.client.util.DragClientBundle;
import com.allen_sauer.gwt.dnd.client.util.Location;
import com.allen_sauer.gwt.dnd.client.util.WidgetLocation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.name.Named;

// TODO move area calculation to preview package & use delegation instead
// (composition when creating drag avatar drag controller while setting
// up system)
/*
 * This class is not safe for multiple simultaneous drags (e.g. multi touch interfaces)
 */
public class DefaultResourceSetAvatarDragController extends
        AbstractDragController implements ResourceSetAvatarDragController {

    /**
     * TODO Decide if 100ms is a good number
     */
    private final static int CACHE_TIME_MILLIS = 100;

    private static final String CSS_SHADE_CLASS = "shade";

    // TODO document // move??
    private static final int CSS_SHADE_Z_INDEX = 1;

    private static void checkGWTIssue1813(Widget child, AbsolutePanel parent) {
        if (!GWT.isScript()) {
            if (child.getElement().getOffsetParent() != parent.getElement()) {
                DOMUtil.reportFatalAndThrowRuntimeException("The boundary panel for this drag controller does not appear to have"
                        + " 'position: relative' CSS applied to it."
                        + " This may be due to custom CSS in your application, although this"
                        + " is often caused by using the result of RootPanel.get(\"some-unique-id\") as your boundary"
                        + " panel, as described in GWT issue 1813"
                        + " (http://code.google.com/p/google-web-toolkit/issues/detail?id=1813)."
                        + " Please star / vote for this issue if it has just affected your application."
                        + " You can often remedy this problem by adding one line of code to your application:"
                        + " boundaryPanel.getElement().getStyle().setProperty(\"position\", \"relative\");");
            }
        }
    }

    private static List<Rectangle> toRectangles(List<Area> areas) {
        List<Rectangle> rectangles = new ArrayList<Rectangle>();
        for (Area dropArea : areas) {
            rectangles.add(dropArea.getRectangle());
        }
        return rectangles;
    }

    /**
     * The implicit boundary drop controller.
     */
    private final BoundaryDropController boundaryDropController;

    private Rectangle boundaryRectangle = new Rectangle(0, 0, 0, 0);

    private final Desktop desktop;

    private ResourceSetAvatar dragProxy;

    private final Map<Widget, ResourceSetAvatarDropController> dropControllers = new HashMap<Widget, ResourceSetAvatarDropController>();

    private long lastResetCacheTimeMillis;

    /**
     * shade background elements for drop targets with rounded corners
     */
    private final List<Element> shadeElements = new ArrayList<Element>();

    private final ShadeManager shadeManager;

    private RemoveHandle shadeRemoveHandle;

    private final List<Widget> temporaryDropTargets = new ArrayList<Widget>();

    private List<Area> visibleDropAreas;

    private final HighlightingModel hoverModel;

    private final ErrorHandler errorHandler;

    @Inject
    public DefaultResourceSetAvatarDragController(
            @Named(ROOT_PANEL) AbsolutePanel panel, Desktop desktop,
            ShadeManager shadeManager, HighlightingModel hoverModel,
            ErrorHandler errorHandler) {

        super(panel);

        assert shadeManager != null;
        assert hoverModel != null;
        assert errorHandler != null;

        this.hoverModel = hoverModel;
        this.shadeManager = shadeManager;
        this.desktop = desktop;
        this.errorHandler = errorHandler;

        this.boundaryDropController = new BoundaryDropController(panel, false);

        setBehaviorDragStartSensitivity(2);
    }

    private void addShade() {
        List<Rectangle> visibleRectangles = toRectangles(visibleDropAreas);
        shadeRemoveHandle = shadeManager.showShade(visibleRectangles);

        insertShadeBehindRoundedDropTargets();
    }

    /*
     * This code is from the dnd library and needs TODO refactoring. There are
     * several possible side effects that need to be checked.
     */
    private void calculateBoundaryOffset() {
        assert context.boundaryPanel == getBoundaryPanel();

        AbsolutePanel boundaryPanel = getBoundaryPanel();
        Location widgetLocation = new WidgetLocation(boundaryPanel, null);
        Element boundaryElement = boundaryPanel.getElement();

        int left = widgetLocation.getLeft()
                + DOMUtil.getBorderLeft(boundaryElement);
        int top = widgetLocation.getTop()
                + DOMUtil.getBorderTop(boundaryElement);
        boundaryRectangle = boundaryRectangle.move(left, top);

    }

    private void calculateBoundaryParameters() {
        calculateBoundaryOffset();

        Element element = getBoundaryPanel().getElement();

        int width = DOMUtil.getClientWidth(element);
        int height = DOMUtil.getClientHeight(element);
        boundaryRectangle = boundaryRectangle.resize(width, height);
    }

    // TODO change to calculate visible areas with reference to drop controller
    private void calculateDropAreas() {
        List<Area> windowAreas = getWindowAreas();
        List<Area> dropTargetAreas = getDropTargetAreas();

        List<Area> dropAreas = new ArrayList<Area>();
        for (Area dropArea : dropTargetAreas) {
            dropAreas.addAll(dropArea.getVisibleParts(windowAreas));
        }

        visibleDropAreas = dropAreas;
    }

    /**
     * Calculates which of the hidden drop targets are relevant to this drop
     * operation and stores them as temporary drop targets.
     */
    private void calculateTemporaryDropTargets() {
        for (ResourceSetAvatarDropController dropController : getAvailableDropControllers()) {
            Widget dropTarget = dropController.getDropTarget();
            if (!dropTarget.isVisible()) {
                temporaryDropTargets.add(dropTarget);
            }
        }
    }

    private boolean canDropOn(ResourceSetAvatarDropController dropController) {
        return dropController.canDrop(context);
    }

    private void clearDropAreas() {
        visibleDropAreas = null;
    }

    private void clearTemporaryDropTargets() {
        temporaryDropTargets.clear();
    }

    /**
     * Creates a shade element that has bounds of drop target element, but an
     * absolute location, and is positioned behind the drop target.
     */
    private Element createShadeElement(Widget dropTarget) {
        Element shade = DOM.createSpan();

        shade.addClassName(CSS_SHADE_CLASS);

        CSS.setZIndex(shade, CSS_SHADE_Z_INDEX);

        WindowPanel window = getWindow(dropTarget);
        CSS.setAbsoluteBounds(shade,
                dropTarget.getAbsoluteLeft() - window.getAbsoluteLeft(),
                dropTarget.getAbsoluteTop() - window.getAbsoluteTop(),
                dropTarget.getOffsetWidth(), dropTarget.getOffsetHeight());

        return shade;
    }

    @Override
    public void dragEnd() {
        setResourceSetHighlighted(false);
        removeShade();
        setTemporaryDropTargetsVisible(false);
        clearTemporaryDropTargets();
        clearDropAreas();
        notifyDropControllerOnDragEnd();
        removeDragProxy();

        super.dragEnd();
    }

    @Override
    public void dragMove() {
        updateCacheAndBoundary();
        moveDragProxy();
        updateDropController();
    }

    @Override
    public void dragStart() {
        super.dragStart();

        updateLastCacheResetTime();
        initDragProxy();
        calculateBoundaryParameters();
        calculateTemporaryDropTargets();
        setTemporaryDropTargetsVisible(true);
        calculateDropAreas();
        addShade();
        setResourceSetHighlighted(true);
    }

    private List<ResourceSetAvatarDropController> getAvailableDropControllers() {
        List<ResourceSetAvatarDropController> availableControllers = new ArrayList<ResourceSetAvatarDropController>();
        for (ResourceSetAvatarDropController dropController : dropControllers
                .values()) {

            /*
             * We use an error handler to make sure exception in the drop
             * controllers are recorded, but do not affect the overall drag
             * operation.
             */
            try {
                if (canDropOn(dropController)) {
                    availableControllers.add(dropController);
                }
            } catch (Throwable ex) {
                errorHandler.handleError(ex);
            }
        }
        return availableControllers;
    }

    private DraggableResourceSetAvatar getAvatar(DragContext context) {
        assert context != null;
        assert context.draggable != null;
        assert context.draggable instanceof DraggableResourceSetAvatar : "context.draggable is not of type DraggableResourceSetAvatar";
        return (DraggableResourceSetAvatar) context.draggable;
    }

    private int getDesiredLeft() {
        int desiredLeft = context.desiredDraggableX - boundaryRectangle.getX();
        if (getBehaviorConstrainedToBoundaryPanel()) {
            desiredLeft = MathUtils.restrictToInterval(
                    boundaryRectangle.getWidth()
                            - context.draggable.getOffsetWidth(), 0,
                    desiredLeft);
        }
        return desiredLeft;
    }

    private int getDesiredTop() {
        int desiredTop = context.desiredDraggableY - boundaryRectangle.getY();
        if (getBehaviorConstrainedToBoundaryPanel()) {
            desiredTop = MathUtils.restrictToInterval(
                    boundaryRectangle.getHeight()
                            - context.draggable.getOffsetHeight(), 0,
                    desiredTop);
        }
        return desiredTop;
    }

    /**
     * @param x
     *            offset left relative to document body
     * @param y
     *            offset top relative to document body
     * @return a drop controller for the intersecting drop target or
     *         <code>null</code> if none are applicable
     */
    private DropController getDropControllerForLocation(int x, int y) {
        // our rectangles/areas have absolute offsets so we are good
        // since we already calculated the visible ones, we don't need ordering

        for (Area area : visibleDropAreas) {
            if (area.getRectangle().contains(x, y)) {
                return area.getDropController();
            }
        }

        return boundaryDropController;
    }

    private List<Area> getDropTargetAreas() {
        List<Area> areas = new ArrayList<Area>();
        for (ResourceSetAvatarDropController dropController : getAvailableDropControllers()) {
            Widget dropTarget = dropController.getDropTarget();
            Rectangle rectangle = Rectangle.fromWidget(dropTarget);
            WindowPanel window = getWindow(dropTarget);

            areas.add(new Area(rectangle, window, dropController));
        }
        return areas;
    }

    private WindowPanel getWindow(Widget originalWidget) {
        assert originalWidget != null;

        Widget widget = originalWidget;
        while (widget != null) {
            if (widget instanceof WindowPanel) {
                return (WindowPanel) widget;
            }
            widget = widget.getParent();
        }

        throw new RuntimeException("no window found for widget "
                + originalWidget);
    }

    private List<Area> getWindowAreas() {
        List<Area> windowAreas = new ArrayList<Area>();
        List<WindowPanel> windows = desktop.getWindows();
        for (WindowPanel window : windows) {
            Rectangle r = Rectangle.fromWidget(window);
            windowAreas.add(new Area(r, window, null));
        }
        return windowAreas;
    }

    // TODO might need a more generic implementation at some point
    private boolean hasRoundedCorners(Widget dropTarget) {
        return dropTarget instanceof ResourceSetAvatar;
    }

    /**
     * Creates the visual representation of the object that is being dragged.
     * This representation is used to indicate what is being dragged to the user
     * during the drag operation.
     */
    private void initDragProxy() {
        WidgetLocation currentDraggableLocation = new WidgetLocation(
                context.draggable, context.boundaryPanel);

        dragProxy = getAvatar(context).createProxy();
        dragProxy.setHover(true);

        context.boundaryPanel.add(dragProxy,
                currentDraggableLocation.getLeft(),
                currentDraggableLocation.getTop());
        checkGWTIssue1813(dragProxy, context.boundaryPanel);
        dragProxy.addStyleName(DragClientBundle.INSTANCE.css().movablePanel());
    }

    /**
     * Inserts a shade element behind resource set avatars that are available
     * drop targets, because they have rounded corners and the area outside of
     * those corners needs to be shaded.
     */
    private void insertShadeBehindRoundedDropTargets() {
        for (ResourceSetAvatarDropController dropController : getAvailableDropControllers()) {
            Widget dropTarget = dropController.getDropTarget();
            if (hasRoundedCorners(dropTarget)) {
                Element shade = createShadeElement(dropTarget);

                ((Element) dropTarget.getElement().getParentNode())
                        .appendChild(shade);
                shadeElements.add(shade);
            }
        }
    }

    private void moveDragProxy() {
        Style style = dragProxy.getElement().getStyle();

        style.setPropertyPx(CSS.LEFT, getDesiredLeft());
        style.setPropertyPx(CSS.TOP, getDesiredTop());
    }

    private void notifyDropControllerOnDragEnd() {
        assert (context.finalDropController == null) != (context.vetoException == null);
        if (context.vetoException == null) {
            context.dropController.onDrop(context);
            context.dropController.onLeave(context);
            context.dropController = null;
        }
    }

    // copied from PickupDragController
    @Override
    public void previewDragEnd() throws VetoDragException {
        assert context.finalDropController == null;
        assert context.vetoException == null;
        try {
            try {
                // may throw VetoDragException
                context.dropController.onPreviewDrop(context);
                context.finalDropController = context.dropController;
            } finally {
                // may throw VetoDragException
                super.previewDragEnd();
            }
        } catch (VetoDragException ex) {
            context.finalDropController = null;
            throw ex;
        }
    }

    @Override
    public void registerDropController(
            ResourceSetAvatarDropController dropController) {
        dropControllers.put(dropController.getDropTarget(), dropController);
    }

    private void removeDragProxy() {
        dragProxy.removeFromParent();
        dragProxy = null;
    }

    private void removeShade() {
        for (Element shadeElement : shadeElements) {
            shadeElement.removeFromParent();
        }
        shadeElements.clear();
        shadeRemoveHandle.remove();
    }

    @Override
    public void setDraggable(Widget widget, boolean draggable) {
        if (draggable) {
            makeDraggable(widget);
        } else {
            makeNotDraggable(widget);
        }
    }

    private void setResourceSetHighlighted(boolean highlighted) {
        hoverModel.setHighlightedResourceSet(highlighted ? getAvatar(context)
                .getResourceSet() : null);
    }

    private void setTemporaryDropTargetsVisible(boolean visible) {
        for (Widget w : temporaryDropTargets) {
            w.setVisible(visible);
        }
    }

    @Override
    public void unregisterDropController(
            ResourceSetAvatarDropController dropController) {

        assert dropController != null;

        dropControllers.remove(dropController.getDropTarget());
    }

    @Override
    public void unregisterDropControllerFor(Widget dropTarget) {
        unregisterDropController(dropControllers.get(dropTarget));
    }

    private void updateCacheAndBoundary() {
        // may have changed due to scrollIntoView(), developer driven changes
        // or manual user scrolling
        if (System.currentTimeMillis() - lastResetCacheTimeMillis >= CACHE_TIME_MILLIS) {
            updateLastCacheResetTime();
            resetCache();
            calculateBoundaryOffset();
        }
    }

    private void updateDropController() {
        try {
            DropController newDropController = getDropControllerForLocation(
                    context.mouseX, context.mouseY);

            if (context.dropController != newDropController) {
                if (context.dropController != null) {
                    context.dropController.onLeave(context);
                }
                context.dropController = newDropController;
                if (context.dropController != null) {
                    context.dropController.onEnter(context);
                }
            }

            if (context.dropController != null) {
                context.dropController.onMove(context);
            }
        } catch (Exception ex) {
            // abort drop operation and show dialog if exception occurs
            // TODO abort drop operation

            errorHandler.handleError(ex);
        }
    }

    private void updateLastCacheResetTime() {
        lastResetCacheTimeMillis = System.currentTimeMillis();
    }

}