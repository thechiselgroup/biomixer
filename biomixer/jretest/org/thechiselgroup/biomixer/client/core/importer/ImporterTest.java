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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;

import com.ibm.icu.text.SimpleDateFormat;

public class ImporterTest {

    /**
     * JRE testing compatible importer modification.
     */
    public static class TestImporter extends Importer {

        private SimpleDateFormat dataFormat1;

        private SimpleDateFormat dataFormat2;

        @Override
        protected void initDateFormats() {
            dataFormat1 = new SimpleDateFormat(DATE_FORMAT_1_PATTERN);
            dataFormat2 = new SimpleDateFormat(DATE_FORMAT_2_PATTERN);
        }

        @Override
        protected Date parseDate(String dateStr) {
            try {
                if (DATE_FORMAT_1_REGEX.test(dateStr)) {
                    return dataFormat1.parse(dateStr);
                } else if (DATE_FORMAT_2_REGEX.test(dateStr)) {
                    return dataFormat2.parse(dateStr);
                }
                return null;
            } catch (java.text.ParseException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected Date parseDate1(String stringValue) {
            try {
                return dataFormat1.parse(stringValue);
            } catch (java.text.ParseException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected Date parseDate2(String stringValue) {
            try {
                return dataFormat2.parse(stringValue);
            } catch (java.text.ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Date getDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Importer underTest;

    @Test
    public void emptyStringTable() throws Exception {
        String[] columns = new String[] { "c1" };
        List<String[]> values = new ArrayList<String[]>();

        ResourceSet result = underTest.createResources(new StringTable(columns,
                values));

        assertEquals(0, result.size());
    }

    @Test
    public void errorInconsistentDateColumn() {
        try {
            String[] columns = new String[] { "c1" };
            List<String[]> values = new ArrayList<String[]>();
            values.add(new String[] { "12/12/2012" });
            values.add(new String[] { "text" });

            underTest.createResources(new StringTable(columns, values));

            fail("no ParseException thrown");
        } catch (ParseException e) {
            assertEquals("text", e.getUnparseableValue());
            assertEquals(2, e.getLineNumber());
        }
    }

    @Test
    public void errorInconsistentLocationColumn() {
        try {
            String[] columns = new String[] { "c1" };
            List<String[]> values = new ArrayList<String[]>();
            values.add(new String[] { "0.9/0.2" });
            values.add(new String[] { "text" });

            underTest.createResources(new StringTable(columns, values));

            fail("no ParseException thrown");
        } catch (ParseException e) {
            assertEquals("text", e.getUnparseableValue());
            assertEquals(2, e.getLineNumber());
        }
    }

    @Test
    public void errorInconsistentNumberColumn() {
        try {
            String[] columns = new String[] { "c1" };
            List<String[]> values = new ArrayList<String[]>();
            values.add(new String[] { "0.9" });
            values.add(new String[] { "text" });

            underTest.createResources(new StringTable(columns, values));

            fail("no ParseException thrown");
        } catch (ParseException e) {
            assertEquals("text", e.getUnparseableValue());
            assertEquals(2, e.getLineNumber());
        }
    }

    /**
     * Test for "yyyy-MM-dd" and "dd/MM/yyyy" formats if mixed in column.
     */
    @Test
    public void parsesMixedDateFormats() throws ParseException {
        String[] columns = new String[] { "c1" };
        List<String[]> values = new ArrayList<String[]>();
        values.add(new String[] { "2011-10-08" });
        values.add(new String[] { "08/10/2011" });

        ResourceSet createdResources = underTest
                .createResources(new StringTable(columns, values));

        assertEquals(2, createdResources.size());
        for (Resource resource : createdResources) {
            assertEquals(getDate(2011, 10, 8), resource.getValue("c1"));
        }
    }

    @Test
    public void sameUriTypeInSameImport() throws Exception {
        String[] columns = new String[] { "c1" };
        List<String[]> values = new ArrayList<String[]>();
        values.add(new String[] { "v11" });
        values.add(new String[] { "v21" });

        ResourceSet result = underTest.createResources(new StringTable(columns,
                values));

        Set<String> uris = new HashSet<String>();
        for (Resource resource : result) {
            uris.add(resource.getUri().substring(0,
                    resource.getUri().indexOf(':')));
        }

        assertEquals(1, uris.size());
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        underTest = new TestImporter();
    }

    @Test
    public void uniqueUris() throws Exception {
        String[] columns = new String[] { "c1" };
        List<String[]> values = new ArrayList<String[]>();
        values.add(new String[] { "v1" });

        ResourceSet result1 = underTest.createResources(new StringTable(
                columns, values));
        ResourceSet result2 = underTest.createResources(new StringTable(
                columns, values));

        Set<String> uris = new HashSet<String>();
        for (Resource resource : result1) {
            uris.add(resource.getUri());
        }
        for (Resource resource : result2) {
            uris.add(resource.getUri());
        }

        assertEquals(result1.size() + result2.size(), uris.size());
    }

}
