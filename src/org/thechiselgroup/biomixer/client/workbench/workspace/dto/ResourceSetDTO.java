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
package org.thechiselgroup.biomixer.client.workbench.workspace.dto;

import java.io.Serializable;
import java.util.List;

public class ResourceSetDTO implements Serializable {

    private static final long serialVersionUID = -8433016803527402235L;

    private int delegateSetId = -1;

    private int id;

    private String label;

    private List<String> resourceIds;

    public ResourceSetDTO() {
        // for GWT
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResourceSetDTO other = (ResourceSetDTO) obj;
        if (id != other.id)
            return false;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        if (resourceIds == null) {
            if (other.resourceIds != null)
                return false;
        } else if (!resourceIds.equals(other.resourceIds))
            return false;
        return true;
    }

    public int getDelegateSetId() {
        return delegateSetId;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getResourceIds() {
        return resourceIds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result
                + ((resourceIds == null) ? 0 : resourceIds.hashCode());
        return result;
    }

    public boolean isUnmodifiable() {
        return getDelegateSetId() != -1;
    }

    public void setDelegateSetId(int delegateSetId) {
        this.delegateSetId = delegateSetId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

}
