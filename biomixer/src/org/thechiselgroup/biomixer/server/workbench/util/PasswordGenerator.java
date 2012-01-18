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
package org.thechiselgroup.biomixer.server.workbench.util;

import java.security.SecureRandom;

public class PasswordGenerator {

    public static char[] ALPHA_NUMERIC = new char[] { 'A', 'B', 'C', 'D', 'E',
            'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '0' };

    private final SecureRandom random;

    public PasswordGenerator(SecureRandom random) {
        assert random != null;
        this.random = random;
    }

    public String generatePassword(int length) {
        return generatePasswort(length, ALPHA_NUMERIC);
    }

    private String generatePasswort(int length, char[] characters) {
        assert length >= 0;
        assert characters != null;

        StringBuffer result = new StringBuffer();
        for (int i = 0; i < length; i++) {
            result.append(characters[random.nextInt(characters.length)]);
        }
        return result.toString();
    }

}