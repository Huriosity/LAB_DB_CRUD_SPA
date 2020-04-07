import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

final public class ConfigLoader {

    public static void loadConfigENV(String file){
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String headerLine = null;
            ArrayList<String> firstHalf = new ArrayList<String>();
            ArrayList<String> secondHalf = new ArrayList<String>();
            while((headerLine = br.readLine()) != null){
                // regex = all spaces include multiply spaces
                headerLine  = headerLine.replaceAll("\\s+","");
                firstHalf.add(headerLine.split("=",2)[0]);
                secondHalf.add(headerLine.split("=",2)[1]);
            }
            setEnv(firstHalf,secondHalf);

        } catch (IOException e) {
            System.err.println("ERROR: The properties file is not available!");
        } finally {
            try{
                if(fr!=null) {
                    if(br!=null) {
                        br.close();
                    }
                    fr.close();
                    System.out.println("properties filе succеsfull closеd");
                }
            }
            catch(IOException ex){

                System.out.println(ex.getMessage());
            }
        }
    }
    private static void setEnv(ArrayList<String>  key, ArrayList<String>  value) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            for(int i = 0; i < key.size();i++) {
                writableEnv.put(key.get(i), value.get(i));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
    }
}
