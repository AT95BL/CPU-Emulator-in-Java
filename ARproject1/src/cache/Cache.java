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

   /*
   *    Cache line je osnovna jedinica podataka koja se čuva u kešu.
   *    To je kontinuirani blok memorije unutar keša koji se koristi za čuvanje podataka koji su nedavno pristupani
   *    ili se očekuje da će uskoro biti pristupljeni.
   *    Cache line veličina određuje koliko podataka se može čuvati u jednoj liniji keša.
   */
    private static final int CACHE_LINE_SIZE = 64; // 64 bytes
    // Cache levels                                                             --from the old systems version
    // private Map<Long, CacheLevel> l1Cache = new LruCache<>(L1_CACHE_SIZE);   --from the old systems version
    // private Map<Long, CacheLevel> l2Cache = new LruCache<>(L2_CACHE_SIZE);   --from the old systems version
    // private Map<Long, CacheLevel> l3Cache = new LruCache<>(L3_CACHE_SIZE);   --from the old systems version

    // Lista cacheLevels predstavlja hijerarhiju keš nivoa.
    // Svaki keš nivo je predstavljen mapom gde su ključevi adrese (tipa Long) i vrednosti CacheLevel objekti.
    // Ova struktura omogućava organizaciju i efikasno upravljanje keširanim podacima na različitim nivoima keš memorije.
    // Korišćenje liste omogućava lako proširivanje sistema dodavanjem novih keš nivoa.
    private List<Map<Long, CacheLevel>> cacheLevels = new ArrayList<>();    // index-cachelevel 0-L1,   1-L2,   2-L3

    // Cache levels and sizes
    private int numCacheLevels;        // Number of cache levels: 1, 2 or 3 ..
    private int[] cacheSizes;          // Cache sizes for each level

    /*
        associativities kesa se odnosi na koncept koji opisuje koliko linija keša može držati podatke koji se
        mapiraju na isti indeks.

        Ovaj koncept je povezan sa načinom na koji se podaci smeštaju u keš,
        odnosno kako se pristupa podacima koji se nalaze na istom mestu unutar keša.

        U opštem smislu, asocijativnost kesa se može podeliti na tri glavne vrste:

    *   Direktno mapiranje (Direct Mapped):
            Svaka linija keša je mapirana na jedno određeno mesto u kešu.
    *       To znači da svaka adresa iz memorije može biti smeštena samo u jednu određenu liniju u kešu.

    *   Asocijativno mapiranje (Associative Mapping):
            Svaka adresa iz memorije može biti smeštena u bilo koju liniju keša.
    *       Ovde postoji sloboda u tome gde će biti smešteni podaci iz memorije koji se mapiraju na isti indeks.

    *   Set-Associative Mapping:
            Ovo je hibrid između direktnog mapiranja i asocijativnog mapiranja.
    *       Keš je podeljen na skupove (sets), a svaki set sadrži određeni broj linija keša.

    *   Svaka adresa iz memorije može biti smeštena u bilo koju liniju unutar određenog seta.
    *   U slučaju vašeg koda, associativities je niz koji sadrži informacije o tome koliko je svaki nivo keša asocijativan.
    *   Na primer, ako je asocijativnost za L1, L2 i L3 postavljena na 2, 4 i 8 redom,
    *   to znači da su ovi nivoi keša dvostruko, četvorostruko, odnosno osmostruko asocijativni.
    *   Ovo određuje koliko linija keša može držati podatke koji se mapiraju na isti indeks.
    *   Veća asocijativnost obično dovodi do manje verovatnoće sukoba (conflict) i bolje iskorišćenosti keša,
    *   ali takođe zahteva više resursa.
    * */
    private int[] associativities;
    private int cacheLineSize;         // Cache line size
    private int cacheHits = 0;          //  counter for the number of cache hits
    private int cacheMisses = 0;        //  counter for the number of cache misses
    private Memory memory;              // Reference to the Memory instance

    // Constructor to initialize Memory instance --lakrdija koju treba eliminisati!!
    public Cache(Memory memory) {
        this.memory = memory;
        initializeCaches();
    }

    //  Constructor
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

    /*
     Ova metoda initializeCaches() ima zadatak da inicijalizuje nivoe keša na osnovu specificiranih parametara.
     Za svaki nivo keša, kreira keš određene veličine i asocijativnosti, inicijalizuje ga sa  linijama keša i dodaje ga na listu keš nivoa.
     Iteracija kroz nivoe keša:
     Petlja for prolazi kroz svaki nivo keša.
     Dohvatanje veličine i asocijativnosti:
     Za svaki nivo keša, dohvaćaju se veličina keša i asocijativnost iz odgovarajućih nizova cacheSizes i associativities.
     Kreiranje novog keša: Na osnovu veličine keša,
     kreira se novi keš koristeći LRU strategiju zamene (Least Recently Used) kako bi se osiguralo optimalno upravljanje kešom.
     Inicijalizacija nivoa keša: Poziva se metoda initializeCacheLevel() kako bi se inicijalizovao nivo keša sa linijama keša.
     Dodavanje keša u listu: Inicijalizovani keš se dodaje u listu cacheLevels koja sadrži sve nivoe keša.
    */
    /**
     * Initializes the cache levels based on the specified parameters.
     * For each cache level, creates a cache with the given size and associativity,
     * initializes it with cache lines, and adds it to the list of cache levels.
     */
    private void initializeCaches() {
        // Iterate over each cache level
        for (int i = 0; i < numCacheLevels; i++) {
            // Retrieve cache size and associativity for the current level
            int cacheSize = cacheSizes[i];
            int associativity = associativities[i];

            // Create a new LRU cache with the specified size for the current level
            Map<Long, CacheLevel> cache = new LruCache<>(cacheSize);

            // Initialize the cache level with cache lines
            initializeCacheLevel(cache, cacheSize, associativity);

            // Add the initialized cache to the list of cache levels
            cacheLevels.add(cache);
        }
    }

    /**
     * Initializes a cache level with cache lines based on the specified cache size and associativity.
     * Creates cache lines within the cache map using sequential indices as keys.
     *
     * @param cache         The cache map to initialize.
     * @param cacheSize     The size of the cache level in bytes.
     * @param associativity The associativity of the cache level, indicating how many cache lines can hold data mapped to the same index.
     */
    private void initializeCacheLevel(Map<Long, CacheLevel> cache, int cacheSize, int associativity) {
        // Calculate the number of cache lines based on the cache size and line size
        int numCacheLines = cacheSize / CACHE_LINE_SIZE;

        // Create cache lines within the cache map using sequential indices as keys
        for (long j = 0; j < numCacheLines; j++) {
            // Each cache line is represented by an instance of CacheLevel
            cache.put(j, new CacheLevel());
        }
    }

    /**
     * Reads data from the cache at the specified address.
     * If the data is present in the cache (cache hit), it is returned.
     * If the data is not present in the cache (cache miss), it is read from RAM,
     * updated in the cache, and then returned.
     *
     * @param address The memory address to read from.
     * @return The data read from the cache or RAM.
     */
    public byte readFromCache(long address) {
        // Get the cache level corresponding to the address
        CacheLevel cacheLevel = getCacheLevel(address);

        // Check if the cache level is not null (cache hit)
        if (cacheLevel != null) {
            try {
                // Read data from the cache level
                byte data = cacheLevel.read(address);

                // If data is read from the cache (cache hit)
                if (data != 0) {
                    cacheHits++; // Increment cache hits counter
                    System.out.println(cacheHits); // Print cache hits count
                } else {
                    System.out.println(cacheMisses); // Print cache misses count
                }

                // Print cache hit message with the retrieved data
                System.out.println("Cache Hit!! Data: " + data);
                return data; // Return the data read from the cache
            } catch (IllegalStateException e) {
                // Handle any exceptions thrown during cache read
                System.err.println("Exception caught: " + e.getMessage());
                return 0; // Return default value (0) if an exception occurs
            }
        }

        // If cache level is null (cache miss)
        System.out.println("cacheLevel = 0");
        cacheMisses++; // Increment cache misses counter

        // Read data from RAM
        byte[] dataFromRAM = readFromRAM(address);
        String str = "";
        // Convert the data read from RAM to string for printing
        for (byte s : dataFromRAM) {
            str += s;
            str += " ";
        }
        System.out.println("Cache Miss!! Data read From RAM: " + str); // Print cache miss message with data from RAM

        // Update caches with data read from RAM
        updateCaches(address, dataFromRAM);

        // Return the data read from RAM
        return dataFromRAM[getOffset(address)];
    }
    /*
    * Ova metoda readFromCache() čita podatke iz keša na određenoj adresi.
    * Ako su podaci prisutni u kešu (pogodak u kešu), oni se vraćaju.
    * Ako podaci nisu prisutni u kešu (promašaj u kešu), čitaju se iz RAM-a, a zatim se ažuriraju u kešu i vraćaju.
    * Parametar metode:
    * address: Adresa memorije s koje se čita.
    * Čitanje iz keša:
    * Dobija se nivo keša koji odgovara adresi.
    * Proverava se da li je nivo keša različit od null (pogodak u kešu).
    * Ako je nivo keša različit od null, podaci se čitaju iz tog nivoa.
    * Ako su podaci pročitani iz keša (pogodak u kešu),
    * brojač pogodaka keša se povećava, a zatim se prikazuju informacije o pogotku keša i vraćaju se pročitani podaci.
    * Ako dođe do izuzetka prilikom čitanja iz keša, hvata se izuzetak i vraća se podrazumevana vrednost (0).
    * Promašaj u kešu:
    * Ako je nivo keša jednak null (promašaj u kešu), povećava se brojač promašaja keša.
    * Podaci se čitaju iz RAM-a na osnovu adrese.
    * Prikazuju se informacije o promašaju keša i podaci se ažuriraju u kešu sa podacima pročitanim iz RAM-a.
    * Vraća se pročitani podatak iz RAM-a.
    * */

    /**
     * Writes data to the cache at the specified address.
     * If the data is present in the cache (cache hit), it is updated with the new data.
     * If the data is not present in the cache (cache miss), it is written to RAM and then updated in the cache.
     *
     * @param address The memory address to write to.
     * @param data    The data to write.
     */
    public void writeToCache(long address, byte data) {
        System.out.println("Write to Cache <address,data>: " + "<" + address + "," + data + ">"); // Print message indicating write to cache

        // Get the cache level corresponding to the address
        CacheLevel cacheLevel = getCacheLevel(address);

        // Check if the cache level is not null (cache hit)
        if (cacheLevel != null) {
            System.out.println("cacheLevel != 0"); // Print message indicating cache hit
            System.out.println("Cache Hit!! Data: " + data); // Print message indicating cache hit with data
            cacheLevel.write(address, data); // Write data to the cache level
            cacheHits++; // Increment cache hits counter
        } else {
            // If cache level is null (cache miss), print message indicating cache miss
            System.out.println("cacheLevel = 0");

            // Write data to RAM
            writeToRAM(address, data);

            // Update caches with data written to RAM
            updateCaches(address, new byte[]{data});

            // Increment both cache hits and misses counters
            cacheHits++;
            cacheMisses++;
        }
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

    public void updateCaches(long address, byte[] data) {
        System.out.println("Method: updateCaches");
        // Assuming LRU strategy for simplicity
        CacheLevel cacheLevel = getCacheLevel(address);
        if (cacheLevel != null) {
            cacheLevel.write(address, data[0]);
            cacheHits++; // Ovde je bio problem, jer se cacheHits povećavao iako je došlo do promašaja u kešu
        } else {
            cacheMisses++; // Ovde je ispravno povećati brojač promašaja u kešu
        }
    }

    /*
    private void updateCaches(long address, byte[] data) {
        System.out.println("Method: updateCaches");
        // Assuming LRU strategy for simplicity
        CacheLevel cacheLevel = getCacheLevel(address);
        if (cacheLevel != null) {
            cacheLevel.write(address, data[0]);
            cacheHits++;
        }
    }
    */

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

    public int getCacheHits(){
        return this.cacheHits;
    }

    public int getCacheMisses(){
        return this.cacheMisses;
    }
}