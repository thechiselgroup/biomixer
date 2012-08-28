package org.thechiselgroup.biomixer.client.core.util.animation;

public interface Animation {

    void cancel();

    void onUpdate(double progress);

    void run(int duration);

}
