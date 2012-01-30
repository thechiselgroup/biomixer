/*******************************************************************************
 * Copyright 2012 David Rusk 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArcElementList implements Iterable<ArcElement> {

    private List<ArcElement> arcElements;

    public ArcElementList() {
        arcElements = new ArrayList<ArcElement>();
    }

    public ArcElementList(List<ArcElement> arcElements) {
        this.arcElements = arcElements;
    }

    public void add(ArcElement arcElement) {
        arcElements.add(arcElement);
    }

    public boolean containsArcWithId(String id) {
        for (ArcElement arcElement : arcElements) {
            if (arcElement.getArcId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public ArcElement getArcElement(String id) {
        for (ArcElement arcElement : arcElements) {
            if (arcElement.getArcId().equals(id)) {
                return arcElement;
            }
        }
        return null;
    }

    @Override
    public Iterator<ArcElement> iterator() {
        return arcElements.iterator();
    }

    public void remove(String arcId) {
        ArcElement arcElement = getArcElement(arcId);
        arcElement.removeNodeConnections();
        arcElements.remove(arcElement);
    }

}
