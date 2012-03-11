package org.thechiselgroup.biomixer.client.core.visualization;

import org.thechiselgroup.biomixer.client.core.ui.Presenter;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

public class PresenterInCenterRightConfigurationBarExtension implements ConfigurationBarExtension {

    private final Presenter presenter;

    public PresenterInCenterRightConfigurationBarExtension(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void dispose() {
        presenter.dispose();
    }

    @Override
    public void init(DockPanel configurationBar) {
        presenter.init();

        Widget widget = presenter.asWidget();
        configurationBar.add(widget, DockPanel.CENTER);
        configurationBar.setCellHorizontalAlignment(widget,
                HasAlignment.ALIGN_RIGHT);
        configurationBar.setCellWidth(widget, "100%"); // eats up all
    }
}