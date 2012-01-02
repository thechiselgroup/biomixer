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
package org.thechiselgroup.biomixer.server.core.util.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.thechiselgroup.biomixer.shared.core.util.date.DateTimeFormat;
import org.thechiselgroup.biomixer.shared.core.util.date.DateTimeFormatFactory;

public class SimpleDateTimeFormatFactory implements DateTimeFormatFactory {

    public static class SimpleDateFormatWrapper implements DateTimeFormat {

        private final SimpleDateFormat delegate;

        public SimpleDateFormatWrapper(String pattern) {
            this.delegate = new SimpleDateFormat(pattern);
        }

        @Override
        public String format(Date date) {
            return delegate.format(date);
        }

        @Override
        public Date parse(String text) throws IllegalArgumentException {
            try {
                return delegate.parse(text);
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    @Override
    public DateTimeFormat createDateTimeFormat(String pattern) {
        return new SimpleDateFormatWrapper(pattern);
    }

}
