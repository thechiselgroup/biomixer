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
package org.thechiselgroup.biomixer.client.services.hierarchy;

import java.io.IOException;
import java.util.Set;

import org.junit.Before;
import org.thechiselgroup.biomixer.client.services.AbstractJsonParserTest;
import org.thechiselgroup.biomixer.server.workbench.util.json.JavaJsonParser;

public class HierarchyJsonParserTest extends AbstractJsonParserTest {

    private HierarchyJsonParser underTest;

    public HierarchyJsonParserTest() {
        super(HierarchyJsonParserTest.class);
    }

    private Set<String> parseResourcePath(String conceptShortId,
            String virtualOntologyId, String jsonFilename) throws IOException {
        return underTest.parse(conceptShortId,
                getFileContentsAsString(jsonFilename), virtualOntologyId);
    }

    @Before
    public void setUp() {
        underTest = new HierarchyJsonParser(new JavaJsonParser());
    }

}
