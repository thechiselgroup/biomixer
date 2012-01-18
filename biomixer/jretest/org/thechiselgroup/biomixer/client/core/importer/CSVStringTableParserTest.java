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
package org.thechiselgroup.biomixer.client.core.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.importer.CSVStringTableParser;
import org.thechiselgroup.biomixer.client.core.importer.ParseException;
import org.thechiselgroup.biomixer.client.core.importer.StringTable;

public class CSVStringTableParserTest {

    private CSVStringTableParser underTest;

    @Test
    public void failureNoData() {
        try {
            String data = "";
            underTest.parse(data);
            fail("no exception thrown");
        } catch (ParseException e) {
            assertEquals(-1, e.getLineNumber());
        }
    }

    @Test
    public void failureNotEnoughValues() {
        try {
            String data = "columnA,columnB\nvalue1A,value1B\nvalue2A";
            underTest.parse(data);
            fail("no exception thrown");
        } catch (ParseException e) {
            assertEquals(3, e.getLineNumber());
        }
    }

    @Test
    public void failureTooManyValues() {
        try {
            String data = "columnA,columnB\nvalue1A,value1B\nvalue2A,value2B,value2C";
            underTest.parse(data);
            fail("no exception thrown");
        } catch (ParseException e) {
            assertEquals(3, e.getLineNumber());
        }
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        underTest = new CSVStringTableParser();
    }

    @Test
    public void simpleImport() throws ParseException {
        String data = "columnA,columnB\nvalue1A,value1B\nvalue2A,value2B";

        StringTable result = underTest.parse(data);

        assertEquals(2, result.getColumnCount());
        assertEquals(2, result.getRowCount());

        assertEquals("columnA", result.getColumnName(0));
        assertEquals("columnB", result.getColumnName(1));

        assertEquals("value1A", result.getValue(0, 0));
        assertEquals("value1B", result.getValue(0, 1));
        assertEquals("value2A", result.getValue(1, 0));
        assertEquals("value2B", result.getValue(1, 1));
    }

}
