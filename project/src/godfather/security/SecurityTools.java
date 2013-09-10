package godfather.security;

import godfather.security.storage.SecurityStorageKey;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import rice.p2p.util.Base64;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;



public class SecurityTools {
    static Logger logger = Logger.getLogger(SecurityTools.class);

    private static String CIPHER_PROVIDER = "RSA/ECB/PKCS1Padding";
    private static String KEY_PAIR_ALG = "RSA";
    private static int RSA_KEY_SIZE = 2048;
    private static String DIGEST_ALG = "SHA-256";
    private static String SYMMETRIC_ALG = "AES";

    public static KeyPair createNewKeyPair() {
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance(KEY_PAIR_ALG);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.initialize(RSA_KEY_SIZE);
        KeyPair keys = keyGen.generateKeyPair();
        logger.debug("Private Key: " + writeByteArray(keys.getPrivate().getEncoded()));
        logger.debug("Public Key: " + writeByteArray(keys.getPublic().getEncoded()));
        return keys;
    }



    /***************************
     * Symetric Cipher
     * 
     * @throws
     * @throws Exception
     ******************************************/
    public static String cipherSymmetricObject(byte[] key, Serializable obj) throws Exception {
        Cipher c = Cipher.getInstance(SYMMETRIC_ALG);
        SecretKeySpec k = new SecretKeySpec(key, SYMMETRIC_ALG);
        c.init(Cipher.ENCRYPT_MODE, k);
        SealedObject sealedObject = new SealedObject(obj, c);
        return Base64.encodeObject(sealedObject);
    }


    public static Object decipherSymmetricObject(byte[] key, String cipherText) throws Exception {
        // encrypt the plaintext using the public key
        Cipher c = Cipher.getInstance(SYMMETRIC_ALG);

        SecretKeySpec k = new SecretKeySpec(key, SYMMETRIC_ALG);
        c.init(Cipher.DECRYPT_MODE, k);
        SealedObject sealedObject = (SealedObject) Base64.decodeToObject(cipherText);
        Object result = sealedObject.getObject(c);
        if (result == null) {
            throw new Exception("Can not decipher message");
        }
        return result;
    }


    /************************* Encrypt & Decrypt *****************************************/
    /**
     * Cipher the Object
     * 
     * @param key
     * @param obj
     * @return
     * @throws Exception
     */
    public static String cipherWithKey(PublicKey key, Serializable obj) throws Exception {
        // encrypt the plaintext using the public key
        Cipher cipher = Cipher.getInstance(CIPHER_PROVIDER);

        if (key == null) {
            throw new RuntimeException();
        }

        cipher.init(Cipher.ENCRYPT_MODE, key);
        SealedObject sealedObject = new SealedObject(obj, cipher);
        return Base64.encodeObject(sealedObject);
    }

    /**
     * To Decrypt objects
     * 
     * @param key
     * @param cipherText
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws Exception
     */
    public static Object decryptWithKey(PrivateKey key, String cipherText) throws IllegalBlockSizeException,
            BadPaddingException,
            IOException,
            ClassNotFoundException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException {
        // encrypt the plaintext using the public key
        Cipher cipher;
        cipher = Cipher.getInstance(CIPHER_PROVIDER);
        cipher.init(Cipher.DECRYPT_MODE, key);
        SealedObject sealedObject = (SealedObject) Base64.decodeToObject(cipherText);
        return sealedObject.getObject(cipher);
    }



    /************************* Sign *****************************************/
    /** auxiliary method to calculate digest from text and cipher it */
    public static String makeDigitalSignature(Serializable obj, PrivateKey key) throws Exception {
        String encodedObject = Base64.encodeObject(obj);


        // get a message digest object using the MD5 algorithm
        MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALG);

        // calculate the digest and print it out
        messageDigest.update(encodedObject.getBytes());
        byte[] digest = messageDigest.digest();
        // logger.debug("\nDigest: " + writeByteArray(digest));

        Cipher cipher = Cipher.getInstance(CIPHER_PROVIDER);

        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherDigest = cipher.doFinal(digest);
        // logger.debug("\nCipherDigest: " + writeByteArray(cipherDigest));




        // logger.debug("Signed!!!");

        BASE64Encoder b64e = new BASE64Encoder();
        return b64e.encodeBuffer(cipherDigest);

    }

    /**
     * auxiliary method to calculate new digest from text and compare it to the to
     * deciphered digest
     * 
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static boolean verifyDigitalSignature(String cipherString, Serializable obj, PublicKey publicKey) throws IOException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {
        BASE64Decoder b64d = new BASE64Decoder();
        byte[] cipherDigest = null;
        cipherDigest = b64d.decodeBuffer(cipherString);



        // get an RSA cipher object and print the provider
        Cipher cipher = null;
        cipher = Cipher.getInstance(CIPHER_PROVIDER);


        // decrypt the ciphered digest using the public key
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        byte[] decipheredDigest = null;
        decipheredDigest = cipher.doFinal(cipherDigest);







        String encodedObject = Base64.encodeObject(obj);

        // get a message digest object using the MD5 algorithm
        MessageDigest messageDigest = null;
        messageDigest = MessageDigest.getInstance(DIGEST_ALG);

        // calculate the digest and print it out
        messageDigest.update(encodedObject.getBytes());
        byte[] digest = messageDigest.digest();
        // logger.debug("\nDigest: " + writeByteArray(digest));






        // logger.debug("Finish decryption: ");
        // logger.debug("\nDecipheredDigest: " + writeByteArray(decipheredDigest));

        // compare digests
        if (digest.length != decipheredDigest.length) {
            return false;
        }
        for (int i = 0; i < digest.length; i++) {
            if (digest[i] != decipheredDigest[i]) {
                return false;
            }
        }
        return true;
    }


    // ##################### GERIR CERTIFICADOS ############################

    /**
     * Verificar se um certificado ainda é válido: não está na blacklist, ainda tem data
     * válida e a CA é que assinou o certificado
     * 
     * @param certificate - certificado a validar
     * @param blackList - lista de certificados revogados
     * @return true se válido, false se inválido
     */
    public static boolean certificateValidation(X509Certificate certificate, PublicKey caPublicKey, X509CRL blackList) throws CertificateException {
        try {
            certificate.checkValidity(new Date());
            if (blackList.isRevoked(certificate)) {
                logger.debug("Certificado Revogado!!!");
                throw new CertificateException("Certificado revogado");
            }
            certificate.verify(caPublicKey); // a chave que assinou o cert é da CA
        } catch (CertificateExpiredException e) {
            throw new CertificateException("Certificado expirou");
        } catch (CertificateNotYetValidException e) {
            throw new CertificateException("Certificado ainda não é válido");
        } catch (InvalidKeyException e) {
            throw new CertificateException("Não é possivel verificar certificado");
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException("Não é possivel verificar certificado");
        } catch (NoSuchProviderException e) {
            throw new CertificateException("Não é possivel verificar certificado");
        } catch (SignatureException e) {
            throw new CertificateException("Assinatura do certificado não é válida");
        }
        return true;
    }



    /**************************************************************************
     * Generate Keys
     **************************************************************************/
    /**
     * Carregar as chaves existentes em ficheiro, se não existirem, cria-as.
     * 
     * @param forceNewKeys: obrigar a gerar novas e substituir chaves existentes
     */
    public static KeyPair loadKeyPair(String keysFolderPath, String owner, boolean forceNewKeys) throws Exception {
        if (forceNewKeys) {
            generateKeys(keysFolderPath, owner);
        }
        KeyPair keyPair = null;
        try {
            String fileName = owner;
            keyPair = SecurityStorageKey.loadKeys(keysFolderPath + fileName + ".pub", keysFolderPath + fileName + ".priv");
            return keyPair;
        } catch (FileNotFoundException e) {
            logger.info("Keys not found, create new keys");
            // o ficheiro não existe, criar e depois voltará a invocar este método
            return generateKeys(keysFolderPath, owner);
        }
    }


    public static KeyPair generateKeys(String keysFolderPath, String owner) throws Exception {
        KeyPair keys = createNewKeyPair();
        // guardar chave
        SecurityStorageKey.saveKeys(keys, keysFolderPath, owner);
        return loadKeyPair(keysFolderPath, owner, false); // Load generated Keys
    }





    /**
     * Create a self-signed X.509 Certificate.
     * 
     * @param subjectName the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
     * @param pair the KeyPair
     * @param minutes how many days from now the Certificate is valid for
     * @param algorithm the signing algorithm, eg "SHA1withRSA" or "md5WithRSA"
     */
    public static X509Certificate generateCertificate(X500Name issuer, X500Name owner, PrivateKey masterPrivateKey, PublicKey publicKey, long expiration_time) throws GeneralSecurityException,
            IOException {
        X509CertInfo info = new X509CertInfo(); // 1 segundo = 1000 | 1 minuto = 60
        Date from = new Date(); // data actual
        // Durante quantos minutos e que o certificado e x valido
        Date to = new Date(from.getTime() + expiration_time * 60000l);
        // Validade do certificado
        CertificateValidity interval = new CertificateValidity(from, to);

        // Número serie do certificado
        BigInteger serialnumber = new BigInteger(64, new SecureRandom());

        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(serialnumber));
        info.set(X509CertInfo.ISSUER, new CertificateIssuerName(issuer));
        info.set(X509CertInfo.VALIDITY, interval);

        info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
        info.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));

        // By default, the encription algorithm is md5WithRSA
        String algorithm = new String("md5WithRSA");

        AlgorithmId algorithmID = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid); // MD5+RSA
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algorithmID));

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(masterPrivateKey, algorithm);

        // Update the algorith, and resign.
        algorithmID = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algorithmID);
        cert = new X509CertImpl(info);

        cert.sign(masterPrivateKey, algorithm);

        return cert;
    }










    /** Writes a byte array in hexadecimal text format */
    public static String writeByteArray(byte[] data) {
        int loc;
        int end;

        StringBuffer sb = new StringBuffer();
        int len = data.length;
        int off = 0;

        // Print a hexadecimal dump of 'data[off...off+len-1]'
        if (off >= data.length)
            off = data.length;

        end = off + len;
        if (end >= data.length)
            end = data.length;

        len = end - off;
        if (len <= 0)
            return null;

        loc = (off / 0x10) * 0x10;

        for (int i = loc; i < len; i += 0x10, loc += 0x10) {
            int j = 0;

            for (; j < 0x10 && i + j < end; j++) {
                int ch;
                int d;

                if (j == 0x08)
                    sb.append(' ');

                ch = data[i + j] & 0xFF;

                d = (ch >>> 4);
                d = (d < 0xA ? d + '0' : d - 0xA + 'A');
                sb.append((char) d);

                d = (ch & 0x0F);
                d = (d < 0xA ? d + '0' : d - 0xA + 'A');
                sb.append((char) d);

                sb.append(' ');
            }
        }

        return "[ " + sb.toString() + "], length=" + sb.length() / 3;
    }
}
