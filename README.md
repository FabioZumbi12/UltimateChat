![logo](https://media-elerium.cursecdn.com/attachments/123/595/uchat.png)  
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

## APIs:

**Repository:**  
```xml
<repositories> 
    <repository> 
        <id>ultimatechat-repo</id> 
        <url>https://raw.githubusercontent.com/FabioZumbi12/UltimateChat/mvn-repo/</url> 
    </repository> 
</repositories>
```


**UltimateChat API:**  
```xml
<dependencies>
    <dependency>
        <groupId>br.net.fabiozumbi12.UltimateChat</groupId>
        <artifactId>UltimateChat-[version]</artifactId>
        <version>LATEST</version>
        <scope>provided</scope>
    </dependency> 
    <dependency>
        <groupId>br.net.fabiozumbi12.UltimateChat</groupId>
        <artifactId>UltimateChat-[version]</artifactId>
        <version>LATEST</version>
        <classifier>javadoc</classifier>
    </dependency>
</dependencies>
```
Check ultimatechat version here: https://github.com/FabioZumbi12/UltimateChat/tree/mvn-repo/br/net/fabiozumbi12/UltimateChat  


**UltimateFancy API:**  
You need to shade UltimateFancy into your plugin or compile the jar.  
```xml
<dependencies>
    <dependency>
        <groupId>br.net.fabiozumbi12.UltimateChat</groupId>
        <artifactId>UltimateFancy</artifactId>
        <version>LATEST</version>
        <scope>compile</scope>
    </dependency> 
    <dependency>
        <groupId>br.net.fabiozumbi12.UltimateChat</groupId>
        <artifactId>UltimateFancy</artifactId>
        <version>LATEST</version>
        <classifier>javadoc</classifier>
    </dependency>
</dependencies>
```

## Wiki:
UltimateChat WIKI: [Click Here!](https://github.com/FabioZumbi12/UltimateChat/wiki)