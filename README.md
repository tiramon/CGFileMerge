# CGFileMerge
On the site http://www.codingame.com you can solve coding puzzles, but your solutions must be located in one file. Because it is easier to work with multiple files in a local workspace, i create this little program that will take all content from all files found in the given folders and merge it in one file.

This is designed to work with Java and its files. 

This little program has the following features (or should have when the last changes are added):
* remove all package lines and merge everything in the default package
* merge imports of all files found identified by the line starting with `import ` and remove those of in project imports
* replace all modifiers in front of class, interface, enum and remove it `public class` -> `class` 
* writes everything into a `Output.java` at the location where the program was started


## How to build
`mvn clean package`

## How to start
`java -jar CGFileMerge-0.0.1-SNAPSHOT.jar /PATH/TO/PROJECT/ROOT/src/main/java /PATH/TO/OTHER/ROOT/src/main/java`