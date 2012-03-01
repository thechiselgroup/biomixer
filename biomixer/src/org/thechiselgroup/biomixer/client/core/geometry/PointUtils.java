/*******************************************************************************
 * Copyright 2012 David Rusk 
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
package org.thechiselgroup.biomixer.client.core.geometry;

import static java.lang.Math.atan2;
import static java.lang.Math.toDegrees;

public class PointUtils {

    public static PointDouble getMidPoint(PointDouble point1, PointDouble point2) {
        double midX = (point1.getX() + point2.getX()) / 2;
        double midY = (point1.getY() + point2.getY()) / 2;
        return new PointDouble(midX, midY);
    }

    public static double getRotationAngle(PointDouble sourcePoint,
            PointDouble targetPoint) {
        double dX = targetPoint.getX() - sourcePoint.getX();
        double dY = targetPoint.getY() - sourcePoint.getY();
        return toDegrees(atan2(dY, dX));
    }

}
