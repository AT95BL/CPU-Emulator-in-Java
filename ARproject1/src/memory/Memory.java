package memory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Memory {
    private static Map<Long, MemoryPage> pageTableLevel4 = new HashMap<>(); // prebaci je na 'static' 15.02.2024

    public Memory() {
        initializeMemory();
    }

    private void initializeMemory() {
        // Dodajte inicijalne stranice u mapu   15.02.2024
        for (long i = 0; i < 512; i++) {
            pageTableLevel4.put(i, new MemoryPage(i));
        }
    }

    // Public method to read from a virtual address
    public byte readFromVirtualAddress(long virtualAddress) {
        MemoryPage page = translateVirtualToPhysical(virtualAddress);
        if (page != null) {
            int offset = getOffset(virtualAddress);
            System.out.println("Reading from page: " + page.getIndex() + " at offset: " + offset);
            return page.read(offset);
        }
        return 0; // Placeholder, replace with actual implementation
    }

    // Public method to write to a virtual address | 15.02.2024
    public void writeToVirtualAddress(long virtualAddress, byte data) {

        MemoryPage page = translateVirtualToPhysical(virtualAddress);
        System.out.println("Page(translateVirtualToPhysical): " + page);

        if (page != null) {
            int offset = getOffset(virtualAddress);
            System.out.println("offset" + offset);
            page.write(offset, data);
        }
    }

    // Method to translate virtual address to physical address using a hierarchical structure
    public MemoryPage translateVirtualToPhysical(long virtualAddress) {
        long level4Index = (virtualAddress >> 39) & 0x1FF;
        long level3Index = (virtualAddress >> 30) & 0x1FF;
        long level2Index = (virtualAddress >> 21) & 0x1FF;
        long level1Index = (virtualAddress >> 12) & 0x1FF;

        MemoryPage level4Page = pageTableLevel4.get(level4Index);
        if (level4Page != null) {
            System.out.println("Level 4 Page found: " + level4Page.getIndex());
            MemoryPage level3Page = level4Page.getPage(level3Index);
            if (level3Page != null) {
                System.out.println("Level 3 Page found: " + level3Page.getIndex());
                MemoryPage level2Page = level3Page.getPage(level2Index);
                if (level2Page != null) {
                    System.out.println("Level 2 Page found: " + level2Page.getIndex());
                    MemoryPage level1Page = level2Page.getPage(level1Index);
                    if (level1Page != null) {
                        // Calculate the physical address
                        long offset = getOffset(virtualAddress);
                        long physicalAddress = (level1Page.getIndex() << 12) | offset;
                        System.out.println("Translated virtual address " + virtualAddress + " to physical address " + physicalAddress);
                        return level1Page; // Change to return level1Page directly 15.02.2024
                    }
                }
            }
        }
        System.out.println("Translation failed. Returning null.");
        return null; // Return null to indicate a failure in translation
    }

    // Method to get the offset from a virtual address
    public int getOffset(long virtualAddress) {
        return (int) (virtualAddress & 0xFFF);
    }

    // Class representing a memory page
    public static class MemoryPage {
        private byte[] data = new byte[4096];
        private long index; // New field to store the index

        public MemoryPage(long index) {
            this.index = index;
        }

        public byte read(int offset) {
            return data[offset];
        }

        public void write(int offset, byte value) {
            data[offset] = value;
        }

        public static MemoryPage getPage(long index) { // 15.02.2024
            // Ako stranica već postoji, vrati je
            if (pageTableLevel4.containsKey(index)) {
                return pageTableLevel4.get(index);
            }

            // Inače stvori novu stranicu i dodaj je u mapu
            MemoryPage newPage = new MemoryPage(index);
            pageTableLevel4.put(index, newPage);
            return newPage;
        }

        public long getIndex() {
            return index;
        }
    }

    @Override
    public String toString() {
        return "Memory{" +
                "pageTableLevel4=" + pageTableLevel4 +
                '}';
    }
}
