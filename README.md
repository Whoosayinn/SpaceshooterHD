# SpaceshooterHD
Object Oriented Programming Final Project

## Requirements

- Java Development Kit (JDK) 21 or newer
- VS Code with the Extension Pack for Java, or Eclipse

The project does not contain a machine-specific Java path. Configure `JAVA_HOME`
on your computer, then restart your editor so it can discover the JDK.

### Windows (PowerShell)

```powershell
$env:JAVA_HOME = "C:\path\to\your\jdk"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

To save `JAVA_HOME` permanently, set it in **System Properties > Environment
Variables** and add `%JAVA_HOME%\bin` to `Path`.

### macOS (zsh)

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

Add those lines to `~/.zshrc` to keep the setting between terminal sessions.

## Run

Open `src/com/r3m/spaceshooter/application/Main.java` and select **Run Java**.
