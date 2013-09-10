package godfather.communication.accomplice;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Date;

import rice.p2p.commonapi.Id;



public class Accomplice
        implements Serializable, Comparable<Accomplice> {

    // milisecounds
    private static final long MAX_TIME_WITHOUT_RECEIVE_MSG = 60000;

    public Id id;
    public PublicKey key;
    public long validMsgReceived;
    public Date lastMsgReceived;

    public Accomplice(Id id, PublicKey key) {
        super();
        this.id = id;
        this.key = key;
        this.validMsgReceived = 0;
        this.lastMsgReceived = new Date();
    }


    public boolean isExpired() {
        long delta = new Date().getTime() - lastMsgReceived.getTime();
        return delta > MAX_TIME_WITHOUT_RECEIVE_MSG;
    }





    @Override
    public String toString() {
        return "Accomplice [id=" + id + ", key=" + key + ", validMsgReceived=" + validMsgReceived + ", lastMsgReceived=" + lastMsgReceived + "]";
    }


    @Override
    public int compareTo(Accomplice other) {
        long delta = new Date().getTime() - lastMsgReceived.getTime();
        if ((this.validMsgReceived > other.validMsgReceived) && (!this.isExpired())) {
            return 1;
        } else {
            return -1;
        }
    }
}
