package emulator;

import processor.Processor;
public class Emulator {
    // Main method
    public static void main(String[] args) {

        Processor processor = new Processor();

        System.out.println("Hardcode Testiranje..par demonstrativnih primjera: \n");
        // Example usage
        processor.mov(0, 10); // Move 10 to register 0
        processor.mov(1, 5);  // Move 5 to register 1
        processor.add(2, 0);  // Add register 0 to register 2
        processor.sub(3, 1);  // Subtract register 1 from register 3
        processor.mul(0, 2);  // Multiply register 2 by register 0
        processor.div(1, 3);  // Divide register 3 by register 1
        processor.and(2, 0);  // Bitwise AND register 0 with register 2
        processor.or(3, 1);   // Bitwise OR register 1 with register 3
        processor.not(0);     // Bitwise NOT register 0
        processor.xor(1, 2);  // Bitwise XOR register 2 with register 1
        processor.jmp(100);   // Jump to address 100

        // Load program from a file
        System.out.println("Testiranje mogucnosti da se procesor izvrsava kao emulator: \n");
        processor.loadProgramFromFile(processor, "C:\\Users\\AT95\\IdeaProjects\\ARproject1\\src\\processor\\sample_program.txt");

        System.out.println("Pri pokretanju emulatora, treba biti moguće konfigurisati: broj keš nivoa, veličine keša po nivoima, asocijativnost keša po nivoima i veličinu keš linije. \n");
        processor.configureCacheManually();

        /*
        * int numCacheLevels = 3;

        // Configuration for Cache Level 1
        int cacheSizeL1 = 512; // in kilobytes
        int associativityL1 = 2;
        int cacheLineSizeL1 = 64; // in bytes

        // Configuration for Cache Level 2
        int cacheSizeL2 = 2048; // in kilobytes
        int associativityL2 = 4;
        int cacheLineSizeL2 = 64; // in bytes

        // Configuration for Cache Level 3
        int cacheSizeL3 = 8192; // in kilobytes
        int associativityL3 = 8;
        int cacheLineSizeL3 = 64; // in bytes
        *
        * */

        // Start the processor execution loop
        processor.run();
    }
}
