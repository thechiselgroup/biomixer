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
 * Implementation of {@link NodeAnimation} which does not have any have any
 * dependency on JSNI code. Can be used in JRE tests. It causes the animation to
 * complete immediately with no intermediary steps.
 * 
 * @author drusk
 * 
 */
public class NullNodeAnimation extends NodeAnimation {

    public NullNodeAnimation(LayoutNodeAnimatable animatable) {
        super(animatable);
    }

    @Override
    protected void doCancel() {
        // nothing to cancel
    }

    @Override
    protected void doRun(int duration) {
        doUpdate(1d);
    }

}
