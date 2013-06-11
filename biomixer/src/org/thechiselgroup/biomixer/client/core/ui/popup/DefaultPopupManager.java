/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.ui.popup;

import org.thechiselgroup.biomixer.client.core.fx.Opacity;
import org.thechiselgroup.biomixer.client.core.geometry.Point;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class DefaultPopupManager implements PopupManager {

    private class MouseHandlersPopupManagerLink implements MouseOverHandler,
            MouseOutHandler, MouseMoveHandler, MouseDownHandler,
            AttachEvent.Handler {

        @Override
        public void onAttachOrDetach(AttachEvent event) {
            assert event != null;

            boolean detached = !event.isAttached();
            if (detached) {
                DefaultPopupManager.this.onDetach();
            }
        }

        @Override
        public void onMouseDown(MouseDownEvent event) {
            DefaultPopupManager.this.onMouseDown(event.getNativeEvent());
        }

        @Override
        public void onMouseMove(MouseMoveEvent event) {
            DefaultPopupManager.this.onMouseMove(event.getClientX(),
                    event.getClientY());
        }

        @Override
        public void onMouseOut(MouseOutEvent event) {
            DefaultPopupManager.this.onMouseOut(event.getClientX(),
                    event.getClientY());
        }

        @Override
        public void onMouseOver(MouseOverEvent event) {
            DefaultPopupManager.this.onMouseOver(event.getClientX(),
                    event.getClientY());
        }

    }

    // separate from raw events --> onPopupGainsAttentation,
    // onPopupLosesAttention, onSourceGainsAttention, onSourceLosesAttention,
    // onActivatePopup, onHidePopup, onTimeout
    protected static class State {

        public void enter(DefaultPopupManager manager) {
        }

        public void leave(DefaultPopupManager manager) {
        }

        public void onPopupMouseOut(DefaultPopupManager manager) {
        }

        public void onPopupMouseOver(DefaultPopupManager manager) {
        }

        public void onSourceMouseMove(DefaultPopupManager manager) {
        }

        public void onSourceMouseOut(DefaultPopupManager manager) {
        }

        public void onSourceMouseOver(DefaultPopupManager manager) {
        }

        // TODO this could be special activation??
        public void onSourceRightClick(DefaultPopupManager manager) {
        }

        public void onTimeout(DefaultPopupManager manager) {
        }

    }

    public final static State ACTIVE_STATE = new State() {

        @Override
        public void enter(DefaultPopupManager manager) {
            manager.setOpacity(Opacity.OPAQUE);
        }

        @Override
        public void onPopupMouseOut(DefaultPopupManager manager) {
            if (manager.isPopupHidingDelayed()) {
                manager.setState(SEMITRANSPARENT_WAITING_STATE);
            } else {
                manager.setState(INACTIVE_STATE);
            }
        }

    };

    public static final int DEFAULT_HIDE_DELAY = 250;

    /**
     * Default delay until popup is shown automatically in semi-transparent
     * state when mouse cursor is over trigger.
     */
    public static final int DEFAULT_SHOW_DELAY = 500;

    public final static State DISABLED_STATE = new State() {

        @Override
        public void enter(DefaultPopupManager manager) {
            manager.hide();
        }

    };

    public final static State INACTIVE_STATE = new State() {

        @Override
        public void enter(DefaultPopupManager manager) {
            manager.hide();
        }

        @Override
        public void onSourceMouseOver(DefaultPopupManager manager) {
            if (manager.isPopupDisplayDelayed()) {
                manager.setState(WAITING_STATE);
            } else {
                manager.setState(SEMITRANSPARENT_STATE);
            }
        }

    };

    private static final int POPUP_OFFSET_X = 20;

    private static final int POPUP_OFFSET_Y = 15;

    public final static State SEMITRANSPARENT_STATE = new State() {

        @Override
        public void enter(DefaultPopupManager manager) {
            manager.setOpacity(Opacity.SEMI_TRANSPARENT);
        }

        @Override
        public void onPopupMouseOver(DefaultPopupManager manager) {
            manager.setState(ACTIVE_STATE);
        }

        @Override
        public void onSourceMouseOut(DefaultPopupManager manager) {
            if (manager.isPopupHidingDelayed()) {
                manager.setState(SEMITRANSPARENT_WAITING_STATE);
            } else {
                manager.setState(INACTIVE_STATE);
            }
        }

    };

    public final static State SEMITRANSPARENT_WAITING_STATE = new State() {

        @Override
        public void enter(DefaultPopupManager manager) {
            manager.setOpacity(Opacity.SEMI_TRANSPARENT);
            manager.startTimer(manager.hideDelay);
        }

        @Override
        public void leave(DefaultPopupManager manager) {
            manager.cancelTimer();
        }

        @Override
        public void onPopupMouseOver(DefaultPopupManager manager) {
            manager.setState(ACTIVE_STATE);
        }

        @Override
        public void onSourceMouseOver(DefaultPopupManager manager) {
            manager.setState(SEMITRANSPARENT_STATE);
        }

        @Override
        public void onTimeout(DefaultPopupManager manager) {
            manager.setState(INACTIVE_STATE);
        }

    };

    public final static State WAITING_STATE = new State() {

        @Override
        public void enter(DefaultPopupManager manager) {
            manager.startTimer(manager.showDelay);
        }

        @Override
        public void leave(DefaultPopupManager manager) {
            manager.cancelTimer();
        }

        @Override
        public void onSourceMouseMove(DefaultPopupManager manager) {
            manager.startTimer(manager.showDelay);
        }

        @Override
        public void onSourceMouseOut(DefaultPopupManager manager) {
            manager.setState(INACTIVE_STATE);
        }

        @Override
        public void onSourceRightClick(DefaultPopupManager manager) {
            manager.setState(SEMITRANSPARENT_STATE);
        }

        @Override
        public void onTimeout(DefaultPopupManager manager) {
            manager.setState(SEMITRANSPARENT_STATE);
        }
    };

    protected int hideDelay = DEFAULT_HIDE_DELAY;

    private int clientX = -1;

    private int clientY = -1;

    protected int showDelay = DEFAULT_SHOW_DELAY;

    protected State state = INACTIVE_STATE;

    protected Popup popup;

    private Timer timer;

    public DefaultPopupManager(Popup popup) {
        assert popup != null;
        this.popup = popup;

        this.popup.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                updateMousePosition(event.getClientX(), event.getClientY());
                state.onPopupMouseOut(DefaultPopupManager.this);
            }
        }, MouseOutEvent.getType());
        this.popup.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                updateMousePosition(event.getClientX(), event.getClientY());
                state.onPopupMouseOver(DefaultPopupManager.this);
            }
        }, MouseOverEvent.getType());

        this.timer = createTimer();
    }

    protected void cancelTimer() {
        timer.cancel();
    }

    protected Timer createTimer() {
        return new Timer() {
            @Override
            public void run() {
                state.onTimeout(DefaultPopupManager.this);
            }
        };
    }

    @Override
    public int getHideDelay() {
        return hideDelay;
    }

    @Override
    public Popup getPopup() {
        return popup;
    }

    /**
     * @return Delay between showing mouse movement stops and popups gets shown
     *         in ms.
     */
    @Override
    public int getShowDelay() {
        return showDelay;
    }

    protected void hide() {
        setOpacity(Opacity.TRANSPARENT);
    }

    @Override
    public void hidePopup() {
        if (isEnabled()) {
            setState(INACTIVE_STATE);
        }
    }

    @Override
    public boolean isEnabled() {
        return state != DISABLED_STATE;
    }

    private boolean isPopupDisplayDelayed() {
        return showDelay > 0;
    }

    private boolean isPopupHidingDelayed() {
        return hideDelay > 0;
    }

    /**
     * Links a widget source to this {@link PopupManager}.
     */
    @Override
    public <T extends Widget & HasAllMouseHandlers & HasAttachHandlers> HandlerRegistration linkToWidget(
            T widget) {

        MouseHandlersPopupManagerLink link = new MouseHandlersPopupManagerLink();

        final HandlerRegistration reg1 = widget.addMouseOverHandler(link);
        final HandlerRegistration reg2 = widget.addMouseOutHandler(link);
        final HandlerRegistration reg3 = widget.addMouseMoveHandler(link);
        final HandlerRegistration reg4 = widget.addMouseDownHandler(link);
        final HandlerRegistration reg5 = widget.addAttachHandler(link);

        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                reg1.removeHandler();
                reg2.removeHandler();
                reg3.removeHandler();
                reg4.removeHandler();
                reg5.removeHandler();
            }
        };
    }

    public void onDetach() {
        state.onSourceMouseOut(this);
    }

    // TODO we need to separate the popup display from the
    // extended state machine, because there can be different
    // popup menu models (e.g. triggered on mouse down)
    @Override
    public void onMouseDown(NativeEvent event) {
        assert event != null;

        updateMousePosition(event.getClientX(), event.getClientY());

        if (!isEnabled()) {
            return;
        }

        if (event.getButton() == NativeEvent.BUTTON_RIGHT) {
            event.stopPropagation();
            event.preventDefault();

            state.onSourceRightClick(this);
        } else if (event.getButton() == NativeEvent.BUTTON_LEFT) {
            /*
             * NOTE: Mouse down triggers click operations. The popup should not
             * interfere with this, so if we are already in the waiting state,
             * the timer is reset.
             */
            if (state == WAITING_STATE) {
                cancelTimer();
                startTimer(showDelay);
            }
        }
    }

    @Override
    public void onMouseMove(int clientX, int clientY) {
        /*
         * Some browsers (e.g. Safari 5.0.3, Chome 10.0.648.127 beta) seem to
         * fire mouse move events continously under certain circumstance even
         * though the mouse was not moved. Null check is to prevent errors in
         * IE, but the arc popups don't work...oh, I moved the null check to the
         * caller. Nonetheless, it should never get this far in IE...
         */
        if ((clientX == this.clientX) && (clientY == this.clientY)) {
            return;
        }

        updateMousePosition(clientX, clientY);
        state.onSourceMouseMove(this);
    }

    @Override
    public void onMouseOut(int clientX, int clientY) {
        updateMousePosition(clientX, clientY);
        state.onSourceMouseOut(DefaultPopupManager.this);
    }

    @Override
    public void onMouseOver(int clientX, int clientY) {
        updateMousePosition(clientX, clientY);
        state.onSourceMouseOver(DefaultPopupManager.this);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled == isEnabled()) {
            return;
        }

        if (enabled) {
            setState(INACTIVE_STATE);
        } else {
            setState(DISABLED_STATE);
        }
    }

    /**
     * @param delay
     *            Delay between showing mouse out of source/popup and popups
     *            gets hidden in ms.
     */
    @Override
    public void setHideDelay(int delay) {
        this.hideDelay = delay;
    }

    protected void setOpacity(int opacity) {
        if (popup.getOpacity() == Opacity.TRANSPARENT) {
            // Compute the position so it's centered on the parent element,
            // but not falling off the view edge.
            int x = clientX + POPUP_OFFSET_X;
            int y = clientY + POPUP_OFFSET_Y;

            int maxX = RootPanel.get().getOffsetWidth();
            int maxY = RootPanel.get().getOffsetHeight();
            int popupWidth = popup.getSize().getWidth();
            int popupHeight = popup.getSize().getHeight();
            // Only check in one direction, because we normally put the
            // top left corner on the parent.
            int deltaX = maxX - (x + popupWidth);
            int deltaY = maxY - (y + popupHeight);
            // Move towards direction with the most spare room, by height and/or
            // width
            // But don't stick the popup right over the parent. That'd be bad.
            if (deltaX < 0 && x > popupWidth) {
                x -= popupWidth;
            }
            if (deltaY < 0 && y > popupHeight) {
                y -= popupHeight;
            }

            popup.setLocation(new Point(x, y));
        }

        popup.setOpacity(opacity);
    }

    /**
     * @param delay
     *            Delay between showing mouse movement stops and popups gets
     *            shown in ms.
     */
    @Override
    public void setShowDelay(int showDelay) {
        this.showDelay = showDelay;
    }

    public void setState(State newState) {
        assert newState != null;

        this.state.leave(this);
        this.state = newState;
        this.state.enter(this);
    }

    protected void startTimer(int delayInMs) {
        timer.schedule(delayInMs);
    }

    private void updateMousePosition(int clientX, int clientY) {
        this.clientX = clientX;
        this.clientY = clientY;
    }

}
