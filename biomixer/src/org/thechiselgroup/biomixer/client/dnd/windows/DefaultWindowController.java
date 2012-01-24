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

import org.thechiselgroup.biomixer.client.core.geometry.Point;

/*
 * This class implements algorithms related to WindowPanel and executes them on callback
 * methods. The intention of this class is to enable the testing of error-prone areas
 * of the Window framework. 
 */
public class DefaultWindowController implements WindowController {

    private WindowCallback callback;

    public DefaultWindowController(WindowCallback callback) {
        assert callback != null;

        this.callback = callback;
    }

    @Override
    public int getHeight() {
        return callback.getHeight();
    }

    @Override
    public int getWidth() {
        return callback.getWidth();
    }

    @Override
    public void resize(int deltaX, int deltaY, int targetWidth, int targetHeight) {
        assert targetWidth >= 0;
        assert targetHeight >= 0;

        callback.setPixelSize(targetWidth, targetHeight);

        if (deltaX != 0 || deltaY != 0) {
            /*
             * adjust move to the extent the resize actually succeeded
             */
            if (deltaX != 0) {
                deltaX += targetWidth - getWidth();
            }
            if (deltaY != 0) {
                deltaY += targetHeight - getHeight();
            }

            Point location = callback.getLocation();
            callback.setLocation(location.getX() + deltaX, location.getY()
                    + deltaY);
        }
    }

}