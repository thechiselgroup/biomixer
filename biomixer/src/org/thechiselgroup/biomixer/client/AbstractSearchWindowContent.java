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
package org.thechiselgroup.biomixer.client;

import static org.thechiselgroup.biomixer.client.visualization_component.text.TextVisualization.LABEL_SLOT;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.FIXED_NUMBER_1_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.TEXT_PROPERTY_RESOLVER_FACTORY;

import java.util.Set;

import org.thechiselgroup.biomixer.client.core.error_handling.LoggingErrorHandler;
import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.persistence.Persistable;
import org.thechiselgroup.biomixer.client.core.persistence.PersistableRestorationService;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetCollector;
import org.thechiselgroup.biomixer.client.core.ui.HasTextParameter;
import org.thechiselgroup.biomixer.client.core.visualization.DefaultView;
import org.thechiselgroup.biomixer.client.dnd.resources.DropEnabledViewContentDisplay;
import org.thechiselgroup.biomixer.client.dnd.windows.AbstractWindowContent;
import org.thechiselgroup.biomixer.client.dnd.windows.ViewWindowContent;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowPanel;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;
import org.thechiselgroup.biomixer.client.services.search.concept.ConceptSearchServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.text.TextVisualization;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This class serves as the basis for search windows, where merely the search
 * service and resource name for displayed text are plugged in.
 * 
 * Extensions of this class implement the
 * {@link #searchForTerm(String, AsyncCallback)} method, which will make use of
 * some search mechanism such as something extending
 * {@link AbstractWebResourceService}, for example,
 * {@link ConceptSearchServiceAsync}.
 * 
 * 
 * @author everbeek
 * 
 */
abstract public class AbstractSearchWindowContent extends AbstractWindowContent
        implements HasTextParameter, Persistable {

    private static class ViewContentDeckpanel extends DeckPanel {

        @Override
        public int getOffsetHeight() {
            // HACK: no padding / margin / border allowed
            return getWidget(getVisibleWidget()).getOffsetHeight();
        }

        @Override
        public int getOffsetWidth() {
            // HACK: no padding / margin / border allowed
            return getWidget(getVisibleWidget()).getOffsetWidth();
        }

        @Override
        public void setPixelSize(int width, int height) {
            getWidget(getVisibleWidget()).setPixelSize(width, height);
            super.setPixelSize(width, height);
        }

        public void updateWindowSize() {
            Widget w = this;
            while (w != null && !(w instanceof WindowPanel)) {
                w = w.getParent();
            }

            if (w == null) {
                return;
            }

            ((WindowPanel) w).adjustSize();
        }

        public void setWindowPanelTitle(String title) {
            Widget w = this;
            while (w != null && !(w instanceof WindowPanel)) {
                w = w.getParent();
            }

            if (w == null) {
                return;
            }
            ((WindowPanel) w).setWindowTitle(title);
        }

    }

    private static final String MEMENTO_HEIGHT = "height";

    private static final String MEMENTO_INDEX = "index";

    private static final String MEMENTO_LABEL = "label";

    private static final String MEMENTO_SEARCH_TERM = "searchTerm";

    private static final String MEMENTO_VIEW = "view";

    private static final String MEMENTO_WIDTH = "width";

    private ViewContentDeckpanel deckPanel;

    private Label infoLabel;

    private final ResourceSetFactory resourceSetFactory;

    private DefaultView resultView;

    private final String textPropertyForResolver;

    private String searchTerm;

    private final ViewWindowContentProducer viewFactory;

    @Inject
    private LoggingErrorHandler loggingErrorHandler;

    /**
     * 
     * @param resourceSetFactory
     * @param textPropertyForResolver
     *            The property for the search service resources, which can be
     *            used as the renderable text for the results.
     * @param viewFactory
     * @param idContentType
     */
    public AbstractSearchWindowContent(ResourceSetFactory resourceSetFactory,
            String textPropertyForResolver,
            ViewWindowContentProducer viewFactory, String idContentType) {

        /*
         * In WindowContent implementations, the proxy view factory should be
         * used to prevent cycles during the initialization.
         */

        super("", idContentType);

        this.resourceSetFactory = resourceSetFactory;
        this.textPropertyForResolver = textPropertyForResolver;
        this.viewFactory = viewFactory;
    }

    @Override
    public Widget asWidget() {
        return deckPanel;
    }

    /**
     * When extending this class, put the actual search method call in this
     * method, making use of the callback provided. A given search service can
     * have multiple search methods, but a given search window has but one
     * search method that it will use.
     * 
     * @param searchTerm
     * @param windowCallBack
     */
    abstract protected void searchForTerm(String queryText,
            AsyncCallback<Set<Resource>> callBack);

    @Override
    public void init() {
        ViewWindowContent windowContent = (ViewWindowContent) viewFactory
                .createWindowContent(TextVisualization.ID);
        resultView = (DefaultView) windowContent.getView();

        infoLabel = new Label("Searching...");
        infoLabel.addStyleName("infoLabel");
        deckPanel = new ViewContentDeckpanel();

        resultView.init();
        deckPanel.add(resultView.asWidget());
        deckPanel.add(infoLabel);
        deckPanel.showWidget(1);

        ((TextVisualization) ((DropEnabledViewContentDisplay) resultView
                .getModel().getViewContentDisplay()).getDelegate())
                .setTagCloud(false);

        if (searchTerm == null) {
            // this is the case if we restore from mememento
            // TODO find better solution
            return;
        }

        searchForTerm(searchTerm, new AsyncCallback<Set<Resource>>() {
            @Override
            public void onFailure(Throwable caught) {
                infoLabel.setText("Search failed for '" + searchTerm + "'");
                deckPanel.updateWindowSize();

                loggingErrorHandler.handleError(caught);
            }

            @Override
            public void onSuccess(Set<Resource> result) {
                if (result.isEmpty()) {
                    infoLabel.setText("No results found for search term '"
                            + searchTerm + "'");
                    deckPanel.updateWindowSize();
                    return;
                }

                // TODO add convenience method to
                // resourceSetFactory
                ResourceSet resourceSet = resourceSetFactory
                        .createResourceSet();

                resourceSet.addAll(result);
                resultView.getResourceModel().addResourceSet(resourceSet);

                updateSearchHeader(resourceSet.size());

                deckPanel.showWidget(0);

                /*
                 * Resizing the deck panel to minimum height of 400px to ensure
                 * that results are visible.
                 */
                int offsetHeight = deckPanel.getOffsetHeight();
                int offsetWidth = deckPanel.getOffsetWidth();
                deckPanel.setPixelSize(offsetWidth,
                        offsetHeight > 400 ? offsetWidth : 400);
                deckPanel.updateWindowSize();

                resultView.getModel().setResolver(
                        LABEL_SLOT,
                        TEXT_PROPERTY_RESOLVER_FACTORY
                                .create(textPropertyForResolver));
                resultView.getModel().setResolver(
                        TextVisualization.FONT_SIZE_SLOT,
                        // was: size 12
                        FIXED_NUMBER_1_RESOLVER_FACTORY.create());
            }

        });
    }

    @Override
    public void initParameter(String searchTerm) {
        setSearchTerm(searchTerm);
    }

    @Override
    public void restore(Memento state,
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor) {

        infoLabel.setText((String) state.getValue(MEMENTO_LABEL));
        setSearchTerm((String) state.getValue(MEMENTO_SEARCH_TERM));
        resultView.restore(state.getChild(MEMENTO_VIEW), restorationService,
                accessor);
        deckPanel.showWidget((Integer) state.getValue(MEMENTO_INDEX));
        deckPanel.setPixelSize((Integer) state.getValue(MEMENTO_WIDTH),
                (Integer) state.getValue(MEMENTO_HEIGHT));
    }

    @Override
    public Memento save(ResourceSetCollector resourceSetCollector) {
        Memento state = new Memento();

        state.setValue(MEMENTO_INDEX, deckPanel.getVisibleWidget());
        state.setValue(MEMENTO_WIDTH, deckPanel.getOffsetWidth());
        state.setValue(MEMENTO_HEIGHT, deckPanel.getOffsetHeight());
        state.setValue(MEMENTO_SEARCH_TERM, searchTerm);
        state.addChild(MEMENTO_VIEW, resultView.save(resourceSetCollector));
        state.setValue(MEMENTO_LABEL, infoLabel.getText());

        return state;
    }

    private void setSearchTerm(String searchTerm) {
        assert searchTerm != null;
        this.searchTerm = searchTerm;
        String baseLabel = "Search results for '" + searchTerm + "'";
        setLabel(baseLabel);
    }

    private void updateSearchHeader(Integer resultCount) {
        if (null != resultCount) {
            deckPanel
                    .setWindowPanelTitle(getLabel() + " (" + resultCount + ")");
        }
    }
}