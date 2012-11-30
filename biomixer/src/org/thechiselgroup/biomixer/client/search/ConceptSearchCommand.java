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
package org.thechiselgroup.biomixer.client.search;

import org.thechiselgroup.biomixer.client.core.command.CommandManager;
import org.thechiselgroup.biomixer.client.dnd.windows.Desktop;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowContentProducer;
import org.thechiselgroup.biomixer.client.visualization_component.text.ConceptListViewContentDisplayFactory;
import org.thechiselgroup.biomixer.client.workbench.ui.SearchCommand;

import com.google.inject.Inject;

public class ConceptSearchCommand extends SearchCommand {

    public static final String NCBO_CONCEPT_SEARCH = ConceptListViewContentDisplayFactory.ID; // "ncbo-search-concepts";

    @Inject
    public ConceptSearchCommand(CommandManager commandManager, Desktop desktop,
            WindowContentProducer viewFactory) {
        super(commandManager, desktop, viewFactory, NCBO_CONCEPT_SEARCH);
    }

}
