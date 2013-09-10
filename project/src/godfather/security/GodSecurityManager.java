package godfather.security;

import godfather.PeerType;
import godfather.security.Certificate.CertificateManager;
import godfather.security.storage.SecurityStorageKey;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;


/**
 * Create or load the Master certificate from
 */
public class GodSecurityManager {
    static Logger logger = Logger.getLogger(GodSecurityManager.class);

    private final String SECURITY_DIRECTORY = "security/";

    /** Peer Keys used for sessions */
    private final KeyPair keys;
    private X509Certificate MASTERCert;

    private final PeerType peerType;
    private CertificateManager certificateManager;

    private final boolean FORCE_NEW_KEY_PAIR = false;

    public GodSecurityManager(PeerType type) throws Exception {
        peerType = type;

        // Generate Private/Pub keys
        keys = SecurityTools.loadKeyPair(SECURITY_DIRECTORY, type.toString(), FORCE_NEW_KEY_PAIR);

        certificateManager = null;
        switch (type) {
        case master:
            certificateManager = new CertificateManager("TheGodfather", true);
            break;
        case client:
            certificateManager = new CertificateManager("client", false);
            break;
        }
        loadMASTERCert(); // Carregar o certificado do Master guardado em disco
    }





    /**
     * Carregar o certificado da MASTER que já vem com o programa
     * 
     * @throws CertificateException
     * @throws IOException
     */
    private void loadMASTERCert() throws CertificateException, IOException {
        MASTERCert = SecurityStorageKey.loadCertificate(SECURITY_DIRECTORY + "TheGodfather.cer");
    }








    /*****************************************************************************
     * get & set *
     *****************************************************************************/
    public PeerType getBottype() {
        return peerType;
    }

    public CertificateManager getCertificateManager() {
        return certificateManager;
    }

    public PublicKey getSessionPublicKey() {
        return keys.getPublic();
    }

    public X509Certificate getMASTERCert() {
        return MASTERCert;
    }

    public PrivateKey getPrivateKey() {
        return keys.getPrivate();
    }










}
