package memory;

import java.util.HashMap;
import java.util.Map;

public class Memory {

    private Map<Long, MemoryPage> pageTableLevel4 = new HashMap<>();

    public Memory() {
        initializeMemory();
    }

    private void initializeMemory() {}

    // Public method to read from a virtual address
    public byte readFromVirtualAddress(long virtualAddress) {
        MemoryPage page = translateVirtualToPhysical(virtualAddress);
        if (page != null) {
            int offset = getOffset(virtualAddress);
            return page.read(offset);
        }
        return 0; // Placeholder, replace with actual implementation
    }

    // Public method to write to a virtual address
    public void writeToVirtualAddress(long virtualAddress, byte data) {
        MemoryPage page = translateVirtualToPhysical(virtualAddress);
        if (page != null) {
            int offset = getOffset(virtualAddress);
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
            MemoryPage level3Page = level4Page.getPage(level3Index);
            if (level3Page != null) {
                MemoryPage level2Page = level3Page.getPage(level2Index);
                if (level2Page != null) {
                    MemoryPage level1Page = level2Page.getPage(level1Index);
                    if (level1Page != null) {
                        // Calculate the physical address
                        long offset = getOffset(virtualAddress);
                        return new MemoryPage((level1Page.getIndex() << 12) | offset);
                    }
                }
            }
        }
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

        public MemoryPage getPage(long index) {
            // For simplicity, create a new page if it doesn't exist
            return new MemoryPage(index);
        }

        public long getIndex() {
            return index;
        }
    }
}
