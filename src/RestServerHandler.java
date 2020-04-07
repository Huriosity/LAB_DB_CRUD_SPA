import org.json.simple.JSONArray;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RestServerHandler  extends Thread {

    private Socket socket;

    private String directory;

    private String method;

    private String requestURL;

    private String Host;

    private String UserAgent;

    private String requestPayload;

    private DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    RestServerHandler(Socket socket, String directory) {
        this.socket = socket;
        this.directory = directory;
    }

    public void run() {
        try (var input = this.socket.getInputStream(); var output = this.socket.getOutputStream()) {
            parseRequest(input);
            switch (method){
                case "GET":{
                    var extension = "json";
                    var type = "application/json";
                    var fileBytes =  getAllInfoFromDB().toString().getBytes("utf-8");
                    this.sendHeader(output, 200, "OK", type, fileBytes.length);

                    LogSystem.acces_log(Host, DTF.format(LocalDateTime.now()).toString(),method + " " +
                            requestURL + "HTTP/1.1", 200,fileBytes.length, requestURL, UserAgent);

                    output.write(fileBytes);

                    break;
                }
                case "POST":{
                    break;
                }
                case "PUT":{
                    break;
                }
                case "DELETE":{
                    break;
                }

            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void parseRequest(InputStream input) throws IOException {
        InputStreamReader isReader = new InputStreamReader(input);
        BufferedReader br = new BufferedReader(isReader);

        String firstLine = br.readLine();
        method = firstLine.split(" ")[0];
        requestURL = firstLine.split(" ")[1];
        Host = br.readLine().split(" ")[1];;

        String headerLine = null;
        while((headerLine = br.readLine()).length() != 0){
            System.out.println(headerLine);
            if(headerLine.contains("User-Agent")){
                UserAgent = headerLine.split(" ",2)[1];
            }
        }

        StringBuilder payload = new StringBuilder();
        while(br.ready()){
            payload.append((char) br.read());
        }

        requestPayload = payload.toString();
    }

    private void sendHeader(OutputStream output, int statusCode, String statusText, String type, long lenght) {
        var ps = new PrintStream(output);
        ps.printf("HTTP/1.1 %s %s%n", statusCode, statusText);
        ps.printf("Date: %s%n", DTF.format(LocalDateTime.now())); ////////////////
        ps.printf("Content-Type: %s%n", type);
        ps.printf("Content-Length: %s%n%n", lenght);
    }

    public static JSONArray getAllInfoFromDB(){
        return configurateJsonArray(Database.getAllInfoFromDatabase());
    }

    private static JSONArray configurateJsonArray(DatabaseResponse databaseResponse){
        JSONArray result = new JSONArray();

        JSONArray list = new JSONArray();

        for (int i = 0; i < databaseResponse.arr_col_names.length; i ++){
            list.add(databaseResponse.arr_col_names[i]);
        }
        result.add(list);

        for (int row = 0; row < databaseResponse.fullDataFromSet[0].length; row++) {
            JSONArray list2 = new JSONArray();
            for(int col = 0;col < databaseResponse.fullDataFromSet.length; col++){
                list2.add(databaseResponse.fullDataFromSet[col][row]);
            }
            result.add(list2);
        }

        return result;
    }
}
