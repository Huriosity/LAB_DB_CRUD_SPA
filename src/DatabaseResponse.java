public class DatabaseResponse {
    String[] arr_col_names;
    String[][] fullDataFromSet;

    DatabaseResponse( String[] new_arr_col_names, String[][] new_fullDataFromSet){
        arr_col_names = new_arr_col_names;
        fullDataFromSet = new_fullDataFromSet;
    }

}
