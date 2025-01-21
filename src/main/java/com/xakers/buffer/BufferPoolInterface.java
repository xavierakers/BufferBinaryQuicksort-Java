package main.java.com.xakers.buffer;

import java.io.IOException;

/**
 * Interface for BufferPool operations
 *
 * @author Xavier Akers
 * @version 2025-01-16
 */
public interface BufferPoolInterface {

    /**
     * Inserts data into the buffer pool.
     *
     * @param data        The byte array containing the data to be inserted.
     * @param dataSize    The size of the data to be inserted, in bytes.
     * @param absolutePos The absolute position in the file where the data should be inserted.
     * @throws Exception If an error occurs during the insertion process.
     */
    void setBytes(byte[] data, int dataSize, int absolutePos) throws Exception;

    /**
     * Retrieves data from the buffer pool.
     *
     * @param data        The byte array to store the retrieved data.
     * @param dataSize    The size of the data to retrieve, in bytes.
     * @param absolutePos The absolute position in the file from which the data should be retrieved.
     * @throws Exception If an error occurs during the retrieval process.
     */
    void getBytes(byte[] data, int dataSize, int absolutePos) throws Exception;

    /**
     * Flushes all dirty buffers to the underlying storage and clears the buffer pool.
     *
     * @throws IOException If an error occurs while writing data to the storage.
     */
    void flush() throws IOException;

    /**
     * Gets the total size of the underlying data file.
     *
     * @return The size of the data file, in bytes
     */
    int getDataFileSize() throws IOException;

}
