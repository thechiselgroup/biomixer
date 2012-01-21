package org.thechiselgroup.biomixer.client.core.ui.widget.listbox;

import java.util.List;

import org.thechiselgroup.biomixer.client.core.error_handling.ThrowableCaught;
import org.thechiselgroup.biomixer.client.core.error_handling.ThrowableCaughtEvent;
import org.thechiselgroup.biomixer.client.core.error_handling.ThrowablesContainerEventListener;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;

public class ErrorListBoxControl extends ListBoxControl<ThrowableCaught>
        implements ThrowablesContainerEventListener {

    public ErrorListBoxControl(ListBoxPresenter presenter,
            Transformer<ThrowableCaught, String> formatter) {
        super(presenter, formatter);
    }

    @Override
    public void onThrowableCaughtAdded(ThrowableCaughtEvent event) {
        List<ThrowableCaught> newValues = getValues();
        newValues.add(event.getThrowableCaught());
        setValues(newValues);
    }

    @Override
    public void onThrowableCaughtRemoved(ThrowableCaughtEvent event) {
        List<ThrowableCaught> newValues = getValues();
        ThrowableCaught throwableCaught = event.getThrowableCaught();
        int indexOfItem = newValues.indexOf(throwableCaught);
        assert indexOfItem >= 0;

        newValues.remove(indexOfItem);
        setValues(newValues);
    }
}
