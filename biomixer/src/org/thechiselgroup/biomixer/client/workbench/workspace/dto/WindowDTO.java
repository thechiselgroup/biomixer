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

public class WindowDTO implements Serializable {

    private static final long serialVersionUID = -8166733920666870199L;

    private String contentType;

    private int height;

    // TODO replace with factory
    private String title;

    private Memento viewState;

    private int width;

    private int x;

    private int y;

    public String getContentType() {
        return contentType;
    }

    public int getHeight() {
        return height;
    }

    public String getTitle() {
        return title;
    }

    public Memento getViewState() {
        return viewState;
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setViewState(Memento viewState) {
        this.viewState = viewState;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

}