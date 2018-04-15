/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.flowingliquids.world.block;

import org.terasology.math.Side;

/**
 * Utility class for encoding and decoding liquid data.
 */
public class LiquidData {
    public static final int MAX_HEIGHT = 8;
    public static final int MAX_RATE = 1;
    public static final int MAX_DOWN_RATE = 2;
    public static final byte FULL = (byte) 0b00_110_111;
    
    /**
     * Extracts the amount of liquid in the block from a byte of liquid data.
     * @param status The packed liquid data
     * @return The height as an int
     */
    public static int getHeight(byte status){
        return (int) (status & 0b00_000_111)+1;
    }
    
    /**
     * Replaces the height in a byte of liquid data with a different height.
     * @param status The byte to modify
     * @param height The height to store
     * @return The modified byte
     */
    public static byte setHeight(byte status, int height){
        if (height < 1 || height > MAX_HEIGHT)
            throw new IllegalArgumentException("Liquid heights are constrained to the range 1 to 8.");
        return (byte) ((status & ~0b00_000_111) | (height-1));
    }

    /**
     * Extracts the direction of flow from a byte of liquid data.
     * @param status The packed liquid data
     * @return The direction, or null if there is no flow
     */
    public static Side getDirection(byte status) {
        int sideData = sideData(status);
        if (sideData < 6) {
            return Side.values()[sideData];
        } else if (sideData == 6) {
            return null;
        } else { // sideData == 7
            return Side.BOTTOM;
        }
    }
    
    /**
     * Replaces the flow direction in a byte of liquid data with a different direction.
     * @param status The byte to modify
     * @param side The direction to store, or null for no flow
     * @return The modified byte
     */
    public static byte setDirection(byte status, Side side) {
        int sideData = side == null ? 6 : side.ordinal();
        return (byte) ((status & ~0b00_111_000) | (sideData << 3));
    }
    
    /**
     * Extracts the flow rate from a byte of liquid data.
     * @param status The packed liquid data
     * @return The flow rate as an int
     */
    public static int getRate(byte status) {
        int sideData = sideData(status);
        if (sideData < 6) {
            return 1;
        } else if (sideData == 6) {
            return 0;
        } else { // sideData == 7
            return 2;
        }
    }
    
    /**
     * Replaces the flow rate in a byte of liquid data with a different rate.
     * @param status The byte to modify
     * @param side The rate to store
     * @return The modified byte
     */
    public static byte setRate(byte status, int rate) {
        int sideData = sideData(status);
        if (rate == 0) {
            return setDirection(status, null);
        } else if (rate == 1 && sideData < 6) {
            return status;
        } else if (rate == 1 && sideData == 7) {
            return setDirection(status, Side.BOTTOM);
        } else if (rate == 2 && sideData == Side.BOTTOM.ordinal()) {
            return (byte) ((status & ~0b00_111_000) | (7 << 3));
        } else if (sideData == 6) {
            throw new IllegalArgumentException("Can't set rate > 0 as there is no current direction.");
        } else {
            throw new IllegalArgumentException("Liquid rates are constrained to the range 0 to 1 (or 2 for downwards). Was "+rate);
        }
    }
    
    /**
     * Extracts the 3 bits relevant to flow direction from a byte of liquid data.
     * @param status The packed liquid data
     * @return The index of the flow direction: up, down, left, right, forward, backward, none, quickly down
     */
    private static int sideData(byte status) {
        return (status & 0b00_111_000) >> 3;
    }
}
