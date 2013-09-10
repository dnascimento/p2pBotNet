package godfather.localCommunication;

import godfather.communication.MsgSenders.msgTypes.CipherMsg;
import godfather.security.SecurityTools;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;

/**
 * Message used to communicate between peers. It is send always cipher
 * 
 * @author darionascimento
 */
public class PeersMsg
        implements Serializable {
    static Logger logger = Logger.getLogger(PeersMsg.class);

    public PeersMsgTypes msgType;
    public String order;

    public String signature;
    public X509Certificate clientCertificate;

    public long nounce;


    public PeersMsg(PeersMsgTypes type, String order, long nounce) {
        this(type, order, null, null, nounce);
    }


    public PeersMsg(PeersMsgTypes msgType, String order, String signature, X509Certificate clientCertificate, long nounce) {
        super();
        this.msgType = msgType;
        this.order = order;
        this.signature = signature;
        this.clientCertificate = clientCertificate;
        this.nounce = nounce;
    }


    public void signMessage(PrivateKey privateKey) throws Exception {
        if (clientCertificate == null) {
            logger.info("Signed as Masters");
        } else {
            logger.info("Signed as Client");
        }
        signature = SecurityTools.makeDigitalSignature(this, privateKey);
    }

    public void verifySignatureIsValid(PublicKey publicMasterKey) throws Exception {
        if (signature == null) {
            throw new Exception("Not signed");
        }
        boolean valid = false;
        try {
            X509Certificate cert = this.clientCertificate;
            // Validate msg with master certificate
            if (cert == null) {
                logger.info("Going to Verify Master Signature");
                valid = checkSignatureWithPublicKey(publicMasterKey);

            } else {
                logger.info("Going to Verify Client Signature");
                cert.checkValidity();
                cert.verify(publicMasterKey);
                valid = checkSignatureWithPublicKey(cert.getPublicKey());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage());
            throw e;
        }
        if (!valid) {
            throw new Exception("Not valid");
        }
    }

    private boolean checkSignatureWithPublicKey(PublicKey issuerKey) throws Exception {
        String signLocal = signature;
        this.signature = null;
        boolean result = SecurityTools.verifyDigitalSignature(signLocal, this, issuerKey);
        this.signature = signLocal;
        return result;
    }



    public static PeersMsg factoryFromCipherMsg(CipherMsg cipher) throws Exception {
        if (cipher.isCyphered()) {
            throw new Exception("Is cipher, I can not convert it");
        }
        PeersMsg msg = new PeersMsg(cipher.messageType, cipher.order, cipher.signature, cipher.clientCertificate, cipher.nounce);
        return msg;
    }

    public CipherMsg convertToCipherMsg(Id souceId) {
        CipherMsg msg = new CipherMsg(souceId, this.msgType, order, nounce);
        msg.clientCertificate = this.clientCertificate;
        msg.signature = this.signature;
        return msg;
    }


    @Override
    public String toString() {
        return "PeersMsg [msgType=" + msgType + ", order=" + order + ", signature=" + signature + ", clientCertificate=" + clientCertificate + ", nounce=" + nounce + "]";
    }







}
