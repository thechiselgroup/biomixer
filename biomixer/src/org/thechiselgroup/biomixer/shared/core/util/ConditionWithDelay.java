package org.thechiselgroup.biomixer.shared.core.util;

/**
 * This class allows for conditionalizing on a time span having passed, and
 * providing this to a schedulable task that can be re-tried after the
 * difference in time has passed.
 * 
 */
public class ConditionWithDelay implements Condition {

    private int delayDuration = 500;

    private long lastRequestTime = Long.MIN_VALUE;

    public ConditionWithDelay() {

    }

    public ConditionWithDelay(int delayDuration) {
        this.delayDuration = delayDuration;
    }

    @Override
    public boolean isMet() {
        return getDelay() > delayDuration;
    }

    public int getDelay() {
        return (int) (System.currentTimeMillis() - lastRequestTime);
    }

    public long getPredictedConditionMetTime() {
        return lastRequestTime + delayDuration;
    }

    public void setRequestTime(long newRequestTime) {
        this.lastRequestTime = newRequestTime;
    }

}
