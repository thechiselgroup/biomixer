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

import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;

public class Arc {

    private String id;

    private String sourceNodeId;

    private String targetNodeId;

    private String type;

    /**
     * This is a generic weight, and semantics depend on the arc semantics.
     */
    private double weight = 1.0;

    /**
     * The size of the arc is intended to mean the number of other arcs that are
     * abstracted over by this arc. That is, if the arc represents a
     * congregation of other relation information, then size is the number of
     * relations aggregated over in that congregation. For example, ontology
     * arcs would have a size corresponding to the number of concept mapping
     * arcs between the two ontologies.
     */
    private int outgoingSize = 1;

    // If we want bidirectional rather than nondirectional mapping arcs, work
    // out the best way of having incoming and outgoing counts.
    // private int incomingSize = 1;

    private String arcLabel;

    private boolean directed;

    private final VisualItem visualItem;

    public Arc(String id, String sourceNodeId, String targetNodeId,
            String type, String arcLabel, boolean directed,
            VisualItem visualItem) {

        assert id != null;
        assert sourceNodeId != null;
        assert targetNodeId != null;
        assert type != null;
        assert visualItem != null;

        this.visualItem = visualItem;
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
        }
        if (targetNodeId == null) {
            if (other.targetNodeId != null) {
                return false;
            }
        }

        // Already know direction here, so safe to look at only this.direction.
        String thisFirstUri;
        String thisSecondUri;
        String otherFirstUri;
        String otherSecondUri;
        if (!directed) {
            boolean isConcept1First = sourceNodeId.compareTo(targetNodeId) < 0;
            thisFirstUri = isConcept1First ? sourceNodeId : targetNodeId;
            thisSecondUri = isConcept1First ? targetNodeId : sourceNodeId;
            otherFirstUri = isConcept1First ? other.sourceNodeId
                    : other.targetNodeId;
            ;
            otherSecondUri = isConcept1First ? other.targetNodeId
                    : other.sourceNodeId;
            ;
        } else {
            thisFirstUri = sourceNodeId;
            thisSecondUri = targetNodeId;
            otherFirstUri = other.sourceNodeId;
            otherSecondUri = other.targetNodeId;
        }

        if (!thisFirstUri.equals(otherFirstUri)) {
            return false;
        }

        if (!thisSecondUri.equals(otherSecondUri)) {
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

    /**
     * Get this arc's weight.
     * 
     * @return
     */
    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Get the number of lower level arcs that this arc aggregates over
     * (representatively). For example, if the arc represents mappings between
     * two ontologies, it might be actually aggregating conceptually over top of
     * all the concept to concept mapping arcs.
     * 
     * @return
     */
    public int getSize() {
        return outgoingSize;
    }

    public void setSize(int size) {
        this.outgoingSize = size;
    }

    // /**
    // * Like {@link Arc#getOutgoingSize()}, but count from target to source.
    // * Frequently equal for mapping arcs. For directed arcs, this is likely
    // not
    // * useful, but in the case of mapping undirected arcs, it allows us to use
    // a
    // * single arc rather than two.
    // *
    // * @return
    // */
    // public int getIncomingSize() {
    // return incomingSize;
    // }

    // /**
    // * As per {@link Arc#getOutgoingSize()}. Sets both incoming and outgoing
    // * size.
    // *
    // * @param outgoingSize
    // * @param incomingSize
    // */
    // public void setSize(int outgoingSize, int incomingSize) {
    // this.outgoingSize = outgoingSize;
    // this.incomingSize = incomingSize;
    // }

    @Override
    public int hashCode() {
        String firstUri;
        String secondUri;
        if (!directed) {
            boolean isConcept1First = sourceNodeId.compareTo(targetNodeId) < 0;
            firstUri = isConcept1First ? sourceNodeId : targetNodeId;
            secondUri = isConcept1First ? targetNodeId : sourceNodeId;
        } else {
            firstUri = sourceNodeId;
            secondUri = targetNodeId;
        }
        final int prime = 31;
        int result = 1;
        result = prime * result + (directed ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((firstUri == null) ? 0 : firstUri.hashCode());
        result = prime * result
                + ((secondUri == null) ? 0 : secondUri.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        // Don't use weight or size in here, I don't think.
        return result;
    }

    public boolean isDirected() {
        return directed;
    }

    @Override
    public String toString() {
        return "Arc [id=" + id + ", sourceNodeId=" + sourceNodeId
                + ", targetNodeId=" + targetNodeId + ", type=" + type
                + ", directed=" + directed + ", weight=" + weight
                + ", arcLabel=" + arcLabel + ", size=" + outgoingSize
                // + ", outgoingSize=" + outgoingSize
                // + ", incomingSize=" + incomingSize
                + "]";
    }

    public String getLabel() {
        return this.arcLabel;
    }

    /**
     * More convenient than needing a reference to the Graph object in order to
     * get at the underlying model with the similarly named method found there.
     * 
     * @return
     */
    public VisualItem getVisualItem() {
        return this.visualItem;
    }
}
