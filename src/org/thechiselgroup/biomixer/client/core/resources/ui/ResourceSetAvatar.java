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
package org.thechiselgroup.biomixer.client.core.resources.ui;

import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.ui.CSS;
import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.core.util.DisposableComposite;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;

public class ResourceSetAvatar extends Label implements Disposable {

    protected static final String CSS_AVATAR_DISABLED = "avatar-disabled";

    protected static final String CSS_CLASS = "avatar";

    protected static final String CSS_HOVER = "avatar-hover";

    private DisposableComposite disposables = new DisposableComposite();

    private boolean enabled = true;

    protected String enabledCSSClass;

    protected ResourceSet resourceSet;

    private boolean showHover;

    protected final ResourceSetAvatarType type;

    public ResourceSetAvatar(String text, String enabledCSSClass,
            ResourceSet resources, ResourceSetAvatarType type) {

        assert enabledCSSClass != null;
        assert resources != null;
        assert type != null;

        resourceSet = resources;
        this.enabledCSSClass = enabledCSSClass;
        this.type = type;

        addStyleName(CSS_CLASS);
        setText(text);
        updateState();
    }

    protected ResourceSetAvatar(String text, String enabledCSSClass,
            ResourceSet resources, ResourceSetAvatarType type, Element element) {

        super(element);

        assert resources != null;
        assert enabledCSSClass != null;
        assert type != null;

        resourceSet = resources;
        this.enabledCSSClass = enabledCSSClass;
        this.type = type;

        addStyleName(CSS_CLASS);
        setText(text);
    }

    public void addDisposable(Disposable disposable) {
        disposables.addDisposable(disposable);
    }

    public HandlerRegistration addEnabledStatusHandler(
            ResourceSetAvatarEnabledStatusEventHandler handler) {
        return addHandler(handler, ResourceSetAvatarEnabledStatusEvent.TYPE);
    }

    public HandlerRegistration addResourceChangedHandler(
            ResourceSetAvatarResourcesChangedEventHandler handler) {
        return addHandler(handler, ResourceSetAvatarResourcesChangedEvent.TYPE);
    }

    @Override
    public void dispose() {
        if (isDisposed()) {
            return;
        }

        disposables.dispose();
        disposables = null;

        /*
         * we do not clear the resources on purpose, sometimes they are used
         * after dispose.
         */
    }

    public String getDisabledCSSClass() {
        return CSS_AVATAR_DISABLED;
    }

    public final String getEnabledCSSClass() {
        return enabledCSSClass;
    }

    public ResourceSet getResourceSet() {
        return resourceSet;
    }

    public ResourceSetAvatarType getType() {
        return type;
    }

    private boolean isDisposed() {
        return disposables == null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (isEnabled() == enabled) {
            return;
        }

        this.enabled = enabled;

        updateState();

        fireEvent(new ResourceSetAvatarEnabledStatusEvent(this));
    }

    // XXX HACK clean up
    public void setEnabledCSSClass(String enabledCSSClass) {
        if (isEnabled()) {
            removeStyleName(this.enabledCSSClass);
            addStyleName(enabledCSSClass);
        }
        this.enabledCSSClass = enabledCSSClass;
    }

    // TODO move this functionality into highlighting drag avatar factory
    public void setHover(boolean showHover) {
        if (this.showHover == showHover) {
            return;
        }

        if (showHover) {
            addStyleName(CSS_HOVER);
            removeStyleName(getEnabledCSSClass());
        } else {
            removeStyleName(CSS_HOVER);
            addStyleName(getEnabledCSSClass());
        }

        this.showHover = showHover;
    }

    public void setResourceSet(ResourceSet newResourceSet) {
        if (newResourceSet == resourceSet) {
            return;
        }

        ResourceSet oldResources = resourceSet;

        resourceSet = newResourceSet;

        fireEvent(new ResourceSetAvatarResourcesChangedEvent(this,
                newResourceSet, oldResources));
    }

    protected void updateState() {
        if (isEnabled()) {
            getElement().getStyle().setProperty(CSS.CURSOR, CSS.CURSOR_POINTER);
            addStyleName(getEnabledCSSClass());
            removeStyleName(getDisabledCSSClass());
        } else {
            getElement().getStyle().setProperty(CSS.CURSOR, CSS.CURSOR_DEFAULT);
            addStyleName(getDisabledCSSClass());
            removeStyleName(getEnabledCSSClass());
        }
    }

}