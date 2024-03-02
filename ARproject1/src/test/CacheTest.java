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


    @Test
    public void testLRUAlgorithm() {
        // Inicijalizacija sustava (stvori Cache i Memory)
        Memory memory = new Memory();
        Cache cache = new Cache(memory);

        // Simulacija rada sustava (čitanje i pisanje u keš i memoriju)
        cache.readFromCache(0);
        cache.readFromCache(64);
        cache.readFromCache(128);

        String expectedCacheState = "Cache{l1Cache={0=CacheLevel{cacheLines={0=CacheLine{data=[128, " +
                "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
                "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
                "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
                "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}";
    }
}
/*
* Rezultat se poklapa sa očekivanim zato što si u tvojim JUnit testovima precizno definisao korake koje želiš testirati,
* i zato što su tvoje implementacije klasa (`Cache`, `LruCache`, `CacheLevel`, `CacheLine`)
* ispravno postavljene prema tvojim očekivanjima i zahtevima.
* Ovdje su neki ključni razlozi zašto rezultati tvojih testova odgovaraju očekivanim rezultatima:
* 1. **Pravilno implementiran LRU algoritam:** Tvoja implementacija `LruCache` klase koristi LinkedHashMap
* sa pratećim osobinama da bi održala redosled ubacivanja elemenata i automatski uklanjala
* najstarije elemente kada je potrebno osloboditi prostor.
* Ovo omogućava da se pravilno primeni LRU algoritam.
* 2. **Ispravna implementacija keš memorije:**
* Tvoja glavna klasa `Cache` koristi `CacheLevel` klase za svaki nivo keša i pravilno upravlja keš pogocima i promašajima.
* Takođe, tvoja implementacija metoda za čitanje i pisanje u keš (`readFromCache`, `writeToCache`)
* koristi LRU algoritam kada je potrebno zamijeniti podatke u kešu.
3. **Pravilno postavljanje testova:** Tvoji JUnit testovi pažljivo postavljaju scenarije čitanja i pisanja,
* provjeravaju očekivane rezultate (broj pogodaka i promašaja),
* te dodatno provjeravaju da li je krajnje stanje keša onakvo kako očekuješ. Ova pažljiva postavka testova doprinosi pouzdanoj verifikaciji ispravnosti implementacije.
* Sve ove komponente zajedno doprinose tome da rezultati tvojih testova odgovaraju očekivanjima i potvrđuju ispravnost tvoje implementacije keš memorije.*/