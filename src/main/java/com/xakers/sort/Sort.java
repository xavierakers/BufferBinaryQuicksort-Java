package main.java.com.xakers.sort;

import main.java.com.xakers.buffer.BufferPoolInterface;

/**
 * Provides a QuickSort implementation that operates on large binary files using a buffer pool.
 * <p>
 * The class is designed for sorting records of fixed size (4 bytes) stores in a file, where
 * data access is managed by a {@link BufferPoolInterface}
 *
 * @author Xavier Akers
 * @version 2025-01-21
 * @since 2025-01-16
 */
public class Sort {
    private static final int RECORD_SIZE = 4; // Size of a single record in bytes
    private static final byte[] TEMP_1 = new byte[RECORD_SIZE]; // Temporary buffer for swapping records
    private static final byte[] TEMP_2 = new byte[RECORD_SIZE]; //  Additional temporary  buffer for swapping records

    /**
     * Initiates the sorting process on a file managed by the provided buffer pool.
     *
     * @param bufferPool The buffer pool for accessing and modifying file data.
     *                   Must adhere to the {@link BufferPoolInterface}.
     * @throws IllegalArgumentException if the bufferPool is null.
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
     * Recursive QuickSort implementation using a buffer pool for data access.
     *
     * @param bufferPool The buffer pool for data access.
     * @param left       The starting index of the current partition.
     * @param right      The ending index of the current partition.
     * @throws Exception If there is an issue accessing the buffer pool.
     */
    private static void quickSort(BufferPoolInterface bufferPool, int left, int right) throws Exception {
        if (left >= right) return; // Base case: Partition is sorted

        // Determine the pivot index and value
        int pivotIdx = findPivot(left, right);
        byte[] pivotVal = getPivot(bufferPool, pivotIdx);
        short key = getKey(pivotVal);

        // Move the pivot to the end of the partition
        bufferPool.getBytes(TEMP_1, RECORD_SIZE, right & -RECORD_SIZE);
        swap(bufferPool, pivotIdx, pivotVal, right, TEMP_1);

        // Partition the data around the pivot
        pivotIdx = partition(bufferPool, left, right - RECORD_SIZE, key);

        // Place the pivot in its correct position
        bufferPool.getBytes(TEMP_1, RECORD_SIZE, pivotIdx);
        swap(bufferPool, pivotIdx, TEMP_1, right, pivotVal);

        // Recursively sort the left and right partitions
        quickSort(bufferPool, left, pivotIdx - RECORD_SIZE);
        quickSort(bufferPool, pivotIdx + RECORD_SIZE, right);
    }

    /**
     * Partitions the data in the buffer pool around the pivot.
     *
     * @param bufferPool The buffer pool for data access.
     * @param left       The starting index of the partition.
     * @param right      The ending index of the partition (exclusive).
     * @param key        The pivot value for comparison.
     * @return The final position of the pivot after partitioning.
     * @throws Exception If there is an issue accessing the buffer pool.
     */
    private static int partition(BufferPoolInterface bufferPool, int left, int right, short key) throws Exception {
        right &= ~3; // Align right to the nearest record boundary

        while (left <= right) {
            // Find the first record on the left >= pivot
            bufferPool.getBytes(TEMP_1, RECORD_SIZE, left);
            while (getKey(TEMP_1) < key) {
                left += RECORD_SIZE;
                bufferPool.getBytes(TEMP_1, RECORD_SIZE, left);
            }

            // Find the first record on the right < pivot
            bufferPool.getBytes(TEMP_2, RECORD_SIZE, right);
            while (right >= left && getKey(TEMP_2) >= key) {
                right -= RECORD_SIZE;
                if (right >= 0) {
                    bufferPool.getBytes(TEMP_2, RECORD_SIZE, right);
                }
            }

            // Swap records if needed
            if (right > left) {
                swap(bufferPool, left, TEMP_1, right, TEMP_2);
            }
        }

        return left; // Return the partition point

    }

    /**
     * Calculates the pivot index for the partition.
     *
     * @param left  The starting index of the partition.
     * @param right The ending index of the partition.
     * @return The pivot index, aligned to the nearest record boundary.
     */
    private static int findPivot(int left, int right) {
        return (((left / 4) + (right / 4)) / 2) * 4;
    }

    /**
     * Retrieves the pivot value from the buffer pool.
     *
     * @param bufferPool The buffer pool for data access.
     * @param pivotIdx   The index of the pivot.
     * @return The pivot value as a byte array.
     * @throws Exception If there is an issue accessing the buffer pool.
     */
    private static byte[] getPivot(BufferPoolInterface bufferPool, int pivotIdx) throws Exception {
        byte[] pivotVal = new byte[RECORD_SIZE];
        bufferPool.getBytes(pivotVal, RECORD_SIZE, pivotIdx);
        return pivotVal;
    }

    /**
     * Extracts the key from a record for sorting.
     *
     * @param record The byte array representing a record.
     * @return The key as a short value.
     */
    private static short getKey(byte[] record) {
        return (short) ((record[0] & 0xFF) << 8 | (record[1] & 0xFF));
    }

    /**
     * Swaps two records in the buffer pool.
     *
     * @param bufferPool The buffer pool for data access.
     * @param index1     The index of the first record.
     * @param value1     The byte array of the first record.
     * @param index2     The index of the second record.
     * @param value2     The byte array of the second record.
     * @throws Exception If there is an issue accessing the buffer pool.
     */
    private static void swap(BufferPoolInterface bufferPool, int index1, byte[] value1, int index2, byte[] value2) throws Exception {
        index1 &= ~3; // Align index1 to the nearest record boundary
        index2 &= ~3; // Align index2 to the nearest record boundary

        if (index1 >= 0 && index1 < bufferPool.getDataFileSize()
                && index2 >= 0 && index2 < bufferPool.getDataFileSize()) {
            bufferPool.setBytes(value1, RECORD_SIZE, index2);
            bufferPool.setBytes(value2, RECORD_SIZE, index1);
        }
    }

}

