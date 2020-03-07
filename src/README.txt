-------------------------------------------------
WEIGHTED PAXOS 
Shayani Deb
Chinmaya Dattathri
Nov 26, 2013
--------------------------------------------------
Base Directory : src 
Commands to run to compile and execute:
javac *.java
java Env input.txt | tee out/output.txt
-------------------------------------------------
Code runs for infinite time. To Terminate when needed, press ctrl-C
input.txt is the config file for Weighted Paxos
README for "How to Use input.txt" available inside input.txt
-------------------------------------------------
How to : Run "script" to check if all Replicas executed commands in the same order
Commands to run from src folder: 
cd out
./script 
------------------------------------------------
Output of script can be viewed by double clicking the html created in "out" folder with the name <testcurrentdate-time.html>. 
__________________________________________________
Thats all Folks!


