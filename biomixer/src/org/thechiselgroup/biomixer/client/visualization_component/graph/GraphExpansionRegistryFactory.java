package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogManager;
import org.thechiselgroup.biomixer.client.graph.AutomaticConceptExpander;
import org.thechiselgroup.biomixer.client.graph.AutomaticOntologyExpander;
import org.thechiselgroup.biomixer.client.graph.ConceptConceptNeighbourhoodExpander;
import org.thechiselgroup.biomixer.client.graph.ConceptConceptNeighbourhoodLoader;
import org.thechiselgroup.biomixer.client.graph.ConceptMappingNeighbourhoodExpander;
import org.thechiselgroup.biomixer.client.graph.ConceptMappingNeighbourhoodLoader;
import org.thechiselgroup.biomixer.client.graph.MappingExpander;
import org.thechiselgroup.biomixer.client.graph.OntologyNodeMappingExpander;
import org.thechiselgroup.biomixer.client.services.mapping.ConceptMappingServiceAsync;
import org.thechiselgroup.biomixer.client.services.ontology_overview.OntologyMappingCountServiceAsync;
import org.thechiselgroup.biomixer.client.services.search.ontology.OntologyMetricServiceAsyncClientImplementation;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;

import com.google.inject.Inject;

public class GraphExpansionRegistryFactory {

    @Inject
    // Needed this here due to need for explicit constructor call; can't push
    // field injection into receiving class below.
    DialogManager dialogManager;

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private ConceptMappingServiceAsync conceptMappingService;

    @Inject
    private OntologyMappingCountServiceAsync ontologyMappingService;

    @Inject
    // Can't make this the interface despite binding...why not?
    private OntologyMetricServiceAsyncClientImplementation ontologyMetricService;

    @Inject
    private ConceptNeighbourhoodServiceAsync conceptNeighbourhoodService;

    @Inject
    private TermServiceAsync termService;

    public GraphExpansionRegistry createRegistry(ErrorHandler errorHandler) {

        DefaultGraphExpansionRegistry registry = new DefaultGraphExpansionRegistry();

        registry.putAutomaticExpander(Concept.RESOURCE_URI_PREFIX,
                new AutomaticConceptExpander(
                        new ConceptMappingNeighbourhoodLoader(
                                conceptMappingService, resourceManager,
                                errorHandler),
                        new ConceptConceptNeighbourhoodLoader(errorHandler,
                                resourceManager, conceptNeighbourhoodService)));

        registry.putNodeMenuEntry(Concept.RESOURCE_URI_PREFIX, "Concepts",
                new ConceptConceptNeighbourhoodExpander(errorHandler,
                        resourceManager, conceptNeighbourhoodService,
                        dialogManager));

        registry.putNodeMenuEntry(Concept.RESOURCE_URI_PREFIX, "Mappings",
                new ConceptMappingNeighbourhoodExpander(conceptMappingService,
                        errorHandler, resourceManager, termService));

        registry.putNodeMenuEntry(Mapping.RESOURCE_URI_PREFIX, "Concepts",
                new MappingExpander(resourceManager));

        registry.putAutomaticExpander(Ontology.RESOURCE_URI_PREFIX,
                new AutomaticOntologyExpander(ontologyMetricService,
                        errorHandler));

        registry.putAutomaticBulkExpander(Ontology.RESOURCE_URI_PREFIX,
                new OntologyNodeMappingExpander(ontologyMappingService,
                        errorHandler));

        return registry;
    }

}