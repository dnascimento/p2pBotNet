package godfather.exception;

public class GodfatherException extends
        Exception {

    private static final long serialVersionUID = 1L;
    ExceptionEnum msg;

    public GodfatherException(ExceptionEnum msg) {
        super();
        this.msg = msg;

    }

    public ExceptionEnum getMsg() {
        return msg;
    }

    public void setMsg(ExceptionEnum msg) {
        this.msg = msg;
    }


}
