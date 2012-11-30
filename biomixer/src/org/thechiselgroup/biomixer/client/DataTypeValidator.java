package org.thechiselgroup.biomixer.client;

import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;

public interface DataTypeValidator {

    /**
     * Used to determine if the resource set provided consists of the sort of
     * data required. Useful for data drop processing.
     * 
     * @param resourceSet
     * @return
     */
    boolean validateDataTypes(ResourceSet resourceSet);

}
