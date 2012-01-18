package org.thechiselgroup.biomixer.client.core.importer;

import java.util.List;

public class LabeledStringTable extends StringTable {

    private String label;

    // for GWT serialization
    public LabeledStringTable() {

    }

    public LabeledStringTable(String label, String[] columns,
            List<String[]> values) {
        super(columns, values);

        assert label != null;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
