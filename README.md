# Exposure for Minecraft 1.21.11 and NeoForge

Unofficial NeoForge port of [Exposure](https://github.com/mortuusars/Exposure) 1.9.12.
The Minecraft API migration builds on [BelzeBool's Fabric port](https://github.com/BelzeBool/Exposure-Port/tree/1.21.11)
and retains its commit history. The NeoForge integration and data build on mortuusars' original project.

Build with Java 21 using `./gradlew assemble` (`gradlew.bat assemble` on Windows).
Install the JAR from `build/libs` in a Minecraft 1.21.11 instance running NeoForge
21.11.42 or newer in the 21.11 series.

This branch builds NeoForge only. Optional JEI and KubeJS integrations are omitted.
Create recipes are conditional on Create being installed and were not tested here.

See [LICENSE.md](LICENSE.md) for the MIT license.
