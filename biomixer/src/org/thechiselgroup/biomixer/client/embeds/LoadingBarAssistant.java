package org.thechiselgroup.biomixer.client.embeds;

import org.thechiselgroup.biomixer.client.core.visualization.LeftViewTopBarExtension;
import org.thechiselgroup.biomixer.client.core.visualization.View;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

public class LoadingBarAssistant {

    static final private String ELEMENT_ID = "loadingMessage";

    static final private String BAR_IMAGE_PATH = "images/ajax-loader-bar.gif";

    public void initialize(View view) {
        Image loadingMessage = new Image(GWT.getModuleBaseURL()
                + BAR_IMAGE_PATH);
        LeftViewTopBarExtension leftViewTopBarExtension = new LeftViewTopBarExtension(
                loadingMessage);
        view.addTopBarExtension(leftViewTopBarExtension);
        loadingMessage.getElement().setId(ELEMENT_ID);
    }

    public void hide() {
        // In Dev mode, things don't work for this.
        // http://turbomanage.wordpress.com/2010/01/12/gwt-layout-gotcha/
        // http://stackoverflow.com/questions/6183181/how-to-add-a-custom-widget-to-an-element
        try {
            RootPanel rootPanel = RootPanel.get(ELEMENT_ID);
            rootPanel.setVisible(false);
        } catch (Throwable t) {
            // If this fails, we could be in dev mode, or there's
            // nothing to do about it anyway.
        }
    }

}
