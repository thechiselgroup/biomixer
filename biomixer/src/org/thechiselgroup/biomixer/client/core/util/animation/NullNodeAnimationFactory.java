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
package org.thechiselgroup.biomixer.client.core.util.animation;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.LayoutNodeAnimatable;

/**
 * Creates {@link NullNodeAnimation}s which are safe to be used in JRE tests
 * since they do not use JSNI code.
 * 
 * @author drusk
 * 
 */
public class NullNodeAnimationFactory implements NodeAnimationFactory {

    @Override
    public NodeAnimation createNodeAnimation(LayoutNodeAnimatable animatable) {
        return new NullNodeAnimation(animatable);
    }

}
