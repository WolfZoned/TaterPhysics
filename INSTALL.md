# Installation & Setup

## Build

**Requirements:** Java 25+ (JDK from https://adoptium.net/)

**Build menu (interactive):**
```bash
./build.sh        # Linux/macOS
build.bat         # Windows
```

Choose from:
- JAR only
- Lightweight EXE (requires Java 25 on user machine)
- Bundled EXE (self-contained)
- All of the above

**Outputs:**
- JAR: `build/TaterPhysics.jar`
- EXE: `build/TaterPhysics.exe`
- Bundled ZIP: `build/TaterPhysics-exe-bundled.zip`

---

## Run

**Using launcher script:**
```bash
./run.sh          # Linux/macOS
run.bat           # Windows
```

**Direct:**
```bash
java -jar build/TaterPhysics.jar
```

---


## Troubleshooting

- "Java not found" → Install JDK from https://adoptium.net/
- "javac not found" → Install JDK (not JRE)
- "Permission denied" → `chmod +x ./compile.sh ./run.sh`
- "JAR not found" → Run `./compile.sh` first
- Low FPS → `java -Xmx1024m -jar build/TaterPhysics.jar`
- launch4j: jre not found → Check `jre/bin/java.exe` exists

---

## IDE Setup

- **IntelliJ:** Open folder, mark `src/` as Sources Root
- **VS Code:** Install Extension Pack for Java
- **Eclipse:** Import folder, set Java 25+ SDK

Main: `src/Main.java`




