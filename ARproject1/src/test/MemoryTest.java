package test;

import org.junit.Test;
import static org.junit.Assert.*;

import memory.Memory;

public class MemoryTest {

    @Test   // passed 15.02.2024
    public void testReadAndWriteToVirtualMemory() {
        Memory memory = new Memory();
        long virtualAddress = 0x123456789ABCDEFL;
        byte testData = 42;

        memory.writeToVirtualAddress(virtualAddress, testData);
        byte readData = memory.readFromVirtualAddress(virtualAddress);

        System.out.println("Expected: " + testData);
        System.out.println("Actual: " + readData);

        assertEquals(testData, readData);
    }

    @Test   // passed 15.02.2024
    public void testPageBoundary() {
        Memory memory = new Memory();
        long virtualAddress = 0x1FF00000000L;
        byte testData = 42;

        memory.writeToVirtualAddress(virtualAddress, testData);
        byte readData = memory.readFromVirtualAddress(virtualAddress);

        System.out.println("Expected: " + testData);
        System.out.println("Actual: " + readData);

        assertEquals(testData, readData);
    }

    @Test   // passed 14.02.2024
    public void testTranslationFailure() {
        Memory memory = new Memory();
        long virtualAddress = 0xFFFFFFFFFFFFFFFFL;

        assertNull(memory.translateVirtualToPhysical(virtualAddress));
    }

    @Test   // passed 14.02.2024
    public void testReadUninitializedData() {
        Memory memory = new Memory();
        long virtualAddress = 0x000000000000L;

        assertEquals(0, memory.readFromVirtualAddress(virtualAddress));
    }

    @Test   // passed 14.02.2024
    public void testPerformance() {
        Memory memory = new Memory();
        long virtualAddress = 0x123456789ABCDEFL;
        byte testData = 42;

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000000; i++) {
            memory.writeToVirtualAddress(virtualAddress + i, testData);
            byte readData = memory.readFromVirtualAddress(virtualAddress + i);
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        System.out.println("Execution time for 1,000,000 operations: " + executionTime + " ms");
    }
}
