SDIS-2019

To setup: In the program directory type: . ./setup.sh
To run: Open a terminal in the program directory and type this: Use make client <peer_ap> <sub_protocol> <opnd_1> <opnd_2>

where:

<peer_ap>
    Is the peer's access point, which will be a name given for each of the peers(check the names in the sh file where the servers are executed, by default they will be dbs$i)
<operation>
    Is the operation the peer of the backup service must execute. It can be either the triggering of the subprotocol to test, or the retrieval of the peer's internal state. In the first case it must be one of: BACKUP, RESTORE, DELETE, RECLAIM. In the case of enhancements, you must append the substring ENH at the end of the respecive subprotocol, e.g. BACKUPENH. To retrieve the internal state, the value of this argument must be STATE 
<opnd_1>
    Is either the path name of the file to backup/restore/delete, for the respective 3 subprotocols, or, in the case of RECLAIM the maximum amount of disk space (in KByte) that the service can use to store the chunks. In the latter case, the peer should execute the RECLAIM protocol, upon deletion of any chunk. The STATE operation takes no operands.
<opnd_2>
    This operand is an integer that specifies the desired replication degree and applies only to the backup protocol (or its enhancement) 