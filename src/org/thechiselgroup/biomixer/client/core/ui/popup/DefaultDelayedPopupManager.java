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
package org.thechiselgroup.biomixer.client.core.ui.popup;

import org.thechiselgroup.biomixer.client.core.fx.Opacity;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;

// TODO unify / replace with new popup system
public class DefaultDelayedPopupManager implements DelayedPopupManager {

    private int hideDelay;

    private Timer hideTimer = new Timer() {
        @Override
        public void run() {
            hide();
        }
    };

    private int showDelay;

    private Timer showTimer = new Timer() {
        @Override
        public void run() {
            updatePosition();
            show();
        }
    };

    private final Popup popup;

    public DefaultDelayedPopupManager(int showDelay, int hideDelay, Popup popup) {
        this.showDelay = showDelay;
        this.hideDelay = hideDelay;
        this.popup = popup;

        this.popup.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                hideTimer.cancel();
            }
        },
                MouseOverEvent.getType());

        this.popup.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                hideDelayed();
            }
        },
                MouseOutEvent.getType());
    }

    public DefaultDelayedPopupManager(Popup popup) {
        this(400, 200, popup);
    }

    @Override
    public Popup getPopup() {
        return popup;
    }

    protected void hide() {
        popup.hide();
    }

    @Override
    public void hideDelayed() {
        showTimer.cancel();
        hideTimer.schedule(hideDelay);
    }

    protected void show() {
        popup.setOpacity(Opacity.OPAQUE);
    }

    @Override
    public void showDelayed() {
        hideTimer.cancel();
        showTimer.schedule(showDelay);
    }

    protected void updatePosition() {
        // TODO fix
    }

}
