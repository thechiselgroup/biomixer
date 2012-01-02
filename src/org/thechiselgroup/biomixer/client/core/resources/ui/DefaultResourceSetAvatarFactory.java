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
package org.thechiselgroup.biomixer.client.core.resources.ui;

import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;

import com.google.inject.Inject;

public class DefaultResourceSetAvatarFactory implements
        ResourceSetAvatarFactory {

    private String enabledCSSClass;

    private ResourceSetAvatarType type;

    @Inject
    public DefaultResourceSetAvatarFactory(String enabledCSSClass,
            ResourceSetAvatarType type) {
        assert enabledCSSClass != null;
        assert type != null;

        this.enabledCSSClass = enabledCSSClass;
        this.type = type;
    }

    @Override
    public ResourceSetAvatar createAvatar(ResourceSet resources) {
        assert resources != null;
        return new ResourceSetAvatar(resources.getLabel(), enabledCSSClass,
                resources, type);
    }

}