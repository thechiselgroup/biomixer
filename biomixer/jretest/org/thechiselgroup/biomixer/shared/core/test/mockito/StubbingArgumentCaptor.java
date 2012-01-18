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
package org.thechiselgroup.biomixer.shared.core.test.mockito;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class StubbingArgumentCaptor<T> implements Answer<T> {

    private Object[] arguments;

    @Override
    public T answer(InvocationOnMock invocation) {
        arguments = invocation.getArguments();
        return null;
    }

    public <S> S getAs(Class<S> clazz, int index) {
        return clazz.cast(arguments[index]);
    }

    public boolean wasCalled() {
        return arguments != null;
    }
}