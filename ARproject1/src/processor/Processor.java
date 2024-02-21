package processor;

import cache.Cache;
import memory.Memory;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Processor{
    private static final int NUM_GENERAL_PURPOSE_REGISTERS = 4;
    public  long[] generalPurposeRegisters; // 64-bit general-purpose registers
    private long programCounter; // 64-bit program counter
    private Cache cache;
    private Memory memory;

    //  flags
    private boolean zeroFlag;
    private boolean greaterThanFlag;
    private boolean lessThanFlag;

    //  for halting..
    private boolean isRunning = true;

    public Processor(Memory memory, Cache cache) {
        this.generalPurposeRegisters = new long[NUM_GENERAL_PURPOSE_REGISTERS];
        this.programCounter = 0;
        this.memory = memory;
        this.cache = cache;
    }

    public void add(int destRegister, int srcRegister1, int srcRegister2) {
        long operand1 = generalPurposeRegisters[srcRegister1];
        long operand2 = generalPurposeRegisters[srcRegister2];
        long result = operand1 + operand2;
        generalPurposeRegisters[destRegister] = result;
        programCounter += 1;
    }

    public void sub(int destRegister, int srcRegister1, int srcRegister2) {
        long operand1 = generalPurposeRegisters[srcRegister1];
        long operand2 = generalPurposeRegisters[srcRegister2];
        long result = operand1 - operand2;
        generalPurposeRegisters[destRegister] = result;
        programCounter += 1;
    }

    public void mul(int destRegister, int srcRegister1, int srcRegister2) {
        long operand1 = generalPurposeRegisters[srcRegister1];
        long operand2 = generalPurposeRegisters[srcRegister2];
        long result = operand1 * operand2;
        generalPurposeRegisters[destRegister] = result;
        programCounter += 1;
    }

    public void div(int destRegister, int srcRegister1, int srcRegister2) {
        long operand1 = generalPurposeRegisters[srcRegister1];
        long operand2 = generalPurposeRegisters[srcRegister2];
        if (operand2 != 0) {
            long result = operand1 / operand2;
            generalPurposeRegisters[destRegister] = result;
        } else {
            // Handle division by zero
        }
        programCounter += 1;
    }

    // Logičke operacije
    public void and(int destRegister, int srcRegister1, int srcRegister2) {
        long operand1 = generalPurposeRegisters[srcRegister1];
        long operand2 = generalPurposeRegisters[srcRegister2];
        long result = operand1 & operand2;
        generalPurposeRegisters[destRegister] = result;
        programCounter += 1;
    }

    public void or(int destRegister, int srcRegister1, int srcRegister2) {
        long operand1 = generalPurposeRegisters[srcRegister1];
        long operand2 = generalPurposeRegisters[srcRegister2];
        long result = operand1 | operand2;
        generalPurposeRegisters[destRegister] = result;
        programCounter += 1;
    }

    public void xor(int destRegister, int srcRegister1, int srcRegister2) {
        long operand1 = generalPurposeRegisters[srcRegister1];
        long operand2 = generalPurposeRegisters[srcRegister2];
        long result = operand1 ^ operand2;
        generalPurposeRegisters[destRegister] = result;
        programCounter += 1;
    }

    public void not(int destRegister, int srcRegister) {
        long operand = generalPurposeRegisters[srcRegister];
        long result = ~operand;
        generalPurposeRegisters[destRegister] = result;
        programCounter += 1;
    }

    // Transfer podataka (MOV) s podrškom za direktno i indirektno adresiranje
    public void mov(int destRegister, long source) {
        // Direktno adresiranje ??
        generalPurposeRegisters[destRegister] = source;
        programCounter += 1;
    }

    public void mov(int destRegister, int srcRegister) {
        // Direktno adresiranje
        generalPurposeRegisters[destRegister] = generalPurposeRegisters[srcRegister];
        programCounter += 1;
    }

    public void movFromRam(int destRegister, long address, boolean indirect) {
        // Indirektno adresiranje
        if (indirect) {
            long targetAddress = cache.readFromCache(address);
            generalPurposeRegisters[destRegister] = memory.readFromVirtualAddress(targetAddress);
        } else {
            // Direktno adresiranje
            generalPurposeRegisters[destRegister] = memory.readFromVirtualAddress(address);
        }
        programCounter += 1;
    }

    public void movToRam(int destRegisterIndex, long memoryAddress, boolean indirect) {
        if (destRegisterIndex < 0 || destRegisterIndex >= NUM_GENERAL_PURPOSE_REGISTERS) {
            halt();
        }

        // Dohvati podatak iz odredišnog registra
        long dataToMove = generalPurposeRegisters[destRegisterIndex];

        // Razdvoji 64-bitni podatak u osam 8-bitnih vrijednosti
        for (int i = 0; i < Long.BYTES; i++) {
            byte dataByte = (byte) (dataToMove >>> (i * Byte.SIZE));

            // Ako je indirektno adresiranje, pročitaj stvarnu adresu iz keš memorije
            long actualAddress = indirect ? cache.readFromCache(memoryAddress + i) : memoryAddress + i;

            // Pozovi writeToMemory metodu iz Cache klase za pisanje u RAM memoriju
            cache.writeToRAM(actualAddress, dataByte);
        }
        programCounter += 1;
    }

    public void jmp(long targetAddress, boolean indirect) {
        // Ako je indirektno grananje, pročitaj stvarnu adresu iz keš memorije
        long actualAddress = indirect ? cache.readFromCache(targetAddress) : targetAddress;

        // Postavi programski brojač na novu adresu
        //programCounter = actualAddress;
        setProgramCounter(actualAddress);
    }

    public void je(long targetAddress, boolean indirect) {
        // Proveri da li je uslov ispunjen (jednako)
        if (zeroFlag) {
            jmp(targetAddress, indirect);
        } else {
            programCounter += 1;
        }
    }

    public void jne(long targetAddress, boolean indirect) {
        // Proveri da li je uslov ispunjen (nije jednako)
        if (!zeroFlag) {
            jmp(targetAddress, indirect);
        } else {
            programCounter += 1;
        }
    }

    public void jge(long targetAddress, boolean indirect) {
        // Proveri da li je uslov ispunjen (veće ili jednako)
        if (greaterThanFlag || zeroFlag) {
            jmp(targetAddress, indirect);
        } else {
            programCounter += 1;
        }
    }

    public void jl(long targetAddress, boolean indirect) {
        // Proveri da li je uslov ispunjen (manje)
        if (!greaterThanFlag && !zeroFlag) {
            jmp(targetAddress, indirect);
        } else {
            programCounter += 1;
        }
    }

    // Comparison
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

    public void inputChar(int destRegisterIndex) {
        if (destRegisterIndex < 0 || destRegisterIndex >= NUM_GENERAL_PURPOSE_REGISTERS) {
            halt(); // Prekid izvršavanja ako je indeks registra neispravan
        }

        // Simulacija učitavanja znaka sa tastature
        Scanner scanner = new Scanner(System.in);
        char inputChar = scanner.next().charAt(0);

        // Smjesti učitani znak u odredišni registar
        generalPurposeRegisters[destRegisterIndex] = inputChar;
        programCounter += 1;
    }

    public void outputChar(int srcRegisterIndex) {
        if (srcRegisterIndex < 0 || srcRegisterIndex >= NUM_GENERAL_PURPOSE_REGISTERS) {
            halt(); // Prekid izvršavanja ako je indeks registra neispravan
        }

        // Simulacija ispisa znaka na ekran
        char outputChar = (char) generalPurposeRegisters[srcRegisterIndex];
        System.out.print(outputChar);
        programCounter += 1;
    }

    public void halt(){
        this.isRunning = false;
    }

    public long getGeneralPurposeRegisterValue(int registerIndex) {
        if (registerIndex < 0 || registerIndex >= NUM_GENERAL_PURPOSE_REGISTERS) {
            throw new IllegalArgumentException("Neispravan indeks registra");
        }
        return generalPurposeRegisters[registerIndex];
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
    public void printProcessorState() {
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

   public long fetchInstructionFromMemory(long programCounter) {
        // Koristi programski brojač za čitanje instrukcije iz memorije
        byte[] instructionBytes = new byte[4]; // Pretpostavljamo da je svaka instrukcija 4 bajta
        for (int i = 0; i < 4; i++) {
            instructionBytes[i] = readFromMemory(programCounter + i);
        }

        // Pretvori niz bajtova u 32-bitni long (instrukciju)
        long instruction = 0;
        for (int i = 0; i < 4; i++) {
            instruction |= (instructionBytes[i] & 0xFFL) << (i * 8);
        }
        return instruction;
    }

    public void executeInstruction(long decodedInstruction) {
        Opcode opcode = extractOpcode(decodedInstruction);
        int destRegister = extractDestRegister(decodedInstruction);
        int srcRegister1 = extractSrcRegister1(decodedInstruction);
        int srcRegister2 = extractSrcRegister2(decodedInstruction);

        switch (opcode) {
            case ADD:
                add(destRegister, srcRegister1, srcRegister2);
                break;
            case SUB:
                sub(destRegister, srcRegister1, srcRegister2);
                break;
            case MUL:
                mul(destRegister, srcRegister1, srcRegister2);
                break;
            case DIV:
                div(destRegister, srcRegister1, srcRegister2);
                break;
            case AND:
                and(destRegister, srcRegister1, srcRegister2);
                break;
            case OR:
                or(destRegister, srcRegister1, srcRegister2);
                break;
            case XOR:
                xor(destRegister, srcRegister1, srcRegister2);
                break;
            case NOT:
                not(destRegister, srcRegister1);
                break;
            case MOV_REG:
                mov(destRegister, srcRegister1);
                break;
            case MOV_IMM:
                // Assuming immediate value is in the lower 32 bits of the decodedInstruction
                long immediateValue = decodedInstruction & 0xFFFFFFFFL;
                mov(destRegister, immediateValue);
                break;
            case MOV_RAM:
                // Assuming address is in the lower 32 bits of the decodedInstruction
                long memoryAddress = decodedInstruction & 0xFFFFFFFFL;
                movFromRam(destRegister, memoryAddress, true);
                break;
            case JMP:
                // Assuming address is in the lower 32 bits of the decodedInstruction
                long jumpAddress = decodedInstruction & 0xFFFFFFFFL;
                jmp(jumpAddress, true);
                break;
            case JE:
                // Assuming address is in the lower 32 bits of the decodedInstruction
                long jeAddress = decodedInstruction & 0xFFFFFFFFL;
                je(jeAddress, true);
                break;
            case JNE:
                // Assuming address is in the lower 32 bits of the decodedInstruction
                long jneAddress = decodedInstruction & 0xFFFFFFFFL;
                jne(jneAddress, true);
                break;
            case JGE:
                // Assuming address is in the lower 32 bits of the decodedInstruction
                long jgeAddress = decodedInstruction & 0xFFFFFFFFL;
                jge(jgeAddress, true);
                break;
            case JL:
                // Assuming address is in the lower 32 bits of the decodedInstruction
                long jlAddress = decodedInstruction & 0xFFFFFFFFL;
                jl(jlAddress, true);
                break;
            case CMP:
                cmp(srcRegister1, srcRegister2);
                break;
            case INPUT_CHAR:
                inputChar(destRegister);
                break;
            case OUTPUT_CHAR:
                outputChar(srcRegister1);
                break;
            // Add more cases for other instructions as needed
            default:
                // Invalid instruction
                halt();
        }
    }

    // Metoda za dekodiranje instrukcije
    public long decodeInstruction(long instruction) {
        // Pretpostavljamo da su stariji 8 bitova opcode, a preostalih 56 bitova su registri
        int opcode = (int) (instruction >> 56);
        long registers = instruction & 0x00FFFFFFFFFFFFFFL;

        // Možete sada raditi nešto sa opcode-om i registrima, na primer, napraviti dekodiranu instrukciju
        long decodedInstruction = (opcode << 56) | registers;

        return decodedInstruction;
    }

    // Pomoćna metoda za ekstrakciju opcode-a
    private Opcode extractOpcode(long decodedInstruction) {
        // Prvi bajt instrukcije predstavlja opcode
        int opcodeValue = (int) ((decodedInstruction >> 56) & 0xFF);
        return Opcode.values()[opcodeValue];
    }

    // Pomoćna metoda za ekstrakciju destinacionog registra
    private int extractDestRegister(long decodedInstruction) {
        // Sledeći bajt nakon opcode-a
        return (int) ((decodedInstruction >> 48) & 0xFF);
    }

    // Pomoćna metoda za ekstrakciju prvog izvornog registra
    private int extractSrcRegister1(long decodedInstruction) {
        // Sledeći bajt nakon destinacionog registra
        return (int) ((decodedInstruction >> 40) & 0xFF);
    }

    // Pomoćna metoda za ekstrakciju drugog izvornog registra
    private int extractSrcRegister2(long decodedInstruction) {
        // Poslednji bajt instrukcije
        return (int) ((decodedInstruction >> 32) & 0xFF);
    }

    public long getProgramCounter(){
        return this.programCounter;
    }

    public void setProgramCounter(long programCounter){
        this.programCounter = programCounter;
    }
    public boolean isRunning(){
        return this.isRunning;
    }

    public void setZeroFlag(boolean zeroFlag){
        this.zeroFlag = zeroFlag;
    }

    public boolean isZeroFlag(){
        return this.zeroFlag;
    }

    public void setLessThanFlag(boolean lessThanFlag){
        this.lessThanFlag = lessThanFlag;
    }
    public boolean isLessThanFlag(){
        return this.lessThanFlag;
    }

    public void setGreaterThanFlag(boolean greaterThanFlag){
        this.greaterThanFlag = greaterThanFlag;
    }

    public boolean isGreaterThanFlag(){
        return this.greaterThanFlag;
    }
}