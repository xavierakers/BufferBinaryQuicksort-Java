package main.java.com.xakers.app;

import main.java.com.xakers.checkfile.CheckFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuicksortTest {
    @Test
    void testFileGenerator() throws Exception {
        Quicksort.generateFile("data.txt", "1", 'a');
        CheckFile checkFile = new CheckFile();
        assertFalse(checkFile.checkFile("data.txt"));
    }

    @Test
    void testSort1Block1BufferAscii() throws Exception {
        String infile = "data.txt";
        String[] args = {infile, "1", "stats.txt"};

        Quicksort.generateFile(infile, "1", 'a');
        Quicksort.main(args);
        CheckFile checkFile = new CheckFile();
        assertTrue(checkFile.checkFile(infile));
    }

    @Test
    void testSort10Block1BufferBin() throws Exception {
        String infile = "data.txt";
        String[] args = {infile, "1", "stats.txt"};

        Quicksort.generateFile(infile, "10", 'b');
        Quicksort.main(args);
        CheckFile checkFile = new CheckFile();
        assertTrue(checkFile.checkFile(infile));
    }

    @Test
    void testSort100Block4BufferBin() throws Exception {
        String infile = "data.txt";
        String[] args = {infile, "4", "stats.txt"};

        Quicksort.generateFile(infile, "100", 'b');
        Quicksort.main(args);
        CheckFile checkFile = new CheckFile();
        assertTrue(checkFile.checkFile(infile));
    }
}