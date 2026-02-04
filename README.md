![logo](https://media.forgecdn.net/attachments/123/595/uchat.png)  
Ultimate and advanced chat plugin for Spigot, BungeeCord, and Sponge with rich formatting, channels, hooks, and cross‑server support.

[![Latest Release](https://img.shields.io/github/v/release/FabioZumbi12/UltimateChat?label=release)](https://github.com/FabioZumbi12/UltimateChat/releases/latest)

## Downloads
- GitHub Releases (latest stable and pre‑releases): https://github.com/FabioZumbi12/UltimateChat/releases  
- Spigot: https://www.spigotmc.org/resources/uchat.23767/  
- Bukkit: https://dev.bukkit.org/projects/uchat  
- Sponge: https://ore.spongepowered.org/FabioZumbi12/UltimateChat  

## Source
https://github.com/FabioZumbi12/UltimateChat  

## CI Builds
Builds are published to GitHub Releases (pre‑releases for main branch commits).

## UltimateChat WIKI
Check WIKI for:  
* Commands
* Permissions
* Channels
* BungeeCoord
* Redis Server Messaging
* Discord Configuration
* API Usage
* ...and more

## API repository
UltimateChat is hosted on Maven Central.
### Maven
```xml
<dependencies>
    <dependency>
        <groupId>io.github.fabiozumbi12.UltimateChat</groupId>
        <artifactId>UltimateChat-Spigot</artifactId>
        <version>1.9.4-SNAPSHOT</version>
        <exclusions>
            <exclusion>
                <!-- We don't need any of the dependencies -->
                <groupId>*</groupId>
                <artifactId>*</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <!-- Import Javadocs -->
    <dependency>
        <groupId>io.github.fabiozumbi12.UltimateChat</groupId>
        <artifactId>UltimateChat-Spigot</artifactId>
        <version>1.9.4-SNAPSHOT</version>
        <classifier>javadoc</classifier>
    </dependency> 
</dependencies>  
```

### Gradle
```
repositories {
    mavenCentral()
    maven { url = ' https://central.sonatype.com/repository/maven-snapshots/' } // Only for snapshots
}

dependencies {
    compileOnly ("io.github.fabiozumbi12.UltimateChat:UltimateChat-Spigot:1.9.4-SNAPSHOT"){ exclude(group = "*")}
}
```

## Wiki
UltimateChat WIKI: https://github.com/FabioZumbi12/UltimateChat/wiki
