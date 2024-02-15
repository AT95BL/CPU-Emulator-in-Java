package test;

import org.junit.Test;
import static org.junit.Assert.*;

import cache.Cache;
import memory.Memory;

public class CacheTest {

    @Test   // passed 15.02.2024
    public void testReadFromCache() {
        Memory memory = new Memory();
        Cache cache = new Cache(memory);

        // Write some data to memory
        long virtualAddress = 0x1000;
        byte testData = 42;
        memory.writeToVirtualAddress(virtualAddress, testData);

        // Read from cache and assert the value
        byte readData = cache.readFromCache(virtualAddress);
        assertEquals(testData, readData);
    }

    @Test   //  passed 15.02.2024
    public void testWriteToCache() {
        Memory memory = new Memory();
        Cache cache = new Cache(memory);

        // Write some data to cache
        long virtualAddress = 0x1000;
        byte testData = 42;
        cache.writeToCache(virtualAddress, testData);

        // Read from memory and assert the value
        byte readData = memory.readFromVirtualAddress(virtualAddress);
        assertEquals(testData, readData);
    }

    @Test   //  passed 15.02.2024
    public void testGetCacheHitPercentage() {
        Memory memory = new Memory();
        Cache cache = new Cache(memory);

        // Perform some cache hits and misses
        long virtualAddress1 = 0x1000;
        long virtualAddress2 = 0x2000;

        System.out.println("Reading from cache at virtual address 0x1000");
        cache.readFromCache(virtualAddress1);

        System.out.println("Writing to cache at virtual address 0x2000");
        cache.writeToCache(virtualAddress2, (byte) 42);

        System.out.println("Reading from cache again at virtual address 0x1000");
        cache.readFromCache(virtualAddress1);

        // Assert the cache hit percentage
        System.out.println("Cache hit percentage: " + cache.getCacheHitPercentage());
        assertEquals(50.0, cache.getCacheHitPercentage(), 0.01);
    }
}
