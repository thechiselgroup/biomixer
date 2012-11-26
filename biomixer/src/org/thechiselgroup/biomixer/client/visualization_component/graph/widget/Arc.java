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
package org.thechiselgroup.biomixer.client.visualization_component.graph.widget;

public class Arc {

    private String id;

    private String sourceNodeId;

    private String targetNodeId;

    private String type;

    private String arcLabel;

    private boolean directed;

    public Arc(String id, String sourceNodeId, String targetNodeId,
            String type, String arcLabel, boolean directed) {

        assert id != null;
        assert sourceNodeId != null;
        assert targetNodeId != null;
        assert type != null;

        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.type = type;
        this.directed = directed;
        this.arcLabel = arcLabel;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Arc other = (Arc) obj;
        if (directed != other.directed) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (sourceNodeId == null) {
            if (other.sourceNodeId != null) {
                return false;
            }
        } else if (!sourceNodeId.equals(other.sourceNodeId)) {
            return false;
        }
        if (targetNodeId == null) {
            if (other.targetNodeId != null) {
                return false;
            }
        } else if (!targetNodeId.equals(other.targetNodeId)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    public String getId() {
        return id;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    public String getTargetNodeId() {
        return targetNodeId;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (directed ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((sourceNodeId == null) ? 0 : sourceNodeId.hashCode());
        result = prime * result
                + ((targetNodeId == null) ? 0 : targetNodeId.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    public boolean isDirected() {
        return directed;
    }

    @Override
    public String toString() {
        return "Arc [id=" + id + ", sourceNodeId=" + sourceNodeId
                + ", targetNodeId=" + targetNodeId + ", type=" + type
                + ", directed=" + directed + ", arcLabel=" + arcLabel + "]";
    }

    public String getLabel() {
        return this.arcLabel;
    }

}
