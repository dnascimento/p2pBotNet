package godfather.menu;

import godfather.Godfather;

import java.util.Scanner;

import org.apache.log4j.Logger;



public class BotUI {
    static Logger logger = Logger.getLogger(BotUI.class);

    private final Godfather app;
    boolean running = true;


    public BotUI(Godfather app) {
        this.app = app;
        logger.info("Type help for Menu");
    }


    public void start() {
        logger.info("\n Write help to show the list of Commands \n");
        logger.info("\n You are a bot, would you like a cookie? !\n");
        String input = null;
        while (running) {
            // reads from user input if no commands loaded
            input = getUserInput();
            selectMenu(input);
        }
    }


    /**
     * Selects the comand selected by the User
     */
    private void selectMenu(String input) {
        String[] parsed = input.split(" ");

        try {
            if (parsed.length > 0) {
                if (parsed[0].length() == 0) {
                    return;
                }
                MenuOption opt = MenuOption.valueOf(parsed[0]);
                switch (opt) {
                case exit:
                    running = false;
                    break;
                case help:
                    showCommandList();
                    break;
                // case update:
                // UpdateRelayList();
                // break;
                default:
                    logger.info("Command do not exists");
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.info("Invalid Command");
        } catch (StringIndexOutOfBoundsException e) {
            logger.info("Invalid Command");
        } catch (IllegalArgumentException e) {
            logger.info("Invalid Command");
        }
    }

    private void showCommandList() {
        logger.info("\n DHTagus BotNet Bot Menu:\n");
        logger.info("update");
    }


    /**
     * Method used to get User Input
     */
    private String getUserInput() {
        Scanner scan = new Scanner(System.in);
        String userInput = scan.nextLine();
        return userInput;
    }


    // private void UpdateRelayList() {
    // app.updateRelayList();
    // }


}
