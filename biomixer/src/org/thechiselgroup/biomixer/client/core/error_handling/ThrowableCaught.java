/*******************************************************************************
 * Copyright 2012 David Rusk 
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

import java.util.Date;

public class ThrowableCaught {

    private final Date timeStamp;

    private final Throwable throwable;

    // maybe some additional information

    public ThrowableCaught(Throwable throwable) {
        this.timeStamp = new Date();
        this.throwable = throwable;
    }

    public ThrowableCaught(Throwable throwable, Date timeStamp) {
        this.throwable = throwable;
        this.timeStamp = timeStamp;
    }

    // @Override
    // public boolean equals(Object other) {
    // return other instanceof ThrowableCaught
    // && timeStamp.equals(((ThrowableCaught) other).getTimeStamp())
    // && throwable.equals(((ThrowableCaught) other).getThrowable());
    // }

    public Throwable getThrowable() {
        return throwable;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return timeStamp.toString() + ": " + throwable.getLocalizedMessage();
    }

}
