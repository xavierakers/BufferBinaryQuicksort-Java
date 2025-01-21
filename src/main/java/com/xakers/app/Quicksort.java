package main.java.com.xakers.app;

import main.java.com.xakers.buffer.BufferPool;
import main.java.com.xakers.filegenerator.FileGenerator;
import main.java.com.xakers.sort.Sort;


/**
 * Main class for executing the quicksort program using a buffer pool.
 * This program sorts a binary file using an external quicksort algorithm
 * and measures performance metrics such as cache hits, disk reads, and writes.
 *
 * Features:
 * - File generation
 * - Sorting with buffer management
 * - Metric logging
 *
 * Usage:
 * - <data-file-name> <num-buffers> <stat-file-name>
 *
 * @author Xavier Akers
 * @version 2025-01-21
 */

import java.io.FileWriter;
import java.io.IOException;

/**
 * The class containing the main method.
 *
 * @author Xavier Akers
 * @version 20254
 */

public class Quicksort {
    /**
     * Default buffer size in bytes
     */
    static final String BUFFER_SIZE = "4096";

    /**
     * This method is used to generate a file of a certain size, containing a
     * specified number of records.
     *
     * @param filename  the name of the file to create/write to
     * @param blockSize the size of the file to generate
     * @param format    the format of file to create
     * @throws IOException throw if the file is not open and proper
     */
    public static void generateFile(String filename, String blockSize,
                                    char format) throws IOException {
        FileGenerator generator = new FileGenerator();
        String[] inputs = new String[3];
        inputs[0] = "-" + format;
        inputs[1] = filename;
        inputs[2] = blockSize;
        generator.generateFile(inputs);
    }


    /**
     * The entry point for the quicksort program.
     *
     * @param args Command line parameters:
     *             1. <data-file-name>>: Name of the binary file to sort
     *             2. <num-buffers>: Number of buffers to use for sorting.
     *             3. <stat-file-name>: File to log performance metrics
     * @throws IOException if an error occurs during sorting or metric logging.
     */
    public static void main(String[] args) throws IOException {
        // This is the main file for the program.
        if (args.length != 3) {
            System.err.println("command usage: <data-file-name> <num-buffers> <stat-file-name>");
            System.exit(1);
        }
        String filepath = args[0];
        int numBuffers = Integer.parseInt(args[1]);
        String statFilePath = args[2];

        long startTime = System.currentTimeMillis();

        BufferPool bufferPool = new BufferPool(filepath, numBuffers, Integer.parseInt(BUFFER_SIZE));
        Sort.sort(bufferPool);
        bufferPool.flush();

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.printf("Execution Time: %d milliseconds\n", totalTime);
        System.out.printf("Cache Hits: \t%d\n", bufferPool.getCacheHits());
        System.out.printf("Disk Reads: \t%d\n", bufferPool.getDiskReads());
        System.out.printf("Disk Writes: \t%d\n", bufferPool.getDiskWrites());
        getMetrics(bufferPool, statFilePath, totalTime);
    }

    /**
     * Log performance metrics to a file.
     *
     * @param bufferPool    The buffer pool object containing metric data.
     * @param filepath      The path to the file where metrics will be logged.
     * @param executionTime The total execution time of the sorting operation, in milliseconds.
     */
    private static void getMetrics(BufferPool bufferPool, String filepath, long executionTime) {
        try (FileWriter fout = new FileWriter(filepath)) {
            fout.write("Execution Time:\t" + executionTime + " milliseconds\n");
            fout.write("Cache Hits:\t" + bufferPool.getCacheHits() + "\n");
            fout.write("Disk Reads:\t" + bufferPool.getDiskReads() + "\n");
            fout.write("Disk Writer:\t" + bufferPool.getDiskWrites() + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
