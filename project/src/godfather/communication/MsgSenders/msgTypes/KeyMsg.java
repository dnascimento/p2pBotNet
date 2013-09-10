package godfather.communication.MsgSenders.msgTypes;

import godfather.communication.MsgSenders.Msg;
import godfather.communication.MsgSenders.MsgType;
import godfather.security.Cryptopuzzle;
import godfather.security.SecurityTools;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;

public class KeyMsg extends
        Msg {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(KeyMsg.class);

    public Id fromNodeID;
    public String nodeIDCipher;
    public PublicKey senderPublicKey;
    public String publicKeyCipher;
    public final MsgType keyMsgtype;
    public final Cryptopuzzle puzzle;



    public KeyMsg(Id fromNodeId, MsgType type, PublicKey senderPublicKey, PrivateKey privKey) throws Exception {
        super(type);
        this.fromNodeID = fromNodeId;
        this.senderPublicKey = senderPublicKey;
        this.keyMsgtype = type;

        this.puzzle = new Cryptopuzzle(senderPublicKey, privKey);
        byte[] solution = puzzle.extractSolution();

        cipherThisKeyMsg(solution);
    }

    private void cipherThisKeyMsg(byte[] solution) throws Exception {
        solution = keyPadding(solution);

        // Cipher the ID and the PublickKey
        nodeIDCipher = SecurityTools.cipherSymmetricObject(solution, fromNodeID);
        publicKeyCipher = SecurityTools.cipherSymmetricObject(solution, senderPublicKey);
        fromNodeID = null;
        senderPublicKey = null;
    }

    public void decipherThisKeyMsg() throws Exception {
        byte[] solution = puzzle.solve();
        byte[] secret = puzzle.getJoinSecretPart(solution);

        solution = keyPadding(secret);

        // Decipher the ID and PublicKey
        fromNodeID = (Id) SecurityTools.decipherSymmetricObject(solution, nodeIDCipher);
        senderPublicKey = (PublicKey) SecurityTools.decipherSymmetricObject(solution, publicKeyCipher);
        nodeIDCipher = "";
        publicKeyCipher = "";

    }


    private byte[] keyPadding(byte[] solution) {
        byte key[] = new byte[16];
        int solLength = solution.length;

        for (int k = 0; k < 16; k++) {
            key[k] = solution[solLength - k - 1];
        }
        return key;
    }
}
