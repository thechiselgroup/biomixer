package org.thechiselgroup.biomixer.client.workbench.util.url;

import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Retrieves the content of URLs using a JSONP.
 */
public class JsonpUrlFetchService implements UrlFetchService {
    private static enum Status {
        INITIALIZING, NOT_INITIALIZED, READY
    }

    // @formatter:off
    private static native void _requestURL(String swfID, String url) /*-{
		$doc.getElementById(swfID).call(url, "_flexproxy_callback");
    }-*/;

    
    private Status status = Status.NOT_INITIALIZED;
    
    private final Map<String, List<AsyncCallback<String>>> requests = CollectionFactory
            .createStringMap();
    
    
    private void addUrlCallback(String url, AsyncCallback<String> callback) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void fetchURL(String url, AsyncCallback<String> callback) {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        
    }

    private void requestURL(String url) {
        // TODO Auto-generated method stub
        
    }

}
