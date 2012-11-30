/*******************************************************************************
 * Copyright 2009, 2010 Eric Verbeek 
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
package org.thechiselgroup.biomixer.client.core.ui.widget.spinner;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.Spinner;
import com.google.gwt.widgetideas.client.SpinnerListener;

/**
 * Implements the ListBoxPresenter interface to allow for testing.
 * 
 * TODO This class is entirely incomplete and untried. Based off of
 * ListBoxPresenter et al., so see those for more advice.
 */
public class IntegerSpinner extends Spinner implements SpinnerControlPresenter {

    public IntegerSpinner(SpinnerListener spinner, long value, long min,
            long max, int minStep, int maxStep, boolean constrained,
            SpinnerResources images) {
        super(spinner, value, min, max, minStep, maxStep, constrained, images);
    }

    @Override
    public Widget asWidget() {
        return null; // TODO This needs something...wrap spinner in panel?
    }

    @Override
    public HandlerRegistration addChangeHandler(ChangeHandler handler) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLowerLimit(long lowerLimit) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setUpperLimit(long upperLimit) {
        // TODO Auto-generated method stub

    }

    @Override
    public long getLowerLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getUpperLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setValue(long i) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isVisible() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setVisible(boolean visible) {
        // TODO Auto-generated method stub

    }

}
