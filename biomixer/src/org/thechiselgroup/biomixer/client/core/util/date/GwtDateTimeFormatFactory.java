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
package org.thechiselgroup.biomixer.client.core.util.date;

import java.util.Date;

import org.thechiselgroup.biomixer.shared.core.util.date.DateTimeFormat;
import org.thechiselgroup.biomixer.shared.core.util.date.DateTimeFormatFactory;

public class GwtDateTimeFormatFactory implements DateTimeFormatFactory {

    public static class GwtDateFormatWrapper implements DateTimeFormat {

        private final com.google.gwt.i18n.client.DateTimeFormat delegate;

        public GwtDateFormatWrapper(String pattern) {
            this.delegate = com.google.gwt.i18n.client.DateTimeFormat
                    .getFormat(pattern);
        }

        @Override
        public String format(Date date) {
            return delegate.format(date);
        }

        @Override
        public Date parse(String text) throws IllegalArgumentException {
            /*
             * XXX Hack because GWT DateTimeFormat does not support time zone
             * labels, only works with 'z' parsing and for Pacific Time
             */
            text = text.replace("PST", "UTC-8");
            text = text.replace("PDT", "UTC-7");

            return delegate.parse(text);
        }
    }

    @Override
    public DateTimeFormat createDateTimeFormat(String pattern) {
        return new GwtDateFormatWrapper(pattern);
    }
}
