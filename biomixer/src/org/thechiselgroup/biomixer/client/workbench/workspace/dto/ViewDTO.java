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
package org.thechiselgroup.biomixer.client.workbench.workspace.dto;

import java.io.Serializable;

import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.resources.Resource;

public class ViewDTO implements Serializable {

    private static final long serialVersionUID = -8166733920666870199L;

    private String contentType;

    // TODO replace with factory
    private String title;

    private Long id;

    private ResourceSetDTO[] resourceSets;

    private Resource[] resources;

    private Memento viewState;

    public String getContentType() {
        return contentType;
    }

    public Long getId() {
        return id;
    }

    public Resource[] getResources() {
        return resources;
    }

    public ResourceSetDTO[] getResourceSets() {
        return resourceSets;
    }

    public String getTitle() {
        return title;
    }

    public Memento getViewState() {
        return viewState;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

    public void setResourceSets(ResourceSetDTO[] resourceSets) {
        this.resourceSets = resourceSets;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setViewState(Memento viewState) {
        this.viewState = viewState;
    }

}
