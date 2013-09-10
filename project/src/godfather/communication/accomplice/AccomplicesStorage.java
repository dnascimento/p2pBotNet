package godfather.communication.accomplice;

import godfather.Godfather;

import java.security.PublicKey;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;
import rice.pastry.NodeIdFactory;
import rice.pastry.standard.RandomNodeIdFactory;



public class AccomplicesStorage extends
        Thread {

    private final long TIME_BETWEEN_ACCOMPLICES_SEARCH = 30000;
    private final boolean running = true;

    private final ArrayList<Accomplice> accomplicesList;
    private final KeyExchanger keyExchanger;
    private final Godfather godfather;

    static Logger logger = Logger.getLogger(AccomplicesStorage.class);


    private static int ACCOMPLICES_TO_SEARCH = 2;
    private static int ACCOMPLICES_LIMIT = 10;

    public AccomplicesStorage(Godfather godfather) {
        accomplicesList = new ArrayList<Accomplice>();
        this.keyExchanger = new KeyExchanger(godfather);
        this.godfather = godfather;
    }


    @Override
    public void run() {
        try {
            Thread.sleep(5000);
            // while (running) {
            findAccomplices();
            Thread.sleep(TIME_BETWEEN_ACCOMPLICES_SEARCH);
            // }
        } catch (InterruptedException e) {
            logger.info("Accomplice Storage Refresh Stop");
        }
    }





    public void findAccomplices() {
        removeExpiredAccomplices();

        logger.info("find accomplices");
        for (int i = 0; i < ACCOMPLICES_TO_SEARCH; i++) {
            // Generate X random ID's
            NodeIdFactory nidFactory = new RandomNodeIdFactory(godfather.getEnv());
            Id randomID = nidFactory.generateNodeId();

            // Send our Public Key
            try {
                keyExchanger.askForKey(randomID);
            } catch (Exception e) {
                logger.info("Error finding accomplices: " + e);
                // i -= 1;
            }
        }
    }





    public void findPublicKey(Id destination) {
        try {
            keyExchanger.askForKey(destination);
        } catch (Exception e) {
            // This will never happen, just if the puzzle is malformed
            logger.info("Error finding PublicKey: " + e.getMessage());
        }
    }






    /**
     * Alguém pediu a nossa chave, vamos responder de volta e considerar essa pessoa nossa
     * amiga
     * 
     * @throws Exception
     */
    public void receiveAccompliceRequest(Id accompliceId, PublicKey accompliceKey) throws Exception {
        logger.debug("Accomplice request received from " + accompliceId);

        // If we have accomplices enougth, reject new accompliceship
        if (countAccomplices() >= ACCOMPLICES_LIMIT) {
            return;
        }

        // Add to our thurst accomplices list
        addAccomplice(accompliceId, accompliceKey);

        logger.debug(accomplicesList);


        try {
            keyExchanger.replyAskKey(accompliceId);
        } catch (Exception e) {
            logger.info("error receiveAccompliceRequest: " + e.getMessage());
        }
    }





    public void receiveAcceptRequest(Id idDestinationID, PublicKey destinationKey) throws Exception {
        logger.debug("recived Accept Request: " + idDestinationID);

        // Adicionar a PK à nossa lista de amigos
        addAccomplice(idDestinationID, destinationKey);

        logger.debug(accomplicesList);
    }









    // ###################### UTILS #######################################

    private int countAccomplices() {
        return accomplicesList.size();
    }


    private void addAccomplice(Id withNodeID, PublicKey key) throws Exception {
        if (key == null) {
            throw new Exception("Add Accomplice with null key");
        }
        Accomplice accomplice = new Accomplice(withNodeID, key);
        accomplicesList.add(accomplice);
    }

    public PublicKey getPublicKey(Id nodeID) throws Exception {
        for (Accomplice accomplice : accomplicesList) {
            if (accomplice.id.equals(nodeID)) {
                if (accomplice.key == null) {
                    throw new Exception("Ando a guardar chaves de amigos vazias");
                }
                return accomplice.key;
            }
        }
        throw new Exception("Accomplice not Exists");
    }


    private void removeExpiredAccomplices() {
        for (Accomplice accomplice : accomplicesList) {
            if (accomplice.isExpired()) {
                accomplicesList.remove(accomplice);
            }
        }
    }



    public void addAccomplices(Accomplice comp) {
        accomplicesList.add(comp);



    }


    public void addCredits(Id source) {
        for (Accomplice comp : accomplicesList) {
            if (comp.id == source) {
                comp.validMsgReceived++;
                logger.info("Complicer credits updated: " + comp.validMsgReceived);
                return;
            }
        }
        logger.info("complicer not found");
    }


    public ArrayList<Accomplice> getAccomplicesList() {
        return accomplicesList;
    }



}
