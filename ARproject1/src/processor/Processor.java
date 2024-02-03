package processor;

import cache.Cache;
import memory.Memory;

import java.util.Scanner;

public class Processor {

    private static final int NUM_GENERAL_PURPOSE_REGISTERS = 4;                         // u skladu sa tekstom zadatka..

    private long[] generalPurposeRegisters = new long[NUM_GENERAL_PURPOSE_REGISTERS];   // sizeof(long) = 8[B]
    private long programCounter = 0;                                                    //  indeksiranje..
    private Memory memory = new Memory();   //  radna memorija..
    private Cache cache = new Cache(memory);      //  cache memorija..
    private boolean isRunning = true;       //  halt yes/no ..

    // Arithmetical Operations: add, sub, mul, div
    private void add(int destReg, int srcReg) {
        generalPurposeRegisters[destReg] += generalPurposeRegisters[srcReg];
    }

    private void sub(int destReg, int srcReg) {
        generalPurposeRegisters[destReg] -= generalPurposeRegisters[srcReg];
    }

    private void mul(int destReg, int srcReg) {
        generalPurposeRegisters[destReg] *= generalPurposeRegisters[srcReg];
    }

    private void div(int destReg, int srcReg) {
        if (generalPurposeRegisters[srcReg] != 0) {
            generalPurposeRegisters[destReg] /= generalPurposeRegisters[srcReg];
        } else {
            halt();
        }
    }

    // Logical Operations: and, or, not, xor, mov, mov
    private void and(int destReg, int srcReg) {
        generalPurposeRegisters[destReg] &= generalPurposeRegisters[srcReg];
    }

    private void or(int destReg, int srcReg) {
        generalPurposeRegisters[destReg] |= generalPurposeRegisters[srcReg];
    }

    private void not(int destReg) {
        generalPurposeRegisters[destReg] = ~generalPurposeRegisters[destReg];
    }

    private void xor(int destReg, int srcReg) {
        generalPurposeRegisters[destReg] ^= generalPurposeRegisters[srcReg];
    }

    // Data Transfer Instructions
    private void mov(int destRegisterIndex, long value) {
        if (destRegisterIndex >= 0 && destRegisterIndex < NUM_GENERAL_PURPOSE_REGISTERS) {
            generalPurposeRegisters[destRegisterIndex] = value;
        } else {
            halt();
        }
    }

    private void mov(int destRegisterIndex, int sourceRegisterIndex) {
        if (destRegisterIndex >= 0 && destRegisterIndex < NUM_GENERAL_PURPOSE_REGISTERS &&
                sourceRegisterIndex >= 0 && sourceRegisterIndex < NUM_GENERAL_PURPOSE_REGISTERS) {
            generalPurposeRegisters[destRegisterIndex] = generalPurposeRegisters[sourceRegisterIndex];
        } else {
            halt();
        }
    }


    // Branching Instructions: jump, jump-equal, jump-not-euqal, jump greater-less, jump-less

    private void jmp(long targetAddress) {programCounter = targetAddress;}

    private void je(long targetAddress, int srcReg1, int srcReg2) {
        if (generalPurposeRegisters[srcReg1] == generalPurposeRegisters[srcReg2]) {
            programCounter = targetAddress;
        }
    }

    private void jne(long targetAddress, int srcReg1, int srcReg2) {
        if (generalPurposeRegisters[srcReg1] != generalPurposeRegisters[srcReg2]) {
            programCounter = targetAddress;
        }
    }

    private void jge(long targetAddress, int srcReg1, int srcReg2) {
        if (generalPurposeRegisters[srcReg1] >= generalPurposeRegisters[srcReg2]) {
            programCounter = targetAddress;
        }
    }

    private void jl(long targetAddress, int srcReg1, int srcReg2) {
        if (generalPurposeRegisters[srcReg1] < generalPurposeRegisters[srcReg2]) {
            programCounter = targetAddress;
        }
    }

    // Comparison
    private boolean zeroFlag;
    private boolean greaterThanFlag;
    private boolean lessThanFlag;

    private void setFlags(boolean zero, boolean greaterThan, boolean lessThan) {
        this.zeroFlag = zero;
        this.greaterThanFlag = greaterThan;
        this.lessThanFlag = lessThan;
    }

    private void cmp(int registerIndex1, int registerIndex2) {
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
    private void readFromKeyboard(int registerIndex) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a value: ");
        long value = scanner.nextLong();
        if (value < 0 || value >= NUM_GENERAL_PURPOSE_REGISTERS) {
            halt();
        }
        generalPurposeRegisters[registerIndex] = value;
    }

    private void writeToScreen(int registerIndex) {
        System.out.println("Value in register " + registerIndex + ": " + generalPurposeRegisters[registerIndex]);
    }

    // Halt Instruction
    private void halt() {
        isRunning = false;
        System.out.println("HALT instruction encountered. Emulation halted.");
    }

    // Method to read from memory (using cache)
    private byte readFromMemory(long address) {
        return cache.readFromCache(address);
    }

    // Method to write to memory (using cache)
    private void writeToMemory(long address, byte data) {
        cache.writeToCache(address, data);
    }

    // Method to read from a virtual address
    private byte readFromVirtualAddress(long virtualAddress) {
        long physicalAddress = translateVirtualToPhysical(virtualAddress);
        return readFromMemory(physicalAddress);
    }

    // Method to write to a virtual address
    private void writeToVirtualAddress(long virtualAddress, byte data) {
        long physicalAddress = translateVirtualToPhysical(virtualAddress);
        writeToMemory(physicalAddress, data);
    }

    // Method to translate virtual address to physical address
    private long translateVirtualToPhysical(long virtualAddress) {
        return memory.translateVirtualToPhysical(virtualAddress).getIndex();
    }

    // Method to get the offset from a virtual address
    private int getOffset(long virtualAddress) {
        return memory.getOffset(virtualAddress);
    }

    // Method to get the cache level based on the address
    private Cache.CacheLevel getCacheLevel(long address) {
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

            // Print processor state after each instruction
            printProcessorState();

            try{
                Thread.sleep(100);
            }catch(InterruptedException ex){
                ex.printStackTrace();
                halt();
            }
        }
    }

    // Method to execute an instruction
    private void executeInstruction(long instruction) {
        // Decode the instruction and perform the corresponding operation
        int opcode = getOpcode(instruction);

        switch (opcode) {
            case ADD_OPCODE:
                add(getDestReg(instruction), getSrcReg(instruction));
                break;
            case SUB_OPCODE:
                sub(getDestReg(instruction), getSrcReg(instruction));
                break;
            case MUL_OPCODE:
                mul(getDestReg(instruction), getSrcReg(instruction));
                break;
            case DIV_OPCODE:
                div(getDestReg(instruction), getSrcReg(instruction));
                break;
            case AND_OPCODE:
                and(getDestReg(instruction), getSrcReg(instruction));
                break;
            case OR_OPCODE:
                or(getDestReg(instruction), getSrcReg(instruction));
                break;
            case NOT_OPCODE:
                not(getDestReg(instruction));
                break;
            case XOR_OPCODE:
                xor(getDestReg(instruction), getSrcReg(instruction));
                break;
            case MOV_VALUE_OPCODE:
                mov(getDestReg(instruction), getImmediateValue(instruction));
                break;
            case MOV_REGISTER_OPCODE:
                mov(getDestReg(instruction), getSrcReg(instruction));
                break;
            case JMP_OPCODE:
                jmp(getTargetAddress(instruction));
                break;
            case JE_OPCODE:
                je(getTargetAddress(instruction), getSrcReg(instruction), getSrcReg2(instruction));
                break;
            case JNE_OPCODE:
                jne(getTargetAddress(instruction), getSrcReg(instruction), getSrcReg2(instruction));
                break;
            case JGE_OPCODE:
                jge(getTargetAddress(instruction), getSrcReg(instruction), getSrcReg2(instruction));
                break;
            case JL_OPCODE:
                jl(getTargetAddress(instruction), getSrcReg(instruction), getSrcReg2(instruction));
                break;
            case CMP_OPCODE:
                cmp(getSrcReg(instruction), getSrcReg2(instruction));
                break;
            case READ_KEYBOARD_OPCODE:
                readFromKeyboard(getDestReg(instruction));
                break;
            case WRITE_SCREEN_OPCODE:
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

    private int getOpcode(long instruction) {
        return (int) ((instruction >> OPCODE_SHIFT) & OPCODE_MASK);
    }

    private int getDestReg(long instruction) {
        return (int) ((instruction >> DEST_REG_SHIFT) & DEST_REG_MASK);
    }

    private int getSrcReg(long instruction) {
        return (int) ((instruction >> SRC_REG_SHIFT) & SRC_REG_MASK);
    }

    private long getImmediateValue(long instruction) {
        return (instruction >> IMMEDIATE_VALUE_SHIFT) & IMMEDIATE_VALUE_MASK;
    }

    private long getTargetAddress(long instruction) {
        return (instruction >> TARGET_ADDRESS_SHIFT) & TARGET_ADDRESS_MASK;
    }

    private int getSrcReg2(long instruction) {
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

    // Main method
    public static void main(String[] args) {
        Processor processor = new Processor();

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

        // Start the processor execution loop
        processor.run();
    }
}