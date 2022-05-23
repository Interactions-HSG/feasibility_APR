package test.Encodability;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;


public class IATE {
    public static void main(String[] args) {
        try{

            String token= connectIATE();
            System.out.println(queryIATE(token, "human rights"));
            System.out.println(queryIATE(token, "drones"));
            System.out.println(queryIATE(token, "data"));
            System.out.println(queryIATE(token, "fsdfjkfls"));
            //App.connect(new URL(url_test), "iate_get_test.txt");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static boolean queryIATE(String token, String query){
        String def="";
        try{
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("https://iate.europa.eu/em-api/entries/_search?expand=true&limit=5&offset=0");
            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Content-Type", "application/json");
            httppost.setHeader("Authorization", "Bearer "+token);
            
            String query_json = "{\"query\": \""+query+" \","+ 
                "\"source\": \"en\","+
                "\"targets\": [\"en\"],"+
                "\"search_in_fields\": [0],"+
                "\"search_in_term_types\": [0,1,2,3,4],"+
                "\"query_operator\": 1}";
            StringEntity entity_query = new StringEntity(query_json);
            httppost.setEntity(entity_query);

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                BufferedReader in = new BufferedReader(new InputStreamReader(instream));

                StringBuilder sb = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    sb.append(inputLine);
                in.close();
                
                //System.out.println(sb.toString());

                JsonObject answer = new JsonParser().parse(sb.toString()).getAsJsonObject();
                def = ((JsonObject)((JsonObject)((JsonObject)((JsonArray)answer.get("items")).get(0)).get("language")).get("en")).get("definition").getAsString();
                //System.out.println(def);
            }
        }
        catch(Exception e){
            return false;
        }
        return (def.length()>0);
    }

    public static String connectIATE() throws Exception{
        String token="";
        
        String username="guitton_unisg";
        String password="2zFJXJ6MdXV7sgHe";

        //Authenticate -- need to get a token
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("https://iate.europa.eu/uac-api/ws/oauth2/token?username="
                                            +username+"&password="+password+"&grant_type=password");    
        httppost.addHeader("Accept", "application/vnd.iate.token+json; version=1");
        httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");

        //Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();    
        if (entity != null) {
            try{
                InputStream instream = entity.getContent();
                BufferedReader in = new BufferedReader(new InputStreamReader(instream));

                StringBuilder sb = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    sb.append(inputLine);
                in.close();

                JsonObject answer = new JsonParser().parse(sb.toString()).getAsJsonObject();
                token = answer.get("tokens").getAsString();
                //String refresh_token=answer.get("refresh_token").getAsString();
    
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return token;
    }
}
