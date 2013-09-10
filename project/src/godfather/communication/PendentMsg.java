package godfather.communication;

import godfather.localCommunication.PeersMsg;
import rice.p2p.commonapi.Id;

public class PendentMsg {

    public PeersMsg msgPendent;
    public Id destination;

    public PendentMsg(PeersMsg msgPendent, Id destination) {
        super();
        this.msgPendent = msgPendent;
        this.destination = destination;
    }
}
