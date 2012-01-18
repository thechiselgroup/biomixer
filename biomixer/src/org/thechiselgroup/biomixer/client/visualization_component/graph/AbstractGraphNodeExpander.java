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

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

/**
 * @deprecated Use {@link AbstractGraphNodeSingleResourceNeighbourhoodExpander}
 *             instead.
 */
@Deprecated
public abstract class AbstractGraphNodeExpander implements GraphNodeExpander {

    protected void addResources(GraphNodeExpansionCallback expansionCallback,
            List<String> resourceUrisToAdd, Resource resource) {

        List<String> added = new ArrayList<String>();
        for (String uri : resourceUrisToAdd) {
            if (!expansionCallback.containsResourceWithUri(uri)) {
                if (expansionCallback.getResourceManager().contains(uri)) {
                    /*
                     * XXX we are ignoring resources that are not available in
                     * the resource manager.
                     */
                    Resource resourceToAdd = expansionCallback
                            .getResourceManager().getByUri(uri);
                    assert resourceToAdd != null : "resource with uri '" + uri
                            + "' must be available";
                    expansionCallback.addAutomaticResource(resourceToAdd);
                    added.add(uri);
                }
            }
        }

        // TODO extract + refactor layout (have method layout on node)
        Node inputNode = expansionCallback.getDisplay().getNode(
                resource.getUri());
        Point inputLocation = expansionCallback.getDisplay().getLocation(
                inputNode);

        List<Node> nodesToLayout = new ArrayList<Node>();
        for (String uri : added) {
            Node node = expansionCallback.getDisplay().getNode(uri);
            expansionCallback.getDisplay().setLocation(node, inputLocation);
            nodesToLayout.add(node);
        }

        expansionCallback.getDisplay().runLayoutOnNodes(nodesToLayout);
    }

    // TODO do not ask for a uri list
    protected List<String> calculateUrisToAdd(Resource resource,
            String... properties) {

        List<String> resourceUrisToAdd = new ArrayList<String>();

        for (String property : properties) {

            if (resource.isUriList(property)) {
                for (String uri : resource.getUriListValue(property)) {
                    resourceUrisToAdd.add(uri);
                }
            }

            else {
                resourceUrisToAdd.add((String) resource.getValue(property));
            }
        }

        return resourceUrisToAdd;
    }

}
