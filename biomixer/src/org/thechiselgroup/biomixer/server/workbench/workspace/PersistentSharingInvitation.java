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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class PersistentSharingInvitation {

    @Persistent
    private String email;

    @Persistent
    private Date invitationDate;

    @Persistent
    private String password;

    @Persistent
    private String senderUserId;

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key uid;

    @Persistent
    private PersistentWorkspace workspace;

    @Override
    public boolean equals(Object object) {
        return uid.equals(object);
    }

    public String getEmail() {
        return email;
    }

    public Date getInvitationDate() {
        return invitationDate;
    }

    public String getPassword() {
        return password;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public Key getUid() {
        return uid;
    }

    public String getUidAsString() {
        return KeyFactory.keyToString(uid);
    }

    public PersistentWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setInvitationDate(Date invitationDate) {
        this.invitationDate = invitationDate;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSenderUserId(String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public void setUid(Key uid) {
        this.uid = uid;
    }

    public void setWorkspace(PersistentWorkspace workspace) {
        this.workspace = workspace;
    }

}