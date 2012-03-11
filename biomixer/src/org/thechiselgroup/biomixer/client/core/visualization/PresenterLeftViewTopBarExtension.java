package org.thechiselgroup.biomixer.client.core.visualization;

import org.thechiselgroup.biomixer.client.core.ui.Presenter;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

public class PresenterLeftViewTopBarExtension implements ViewTopBarExtension {

    private final Presenter presenter;

    public PresenterLeftViewTopBarExtension(Presenter presenter) {
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

        configurationBar.add(widget, DockPanel.WEST);
        configurationBar.setCellHorizontalAlignment(widget,
                HasAlignment.ALIGN_LEFT);
    }
}