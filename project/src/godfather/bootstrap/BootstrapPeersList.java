package godfather.bootstrap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.log4j.Logger;


public class BootstrapPeersList {

    static Logger logger = Logger.getLogger(BootstrapPeersList.class);

    // private ArrayList<BootstrapPeer> bootstrapPeers ;
    private final HashMap<String, BootstrapPeer> bootstrapPeers;

    public BootstrapPeersList() {
        bootstrapPeers = new HashMap<String, BootstrapPeer>();
    }

    public BootstrapPeersList(String filePathToLoad) {
        bootstrapPeers = new HashMap<String, BootstrapPeer>();
        loadPeers(filePathToLoad);
    }


    public void loadPeers(String filePathToLoad) {
        // IP PORT \n
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream((new File(filePathToLoad)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.info("File Not Found");
            return;
        }
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader b = new BufferedReader(new InputStreamReader(in));

        String lineRead;
        try {
            while (true) {
                lineRead = b.readLine();
                if (lineRead == null) {
                    break;
                } else {
                    String[] parsed = lineRead.split(" ");
                    addPeer(parsed[0], parsed[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("Not able to read from buffer");
        }
    }

    public void addPeer(String IP, String Port) {
        BootstrapPeer newPeer = new BootstrapPeer(IP, Port);
        bootstrapPeers.put(IP, newPeer);
    }

    public void savePeerList(String pathToSave) {
        try {
            // Create file
            FileWriter fstream = new FileWriter(pathToSave);
            BufferedWriter out = new BufferedWriter(fstream);
            for (BootstrapPeer bp : bootstrapPeers.values()) {
                out.write(bp.toString());
            }
            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void removePeer(String IP) {
        bootstrapPeers.remove(IP);
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, BootstrapPeer> getBootstrapPeers() {
        return (HashMap<String, BootstrapPeer>) bootstrapPeers.clone();
    }


}
