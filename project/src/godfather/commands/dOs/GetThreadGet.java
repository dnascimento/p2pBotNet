package godfather.commands.dOs;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;


public class GetThreadGet extends
        Thread {

    private final HttpClient httpClient;
    private final HttpContext context;
    private final HttpGet httpget;

    public GetThreadGet(HttpClient httpClient, HttpGet httpget) {
        this.httpClient = httpClient;
        this.context = new BasicHttpContext();
        this.httpget = httpget;
    }

    @Override
    public void run() {
        try {
            HttpResponse response = this.httpClient.execute(this.httpget, this.context);

            HttpEntity entity = response.getEntity();
            if (entity != null) {


                // read it with BufferedReader
                BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                System.out.println(sb.toString());
                System.out.println(response.getStatusLine());
            }
            // ensure the connection gets released to the manager
            EntityUtils.consume(entity);
        } catch (Exception ex) {
            ex.printStackTrace();
            this.httpget.abort();
        }
    }
}
