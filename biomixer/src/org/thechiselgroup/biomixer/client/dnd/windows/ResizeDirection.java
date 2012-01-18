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
package org.thechiselgroup.biomixer.client.dnd.windows;

public enum ResizeDirection {

    EAST("e"),

    NORTH("n"),

    NORTH_EAST("ne"),

    NORTH_WEST("nw"),

    SOUTH("s"),

    SOUTH_EAST("se"),

    SOUTH_WEST("sw"),

    WEST("w");

    private final String directionLetters;

    ResizeDirection(String directionLetters) {
        this.directionLetters = directionLetters;
    }

    public String getDirectionLetters() {
        return directionLetters;
    }

    public boolean isEast() {
        switch (this) {
        case SOUTH_EAST:
        case EAST:
        case NORTH_EAST:
            return true;
        }

        return false;
    }

    public boolean isNorth() {
        switch (this) {
        case NORTH_WEST:
        case NORTH:
        case NORTH_EAST:
            return true;
        }

        return false;
    }

    public boolean isSouth() {
        switch (this) {
        case SOUTH_WEST:
        case SOUTH:
        case SOUTH_EAST:
            return true;
        }

        return false;
    }

    public boolean isWest() {
        switch (this) {
        case SOUTH_WEST:
        case WEST:
        case NORTH_WEST:
            return true;
        }

        return false;
    }
}