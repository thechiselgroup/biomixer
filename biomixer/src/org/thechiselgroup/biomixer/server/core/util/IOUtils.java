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
package org.thechiselgroup.biomixer.server.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class IOUtils {

    public static String readIntoString(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream));
        try {
            StringBuffer sb = new StringBuffer(1024);
            char[] buffer = new char[1024];
            int numberOfBytesRead = 0;
            while ((numberOfBytesRead = reader.read(buffer)) != -1) {
                sb.append(String.valueOf(buffer, 0, numberOfBytesRead));
            }
            return sb.toString();
        } finally {
            reader.close();
        }
    }

    private IOUtils() {
    }

}