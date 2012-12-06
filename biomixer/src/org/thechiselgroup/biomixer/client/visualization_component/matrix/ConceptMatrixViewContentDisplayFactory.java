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
package org.thechiselgroup.biomixer.client.visualization_component.matrix;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.DataTypeValidator;
import org.thechiselgroup.biomixer.client.core.command.CommandManager;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.initialization.ViewContentDisplayFactory;
import org.thechiselgroup.biomixer.client.graph.ConceptMappingNeighbourhoodLoader;

import com.google.inject.Inject;

public class ConceptMatrixViewContentDisplayFactory implements
        ViewContentDisplayFactory {

    public final static String ID = ConceptMatrixViewContentDisplayFactory.class
            .toString();

    private String viewName = "Concept Matrix";

    static final int defaultHeight = 400;

    static final int defaultWidth = 300;

    // static final ConceptMatrixRendererFactory rendererFactory = new
    // ConceptMatrixRendererFactory();

    // @Inject
    // private ArcTypeProvider arcStyleProvider;

    @Inject
    private CommandManager commandManager;

    @Inject
    private ResourceCategorizer resourceCategorizer;

    @Inject
    ResourceManager resourceManager;

    @Inject
    private ConceptMappingNeighbourhoodLoader mappingLoader;

    @Inject
    public ConceptMatrixViewContentDisplayFactory() {
    }

    @Override
    public ViewContentDisplay createViewContentDisplay(ErrorHandler errorHandler) {
        return new NeoD3Matrix(createDataTypeValidator());
    }

    // TODO Trying the Timeline approach instead.
    // @Override
    // public ViewContentDisplay createViewContentDisplay(ErrorHandler
    // errorHandler) {
    //
    // return new ConceptMatrix(new ConceptMatrixDisplayController(
    // defaultWidth, defaultHeight, viewName, rendererFactory,
    // errorHandler), commandManager, resourceManager,
    // resourceCategorizer,
    // // arcStyleProvider,
    // mappingLoader, errorHandler, createDataTypeValidator());
    // }

    @Override
    public DataTypeValidator createDataTypeValidator() {
        return new Concept.ConceptDataTypeValidator();
    }

    @Override
    public String getViewContentTypeID() {
        return ID;
    }
}