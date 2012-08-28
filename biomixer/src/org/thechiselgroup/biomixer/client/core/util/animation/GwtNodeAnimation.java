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

import com.google.gwt.animation.client.Animation;

/**
 * Implementation of {@link NodeAnimation} which makes use of the GWT class
 * {@link Animation}. This class cannot be used in JRE tests due to JSNI code.
 * 
 * @author drusk
 * 
 */
public class GwtNodeAnimation extends NodeAnimation {

    private Animation gwtAnimation;

    public GwtNodeAnimation(LayoutNodeAnimatable animatable) {
        super(animatable);
        init();
    }

    @Override
    protected void doCancel() {
        gwtAnimation.cancel();
    }

    @Override
    protected void doRun(int duration) {
        gwtAnimation.run(duration);
    }

    private void init() {
        gwtAnimation = new Animation() {

            @Override
            protected void onUpdate(double progress) {
                doUpdate(progress);
            }
        };

    }

}
