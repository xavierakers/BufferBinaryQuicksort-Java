package main.java.com.xakers.sort;

import main.java.com.xakers.buffer.BufferPoolInterface;

import java.nio.ByteBuffer;

/**
 * @author Xavier Akers
 * @since 2025-01-16
 */
public class Sort {
    private static final int RECORD_SIZE = 4;
    private static final byte[] TEMP_1 = new byte[RECORD_SIZE];
    private static final byte[] TEMP_2 = new byte[RECORD_SIZE];

    /**
     * Public sort method to initiate quicksort
     *
     * @param bufferPool The buffer pool for data access adhering to the BufferPool interface.
     */
    public static void sort(BufferPoolInterface bufferPool) {
        if (bufferPool == null) {
            throw new IllegalArgumentException("BufferPool cannot be null.");
        }
        try {
            quickSort(bufferPool, 0, bufferPool.getDataFileSize() - RECORD_SIZE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // ----------------------------------------------------------
    // Private Helper Methods
    // ----------------------------------------------------------

    /**
     * QuickSort implementation using buffer pool.
     *
     * @param bufferPool The buffer pool for data access adhering to the BufferPool interface.
     * @param left       The leftmost index of the partition.
     * @param right      The rightmost index of the partition
     */
    private static void quickSort(BufferPoolInterface bufferPool, int left, int right) throws Exception {
        if (left >= right) return;

        int pivotIdx = findPivot(left, right);
        byte[] pivotVal = getPivot(bufferPool, pivotIdx);
        short key = getKey(pivotVal);

        bufferPool.getBytes(TEMP_1, RECORD_SIZE, right & -RECORD_SIZE);
        swap(bufferPool, pivotIdx, pivotVal, right, TEMP_1);

        pivotIdx = partition(bufferPool, left, right - RECORD_SIZE, key);

        bufferPool.getBytes(TEMP_1, RECORD_SIZE, pivotIdx);
        swap(bufferPool, pivotIdx, TEMP_1, right, pivotVal);

        quickSort(bufferPool, left, pivotIdx - RECORD_SIZE);
        quickSort(bufferPool, pivotIdx + RECORD_SIZE, right);
    }

    private static int partition(BufferPoolInterface bufferPool, int left, int right, short key) throws Exception {
        right &= ~3;

        while (left <= right) {
            bufferPool.getBytes(TEMP_1, RECORD_SIZE, left);
            while (getKey(TEMP_1) < key) {
                left += RECORD_SIZE;
                bufferPool.getBytes(TEMP_1, RECORD_SIZE, left);
            }

            bufferPool.getBytes(TEMP_2, RECORD_SIZE, right);
            while (right >= left && getKey(TEMP_2) >= key) {
                right -= RECORD_SIZE;
                if (right >= 0) {
                    bufferPool.getBytes(TEMP_2, RECORD_SIZE, right);
                }
            }
            if (right > left) {
                swap(bufferPool, left, TEMP_1, right, TEMP_2);
            }
        }

        return left;

    }

    /**
     * Finds the pivot of the bufferedStorage
     * Adjusted for 4 bytes records
     *
     * @param left  left most index
     * @param right right most index
     * @return the Pivot index of the record
     */
    private static int findPivot(int left, int right) {
        return (((left / 4) + (right / 4)) / 2) * 4;
    }

    private static byte[] getPivot(BufferPoolInterface bufferPool, int pivotIdx) throws Exception {
        byte[] pivotVal = new byte[RECORD_SIZE];
        bufferPool.getBytes(pivotVal, RECORD_SIZE, pivotIdx);
        return pivotVal;
    }

    private static short getKey(byte[] record) {
        return (short) ((record[0] & 0xFF) << 8 | (record[1] & 0xFF));
//        ByteBuffer buffer = ByteBuffer.wrap(record);
//        return buffer.getShort();
    }


    private static void swap(BufferPoolInterface bufferPool, int index1, byte[] value1, int index2, byte[] value2) throws Exception {
        index1 &= ~3;
        index2 &= ~3;

        if (index1 >= 0 && index1 < bufferPool.getDataFileSize()
                && index2 >= 0 && index2 < bufferPool.getDataFileSize()) {
            bufferPool.setBytes(value1, RECORD_SIZE, index2);
            bufferPool.setBytes(value2, RECORD_SIZE, index1);
        }
    }

}

