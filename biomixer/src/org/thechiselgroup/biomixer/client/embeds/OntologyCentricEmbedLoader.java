package org.thechiselgroup.biomixer.client.embeds;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.ui.widget.listbox.ExtendedListBox;
import org.thechiselgroup.biomixer.client.core.ui.widget.listbox.ListBoxControl;
import org.thechiselgroup.biomixer.client.core.util.collections.IdentifiablesList;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbedLoader;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbeddedViewLoader;
import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

/**
 * Loader for ontology oriented embed views. Multiple options can be made
 * available and can be changed between. Currently there is only a pre-packed
 * view for showing the mappings amogn all of the pre-specified ontologies.
 * 
 * @author everbeek
 * 
 */
public class OntologyCentricEmbedLoader implements EmbeddedViewLoader {

    @Inject
    private ErrorHandler errorHandler;

    private IdentifiablesList<OntologyEmbedLoader> embedLoaders = new IdentifiablesList<OntologyEmbedLoader>();

    private String centralNodeUri;

    @Override
    public String getCentralEntityUri() {
        return centralNodeUri;
    }

    @Inject
    public OntologyCentricEmbedLoader(
            OntologyMappingNeighbourhoodLoader ontologyMappingLoader) {
        registerLoader(ontologyMappingLoader);
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

        // TODO This will be a series of ontology acronyms if this is set up for
        // the embed. Probably. Mothballing. Sort this out later.
        String ontologyAcronym = windowLocation
                .getParameter("ontology_acronym");
        List<String> ontologyAcronyms = new ArrayList<String>();
        ontologyAcronyms.add(ontologyAcronym);

        centralNodeUri = Ontology.toOntologyURI(ontologyAcronym);

        // TODO Pulling ontolgoy ids from the current page url works for the
        // embed only...and we are not aiming this feature in the embed version
        // yet.
        // I am mothballing the embed here, so I will pass an empty resource set
        // in, and leave the gathering of relevant ontology resources for later.

        // if (!resourceManager.contains(sourceUri)) {
        // // XXX broken, might need to call to term service?
        // // --> assume available via resource manager...
        // // Resource concept = Resource.createIndexedResource(sourceUri);
        // // resourceManager2.add(concept);
        // }
        //
        // Resource concept = resourceManager.getByUri(sourceUri);
        // Resource ontology =
        ResourceSet virtualOntologies = new DefaultResourceSet();
        // virtualOntologies.add(ontology);

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

        ontologyEmbedLoader.loadView(virtualOntologies, ontologyAcronyms,
                selector, callback);
    }

    protected void registerLoader(OntologyEmbedLoader loader) {
        embedLoaders.add(loader);
    }

}
