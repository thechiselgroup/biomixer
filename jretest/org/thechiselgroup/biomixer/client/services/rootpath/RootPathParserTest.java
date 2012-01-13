package org.thechiselgroup.biomixer.client.services.rootpath;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourcePath;
import org.thechiselgroup.biomixer.server.core.util.IOUtils;
import org.thechiselgroup.biomixer.server.workbench.util.xml.StandardJavaXMLDocumentProcessor;

public class RootPathParserTest {

    private RootPathParser underTest;

    private final String ontologyVersionId = "42948";

    private final String virtualOntologyId = "1148";

    /**
     * Checks the size of the list of resources in the path, not including the
     * target resource
     * 
     * @param path
     *            : ResourcePath
     * @param expectedSize
     *            : Expected size of list of resources in ResourcePath
     */
    private void assertPathResourceSize(ResourcePath path, int expectedSize) {
        assertThat(path.getSurroundingResources().size(), equalTo(expectedSize));
    }

    public void assertResourceStringValueConsistent(Resource resource,
            String valueKey, String expectedValue) {
        assertThat((String) resource.getValue(valueKey), equalTo(expectedValue));
    }

    private void assertResourceUriConsistent(Resource resource, String conceptId) {
        assertThat(resource.getUri(), equalTo(generateUri(conceptId)));
    }

    private void assertUriListSize(UriList uriList, int size) {
        if (uriList == null) {
            // XXX should there be an empty list in the properties? Might be
            // better than leaving null.
            assertThat(size, equalTo(0));
        } else {
            assertThat(uriList.size(), equalTo(size));
        }

    }

    @Test
    public void generatePathThreeLevelChildrenDoubleBranch() throws Exception {
        String conceptId = "BioPortalUser";
        String conceptFullId = "http://protege.stanford.edu/ontologies/metadata/BioPortalMetadata.owl#BioPortalUser";
        String concept1fullId = "http://omv.ontoware.org/2005/05/ontology#Party";
        String concept2fullId = "http://omv.ontoware.org/2005/05/ontology#Person";
        String concept3fullId = "http://omv.ontoware.org/2005/05/ontology#Organisation";

        ResourcePath fullResoucePath = getResourcePath(conceptId,
                "three_level_children_single_branch.response");
        List<Resource> pathToRootResources = fullResoucePath
                .getPathToRootResources();

        assertThat(pathToRootResources.size(), equalTo(3));
        assertThat(pathToRootResources.get(0).getUri(),
                equalTo(generateUri(conceptFullId)));
        assertThat(pathToRootResources.get(1).getUri(),
                equalTo(generateUri(concept2fullId)));
        assertThat(pathToRootResources.get(2).getUri(),
                equalTo(generateUri(concept1fullId)));
    }

    private String generateUri(String conceptId) {
        return Concept.toConceptURI(virtualOntologyId, conceptId);
    }

    private UriList getChildUris(Resource concept) {
        return (UriList) concept.getProperties().get("childConcepts");
    }

    private UriList getParentUris(Resource concept) {
        return (UriList) concept.getProperties().get("parentConcepts");
    }

    private ResourcePath getResourcePath(String conceptId, String xmlFilename)
            throws IOException, Exception {
        String responseXML = IOUtils.readIntoString(RootPathParserTest.class
                .getResourceAsStream(xmlFilename));

        return underTest.parse(ontologyVersionId, virtualOntologyId, conceptId,
                responseXML);
    }

    @Test
    public void parentChildLinks() throws Exception {
        String conceptId = "BioPortalUser";
        ResourcePath path = getResourcePath(conceptId,
                "three_level_children_single_branch.response");

        List<Resource> pathToRootResources = path.getPathToRootResources();
        Resource targetConcept = pathToRootResources.get(0);
        Resource concept2 = pathToRootResources.get(1);
        Resource concept3 = pathToRootResources.get(2);

        UriList targetParentUriList = targetConcept
                .getUriListValue(Concept.PARENT_CONCEPTS);
        UriList targetChildUriList = targetConcept
                .getUriListValue(Concept.CHILD_CONCEPTS);

        assertThat(targetParentUriList.size(), equalTo(1));
        assertThat(targetChildUriList.size(), equalTo(0));
        assertThat(targetParentUriList.getUri(0), equalTo(concept2.getUri()));

        UriList concept2ParentUriList = concept2
                .getUriListValue(Concept.PARENT_CONCEPTS);
        UriList concept2ChildUriList = concept2
                .getUriListValue(Concept.CHILD_CONCEPTS);

        assertThat(concept2ParentUriList.size(), equalTo(1));
        assertThat(concept2ChildUriList.size(), equalTo(1));
        assertThat(concept2ParentUriList.getUri(0), equalTo(concept3.getUri()));
        assertThat(concept2ChildUriList.getUri(0),
                equalTo(targetConcept.getUri()));

        UriList concept3ParentUriList = concept3
                .getUriListValue(Concept.PARENT_CONCEPTS);
        UriList concept3ChildUriList = concept3
                .getUriListValue(Concept.CHILD_CONCEPTS);

        assertThat(concept3ParentUriList.size(), equalTo(0));
        assertThat(concept3ChildUriList.size(), equalTo(2));
        assertTrue(concept3ChildUriList.contains(concept2.getUri()));
    }

    @Test
    public void parseBioPortalUserFullResponse() throws Exception {
        String conceptId = "BioPortalUser";
        String conceptFullId = "http://protege.stanford.edu/ontologies/metadata/BioPortalMetadata.owl#BioPortalUser";
        String concept1fullId = "http://omv.ontoware.org/2005/05/ontology#Party";
        String concept2fullId = "http://omv.ontoware.org/2005/05/ontology#Person";
        String concept3fullId = "http://omv.ontoware.org/2005/05/ontology#Organisation";

        ResourcePath path = getResourcePath(conceptId,
                "bioportaluser_full.response");

        List<Resource> pathToRootResources = path.getPathToRootResources();
        assertThat(pathToRootResources.size(), equalTo(3));
        assertThat(pathToRootResources.get(0).getUri(),
                equalTo(generateUri(conceptFullId)));
        assertThat(pathToRootResources.get(1).getUri(),
                equalTo(generateUri(concept2fullId)));
        assertThat(pathToRootResources.get(2).getUri(),
                equalTo(generateUri(concept1fullId)));
    }

    @Test
    public void parseResponseThingAndSingleChild() throws Exception {
        String conceptId = "Location";
        String conceptFullId = "http://omv.ontoware.org/2005/05/ontology#Location";
        ResourcePath path = getResourcePath(conceptId,
                "thing_and_single_child.response");

        // just the target, no neighbouring path elements
        assertPathResourceSize(path, 0);
        Resource target = path.getTarget();
        assertResourceUriConsistent(target, conceptFullId);
        assertResourceStringValueConsistent(target, Concept.FULL_ID,
                conceptFullId);
        assertResourceStringValueConsistent(target,
                Concept.VIRTUAL_ONTOLOGY_ID, virtualOntologyId);
        assertResourceStringValueConsistent(target, Concept.LABEL, "Location");
    }

    @Test
    public void parseResponseThingAndThreeChildren() throws Exception {
        String conceptId = "OntologyEngineeringMethodology";
        String conceptFullId = "http://omv.ontoware.org/2005/05/ontology#OntologyEngineeringMethodology";
        ResourcePath path = getResourcePath(conceptId,
                "thing_and_three_children.response");

        // target plus two siblings
        assertPathResourceSize(path, 2);
        assertResourceUriConsistent(path.getTarget(), conceptFullId);
        assertResourceUriConsistent(path.getSurroundingResources().get(0),
                "http://omv.ontoware.org/2005/05/ontology#OntologyLanguage");
        assertResourceUriConsistent(path.getSurroundingResources().get(1),
                "http://omv.ontoware.org/2005/05/ontology#KnowledgeRepresentationParadigm");
    }

    @Test
    public void parseResponseThreeLevelChildrenDoubleBranch() throws Exception {

        // Graph structure with t as target, 1=concept1, etc

        // 1
        // |\
        // 2 3
        // |
        // t

        String conceptId = "BioPortalUser";
        String conceptFullId = "http://protege.stanford.edu/ontologies/metadata/BioPortalMetadata.owl#BioPortalUser";
        String concept1fullId = "http://omv.ontoware.org/2005/05/ontology#Party";
        String concept2fullId = "http://omv.ontoware.org/2005/05/ontology#Person";
        String concept3fullId = "http://omv.ontoware.org/2005/05/ontology#Organisation";

        ResourcePath path = getResourcePath(conceptId,
                "three_level_children_single_branch.response");
        Resource concept1 = path.getSurroundingResources().get(0);
        Resource concept2 = path.getSurroundingResources().get(1);
        Resource concept3 = path.getSurroundingResources().get(2);
        Resource target = path.getTarget();

        assertPathResourceSize(path, 3);
        assertResourceUriConsistent(target, conceptFullId);
        assertResourceUriConsistent(concept1, concept1fullId);
        assertResourceUriConsistent(concept2, concept2fullId);
        assertResourceUriConsistent(concept3, concept3fullId);

        // TODO: code duplication, don't know how to eliminate
        UriList concept1parentUris = getParentUris(concept1);
        UriList concept1childUris = getChildUris(concept1);
        assertUriListSize(concept1parentUris, 0);
        assertUriListSize(concept1childUris, 2);
        assertTrue(concept1childUris.contains(generateUri(concept2fullId)));
        assertTrue(concept1childUris.contains(generateUri(concept3fullId)));

        UriList concept2parentUris = getParentUris(concept2);
        UriList concept2childUris = getChildUris(concept2);
        assertUriListSize(concept2parentUris, 1);
        assertUriListSize(concept2childUris, 1);
        assertTrue(concept2parentUris.contains(generateUri(concept1fullId)));
        assertTrue(concept2childUris.contains(generateUri(conceptFullId))); // target

        UriList concept3parentUris = getParentUris(concept3);
        UriList concept3childUris = getChildUris(concept3);
        assertUriListSize(concept3parentUris, 1);
        assertUriListSize(concept3childUris, 0);
        assertTrue(concept3parentUris.contains(generateUri(concept1fullId)));

        UriList targetParentUris = getParentUris(target);
        UriList targetChildUris = getChildUris(target);
        assertUriListSize(targetParentUris, 1);
        assertUriListSize(targetChildUris, 0);
        assertTrue(targetParentUris.contains(generateUri(concept2fullId)));
    }

    @Test
    public void parseResponseTwoLevelChildrenSingleBranch() throws Exception {
        String conceptId = "OntologyView";
        String conceptFullId = "http://protege.stanford.edu/ontologies/metadata/BioPortalMetadata.owl#OntologyView";
        ResourcePath path = getResourcePath(conceptId,
                "two_level_children_single_branch.response");

        assertPathResourceSize(path, 1);
        Resource target = path.getTarget();
        assertResourceUriConsistent(target, conceptFullId);
        assertResourceStringValueConsistent(target, Concept.LABEL,
                "OntologyView");

        Resource concept1 = path.getSurroundingResources().get(0);
        String concept1fullId = "http://omv.ontoware.org/2005/05/ontology#Ontology";
        assertResourceUriConsistent(concept1, concept1fullId);
        assertResourceStringValueConsistent(concept1, Concept.LABEL, "Ontology");

        UriList concept1parentUris = getParentUris(concept1);
        UriList concept1childUris = getChildUris(concept1);

        assertUriListSize(concept1parentUris, 0);
        assertUriListSize(concept1childUris, 1);
        assertTrue(concept1childUris.contains(generateUri(conceptFullId)));

        UriList targetParentUris = (UriList) target.getProperties().get(
                "parentConcepts");
        UriList targetChildUris = getChildUris(target);

        assertUriListSize(targetParentUris, 1);
        assertUriListSize(targetChildUris, 0);
        assertTrue(targetParentUris.contains(generateUri(concept1fullId)));

    }

    @Before
    public void setUp() throws Exception {
        underTest = new RootPathParser(new StandardJavaXMLDocumentProcessor());
    }

}
