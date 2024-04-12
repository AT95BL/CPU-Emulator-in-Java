package memory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Memory {
    
    private static Map<Long, MemoryPage> pageTableLevel4 = new HashMap<>(); // prebaci je na 'static' 15.02.2024

    // Constuctor
    public Memory() {
        initializeMemory();
    }

    /**
     * Inicijalizuje memoriju dodajući početne stranice u mapu stranica.
     * Svaka stranica ima svoj indeks koji se dodjeljuje automatski.
     * Metoda se obično poziva prilikom inicijalizacije objekta klase Memory.
     */
    public void initializeMemory() {
        // Dodajte inicijalne stranice u mapu
        for (long i = 0; i < 512; i++) {
            pageTableLevel4.put(i, new MemoryPage(i));
        }
    }

    // Vrši čitanje bajta iz memorije na osnovu virtuelne adrese.
    // Ako je virtuelna adresa validna, izvršava čitanje na odgovarajućoj fizičkoj adresi i vraća pročitani bajt.
    // Ako virtuelna adresa nije validna ili se ne može prevesti u fizičku adresu, vraća se nula.
    // @param virtualAddress Virtuelna adresa sa koje se čita bajt.
    // @return Pročitani bajt iz memorije, ili 0 ako se virtuelna adresa ne može prevesti.
    public byte readFromVirtualAddress(long virtualAddress) {

        MemoryPage page = translateVirtualToPhysical(virtualAddress);

        // Ako je stranica pronađena
        if (page != null) {
            // Izračunaj offset unutar stranice
            int offset = getOffset(virtualAddress);
            // Ispisuje informacije o čitanju
            System.out.println("Reading from page: " + page.getIndex() + " at offset: " + offset);
            // Vrši čitanje sa odgovarajuće fizičke adrese i vraća pročitani bajt
            return page.read(offset); 
        }
        // Vraća nulu ako virtuelna adresa nije validna ili se ne može prevesti
        return 0; // Placeholder, replace with actual implementation!?
    }

    /**
     * Vrši upisivanje podataka na određenu virtuelnu adresu u memoriju.
     *
     * @param virtualAddress Virtuelna adresa na koju se vrši upisivanje podataka.
     * @param data Podaci koji se upisuju na navedenu virtuelnu adresu.
     */
    public void writeToVirtualAddress(long virtualAddress, byte data) {
        // Prevedi virtuelnu adresu u odgovarajuću fizičku adresu
        MemoryPage page = translateVirtualToPhysical(virtualAddress);
        // Ispisuje informacije o stranici dobijenoj iz prevođenja virtuelne adrese u fizičku (za praćenje)
        System.out.println("Page(translateVirtualToPhysical): " + page);

        // Proveri da li je stranica pronađena
        if (page != null) {
            // Izračunaj offset unutar stranice
            int offset = getOffset(virtualAddress);
            // Ispisuje informacije o offsetu (za praćenje)
            System.out.println("offset" + offset);
            // Upisuje podatke na odgovarajuću fizičku adresu unutar stranice
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
    /**
     * Izračunava offset unutar stranice na osnovu virtuelne adrese.
     * Offset predstavlja poziciju podataka unutar stranice i koristi se za precizno adresiranje.
     *
     * @param virtualAddress Virtuelna adresa čiji se offset računa.
     * @return Offset unutar stranice, predstavljen kao ceo broj.
     */
    public int getOffset(long virtualAddress) {
        // Primena bitovskog AND operatora sa 0xFFF izdvaja poslednjih 12 bitova, što predstavlja offset unutar stranice.
        // Ovo omogućava precizno adresiranje podataka unutar stranice.
        return (int) (virtualAddress & 0xFFF);
    }

    // Class representing a memory page
    public static class MemoryPage {
        private byte[] data = new byte[4096];   //  krupno zrno
        private long index;                     // New field to store the index

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
