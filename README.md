![logo](https://media.forgecdn.net/attachments/123/595/uchat.png)  
Ultimate and advanced Chat for Spigot, Sponge and BungeeCoord

## Available versions:
Spigot: [https://www.spigotmc.org/resources/uchat.23767/](https://www.spigotmc.org/resources/uchat.23767/)  
Bukkit: [https://dev.bukkit.org/projects/uchat](https://dev.bukkit.org/projects/uchat)  
Sponge: [https://ore.spongepowered.org/FabioZumbi12/UltimateChat](https://ore.spongepowered.org/FabioZumbi12/UltimateChat)  

## Source:
The source is available on GitHub: [https://github.com/FabioZumbi12/UltimateChat](https://github.com/FabioZumbi12/UltimateChat)  

## Dev Builds:
Dev builds on Jenkins: [![Build Status](http://host.areaz12server.net.br:8081/buildStatus/icon?job=UltimateChat)](http://host.areaz12server.net.br:8081/job/UltimateChat/)

## UltimateChat WIKI:
Check WIKI for:  
* Commands
* Permissions
* Channels
* BungeeCoord
* Redis Server Messaging
* Discord Configuration
* API Usage
* ...and more

## API repository:

**Repository:**  
UltimateChat is hosted on Maven Central
### Maven
```xml
<dependencies>
    <dependency>
        <groupId>io.github.fabiozumbi12.UltimateChat</groupId>
        <artifactId>UltimateChat-Spigot</artifactId>
        <version>1.9.3-SNAPSHOT</version>
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
        <version>1.9.3-SNAPSHOT</version>
        <classifier>javadoc</classifier>
    </dependency> 
</dependencies>  
```

### Gradle:
```
repositories {
    mavenCentral()
    maven { url = 'https://s01.oss.sonatype.org/content/repositories/snapshots/' } // Only for snapshots
}

dependencies {
    compileOnly ("io.github.fabiozumbi12.UltimateChat:UltimateChat-Spigot:1.9.3-SNAPSHOT"){ exclude(group: "*") }
}
```

## Wiki:
UltimateChat WIKI: [Click Here!](https://github.com/FabioZumbi12/UltimateChat/wiki)