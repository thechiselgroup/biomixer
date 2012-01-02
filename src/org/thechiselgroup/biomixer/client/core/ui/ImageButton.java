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

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Image;

public class ImageButton extends Image implements HasEnabledState {

    private static final String EXPANDER = "expander";

    public static ImageButton createExpanderButton() {
        return new ImageButton(IconURLBuilder.getIconUrl(EXPANDER,
                IconType.NORMAL, ""), IconURLBuilder.getIconUrl(EXPANDER,
                IconType.HIGHLIGHTED, ""), null);
    }

    public static ImageButton createImageButton(String name) {
        return new ImageButton(getIconUrl(name, IconType.NORMAL), getIconUrl(
                name, IconType.HIGHLIGHTED),
                getIconUrl(name, IconType.DISABLED));
    }

    private String disabledUrl;

    private String normalUrl;

    private String highlightedUrl;

    private boolean enabled = true;

    private boolean mouseOver = false;

    public ImageButton(String normalUrl, String highlightedUrl,
            String disabledUrl) {

        assert normalUrl != null;

        this.normalUrl = normalUrl;
        this.highlightedUrl = highlightedUrl != null ? highlightedUrl
                : normalUrl;
        this.disabledUrl = disabledUrl != null ? disabledUrl : normalUrl;

        setUrl(normalUrl);

        addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                mouseOver = true;
                if (enabled) {
                    setUrl(ImageButton.this.highlightedUrl);
                }
            }
        });
        addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                mouseOver = false;
                if (enabled) {
                    setUrl(ImageButton.this.normalUrl);
                }
            }
        });
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled == this.enabled) {
            return;
        }

        // TODO focus changes !??
        // if (!enabled) {
        // focusable.setFocus(false);
        // }

        this.enabled = enabled;

        if (enabled) {
            if (mouseOver) {
                setUrl(highlightedUrl);
            } else {
                setUrl(normalUrl);
            }
        } else {
            setUrl(disabledUrl);
        }
    }

}