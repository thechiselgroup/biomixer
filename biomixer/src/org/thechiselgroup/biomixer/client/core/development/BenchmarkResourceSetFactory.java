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
package org.thechiselgroup.biomixer.client.core.development;

import java.util.Date;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetUtils;
import org.thechiselgroup.biomixer.client.core.resources.UriList;

import com.google.gwt.user.client.Random;

/**
 * Creates ResourceSets with generated Resources that contain random values.
 * This class should be used to create ResourceSets for testing and benchmarking
 * the client.
 * 
 * @see #createResourceSet(int, ResourceSetFactory)
 * 
 * @author Lars Grammel
 */
public final class BenchmarkResourceSetFactory {

    private static final int MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;

    private static final String BENCHMARK_CLASS = "benchmark";

    public static final String LOCATION_1 = "location_1";

    public static final String LOCATION_2 = "location_2";

    public static final String DATE_1 = "date_1";

    public static final String DATE_2 = "date_2";

    public static final String NUMBER_1 = "number_1";

    public static final String NUMBER_2 = "number_2";

    public static final String NUMBER_3 = "number_3";

    public static final String TEXT_1 = "text_1";

    public static final String TEXT_2 = "text_2";

    public static final String TEXT_3 = "text_3";

    public static final String RESOURCE_1 = "resource_1";

    private static Date createRandomDate(int numberOfDaysIntoPast,
            long timestamp) {

        int intervalLength = numberOfDaysIntoPast * MILLISECONDS_PER_DAY;
        int randomValue = Random.nextInt(intervalLength);
        return new Date(timestamp - randomValue);
    }

    /**
     * Because the Mercator projection used in many maps gets useless for
     * extreme values and these are often not displayed, the latitude values are
     * limited to +/- 85 degrees.
     */
    private static Resource createRandomLocation() {
        Resource locationResource = new Resource();

        int randomLatitudeValue = Random.nextInt(100 * 85 * 2);
        double latitude = (randomLatitudeValue / 100d) - 85;
        locationResource.putValue(ResourceSetUtils.LATITUDE, latitude);

        int randomLongitudeValue = Random.nextInt(100 * 180 * 2);
        double longitude = (randomLongitudeValue / 100d) - 180;
        locationResource.putValue(ResourceSetUtils.LONGITUDE, longitude);

        return locationResource;
    }

    private static Double createRandomNumber(int min, int max) {
        int intervalLength = max - min;
        int randomValue = Random.nextInt(intervalLength);
        return (double) (randomValue + min);
    }

    private static String createRandomText(int numberOfCategories) {
        int randomValue = Random.nextInt(numberOfCategories);
        return "category-" + randomValue;
    }

    private static UriList createRandomUriList(String uriClass,
            int numberOfResources) {

        UriList uriList = new UriList();
        int randomValue = Random.nextInt(numberOfResources);
        uriList.add(uriClass + ":" + randomValue);
        return uriList;
    }

    private static Resource createResource(int index, long timestamp,
            String uriClass, int numberOfResources) {

        Resource resource = new Resource(uriClass + ":" + index);

        resource.putValue(LOCATION_1, createRandomLocation());
        resource.putValue(LOCATION_2, createRandomLocation());
        resource.putValue(DATE_1, createRandomDate(365, timestamp));
        resource.putValue(DATE_2, createRandomDate(180, timestamp));
        resource.putValue(NUMBER_1, createRandomNumber(0, 10000));
        resource.putValue(NUMBER_2, createRandomNumber(0, 100));
        resource.putValue(NUMBER_3, createRandomNumber(-100, 100));
        resource.putValue(TEXT_1, "text" + index);
        resource.putValue(TEXT_2, createRandomText(10));
        resource.putValue(TEXT_3, createRandomText(100));
        resource.putValue(RESOURCE_1,
                createRandomUriList(uriClass, numberOfResources));

        return resource;
    }

    /**
     * Creates a <code>ResourceSet</code> with <code>numberOfResources</code>
     * randomly created resources. To avoid conflicts with other benchmark
     * resource sets, the type contains the current timestamp.The resources have
     * the following properties:
     * <ol>
     * <li><code>LOCATION_1</code> - random location</li>
     * <li><code>LOCATION_2</code> - another random location</li>
     * <li><code>DATE_1</code> - random date within the last 365 days</li>
     * <li><code>DATE_2</code> - random date within the last 180 days</li>
     * <li><code>NUMBER_1</code> - random number between 0 and 10000</li>
     * <li><code>NUMBER_2</code> - random number between 0 and 100</li>
     * <li><code>NUMBER_3</code> - random number between -100 and 100</li>
     * <li><code>TEXT_1</code> - text string unique to resource</li>
     * <li><code>TEXT_2</code> - random text category (10 categories)</li>
     * <li><code>TEXT_3</code> - random text category (100 categories)</li>
     * <li><code>RESOURCE_1</code> - one randomly selected URI of another
     * resource in this set</li>
     * </ol>
     * 
     * @param numberOfResources
     *            number of resources that should be created
     * @param resourceSetFactory
     *            factory that is used for creating the resource set
     * 
     * @return ResourceSet with <code>numberOfResources</code> randomly created
     *         resources. The label of the resource set contains the URI class.
     */
    public static ResourceSet createResourceSet(int numberOfResources,
            ResourceSetFactory resourceSetFactory) {

        ResourceSet resourceSet = resourceSetFactory.createResourceSet();
        long timestamp = System.currentTimeMillis();
        String uriClass = BENCHMARK_CLASS + timestamp;
        resourceSet.setLabel(uriClass);

        for (int i = 0; i < numberOfResources; i++) {
            resourceSet.add(createResource(i, timestamp, uriClass,
                    numberOfResources));
        }

        return resourceSet;
    }

    private BenchmarkResourceSetFactory() {
    }

}
