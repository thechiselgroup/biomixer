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
package org.thechiselgroup.biomixer.client.dnd.windows;

import java.util.ArrayList;
import java.util.List;

import org.adamtacy.client.ui.NEffectPanel;
import org.adamtacy.client.ui.effects.NEffect;
import org.adamtacy.client.ui.effects.events.EffectCompletedEvent;
import org.adamtacy.client.ui.effects.events.EffectCompletedHandler;
import org.adamtacy.client.ui.effects.impl.Move;
import org.thechiselgroup.biomixer.client.dnd.DragProxyAttachedEvent;
import org.thechiselgroup.biomixer.client.dnd.DragProxyAttachedEventHandler;
import org.thechiselgroup.choosel.core.client.fx.FXUtil;
import org.thechiselgroup.choosel.core.client.fx.Opacity;
import org.thechiselgroup.choosel.core.client.geometry.Point;
import org.thechiselgroup.choosel.core.client.ui.CSS;
import org.thechiselgroup.choosel.core.client.ui.ResizingTextBox;
import org.thechiselgroup.choosel.core.client.ui.popup.PopupManager;
import org.thechiselgroup.choosel.core.client.ui.popup.PopupManagerFactory;
import org.thechiselgroup.choosel.core.client.util.math.MathUtils;

import com.allen_sauer.gwt.dnd.client.util.WidgetLocation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WindowPanel extends NEffectPanel implements WindowController {

    private static final int INNER_CSS_BORDER_WIDTH = 2;

    private static final int OUTER_CSS_BORDER_WIDTH = 2;

    private static final int BORDER_THICKNESS = 7;

    private static final String CSS_WINDOW = "choosel-WindowPanel";

    private static final String CSS_WINDOW_BUTTON_PANEL = "choosel-WindowPanel-ButtonPanel";

    private static final String CSS_WINDOW_HEADER = "choosel-WindowPanel-Header";

    private static final String CSS_WINDOW_RESIZE_EDGE_LEFT_BORDER = "choosel-WindowPanel-ResizeEdge-LeftBorder";

    private static final String CSS_WINDOW_RESIZE_EDGE_RIGHT_BORDER = "choosel-WindowPanel-ResizeEdge-RightBorder";

    private static final String CSS_WINDOW_RESIZE_EDGE_TOP_BORDER = "choosel-WindowPanel-ResizeEdge-TopBorder";

    private static final String CSS_WINDOW_HEADER_LABEL = "choosel-WindowPanel-HeaderLabel";

    private static final String CSS_WINDOW_HEADER_TEXT = "choosel-WindowPanel-HeaderText";

    private static final String CSS_WINDOW_RESIZE = "choosel-WindowPanel-Resize-";

    private static final String CSS_WINDOW_RESIZE_EDGE = "choosel-WindowPanel-ResizeEdge";

    private static final String IMAGE_CLOSE_ACTIVE = "images/close_active.gif";

    private static final String IMAGE_CLOSE_INVISIBLE = "images/close_invisible.gif";

    private static final String IMAGE_CLOSE_VISIBLE = "images/close_visible.gif";

    private static final int TOTAL_BORDER_THICKNESS = 2 * BORDER_THICKNESS
            + OUTER_CSS_BORDER_WIDTH + INNER_CSS_BORDER_WIDTH;

    private Image closeImage;

    private Widget contentWidget;

    private Widget eastTopWidget;

    private Widget eastWidget;

    private FlexTable grid;

    private Widget headerWidget;

    private Widget northWidget;

    private final List<Widget> removeFromDragControllerOnDispose = new ArrayList<Widget>();

    protected FocusPanel rootPanel;

    private Widget southWidget;

    // TODO move into a better place as this is relevant for persistency only
    private WindowContent windowContent;

    private Widget westTopWidget;

    private Widget westWidget;

    private WindowManager manager;

    private String windowTitle;

    private WindowController controller;

    /**
     * Panel that contains the title of the window and window buttons such as
     * the close button.
     */
    private HorizontalPanel headerBar;

    private PopupManagerFactory popupManagerFactory;

    @Inject
    public WindowPanel(PopupManagerFactory popupManagerFactory) {
        this.popupManagerFactory = popupManagerFactory;
    }

    /**
     * Adjusts the size of the window or the size of its content. If the window
     * is larger than its content, then the content size is increased. If the
     * window is smaller, its size is increased. It also checks for maximum
     * sizes because of desktop limitations.
     */
    public void adjustSize() {
        /*
         * Calculate target width: max of header, adjusted window, content
         * 
         * TODO: restrict to available desktop space
         */
        int targetWidth = TOTAL_BORDER_THICKNESS
                + MathUtils.maxInt(headerBar.getOffsetWidth(),
                        contentWidget.getOffsetWidth(), getWidth()
                                - TOTAL_BORDER_THICKNESS);
        /*
         * Calculate target height: max of adjusted window, content
         * 
         * TODO: restrict to available desktop space
         */
        int targetHeight = TOTAL_BORDER_THICKNESS
                + MathUtils.maxInt(contentWidget.getOffsetHeight(), getHeight()
                        - TOTAL_BORDER_THICKNESS - headerBar.getOffsetHeight());

        setPixelSize(targetWidth, targetHeight);
    }

    /**
     * Fades out the window and removes it from the window controller
     * afterwards.
     */
    // TODO rename to animate hide, add effect completed handler in method
    public void close() {
        NEffect fade = createHideEffect();

        fade.addEffectCompletedHandler(new EffectCompletedHandler() {
            @Override
            public void onEffectCompleted(EffectCompletedEvent event) {
                removeEffects();
                manager.close(WindowPanel.this);
            }
        });

        addEffect(fade);
        playEffects();
    }

    // hook
    protected ClickHandler createCloseButtonClickHandler() {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                close();
            }
        };
    }

    // hook
    protected NEffect createHideEffect() {
        return FXUtil.createOpacityMorphEffect(Opacity.OPAQUE,
                Opacity.TRANSPARENT);
    }

    protected NEffect createShowEffect() {
        return FXUtil.createOpacityMorphEffect(Opacity.TRANSPARENT,
                Opacity.OPAQUE);
    }

    // TODO use window manager
    public int getAbsoluteX() {
        return getAbsoluteLeft() - getParent().getAbsoluteLeft();
    }

    // TODO use window manager
    public int getAbsoluteY() {
        return getAbsoluteTop() - getParent().getAbsoluteTop();
    }

    private String getActiveCloseImageUrl() {
        return getModuleBase() + IMAGE_CLOSE_ACTIVE;
    }

    /*
     * hook method
     */
    protected String getClosePopupLabel() {
        return "Close";
    }

    @Override
    public int getHeight() {
        return grid.getOffsetHeight() + OUTER_CSS_BORDER_WIDTH;
    }

    private String getInvisibleCloseImageUrl() {
        return getModuleBase() + IMAGE_CLOSE_INVISIBLE;
    }

    private Point getLocation() {
        return manager.getLocation(this);
    }

    protected String getModuleBase() {
        return GWT.getModuleBaseURL();
    }

    // TODO move into model
    public WindowContent getViewContent() {
        return this.windowContent;
    }

    private String getVisibleCloseImageUrl() {
        return getModuleBase() + IMAGE_CLOSE_VISIBLE;
    }

    @Override
    public int getWidth() {
        return grid.getOffsetWidth() + OUTER_CSS_BORDER_WIDTH;
    }

    public String getWindowTitle() {
        return windowTitle;
    }

    public int getZIndex() {
        return DOM.getIntStyleAttribute(getElement(), CSS.Z_INDEX);
    }

    public void init(WindowManager windowManager, String title,
            boolean titleEditable, Widget contentWidget) {

        this.controller = new DefaultWindowController(new WindowCallback() {
            @Override
            public int getHeight() {
                return WindowPanel.this.getHeight();
            }

            @Override
            public Point getLocation() {
                return WindowPanel.this.getLocation();
            }

            @Override
            public int getWidth() {
                return WindowPanel.this.getWidth();
            }

            @Override
            public void setLocation(int x, int y) {
                WindowPanel.this.setLocation(x, y);
            }

            @Override
            public void setPixelSize(int width, int height) {
                WindowPanel.this.setPixelSize(width, height);
            }
        });

        initShowEvent();

        DOM.setStyleAttribute(getElement(), "border", "0px"); // TODO move to
                                                              // CSS class or
                                                              // CSS file

        this.windowTitle = title;
        this.rootPanel = new FocusPanel();
        setWidget(this.rootPanel);

        this.manager = windowManager;

        rootPanel.addStyleName(CSS_WINDOW);

        this.contentWidget = contentWidget;
        initHeader(windowManager, titleEditable, title);

        rootPanel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // XXX fix bug: window not to front
                // deactivated because of list box issue
                // // force our panel to the top of our z-index context
                // AbsolutePanel boundaryPanel = windowController
                // .getBoundaryPanel();
                // WidgetLocation location = new
                // WidgetLocation(WindowPanel.this,
                // boundaryPanel);
                // boundaryPanel.add(WindowPanel.this, location.getLeft(),
                // location.getTop());
            }
        });

        initGrid(contentWidget);
    }

    private Widget initCell(int row, int col, ResizeDirection direction,
            String additionalCSSClass) {

        FocusPanel borderWidget = new FocusPanel();

        /*
         * Sets the size for the elements that are not resized (e.g. corners and
         * corner extensions). The corner extensions have the same size as the
         * corners.
         */
        borderWidget.setPixelSize(BORDER_THICKNESS, BORDER_THICKNESS);

        grid.setWidget(row, col, borderWidget);

        manager.getResizeDragController()
                .makeDraggable(borderWidget, direction);
        removeFromDragControllerOnDispose.add(borderWidget);

        /*
         * all CSS classes need to be set in one call due to limitations in
         * getCellFormatter().addStyleName
         */
        String css = CSS_WINDOW_RESIZE_EDGE;
        css += " " + CSS_WINDOW_RESIZE + direction.getDirectionLetters();
        if (additionalCSSClass != null) {
            css += " " + additionalCSSClass;
        }
        grid.getCellFormatter().addStyleName(row, col, css);

        return borderWidget;
    }

    private void initCloseImage() {
        closeImage = new Image(getInvisibleCloseImageUrl());
        closeImage.addStyleName(CSS_WINDOW_BUTTON_PANEL);

        closeImage.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                closeImage.setUrl(getActiveCloseImageUrl());
            }
        });

        closeImage.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                closeImage.setUrl(getVisibleCloseImageUrl());
            }
        });

        rootPanel.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                closeImage.setUrl(getVisibleCloseImageUrl());
            }
        });

        rootPanel.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                closeImage.setUrl(getInvisibleCloseImageUrl());
            }
        });

        // disable dragging / transparency on mouse down over image
        closeImage.addMouseDownHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                event.stopPropagation();
                event.preventDefault();
            }
        });

        closeImage.addClickHandler(createCloseButtonClickHandler());

        // hide close image when dnd operation starts
        addHandler(new DragProxyAttachedEventHandler() {
            @Override
            public void onDragProxyAttached(DragProxyAttachedEvent event) {
                closeImage.setUrl(getInvisibleCloseImageUrl());
            }
        }, DragProxyAttachedEvent.TYPE);

        PopupManager popupManager = popupManagerFactory
                .createPopupManager(new Label(getClosePopupLabel()));
        popupManager.linkToWidget(closeImage);
        popupManager.setHideDelay(0);

        headerBar.add(closeImage);
        headerBar.setCellHorizontalAlignment(closeImage,
                HasAlignment.ALIGN_RIGHT);
        headerBar.setCellVerticalAlignment(closeImage, HasAlignment.ALIGN_TOP);
    }

    private void initGrid(Widget contentWidget) {
        grid = new FlexTable();
        grid.setBorderWidth(0);
        grid.setCellSpacing(0);
        grid.setCellPadding(0);
        rootPanel.add(grid);

        initCell(0, 0, ResizeDirection.NORTH_WEST, null);
        initCell(0, 1, ResizeDirection.NORTH_WEST, null);
        northWidget = initCell(0, 2, ResizeDirection.NORTH, null);
        initCell(0, 3, ResizeDirection.NORTH_EAST, null);
        initCell(0, 4, ResizeDirection.NORTH_EAST, null);

        initCell(1, 0, ResizeDirection.NORTH_WEST, null);
        grid.setWidget(1, 1, headerBar);
        grid.getCellFormatter().addStyleName(1, 1, CSS_WINDOW_HEADER);
        grid.getFlexCellFormatter().setColSpan(1, 1, 3);
        grid.getFlexCellFormatter().setRowSpan(1, 1, 2);
        initCell(1, 2, ResizeDirection.NORTH_EAST, null);

        westTopWidget = initCell(2, 0, ResizeDirection.WEST, null);
        eastTopWidget = initCell(2, 1, ResizeDirection.EAST, null);

        westWidget = initCell(3, 0, ResizeDirection.WEST,
                CSS_WINDOW_RESIZE_EDGE_RIGHT_BORDER);
        grid.setWidget(3, 1, contentWidget);
        grid.getFlexCellFormatter().setColSpan(3, 1, 3);
        grid.getFlexCellFormatter().setRowSpan(3, 1, 2);
        eastWidget = initCell(3, 2, ResizeDirection.EAST,
                CSS_WINDOW_RESIZE_EDGE_LEFT_BORDER);

        initCell(4, 0, ResizeDirection.SOUTH_WEST,
                CSS_WINDOW_RESIZE_EDGE_RIGHT_BORDER);
        initCell(4, 1, ResizeDirection.SOUTH_EAST,
                CSS_WINDOW_RESIZE_EDGE_LEFT_BORDER);

        initCell(5, 0, ResizeDirection.SOUTH_WEST, null);
        initCell(5, 1, ResizeDirection.SOUTH_WEST,
                CSS_WINDOW_RESIZE_EDGE_TOP_BORDER);
        southWidget = initCell(5, 2, ResizeDirection.SOUTH,
                CSS_WINDOW_RESIZE_EDGE_TOP_BORDER);
        initCell(5, 3, ResizeDirection.SOUTH_EAST,
                CSS_WINDOW_RESIZE_EDGE_TOP_BORDER);
        initCell(5, 4, ResizeDirection.SOUTH_EAST, null);
    }

    private void initHeader(WindowManager windowManager, boolean editableTitle,
            String title) {

        headerBar = new HorizontalPanel();
        headerBar.setSize("100%", "");

        initTitleWidget(title, editableTitle, windowManager);
        initMoveLabel(windowManager);
        initCloseImage();
    }

    private void initMoveLabel(WindowManager windowManager) {
        Label moveLabel = new Label(" ");
        moveLabel.getElement().getStyle().setWidth(100d, Unit.PCT);
        moveLabel.getElement().getStyle().setHeight(20, Unit.PX);
        windowManager.getMoveDragController().makeDraggable(this, moveLabel);
        headerBar.add(moveLabel);
        headerBar.setCellWidth(moveLabel, "100%");
    }

    private void initShowEvent() {
        NEffect showEffect = createShowEffect();
        showEffect.addEffectCompletedHandler(new EffectCompletedHandler() {
            @Override
            public void onEffectCompleted(EffectCompletedEvent event) {
                removeEffects();

                // TODO extract constant
                // DOM.setStyleAttribute(rootPanel.getElement(), "opacity",
                // null);
            }
        });
        addEffect(showEffect);
    }

    private void initTitleWidget(String title, boolean editableTitle,
            WindowManager windowManager) {

        if (editableTitle) {
            headerWidget = new ResizingTextBox(20, 250);
            TextBox headerText = (TextBox) this.headerWidget;
            headerText.setText(title);
            headerText.addStyleName(CSS_WINDOW_HEADER_TEXT);
            headerText.addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    /*
                     * XXX The title is kept at 2 locations right now.
                     */
                    windowContent.setLabel(event.getValue());
                    windowTitle = event.getValue();
                }
            });
        } else {
            headerWidget = new Label(title);
            headerWidget.addStyleName(CSS_WINDOW_HEADER_LABEL);
            windowManager.getMoveDragController().makeDraggable(this,
                    headerWidget);
        }

        headerBar.add(headerWidget);
        headerBar.setCellHorizontalAlignment(headerWidget,
                HasAlignment.ALIGN_LEFT);
    }

    @Override
    protected void onDetach() {
        manager.getMoveDragController().makeNotDraggable(this);

        for (Widget w : removeFromDragControllerOnDispose) {
            manager.getResizeDragController().makeNotDraggable(w);
        }

        // DragController#unregisterDropController

        super.onDetach();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        adjustSize();
        playEffects();
    }

    @Override
    public void resize(int deltaX, int deltaY, int targetWidth, int targetHeight) {
        controller.resize(deltaX, deltaY, targetWidth, targetHeight);
    }

    /**
     * Sets the location of this window on the pane defined by the
     * WindowManager. This version of <code>setLocation</code> is not animated.
     * 
     * @param x
     *            x coordinate in the coordinate space defined by the
     *            WindowManager
     * @param y
     *            y coordinate in the coordinate space defined by the
     *            WindowManager
     */
    public void setLocation(int x, int y) {
        manager.setLocation(this, x, y);
    }

    public void setLocation(final int x, final int y, boolean animate) {
        /*
         * Warning: This method is fairly fragile. Proceed with caution.
         */

        if (!animate) {
            setLocation(x, y);
            return;
        }

        assert animate;

        Point location = getLocation();

        Move move = new Move(x - location.getX(), y - location.getY()) {
            @Override
            public void tearDownEffect() {
                /*
                 * do not super.tearDownEffects as this resets to original state
                 * reset root panel position as this is affected by move
                 */
                CSS.setLocation(rootPanel, 0, 0);
            }
        };

        move.addEffectCompletedHandler(new EffectCompletedHandler() {
            @Override
            public void onEffectCompleted(EffectCompletedEvent event) {
                removeEffects();
                setLocation(x, y);
                assert 0 == new WidgetLocation(rootPanel, WindowPanel.this)
                        .getLeft();
                assert 0 == new WidgetLocation(rootPanel, WindowPanel.this)
                        .getTop();
            }
        });

        move.setTransitionType(FXUtil.EASE_OUT);
        move.setDuration(FXUtil.DEFAULT_EFFECT_DURATION);

        addEffect(move);
        playEffects();
    }

    // TODO test
    @Override
    public void setPixelSize(int width, int height) {
        /*
         * setPixelSize calculates the expected size of the content widget and
         * sets it to this size. It then requests the real size of the content
         * widget, because we have no better way to find out about the minimum
         * size. Afterwards, we change the border width and finally adjust the
         * window size.
         * 
         * This method breaks easily, so proceed with caution here.
         */
        assert width >= 0;
        assert height >= 0;

        if (width == getWidth() && height == getHeight()) {
            return;
        }

        int headerHeight = headerBar.getOffsetHeight();

        /*
         * Restrict the width/height to be at least the minimum determined by
         * header & borders.
         */
        width = Math.max(width, 4 * TOTAL_BORDER_THICKNESS);
        height = Math.max(height, 4 * TOTAL_BORDER_THICKNESS + headerHeight);

        int contentWidth = width - TOTAL_BORDER_THICKNESS;
        int contentHeight = height - TOTAL_BORDER_THICKNESS - headerHeight;

        contentWidget.setPixelSize(contentWidth, contentHeight);

        int realContentWidth = contentWidget.getOffsetWidth();
        int realContentHeight = contentWidget.getOffsetHeight();

        headerBar.setPixelSize(realContentWidth, headerHeight);

        /*
         * adjust for the case where headerWidth > contentContentWidth
         * (otherwise content does not fill the available space)
         */
        int headerWidth = headerBar.getOffsetWidth();
        if (headerWidth > realContentWidth) {
            realContentWidth = headerWidth;
        }

        /*
         * If the real size is different from the set content size, we set it
         * again. This enables the content widget to set the right sizes in
         * affected child widgets.
         */
        if (contentHeight != realContentHeight
                || contentWidth != realContentWidth) {
            contentWidget.setPixelSize(realContentWidth, realContentHeight);
        }

        updateBorderWidths(realContentWidth, realContentHeight, headerHeight);

        int windowWidth = realContentWidth + TOTAL_BORDER_THICKNESS;
        int windowHeight = realContentHeight + TOTAL_BORDER_THICKNESS
                + headerHeight;

        super.setPixelSize(windowWidth, windowHeight);
    }

    public void setViewContent(WindowContent viewContent) {
        this.windowContent = viewContent;
    }

    public void setZIndex(final int zIndex) {
        // Bugfix: need to set zIndex manually because of timeline/firefox issue
        CSS.setZIndex(this, zIndex);
    }

    private void updateBorderWidths(int contentWidth, int contentHeight,
            int headerHeight) {

        northWidget.setPixelSize(contentWidth - 2 * BORDER_THICKNESS,
                BORDER_THICKNESS);
        southWidget.setPixelSize(contentWidth - 2 * BORDER_THICKNESS,
                BORDER_THICKNESS);

        westTopWidget.setPixelSize(BORDER_THICKNESS, headerHeight
                - BORDER_THICKNESS);
        eastTopWidget.setPixelSize(BORDER_THICKNESS, headerHeight
                - BORDER_THICKNESS);

        westWidget.setPixelSize(BORDER_THICKNESS, contentHeight
                - BORDER_THICKNESS);
        eastWidget.setPixelSize(BORDER_THICKNESS, contentHeight
                - BORDER_THICKNESS);
    }
}
