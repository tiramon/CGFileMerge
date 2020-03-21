# CGFileMerge
On the site http://www.codingame.com you can solve coding puzzles, but your solutions must be located in one file. Because it is easier to work with multiple files in a local workspace, i create this little program that will take all content from all files found in the given folders and merge it in one file.

This is designed to work with Java and its files. 

This little program has the following features (or should have when the last changes are added):
* remove all package lines and merge everything in the default package
* merge imports of all files found identified by the line starting with `import ` and remove those of in project imports
* replace all modifiers in front of class, interface, enum and remove it `public class` -> `class` 
* writes everything into a output file at the given location


## How to build
`mvn clean package`

Result jar is created in the target folder inside of the project

## How to start
`java -jar CGFileMerge-0.0.1-SNAPSHOT.jar /PATH/TO/PROJECT/ROOT/src/main/java /PATH/TO/PROJECT/ROOT/Output.java`

## How to use with maven without local build

`<dependencies>
		<dependency>
			<groupId>com.github.Tiramon</groupId>
			<artifactId>CGFileMerge</artifactId>
			<version>HEAD</version>
		</dependency>
	</dependencies>
	<repositories>
		<repository>
			<id>jitpack.io</id>
			<url>https://jitpack.io</url>
		</repository>
	</repositories>`
