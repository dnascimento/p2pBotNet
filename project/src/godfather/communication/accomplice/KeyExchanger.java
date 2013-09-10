package godfather.communication.accomplice;

import godfather.Godfather;
import godfather.communication.MsgSenders.Msg;
import godfather.communication.MsgSenders.MsgSender;
import godfather.communication.MsgSenders.MsgType;
import godfather.communication.MsgSenders.msgTypes.KeyMsg;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.DeliveryNotification;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.MessageReceipt;



public class KeyExchanger extends
        MsgSender {

    private final Godfather godfather;
    static Logger logger = Logger.getLogger(KeyExchanger.class);


    public KeyExchanger(Godfather godfather) {
        super(godfather, "keyexchanger");
        this.godfather = godfather;
    }

    public void askForKey(Id nodeID) {
        Id ourId = godfather.getPastryNode().getId();
        PublicKey ourPublicKey = godfather.getGodSecurityManager().getSessionPublicKey();
        PrivateKey ourPrivateKey = godfather.getGodSecurityManager().getPrivateKey();

        // Send a prof-of-work
        KeyMsg msgToSend;
        try {
            msgToSend = new KeyMsg(ourId, MsgType.AskKey, ourPublicKey, ourPrivateKey);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        DeliveryNotification notification = new DeliveryNotification() {
            @Override
            public void sendFailed(MessageReceipt arg0, Exception arg1) {
                logger.info("Not able to send Ask Key");
            }

            @Override
            public void sent(MessageReceipt arg0) {
                // logger.info("Ask Key Sent Ok");
            }
        };
        routeMsgId(nodeID, msgToSend, notification);
    }

    @Override
    public void deliver(Id id, Msg msgReceived) {
        if ((msgReceived.getType() != MsgType.AskKey) && (msgReceived.getType() != MsgType.ReplyAskKey)) {
            return;
        }
        KeyMsg msg = (KeyMsg) msgReceived;

        logger.info("Received Message in KeyExchanger of type: " + msg.keyMsgtype);


        MsgType messageType = msg.keyMsgtype;
        try {
            switch (messageType) {
            case AskKey:
                receivedAskKey(msg);
                break;
            case ReplyAskKey:
                receivedReplyAskKey(msg);
                break;
            default:
                logger.info("Unknown message type");
                break;
            }

        } catch (Exception e) {
            logger.info("Error deliver: " + e.getMessage());
        }
    }

    /**
     * Received accomplice request, asking for my key
     * 
     * @param msgReceived
     * @throws Exception
     */
    public void receivedAskKey(KeyMsg msg) throws Exception {
        // Solve the prof of work
        msg.decipherThisKeyMsg();



        // Check If I'm talking with myself,
        if (msg.fromNodeID.equals(godfather.getPastryNode().getId())) {
            // Not doesnt accept requests from myself
            logger.debug("to my self, ignore");
            return;
        }

        // ACCEPT REQUEST:
        godfather.getCommunicationAPI().accomplicesStorage.receiveAccompliceRequest(msg.fromNodeID, msg.senderPublicKey);


    }


    public void replyAskKey(Id destinationId) throws Exception {
        Id ourId = godfather.getPastryNode().getId();
        PublicKey ourPublicKey = godfather.getGodSecurityManager().getSessionPublicKey();
        PrivateKey ourPrivateKey = godfather.getGodSecurityManager().getPrivateKey();


        KeyMsg msgToSend = new KeyMsg(ourId, MsgType.ReplyAskKey, ourPublicKey, ourPrivateKey);


        // Responder de volta
        DeliveryNotification notification = new DeliveryNotification() {
            @Override
            public void sendFailed(MessageReceipt arg0, Exception arg1) {
                logger.info("Not able to send Reply Ask Key");
            }

            @Override
            public void sent(MessageReceipt arg0) {
                // logger.info("Reply Ask Key Sent Ok");
            }
        };
        routeMsgId(destinationId, msgToSend, notification);
    }


    public void receivedReplyAskKey(KeyMsg msg) throws Exception {
        // Solve prof-of-work
        msg.decipherThisKeyMsg();


        // Check If I'm talking with myself,
        if (msg.fromNodeID.equals(godfather.getPastryNode().getId())) {
            // Not doesnt accept requests from myself
            logger.debug("to my self, ignore");
            return;
        }



        logger.debug("Received Reply Ask Key");
        // ACCEPT REQUEST:
        godfather.getCommunicationAPI().accomplicesStorage.receiveAcceptRequest(msg.fromNodeID, msg.senderPublicKey);
        godfather.getCommunicationAPI().notifyNewPublicKeyArrive(msg.fromNodeID, msg.senderPublicKey);


    }

}
