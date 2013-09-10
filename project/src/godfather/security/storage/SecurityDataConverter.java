package godfather.security.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;


/**
 * Biblioteca para realizar a conversão de objectos de segurança como chaves e
 * certificados para formato string para serem transmitidas na rede em formato texto
 * garantido o formato canónico do XML.
 */
@SuppressWarnings(value = { "all" })
final public class SecurityDataConverter {
    /**
     * Codificar uma chave, ex: PrivateKey ou PublicKey para String para ser transmitida
     * por XML.
     * 
     * @param key chave a codificar
     * @return string da chave convertida
     */
    public static String keyToText(Key key) {
        byte[] bits = key.getEncoded();
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(bits);
    }

    /**
     * Converter uma string numa chave privada.
     * 
     * @param text the text
     * @return the private key
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidKeySpecException the invalid key spec exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public static PrivateKey textToPrivateKey(String text) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] privEncoded = decoder.decodeBuffer(text);

        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
        KeyFactory keyFacPriv = KeyFactory.getInstance("RSA");

        PrivateKey priv = keyFacPriv.generatePrivate(privSpec);
        return priv;
    }

    /**
     * Converter uma string numa chave publica.
     * 
     * @param text the text
     * @return the public key
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidKeySpecException the invalid key spec exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public static PublicKey textToPublicKey(String text) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] privEncoded = decoder.decodeBuffer(text);

        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(privEncoded);
        KeyFactory keyFacPub = KeyFactory.getInstance("RSA");

        PublicKey priv = keyFacPub.generatePublic(pubSpec); // diferenca esta aqui
        return priv;
    }


    /**
     * Converter um certificado numa string.
     * 
     * @param certificate the certificate
     * @return the string
     * @throws CertificateEncodingException the certificate encoding exception
     */
    public static String certificateToText(X509Certificate certificate) throws CertificateEncodingException {
        byte[] data = certificate.getEncoded();
        BASE64Encoder b64e = new BASE64Encoder();
        return b64e.encode(data);
    }


    /**
     * Converter os dados de uma string no certificado condificado nesta.
     * 
     * @param certificateString the certificate string
     * @return the x509 certificate
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws CertificateException the certificate exception
     */
    public static X509CertImpl TextToCertificate(String certificateString) throws IOException, CertificateException {
        BASE64Decoder b64e = new BASE64Decoder();
        byte[] certBit = b64e.decodeBuffer(certificateString);

        InputStream inStream = new ByteArrayInputStream(certBit);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509CertImpl) cf.generateCertificate(inStream);
    }


    /* ############ CRL ########## */
    public static String crlToText(X509CRL crl) throws CRLException {
        byte[] data = crl.getEncoded();
        BASE64Encoder b64e = new BASE64Encoder();
        return b64e.encode(data);
    }

    public static X509CRLImpl textToCRL(String string) throws CRLException, IOException {
        BASE64Decoder b64e = new BASE64Decoder();
        byte[] certBit = b64e.decodeBuffer(string);

        return new X509CRLImpl(certBit);
    }




}
