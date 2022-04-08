import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class solution {
    public static void main(String[] args){
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();

        char arr[] = s.toCharArray();
        HashMap<Character, Integer> unique = new HashMap<Character, Integer>();
        Set<Character> nonUnique = new HashSet<>();
        for(int i=0; i<s.length(); i++){
            if(!unique.containsKey(arr[i])){
                unique.put(arr[i], i);
            }
        
        }
        for(int j=0; j<s.length(); j++){
            if(unique.containsValue(j)){
                System.out.println(j+1);
            }
        }
    }
}