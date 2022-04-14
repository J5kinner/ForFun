import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class solution {
    public static void main(String[] args){
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();

        char arr[] = s.toCharArray();
        HashMap<Character, Integer> unique = new HashMap<Character, Integer>();
        Set<Character> nonUnique = new HashSet<>();
        for(int i=0; i<s.length(); i++){
            if(unique.containsKey(arr[i])){
                nonUnique.add(arr[i]);
            }   
            if(!unique.containsKey(arr[i])){
                unique.put(arr[i], i);
            }
        
        }
        //solve by going through the map and checking with the set
        for(int j=0; j<s.length(); j++){
            if(unique.containsValue(j)){
                System.out.println(j+1);
            }
        }

        //Add word to a set ->
        //Check if letter is already in set 
        //if not then that letter is now unique -> store the 1-based index
        //return the latest unique index. 
        //else return -1;
    }
}