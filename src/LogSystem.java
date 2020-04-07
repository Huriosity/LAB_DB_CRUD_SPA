import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LogSystem {
    private static String path = "log\\";
    private static String accesLogFileName = "accesLog.log";


    public static void acces_log(String host,String date, String request, int  status,
                                 int byteLength,String referer,String UserAgent){
        var filePath = Path.of(path + accesLogFileName);
        if (!Files.exists(filePath)){
            File form = new File(filePath.toString());
        }
        try(FileWriter writer = new FileWriter(filePath.toFile(), true))
        {
            writer.write(host + "   " + date + "   " + request + "   " + status + "   "  + byteLength + "   " +
                    referer + "   " + UserAgent + "\n");

            writer.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
