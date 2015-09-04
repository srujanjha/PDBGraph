# PDBGraph
This repository contains source files for PDBGraph. PDBGraph is an application which can plot Ramachandran Plots for large number of proteins simulataneously. You just need to have PDB files for such proteins.

>Analysis of Proteins using their Phi-Psi Angles on Ramachandran Plot
PDBs are obtained from http://www.rcsb.org/
1. Molecule type: Protein only
2. Deposit Date: 1st January, 2009 to 31st December, 2010
3. X-Ray Resolution:
a. Between 0Å and 1Å
b. Between 1Å and 2Å
c. Greater than 2Å

1. A Windows Application is developed using C# in Visual Studio 2015.
a. This software can be used to plot both normal as well as the Difference Plot.
b. One can also modify the graph such as the title, labels on the axes, size of the point on the graph etc.
c. One can also save the graph at a desired location.
Setup Instructions: Double Click the “Setup” file in the Publish folder of the C# Source Code.

2. A Java application developed on Eclipse.
Setup Instructions:
In the command line:
>javac PDBGraph.java
>java PDBGraph
>D://PDB1 (Location of the PDB Files)
>D://Temp (Temporary Location for working)
>Output: Ramachandran Plot
