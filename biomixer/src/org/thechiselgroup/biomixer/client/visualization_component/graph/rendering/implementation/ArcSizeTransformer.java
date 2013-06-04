package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.thechiselgroup.biomixer.client.core.util.GwtRecomputedDelayExecutor;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.shared.core.util.ConditionWithDelay;

/**
 * Perhaps this could be merged with the NodeSizeTransformer. The code might not
 * require differences other than constants and whatnot.
 * 
 */
public abstract class ArcSizeTransformer implements Transformer<Double, Double> {

    // 20 * 7 seems too big. Got 20 from other transformers.
    double MAX_ON_SCREEN_SIZE = 5;

    double MIN_ON_SCREEN_SIZE = 0.2;

    protected double minRawSize = -1;

    protected double maxRawSize = -1;

    private Set<AbstractGraphRenderer> graphRenderingListeners = new HashSet();

    /**
     * As the raw value sizes are scaled, if they are beyond the range that is
     * currently known, we have to re-scale the entire graph. This is made more
     * efficient internally.
     * 
     * @param rawValue
     * @return
     */
    protected double scaleForContextRange(double rawValue) {

        if (rawValue > maxRawSize || maxRawSize == -1) {
            setScalingContextRange(this.minRawSize, rawValue);

        }
        if (rawValue < minRawSize || minRawSize == -1) {
            setScalingContextRange(rawValue, this.maxRawSize);
        }

        // Our factor here is linear. Perhaps we want other functions?
        double factor = this.computeFactorOfRange(rawValue);
        // double factor = computeFactorOfMax(rawValue);

        double transformedValue = linearAreaRelativeScaledRangeValue(factor);

        // double transformedValue = linearRelativeScaledRangeValue(factor);
        // double transformedValue = sqRootRelativeScaledRangeValue(factor);
        // double transformedValue = logRelativeScaledRangeValue(factor);
        return transformedValue;
    }

    private double computeFactorOfMax(double rawValue) {
        return rawValue / Math.max(1, this.maxRawSize);
    }

    private double computeFactorOfRange(double rawValue) {
        return 1.0 - (this.maxRawSize - rawValue) / this.computeRangeRawSize();
    }

    private double linearAreaRelativeScaledRangeValue(double factor) {
        double linearArea = Math.PI * Math.pow(MIN_ON_SCREEN_SIZE, 2) + factor
                * Math.PI * Math.pow(MAX_ON_SCREEN_SIZE, 2);
        double diameter = Math.sqrt(linearArea / Math.PI);
        return diameter;
    }

    private double linearRelativeScaledRangeValue(double factor) {
        return MIN_ON_SCREEN_SIZE + factor
                * (MAX_ON_SCREEN_SIZE - MIN_ON_SCREEN_SIZE);
    }

    private double sqRootRelativeScaledRangeValue(double factor) {
        // TODO This is ok, but not great. If the linear one isn't working
        // to satisfaction, try tweaking this one.
        double multiplier = 6;
        return multiplier
                * Math.sqrt(MIN_ON_SCREEN_SIZE + factor
                        * (MAX_ON_SCREEN_SIZE - MIN_ON_SCREEN_SIZE));
    }

    private double logRelativeScaledRangeValue(double factor) {
        // TODO This is ok, but not great. If the linear one isn't working
        // to satisfaction, try tweaking this one.
        double multiplier = 6;
        return multiplier
                * Math.log(MIN_ON_SCREEN_SIZE + factor
                        * (MAX_ON_SCREEN_SIZE - MIN_ON_SCREEN_SIZE)
                        / Math.log(10));
    }

    private double computeRangeRawSize() {
        return Math.max(1, maxRawSize - minRawSize);
    }

    /**
     * When arcs are known in advance of adding them, this can be used to avoid
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
        if (informListeners) {
            informListeners();
        }
    }

    private int REFRESH_LOOP_DELAY_MS = 500;

    private GwtRecomputedDelayExecutor executor = new GwtRecomputedDelayExecutor();

    private ConditionWithDelay condition = new ConditionWithDelay(
            REFRESH_LOOP_DELAY_MS);

    private Runnable arcRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            loopOverArcRefreshListeners();
        }
    };

    /**
     * This should not cause inefficiency due to multiple calls.
     */
    private void informListeners() {
        condition.setRequestTime(System.currentTimeMillis());
        // If the executor hasn't actually run the code yet, we shouldn't
        // be requesting another run of it.
        if (executor.canMakeNewRequest()) {
            // Use same delay as the one for comparing when it was last called.
            executor.execute(arcRefreshRunnable, condition,
                    REFRESH_LOOP_DELAY_MS);
        }

    }

    private void loopOverArcRefreshListeners() {
        for (AbstractGraphRenderer listener : graphRenderingListeners) {
            listener.refreshAllArcSizes();
        }
    }

    public boolean addingScalingContextRange(Arc changedArc) {
        double newSize = changedArc.getSize();
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

    public boolean removingScalingContextRange(Arc changedArc,
            TreeSet<Arc> sortedArcs) {
        double newSize = changedArc.getSize();
        boolean changed = false;
        if (newSize < maxRawSize || newSize > minRawSize && maxRawSize != -1
                && minRawSize != -1) {
            changed = false;
        } else {
            // Recompute bounds
            this.minRawSize = sortedArcs.first().getSize();
            this.maxRawSize = sortedArcs.last().getSize();
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
