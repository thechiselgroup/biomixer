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
package org.thechiselgroup.biomixer.client.core.label;

public class IncrementingSuffixLabelFactory implements LabelProvider {

    private final String prefix;

    private int currentIndex = 1;

    public IncrementingSuffixLabelFactory(String prefix) {
        assert prefix != null;
        this.prefix = prefix;
    }

    @Override
    public String nextLabel() {
        return prefix + Integer.toString(currentIndex++);
    }

}