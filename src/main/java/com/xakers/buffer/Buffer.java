package main.java.com.xakers.buffer;

/**
 * Represents a buffer used in the buffer pool.
 * Each buffer contains a fixed amount of data and tracks its state, such as whether it has been modified (dirty)
 * or its current position in the underlying file.
 *
 * @author Xavier Akers
 * @version 2025-01-16
 */
public class Buffer {
    private final int bufferSize;   // The size of the buffer in bytes.
    private final byte[] data;      // The byte array that stores the data for this buffer.
    private int pos;                // The position in the file corresponding to the start of this buffer.
    private boolean isDirty;        // Indicates whether the buffer contains modified (dirty) data that needs to be written.

    /**
     * Constructs a new buffer of the specified size.
     * The buffer is initialized as clean (not dirty) and its position is set to -1 (uninitialized),
     *
     * @param bufferSize The size of the buffer in bytes.
     */
    public Buffer(int bufferSize) {
        this.bufferSize = bufferSize;
        this.data = new byte[bufferSize];
        this.isDirty = false;
        this.pos = -1;
    }

    /**
     * Returns the byte array storing the buffer's data.
     *
     * @return The byte array storing the buffer's data.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the size of the buffer.
     *
     * @return The size of the buffer in bytes.
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Returns the current position in the file that this buffer represents.
     *
     * @return The position in the file, or -1 if the position is uninitialized.
     */
    public int getPos() {
        return pos;
    }

    /**
     * Sets the position in the file that this buffer represents.
     *
     * @param pos The new position in the file.
     */
    public void setPos(int pos) {
        this.pos = pos;
    }

    /**
     * Checks whether the buffer is dirty (contains modified data).
     *
     * @return {@code true} if the buffer is dirty, {@code false} otherwise.
     */
    public boolean isDirty() {
        return this.isDirty;
    }

    /**
     * Sets the dirty state of the buffer.
     *
     * @param dirty {@code true} to mark the buffer as dirty, {@code false} to mark it as clean.
     */
    public void setDirty(boolean dirty) {
        this.isDirty = dirty;
    }

    /**
     * Copies data from the buffer to the specified byte array.
     *
     * @param temp The byte array where the data will be copied.
     * @param offset The position in the buffer's data from which to start copying.
     * @param size The number of bytes to copy.
     */
    public void setBytes(byte[] temp, int offset, int size) {
        System.arraycopy(temp, 0, this.data, offset, size);
    }

    /**
     * Copies data from the buffer to the specified byte array.
     *
     * @param temp   The byte array where the data will be copied.
     * @param offset The position in the buffer's data from which to start copying.
     * @param size   The number of bytes to copy.
     */
    public void getBytes(byte[] temp, int offset, int size) {
        System.arraycopy(data, offset, temp, 0, size);
    }
}
