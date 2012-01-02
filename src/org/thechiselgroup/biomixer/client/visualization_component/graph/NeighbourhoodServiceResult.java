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
package org.thechiselgroup.biomixer.client.visualization_component.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.thechiselgroup.choosel.core.client.resources.Resource;

/**
 * @deprecated Use {@link ResourceNeighbourhood} instead.
 */
@Deprecated
public class NeighbourhoodServiceResult implements Serializable {

    private static final long serialVersionUID = 3287669903192891528L;

    // use HashSet instead of Set for GWT serialization
    private HashSet<Resource> neighbours = new HashSet<Resource>();

    // use ArrayList instead of set List GWT serialization
    private ArrayList<Relationship> relationships = new ArrayList<Relationship>();

    private Resource resource;

    private NeighbourhoodServiceResult() {
        // for GWT serialization
    }

    public NeighbourhoodServiceResult(Resource resource) {
        assert resource != null;
        this.resource = resource;
    }

    private void addNeighbour(Resource resource) {
        assert resource != null;

        if (resource.equals(this.resource)) {
            return;
        }

        neighbours.add(resource);
    }

    public Relationship addRelationship(Resource source, Resource target) {
        addNeighbour(source);
        addNeighbour(target);

        Relationship r = new Relationship(source, target);
        relationships.add(r);
        return r;
    }

    public Set<Resource> getNeighbours() {
        return neighbours;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public Resource getResource() {
        return resource;
    }

}
