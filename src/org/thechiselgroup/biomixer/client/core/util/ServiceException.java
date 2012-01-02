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
package org.thechiselgroup.biomixer.client.core.util;

// TODO use more specific exception classes
public class ServiceException extends Exception {

    /*
     * INFO: This class is included in Choosel.core to make it available to
     * different components.
     */

    private static final long serialVersionUID = 1L;

    public ServiceException() {
        // for GWT serialization
    }

    public ServiceException(Exception e) {
        super(e);
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Exception cause) {
        super(message, cause);
    }

}