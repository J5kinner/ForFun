import java.util.Scanner;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Arrays;

public class solution {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String length = in.nextLine();
        HashMap<String, String> dateList = new HashMap<String, String>();
        for(int i=0; i<Integer.parseInt(length); i++){
            String[] str = in.nextLine().split(" ", 3);
            String nameRank = str[0]+" "+ str[1];
            int rank = Integer.parseInt(str[1]);
            String date = str[2];
            //above is what we are comparing the map to
            if(dateList.containsKey(date)){ //if the date already exist then lets compare ranks
                String[] chkDate = dateList.get(date).split(" ", 2); //grabs the actual data in map
                 int chkRank = Integer.parseInt(chkDate[1]);

                if(rank > chkRank){ //if the current rank on the map is greater than the one we're looking at switch em
                    // System.out.println("Rank change! new Rank is: " + chkRank + " for " + nameRank );
                    dateList.remove(date);
                    dateList.put(date, nameRank);
                }
            } else {
                dateList.put(date, nameRank);
            }

            // System.out.println(dateList);
        }
        String arr[] = new String[dateList.size()];
        int i = 0;
        for(String value : dateList.values()){
            String clean[] = value.split(" ", 0);
            arr[i] = clean[0];
            i++;
            // System.out.println(clean[0]);
        }
        Arrays.sort(arr, String.CASE_INSENSITIVE_ORDER);
        System.out.println(arr.length);
        for(int x = 0; x<arr.length; x++){
            System.out.println(arr[x]);
        }





        // System.out.println("This is it "+ " : " + Integer.parseInt(length));
    }
}