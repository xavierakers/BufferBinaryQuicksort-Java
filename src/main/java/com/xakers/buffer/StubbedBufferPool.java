package main.java.com.xakers.buffer;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A stubbed implementation of the {@link BufferPoolInterface} for testing purposes.
 *
 * @author Xavier Akers
 * @since 2025-01-16
 */
public class StubbedBufferPool implements BufferPoolInterface {
    private final RandomAccessFile raf;
    private final byte[] stubbedArray;

    /**
     * StubbedBufferPool constructor
     *
     * @param filePath   The path to the datafile.
     * @param numBuffers The number of buffers in the pool.
     * @param blockSize  The size of each buffer.
     */
    public StubbedBufferPool(String filePath, int numBuffers, int blockSize) throws IOException {
        raf = new RandomAccessFile(filePath, "rw");
        stubbedArray = new byte[(int) raf.length()];
        raf.readFully(stubbedArray);
    }

    /**
     * Inserts data into the buffer pool.
     *
     * @param data        The byte array containing the data to be inserted.
     * @param dataSize    The size of the data to be inserted, in bytes.
     * @param absolutePos The absolute position in the file where the data should be inserted.
     */
    @Override
    public void setBytes(byte[] data, int dataSize, int absolutePos) {
        if (absolutePos >= 0 && absolutePos + dataSize <= stubbedArray.length) {
            System.arraycopy(data, 0, stubbedArray, absolutePos, dataSize);
        }
    }

    /**
     * Retrieves data from the buffer pool.
     *
     * @param data        The byte array to store the retrieve data.
     * @param dataSize    The size of the data to retrieve, in bytes.
     * @param absolutePos The absolute position in the file from which the data should be retrieved.
     */
    @Override
    public void getBytes(byte[] data, int dataSize, int absolutePos) {
        if (absolutePos >= 0 && absolutePos + dataSize <= stubbedArray.length) {
            System.arraycopy(stubbedArray, absolutePos, data, 0, dataSize);
        }
    }

    /**
     * Flushes all dirty buffers to the underlying storage and clears the buffer pool.
     *
     * @throws IOException If an error occurs while writing data to the storage.
     */
    @Override
    public void flush() throws IOException {
        raf.seek(0);
        raf.write(stubbedArray);
        raf.close();
    }

    /**
     * Prints the contents of all buffers in the buffer pool.
     */
    public void displayBufferContents() {
        for (byte c : stubbedArray) {
            System.out.print((char) c);
        }
        System.out.println();
    }

    /**
     * Gets the total size of the underlying data file.
     *
     * @return The size of the data file, in bytes
     */
    @Override
    public int getDataFileSize() {
        return stubbedArray.length;
    }

}
