package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

import com.google.gwt.user.client.Window;

public abstract class NodeSizeTransformer implements
        Transformer<SizeDouble, SizeDouble> {

    double MAX_ON_SCREEN_SIZE = 300;

    double MIN_ON_SCREEN_SIZE = 10;

    protected double minRawSize = -1;

    protected double maxRawSize = -1;

    protected double rangeRawSize;

    private Set<AbstractGraphRenderer> graphRenderingListeners = new HashSet();

    protected double scaleForContextRange(double rawValue) {

        if (rawValue > maxRawSize || maxRawSize == -1) {
            setScalingContextRange(this.minRawSize, rawValue);

        }
        if (rawValue < minRawSize || minRawSize == -1) {
            setScalingContextRange(rawValue, this.maxRawSize);
        }

        double factor = 1.0 - ((this.maxRawSize - rawValue) / this.rangeRawSize);
        Window.alert("Factor is " + factor + " for raw size " + rawValue);
        return MIN_ON_SCREEN_SIZE + factor
                * (MAX_ON_SCREEN_SIZE - MIN_ON_SCREEN_SIZE) / this.rangeRawSize;
    }

    /**
     * When nodes are known in advance of adding them, this can be used to avoid
     * multiple update calls. Setting the values this way short circuits the
     * internal automatic setting of bounds.
     * 
     * @param minRawSize
     * @param maxRawSize
     */
    public void setScalingContextRange(double minRawSize, double maxRawSize) {
        boolean informListeners = false;
        if (this.minRawSize != minRawSize || this.maxRawSize != maxRawSize) {
            informListeners = true;
        }
        this.minRawSize = minRawSize;
        this.maxRawSize = maxRawSize;
        this.rangeRawSize = Math.max(1, maxRawSize - minRawSize);
        if (informListeners) {
            informListeners();
        }
    }

    private void informListeners() {
        for (AbstractGraphRenderer listener : graphRenderingListeners) {
            listener.refreshAllNodeSizes();
        }
    }

    public boolean addingScalingContextRange(Node changedNode) {
        int newSize = changedNode.getSize();
        boolean changed = false;
        if (newSize > maxRawSize || maxRawSize == -1) {
            maxRawSize = newSize;
            changed = true;
        }
        if (newSize < this.minRawSize || minRawSize == -1) {
            this.minRawSize = newSize;
            changed = true;
        }
        setScalingContextRange(this.minRawSize, this.maxRawSize);

        return changed;
    }

    public boolean removingScalingContextRange(Node changedNode,
            TreeSet<Node> sortedNodes) {
        int newSize = changedNode.getSize();
        boolean changed = false;
        if (newSize < maxRawSize || newSize > minRawSize && maxRawSize != -1
                && minRawSize != -1) {
            changed = false;
        } else {
            // Recompute bounds
            this.minRawSize = sortedNodes.first().getSize();
            this.maxRawSize = sortedNodes.last().getSize();
            changed = true;
        }
        setScalingContextRange(this.minRawSize, this.maxRawSize);

        return changed;
    }

    public void addGraphRenderingListener(
            AbstractGraphRenderer abstractGraphRenderer) {
        graphRenderingListeners.add(abstractGraphRenderer);
    }
}
