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
package org.thechiselgroup.biomixer.shared.core.util.date;

/**
 * Provides independence from date parser implementation. It allows for reusing
 * the same code on client and server, enables date parsing optimizations and
 * bugfixes, and improves testability. It supports the date format patterns from
 * {@link com.google.gwt.i18n.client.DateTimeFormat}.
 * 
 * @see com.google.gwt.i18n.client.DateTimeFormat
 * 
 * @author Lars Grammel
 */
public interface DateTimeFormatFactory {

    DateTimeFormat createDateTimeFormat(String pattern);

}