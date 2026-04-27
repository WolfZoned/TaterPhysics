# TaterPhysics - Installation & Setup Guide

## Quick Start

### Fastest Way - Using Run Script

**Linux/macOS:**
```bash
./run.sh
```

**Windows:**
```batch
run.bat
```

This will automatically build and run the demo.

### Quick Build & Run

**First time - Build the project:**
```bash
./setup.sh
```

**Then run:**
```bash
./run.sh
```

**Or manually:**
```bash
java -jar build/libs/TaterPhysics-1.0.0-all.jar
```

## Manual Installation

### Prerequisites
- **Java 21 or later** (JDK - must include javac compiler)
- **No additional dependencies required**

### Step 1: Install Java

If you don't have Java installed, download from:
- **Adoptium (Free, Recommended):** https://adoptium.net/
- **Oracle Java:** https://www.oracle.com/java/technologies/downloads/
- **OpenJDK:** https://openjdk.java.net/

**Important:** Install the **JDK** (Java Development Kit), not just the JRE. You need the `javac` compiler.

Verify installation:
```bash
java -version
javac -version
```

You should see both commands work and show Java 11 or later.

### Step 2: Build the Project

Navigate to the project directory and run the setup script:

**On Linux/macOS:**
```bash
./setup.sh
```

**On Windows:**
Double-click `setup.bat` to run it, or from Command Prompt:
```batch
setup.bat
```

This will:
1. Check for Java and javac
2. Compile all Java source files
3. Package them into a JAR file at: `build/libs/TaterPhysics-1.0.0-all.jar`

### Step 3: Run the Demo

**Using the launcher script (easiest):**
```bash
./run.sh        # Linux/macOS
run.bat         # Windows
```

**Or direct execution:**
```bash
java -jar build/libs/TaterPhysics-1.0.0-all.jar
```

---

## Troubleshooting

### "Java command not found"
- Java is not installed
- Solution: Install Java from https://adoptium.net/

### "javac command not found"
- You have JRE installed but not JDK
- Solution: Install JDK (not JRE) from https://adoptium.net/

### "Compilation failed"
- There are errors in the source code
- Solution: Ensure all `.java` files are in the `src/` directory and intact

### "JAR file not found"
- The project hasn't been built yet
- Solution: Run `./setup.sh` (or `setup.bat` on Windows)

### "Permission denied" (Linux/macOS)
- The shell scripts aren't executable
- Solution: Run `chmod +x ./setup.sh ./run.sh` first

### "Poor Performance / Low FPS"
- Your system may have low available resources
- Try running with more allocated memory: 
  - `java -Xmx1024m -jar build/libs/TaterPhysics-1.0.0-all.jar`
- Or close other applications

### "Black/Empty Window"
- Your graphics system might need time to initialize
- Wait a moment for the window to fully render
- Try resizing the window

## System Requirements

| Requirement | Minimum | Recommended |
|---|---|---|
| Java Version | 21 | 21 or later |
| RAM | 256 MB | 512 MB+ |
| Display | 1200x1000 | 1920x1080+ |
| OS | Windows 7+ / macOS 10.12+ / Linux | Modern versions |

---

## For Developers

If you want to modify the source code:

1. Open the project in an IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Ensure Java SDK 21+ is configured
3. The main entry point is `src/Main.java`
4. The build scripts (`setup.sh` / `setup.bat`) use javac to compile

---

## Running from IDE

**IntelliJ IDEA:**
1. Open the project folder
2. Mark `src/` as Sources Root
3. Run `Main.main()` directly

**VS Code:**
1. Install Extension Pack for Java
2. Open the project folder
3. Press Ctrl+Shift+D to debug, or use the Java menu

---

For more information, see [README.md](README.md)




