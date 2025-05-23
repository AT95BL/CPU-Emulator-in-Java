# Processor Architecture Emulator    

**Project Assignment #1 – Processor Architecture**  

This project implements an **emulator for a custom processor architecture**, adhering to principles of **object-oriented programming (OOP)**, **SOLID principles**, 
and best practices for **readable code**. 
The correctness of all implemented features is tested using **unit tests**.

---

## 📋 Project Overview  

This emulator models a simple processor with the following components:  
- **Four 64-bit general-purpose registers**  
- **Program Counter (PC)**  
- **64-bit address space**, where each memory address holds 1 byte  

The instruction set supports:  
1. **Arithmetic Operations**:  
   - `ADD` – Addition  
   - `SUB` – Subtraction  
   - `MUL` – Multiplication  
   - `DIV` – Division  

2. **Bitwise Logical Operations**:  
   - `AND`, `OR`, `NOT`, `XOR`  

3. **Data Transfer Instructions**:  
   - `MOV` – Supports both **direct** and **indirect addressing**  

4. **Control Flow Instructions** (Unconditional & Conditional Branching):  
   - `JMP`, `JE`, `JNE`, `JGE`, `JL`, `CMP`  

5. **I/O Instructions**:  
   - Input from the keyboard into a register  
   - Output from a register to the screen  

6. **Processor Halt Instruction**:  
   - `HALT` – Stops the emulator execution  

---

## 🛠 Features  

1. **Efficient Memory Usage**  
   - Full access to the **guest address space** while minimizing host memory usage.  

2. **Cache Memory Simulation**  
   - Configurable **cache levels** (e.g., L1, L2, L3).  
   - Set cache **size**, **associativity**, and **line size** per level.  
   - Track **cache hit and miss rates** for memory accesses.  
   - Support for **cache replacement algorithms**:
     - **LRU (Least Recently Used)**
     - **Optimal (Bélády) Algorithm**

3. **Unit Tests**  
   - All core functionalities are tested with unit tests to ensure correctness.  

---

## 🚀 How to Use  

1. **Clone the Repository**:  
   ```bash
   git clone https://github.com/your-username/processor-emulator.git
   cd processor-emulator
   ```

2. **Compile and Run the Program**:  
   ```bash
   javac -d bin src/*.java
   java -cp bin Main
   ```

3. **Configure the Emulator**:  
   - Set cache parameters (number of levels, size, associativity, line size).  
   - Choose a **cache replacement algorithm** (LRU or Optimal).  

4. **Run Example Programs**:  
   - Sample programs demonstrating arithmetic, logic, branching, and I/O instructions are included.  

---

## 🗂 Project Structure  

```
processor-emulator/
│
├── src/                     # Source code
│   ├── core/                # Processor and memory models
│   ├── instructions/        # Instruction implementations (arithmetic, logic, branching)
│   ├── io/                  # Input/Output handling
│   ├── cache/               # Cache memory simulation
│   ├── tests/               # Unit tests for the emulator
│   └── Main.java            # Entry point for the emulator
│
├── examples/                # Sample programs for the emulator
├── README.md                # Project documentation
└── .gitignore               # Ignored files and directories
```

---

## ⚙️ Configuration Options  

- **Cache Levels**: Specify the number of cache levels (e.g., 1 to 3).  
- **Cache Size per Level**: Set the size of each cache level in bytes.  
- **Cache Line Size**: Define the size of a cache line.  
- **Associativity**: Configure direct, set-associative, or fully associative caches.  
- **Replacement Algorithms**:  
  - **LRU** – Evicts the least recently used line.  
  - **Optimal** – Uses the Bélády algorithm for optimal line eviction.  

---

## 📝 Example Usage  

Here’s a simple **example program** that uses arithmetic and branching instructions:  

```
MOV R1, 10    ; Load 10 into register R1  
MOV R2, 20    ; Load 20 into register R2  
ADD R3, R1, R2  ; Add R1 and R2, store result in R3  
CMP R3, 30    ; Compare R3 with 30  
JE END        ; If R3 == 30, jump to END  
HALT          ; Stop the emulator  
END:          ; Label for the end of the program  
MOV R4, 'H'   ; Store character 'H' in R4  
OUT R4        ; Print 'H' to the screen  
```

---

## 📊 Cache Performance Metrics  

The emulator provides detailed statistics on **cache performance**, including:  
- **Cache hit rate**: Percentage of memory accesses served from the cache.  
- **Cache miss rate**: Percentage of memory accesses that result in a cache miss.  

---

## 🧪 Unit Testing  

Unit tests ensure the correctness of:  
- Arithmetic and logic operations  
- Data transfers and branching instructions  
- Cache functionality and replacement algorithms  

Run all unit tests using:  
```bash
java -cp bin org.junit.runner.JUnitCore tests.ProcessorTest
```

---

## 🛡 Best Practices  

- Follow **OOP and SOLID principles**.  
- Ensure **readable code** by following naming conventions and proper formatting.  
- Avoid code duplication and aim for efficient performance.  

---

## 💡 Demonstration  

To demonstrate the functionality of the emulator:  
- Load a sample program from the `examples/` folder.  
- Configure the cache settings before execution.  
- Observe the cache performance metrics and program output on the console.  

---

## 🛡 License  

This project is developed as part of the **Arhitektura računara** course.

---

## 👥 Contributors  

- **Andrej Trožić** – Student at Elektrotehnički fakultet, Banja Luka  

---
