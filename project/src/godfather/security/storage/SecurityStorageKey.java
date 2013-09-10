package godfather.security.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.log4j.Logger;

/**
 * Biblioteca com os métodos necessários à implementação da segurança no sistema
 * distribuido
 */
public class SecurityStorageKey {
    static Logger logger = Logger.getLogger(SecurityStorageKey.class);

    /**
     * Guardar chaves publicas em ficheiro de sistema.
     * 
     * @param key par de chaves a guardar em ficheiro
     * @param publicKeyPath onde guardar a chave publica
     * @param privateKeyPath onde guardar a chave privada
     */
    public static void saveKeys(KeyPair key, String directory, String entity_name) throws Exception {

        File dir = new File(directory);

        dir.mkdirs();



        byte[] pubEncoded = key.getPublic().getEncoded();

        FileOutputStream fout = new FileOutputStream(directory + entity_name + ".pub", false);
        fout.write(pubEncoded);
        fout.flush();
        fout.close();

        byte[] privEncoded = key.getPrivate().getEncoded();

        fout = new FileOutputStream(directory + entity_name + ".priv", false);
        fout.write(privEncoded);
        fout.flush();
        fout.close();

        logger.debug("New RSA KeyPair Saved");
    }

    /**
     * Lêr chaves de um ficheiro e transformá-las em objectos.
     * 
     * @param publicKeyPath localização da chave publica: DO FICHEIRO
     * @param privateKeyPath localização da chave privada: DO FICHEIRO
     */
    public static KeyPair loadKeys(String publicKeyPath, String privateKeyPath) throws FileNotFoundException, Exception {
        FileInputStream fin = new FileInputStream(publicKeyPath);
        byte[] pubEncoded = new byte[fin.available()];
        fin.read(pubEncoded);
        fin.close();


        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
        KeyFactory keyFacPub = KeyFactory.getInstance("RSA");

        PublicKey pub = keyFacPub.generatePublic(pubSpec);

        fin = new FileInputStream(privateKeyPath);
        byte[] privEncoded = new byte[fin.available()];
        fin.read(privEncoded);
        fin.close();

        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
        KeyFactory keyFacPriv = KeyFactory.getInstance("RSA");

        PrivateKey priv = keyFacPriv.generatePrivate(privSpec);

        // logger.debug(priv);
        // logger.debug("---");
        logger.debug("RSA KeyPair Loaded");

        return new KeyPair(pub, priv);
    }


    /**
     * Guardar certificado em ficheiro de sistema. IMPORTANTE: O nome do ficheiro é
     * determinado nesta lib para possibilitar a leitura de todos os certificados de um
     * directório.
     * 
     * @param certificate: certificado a guardar
     * @param directoryPath: directório onde vai guardar o ficheiro ex: /tmp/cert/ (Acabar
     *            em /)
     * @param issuerName : nome da issure associado ao certificado para identificar o
     *            certificado guardado
     * @throws CertificateEncodingException : falha ao codificar
     * @throws IOException: falha ao guardar o ficheiro
     */
    public static void saveCertificate(X509Certificate certificate, String directoryPath, String issuerName) throws CertificateEncodingException, IOException {
        logger.debug("Writing certificate to: '" + directoryPath + issuerName + ".cer");
        byte[] certEncoded = certificate.getEncoded();
        File file = new File(directoryPath);
        file.mkdirs();
        FileOutputStream fout = new FileOutputStream(directoryPath + issuerName + ".cer");
        fout.write(certEncoded);
        fout.flush();
        fout.close();

        logger.debug("Certificated Saved: " + directoryPath + issuerName);
    }




    public static X509Certificate loadCertificate(String filePath) throws IOException, CertificateException {
        X509Certificate cert;
        FileInputStream fin = new FileInputStream(filePath);

        CertificateFactory cf = CertificateFactory.getInstance("X509");
        cert = (X509Certificate) cf.generateCertificate(fin);
        fin.close();
        // logger.debug("Certificated Loaded: " + filePath);
        return cert;

    }

}
