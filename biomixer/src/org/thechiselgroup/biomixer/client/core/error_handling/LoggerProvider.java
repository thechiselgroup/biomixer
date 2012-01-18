/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.logging.Logger;

/**
 * Google Gin does not support java.util.Logger injection right now: <a href=
 * "http://groups.google.com/group/google-gin/browse_thread/thread/b41da0e0067b2bbd"
 * >Gin Group Thread</a>. We therefore use an intermediate interface instead for
 * now. This should be removed once Gin supports Logger injection.
 * 
 * @author Lars Grammel
 */
public interface LoggerProvider {

    Logger getLogger();

}