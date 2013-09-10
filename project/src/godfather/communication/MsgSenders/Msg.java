package godfather.communication.MsgSenders;

import rice.p2p.commonapi.Message;

public class Msg
        implements Message {



    private static final long serialVersionUID = -6884315024492858342L;
    private final MsgType type;

    public Msg(MsgType type) {
        this.type = type;
    }



    public MsgType getType() {
        return type;
    }



    @Override
    public int getPriority() {
        return Message.LOW_PRIORITY;
    }
}
