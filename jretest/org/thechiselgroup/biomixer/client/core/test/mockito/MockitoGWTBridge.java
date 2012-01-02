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
package org.thechiselgroup.biomixer.client.core.test.mockito;

import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWTBridge;
import com.google.gwt.dev.About;

/**
 * This is an exact copy of com.google.gwt.junit.GWTDummyBridge except it
 * returns mocked Widgets instead of null's - also look at
 * com.google.gwt.junit.GWTMockUtilities for static methods.
 * 
 * Orginal code from: http://www.assertinteresting.com/2009/05/unit-testing-gwt/
 * 
 **/
public class MockitoGWTBridge extends GWTBridge {

    private static void setGwtBridge(GWTBridge bridge) {
        Class<GWT> gwtClass = GWT.class;
        Class<?>[] paramTypes = new Class[] { GWTBridge.class };
        Method setBridgeMethod = null;
        try {
            setBridgeMethod = gwtClass.getDeclaredMethod("setBridge",
                    paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        setBridgeMethod.setAccessible(true);
        try {
            setBridgeMethod.invoke(gwtClass, new Object[] { bridge });
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static MockitoGWTBridge setUp() {
        MockitoGWTBridge bridge = new MockitoGWTBridge();
        setGwtBridge(bridge);
        return bridge;
    }

    public static void tearDown() {
        setGwtBridge(null);
    }

    private Map<Class<?>, List<Object>> createdMocks = new HashMap<Class<?>, List<Object>>();

    /**
     * @return Mock
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(Class<?> classLiteral) {
        Object mock = mock(classLiteral);
        saveMock(classLiteral, mock);
        return (T) mock;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCreatedMock(Class<T> classLiteral) {
        List<Object> list = getMockList(classLiteral);
        return (T) (list.isEmpty() ? null : list.get(0));

    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getCreatedMocks(Class<T> classLiteral) {
        return (List<T>) getMockList(classLiteral);
    }

    private List<Object> getMockList(Class<?> classLiteral) {
        if (!createdMocks.containsKey(classLiteral)) {
            createdMocks.put(classLiteral, new ArrayList<Object>());
        }

        List<Object> list = createdMocks.get(classLiteral);
        return list;
    }

    /**
     * @return the current version of GWT ({@link About#getGwtVersionNum()})
     */
    @Override
    public String getVersion() {
        return About.getGwtVersionNum();
    }

    /**
     * @return false
     */
    @Override
    public boolean isClient() {
        return false;
    }

    /**
     * Logs the message and throwable to the standard logger, with level
     * {@link Level#SEVERE}.
     */
    @Override
    public void log(String message, Throwable e) {
        System.out.println(message);
        e.printStackTrace();
    }

    private void saveMock(Class<?> classLiteral, Object mock) {
        getMockList(classLiteral).add(mock);
    }
}
