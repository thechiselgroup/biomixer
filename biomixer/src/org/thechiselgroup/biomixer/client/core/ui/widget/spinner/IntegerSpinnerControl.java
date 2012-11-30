package org.thechiselgroup.biomixer.client.core.ui.widget.spinner;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.ui.widget.listbox.VisibilityChangeEvent;
import org.thechiselgroup.biomixer.client.core.ui.widget.listbox.VisibilityChangeHandler;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * TODO This class is entirely incomplete. It is based off of the ListBoxControl, so see that for further advice.
 * 
 * @author everbeek
 * 
 */
public class IntegerSpinnerControl implements IsWidget {

    private final SpinnerControlPresenter presenter;

    /**
     * TODO: clearly define behaviour.
     * 
     * This change handler is only for when a user selects something, not when
     * content changes.
     */
    private ChangeHandler changeHandler;

    private HandlerRegistration changeHandlerRegistration;

    @SuppressWarnings("unused")
    private ErrorHandler errorHandler;

    private List<VisibilityChangeHandler> visibilityChangeHandlers = new ArrayList<VisibilityChangeHandler>();

    // private final Transformer<Long, String> formatter;

    // TODO refactor: use handler registration / deregistration for
    // changeHandler
    public IntegerSpinnerControl(SpinnerControlPresenter presenter,
            ErrorHandler errorHandler) {
        // this(presenter, formatter, errorHandler, 20);

        this(presenter, errorHandler, 20);
    }

    public IntegerSpinnerControl(SpinnerControlPresenter presenter,
            ErrorHandler errorHandler, long initialValue) {
        // assert formatter != null;
        assert presenter != null;
        assert errorHandler != null;
        // assert initialValue != null;

        // this.formatter = formatter;
        this.presenter = presenter;
        this.errorHandler = errorHandler;
        this.presenter.setValue(initialValue);

    }

    @Override
    public Widget asWidget() {
        return presenter.asWidget();
    }

    private void fireVisibilityChangeEvent(
            VisibilityChangeEvent visibilityChangeEvent) {
        for (VisibilityChangeHandler handler : visibilityChangeHandlers) {
            handler.onVisibilityChange(visibilityChangeEvent);
        }

    }

    public long getValue() {
        return this.presenter.getValue();
    }

    public void setValue(long t) {
        // XXX what if value is not part of values?
        presenter.setValue(t);
    }

    public boolean isVisible() {
        return presenter.isVisible();
    }

    public void registerVisibilityChangeHandler(VisibilityChangeHandler handler) {
        visibilityChangeHandlers.add(handler);
    }

    // TODO this should be changed to addChangeHandler (this is a memory bug)
    // TODO allow for multiple change handlers
    public void setChangeHandler(ChangeHandler changeHandler) {
        assert changeHandler != null;

        this.changeHandler = changeHandler;
        changeHandlerRegistration = presenter.addChangeHandler(changeHandler);
    }

    public void setVisible(boolean visible) {
        presenter.setVisible(visible);
        fireVisibilityChangeEvent(new VisibilityChangeEvent(visible, this));
    }
}
