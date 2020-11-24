# Java Assessment

Java project for BE candidate selection.

## Usage

The Xmx and Xms values should be ~ 3x the filesize importing. The 10G value below works well for ~100 Million OIDs. For reference 100M OIDs is about 2.4G on disk.

```shell script
mvn clean package
java -Xms10G -Xmx10G -jar target/java-assessment-1.0.1.jar
```
```text
-------------------------
 Choose an OID operation 
-------------------------
1 - Load OIDs from file
2 - Load random OIDs
3 - Write loaded OIDs to a file
4 - Print loaded OIDs to the console
5 - Check if OID exists in loaded data
6 - Find OIDs in data matching a prefix
7 - Encode loaded OIDs
8 - Show diff between loaded OIDs and a given file
9 - Quit
```

## How to Generate Random OID Files

You can use the built-in utility to generate text files with random OIDs.

```shell script
mvn clean package
java -Xms10G -Xmx10G -classpath target/java-assessment-1.0.1.jar com.dustintbrown.app.RandomOIDGenerator <num_to_generate> <full_path_to_output_file>.txt
```