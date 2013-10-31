package org.thechiselgroup.biomixer.client.utils;

import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.query.client.GQuery;

/**
 * Use the power of Javascript to decode encoded HTML to text (which will then
 * get rendered correctly as its HTML character). If you see escape sequences,
 * try this.
 * 
 * It was originally made as a hack to fully decode concept labels containing
 * HTML encoded characters. Some symbols (e.g. |) were showing up in the LOINC
 * ontology. JQuery decodes things well, so we use an element to do the dirty
 * work.
 * 
 */
public class HtmlDecoder {
    private static final GQuery DECODING_HACK;

    static {

        DECODING_HACK = $("<span/>").attr("name", "html_decode_hack");

    }

    static public String decode(String htmlEncodedString) {
        return DECODING_HACK.html(htmlEncodedString).text();
    }
}
