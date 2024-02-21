package test;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import processor.Processor;
import memory.Memory;
import cache.Cache;

public class ProcessorTestJMP {

    @Test
    public void testJmp() {
        // Inicijalizacija procesora
        Memory memory = new Memory();  // Prilagodite prema vašoj implementaciji
        Cache cache = new Cache(memory);  // Prilagodite prema vašoj implementaciji
        Processor processor = new Processor(memory, cache);

        // Postavite programski brojač i izvedite jmp
        long targetAddress = 1000;  // Prilagodite prema vašim potrebama
        processor.jmp(targetAddress, false);

        // Provera rezultata
        assertEquals(targetAddress, processor.getProgramCounter());
    }

}