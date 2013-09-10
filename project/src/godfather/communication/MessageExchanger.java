package godfather.communication;

import godfather.Godfather;
import godfather.communication.MsgSenders.Msg;
import godfather.communication.MsgSenders.MsgSender;
import godfather.communication.MsgSenders.MsgType;
import godfather.communication.MsgSenders.msgTypes.CipherMsg;
import godfather.localCommunication.PeersMsg;

import java.security.PublicKey;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.DeliveryNotification;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.MessageReceipt;


public class MessageExchanger extends
        MsgSender {

    static Logger logger = Logger.getLogger(MessageExchanger.class);

    private final Godfather godfather;
    private final CommunicationAPI communicationsCenter;

    public MessageExchanger(Godfather godfather, String instanceName, CommunicationAPI communicationsCenter) {
        super(godfather, instanceName);
        this.godfather = godfather;
        this.communicationsCenter = communicationsCenter;
    }

    /**
     * Convert the local Msg Type to a Cipher Message and send It
     * 
     * @param msg
     * @param targetPublicKey
     */
    public void sendMessage(PeersMsg msg, Id targetNodeId, PublicKey targetPublicKey) {
        if (msg == null || targetNodeId == null || targetPublicKey == null) {
            logger.debug("Send msg with empty field: " + msg + targetNodeId + targetPublicKey);
        }
        Id ourId = godfather.getPastryNode().getId();

        // Create Msg
        logger.info("Sent nouce " + msg.nounce);
        CipherMsg msgToSend = msg.convertToCipherMsg(ourId);

        // Cypher
        msgToSend.cypherMessage(targetPublicKey);

        // Send
        DeliveryNotification notification = new DeliveryNotification() {
            @Override
            public void sendFailed(MessageReceipt arg0, Exception arg1) {
                logger.info("Not able to send Command");
            }

            @Override
            public void sent(MessageReceipt arg0) {
                // logger.info("***\nCommand Sent Ok\n***");
            }
        };
        routeMsgId(targetNodeId, msgToSend, notification);
    }

    @Override
    public void deliver(Id id, Msg msgReceived) {
        if (!msgReceived.getType().equals(MsgType.CipherMsg)) {
            return;
        }
        CipherMsg msg = (CipherMsg) msgReceived;
        // Uncypher
        try {
            msg.decypherMessage(godfather.getGodSecurityManager().getPrivateKey());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Can decipher message");
            return;
        }

        PeersMsg localMsg;
        try {
            localMsg = PeersMsg.factoryFromCipherMsg(msg);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("I can not convert the message back");
            return;
        }

        try {
            communicationsCenter.receiveCommand(localMsg, msg.source);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }



}
