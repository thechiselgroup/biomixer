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
package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.core.command.AbstractUndoableCommand;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.util.HasDescription;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

public class MoveNodeCommand extends AbstractUndoableCommand implements
        HasDescription {

    private final GraphDisplay graphDisplay;

    private final Node node;

    private final Point sourceLocation;

    private final Point targetLocation;

    /**
     * MoveNodeCommand occurs as a result of user interaction in Flex.
     * 
     * @param graphDisplay
     * @param node
     * @param sourceLocation
     * @param targetLocation
     */
    public MoveNodeCommand(GraphDisplay graphDisplay, Node node,
            Point sourceLocation, Point targetLocation) {
        // this command is a result of user interaction, therefore
        // it does not need to be run by the command manager.
        super(true);

        assert graphDisplay != null;
        assert node != null;
        assert sourceLocation != null;
        assert targetLocation != null;

        this.graphDisplay = graphDisplay;
        this.node = node;
        this.sourceLocation = sourceLocation;
        this.targetLocation = targetLocation;
    }

    @Override
    public String getDescription() {
        return "Move node '" + node.getLabel() + "' to " + targetLocation;
    }

    public GraphDisplay getGraphDisplay() {
        return graphDisplay;
    }

    public Node getNode() {
        return node;
    }

    public Point getSourceLocation() {
        return sourceLocation;
    }

    public Point getTargetLocation() {
        return targetLocation;
    }

    @Override
    public void performExecute() {
        graphDisplay.animateMoveTo(node, targetLocation);
    }

    @Override
    public void performUndo() {
        graphDisplay.animateMoveTo(node, sourceLocation);
    }

}
