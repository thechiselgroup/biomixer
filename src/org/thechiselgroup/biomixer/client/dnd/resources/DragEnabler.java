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
package org.thechiselgroup.biomixer.client.dnd.resources;

import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarType;
import org.thechiselgroup.biomixer.client.core.ui.CSS;
import org.thechiselgroup.biomixer.client.core.ui.ZIndex;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction.Type;
import org.thechiselgroup.biomixer.client.dnd.DragProxyDetachedEvent;
import org.thechiselgroup.biomixer.client.dnd.DragProxyDetachedEventHandler;
import org.thechiselgroup.biomixer.client.dnd.windows.Desktop;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class DragEnabler {

    private static class InvisibleResourceSetAvatar extends
            DraggableResourceSetAvatar {

        private final VisualItem item;

        private String text;

        public InvisibleResourceSetAvatar(final VisualItem item, String text,
                String enabledCSSClass, ResourceSetAvatarType type,
                Element element, final DragEnabler dragEnabler) {

            super(text, enabledCSSClass, item.getResources(), type, element);

            this.text = text;
            this.item = item;

            addHandler(new DragProxyDetachedEventHandler() {
                @Override
                public void onDragProxyDetached(DragProxyDetachedEvent event) {
                    item.reportInteraction(new VisualItemInteraction(
                            Type.DRAG_END));
                    dragEnabler.removeAvatar();
                }
            }, DragProxyDetachedEvent.TYPE);
        }

        @Override
        public void addStyleName(String style) {
        }

        @Override
        public ResourceSetAvatar createProxy() {
            item.reportInteraction(new VisualItemInteraction(Type.DRAG_START));
            return super.createProxy();
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public void setText(String text) {
        }
    }

    private Desktop desktop;

    private ResourceSetAvatarDragController dragController;

    private VisualItem visualItem;

    /**
     * Invisible {@link ResourceSetAvatar} that receives events.
     */
    private ResourceSetAvatar hiddenAvatar;

    public DragEnabler(VisualItem item, Desktop desktop,
            ResourceSetAvatarDragController dragController) {

        assert item != null;

        this.desktop = desktop;
        this.dragController = dragController;
        this.visualItem = item;
    }

    private void createDragWidget(int absoluteLeft, int absoluteTop) {
        assert hiddenAvatar == null : "hidden avatar was not null "
                + hiddenAvatar;

        // add to desktop - need widget container
        // FIXME use dependency injection
        AbsolutePanel desktopWidget = desktop.asWidget();

        int left = absoluteLeft - desktopWidget.getAbsoluteLeft();
        int top = absoluteTop - desktopWidget.getAbsoluteTop();

        Element span = DOM.createSpan();

        CSS.setPosition(span, CSS.ABSOLUTE);
        CSS.setLocation(span, left, top);
        CSS.setZIndex(span, -1);

        /*
         * XXX using the view item ID as text is problematic, because it is not
         * a good description if there is no aggregation.
         */
        final String text = visualItem.getId();
        hiddenAvatar = new InvisibleResourceSetAvatar(visualItem, text,
                "avatar-resourceSet", ResourceSetAvatarType.SET, span, this);

        span.setClassName("avatar-invisible");

        desktopWidget.add(hiddenAvatar);

        dragController.setDraggable(hiddenAvatar, true);
    }

    public void createTransparentDragProxy(final int absoluteLeft,
            final int absoluteTop) {
        createDragWidget(absoluteLeft, absoluteTop);

        // TODO remove code duplication
        AbsolutePanel desktopWidget = desktop.asWidget();
        int left = absoluteLeft - desktopWidget.getAbsoluteLeft() - 5;
        int top = absoluteTop - desktopWidget.getAbsoluteTop() - 5;

        Element element = hiddenAvatar.getElement();

        CSS.setLocation(element, left, top);
        CSS.setSize(element, 10, 10);
        CSS.setZIndex(element, ZIndex.POPUP - 1);

        hiddenAvatar.addMouseUpHandler(new MouseUpHandler() {

            @Override
            public void onMouseUp(MouseUpEvent event) {
                removeAvatar();
            }

        });

        // fake mouse down event on widget
        MouseDownEvent mouseEvent = new MouseDownEvent() {
            @Override
            public int getClientX() {
                return absoluteLeft;
            }

            @Override
            public int getClientY() {
                return absoluteTop;
            }

            @Override
            public int getNativeButton() {
                return NativeEvent.BUTTON_LEFT;
            }

            @Override
            public int getRelativeX(com.google.gwt.dom.client.Element target) {
                return getClientX() - target.getAbsoluteLeft()
                        + target.getScrollLeft()
                        + target.getOwnerDocument().getScrollLeft();
            }

            @Override
            public int getRelativeY(com.google.gwt.dom.client.Element target) {
                return getClientY() - target.getAbsoluteTop()
                        + target.getScrollTop()
                        + target.getOwnerDocument().getScrollTop();
            }

            @Override
            public Object getSource() {
                return hiddenAvatar;
            }

            @Override
            public boolean isControlKeyDown() {
                return false;
            }

            @Override
            public boolean isMetaKeyDown() {
                return false;
            }
        };
        mouseEvent.setRelativeElement(element);
        hiddenAvatar.fireEvent(mouseEvent);
    }

    public void forwardMouseDown(NativeEvent e, int absoluteLeft,
            int absoluteTop) {

        createDragWidget(absoluteLeft, absoluteTop);

        // fake mouse down event on widget
        MouseDownEvent mouseEvent = new MouseDownEvent() {
            @Override
            public Object getSource() {
                return hiddenAvatar;
            }
        };

        mouseEvent.setRelativeElement(hiddenAvatar.getElement());
        mouseEvent.setNativeEvent(e);

        hiddenAvatar.fireEvent(mouseEvent);
    }

    public void forwardMouseDownWithEventPosition(NativeEvent e) {
        forwardMouseDown(e, e.getClientX(), e.getClientY());
    }

    public void forwardMouseDownWithTargetElementPosition(NativeEvent e) {
        Element element = e.getCurrentEventTarget().cast();
        int absoluteLeft = element.getAbsoluteLeft();
        int absoluteTop = element.getAbsoluteTop();

        forwardMouseDown(e, absoluteLeft, absoluteTop);
    }

    public void forwardMouseMove(final int clientX, final int clientY) {
        if (hiddenAvatar == null) {
            return;
        }

        MouseMoveEvent mouseEvent = new MouseMoveEvent() {
            @Override
            public int getClientX() {
                return clientX;
            }

            @Override
            public int getClientY() {
                return clientY;
            }

            @Override
            public int getRelativeX(com.google.gwt.dom.client.Element target) {
                return getClientX() - target.getAbsoluteLeft()
                        + target.getScrollLeft()
                        + target.getOwnerDocument().getScrollLeft();
            }

            @Override
            public int getRelativeY(com.google.gwt.dom.client.Element target) {
                return getClientY() - target.getAbsoluteTop()
                        + target.getScrollTop()
                        + target.getOwnerDocument().getScrollTop();
            }

            @Override
            public Object getSource() {
                return hiddenAvatar;
            }

        };
        mouseEvent.setRelativeElement(hiddenAvatar.getElement());
        hiddenAvatar.fireEvent(mouseEvent);
    }

    public void forwardMouseMove(NativeEvent e) {
        if (hiddenAvatar == null) {
            return;
        }

        MouseMoveEvent mouseEvent = new MouseMoveEvent() {
            @Override
            public Object getSource() {
                return hiddenAvatar;
            }
        };
        mouseEvent.setRelativeElement(hiddenAvatar.getElement());
        mouseEvent.setNativeEvent(e);
        hiddenAvatar.fireEvent(mouseEvent);
    }

    public void forwardMouseOut(NativeEvent e) {
        if (hiddenAvatar == null) {
            return;
        }

        MouseOutEvent mouseEvent = new MouseOutEvent() {
            @Override
            public Object getSource() {
                return hiddenAvatar;
            }
        };
        mouseEvent.setRelativeElement(hiddenAvatar.getElement());
        mouseEvent.setNativeEvent(e);
        hiddenAvatar.fireEvent(mouseEvent);
    }

    public void forwardMouseUp(NativeEvent e) {
        if (hiddenAvatar == null) {
            return;
        }

        MouseUpEvent mouseEvent = new MouseUpEvent() {
            @Override
            public Object getSource() {
                return hiddenAvatar;
            }
        };
        mouseEvent.setRelativeElement(hiddenAvatar.getElement());
        mouseEvent.setNativeEvent(e);
        hiddenAvatar.fireEvent(mouseEvent);

        removeAvatar();
    }

    public void onMoveInteraction(VisualItemInteraction interaction) {
        if (interaction.hasNativeEvent()) {
            forwardMouseMove(interaction.getNativeEvent());
        } else {
            forwardMouseMove(interaction.getClientX(), interaction.getClientY());
        }
    }

    private void removeAvatar() {
        if (hiddenAvatar != null) {
            hiddenAvatar.removeFromParent();
            hiddenAvatar = null;
        }
    }
}
