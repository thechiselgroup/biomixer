package org.thechiselgroup.biomixer.client.visualization_component.text;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.DataTypeValidator;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;

import com.google.inject.Inject;

public class ConceptListViewContentDisplayFactory extends
        TextViewContentDisplayFactory {

    public final static String ID = ConceptListViewContentDisplayFactory.class
            .toString();

    @Inject
    public ConceptListViewContentDisplayFactory() {
    }

    @Override
    public TextVisualization createViewContentDisplay(ErrorHandler errorHandler) {
        return new TextVisualization(createDataTypeValidator());
    }

    @Override
    public String getViewContentTypeID() {
        return ID;
    }

    @Override
    public DataTypeValidator createDataTypeValidator() {
        return new Concept.ConceptDataTypeValidator();
    }

}
