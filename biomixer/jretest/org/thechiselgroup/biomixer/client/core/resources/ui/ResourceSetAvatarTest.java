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
package org.thechiselgroup.biomixer.client.core.resources.ui;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.util.Disposable;

import com.google.gwt.user.client.Element;

@Ignore("ignored because GWT 2.1 update introduced DOM access in constructor")
public class ResourceSetAvatarTest {

    private static class TestResourceSetAvatar extends ResourceSetAvatar {

        private TestResourceSetAvatar(String text, String enabledCSSClass,
                ResourceSet resources, ResourceSetAvatarType type) {

            super(text, enabledCSSClass, resources, type, new Element() {
            });
        }

        @Override
        public void addStyleName(String style) {
        }

        @Override
        public void removeStyleName(String style) {
        }

        @Override
        public void setText(String text) {
        }
    }

    private ResourceSetAvatar dragAvatar;

    @Mock
    private ResourceSet resources;

    @Test
    public void canCallDisposeMultipleTimesWithoutException() {
        dragAvatar.dispose();
        dragAvatar.dispose();
        dragAvatar.dispose();
    }

    @Test
    public void disposeCallsDisposeOnAddedDisposable() {
        Disposable disposable = mock(Disposable.class);

        dragAvatar.addDisposable(disposable);
        dragAvatar.dispose();

        verify(disposable, times(1)).dispose();
    }

    @Test
    public void noDoubleStyleWhenHoverCalledTwiceWithFalse() {
        dragAvatar.setHover(true);
        dragAvatar.setHover(false);
        dragAvatar.setHover(false);

        verify(dragAvatar, times(1)).removeStyleName(
                eq(ResourceSetAvatar.CSS_HOVER));
        verify(dragAvatar, times(1)).addStyleName(
                eq(dragAvatar.getEnabledCSSClass()));
    }

    @Test
    public void noDoubleStyleWhenHoverCalledTwiceWithTrue() {
        dragAvatar.setHover(true);
        dragAvatar.setHover(true);

        verify(dragAvatar, times(1)).addStyleName(
                eq(ResourceSetAvatar.CSS_HOVER));
        verify(dragAvatar, times(1)).removeStyleName(
                eq(dragAvatar.getEnabledCSSClass()));
    }

    @Test
    public void setHoverSwitchesToHoverClass() {
        dragAvatar.setHover(true);

        verify(dragAvatar, times(1)).addStyleName(
                eq(ResourceSetAvatar.CSS_HOVER));
        verify(dragAvatar, times(1)).removeStyleName(
                eq(dragAvatar.getEnabledCSSClass()));
    }

    @Test
    public void setToEnabledWhenHoverSwitchedOff() {
        dragAvatar.setHover(true);
        dragAvatar.setHover(false);

        verify(dragAvatar, times(1)).removeStyleName(
                eq(ResourceSetAvatar.CSS_HOVER));
        verify(dragAvatar, times(1)).addStyleName(
                eq(dragAvatar.getEnabledCSSClass()));
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        dragAvatar = spy(new TestResourceSetAvatar("text", "enabledCSSClass",
                resources, ResourceSetAvatarType.SET));
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }
}
