package emulator;

import processor.Processor;
import cache.Cache;
import memory.Memory;
import utility.InstructionLoader;

import java.io.File;

public class Emulator {

    private static void runEmulation(Processor processor) {
        while (processor.isRunning()) {
            // Dohvati instrukciju iz memorije
            long instruction = processor.fetchInstructionFromMemory(processor.getProgramCounter());

            // Dekodiraj instrukciju
            long decodedInstruction = processor.decodeInstruction(instruction);

            // Izvrši dekodiranu instrukciju
            processor.executeInstruction(decodedInstruction);

            // Ispisi trenutno stanje procesora
            processor.printProcessorState();
        }
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java YourMainClass <path_to_instruction_file>");
            System.exit(1);
        }

        String filePath = args[0];

        Memory memory = new Memory();
        InstructionLoader.loadProgram(memory, /*"C:" + File.separator + "Users" + File.separator + "AT95" + File.separator + "IdeaProjects" + File.separator + "ARproject1" + File.separator + "src" + File.separator + "utility" + File.separator + "instructions.txt"*/filePath);
        // Parametri za keš: 3 nivoa, veličine keša, asocijativnost, veličina linije
        int numCacheLevels = 3;
        int[] cacheSizes = {32 * 1024, 512 * 1024, 32 * 1024 * 1024};
        int[] associativities = {4, 8, 16}; // Primer vrednosti asocijativnosti za svaki nivo
        int cacheLineSize = 64; // Primer vrednosti veličine linije

        // Pravimo instancu keša sa konkretnim parametrima
        Cache cache = new Cache(memory, numCacheLevels, cacheSizes, associativities, cacheLineSize);
        Processor processor = new Processor(memory, cache);
        runEmulation(processor);
    }
}
