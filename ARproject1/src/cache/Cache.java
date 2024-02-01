package cache;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

public class Cache {

    // Cache levels and sizes
    private static final int L1_CACHE_SIZE = 32 * 1024; // 32 kB
    private static final int L2_CACHE_SIZE = 512 * 1024; // 512 kB
    private static final int L3_CACHE_SIZE = 32 * 1024 * 1024; // 32 MB

    // Cache line size
    private static final int CACHE_LINE_SIZE = 64; // 64 bytes

    // Cache levels
    private Map<Long, CacheLevel> l1Cache = new HashMap<>();
    private Map<Long, CacheLevel> l2Cache = new HashMap<>();
    private Map<Long, CacheLevel> l3Cache = new HashMap<>();

    public Cache() {
        initializeCaches();
    }

    private void initializeCaches() {
        // Initialize caches with default values or leave them empty
        // ...
    }

    // Method to read from cache
    public byte readFromCache(long address) {
        CacheLevel cacheLevel = getCacheLevel(address);

        if (cacheLevel != null) {
            return cacheLevel.read(address);
        }

        // If cache miss, read from RAM and update caches
        byte[] dataFromRAM = readFromRAM(address);
        updateCaches(address, dataFromRAM);

        return dataFromRAM[getOffset(address)];
    }

    // Method to write to cache
    public void writeToCache(long address, byte data) {
        CacheLevel cacheLevel = getCacheLevel(address);

        if (cacheLevel != null) {
            cacheLevel.write(address, data);
        }

        // If cache miss, write to RAM and update caches
        writeToRAM(address, data);
        updateCaches(address, new byte[]{data});
    }

    // Method to get the cache level based on the address
    public CacheLevel getCacheLevel(long address) {
        long index = getIndex(address);

        if (l1Cache.containsKey(index)) {
            return l1Cache.get(index);
        } else if (l2Cache.containsKey(index)) {
            return l2Cache.get(index);
        } else if (l3Cache.containsKey(index)) {
            return l3Cache.get(index);
        }

        return null;
    }

    // Method to read from RAM (placeholder, replace with actual implementation)
    private byte[] readFromRAM(long address) {
        // Placeholder, replace with actual implementation to read from RAM
        return new byte[]{};
    }

    // Method to write to RAM (placeholder, replace with actual implementation)
    private void writeToRAM(long address, byte data) {
        // Placeholder, replace with actual implementation to write to RAM
    }

    // Method to update caches after a cache miss
    private void updateCaches(long address, byte[] data) {
        // Placeholder, replace with actual implementation to update caches
    }

    // Method to get the index from the address
    private long getIndex(long address) {
        return (address / CACHE_LINE_SIZE) % L1_CACHE_SIZE;
    }

    // Method to get the offset from the address
    private int getOffset(long address) {
        return (int) (address % CACHE_LINE_SIZE);
    }

    // Cache level class
    public static class CacheLevel {
        // Use LinkedHashMap to maintain insertion order for LRU
        private Map<Long, CacheLine> cacheLines = new LinkedHashMap<>(16, 0.75f, true);

        public byte read(long address) {
            CacheLine cacheLine = cacheLines.get(getLineIndex(address));
            if (cacheLine != null) {
                return cacheLine.read(getOffset(address));
            }
            return 0; // Placeholder, replace with actual implementation
        }

        public void write(long address, byte data) {
            long lineIndex = getLineIndex(address);
            CacheLine cacheLine = cacheLines.computeIfAbsent(lineIndex, k -> new CacheLine());
            cacheLine.write(getOffset(address), data);
        }

        private long getLineIndex(long address) {
            return (address / CACHE_LINE_SIZE) % cacheLines.size();
        }

        private int getOffset(long address) {
            return (int) (address % CACHE_LINE_SIZE);
        }

        // Cache line class
        private static class CacheLine {
            private byte[] data = new byte[CACHE_LINE_SIZE];

            public byte read(int offset) {
                return data[offset];
            }

            public void write(int offset, byte value) {
                data[offset] = value;
            }
        }
    }
}