package godfather.twitter;

import godfather.bootstrap.BootstrapPeersList;

import java.util.List;

import org.apache.log4j.Logger;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;


public class FallBackBootStraper {

    static Logger logger = Logger.getLogger(FallBackBootStraper.class);
    static final String ACCOUNT_NAME = "Aliens_Money";
    // static final String ACCOUNT_NAME = "DHTagus";

    private final BootstrapPeersList list = new BootstrapPeersList();

    //
    // public static void main(String[] args) {
    // PropertyConfigurator.configure("log4j.properties");
    // FallBackBootStraper n = new FallBackBootStraper();
    // n.GetServers();
    // }

    public BootstrapPeersList GetServers() {
        Twitter twitter = new TwitterFactory().getInstance();

        String queryS = "from:" + ACCOUNT_NAME;

        logger.info("Retriving BootStrapServers From DHTagus twitter Account");

        try {

            Query query = new Query(queryS);
            QueryResult result = twitter.search(query);

            List<Status> tweets = result.getTweets();

            for (Status tweet : tweets) {
                String[] ipport = tweet.getText().split(":");
                if (ipport.length == 2) {
                    list.addPeer(ipport[0], ipport[1]);
                    logger.info("Server " + tweet.getText() + " Added");
                } else {
                    logger.info("Invalid Server Name: " + tweet.getText());
                }
            }

        } catch (TwitterException te) {
            te.printStackTrace();
            logger.info("Failed to search tweets: " + te.getMessage());
        }
        return list;
    }
}
