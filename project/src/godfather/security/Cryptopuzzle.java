package godfather.security;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Logger;

import rice.p2p.util.Base64;



public class Cryptopuzzle
        implements Serializable {
    static Logger logger = Logger.getLogger(Cryptopuzzle.class);
    private static String DIGEST_ALG = "SHA-1";
    // Bytes
    private static int NOUNCE_SIZE = 5;
    // Number of bytes set to zero at maximum (smaller than nounce size)
    private static int DIFFICULT = 2;

    private final Date date;
    private final byte[] timebyte;
    private final PublicKey pk;
    byte[] nounceX;
    private final String signedSolution;
    private final String signedDate;
    private final byte[] target;
    private byte[] temporarySolution;
    private byte[] solved;

    private final boolean debugToFile = false;


    private Cryptopuzzle(Date date, byte[] timebyte, PublicKey pk, byte[] nounceX, String signedSolution, String signedDate, byte[] target) {
        super();
        this.date = date;
        this.timebyte = timebyte;
        this.pk = pk;
        this.nounceX = nounceX;
        this.signedSolution = signedSolution;
        this.signedDate = signedDate;
        this.target = target;
    }


    public Cryptopuzzle(PublicKey keyA, PrivateKey privateKey) throws Exception {
        pk = keyA;

        date = new Date();
        timebyte = date.toString().getBytes();
        signedDate = SecurityTools.makeDigitalSignature(date.toString(), privateKey);

        MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALG);
        messageDigest.update(pk.getEncoded());
        byte[] keyDigest = messageDigest.digest();



        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte[] nounce = random.generateSeed(NOUNCE_SIZE);


        byte[] secret = byteContactenation(timebyte, keyDigest);
        byte[] solution = byteContactenation(secret, nounce);

        temporarySolution = solution;


        // System.out.println("Solution:");
        // showBytes(solution);


        // System.out.println("Nounce Original:");
        // showBytes(nounce);


        messageDigest.update(solution);
        target = messageDigest.digest();
        signedSolution = SecurityTools.makeDigitalSignature(target, privateKey);

        // System.out.println("Target (solution digested):");
        // showBytes(target);


        nounceX = nounce.clone();
        for (int i = 0; i < DIFFICULT; i++) {
            nounceX[i] = Byte.MIN_VALUE;
        }

        // logger.debug("Tip nounce:");
        // showBytes(nounceX);
    }

    public byte[] extractSolution() {
        byte[] solution = temporarySolution;
        temporarySolution = null;
        return solution;
    }


    public static void main(String[] args) throws Exception {
        KeyPair pair = SecurityTools.createNewKeyPair();
        // for (int i = 0; i < 20; i++) {
        Cryptopuzzle puz = new Cryptopuzzle(pair.getPublic(), pair.getPrivate());

        String msg = puz.convertToStringMsg();
        Cryptopuzzle puz1 = puz.factoryFromString(msg);

        System.out.println(puz1.equals(puz));
    }


    public byte[] getJoinSecretPart(byte[] nounce) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(DIGEST_ALG);
        } catch (NoSuchAlgorithmException e) {
            // Never happen
            e.printStackTrace();
            return null;
        }


        messageDigest.update(pk.getEncoded());
        byte[] keyDigest = messageDigest.digest();
        byte[] secret = byteContactenation(timebyte, keyDigest);
        return byteContactenation(secret, nounce);
    }




    public byte[] solve() throws Exception {
        boolean carry = false;
        byte[] state = nounceX.clone();
        long hits = 0;

        MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALG);
        messageDigest.update(pk.getEncoded());
        byte[] keyDigest = messageDigest.digest();
        byte[] begin = byteContactenation(timebyte, keyDigest);


        for (int x = 0; x < DIFFICULT;) {
            // showBytes(state);
            hits++;

            byte[] solution = byteContactenation(begin, state);
            messageDigest.update(solution);

            if (Cryptopuzzle.byteArrayEquals(target, messageDigest.digest())) {
                if (debugToFile) {
                    stampOnFile(new Long(hits).toString());
                }
                solved = state;
                return state;
            }


            if (carry) {
                if (state[x] == Byte.MAX_VALUE) {
                    state[x] = Byte.MIN_VALUE;
                    x++;
                } else {
                    carry = false;
                    state[x]++;
                    x = 0;
                }
                continue;
            }

            if (state[x] == Byte.MAX_VALUE) {
                for (int y = 0; y <= x; y++) {
                    state[y] = Byte.MIN_VALUE;
                }
                carry = true;
                x++;
                continue;
            } else {
                state[x]++;
            }
        }
        return null;
    }

    /** Helper **/
    public static byte[] byteContactenation(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        // copy a to result
        System.arraycopy(a, 0, result, 0, a.length);
        // copy b to result
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }


    public static void showBytes(byte[] data) {
        String bytes = "";
        for (int i = data.length - 1; i >= 0; i--) {
            bytes += String.format("0x%02X", data[i]);
        }
        logger.debug(bytes);
    }

    public static boolean byteArrayEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < b.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }


    public boolean checkSolution(PublicKey sourcePublicKey, byte[] solutionNounce, long challangeDuration) throws Exception {
        // Check tamper-proff
        if (SecurityTools.verifyDigitalSignature(signedSolution, target, sourcePublicKey) == false) {
            logger.info("Solution tamper");
            return false;
        }

        if (SecurityTools.verifyDigitalSignature(signedDate, date.toString(), sourcePublicKey) == false) {
            logger.info("Time tamper");
            return false;
        }

        if (date.getTime() + challangeDuration < new Date().getTime()) {
            logger.info("Time Exceded");
            return false;
        }



        // Generate the String solution with the nounce attempt
        MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALG);
        messageDigest.update(sourcePublicKey.getEncoded());
        byte[] keyDigest = messageDigest.digest();
        byte[] secret = byteContactenation(timebyte, keyDigest);
        byte[] solutionAttempt = byteContactenation(secret, solutionNounce);
        messageDigest.update(solutionAttempt);

        if (Cryptopuzzle.byteArrayEquals(target, messageDigest.digest())) {
            return true;
        }

        return false;
    }





    public static void stampOnFile(String text) {
        try {
            FileWriter fstream = new FileWriter("puzzle.txt", true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(text + " \n");
            // Close the output stream
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String convertToStringMsg() {
        return date.getTime() + "####" + Base64.encodeBytes(timebyte) + "####" + Base64.encodeObject(pk) + "####" + Base64.encodeBytes(nounceX) + "####" + signedSolution + "####" + signedDate
                + "####" + Base64.encodeBytes(target);
    }

    public static Cryptopuzzle factoryFromString(String str) throws Exception {
        String parse[] = str.split("####");

        if (parse.length < 6) {
            throw new Exception("Invalid string source");
        }

        Date date = new Date(Long.parseLong(parse[0]));
        byte[] timebyte = Base64.decode(parse[1]);
        PublicKey pk = (PublicKey) Base64.decodeToObject(parse[2]);
        byte[] nounceX = Base64.decode(parse[3]);
        String signedSolution = parse[4];
        String signedDate = parse[5];
        byte[] target = Base64.decode(parse[6]);

        return new Cryptopuzzle(date, timebyte, pk, nounceX, signedSolution, signedDate, target);
    }


    @Override
    public String toString() {
        return "Cryptopuzzle [date=" + date + ", timebyte=" + Arrays.toString(timebyte) + ", pk=" + pk + ", nounceX=" + Arrays.toString(nounceX) + ", signedSolution=" + signedSolution
                + ", signedDate=" + signedDate + ", target=" + Arrays.toString(target) + "]";
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + Arrays.hashCode(nounceX);
        result = prime * result + ((pk == null) ? 0 : pk.hashCode());
        result = prime * result + ((signedDate == null) ? 0 : signedDate.hashCode());
        result = prime * result + ((signedSolution == null) ? 0 : signedSolution.hashCode());
        result = prime * result + Arrays.hashCode(target);
        result = prime * result + Arrays.hashCode(timebyte);
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Cryptopuzzle other = (Cryptopuzzle) obj;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (!Arrays.equals(nounceX, other.nounceX))
            return false;
        if (pk == null) {
            if (other.pk != null)
                return false;
        } else if (!pk.equals(other.pk))
            return false;
        if (signedDate == null) {
            if (other.signedDate != null)
                return false;
        } else if (!signedDate.equals(other.signedDate))
            return false;
        if (signedSolution == null) {
            if (other.signedSolution != null)
                return false;
        } else if (!signedSolution.equals(other.signedSolution))
            return false;
        if (!Arrays.equals(target, other.target))
            return false;
        if (!Arrays.equals(timebyte, other.timebyte))
            return false;
        return true;
    }





}
