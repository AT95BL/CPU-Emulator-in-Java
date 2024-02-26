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
            return page.read(offset);   // Čitanje podataka iz fizičke memorije: pristupanje stranici s indeksom dobijenim iz virtuelne adrese, te čitanje podataka s određenog offseta unutar te stranice.
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
        /*
            Ovo predstavlja proces izdvajanja indeksa na svakom nivou hijerarhijske strukture stranica (paging) iz virtualne adrese. Hijerarhijska struktura stranica je organizacija memorije u nivoe, gdje svaki nivo predstavlja određeni broj bitova adrese i omogućava efikasno adresiranje memorije na velikim sistemima.
            Evo objašnjenja svakog reda:
            1. `long level4Index = (virtualAddress >> 39) & 0x1FF;`
            - Pomičemo (shift) virtualnu adresu za 39 bitova udesno, čime izdvajamo prvih 9 bitova (0x1FF predstavlja binarni niz od 9 jedinica). Ovih 9 bitova predstavljaju indeks na Level 4 u hijerarhiji stranica.
            2. `long level3Index = (virtualAddress >> 30) & 0x1FF;`
            - Pomičemo virtualnu adresu za 30 bitova udesno, izdvajajući sljedećih 9 bitova. Ovih 9 bitova predstavljaju indeks na Level 3 u hijerarhiji stranica.
            3. `long level2Index = (virtualAddress >> 21) & 0x1FF;`
            - Pomičemo virtualnu adresu za 21 bit udesno, izdvajajući sljedećih 9 bitova. Ovih 9 bitova predstavljaju indeks na Level 2 u hijerarhiji stranica.
            4. `long level1Index = (virtualAddress >> 12) & 0x1FF;`
            - Pomičemo virtualnu adresu za 12 bitova udesno, izdvajajući sljedećih 9 bitova. Ovih 9 bitova predstavljaju indeks na Level 1 u hijerarhiji stranica.
            Svaki indeks identifikuje određeni nivo u hijerarhiji stranica i omogućava efikasno prevođenje virtualne adrese u fizičku adresu koristeći mapu stranica. Ova struktura omogućava efikasno upravljanje memorijom, a nivoi pružaju granularnost pri pristupu podacima.
        * */

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
    /*
    * jednostavna (pod)klasa za reprezentaciju bloka fizičke memorije
    * instanca te klase jeste memorijska stranica tj. blok RAM memorije veličine 4096[B] ili ti 4[kB]
    * te kroz te bajtove se prolazi koristenjem indeksa..
    * */
    /*
     * U klasi `Memory.java`, virtuelne adrese se implicitno čuvaju u metodama koje vrše mapiranje virtuelnih adresa na fizičke adrese. Nije eksplicitno potrebno čuvati sve virtuelne adrese, jer su adrese dinamičke i mogu zauzeti veliki prostor.
     * Mapiranje virtuelnih adresa na fizičke adrese obavlja se putem hijerarhijske strukture stranica (page table). U metodi `translateVirtualToPhysical`, virtuelna adresa se rastavlja na četiri nivoa (level4, level3, level2, level1), a zatim se koristi ova hijerarhija da bi se pronašla odgovarajuća fizička adresa. Praktično, virtuelne adrese se ne čuvaju eksplicitno u obliku liste ili mape, već se dinamički obrađuju kako bi se pronašle odgovarajuće fizičke adrese prilikom pristupa memoriji.
     * Dakle, u klasi `Memory.java`, virtuelne adrese se obrađuju i koriste dinamički, a ne čuvaju se eksplicitno, jer hijerarhijska struktura stranica omogućava dinamičko mapiranje virtuelnih adresa na fizičke adrese tokom izvršavanja programa.
     * */

    @Override
    public String toString() {
        return "Memory{" +
                "pageTableLevel4=" + pageTableLevel4 +
                '}';
    }
}
