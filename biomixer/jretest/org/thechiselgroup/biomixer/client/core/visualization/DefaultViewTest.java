/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.visualization;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.ui.Presenter;
import org.thechiselgroup.biomixer.client.core.ui.SidePanelSection;
import org.thechiselgroup.biomixer.client.core.ui.widget.listbox.ListBoxControl;
import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.core.util.DisposeUtil;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualizationModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.SelectionModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedSlotMappingConfiguration;
import org.thechiselgroup.biomixer.client.core.visualization.model.persistence.ManagedSlotMappingConfigurationPersistence;
import org.thechiselgroup.biomixer.client.core.visualization.ui.VisualMappingsControl;

public class DefaultViewTest {

    public static interface DisposableVisualizationModel extends
            VisualizationModel, Disposable {
    }

    private DefaultView underTest;

    @Mock
    private DisposableVisualizationModel viewModel;

    @Mock
    private Presenter resourceModelPresenter;

    @Mock
    private Presenter selectionModelPresenter;

    @Test
    public void disposeResourceModelPresenter() {
        underTest.dispose();

        verify(resourceModelPresenter, times(1)).dispose();
    }

    @Test
    public void disposeSelectionModelPresenter() {
        underTest.dispose();

        verify(selectionModelPresenter, times(1)).dispose();
    }

    @Test
    public void disposeViewModel() {
        underTest.dispose();

        verify(viewModel, times(1)).dispose();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        ArrayList<ConfigurationBarExtension> extensions = new ArrayList<ConfigurationBarExtension>();
        extensions.add(new PresenterLeftConfigurationBarExtension(
                resourceModelPresenter));
        extensions.add(new PresenterInCenterRightConfigurationBarExtension(
                selectionModelPresenter));

        underTest = new DefaultView(mock(ViewContentDisplay.class), "label",
                "contentType", extensions, mock(VisualMappingsControl.class),
                LightweightCollections.<SidePanelSection> emptyCollection(),
                viewModel, mock(ResourceModel.class),
                mock(SelectionModel.class),
                mock(ManagedSlotMappingConfiguration.class),
                mock(ManagedSlotMappingConfigurationPersistence.class),
                new DisposeUtil(mock(ErrorHandler.class)),
                mock(ListBoxControl.class));
    }
}