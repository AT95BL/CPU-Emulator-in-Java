package utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import memory.Memory;
public class InstructionLoader {
    public static void loadProgram(Memory memory, String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            long address = 0; // Adresa u memoriji gdje ćemo pohranjivati instrukcije

            while ((line = reader.readLine()) != null) {
                // Pohrani svaki znak iz linije u memoriju
                for (byte b : line.getBytes()) {
                    memory.writeToVirtualAddress(address, b);
                    address += 1; // Promijeni adresu za sljedeći bajt
                }
                // Dodaj novi redak između instrukcija
                memory.writeToVirtualAddress(address, (byte) '\n');
                address += 1; // Promijeni adresu za novi redak
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
