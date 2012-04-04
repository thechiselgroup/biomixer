package org.thechiselgroup.biomixer.client.workbench.util.url;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;

import pl.rmalinowski.gwt2swf.client.ui.SWFWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Retrieves the content of URLs using a invisible Flash widget.
 */
public class JsonpUrlFetchService extends SWFWidget implements UrlFetchService {

    private static enum Status {
        INITIALIZING, NOT_INITIALIZED, READY
    }

    public static final String SWF_FILE = GWT.getModuleBaseURL()
            + "swf/FlexProxy.swf";

    private static Map<String, JsonpUrlFetchService> widgets = CollectionFactory
            .createStringMap();

    static {
        try {
            exportStaticMethods();
        } catch (Exception ex) {
            // XXX use initializables & dependency injection
            Logger.getLogger("").log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public static void _callback(String swfID, String content, String url,
            String error) {
        widgets.get(swfID).callback(content, url, error);
    }

    public static void _onLoad(String swfID) {
        try {
            widgets.get(swfID).onWidgetReady();
        } catch (Exception ex) {
            // XXX use initializables & dependency injection
            Logger.getLogger("").log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    // @formatter:off
    private static native void _requestURL(String swfID, String url) /*-{
		$doc.getElementById(swfID).call(url, "_flexproxy_callback");
    }-*/;

    private static native void exportStaticMethods() /*-{
        $wnd._flexproxy_loaded=$entry(
        @org.thechiselgroup.biomixer.client.workbench.util.url.FlashUrlFetchService::_onLoad(Ljava/lang/String;));
        $wnd._flexproxy_callback=$entry(
        @org.thechiselgroup.biomixer.client.workbench.util.url.FlashUrlFetchService::_callback(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;));
    }-*/;
    // @formatter:on

    private final Map<String, List<AsyncCallback<String>>> requests = CollectionFactory
            .createStringMap();

    private Status status = Status.NOT_INITIALIZED;

    public static final String FLASH_VAR_SWFID = "swfid";

    public JsonpUrlFetchService() {
        super(SWF_FILE, 1, 1);

        addAttribute("wmode", "transparent");

        // TODO extract superclass
        // hack around IE / FF differences with Flash embedding
        addFlashVar(FLASH_VAR_SWFID, getSwfId());
    }

    private void addUrlCallback(String url, AsyncCallback<String> callback) {
        if (!requests.containsKey(url)) {
            requests.put(url, new ArrayList<AsyncCallback<String>>());
        }

        requests.get(url).add(callback);
    }

    private void callback(String content, String url, String error) {
        assert requests.containsKey(url) : "no callback for URL '" + url
                + "' registered";
        List<AsyncCallback<String>> callbacks = requests.remove(url);

        if (error == null) {
            for (AsyncCallback<String> callback : callbacks) {
                callback.onSuccess(content);
            }
        } else {
            for (AsyncCallback<String> callback : callbacks) {
                callback.onFailure(new IOException("Could not retrieve URL '"
                        + url + "' (" + error + ")"));
            }
        }
    }

    @Override
    public void fetchURL(String url, AsyncCallback<String> callback) {
        addUrlCallback(url, callback);

        switch (status) {
        case NOT_INITIALIZED: {
            init();
        }
            break;
        case INITIALIZING: {
        }
            break;
        case READY: {
            if (requests.get(url).size() == 1) {
                requestURL(url);
            }
        }
            break;
        }
    }

    private void init() {
        RootPanel.get().add(this);
        status = Status.INITIALIZING;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        JsonpUrlFetchService.widgets.put(getSwfId(), this);
    }

    @Override
    protected void onUnload() {
        JsonpUrlFetchService.widgets.remove(getSwfId());
        super.onUnload();
    }

    public void onWidgetReady() {
        status = Status.READY;

        for (String url : requests.keySet()) {
            requestURL(url);
        }
    }

    private void requestURL(String url) {
        _requestURL(getSwfId(), url);
    }
}
