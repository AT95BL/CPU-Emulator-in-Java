package processor;

import cache.Cache;
import memory.Memory;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Processor {

    private static final int NUM_GENERAL_PURPOSE_REGISTERS = 4;                         // u skladu sa tekstom zadatka..
    private long[] generalPurposeRegisters = new long[NUM_GENERAL_PURPOSE_REGISTERS];   // sizeof(long) = 8[B]
    private long programCounter = 0;                                                    //  indeksiranje..
    private Memory memory = new Memory();   //  radna memorija..
    private Cache cache = new Cache(memory);      //  cache memorija..
    private boolean isRunning = true;       //  halt yes/no ..

    // Arithmetical Operations: add, sub, mul, div
    public void add(int destReg, int srcReg) {
        generalPurposeRegisters[destReg] += generalPurposeRegisters[srcReg];
    }
    public void sub(int destReg, int srcReg) {
        generalPurposeRegisters[destReg] -= generalPurposeRegisters[srcReg];
    }
    public void mul(int destReg, int srcReg) {
        generalPurposeRegisters[destReg] *= generalPurposeRegisters[srcReg];
    }
    public void div(int destReg, int srcReg) {
        if (generalPurposeRegisters[srcReg] != 0) {
            generalPurposeRegisters[destReg] /= generalPurposeRegisters[srcReg];
        } else {
            halt();
        }
    }

    // Logical Operations: and, or, not, xor, mov, mov
    public void and(int destReg, int srcReg) {
        generalPurposeRegisters[destReg] &= generalPurposeRegisters[srcReg];
    }
    public void or(int destReg, int srcReg) {
        generalPurposeRegisters[destReg] |= generalPurposeRegisters[srcReg];
    }
    public void not(int destReg) {
        generalPurposeRegisters[destReg] = ~generalPurposeRegisters[destReg];
    }
    public void xor(int destReg, int srcReg) {
        generalPurposeRegisters[destReg] ^= generalPurposeRegisters[srcReg];
    }

    // Data Transfer Instructions
   public void mov(int destRegisterIndex, long value) {
        if (destRegisterIndex >= 0 && destRegisterIndex < NUM_GENERAL_PURPOSE_REGISTERS) {
            generalPurposeRegisters[destRegisterIndex] = value;
        } else {
            halt();
        }
    }

    public void mov(int destRegisterIndex, int sourceRegisterIndex) {
        if (destRegisterIndex >= 0 && destRegisterIndex < NUM_GENERAL_PURPOSE_REGISTERS &&
                sourceRegisterIndex >= 0 && sourceRegisterIndex < NUM_GENERAL_PURPOSE_REGISTERS) {
            generalPurposeRegisters[destRegisterIndex] = generalPurposeRegisters[sourceRegisterIndex];
        } else {
            halt();
        }
    }

    // Branching Instructions: jump, jump-equal, jump-not-euqal, jump greater-less, jump-less

    public void jmp(long targetAddress) {programCounter = targetAddress;}

    public void je(long targetAddress, int srcReg1, int srcReg2) {
        if (generalPurposeRegisters[srcReg1] == generalPurposeRegisters[srcReg2]) {
            programCounter = targetAddress;
        }
    }

    public void jne(long targetAddress, int srcReg1, int srcReg2) {
        if (generalPurposeRegisters[srcReg1] != generalPurposeRegisters[srcReg2]) {
            programCounter = targetAddress;
        }
    }

    public void jge(long targetAddress, int srcReg1, int srcReg2) {
        if (generalPurposeRegisters[srcReg1] >= generalPurposeRegisters[srcReg2]) {
            programCounter = targetAddress;
        }
    }

    public void jl(long targetAddress, int srcReg1, int srcReg2) {
        if (generalPurposeRegisters[srcReg1] < generalPurposeRegisters[srcReg2]) {
            programCounter = targetAddress;
        }
    }

    // Comparison
    private boolean zeroFlag;
    private boolean greaterThanFlag;
    private boolean lessThanFlag;

    public void setFlags(boolean zero, boolean greaterThan, boolean lessThan) {
        this.zeroFlag = zero;
        this.greaterThanFlag = greaterThan;
        this.lessThanFlag = lessThan;
    }

    public void cmp(int registerIndex1, int registerIndex2) {
        long value1 = generalPurposeRegisters[registerIndex1];
        long value2 = generalPurposeRegisters[registerIndex2];
        // Set flags based on the comparison result
        if (value1 == value2) {
            setFlags(true, false, false);  // Set zero flag
        } else if (value1 > value2) {
            setFlags(false, true, false);  // Set greater-than flag
        } else {
            setFlags(false, false, true);  // Set less-than flag
        }
    }

    // I/O Routines
    public void readFromKeyboard(int registerIndex) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a value: ");
        long value = scanner.nextLong();
        if (value < 0 || value >= NUM_GENERAL_PURPOSE_REGISTERS) {
            halt();
        }
        generalPurposeRegisters[registerIndex] = value;
    }

    public void writeToScreen(int registerIndex) {
        System.out.println("Value in register " + registerIndex + ": " + generalPurposeRegisters[registerIndex]);
    }

    // Halt Instruction
    public void halt() {
        isRunning = false;
        System.out.println("HALT instruction encountered. Emulation halted.");
    }

    // Method to read from memory (using cache)
    public byte readFromMemory(long address) {
        return cache.readFromCache(address);
    }

    // Method to write to memory (using cache)
    public void writeToMemory(long address, byte data) {
        cache.writeToCache(address, data);
    }

    // Method to read from a virtual address
    public byte readFromVirtualAddress(long virtualAddress) {
        long physicalAddress = translateVirtualToPhysical(virtualAddress);
        return readFromMemory(physicalAddress);
    }

    // Method to write to a virtual address
    public void writeToVirtualAddress(long virtualAddress, byte data) {
        long physicalAddress = translateVirtualToPhysical(virtualAddress);
        writeToMemory(physicalAddress, data);
    }

    // Method to translate virtual address to physical address
    public long translateVirtualToPhysical(long virtualAddress) {
        return memory.translateVirtualToPhysical(virtualAddress).getIndex();
    }

    // Method to get the offset from a virtual address
    public int getOffset(long virtualAddress) {
        return memory.getOffset(virtualAddress);
    }

    // Method to get the cache level based on the address
    public Cache.CacheLevel getCacheLevel(long address) {
        return cache.getCacheLevel(address);
    }

    // Method to print the processor state
    private void printProcessorState() {
        System.out.println("======= Processor State =======");
        System.out.println("Program Counter (PC): " + programCounter);
        System.out.println("General Purpose Registers:");
        for (int i = 0; i < NUM_GENERAL_PURPOSE_REGISTERS; i++) {
            System.out.println("R" + i + ": " + generalPurposeRegisters[i]);
        }
        System.out.println("Zero Flag: " + zeroFlag);
        System.out.println("Greater Than Flag: " + greaterThanFlag);
        System.out.println("Less Than Flag: " + lessThanFlag);
        System.out.println("===============================");
    }

    // Main execution loop
    public void run() {
        while (isRunning) {
            // Fetch instruction from memory at the current program counter
            long instruction = readFromMemory(programCounter);
            // Decode and execute the instruction
            executeInstruction(instruction);
            // Increment program counter to point to the next instruction
            programCounter++;
            try{
                Thread.sleep(100);
            }catch(InterruptedException ex){
                ex.printStackTrace();
                halt();
            }
        }
        // Print processor state after each instruction
        printProcessorState();
    }

    // Method to execute an instruction
    public void executeInstruction(long instruction) {
        // Decode the instruction and perform the corresponding operation
        int opcode = getOpcode(instruction);

        switch (opcode) {
            case ADD_OPCODE:
                System.out.println("ADD_OPCODE");
                add(getDestReg(instruction), getSrcReg(instruction));
                break;
            case SUB_OPCODE:
                System.out.println("SUB_OPCODE");
                sub(getDestReg(instruction), getSrcReg(instruction));
                break;
            case MUL_OPCODE:
                System.out.println("MUL_OPCODE");
                mul(getDestReg(instruction), getSrcReg(instruction));
                break;
            case DIV_OPCODE:
                System.out.println("DIV_OPCODE");
                div(getDestReg(instruction), getSrcReg(instruction));
                break;
            case AND_OPCODE:
                System.out.println("AND_OPCODE");
                and(getDestReg(instruction), getSrcReg(instruction));
                break;
            case OR_OPCODE:
                System.out.println("OR_OPCODE");
                or(getDestReg(instruction), getSrcReg(instruction));
                break;
            case NOT_OPCODE:
                System.out.println("NOT_OPCODE");
                not(getDestReg(instruction));
                break;
            case XOR_OPCODE:
                System.out.println("XOR_OPCODE");
                xor(getDestReg(instruction), getSrcReg(instruction));
                break;
            case MOV_VALUE_OPCODE:
                System.out.println("MOV_VALUE_OPCODE");
                mov(getDestReg(instruction), getImmediateValue(instruction));
                break;
            case MOV_REGISTER_OPCODE:
                System.out.println("MOV_REGISTER_OPCODE");
                mov(getDestReg(instruction), getSrcReg(instruction));
                break;
            case JMP_OPCODE:
                System.out.println("JMP_OPCODE");
                jmp(getTargetAddress(instruction));
                break;
            case JE_OPCODE:
                System.out.println("JE_OPCODE");
                je(getTargetAddress(instruction), getSrcReg(instruction), getSrcReg2(instruction));
                break;
            case JNE_OPCODE:
                System.out.println("JNE_OPCODE");
                jne(getTargetAddress(instruction), getSrcReg(instruction), getSrcReg2(instruction));
                break;
            case JGE_OPCODE:
                System.out.println("JGE_OPCODE");
                jge(getTargetAddress(instruction), getSrcReg(instruction), getSrcReg2(instruction));
                break;
            case JL_OPCODE:
                System.out.println("JL_OPCODE");
                jl(getTargetAddress(instruction), getSrcReg(instruction), getSrcReg2(instruction));
                break;
            case CMP_OPCODE:
                System.out.println("CMP_OPCODE");
                cmp(getSrcReg(instruction), getSrcReg2(instruction));
                break;
            case READ_KEYBOARD_OPCODE:
                System.out.println("READ_KEYBOARD_OPCODE");
                readFromKeyboard(getDestReg(instruction));
                break;
            case WRITE_SCREEN_OPCODE:
                System.out.println("WRITE_SCREEN_OPCODE");
                writeToScreen(getSrcReg(instruction));
                break;
            case HALT_OPCODE:
                halt();
                break;
            default:
                System.out.println("Unrecognized opcode..:( it's time to halt..");
                halt();
                break;
        }
        // Print processor state after each instruction
        printProcessorState();
    }

    // Helper methods to extract fields from the instruction
    private static final int OPCODE_SHIFT = 28; // Example shift for opcode bits
    private static final int OPCODE_MASK = 0xF; // Example mask for opcode bits

    private static final int DEST_REG_SHIFT = 24; // Example shift for destination register bits
    private static final int DEST_REG_MASK = 0xF; // Example mask for destination register bits

    private static final int SRC_REG_SHIFT = 20; // Example shift for source register bits
    private static final int SRC_REG_MASK = 0xF; // Example mask for source register bits

    private static final int IMMEDIATE_VALUE_SHIFT = 0; // Example shift for immediate value bits
    private static final long IMMEDIATE_VALUE_MASK = 0xFFFFFFFFL; // Example mask for immediate value bits

    private static final int TARGET_ADDRESS_SHIFT = 0; // Example shift for target address bits
    private static final long TARGET_ADDRESS_MASK = 0xFFFFFFFFL; // Example mask for target address bits

    private static final int SRC_REG2_SHIFT = 16; // Example shift for second source register bits
    private static final int SRC_REG2_MASK = 0xF; // Example mask for second source register bits

    public int getOpcode(long instruction) {
        return (int) ((instruction >> OPCODE_SHIFT) & OPCODE_MASK);
    }

    public int getDestReg(long instruction) {
        return (int) ((instruction >> DEST_REG_SHIFT) & DEST_REG_MASK);
    }
    public int getSrcReg(long instruction) {
        return (int) ((instruction >> SRC_REG_SHIFT) & SRC_REG_MASK);
    }

    public long getImmediateValue(long instruction) {
        return (instruction >> IMMEDIATE_VALUE_SHIFT) & IMMEDIATE_VALUE_MASK;
    }

    public long getTargetAddress(long instruction) {
        return (instruction >> TARGET_ADDRESS_SHIFT) & TARGET_ADDRESS_MASK;
    }
    public int getSrcReg2(long instruction) {
        return (int) ((instruction >> SRC_REG2_SHIFT) & SRC_REG2_MASK);
    }

    // Define opcode constants (replace these with your actual opcode values)
    private static final int ADD_OPCODE = 1;
    private static final int SUB_OPCODE = 2;
    private static final int MUL_OPCODE = 3;
    private static final int DIV_OPCODE = 4;
    private static final int AND_OPCODE = 5;
    private static final int OR_OPCODE = 6;
    private static final int NOT_OPCODE = 7;
    private static final int XOR_OPCODE = 8;
    private static final int MOV_VALUE_OPCODE = 9;
    private static final int MOV_REGISTER_OPCODE = 10;
    private static final int JMP_OPCODE = 11;
    private static final int JE_OPCODE = 12;
    private static final int JNE_OPCODE = 13;
    private static final int JGE_OPCODE = 14;
    private static final int JL_OPCODE = 15;
    private static final int CMP_OPCODE = 16;
    private static final int READ_KEYBOARD_OPCODE = 17;
    private static final int WRITE_SCREEN_OPCODE = 18;
    private static final int HALT_OPCODE = 19;

    public static void loadProgramFromFile(Processor processor, String filePath) {
        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);
            int lineNumber = 1; // Initialize line number counter
            while (scanner.hasNextLine()) {
                String instructionLine = scanner.nextLine().trim();
                // Skip empty lines
                if (instructionLine.isEmpty()) {
                    continue;
                }
                // Parse and load the instruction into memory
                parseAndLoadInstruction(processor, instructionLine, lineNumber);
                // Increment line number
                lineNumber++;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void parseAndLoadInstruction(Processor processor, String instructionLine, int lineNumber) {
        String[] parts = instructionLine.split("\\s+"); // Split by whitespace
        if (parts.length >= 2) {
            try {
                String opcode = parts[0].toUpperCase();
                int destReg = Integer.parseInt(parts[1]);

                // Check for comments and skip them
                if (opcode.startsWith(";")) {
                    return; // Skip comments
                }

                switch (opcode) {
                    case "ADD":
                        if (parts.length >= 3) {
                            System.out.println("ADD INSTRUCTION \n");
                            int srcReg = Integer.parseInt(parts[2]);
                            processor.add(destReg, srcReg);
                            processor.printProcessorState();
                        } else {
                            System.out.println("ADD without enough operands at line " + lineNumber);
                            processor.halt();
                        }
                        break;
                    case "SUB":
                        if (parts.length >= 3) {
                            System.out.println("SUB INSTRUCTION \n");
                            int srcReg = Integer.parseInt(parts[2]);
                            processor.sub(destReg, srcReg);
                            processor.printProcessorState();
                        } else {
                            System.out.println("SUB without enough operands at line " + lineNumber);
                            processor.halt();
                        }
                        break;
                    case "MUL":
                        if (parts.length >= 3) {
                            System.out.println("MUL INSTRUCTION \n");
                            int srcReg = Integer.parseInt(parts[2]);
                            processor.mul(destReg, srcReg);
                            processor.printProcessorState();
                        } else {
                            System.out.println("MUL without enough operands at line " + lineNumber);
                            processor.halt();
                        }
                        break;
                    case "DIV":
                        if (parts.length >= 3) {
                            System.out.println("DIV INSTRUCTION \n");
                            int srcReg = Integer.parseInt(parts[2]);
                            processor.div(destReg, srcReg);
                            processor.printProcessorState();
                        } else {
                            System.out.println("DIV without enough operands at line " + lineNumber);
                            processor.halt();
                        }
                        break;
                    case "AND":
                        if (parts.length >= 3) {
                            System.out.println("ADD INSTRUCTION  \n");
                            int srcReg = Integer.parseInt(parts[2]);
                            processor.and(destReg, srcReg);
                            processor.printProcessorState();
                        } else {
                            System.out.println("AND without enough operands at line " + lineNumber);
                            processor.halt();
                        }
                        break;
                    case "OR":
                        if (parts.length >= 3) {
                            System.out.println("OR INSTRUCTION  \n");
                            int srcReg = Integer.parseInt(parts[2]);
                            processor.or(destReg, srcReg);
                            processor.printProcessorState();
                        } else {
                            System.out.println("OR without enough operands at line " + lineNumber);
                            processor.halt();
                        }
                        break;
                    case "NOT":
                        System.out.println("NOT INSTRUCTION \n");
                        processor.not(destReg);
                        processor.printProcessorState();
                        break;
                    case "XOR":
                        if (parts.length >= 3) {
                            System.out.println("XOR INSTRUCTION \n");
                            int srcReg = Integer.parseInt(parts[2]);
                            processor.xor(destReg, srcReg);
                            processor.printProcessorState();
                        } else {
                            System.out.println("XOR without enough operands at line " + lineNumber);
                            processor.halt();
                        }
                        break;
                    case "MOV":
                        if (parts.length >= 3) {
                            System.out.println("MOV INSTRUCTION \n");
                            if (Character.isDigit(parts[2].charAt(0)) || parts[2].charAt(0) == '-') {
                                // MOV with immediate value
                                long immediateValue = Long.parseLong(parts[2]);
                                processor.mov(destReg, immediateValue);
                                processor.printProcessorState();
                            } else {
                                // MOV from register
                                int srcReg = Integer.parseInt(parts[2]);
                                processor.mov(destReg, srcReg);
                                processor.printProcessorState();
                            }
                        } else {
                            System.out.println("MOV without enough operands at line " + lineNumber);
                            processor.halt();
                        }
                        break;
                    case "JMP":
                        if (parts.length >= 2) {
                            System.out.println("JMP INSTRUCTION  \n");
                            long targetAddress = Long.parseLong(parts[1]);
                            processor.jmp(targetAddress);
                            processor.printProcessorState();
                        } else {
                            System.out.println("JMP without enough operands at line " + lineNumber);
                            processor.halt();
                        }
                        break;
                    case "JE":
                        if (parts.length >= 4) {
                            System.out.println("JE INSTRUCTION  \n");
                            long targetAddress = Long.parseLong(parts[2]);
                            int srcReg1 = Integer.parseInt(parts[3]);
                            int srcReg2 = Integer.parseInt(parts[4]);
                            processor.je(targetAddress, srcReg1, srcReg2);
                            processor.printProcessorState();
                        } else {
                            System.out.println("JE without enough operands at line " + lineNumber);
                            processor.halt();
                        }
                        break;
                    case "JNE":
                        if (parts.length >= 4) {
                            System.out.println("JNE INSTRUCTION \n");
                            long targetAddress = Long.parseLong(parts[2]);
                            int srcReg1 = Integer.parseInt(parts[3]);
                            int srcReg2 = Integer.parseInt(parts[4]);
                            processor.jne(targetAddress, srcReg1, srcReg2);
                            processor.printProcessorState();
                        } else {
                            System.out.println("JNE without enough operands at line " + lineNumber);
                            processor.halt();
                        }
                        break;
                    case "JGE":
                        if (parts.length >= 4) {
                            System.out.println("JGE INSTRUCTION \n");
                            long targetAddress = Long.parseLong(parts[2]);
                            int srcReg1 = Integer.parseInt(parts[3]);
                            int srcReg2 = Integer.parseInt(parts[4]);
                            processor.jge(targetAddress, srcReg1, srcReg2);
                            processor.printProcessorState();
                        } else {
                            System.out.println("JGE without enough operands at line " + lineNumber);
                            processor.halt();
                        }
                        break;
                    case "JL":
                        if (parts.length >= 4) {
                            System.out.println("JL INSTRUCTION \n");
                            long targetAddress = Long.parseLong(parts[2]);
                            int srcReg1 = Integer.parseInt(parts[3]);
                            int srcReg2 = Integer.parseInt(parts[4]);
                            processor.jl(targetAddress, srcReg1, srcReg2);
                            processor.printProcessorState();
                        } else {
                            System.out.println("JL without enough operands at line " + lineNumber);
                            processor.halt();
                        }
                        break;
                    case "CMP":
                        if (parts.length >= 4) {
                            System.out.println("CMP INSTRUCTION \n");
                            int srcReg1 = Integer.parseInt(parts[2]);
                            int srcReg2 = Integer.parseInt(parts[3]);
                            processor.cmp(srcReg1, srcReg2);
                            processor.printProcessorState();
                        } else {
                            System.out.println("CMP without enough operands at line " + lineNumber);
                            processor.halt();
                        }
                        break;
                    case "READ_KEYBOARD":
                        System.out.println("READ_KEYBOARD INSTRUCTION \n");
                        processor.readFromKeyboard(destReg);
                        processor.printProcessorState();
                        break;
                    case "WRITE_SCREEN":
                        System.out.println("WRITE_SCREEN INSTRUCTION \n");
                        processor.writeToScreen(destReg);
                        processor.printProcessorState();
                        break;
                    case "HALT":
                        processor.halt();
                        break;
                    default:
                        System.out.println("Unrecognized opcode at line " + lineNumber + ": " + opcode);
                        processor.halt();
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error parsing operand at line " + lineNumber + ": " + e.getMessage());
                processor.halt();
            }
        } else {
            System.out.println("Invalid instruction format at line " + lineNumber + ": " + instructionLine);
            processor.halt();
        }
    }

    public void configureCacheManually() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the number of cache levels: ");
        int numCacheLevels = scanner.nextInt();

        int[] cacheSizes = new int[numCacheLevels];
        int[] associativities = new int[numCacheLevels];

        for (int i = 0; i < numCacheLevels; i++) {
            System.out.println("Cache Level " + i + ":");
            System.out.print("Enter the cache size for level " + i + " (in bytes): ");
            cacheSizes[i] = scanner.nextInt();
            System.out.print("Enter the associativity for level " + i + ": ");
            associativities[i] = scanner.nextInt();
        }

        System.out.print("Enter the cache line size (in bytes): ");
        int cacheLineSize = scanner.nextInt();

        // Create a Cache instance with the configured parameters
        cache = new Cache(memory, numCacheLevels, cacheSizes, associativities, cacheLineSize);
        setCache(cache);
    }

    public void setCache(Cache cache){
        this.cache = cache;
    }
}