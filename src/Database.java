import java.sql.*;
import java.util.ArrayList;

public class Database {
    private static String url = System.getenv().get("db_url");
    private static String username = System.getenv().get("db_username");
    private static String password = System.getenv().get("db_password");

    public static DatabaseResponse getAllInfoFromDatabase(){
        ResultSet resultSet = null;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            System.out.println("Connection succesfull!");
            try (Connection connection = DriverManager.getConnection(url, username,password)){
                System.out.println("Connection to rus_ruller DB succesfull!");
                Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                resultSet = statement.executeQuery("select ruller.*,ruller_town_relation.*,town.*," +
                        "ruller_years_of_life.year_of_birth,ruller_years_of_life.year_of_death from ruller," +
                        "ruller_town_relation,town,ruller_years_of_life WHERE (ruller.ruller_ID = " +
                        "ruller_town_relation.foreight_ruller_ID AND ruller_town_relation.foreight_town_ID = " +
                        "town.town_ID AND ruller_years_of_life.foreight_ruller_ID = ruller.ruller_ID);");
                ResultSetMetaData rsmd = resultSet.getMetaData();
                int colNum = rsmd.getColumnCount();
                int rowNum = 0;
                try {
                    resultSet.last();
                    rowNum = resultSet.getRow();
                    resultSet.beforeFirst();
                } catch(Exception ex) {
                    System.err.println(ex);
                }
                String[] arrColNames = new String[colNum];
                String[][] fullDataFromSet = new String[colNum][rowNum];
                for(int i = 0; i < colNum; i++){
                    arrColNames[i] = rsmd.getColumnName(i+1);
                }
                int iter = 1;

                while (resultSet.next()) {// тУТ ДВИЖЕНИЕ ПО ROW
                    for(int i = 0; i < colNum; i++){
                        if(rsmd.getColumnClassName(i+1) == "java.lang.Integer"){
                            try {
                                fullDataFromSet[i][iter-1] = Integer.toString(resultSet.getInt(i+1));
                            }catch (Exception err){
                                System.err.println(err);
                            }
                        }else if(rsmd.getColumnClassName(i+1) == "java.lang.String"){
                            try {
                                fullDataFromSet[i][iter-1] = (String) resultSet.getString(i+1);
                            } catch (Exception err){
                                System.err.println(err);
                            }
                        }
                    }
                    iter++;
                }
                return new  DatabaseResponse(arrColNames,fullDataFromSet);
            }
        } catch (Exception ex){
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
        return null;
    }

    public static void createNewRecordInTheDatabase(ArrayList<ArrayList<String>> keyValuePair){
        String rullerFirstname = null;
        String rullerPatronomic = null;
        String rullerTitle = null;
        String yearOfBirth = null;
        String yearOfDeath = null;
        String townName = null;
        int startYearOfReign = -1;
        int endYearOfReign = -1;

        for (int i = 0; i < keyValuePair.get(0).size(); i++ ){

            String currentKey = keyValuePair.get(0).get(i);
            String currentValue = keyValuePair.get(1).get(i);

            if (currentKey.equals("ruller_firstname")){
                rullerFirstname = currentValue;

            } else if(currentKey.equals("ruller_patronomic")){
                rullerPatronomic = currentValue;

            } else if(currentKey.equals("ruller_title")){
                rullerTitle = currentValue;

            } else if(currentKey.equals("year_of_birth")){
                yearOfBirth = getYearsOfLifiFromKeyValuePair(currentValue);

            } else if(currentKey.equals("year_of_death")){
                yearOfDeath = getYearsOfLifiFromKeyValuePair(currentValue);

            } else if(currentKey.equals("town_name")){
                townName = currentValue;

            } else if(currentKey.equals("start_year")){
                startYearOfReign = getIntFromKeyValuePair(currentValue);

            } else if(currentKey.equals("end_year")){
                endYearOfReign = getIntFromKeyValuePair(currentValue);
            }
        }
        int rullerId = getRullerId(rullerFirstname, rullerPatronomic, rullerTitle);

        if (!checkFieldExistence( townName,"town" )){
            String sqlRequest = "INSERT town(town_name) VALUES ('" + townName + "');";
            executeTheGivenСommandForTheDatabase(sqlRequest);
        }
        if (rullerId == -1){
            String sqlRequestIntoRuller = "INSERT ruller (ruller_firstname,ruller_patronomic,ruller_title) VALUES ('" +
                    rullerFirstname + "','" + rullerPatronomic + "','" + rullerTitle + "');";
            executeTheGivenСommandForTheDatabase(sqlRequestIntoRuller);

            int lastRullerId = getRullerId(rullerFirstname, rullerPatronomic, rullerTitle);
            int townId = getTownIdByTownName(townName);

            String sqlRequestIntoRullerYearsOfLife = "INSERT ruller_years_of_life(foreight_ruller_ID,year_of_birth," +
                    "year_of_death) VALUES (" + lastRullerId +",'" +  yearOfBirth + "','" + yearOfDeath + "');";


            String sqlRequestIntoRullerTownRelation = "INSERT ruller_town_relation(foreight_ruller_ID," +
                    "foreight_town_ID,start_year,end_year) VALUES (" + lastRullerId + "," + townId + "," +
                    startYearOfReign + "," + endYearOfReign + ");";

            executeSeveralTheGivenСommandsForTheDatabase(sqlRequestIntoRullerYearsOfLife,sqlRequestIntoRullerTownRelation);

        } else{
            int town_ID = getTownIdByTownName(townName);
            String sqlRequest = "INSERT ruller_town_relation(foreight_ruller_ID,foreight_town_ID,start_year,end_year) " +
                    "VALUES (" + rullerId + "," + town_ID + "," + startYearOfReign + "," + endYearOfReign + ");";

            executeTheGivenСommandForTheDatabase(sqlRequest);

        }
    }

    public static void updateRecordInTheDatabase(ArrayList<ArrayList<String>> keyValuePair){

        String rullerFirstname = null;
        String rullerPatronomic = null;
        String rullerTitle = null;
        String yearOfBirth = null;
        String yearOfDeath = null;
        String townName = null;
        int startYearOfReign = -1;
        int endYearOfReign = -1;
        int rullerId = -1;
        int oldTownId = -1;

        for (int i = 0; i < keyValuePair.get(0).size(); i++ ){

            String currentKey = keyValuePair.get(0).get(i);
            String currentValue = keyValuePair.get(1).get(i);

            if (currentKey.equals("ruller_firstname")){
                rullerFirstname = currentValue;

            } else if(currentKey.equals("ruller_patronomic")){
                rullerPatronomic = currentValue;

            } else if(currentKey.equals("ruller_title")){
                rullerTitle = currentValue;

            } else if(currentKey.equals("year_of_birth")){
                yearOfBirth = getYearsOfLifiFromKeyValuePair(currentValue);

            } else if(currentKey.equals("year_of_death")){
                yearOfDeath = getYearsOfLifiFromKeyValuePair(currentValue);

            } else if(currentKey.equals("town_name")){
                townName = currentValue;

            } else if(currentKey.equals("start_year")){
                startYearOfReign = getIntFromKeyValuePair(currentValue);

            } else if(currentKey.equals("end_year")){
                endYearOfReign = getIntFromKeyValuePair(currentValue);

            } else if (currentKey.equals("ruller_ID")){
                rullerId = getIntFromKeyValuePair(currentValue);

            } else if (currentKey.equals("foreight_town_ID")){
                oldTownId = getIntFromKeyValuePair(currentValue);

            }
        }

        int foreightTownId = getTownIdByTownName(townName);

        if (foreightTownId == -1){
            String sqlRequest = "INSERT town(town_name) VALUES ('" + townName + "');";

            executeTheGivenСommandForTheDatabase(sqlRequest);

            foreightTownId = getTownIdByTownName(townName);
            sqlRequest = "UPDATE ruller_town_relation SET start_year = "
                    + startYearOfReign + ", end_year = " + endYearOfReign + ", foreight_town_ID = " + foreightTownId +
                    " WHERE foreight_ruller_ID = " + rullerId +
                    " AND foreight_town_ID = " + oldTownId + ";";

            executeTheGivenСommandForTheDatabase(sqlRequest);
        } else {

            String sqlRequestIntoRullerTownRelation = "UPDATE ruller_town_relation SET start_year = "
                    + startYearOfReign + ", end_year = " + endYearOfReign + ", foreight_town_ID = " + foreightTownId +
                    " WHERE foreight_ruller_ID = " + rullerId +
                    " AND foreight_town_ID = " + oldTownId + ";";

            executeTheGivenСommandForTheDatabase(sqlRequestIntoRullerTownRelation);
        }

        String sqlRequestIntoRullerYearsOfLife = "UPDATE ruller_years_of_life SET year_of_birth = '" + yearOfBirth +
                "',year_of_death = '" + yearOfDeath + "' WHERE foreight_ruller_ID = " + rullerId + ";";

        String sqlRequestIntoRuller = "UPDATE ruller SET ruller_firstname = '" + rullerFirstname +
                "', ruller_patronomic = '" + rullerPatronomic + "', ruller_title = '" + rullerTitle +
                "' WHERE ruller_ID = " + rullerId + ";" ;

        executeSeveralTheGivenСommandsForTheDatabase(sqlRequestIntoRullerYearsOfLife,sqlRequestIntoRuller);

    }

    public static void deleteRecordInTheDatabase(ArrayList<ArrayList<String>> keyValuePair){
        int foreighRullerId = -1;
        int foreightTownId = -1;
        int startYearOfReign = -1;
        int endYearOfReign = -1;

        for (int i = 0; i < keyValuePair.get(0).size(); i++ ) {

            String currentKey = keyValuePair.get(0).get(i);
            String currentValue = keyValuePair.get(1).get(i);

            if (currentKey.equals("foreight_ruller_ID")) {
                foreighRullerId = getIntFromKeyValuePair(currentValue);

            } else if (currentKey.equals("foreight_town_ID")) {
                foreightTownId = getIntFromKeyValuePair(currentValue);

            } else if (currentKey.equals("start_year")) {
                startYearOfReign = getIntFromKeyValuePair(currentValue);

            } else if (currentKey.equals("end_year")) {
                endYearOfReign = getIntFromKeyValuePair(currentValue);

            }
        }

        int existingRullerRelations = getRullerCountOfExistingRullerTownRelationships(foreighRullerId);
        int existingTownRelations = getTownCountOfExistingRullerTownRelationships(foreightTownId);
        String sqlRequestDeleteFromRullerTownRelation = "DELETE FROM ruller_town_relation WHERE foreight_ruller_ID = " +
                foreighRullerId + " AND foreight_town_ID = " + foreightTownId + " AND start_year = "
                + startYearOfReign + " AND end_year = " + endYearOfReign + ";";

        if (existingRullerRelations == 1){
            String sqlRequestDeleteFromRuller = "DELETE FROM ruller WHERE ruller_ID = " + foreighRullerId;
            String sqlRequestDeleteFromRullerYearsOfLife = "DELETE FROM ruller_years_of_life WHERE " +
                    "foreight_ruller_ID = " +  foreighRullerId;
            if (existingTownRelations == 1 ) {
                String sqlRequestDeleteFromTown = "DELETE FROM TOWN WHERE town_ID = " + foreightTownId;
                executeSeveralTheGivenСommandsForTheDatabase(sqlRequestDeleteFromRullerTownRelation,
                        sqlRequestDeleteFromRullerYearsOfLife, sqlRequestDeleteFromRuller, sqlRequestDeleteFromTown);
            } else {
                executeSeveralTheGivenСommandsForTheDatabase(sqlRequestDeleteFromRullerTownRelation,
                        sqlRequestDeleteFromRullerYearsOfLife,sqlRequestDeleteFromRuller);
            }

        } else if ( existingTownRelations == 1 ) {
            String sqlRequestDeleteFromTown = "DELETE FROM TOWN WHERE town_ID = " + foreightTownId;
            executeSeveralTheGivenСommandsForTheDatabase(sqlRequestDeleteFromRullerTownRelation,
                    sqlRequestDeleteFromTown);
        } else {
            executeTheGivenСommandForTheDatabase(sqlRequestDeleteFromRullerTownRelation);
        }
    }

    private static String getYearsOfLifiFromKeyValuePair(String str) {
        if(str.isEmpty()){
            return "UNKNOWN";
        }
        return str;
    }

    private static int getIntFromKeyValuePair(String str){
        if(str.isEmpty()){
            return -1;
        }
        return Integer.parseInt(str);
    }

    private static int getRullerId(String ruller_firstname, String ruller_patronomic, String ruller_title){
        String sqlRequest = null;
        sqlRequest = "select (ruller_ID) from ruller WHERE ruller_firstname = '" + ruller_firstname +
                "' AND ruller_patronomic = '" + ruller_patronomic + "' AND ruller_title = '" + ruller_title + "';";

        System.out.println("SQL REQUEST = " + sqlRequest);
        return getSomeIntValueFromTheFirstCellFromSqlRequest(sqlRequest);
    }

    private static int getTownIdByTownName(String town_name){
        String sqlRequest = null;
        sqlRequest = "select (town_ID) from town WHERE town_name = '" + town_name + "';";
        System.out.println("SQL REQUEST = " + sqlRequest);
        return getSomeIntValueFromTheFirstCellFromSqlRequest(sqlRequest);
    }

    private static int getRullerCountOfExistingRullerTownRelationships(int ruller_ID){
        String sqlRequest = "SELECT COUNT(*) FROM ruller_town_relation WHERE foreight_ruller_ID = " + ruller_ID +  ";";
        return getSomeIntValueFromTheFirstCellFromSqlRequest(sqlRequest);
    }

    private static int getTownCountOfExistingRullerTownRelationships(int town_ID){
        String sqlRequest = "SELECT COUNT(*) FROM ruller_town_relation WHERE foreight_town_ID = " + town_ID +  ";";
        return getSomeIntValueFromTheFirstCellFromSqlRequest(sqlRequest);
    }

    private static int getSomeIntValueFromTheFirstCellFromSqlRequest(String sqlRequest){
        int ivalue = -1;
        ResultSet resultSet = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            System.out.println("Connection succesfull!");
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                System.out.println("Connection to rus_ruller DB succesfull!");
                Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                resultSet = statement.executeQuery(sqlRequest);
                while (resultSet.next()) {// тУТ ДВИЖЕНИЕ ПО ROW
                    ivalue = resultSet.getInt(1);
                }
                System.out.println("GET ivalue = " + ivalue);
            }
        } catch (Exception ex){
            System.out.println("Connection failed...");
            System.out.println(ex);
        }

        return ivalue;
    }

    private static Boolean checkFieldExistence(String willCheck, String whereCkeck){
        String sqlRequest = "Select * FROM " + whereCkeck;
        ResultSet resultSet = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            System.out.println("Connection succesfull!");
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                System.out.println("Connection to rus_ruller DB succesfull!");
                Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                resultSet = statement.executeQuery(sqlRequest);
                String tmp = null;
                while (resultSet.next()) {// тУТ ДВИЖЕНИЕ ПО ROW
                    tmp = resultSet.getString(2);
                    System.out.println("TMP == " + tmp);
                    System.out.println("willcheck == " + willCheck);
                    if (tmp.equals( willCheck )){
                        return true;
                    }
                }
            }
        } catch (Exception ex){
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
        return false;
    }

    private static void executeTheGivenСommandForTheDatabase(String sqlRequest){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            System.out.println("Connection succesfull!");
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                System.out.println("Connection to rus_ruller DB succesfull!");
                Statement statement = connection.createStatement();
                System.out.println("Current sqlRequest = " + sqlRequest);
                int rows = statement.executeUpdate(sqlRequest);
                System.out.printf("Added %d rows", rows);
            }
        } catch (Exception ex){
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
    }

    private static void  executeSeveralTheGivenСommandsForTheDatabase(String ...sqlRequest){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            System.out.println("Connection succesfull!");
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                System.out.println("Connection to rus_ruller DB succesfull!");
                for(int i = 0; i < sqlRequest.length; i++) {
                    Statement statement = connection.createStatement();
                    System.out.println("Current sqlRequest = " + sqlRequest[i]);
                    int rows = statement.executeUpdate(sqlRequest[i]);
                    System.out.printf("Added %d rows", rows);
                }
            }
        } catch (Exception ex){
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
    }
}
