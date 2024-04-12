package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import memory.*;
import cache.*;
import processor.*;

import java.util.Scanner;

class ProcessorTest {
    private Processor processor;
    Memory memory = new Memory();
    Cache cache = new Cache(memory, 3, new int[]{32 * 1024, 512 * 1024, 32 * 1024 * 1024}, new int[]{4, 8, 16}, 64);

    @BeforeEach
    void setUp() {
        // Postavljamo procesor prije svakog testa
        processor = new Processor(memory, cache);
    }

    @Test
    void testAdd() {
        processor.add(0, 1, 2);
        assertEquals(0, processor.getGeneralPurposeRegisterValue(0));
        assertEquals(0, processor.getGeneralPurposeRegisterValue(1));
        assertEquals(0, processor.getGeneralPurposeRegisterValue(2));
    }

    @Test
    void testSub() {
        processor.sub(0, 1, 2);
        assertEquals(0, processor.getGeneralPurposeRegisterValue(0));
        assertEquals(0, processor.getGeneralPurposeRegisterValue(1));
        assertEquals(0, processor.getGeneralPurposeRegisterValue(2));
    }

    @Test
    void testMul() {
        processor.mul(0, 1, 2);
        assertEquals(0, processor.getGeneralPurposeRegisterValue(0));
        assertEquals(0, processor.getGeneralPurposeRegisterValue(1));
        assertEquals(0, processor.getGeneralPurposeRegisterValue(2));
    }

    @Test
    void testDiv() {
        // Testiranje dijeljenja kada je djelitelj različit od nule
        processor.div(0, 1, 2);
        assertEquals(0, processor.getGeneralPurposeRegisterValue(0)); // Provjeravamo rezultat dijeljenja

        // Testiranje dijeljenja kada je djelitelj jednak nuli
        // assertThrows(ArithmeticException.class, () -> processor.div(0, 1, 0));
        // Ovaj test bi trebao baciti iznimku jer se dijeli s nulom
    }

    @Test
    void testAnd() {
        // Testiranje logičkog AND operacije
        processor.and(0, 1, 2);
        assertEquals(0, processor.getGeneralPurposeRegisterValue(0)); // Očekujemo rezultat operacije
    }

    @Test
    void testOr() {
        // Testiranje logičkog OR operacije
        processor.or(0, 1, 2);
        assertEquals(0, processor.getGeneralPurposeRegisterValue(0)); // Očekujemo rezultat operacije
    }

    @Test
    void testXor() {
        // Testiranje logičkog XOR operacije
        processor.xor(0, 1, 2);
        assertEquals(0, processor.getGeneralPurposeRegisterValue(0)); // Očekujemo rezultat operacije
    }

    @Test
    void testNot() {
        // Testiranje logičke NOT operacije
        processor.not(0, 1);
        assertEquals(-1, processor.getGeneralPurposeRegisterValue(0)); // Očekujemo rezultat operacije
    }

    @Test
    void testMov() {
        // Testiranje premještanja vrijednosti iz jednog registra u drugi
        processor.mov(0, 1); // Premještanje vrijednosti registra 1 u registar 0
        assertEquals(processor.getGeneralPurposeRegisterValue(1), processor.getGeneralPurposeRegisterValue(0));
    }

    @Test
    void testMovFromRamDirect() {
        // Postavljamo očekivane vrijednosti
        long address = 1000;
        long targetAddress = 2000;
        byte dataToMove = 123;
        int destRegister = 0;

        // Postavljamo adresu u kešu i podatak u memoriju
        cache.writeToCache(address, (byte) targetAddress);
        memory.writeToVirtualAddress((byte) targetAddress, dataToMove);

        // Pozivamo metodu koju testiramo
        processor.movFromRam(destRegister, address, true);

        // Provjeravamo da li je pravilno prenesen podatak u registar
        assertEquals(dataToMove, processor.getGeneralPurposeRegisterValue(destRegister));
    }

    @Test
    void testMovFromRamIndirect() {
        // Postavljamo očekivane vrijednosti
        long address = 1000;
        byte dataToMove = 0;
        int destRegister = 0;

        // Postavljamo podatak u memoriju
        memory.writeToVirtualAddress((byte) address, dataToMove);

        // Pozivamo metodu koju testiramo
        processor.movFromRam(destRegister, address, false);

        // Provjeravamo da li je pravilno prenesen podatak u registar
        assertEquals(dataToMove, processor.getGeneralPurposeRegisterValue(destRegister));
    }

    @Test
    void testMovToRamIndirect() {
        // Postavljamo potrebne vrijednosti
        int destRegisterIndex = 0;
        long memoryAddress = 1000;
        boolean indirect = true;

        // Postavljamo podatak u odredišni registar
        long dataToMove = 123;
        processor.generalPurposeRegisters[destRegisterIndex] = dataToMove;

        // Pozivamo metodu koju testiramo
        processor.movToRam(destRegisterIndex, memoryAddress, indirect);

        // Provjeravamo jesu li podaci pravilno preneseni u memoriju
        for (int i = 0; i < Long.BYTES; i++) {
            long expectedData = (dataToMove >>> (i * Byte.SIZE)) & 0xFF;
            assertEquals(expectedData, memory.readFromVirtualAddress(memoryAddress + i));
        }
    }

    @Test
    void testFetchInstructionFromMemory() {
        // Postavljamo potrebne vrijednosti
        long instructionAddress = 1000;
        byte expectedInstruction = 42;

        // Postavljamo instrukciju u memoriju
        memory.writeToVirtualAddress(instructionAddress, expectedInstruction);

        // Pozivamo metodu koju testiramo
        byte fetchedInstruction = (byte)processor.fetchInstructionFromMemory(instructionAddress);

        // Provjeravamo jesu li instrukcije pravilno prenesene
        assertEquals(expectedInstruction, fetchedInstruction);
    }

}
