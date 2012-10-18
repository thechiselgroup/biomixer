package org.thechiselgroup.biomixer.client.core.util.url;

public interface UrlBuilder {

    /**
     * The port to use when no port should be specified.
     */
    public static final int PORT_UNSPECIFIED = Integer.MIN_VALUE;

    /**
     * Set the hash portion of the location (ex. myAnchor or #myAnchor).
     * 
     * @param hash
     *            the hash
     */
    public abstract UrlBuilder hash(String hash);

    /**
     * Set the host portion of the location (ex. google.com). You can also
     * specify the port in this method (ex. localhost:8888).
     * 
     * @param host
     *            the host
     */
    public abstract UrlBuilder host(String host);

    /**
     * <p>
     * Set a query parameter to a list of values. Each value in the list will be
     * added as its own key/value pair.
     * 
     * <p>
     * <h3>Example Output</h3>
     * <code>?mykey=value0&mykey=value1&mykey=value2</code>
     * </p>
     * 
     * @param key
     *            the key
     * @param values
     *            the list of values
     */
    public abstract UrlBuilder parameter(String key, String... values);

    /**
     * Set the path portion of the location (ex. path/to/file.html).
     * 
     * @param path
     *            the path
     */
    public abstract UrlBuilder path(String path);

    /**
     * Set the port to connect to.
     * 
     * @param port
     *            the port, or {@link #PORT_UNSPECIFIED}
     */
    public abstract UrlBuilder port(int port);

    /**
     * Set the protocol portion of the location (ex. http).
     * 
     * @param protocol
     *            the protocol
     */
    public abstract UrlBuilder protocol(String protocol);

    /**
     * Build the URL and return it as an encoded string.
     * 
     * @return the encoded URL string
     */
    public abstract String toString();

    public abstract UrlBuilder uriParameter(String key, String uriValue);

}