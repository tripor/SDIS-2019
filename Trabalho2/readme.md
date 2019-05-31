SDIS-2019

To run: Open a terminal in the program directory and type this: make arguments="<'option'> <'port'> <'succ_ip'> <'succ_port'>"

where:

<'option'>
    Must be '0' if it's not the initiator server in the chord ring, or 2 if it's suppose to join this ring
<'port'>
    Port of the machine running this server where the tcp communication will happen. (Must always be 0)
<'succ_ip'>
    The successor server ip where this server will connect with in the chord ring.
<'succ_port'>
    The successor server port where the tcp communication will be set