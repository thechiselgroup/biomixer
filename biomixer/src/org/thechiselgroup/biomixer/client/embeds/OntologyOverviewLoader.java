package org.thechiselgroup.biomixer.client.embeds;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class OntologyOverviewLoader implements OntologyEmbedLoader {

    private final String id;

    private final String label;

    public OntologyOverviewLoader(String label, String id) {
        assert label != null;
        assert id != null;

        this.id = id;
        this.label = label;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void loadView(String virtualOntologyId, String fullConceptId,
            IsWidget topBarWidget, AsyncCallback<IsWidget> callback) {

    }

}
