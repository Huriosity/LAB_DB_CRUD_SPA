import org.json.simple.JSONArray;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RestServerHandler  extends Thread {

    private Socket socket;

    private String directory;

    private String method;

    private String requestURL;

    private String Host;

    private String UserAgent;

    private String requestPayload;

    private DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private static final Map<String, String> CONTENT_TYPES = new HashMap<>() {{
        put("jpg", "image/jpeg");
        put("html", "text/html");
        put("json", "application/json");
        put("txt", "text/plain");
        put("", "text/plain");
    }};

    RestServerHandler(Socket socket, String directory) {
        this.socket = socket;
        this.directory = directory;
    }

    public void run() {
        try (var input = this.socket.getInputStream(); var output = this.socket.getOutputStream()) {
            parseRequest(input);
            switch (method){
                case "GET":{
                    if(isRequestInDatabase(requestURL)){
                        var type = "application/json";
                        var fileBytes =  getAllInfoFromDB().toString().getBytes("utf-8");
                        this.sendHeader(output, 200, "OK", type, fileBytes.length);

                        LogSystem.acces_log(Host, DTF.format(LocalDateTime.now()).toString(),method + " " +
                                requestURL + "HTTP/1.1", 200,fileBytes.length, requestURL, UserAgent);

                        output.write(fileBytes);
                    } else {
                        var filePath = Path.of(this.directory, requestURL);
                        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                            var extension = this.getFileExtension(filePath);
                            var type = CONTENT_TYPES.get(extension);
                            var fileBytes = Files.readAllBytes(filePath);
                            this.sendHeader(output, 200, "OK", type, fileBytes.length);

                            LogSystem.acces_log(Host, DTF.format(LocalDateTime.now()).toString(),method + " " +
                                    requestURL + "HTTP/1.1", 200,fileBytes.length,requestURL,UserAgent);

                            output.write(fileBytes);
                        } else {
                            var type = CONTENT_TYPES.get("text");
                            this.sendHeader(output, 404, "Not Found", type, HTTP_MESSAGE.NOT_FOUND_404.length());

                            LogSystem.acces_log(Host, DTF.format(LocalDateTime.now()).toString(),method + " " +
                                            requestURL + "HTTP/1.1", 404,HTTP_MESSAGE.NOT_FOUND_404.length(),
                                    requestURL, UserAgent);

                            output.write(HTTP_MESSAGE.NOT_FOUND_404.getBytes());
                        }

                    }

                    break;
                }
                case "POST":{
                    break;
                }
                case "PUT":{
                    ArrayList<ArrayList<String>> keyValuePair = parseRequestPayload();
                    for(int i = 0; i < keyValuePair.get(0).size(); i++){
                        System.out.println("fist = " + keyValuePair.get(0).get(i));
                        System.out.println("second = " + keyValuePair.get(1).get(i));
                    }

                    var type = "text/plain";
                    updateRecordInTheDB(keyValuePair);
                    // var fileBytes =  updateRecordInTheDB(keyValuePair).toString().getBytes("utf-8")
                    this.sendHeader(output, 200, "OK", type, HTTP_MESSAGE.OK_200.length());

                    LogSystem.acces_log(Host, DTF.format(LocalDateTime.now()).toString(),method + " " +
                            requestURL + "HTTP/1.1", 200,HTTP_MESSAGE.OK_200.length(),requestURL,UserAgent);

                    output.write(HTTP_MESSAGE.OK_200.getBytes());

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

    private ArrayList<ArrayList<String>> parseRequestPayload(){
        ArrayList<ArrayList<String>> HalvesOfParamenter = new ArrayList<ArrayList<String>>();
        for(int i = 0;i < 2; i++) {
            HalvesOfParamenter.add(new ArrayList<String>());
        }
        int i = 0;
        for (String retval : requestPayload.split("&")) {
            HalvesOfParamenter.get(0).add(retval.split("=",2)[0]);
            HalvesOfParamenter.get(1).add(retval.split("=",2)[1]);
            i++;
        }
        return HalvesOfParamenter;
    }

    private Boolean isRequestInDatabase(String str){
        return str.contains("?");
    }

    private String getFileExtension(Path path) {
        var name = path.getFileName().toString();
        var extensionStart = name.lastIndexOf(".");
        return extensionStart == -1 ? "" : name.substring(extensionStart + 1);
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

    public static JSONArray updateRecordInTheDB(ArrayList<ArrayList<String>> keyValuePair){
        Database.updateRecordInTheDatabase(keyValuePair);
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
