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

public class Node {

    private String id;

    private String label;

    private String type;

    private double size;

    public Node(String id, String label, String type, int size) {
        this.size = size;
        assert id != null;
        assert type != null;

        this.id = id;
        this.label = (label == null) ? "" : label;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Note that this size is the basis of the renderable size, and won't be the
     * same as it if transformers are applied.
     * 
     * @return
     */
    public double getSize() {
        return size;
    }

    /**
     * Note that this size is the basis of the renderable size, and won't be the
     * same as it if transformers are applied.
     * 
     * @param size
     */
    public void setSize(double size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Node [id=" + id + ", label=" + label + ", type=" + type + "]";
    }

}
