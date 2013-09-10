package godfather.commands;

import java.io.Serializable;

public interface Command
        extends Serializable {

    public void attack();

    @Override
    public String toString();
}
