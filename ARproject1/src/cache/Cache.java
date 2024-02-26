package cache;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import memory.Memory;

import static org.junit.Assert.assertEquals;

public class Cache {

    // Cache levels and sizes
    private static final int L1_CACHE_SIZE = 32 * 1024; // 32 kB
    private static final int L2_CACHE_SIZE = 512 * 1024; // 512 kB
    private static final int L3_CACHE_SIZE = 32 * 1024 * 1024; // 32 MB
    // Cache line size
    private static final int CACHE_LINE_SIZE = 64; // 64 bytes
    // Cache levels
    // private Map<Long, CacheLevel> l1Cache = new LruCache<>(L1_CACHE_SIZE);
    // private Map<Long, CacheLevel> l2Cache = new LruCache<>(L2_CACHE_SIZE);
    // private Map<Long, CacheLevel> l3Cache = new LruCache<>(L3_CACHE_SIZE);
    private List<Map<Long, CacheLevel>> cacheLevels = new ArrayList<>();

    // Cache levels and sizes
    private int numCacheLevels;        // Number of cache levels
    private int[] cacheSizes;          // Cache sizes for each level
    private int[] associativities;     // Associativities for each level, Ovo je niz koji sadrži asocijativnosti za svaki nivo keša. Asocijativnost se odnosi na to koliko linija keša može držati podatke koji se mapiraju na isti indeks. Na primer, ako je asocijativnost za L1, L2 i L3 postavljena na 2, 4 i 8 redom, to znači da su ovi nivoi keša dvostruko,četvorostruko, odnosno osmostruko asocijativni.
    private int cacheLineSize;         // Cache line size

    // Counters for cache hits and misses
    private int cacheHits = 0;
    private int cacheMisses = 0;
    // private int cacheMissesTimes=0; // pa + cacheHits = cacheAccesses

    // Reference to the Memory instance
    private Memory memory;

    // Constructor to initialize Memory instance
    public Cache(Memory memory) {
        this.memory = memory;
        initializeCaches();
    }

    public Cache(Memory memory, int numCacheLevels, int[] cacheSizes, int[] associativities, int cacheLineSize) {
        this.memory = memory;
        this.numCacheLevels = numCacheLevels;
        this.cacheSizes = cacheSizes;
        this.associativities = associativities;
        this.cacheLineSize = cacheLineSize;
        initializeCaches();
    }

    /*
    private void initializeCaches() {
        for (int i = 0; i < numCacheLevels; i++) {
            int cacheSize = cacheSizes[i];
            int associativity = associativities[i];
            Map<Long, CacheLevel> cache = switch (i) {
                case 0 -> new LruCache<>(cacheSize);
                case 1 -> new LruCache<>(cacheSize);
                case 2 -> new LruCache<>(cacheSize);
                default -> throw new IllegalArgumentException("Invalid cache level: " + i);
            };
            initializeCacheLevel(cache, cacheSize, associativity);
            switch (i) {
                case 0 -> {
                    l1Cache = cache;
                    initializeCacheLevel(l1Cache, cacheSize, associativity);
                }
                case 1 -> {
                    l2Cache = cache;
                    initializeCacheLevel(l2Cache, cacheSize, associativity);
                }
                case 2 -> {
                    l3Cache = cache;
                    initializeCacheLevel(l3Cache, cacheSize, associativity);
                }
                default -> throw new IllegalArgumentException("Invalid cache level: " + i);
            }
        }
    }
    */

    private void initializeCaches() {
        for (int i = 0; i < numCacheLevels; i++) {
            int cacheSize = cacheSizes[i];
            int associativity = associativities[i];
            Map<Long, CacheLevel> cache = new LruCache<>(cacheSize);
            initializeCacheLevel(cache, cacheSize, associativity);
            cacheLevels.add(cache);
        }
    }

    private void initializeCacheLevel(Map<Long, CacheLevel> cache, int cacheSize, int associativity) {
        int numCacheLines = cacheSize / CACHE_LINE_SIZE;
        for (long j = 0; j < numCacheLines; j++) {
            cache.put(j, new CacheLevel());
        }
    }

    // Method to read from cache
    public byte readFromCache(long address) {
        CacheLevel cacheLevel = getCacheLevel(address);
        if (cacheLevel != null) {
            try {
                byte data = cacheLevel.read(address);
                if (data != 0) {
                    cacheHits++;
                    System.out.println(cacheHits);
                } else {
                    System.out.println(cacheMisses);
                }
                System.out.println("Cache Hit!! Data: " + data);
                return data;
            } catch (IllegalStateException e) {
                System.err.println("Exception caught: " + e.getMessage());
                return 0;
            }
        }

        System.out.println("cacheLevel = 0");
        cacheMisses++;
        // If cache miss, read from RAM and update caches
        byte[] dataFromRAM = readFromRAM(address);
        String str = "";
        for(byte s: dataFromRAM){
            str += s;
            str += " ";
        }
        System.out.println("Cache Miss!! Data read From RAM: " + str);
        updateCaches(address, dataFromRAM);
        return dataFromRAM[getOffset(address)];
    }

    // Method to write to cache
    public void writeToCache(long address, byte data) {
        System.out.println("Write to Cache <address,data>: " + "<" + address + "," + data + ">");
        CacheLevel cacheLevel = getCacheLevel(address);
        if (cacheLevel != null) {
            System.out.println("cacheLevel != 0");
            System.out.println("Cache Hit!! Data: " + data);
            cacheLevel.write(address, data);
            cacheHits++;
        }
        // If cache miss, write to RAM and update caches
        System.out.println("cacheLevel = 0");
        writeToRAM(address, data);
        updateCaches(address, new byte[]{data});
        cacheHits++;
        cacheMisses++;
    }

    // Method to get cache hit percentage
    public double getCacheHitPercentage() {
        System.out.println("Method: getCacheHitPercentage ");
        int totalAccesses = cacheHits + cacheMisses; // mozda kao atribut klase? Onda monitorujem procente, a ne ceste pogotke/promasaje..
        if (totalAccesses > 0) {
            return ((double) cacheHits / totalAccesses) * 100;
        }
        return 0;
    }

    // Method to get the cache level based on the address
    /*
    public CacheLevel getCacheLevel(long address) {
        System.out.println("Method: getCacheLevel");
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
    */
    public CacheLevel getCacheLevel(long address) {
        for (Map<Long, CacheLevel> cache : cacheLevels) {
            if (cache.containsKey(address)) {
                return cache.get(address);
            }
        }
        return null;
    }


    // Method to read from RAM
    public byte[] readFromRAM(long address) {
        System.out.println("Method: readFromRAM");
        // Use the Memory instance to read from virtual address
        int cacheLineSize = CACHE_LINE_SIZE;
        byte[] dataFromRAM = new byte[cacheLineSize];
        for (int i = 0; i < cacheLineSize; i++) {
            dataFromRAM[i] = memory.readFromVirtualAddress(address + i);
        }
        // cacheMisses++; kakav, jes lud?
        cacheHits++;
        return dataFromRAM;
    }

    // Method to write to RAM
    public void writeToRAM(long address, byte data) {
        System.out.println("writeToRAM");
        // Use the Memory instance to write to virtual address
        memory.writeToVirtualAddress(address, data);
        // cacheMisses++; kakav, jes lud?
    }

    // Method to update caches after a cache miss
    private void updateCaches(long address, byte[] data) {
        System.out.println("Method: updateCaches");
        // Assuming LRU strategy for simplicity
        CacheLevel cacheLevel = getCacheLevel(address);
        if (cacheLevel != null) {
            cacheLevel.write(address, data[0]);
            cacheHits++;
        }
    }

    // Method to get the index from the address
    private long getIndex(long address) {
        System.out.println("Method: getIndex");
        return (address / CACHE_LINE_SIZE) % L1_CACHE_SIZE;
    }

    // Method to get the offset from the address
    private int getOffset(long address) {
        System.out.println("Method: getOffset");
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
            if (cacheLines.isEmpty()) {
                // Ako je cacheLines prazna, dodajte bar jednu liniju
                cacheLines.put(0L, new CacheLine());
            }
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

    // LRU-based cache class
    private static class LruCache<K, V> extends LinkedHashMap<K, V> {
        private static final long serialVersionUID = 1L;
        private final int maxSize;

        LruCache(int maxSize) {
            super(maxSize, 0.75f, true);
            this.maxSize = maxSize;
        }
        /*
            `super(maxSize, 0.75f, true)` poziva konstruktor nadređene klase, odnosno konstruktor `LinkedHashMap` klase.
            Evo šta svaki od parametara znači:
            1. **maxSize**:
                Ovaj parametar predstavlja maksimalnu veličinu keša.
                Kada broj unosa u kešu dostigne ovu vrednost,
                primenjuje se LRU strategija kako bi se oslobodilo mesto za nove unose.
            2. **0.75f**:
                Ovaj parametar predstavlja faktor opterećenja (load factor) koji se koristi za
                automatsko povećanje veličine keša kada se dostigne određeni procenat od maksimalne veličine.
                U ovom slučaju, kada se keš napuni 75%, veličina će biti automatski povećana.
            3. **true**:
                Ovaj parametar označava da se koristi poredak uvođenja (access order) umesto podrazumevanog poretka uvođenja.
                Kod LRU keša,
                želimo pratiti redosled pristupa elementima kako bismo mogli tačno odrediti koji elementi su najmanje korišćeni i ukloniti ih prilikom potrebe. Postavljanjem ovog parametra na `true`, keš će pratiti redosled pristupa prilikom uvođenja novih elemenata.
            Dakle, poziv `super(maxSize, 0.75f, true)`
            inicijalizuje `LinkedHashMap` sa specifičnim postavkama koje su prilagođene LRU strategiji.
         */

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxSize;
        }
        /*
            Ova metoda `removeEldestEntry` je preklopljeni (overridden)
            metod u klasi `LruCache` koja proširuje klasu `LinkedHashMap`.
            Ovaj metod je od suštinskog značaja za implementaciju LRU (Least Recently Used) keša.
            Evo šta ova metoda radi:
                1. **Map.Entry<K, V> eldest**:
                    Parametar `eldest` predstavlja najstariji unos (entry) u kešu.
                    "Najstariji" se odnosi na unos koji je bio najmanje korišćen ili koji je bio u kešu najduže vreme.
                2. **return size() > maxSize**:
                Ova linija koda proverava da li je trenutna veličina keša (`size()`) veća od maksimalne veličine (`maxSize`).
                Ako je to tačno, metoda vraća `true`, što znači da će najstariji unos biti uklonjen iz keša.

            Ova provera je ključna za implementaciju LRU strategije.
            Kada keš dostigne svoju maksimalnu veličinu, ova metoda će biti pozvana pre nego što se novi unos doda u keš.
            Ako vrati `true`, to znači da je keš premašio svoju maksimalnu veličinu i najstariji unos će biti uklonjen,
            praveći mesto za novi unos. Ako vrati `false`, novi unos će biti dodat u keš, a najstariji će ostati nepromenjen.
         */
    }

    public void cacheMonitor(){
        System.out.println("No. of Cache hits: " + this.cacheHits + "\n");
        System.out.println("No. of Cache misses: " + this.cacheMisses + "\n");
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Cache{");

        // Iterirajte kroz sve keš nivoe
        for (int i = 0; i < numCacheLevels; i++) {
            result.append("l").append(i + 1).append("Cache=").append(cacheLevels.get(i)).append(", ");
        }

        // Dodajte preostale informacije
        result.append("numCacheLevels=").append(numCacheLevels)
                .append(", cacheSizes=").append(Arrays.toString(cacheSizes))
                .append(", associativities=").append(Arrays.toString(associativities))
                .append(", cacheLineSize=").append(cacheLineSize)
                .append(", cacheHits=").append(cacheHits)
                .append(", cacheMisses=").append(cacheMisses)
                .append(/*", memory=" + memory +*/ '}');

        return result.toString();
    }

    /*
    * @Override
    public String toString() {
        return "Cache{" +
                "l1Cache=" + l1Cache +
                ", l2Cache=" + l2Cache +
                ", l3Cache=" + l3Cache +
                ", numCacheLevels=" + numCacheLevels +
                ", cacheSizes=" + Arrays.toString(cacheSizes) +
                ", associativities=" + Arrays.toString(associativities) +
                ", cacheLineSize=" + cacheLineSize +
                ", cacheHits=" + cacheHits +
                ", cacheMisses=" + cacheMisses +
                 //", memory=" + memory +
                '}';
        }*/
}