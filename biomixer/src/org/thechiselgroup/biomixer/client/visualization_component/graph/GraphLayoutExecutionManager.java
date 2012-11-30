/*******************************************************************************
 * Copyright 2012 David Rusk 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputationFinishedEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputationFinishedHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;

/**
 * Manages the repeated execution of a {@link LayoutAlgorithm} on a particular
 * {@link LayoutGraph}.
 * 
 * @author drusk
 * 
 */
public class GraphLayoutExecutionManager {

    private LayoutAlgorithm layoutAlgorithm;

    private LayoutComputation currentComputation = null;

    private LayoutGraph graph;

    /**
     * Handlers for the next computation to be created.
     */
    private List<LayoutComputationFinishedHandler> pendingHandlers = new ArrayList<LayoutComputationFinishedHandler>();

    public GraphLayoutExecutionManager(LayoutAlgorithm layoutAlgorithm,
            LayoutGraph graph) {
        this.layoutAlgorithm = layoutAlgorithm;
        this.graph = graph;
    }

    public void addLayoutComputationFinishedHandler(
            LayoutComputationFinishedHandler handler) {
        if (currentComputation != null) {
            currentComputation.addEventHandler(handler);
        } else {
            pendingHandlers.add(handler);
        }
    }

    private void computeLayout() {
        currentComputation = layoutAlgorithm.computeLayout(graph,
                pendingHandlers);
        pendingHandlers.clear();
    }

    /**
     * Registers a new layout algorithm to be the default for the graph. Stops
     * any current computations then runs this new layout algorithm.
     * 
     * @param newLayoutAlgorithm
     *            the new layout algorithm to be run
     */
    public void registerAndRunLayoutAlgorithm(
            final LayoutAlgorithm newLayoutAlgorithm) {
        if (!layoutAlgorithm.equals(newLayoutAlgorithm)) {
            /*
             * The new layout algorithm takes over
             */
            if (currentComputation.isRunning()) {
                stopCurrentComputationAndScheduleNewLayout(newLayoutAlgorithm);
            } else {
                replaceAlgorithm(newLayoutAlgorithm);
                runLayout();
            }
        } else {
            /*
             * This layout is the one already registered
             */
            runLayout();
        }
    }

    /**
     * Stop any calculations and set a new layout algorithm. Does not run it
     * immediately.
     * 
     * @param newLayoutAlgorithm
     *            the layout algorithm to be run in the future
     */
    public void registerDefaultAlgorithm(
            final LayoutAlgorithm newLayoutAlgorithm) {
        if (currentComputation == null || !currentComputation.isRunning()) {
            replaceAlgorithm(newLayoutAlgorithm);
        } else {
            currentComputation
                    .addEventHandler(new LayoutComputationFinishedHandler() {
                        @Override
                        public void onLayoutComputationFinished(
                                LayoutComputationFinishedEvent e) {
                            replaceAlgorithm(newLayoutAlgorithm);
                        }
                    });
            currentComputation.stop();
        }
    }

    private void replaceAlgorithm(LayoutAlgorithm newLayoutAlgorithm) {
        layoutAlgorithm = newLayoutAlgorithm;
        currentComputation = null;
    }

    /**
     * Runs the layout algorithm currently registered for the graph. Layout
     * requests that come in before the previous finishes are ignored. This is
     * because with each iteration the computation is expected to check for any
     * changes on the graph and take them into account.
     */
    public void runLayout() {
        if (currentComputation == null) {
            /*
             * Run a brand new computation.
             */
            computeLayout();

        } else if (!currentComputation.isRunning()) {
            /*
             * Restart with previous state
             */
            currentComputation.restart();
        }

        /*
         * Otherwise, take no action.
         */
    }

    /**
     * Tell current computation to stop. When it finishes, set the new layout
     * algorithm to be the main one, and run the new layout.
     */
    private void stopCurrentComputationAndScheduleNewLayout(
            final LayoutAlgorithm newLayoutAlgorithm) {
        currentComputation
                .addEventHandler(new LayoutComputationFinishedHandler() {

                    @Override
                    public void onLayoutComputationFinished(
                            LayoutComputationFinishedEvent e) {

                        replaceAlgorithm(newLayoutAlgorithm);
                        runLayout();
                    }
                });

        currentComputation.stop();
    }
}
