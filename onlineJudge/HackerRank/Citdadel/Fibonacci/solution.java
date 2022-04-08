import java.util.Scanner;
import java.util.*;

public class solution {
    public static void main(String[] args){
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        ArrayList<Integer> arr = new ArrayList<Integer>();
        arr.add(0);
        arr.add(1);
        for(int i=1; i<n; i++){
           int result = arr.get(i) + arr.get(i-1);
           arr.add(result);
        }
        for (Integer j : arr) {
            System.out.println(j);        

        }
    }
}