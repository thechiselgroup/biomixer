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

import org.thechiselgroup.biomixer.client.core.util.math.MathUtils;
import org.thechiselgroup.biomixer.client.core.util.text.HtmlTextBoundsEstimator;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.TextBox;

public class ResizingTextBox extends TextBox {

    /*
     * The extra width should roughly accommodate another character, because the
     * text is extended when a new character is inserted and this might cause
     * the text to move.
     */
    private static final int EXTRA_WIDTH = 15;

    private final int minWidth;

    private final int maxWidth;

    private HtmlTextBoundsEstimator textBoundsEstimator;

    public ResizingTextBox(int minWidth, int maxWidth) {
        assert minWidth >= 0;
        assert maxWidth >= minWidth;

        this.minWidth = minWidth;
        this.maxWidth = maxWidth;

        initTextBoundsEstimator();

        addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateWidth();
            }
        });
        addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                // TODO we could do more here, e.g. simulate delete & backspace
                // filters out backspace, arrows etc
                if (event.getCharCode() > 46) {
                    updateWidth(getText() + event.getCharCode());
                } else {
                    updateWidth();
                }
            }
        });

        // needed for copy / paste / delete etc.
        addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateWidth();
            }
        });
    }

    private void initTextBoundsEstimator() {
        textBoundsEstimator = new HtmlTextBoundsEstimator();
        textBoundsEstimator.setUp();
        /*
         * Bounds estimator will need to be used throughout the lifetime of the
         * application, so don't worry about calling tearDown
         */
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        updateWidth();
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        updateWidth();
    }

    private void updateWidth() {
        updateWidth(getText());
    }

    private void updateWidth(String text) {
        int width = textBoundsEstimator.getWidth(text, getElement());

        // we add some extra width for the focus indicator etc
        width += EXTRA_WIDTH;

        width = MathUtils.restrictToInterval(width, minWidth, maxWidth);
        CSS.setWidth(this, width);
    }

}