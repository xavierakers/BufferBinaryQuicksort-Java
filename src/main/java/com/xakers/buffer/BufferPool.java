package main.java.com.xakers.buffer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A BufferPool manages a pool of buffer to efficiently read and write data from a file.
 * It uses an LRU (Least Recently Used) caching mechanism to minimize disk I/O.
 * <p>
 * Each buffer in the pool can store a portion of the file, and the pool tracks cache hits,
 * disk reads, and disk writes to measure performance.
 *
 * @author Xavier Akers
 * @version 2025-01-21
 */
public class BufferPool implements BufferPoolInterface {
    private static final int RECORD_SIZE = 4;   // The size of each record in bytes
    private final int bufferSize;               //  The size of each buffer in bytes
    private final Buffer[] pool;                // The pool of buffers manages by this BufferPool
    private final int numBuffers;               // The total number of buffer in the pool
    private final RandomAccessFile raf;         // The RandomAccessFile object used for reading and writing to the file.
    private int cacheHits;                      // Tracks the number of cache hits
    private int diskReads;                      // Tracks the number of disk reads
    private int diskWrites;                     // Tracks the number of disk writes

    /**
     * Constructs a new BufferPool for a given file
     *
     * @param filepath   The path to the file to be managed by the BufferPool.
     * @param numBuffers The number of buffers in the pool.
     * @param bufferSize The size of each buffer in bytes
     * @throws FileNotFoundException If the file cannot be found or opened.
     */
    public BufferPool(String filepath, int numBuffers, int bufferSize) throws FileNotFoundException {
        this.cacheHits = 0;
        this.diskWrites = 0;
        this.diskReads = 0;
        this.bufferSize = bufferSize;
        this.numBuffers = numBuffers;
        this.raf = new RandomAccessFile(filepath, "rw");
        this.pool = new Buffer[numBuffers];
        for (int i = 0; i < numBuffers; i++) {
            pool[i] = new Buffer(bufferSize);
        }
    }


    /**
     * Inserts data into the buffer pool at the specified position.
     *
     * @param data        The byte array containing the data to be inserted.
     * @param dataSize    The size of the data to be inserted, in bytes.
     * @param absolutePos The absolute position in the file where the data should be inserted.
     * @throws Exception If an error occurs during the insertion process.
     */
    @Override
    public void setBytes(byte[] data, int dataSize, int absolutePos) throws Exception {
        int blockNum = absolutePos / bufferSize;
        int relPos = absolutePos % bufferSize;

        Buffer buffer = findBuffer(absolutePos, blockNum);
        buffer.setDirty(true);
        buffer.setBytes(data, relPos, RECORD_SIZE);
    }

    /**
     * Retrieves data from the buffer pool at a specified position.
     *
     * @param data        The byte array to store the retrieved data.
     * @param dataSize    The size of the data to retrieve, in bytes.
     * @param absolutePos The absolute position in the file from which the data should be retrieved.
     * @throws Exception If an error occurs during the retrieval process.
     */
    @Override
    public void getBytes(byte[] data, int dataSize, int absolutePos) throws Exception {
        int blockNum = absolutePos / bufferSize;
        int relPos = absolutePos % bufferSize;

        Buffer buffer = findBuffer(absolutePos, blockNum);
        buffer.getBytes(data, relPos, RECORD_SIZE);
    }

    /**
     * Flushes all dirty buffers to the underlying storage and clears the buffer pool.
     *
     * @throws IOException If an error occurs while writing data to the storage.
     */
    @Override
    public void flush() throws IOException {
        for (Buffer buffer : pool) {
            if (buffer.isDirty()) {
                raf.seek(buffer.getPos());
                raf.write(buffer.getData());
                diskWrites++;
            }
        }
        raf.close();
    }

    /**
     * Returns the total size of the underlying data file.
     *
     * @return The size of the data file, in bytes
     * @throws IOException If an error occurs while accessing the file.
     */
    @Override
    public int getDataFileSize() throws IOException {
        return (int) raf.length();
    }

    /**
     * Find the buffer containing the specified position or loads the relevant data into the least recently used buffer.
     *
     * @param pos      The absolute position in the file.
     * @param blockNum The block number corresponding to the position.
     * @return The buffer containing the specified position.
     * @throws Exception If an error occurs during the buffer loading process.
     */
    private Buffer findBuffer(int pos, int blockNum) throws Exception {
        Buffer buff = null;
        int bufferIndex = numBuffers - 1;
        // Attempting to find a buffer that exists
        // If position is within the range
        for (int i = 0; i < numBuffers; i++) {
            if (isPositionInBuffer(pos, pool[i])) {
                buff = pool[i];
                bufferIndex = i;
                cacheHits++;
            }
        }
        // Updating list by moving blockIndex to the front
        updateLRU(bufferIndex, blockNum * bufferSize);

        // If we did not find a buffer with our data loaded
        if (buff == null) {
            buff = pool[0];
            buff.setPos(blockNum * bufferSize);
            buff.setDirty(false);
            raf.seek(buff.getPos());
            raf.read(buff.getData());
            diskReads++;
        }
        return buff;
    }

    /**
     * Checks if the given position is within the specified buffer.
     *
     * @param pos    The absolute position to check.
     * @param buffer The buffer to check against.
     * @return {@code true} if the position is within the buffer, {@code false} otherwise.
     */
    private boolean isPositionInBuffer(int pos, Buffer buffer) {
        return pos >= buffer.getPos() && pos < buffer.getPos() + bufferSize && buffer.getPos() >= 0;
    }

    /**
     * Updates the buffer pool using an LRU (Least Recently Used) strategy and writes dirty buffers to the file if needed.
     *
     * @param index The index of the buffer to update.
     * @param pos   The new position to be loaded into the buffer.
     * @throws Exception If an error occurs while writing data to the file.
     */
    private void updateLRU(int index, int pos) throws Exception {
        Buffer buffer = pool[index];
        shiftBuffersLeft(index);

        // replace the first buffer
        pool[0] = buffer;

        // if buffer exists and does not contain the block we want
        if (buffer.getPos() != pos && buffer.isDirty()) {
            raf.seek(buffer.getPos());
            raf.write(buffer.getData());
            buffer.setDirty(false);
            diskWrites++;

        }
    }

    /**
     * Shifts the buffers in the pool to the left, effectively removing a buffer at the specified index.
     *
     * @param index The index of the buffer to remove.
     */
    private void shiftBuffersLeft(int index) {
        // Shift all buffers in front of the current on to the right
        for (int i = index; i > 0; i--) {
            pool[i] = pool[i - 1];
        }
    }

    /**
     * Returns the total number of disk reads performed by the buffer pool.
     *
     * @return The number of disk reads.
     */
    public int getDiskReads() {
        return diskReads;
    }

    /**
     * Returns the total number of cache hits achieved by the buffer pool.
     *
     * @return The number of cache hits.
     */
    public int getCacheHits() {
        return cacheHits;
    }

    /**
     * Returns the total number of disk writes performed by the buffer pool.
     *
     * @return The number of disk writes.
     */
    public int getDiskWrites() {
        return diskWrites;
    }
}
