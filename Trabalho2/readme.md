SDIS-2019

To run Server: Open a terminal in the program directory and type this: make arguments="<option> <port> <succ_ip> <succ_port>"
The java command in the bin folder will not work since the program is dependent of the files folder and the current path

To be the first server in the ring <succ_ip> and <succ_port> must be 0

where:

<option>
    Must be '0' if we want to use the external ip, or !=0 if we want to use the localhost ip
<port>
    Port of the machine running this server where the tcp communication will happen. (Recommended to be 0, so that the port is a available port in this machine)
<succ_ip>
    The successor server ip where this server will connect with in the chord ring.
<succ_port>
    The successor server port where the tcp communication will be set

To run Client: Open a terminal in the program directory and type this: make client arguments="<option> <ip> <port> [other options]"
The java command in the bin folder will not work since the program is dependent of the files folder and the current path

where:

<option>
    Can be BACKUP,RESTORE,DELETE,RECLAIM,STATE for the differents protocols
<ip>
    Ip of a server so that we can request information
<port>
    Port of the server so that we can request information
[other options]
    Depends on the protocol used. BACKUP: <file_name> <rep_degree>. RESTORE: <file_name>. DELETE <file_name>. RECLAIM <new_space>.
    <file_name>
        The name of the file we want to interact. Should be the name and not the path. If BACKUP the file should be stored in files/client. If RESTORE the file is restored to files/client/restored
    <rep_degree>
        The replication degree we want for the file
    <new_space>
        The new max space for the server
