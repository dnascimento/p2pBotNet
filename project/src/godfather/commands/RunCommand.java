package godfather.commands;


public class RunCommand extends
        Thread {

    Command command;


    public RunCommand(Command command) {
        super();
        this.command = command;
    }


    @Override
    public void run() {
        command.attack();
    }

}
