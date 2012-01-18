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
package org.thechiselgroup.biomixer.client.core.ui;

import static org.thechiselgroup.biomixer.client.core.ui.IconURLBuilder.getIconUrl;

import org.thechiselgroup.biomixer.client.core.ui.IconURLBuilder.IconType;
import org.thechiselgroup.biomixer.client.core.util.ObjectUtils;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HasName;

/*
 * This class should not be extended. It is only non-final so we can spy on it in test cases.
 */
public class Action implements Command, HasEnabledState, HasName {

    private String disabledIconUrl;

    private String normalIconUrl;

    private String highlightedIconUrl;

    private String name;

    private String description;

    private boolean enabled;

    private final HandlerManager eventBus;

    private Command command;

    public Action(String name, Command command) {
        this(name, command, null, null, null, true, null);
    }

    public Action(String name, Command command, String iconName) {
        this(name, command, getIconUrl(iconName, IconType.NORMAL), getIconUrl(
                iconName, IconType.DISABLED), getIconUrl(iconName,
                IconType.HIGHLIGHTED), true, null);
    }

    public Action(String name, Command command, String normalIconUrl,
            String disabledIconUrl, String highlightedIconUrl, boolean enabled,
            String description) {

        assert name != null;
        assert command != null;

        this.command = command;
        this.name = name;
        this.description = description;
        this.normalIconUrl = normalIconUrl;
        this.disabledIconUrl = disabledIconUrl;
        this.highlightedIconUrl = highlightedIconUrl;
        this.enabled = enabled;

        this.eventBus = new HandlerManager(this);
    }

    public HandlerRegistration addActionChangedHandler(
            ActionChangedEventHandler handler) {

        assert handler != null;
        return eventBus.addHandler(ActionChangedEvent.TYPE, handler);
    }

    @Override
    public void execute() {
        command.execute();
    }

    protected void fireActionChanged() {
        eventBus.fireEvent(new ActionChangedEvent(this));
    }

    public String getDescription() {
        return description;
    }

    public String getDisabledIconUrl() {
        return disabledIconUrl;
    }

    public String getHighlightedIconUrl() {
        return highlightedIconUrl;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getNormalIconUrl() {
        return normalIconUrl;
    }

    public boolean hasDescription() {
        return description != null;
    }

    public boolean hasDisabledIconUrl() {
        return disabledIconUrl != null;
    }

    public boolean hasHighlightedIconUrl() {
        return highlightedIconUrl != null;
    }

    public boolean hasNormalIconUrl() {
        return normalIconUrl != null;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void removeActionChangedHandler(ActionChangedEventHandler handler) {
        assert handler != null;
        eventBus.removeHandler(ActionChangedEvent.TYPE, handler);
    }

    public void setDescription(String description) {
        if (ObjectUtils.equals(this.description, description)) {
            return;
        }

        this.description = description;

        fireActionChanged();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        fireActionChanged();
    }

    @Override
    public void setName(String name) {
        assert name != null;
        assert this.name != null;

        if (name.equals(this.name)) {
            return;
        }

        this.name = name;

        fireActionChanged();
    }

}