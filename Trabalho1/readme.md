## SDIS-2019

### To setup: 
    In the program directory run: make compile <br>
    Go to the bin folder and run: rmiregistry & <br>
    In the program directory type: . ./setup.sh  <br>
### To run Server independently: 
    Open a terminal in the program directory and type this: Use 'make arguments="<protocol_version> <server_id> <access_point> <ip_address> <port_number>"' <br>

### To run Client:
    In the bin folder run: java Client <access_point> <operand_1> <operand_2> [operand_3] <br>
<br><br>
where:
<br><strong> <protocol_version> </strong><br>
    Is the version of the protocol that the server is going to run. 1.0 and 1.1 is supported. 1.1 implements the backup enhancement and the delete enhancement.
<br>
<br> <strong> <server_id> </strong><br>
    Is the identification of the server.
<br><strong> <access_point> </strong><br>
    Is rmi access point to a server. Normally is dbs(numeber of the server) ex: dbs1
