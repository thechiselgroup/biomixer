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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation;

import java.util.List;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

/**
 * Contains methods that may be useful in calculations for several different
 * types of layouts.
 * 
 * @author drusk
 * 
 */
public final class LayoutUtils {

    public static final double getMaxNodeHeight(List<LayoutNode> nodes) {
        double maxHeight = 0.0;
        for (LayoutNode layoutNode : nodes) {
            double height = layoutNode.getSize().getHeight();
            if (height > maxHeight) {
                maxHeight = height;
            }
        }
        return maxHeight;
    }

    public static final double getMaxNodeWidth(List<LayoutNode> nodes) {
        double maxWidth = 0.0;
        for (LayoutNode layoutNode : nodes) {
            double width = layoutNode.getSize().getWidth();
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        return maxWidth;
    }

}
