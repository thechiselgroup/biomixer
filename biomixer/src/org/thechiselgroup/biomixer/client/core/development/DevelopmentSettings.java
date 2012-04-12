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

import com.google.gwt.core.client.GWT;

/**
 * Contains development flags (e.g. to turn benchmarking on and off).
 * 
 * @author Lars Grammel
 */
public final class DevelopmentSettings {

    /**
     * Flag that activates / deactivates benchmarking. Change to enable
     * benchmarking mode.
     * 
     * @see #isBenchmarkEnabled()
     */
    private final static boolean BENCHMARKING = false;

    /**
     * Flag that activates development mode. In development mode, certain
     * debugging features (e.g. having the browser's context menu available) are
     * active.
     */
    private final static boolean DEVELOPMENT = true;

    /**
     * Returns if benchmarking is active or not. The benchmarking is only active
     * in GWT clients (i.e. not in test environments and if the
     * {@link #BENCHMARKING} flag is set). To enable automatic removal in the
     * JavaScript compilation of GWT if benchmarking is disabled, use as
     * follows:
     * 
     * <pre>
     * if (Benchmark.isBenchmarkEnabled) {
     *     com.google.gwt.core.client.Duration duration = new com.google.gwt.core.client.Duration();
     *     // run your code (should be one method call to prevent
     *     // code duplication)
     *     com.google.gwt.core.client.Window.alert(&quot;execution time: &quot;
     *             + duration.elapsedMillis() + &quot; ms&quot;);
     * } else {
     *     // run your code (should be one method call to prevent
     *     // code duplication)
     * }
     * </pre>
     * 
     * #see {@link #BENCHMARKING}
     */
    @SuppressWarnings("unused")
    public static boolean isBenchmarkEnabled() {
        return BENCHMARKING && GWT.isClient();
    }

    public static boolean isInDevelopmentMode() {
        return DEVELOPMENT || !GWT.isScript();
    }

    /**
     * Determines whether JavaScript or Java implementations should be used. We
     * do not use JavaScript code in OOMPH client mode for performance reasons.
     */
    public static boolean shouldUseJavaScriptImplementation() {
        return GWT.isScript(); // add "|| GWT.isClient()" to test in hosted mode
    }

    private DevelopmentSettings() {
    }

}