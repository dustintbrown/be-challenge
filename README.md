# Java Assessment

Java project for BE candidate selection.

## Usage

The Xmx and Xms values should be ~ 3x the filesize importing. The 6G value below works well for ~100 Million OIDs.

```shell script
java -Xmx6G -Xmx6G com.dustintbrown.app.App
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