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
package org.thechiselgroup.biomixer.client.core.development;

import org.thechiselgroup.biomixer.client.core.resources.ResourceSetContainer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.ui.HasTextParameter;

import com.google.gwt.user.client.Command;

/**
 * Creates a benchmarking resource set and adds it to the data sources.
 * 
 * @author Lars Grammel
 */
public class CreateBenchmarkResourcesCommand implements Command,
        HasTextParameter {

    private int numberOfResources;

    private ResourceSetFactory resourceSetFactory;

    private ResourceSetContainer dataSources;

    public CreateBenchmarkResourcesCommand(
            ResourceSetFactory resourceSetFactory,
            ResourceSetContainer dataSources) {

        this.resourceSetFactory = resourceSetFactory;
        this.dataSources = dataSources;
    }

    @Override
    public void execute() {
        dataSources.addResourceSet(BenchmarkResourceSetFactory
                .createResourceSet(numberOfResources, resourceSetFactory));
    }

    @Override
    public void initParameter(String parameter) {
        numberOfResources = Integer.parseInt(parameter);
    }

}