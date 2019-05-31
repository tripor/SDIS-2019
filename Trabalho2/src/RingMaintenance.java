package src;

import java.net.InetSocketAddress;

/**
 * RingMaintenance
 */
public class RingMaintenance implements Runnable {

    private Node belongs;

    public RingMaintenance(Node belongs) {
        this.belongs = belongs;
    }

    @Override
    public void run() {
        Colours.printBlue("\n-->Starting ring maintenance\n");
        InetSocketAddress succ = this.belongs.getSuccessor();
        if (succ == null) {
            Colours.printBlue("This server has no successor\n");
        } else {
            try {
                // Perguntar ao successor qual é o seu predecedor
                Messages message = new Messages(succ);
                String[] splitedHeader = message.SendYourPre();
                String ip = splitedHeader[1];
                int port = Integer.parseInt(splitedHeader[2]);
                if (ip.equals("null")) {
                    // Notificar que eu sou o predecessor dele
                    Colours.printBlue("Notifying that I'm the predecessor of the successor\n");
                    InetSocketAddress self = this.belongs.getSelfAddress();
                    Messages preSucMessage = new Messages(succ);
                    if (!preSucMessage.SendIamPredecessor(self.getAddress().getHostAddress(), Integer.toString(self.getPort()))) {
                        Colours.printRed(
                                "A error has ocurred while trying to notify the successor that I'm his predecessor\n");
                    }
                } else {
                    InetSocketAddress address = new InetSocketAddress(ip, port);
                    if (!address.equals(this.belongs.getSelfAddress())) {
                        long addressValue = Hash.hashBytesInteger(address.hashCode() + address.getPort());
                        long succValue = Hash.hashBytesInteger(succ.hashCode() + succ.getPort());
                        if (Hash.isBetween(this.belongs.getSelfAddressInteger(), addressValue, succValue)) {
                            // Se o predecedor do successor tiver um id mais baixo, este passa a ser o meu
                            // successor
                            try {
                                InetSocketAddress self = this.belongs.getSelfAddress();
                                Messages preSucMessage = new Messages(ip, port);
                                if (!preSucMessage.SendIamPredecessor(self.getAddress().getHostAddress(),Integer.toString(self.getPort()))) {
                                    throw new Exception();
                                }
                                this.belongs.getFingerTable().clear();
                                this.belongs.getFingerTable().setPosition(1, address);
                                Colours.printYellow("A new successor was set up. Details: \n");
                                Colours.printYellow("\tIp address:- ");
                                System.out.println(ip);
                                Colours.printYellow("\tPort:- ");
                                System.out.println(port);
                            } catch (Exception e) {
                                Colours.printRed("A error has ocurred while trying to set up a new successor. Maybe it is not alive\n");
                            }

                        }
                    }
                }

            } catch (Exception e) {
                Colours.printRed("A error has ocurred while trying to be the successor predecessor. Maybe it is not alive\n");
            }
        }
        Colours.printBlue("-->Ring maintenance has ended\n");
    }

}