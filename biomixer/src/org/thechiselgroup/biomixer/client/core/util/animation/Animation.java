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

/**
 * 
 * An interface which can be used in place of the GWT Animation class which
 * contains JSNI and therefore cannot be used in JRE tests.
 * 
 * See GWT animation JavaDoc:
 * http://google-web-toolkit.googlecode.com/svn/javadoc/2.0/com/google/gwt/
 * animation/client/Animation.html
 * 
 * @author drusk
 * 
 */
public interface Animation {

    void cancel();

    void onUpdate(double progress);

    void run(int duration);

}
