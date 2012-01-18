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

import org.adamtacy.client.ui.NEffectPanel;
import org.adamtacy.client.ui.effects.core.NMorphScalar;
import org.adamtacy.client.ui.effects.events.EffectCompletedEvent;
import org.adamtacy.client.ui.effects.events.EffectCompletedHandler;
import org.thechiselgroup.biomixer.client.core.fx.FXUtil;
import org.thechiselgroup.biomixer.client.core.fx.Opacity;
import org.thechiselgroup.biomixer.client.core.geometry.DefaultSize;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.ui.CSS;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * {@link Popup} that binds the content {@link Widget} to a {@link Panel} and
 * sets the position to FIXED. The widget can be positioned absolutely on the
 * client area.
 * 
 * @author Lars Grammel
 */
// TODO make CSS class changeable
public class DefaultPopup implements Popup {

    private final static int HIDDEN_Z_INDEX = -1;

    private String cssClass = DEFAULT_POPUP_CSS_CLASS;

    private int opacity = Opacity.TRANSPARENT;

    private Point location = new Point(0, 0);

    private Panel rootPanel;

    private NEffectPanel effectPanel = null;

    /**
     * The container panel is used to set a CSS class. The CSS class cannot be
     * set on the effect panel for some reason. The container panel is also used
     * for event handlers.
     */
    private SimplePanel containerPanel;

    private Widget contentWidget;

    private NMorphScalar currentEffect;

    private NMorphScalar nextEffect;

    private int zIndex;

    public DefaultPopup(Panel rootPanel, int zIndex) {
        assert rootPanel != null;

        this.rootPanel = rootPanel;
        this.zIndex = zIndex;
    }

    @Override
    public <H extends EventHandler> HandlerRegistration addDomHandler(
            H handler, DomEvent.Type<H> type) {

        return getContainerPanel().addDomHandler(handler, type);
    };

    @Override
    public <H extends EventHandler> HandlerRegistration addHandler(H handler,
            GwtEvent.Type<H> type) {

        return getContainerPanel().addHandler(handler, type);
    };

    private void attachPopup() {
        NEffectPanel effectPanel = getEffectPanel();

        Style style = effectPanel.getElement().getStyle();

        /*
         * INFO: position is fixed, not absolute, because there are problems
         * with absolute positioning when the window is scrolled.
         */
        style.setProperty(CSS.POSITION, CSS.FIXED);
        style.setProperty(CSS.Z_INDEX, Integer.toString(zIndex));
        style.setPropertyPx(CSS.LEFT, location.getX());
        style.setPropertyPx(CSS.TOP, location.getY());

        rootPanel.add(effectPanel);
    }

    protected SimplePanel createContainerPanel() {
        return new SimplePanel();
    }

    private NMorphScalar createMorphEffect(int currentOpacity, int newOpacity,
            double duration) {

        NMorphScalar morphEffect = FXUtil.createOpacityMorphEffect(
                currentOpacity, newOpacity);

        morphEffect.addEffectCompletedHandler(new EffectCompletedHandler() {
            @Override
            public void onEffectCompleted(EffectCompletedEvent event) {
                effectCompleted();
            }
        });

        return morphEffect;
    }

    private void detachPopup() {
        if (!isEffectPanelInitialized() || !getEffectPanel().isAttached()) {
            return;
        }

        rootPanel.remove(getEffectPanel());
    }

    private void effectCompleted() {
        getEffectPanel().removeEffect(currentEffect);

        if (hasAnotherEffect()) {
            fireOpacityChanged();

            currentEffect = nextEffect;
            nextEffect = null;
            runCurrentEffect();
        } else {
            currentEffect = null;
            if (opacity == Opacity.TRANSPARENT) {
                detachPopup();
            }

            /*
             * NOTE: The opacity changed event is fired after detaching the
             * popup and not before, because otherwise the user might mouse over
             * the popup and trigger other events while a potential client might
             * believe the popup is closed. This led to problems with
             * highlighting and popups.
             */
            fireOpacityChanged();
        }
    }

    private void fireOpacityChanged() {
        getContainerPanel().fireEvent(new PopupOpacityChangedEvent(this));
    }

    private SimplePanel getContainerPanel() {
        if (containerPanel == null) {
            containerPanel = createContainerPanel();
            containerPanel.setStyleName(cssClass);
            containerPanel.setWidget(contentWidget);
        }

        return containerPanel;
    }

    @Override
    public Widget getContentWidget() {
        return contentWidget;
    }

    private int getEffectEndOpacity() {
        assert currentEffect != null;
        return (int) currentEffect.getEndValue();
    }

    private NEffectPanel getEffectPanel() {
        if (!isEffectPanelInitialized()) {
            SimplePanel containerPanel = getContainerPanel();

            effectPanel = new NEffectPanel();
            effectPanel.add(containerPanel);
        }

        return effectPanel;
    }

    @Override
    public Point getLocation() {
        return location;
    }

    @Override
    public int getOpacity() {
        return opacity;
    }

    @Override
    public DefaultSize getSize() {
        /*
         * If the popup panel is not attached --> attach in background, get
         * size, remove panel. Otherwise just report size.
         */
        NEffectPanel effectPanel = getEffectPanel();
        boolean attached = effectPanel.isAttached(); // do not inline

        if (!attached) {
            Style style = effectPanel.getElement().getStyle();
            style.setProperty(CSS.POSITION, CSS.FIXED);
            style.setProperty(CSS.Z_INDEX, Integer.toString(HIDDEN_Z_INDEX));
            rootPanel.add(effectPanel);
        }

        DefaultSize size = new DefaultSize(containerPanel.getOffsetWidth(),
                containerPanel.getOffsetHeight());

        if (!attached) {
            rootPanel.remove(effectPanel);
        }

        return size;
    }

    private boolean hasAnotherEffect() {
        return nextEffect != null;
    }

    @Override
    public void hide() {
        setOpacity(Opacity.TRANSPARENT, 0.0);
    }

    @Override
    public void hide(double durationInSeconds) {
        setOpacity(Opacity.TRANSPARENT, durationInSeconds);
    }

    private boolean isAttached() {
        return getEffectPanel().isAttached();
    }

    private boolean isEffectPanelInitialized() {
        return effectPanel != null;
    }

    private boolean isEffectRunning() {
        return currentEffect != null;
    }

    private void runCurrentEffect() {
        opacity = getEffectEndOpacity();

        NEffectPanel effectPanel = getEffectPanel();
        effectPanel.addEffect(currentEffect);
        effectPanel.playEffects();
    }

    @Override
    public void setContentWidget(Widget w) {
        this.contentWidget = w;

        if (containerPanel != null) {
            containerPanel.setWidget(w);
        }
    }

    @Override
    public void setLocation(Point location) {
        if (this.location.equals(location)) {
            return;
        }

        this.location = location;

        if (isAttached()) {
            Style style = getEffectPanel().getElement().getStyle();

            style.setPropertyPx(CSS.LEFT, location.getX());
            style.setPropertyPx(CSS.TOP, location.getY());
        }
    }

    @Override
    public void setOpacity(int opacity) {
        setOpacity(opacity, DEFAULT_EFFECT_DURATION);
    }

    @Override
    public void setOpacity(int opacity, double durationInSeconds) {
        assert opacity >= Opacity.TRANSPARENT;
        assert opacity <= Opacity.OPAQUE;
        assert durationInSeconds >= 0;

        if (opacity == this.opacity) {
            return;
        }

        if (opacity == Opacity.TRANSPARENT && !isEffectPanelInitialized()) {
            return;
        }

        // need to attach popup to run effects
        if (!isAttached()) {
            attachPopup();
        }

        NMorphScalar effect = createMorphEffect(this.opacity, opacity,
                durationInSeconds);
        if (isEffectRunning()) {
            nextEffect = effect;
        } else {
            currentEffect = effect;
            runCurrentEffect();
        }
    }

}