package godfather.communication.MsgSenders.msgTypes;

import godfather.PeerType;
import godfather.communication.MsgSenders.Msg;
import godfather.communication.MsgSenders.MsgType;
import godfather.localCommunication.PeersMsgTypes;
import godfather.security.SecurityTools;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;




public class CipherMsg extends
        Msg {

    static Logger logger = Logger.getLogger(CipherMsg.class);

    public Id source;
    public String order;
    public boolean cyphered;
    public PeersMsgTypes messageType;
    public long nounce;
    public PeerType peerType;

    public X509Certificate clientCertificate;
    public String signature;



    public CipherMsg(Id souceId, PeersMsgTypes messageType, String order, long nounce) {
        super(MsgType.CipherMsg);
        this.messageType = messageType;
        this.order = order;
        this.source = souceId;
        this.nounce = nounce;

    }









    public void cypherMessage(PublicKey publicKey) {

        try {
            order = SecurityTools.cipherWithKey(publicKey, order);
            cyphered = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Could not Cypher Object " + e.getMessage());
        }
    }

    public void decypherMessage(PrivateKey privateKey) throws InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            IOException,
            ClassNotFoundException {
        order = (String) SecurityTools.decryptWithKey(privateKey, order);
        cyphered = false;
    }


    /**
     * @return the cyphered
     */
    public boolean isCyphered() {
        return cyphered;
    }

}
