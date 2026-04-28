# Installation & Setup

## Build

**Requirements:** Java 25+ (JDK from https://adoptium.net/)

**Compile:**
```bash
./setup.sh        # Linux/macOS
setup.bat         # Windows
```

Output: `build/libs/TaterPhysics-1.0.0-all.jar`

---

## Run

**Using launcher script:**
```bash
./run.sh          # Linux/macOS
run.bat           # Windows
```

**Direct:**
```bash
java -jar build/libs/TaterPhysics-1.0.0-all.jar
```

---

## Windows EXE

**Lightweight (~113 KB)** - needs Java 25:
```bash
launch4j launch4j-config.xml
```

**Self-Contained (~170 MB)** - no Java needed:
1. Download Java 25 JRE from https://adoptium.net/ (Windows x64, .zip)
2. Extract into `TaterPhysics/jre/`
3. `launch4j launch4j-config-bundled.xml`

---

## Troubleshooting

- "Java not found" → Install JDK from https://adoptium.net/
- "javac not found" → Install JDK (not JRE)
- "Permission denied" → `chmod +x ./setup.sh ./run.sh`
- "JAR not found" → Run `./setup.sh` first
- Low FPS → `java -Xmx1024m -jar build/libs/TaterPhysics-1.0.0-all.jar`
- launch4j: jre not found → Check `jre/bin/java.exe` exists

---

## IDE Setup

- **IntelliJ:** Open folder, mark `src/` as Sources Root
- **VS Code:** Install Extension Pack for Java
- **Eclipse:** Import folder, set Java 25+ SDK

Main: `src/Main.java`




