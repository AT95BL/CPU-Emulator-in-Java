package test;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import processor.Processor;
import cache.Cache;
import memory.Memory;

public class ProcessorTestCMP {

    @Test
    public void testCmpLTF() {
        Memory mockMemory = new Memory(); // Zamijenite ovde sa vašim stvarnim objektom za memoriju
        Cache mockCache = new Cache(mockMemory); // Zamijenite ovde sa vašim stvarnim objektom za keš
        Processor processor = new Processor(mockMemory, mockCache);

        int register1 = 0; // Zamijenite sa indeksom vašeg registra
        int register2 = 1; // Zamijenite sa indeksom drugog vašeg registra

        // Postavite vrednosti registara za test
        processor.generalPurposeRegisters[register1] = 5;
        processor.generalPurposeRegisters[register2] = 10;

        // Pozovite cmp metodu
        processor.cmp(register1, register2);

        // Očekujemo da su postavljeni odgovarajući statusni flagovi
        //assertTrue(processor.isZeroFlag());
        // assertTrue(processor.isGreaterThanFlag());
        // Ako je vrednost u register1 bila manja od vrednosti u register2, očekujemo i da je lessThanFlag postavljen
        assertTrue(processor.isLessThanFlag());
    }

    @Test
    public void testCmpEQUAL() {
        Memory mockMemory = new Memory(); // Zamijenite ovde sa vašim stvarnim objektom za memoriju
        Cache mockCache = new Cache(mockMemory); // Zamijenite ovde sa vašim stvarnim objektom za keš
        Processor processor = new Processor(mockMemory, mockCache);

        int register1 = 0; // Zamijenite sa indeksom vašeg registra
        int register2 = 1; // Zamijenite sa indeksom drugog vašeg registra

        // Postavite vrednosti registara za test
        processor.generalPurposeRegisters[register1] = 15;
        processor.generalPurposeRegisters[register2] = 15;

        // Pozovite cmp metodu
        processor.cmp(register1, register2);

        // Očekujemo da su postavljeni odgovarajući statusni flagovi
           assertTrue(processor.isZeroFlag());
        // assertTrue(processor.isGreaterThanFlag());
        // Ako je vrednost u register1 bila manja od vrednosti u register2, očekujemo i da je lessThanFlag postavljen
        // assertTrue(processor.isLessThanFlag());
    }


    @Test
    public void testCmpGTF() {
        Memory mockMemory = new Memory(); // Zamijenite ovde sa vašim stvarnim objektom za memoriju
        Cache mockCache = new Cache(mockMemory); // Zamijenite ovde sa vašim stvarnim objektom za keš
        Processor processor = new Processor(mockMemory, mockCache);

        int register1 = 0; // Zamijenite sa indeksom vašeg registra
        int register2 = 1; // Zamijenite sa indeksom drugog vašeg registra

        // Postavite vrednosti registara za test
        processor.generalPurposeRegisters[register1] = 10;
        processor.generalPurposeRegisters[register2] = 5;

        // Pozovite cmp metodu
        processor.cmp(register1, register2);

        // Očekujemo da su postavljeni odgovarajući statusni flagovi
        //assertTrue(processor.isZeroFlag());
        assertTrue(processor.isGreaterThanFlag());
        // Ako je vrednost u register1 bila manja od vrednosti u register2, očekujemo i da je lessThanFlag postavljen
        //assertTrue(processor.isLessThanFlag());
    }
}

