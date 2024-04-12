package test;

import static org.junit.Assert.*;
import org.junit.Test;
import memory.Memory;
import utility.InstructionLoader;

public class InstructionLoaderTest {

    @Test
    public void testLoadProgram() {
        // Kreirajte novi objekat Memory
        Memory memory = new Memory();
        // Putanja do datoteke sa instrukcijama
        String filePath = "D:\\IdeaProjects\\ARproject1\\src\\utility\\Instructions.txt"; // Zamijenite ovo sa stvarnom putanjom do vaše datoteke sa instrukcijama

        // Učitajte program u memoriju pomoću InstructionLoader
        InstructionLoader.loadProgram(memory, filePath);

        // Adresa u memoriji gde se očekuje prva instrukcija
        long addressOfFirstInstruction = 0;
        // Očekivane instrukcije kao niz bajtova (možete ih hardkodirati ili generisati dinamički)
        byte[] expectedInstructions = { /* Niz bajtova koji predstavljaju očekivane instrukcije */ };
        // Prikupite instrukcije iz memorije
        byte[] actualInstructions = new byte[expectedInstructions.length];
        for (int i = 0; i < expectedInstructions.length; i++) {
            actualInstructions[i] = memory.readFromVirtualAddress(addressOfFirstInstruction + i);
        }
        // Provera da li su dobijene instrukcije identične očekivanim instrukcijama
        assertArrayEquals(expectedInstructions, actualInstructions);
    }
}