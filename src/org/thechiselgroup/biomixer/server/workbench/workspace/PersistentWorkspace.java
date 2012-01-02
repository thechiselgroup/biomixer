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

import java.util.Set;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.ResourceSetDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.WindowDTO;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class PersistentWorkspace {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;

    @Persistent
    @Element(mappedBy = "workspace")
    private Set<PersistentSharingInvitation> invitations;

    @Persistent
    private String name;

    @Persistent
    @Element(mappedBy = "workspace")
    private Set<PersistentWorkspacePermission> permissions;

    @Persistent(serialized = "true")
    private Resource[] resources;

    @Persistent(serialized = "true")
    private ResourceSetDTO[] resourceSets;

    @Persistent(serialized = "true")
    private WindowDTO[] windows;

    public Long getId() {
        return id;
    }

    public Set<PersistentSharingInvitation> getInvitations() {
        return invitations;
    }

    public String getName() {
        return name;
    }

    public Set<PersistentWorkspacePermission> getPermissions() {
        return permissions;
    }

    public Resource[] getResources() {
        return resources;
    }

    public ResourceSetDTO[] getResourceSets() {
        return resourceSets;
    }

    public WindowDTO[] getWindows() {
        return windows;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setInvitations(Set<PersistentSharingInvitation> invitations) {
        this.invitations = invitations;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPermissions(Set<PersistentWorkspacePermission> permissions) {
        this.permissions = permissions;
    }

    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

    public void setResourceSets(ResourceSetDTO[] resourceSets) {
        this.resourceSets = resourceSets;
    }

    public void setWindows(WindowDTO[] windows) {
        this.windows = windows;
    }

}