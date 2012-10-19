package org.thechiselgroup.biomixer.client.embeds;

import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface OntologyEmbedLoader extends Identifiable {

    String getLabel();

    void loadView(String virtualOntologyId, IsWidget topBarWidget,
            AsyncCallback<IsWidget> callback);

}
