package org.thechiselgroup.biomixer.client.embeds;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.ui.widget.listbox.ExtendedListBox;
import org.thechiselgroup.biomixer.client.core.ui.widget.listbox.ListBoxControl;
import org.thechiselgroup.biomixer.client.core.util.UriUtils;
import org.thechiselgroup.biomixer.client.core.util.collections.IdentifiablesList;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbedLoader;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbeddedViewLoader;
import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

public class OntologyCentricEmbedLoader implements EmbeddedViewLoader {

    // public static final String EMBED_MODE = "ontology_overview";
    //
    // @Override
    // public Iterable<String> getEmbedModes() {
    // return new SingleItemIterable<String>(EMBED_MODE);
    // }

    @Inject
    private ErrorHandler errorHandler;

    private IdentifiablesList<OntologyOverviewLoader> embedLoaders = new IdentifiablesList<OntologyOverviewLoader>();

    @Inject
    public OntologyCentricEmbedLoader(
            OntologyOverviewLoader ontologyOverviewLoader) {
        registerLoader(ontologyOverviewLoader);
    }

    @Override
    public Iterable<String> getEmbedModes() {
        return embedLoaders.getIds();
    }

    @Override
    public void loadView(WindowLocation windowLocation, String embedMode,
            AsyncCallback<IsWidget> callback, final EmbedLoader embedLoader) {

        assert embedLoaders.contains(embedMode);

        OntologyEmbedLoader ontologyEmbedLoader = embedLoaders.get(embedMode);

        Window.alert("This won't work");
        String fullConceptId = UriUtils.decodeURIComponent(windowLocation
                .getParameter("full_concept_id"));
        String virtualOntologyId = windowLocation
                .getParameter("virtual_ontology_id");

        // TODO pass in switch

        // final ListBoxControl<OntologyEmbedLoader> selector = new
        // ListBoxControl<OntologyEmbedLoader>(
        // new ExtendedListBox(),
        // new Transformer<OntologyEmbedLoader, String>() {
        // }, errorHandler) {
        //
        // };

        final ListBoxControl<OntologyEmbedLoader> selector = new ListBoxControl<OntologyEmbedLoader>(
                new ExtendedListBox(),
                new Transformer<OntologyEmbedLoader, String>() {
                    @Override
                    public String transform(OntologyEmbedLoader loader) {
                        return loader.getLabel();
                    }
                }, errorHandler);
        for (OntologyEmbedLoader termLoader : embedLoaders) {
            selector.addItem(termLoader);
        }
        selector.setSelectedValue(ontologyEmbedLoader);
        selector.setChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                embedLoader.switchMode(selector.getSelectedValue().getId());
            }
        });

        ontologyEmbedLoader.loadView(virtualOntologyId, selector, callback);

    }

    protected void registerLoader(OntologyOverviewLoader loader) {
        embedLoaders.add(loader);
    }

}
