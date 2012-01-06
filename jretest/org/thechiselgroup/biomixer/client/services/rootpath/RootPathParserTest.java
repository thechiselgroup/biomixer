package org.thechiselgroup.biomixer.client.services.rootpath;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourcePath;
import org.thechiselgroup.biomixer.server.core.util.IOUtils;
import org.thechiselgroup.biomixer.server.workbench.util.xml.StandardJavaXMLDocumentProcessor;

public class RootPathParserTest {

    private RootPathParser underTest;

    private final String ontologyId = "42948";

    private ResourcePath getResourcePath(String conceptId, String xmlFilename)
            throws IOException, Exception {
        String responseXML = IOUtils.readIntoString(RootPathParserTest.class
                .getResourceAsStream(xmlFilename));

        return underTest.parse(ontologyId, conceptId, responseXML);
    }

    @Test
    public void parseResponseThingAndSingleChild() throws Exception {
        String conceptId = "http://omv.ontoware.org/2005/05/ontology#Location";
        ResourcePath path = getResourcePath(conceptId,
                "thing_and_single_child.response");

        // just the target, no neighbouring path elements
        assertThat(path.getResources().size(), equalTo(0));

        Resource target = path.getTarget();

        assertThat(target.getUri(),
                equalTo(Concept.toConceptURI(ontologyId, conceptId)));

        assertThat((String) target.getValue(Concept.FULL_ID),
                equalTo(conceptId));

        assertThat((String) target.getValue(Concept.ONTOLOGY_ID),
                equalTo(ontologyId));

        assertThat((String) target.getValue(Concept.LABEL), equalTo("Location"));

    }

    public void parseResponseThingAndThreeChildren() throws Exception {
        String conceptId = "http://omv.ontoware.org/2005/05/ontology#OntologyEngineeringMethodology";
        ResourcePath path = getResourcePath(conceptId,
                "thing_and_three_children.response");

        // target plus two siblings
        assertThat(path.getResources().size(), equalTo(2));

        assertThat(path.getTarget().getUri(),
                equalTo(Concept.toConceptURI(ontologyId, conceptId)));

        assertThat(
                path.getResources().get(0).getUri(),
                equalTo(Concept
                        .toConceptURI(ontologyId,
                                "http://omv.ontoware.org/2005/05/ontology#OntologyLanguage")));

        assertThat(
                path.getResources().get(1).getUri(),
                equalTo(Concept
                        .toConceptURI(ontologyId,
                                "http://omv.ontoware.org/2005/05/ontology#KnowledgeRepresentationParadigm")));

    }

    @Test
    public void parseResponseThreeLevelChildrenDoubleBranch() throws Exception {

        // Graph structure with t as target, x as other nodes

        // x
        // |\
        // x x
        // |
        // t

        String conceptId = "http://protege.stanford.edu/ontologies/metadata/BioPortalMetadata.owl#BioPortalUser";
        ResourcePath path = getResourcePath(conceptId,
                "three_level_children_single_branch.response");

        assertThat(path.getResources().size(), equalTo(3));

        assertThat(path.getTarget().getUri(),
                equalTo(Concept.toConceptURI(ontologyId, conceptId)));

        Resource concept1 = path.getResources().get(0);
        Resource concept2 = path.getResources().get(1);
        Resource concept3 = path.getResources().get(2);

        assertThat(concept1.getUri(), equalTo(Concept.toConceptURI(ontologyId,
                "http://omv.ontoware.org/2005/05/ontology#Party")));

        assertThat(concept2.getUri(), equalTo(Concept.toConceptURI(ontologyId,
                "http://omv.ontoware.org/2005/05/ontology#Person")));

        assertThat(concept3.getUri(), equalTo(Concept.toConceptURI(ontologyId,
                "http://omv.ontoware.org/2005/05/ontology#Organisation")));

        System.out.println("Concept1: " + concept1.toString());
        System.out.println("Concept2: " + concept2.toString());
        System.out.println("Concept3: " + concept3.toString());
        System.out.println("Target: " + path.getTarget().toString());
    }

    @Test
    public void parseResponseTwoLevelChildrenSingleBranch() throws Exception {
        String conceptId = "http://protege.stanford.edu/ontologies/metadata/BioPortalMetadata.owl#OntologyView";
        ResourcePath path = getResourcePath(conceptId,
                "two_level_children_single_branch.response");

        assertThat(path.getResources().size(), equalTo(1));

        assertThat(path.getTarget().getUri(),
                equalTo(Concept.toConceptURI(ontologyId, conceptId)));

        assertThat((String) path.getTarget().getValue(Concept.LABEL),
                equalTo("OntologyView"));

        Resource concept1 = path.getResources().get(0);

        assertThat(concept1.getUri(), equalTo(Concept.toConceptURI(ontologyId,
                "http://omv.ontoware.org/2005/05/ontology#Ontology")));

        assertThat((String) concept1.getValue(Concept.LABEL),
                equalTo("Ontology"));

        System.out.println("Concept1: " + concept1.toString());
        System.out.println("Target: " + path.getTarget().toString());
    }

    @Before
    public void setUp() throws Exception {
        underTest = new RootPathParser(new StandardJavaXMLDocumentProcessor());
    }

}
