package Servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class Servidor {
    ServerSocket providerSocket;
    Socket connection = null;
    ObjectOutputStream out;
    ObjectInputStream in;
    Scanner sc = new Scanner(System.in);
    String message;
    Servidor(){}
    void run(){
        try{
            //1. creating a server socket
            providerSocket = new ServerSocket(2004, 10);
            //2. Wait for connection
            System.out.println("Waiting for connection");
            connection = providerSocket.accept();
            System.out.println("Connection received from " + connection.getInetAddress().getHostName());
            //3. get Input and Output streams
            out = new ObjectOutputStream(connection.getOutputStream());
            out.flush();
            in = new ObjectInputStream(connection.getInputStream());
            sendMessage("Connection successful");
            //4. The two parts communicate via the input and output streams
            do{
                message = (String)in.readObject();
                System.out.println("client>" + message);
                jsonObj(message);
                sendMessage(message);
            }while(!message.equals("bye"));
        }
        catch(ClassNotFoundException classnot){
            System.err.println("Data received in unknown format");
        }
        catch(IOException ioException){
            System.err.println("Error: "+ioException.getMessage());
        }
        catch(Exception e){
            System.err.println("Error: "+e.getMessage());
        }
        
        finally{
            //4: Closing connection
            try{
                in.close();
                out.close();
                providerSocket.close();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }
    
    void sendMessage(String msg){
        try{
            out.writeObject(msg);
            out.flush();
            System.out.println("server>" + msg);
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
    
    public void jsonObj(String msg) throws Exception{
        StringBuilder nom = new StringBuilder(msg);
        cambiarEspacios(nom);
        
        String URL = getHTML(
                "http://www.giantbomb.com/api/search/?api_key=0a3892ba8bf068d15dc4016d34cfeda13dca5c5e&format=json&query="+
                        nom+"&resources=game");
        
        JSONObject jobj = new JSONObject(URL);
        
        JSONArray jarr = jobj.getJSONArray("results").getJSONObject(0).getJSONArray("platforms");
        
        String platforms = "";
        for(int x = 0; x < jarr.length(); x++)
            platforms += jarr.getJSONObject(x).getString("name")+"\n";
        
        String gameID = jobj.getJSONArray("results").getJSONObject(0).getString("api_detail_url");
        String id = getGameID(gameID);
        
        String genreURL = getHTML(
                "http://www.giantbomb.com/api/game/"+
                        id+"/?api_key=0a3892ba8bf068d15dc4016d34cfeda13dca5c5e&format=json&field_list=genres,name");
        
        jobj = new JSONObject(genreURL);
        
        jarr = jobj.getJSONObject("results").getJSONArray("genres");
        
        String genres = "";
        for(int x = 0; x<jarr.length(); x++)
            genres+=jarr.getJSONObject(x).getString("name")+"\n";
        
        String publisherURL = getHTML(
                "http://www.giantbomb.com/api/game/"+
                        id+"/?api_key=0a3892ba8bf068d15dc4016d34cfeda13dca5c5e&format=json&field_list=publishers,name");
        
        jobj = new JSONObject(publisherURL);
        
        jarr = jobj.getJSONObject("results").getJSONArray("publishers");
        
        String publishers = "";
        for(int x = 0; x<jarr.length(); x++)
            publishers += jarr.getJSONObject(x).getString("name")+"\n";
        message = "\nPlatforms: "+platforms+"\nGenres: "+genres+"\nPublishers: "+publishers;
    }
    
    public String getGameID(String url){
        String id = "";
        for(int cont = 34; cont<url.length()-1; cont++)
            id+=url.charAt(cont);
        return id;
    }
    
    public void cambiarEspacios(StringBuilder nomGame){
        for(int cont = 0; cont<nomGame.length(); cont++){
            if(nomGame.charAt(cont)==' '){
                nomGame.deleteCharAt(cont);
                nomGame.replace(cont, cont, "%20");
            }
        }
    }
    
    public static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
           result.append(line);
        }
        rd.close();
        return result.toString();
    }
    
    public static void main(String args[]){
        Servidor server = new Servidor();
        while(true){
            server.run();
        }
    }
}
