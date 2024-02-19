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

            // Ispisi trenutno stanje procesora (možeš zakomentarisati ili izbaciti ovo ako ne želiš stalno ispisivanje)
            processor.printProcessorState();
        }
    }

    public static void main(String[] args) {
        Memory memory = new Memory();
        InstructionLoader.loadProgram(memory, "C:" + File.separator + "Users" + File.separator + "AT95" + File.separator + "IdeaProjects" + File.separator + "ARproject1" + File.separator + "src" + File.separator + "utility" + File.separator + "instructions.txt");
        Cache cache = new Cache(memory);
        Processor processor = new Processor(memory, cache);
    }
}
