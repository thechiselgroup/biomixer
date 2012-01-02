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
package org.thechiselgroup.biomixer.server.workbench.workspace;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.thechiselgroup.biomixer.client.workbench.workspace.dto.ResourceSetDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.WindowDTO;
import org.thechiselgroup.choosel.core.client.persistence.Memento;
import org.thechiselgroup.choosel.core.client.resources.Resource;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class PersistentView {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;

    @Persistent(serialized = "true")
    private String title;

    @Persistent(serialized = "true")
    private String contentType;

    @Persistent(serialized = "true")
    private Resource[] resources;

    @Persistent(serialized = "true")
    private ResourceSetDTO[] resourceSets;

    @Persistent(serialized = "true")
    private WindowDTO[] windows;

    @Persistent(serialized = "true")
    private Memento viewState;

    @Persistent(serialized = "true")
    private Date sharedDate;

    @Persistent
    private String userEmail;

    @Persistent
    private String userId;

    @Persistent
    private String userName;

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

    public Date getSharedDate() {
        return sharedDate;
    }

    public String getTitle() {
        return title;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public Memento getViewState() {
        return viewState;
    }

    public WindowDTO[] getWindows() {
        return windows;
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

    public void setSharedDate(Date sharedDate) {
        this.sharedDate = sharedDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setViewState(Memento viewState) {
        this.viewState = viewState;
    }

}