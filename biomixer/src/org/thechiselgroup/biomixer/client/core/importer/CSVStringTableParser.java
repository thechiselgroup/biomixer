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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.util.CSVParser;

public class CSVStringTableParser implements StringTableParser {

    private CSVParser parser = new CSVParser(',');

    @Override
    public StringTable parse(String data) throws ParseException {
        assert data != null;

        try {
            String[] lines = data.split("\n");

            if (lines.length <= 1) {
                throw new ParseException("No values to import");
            }

            String[] columns = parser.parseLine(lines[0]);
            List<String[]> values = new ArrayList<String[]>();
            for (int i = 1; i < lines.length; i++) {
                String[] lineValues = parser.parseLine(lines[i]);

                if (lineValues.length < columns.length) {
                    throw new ParseException("Not enough values in line '"
                            + lines[i] + "'", i + 1);
                }

                if (lineValues.length > columns.length) {
                    throw new ParseException("Too many values in line '"
                            + lines[i] + "'", i + 1);
                }

                values.add(lineValues);
            }

            return new StringTable(columns, values);
        } catch (IOException ex) {
            return null;
        }
    }
}
