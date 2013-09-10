package godfather.security.Certificate;


import godfather.security.SecurityTools;
import godfather.security.storage.SecurityStorageKey;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import org.apache.log4j.Logger;

import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;



public class CertificateManager {
    static Logger logger = Logger.getLogger(CertificateManager.class);

    private final X500Name issuer = new X500Name("CN=TheGodfather");
    private final X500Name owner;
    private X509Certificate controllerCert;
    private KeyPair controllerKeyPair;

    private final long CERTIFICATE_EXPIRATION_TIME = 99999; // in minutes
    private final String SECURITY_DIRECTORY = "security/";
    private final String GENERATED_CLIENT_PATH = "security/clients/";

    // True if you want create a new certificate for master
    private final boolean FORCE_NEW_CERTIFICATE = false;
    public final HashMap<String, X509CertImpl> approvedCertificates;
    private final boolean master;

    private long nounce;

    // The difference between master and a client is the name.
    public CertificateManager(String name, boolean master) throws Exception {
        approvedCertificates = new HashMap<String, X509CertImpl>();
        owner = new X500Name("CN=" + name);
        this.master = master;
        loadCertificateAndKeys(FORCE_NEW_CERTIFICATE);

        // TODO Se quisermos mobilidade do cliente, os nounce têm de ser inicializados
        // pela rede com base no último nounce
        nounce = 0;
    }



    public void loadCertificateAndKeys(boolean forceCreation) throws Exception {
        // Load certificate and Keys
        try {
            controllerCert = SecurityStorageKey.loadCertificate(SECURITY_DIRECTORY + owner.getCommonName() + ".cer");
            controllerKeyPair = SecurityStorageKey.loadKeys(SECURITY_DIRECTORY + owner.getCommonName() + ".pub", SECURITY_DIRECTORY + owner.getCommonName() + ".priv");
        } catch (Exception e) {
            logger.info("Certificate not found, create new");
            // If can't load, generate new certificate and keys
            generateCertificateAndKeys();
        }
    }


    public void generateCertificateAndKeys() throws Exception {
        if (!master) {
            throw new Exception("You are not master, you can't create  certificates");
        }
        controllerKeyPair = SecurityTools.generateKeys(SECURITY_DIRECTORY, owner.getCommonName());
        X509Certificate cert = SecurityTools.generateCertificate(issuer, owner, controllerKeyPair.getPrivate(), controllerKeyPair.getPublic(), CERTIFICATE_EXPIRATION_TIME);
        // guardar em disco o certificado gerado
        SecurityStorageKey.saveCertificate(cert, SECURITY_DIRECTORY, issuer.getCommonName());
        controllerCert = cert;
        logger.debug(controllerKeyPair);
        logger.debug(controllerCert);
    }



    public void createClientCertificateAndKeys(String clientname) throws Exception {
        if (!master) {
            throw new Exception("You are not master, you can't create  certificates");
        }
        KeyPair clientKeys = SecurityTools.createNewKeyPair();
        X500Name owner = new X500Name("CN=" + clientname);
        X509Certificate cert = SecurityTools.generateCertificate(issuer, owner, controllerKeyPair.getPrivate(), clientKeys.getPublic(), CERTIFICATE_EXPIRATION_TIME);
        try {
            SecurityStorageKey.saveCertificate(cert, GENERATED_CLIENT_PATH + clientname + "/", "client");
        } catch (Exception e) {
            logger.info("Could not create client certificate: " + e.getMessage());
        }
        try {
            SecurityStorageKey.saveKeys(clientKeys, GENERATED_CLIENT_PATH + clientname + "/", "client");
            logger.debug(controllerKeyPair);
            logger.debug(controllerCert);
        } catch (Exception e) {
            logger.info("Could not Save Keys: " + e.getMessage());
        }
    }


    /**
     * @return an exclusive nounce from master
     */
    public long getMasterNounce() {
        return nounce++;
    }


    // #########################################################################
    // # set & get
    // ########################################################################


    public PublicKey getPublicKey() {
        return controllerKeyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return controllerKeyPair.getPrivate();
    }


    public X509Certificate getCertificate() {
        return controllerCert;
    }







}
