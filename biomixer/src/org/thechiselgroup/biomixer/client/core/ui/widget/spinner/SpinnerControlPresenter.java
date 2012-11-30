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

import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.user.client.ui.IsWidget;

public interface SpinnerControlPresenter extends HasChangeHandlers, IsWidget {

    void setLowerLimit(long lowerLimit);

    void setUpperLimit(long upperLimit);

    long getLowerLimit();

    long getUpperLimit();

    long getValue();

    void setValue(long i);

    boolean isVisible();

    void setVisible(boolean visible);

}