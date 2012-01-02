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
package org.thechiselgroup.biomixer.client.core.visualization.behaviors;

import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.HighlightingModel;

/**
 * Manages the highlighting state of a single user interface resource (i.e. if
 * the resource is currently triggering highlighting or not). This is required
 * because we use a counting resource set and every highlighting trigger needs
 * to always remove the highlighting appropriately.
 * 
 * @see http://code.google.com/p/choosel/issues/detail?id=30
 * 
 * @author Lars Grammel
 */
public class HighlightingManager implements Disposable {

    private boolean highlighted = false;

    private ResourceSet resources;

    private HighlightingModel hoverModel;

    public HighlightingManager(HighlightingModel hoverModel, ResourceSet resources) {
        assert hoverModel != null;
        assert resources != null;

        this.hoverModel = hoverModel;
        this.resources = resources;
    }

    @Override
    public void dispose() {
        setHighlighting(false);
    }

    public void setHighlighting(boolean shouldHighlight) {
        if (shouldHighlight == highlighted) {
            return;
        }

        if (shouldHighlight) {
            hoverModel.addHighlightedResources(resources);
        } else {
            hoverModel.removeHighlightedResources(resources);
        }

        highlighted = shouldHighlight;
    }
}