import java.util.Scanner;
import java.util.HashMap;


public class p1 {

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		 HashMap<Character,Character> reverse = new HashMap<>();
        reverse.put('A','A');
        reverse.put('E','3');
        reverse.put('H','H');
        reverse.put('I','I');
        reverse.put('J','L');
        reverse.put('L','J');
        reverse.put('M','M');
        reverse.put('O','O');
        reverse.put('S','2');
        reverse.put('T','T');
        reverse.put('U','U');
        reverse.put('V','V');
        reverse.put('W','W');
        reverse.put('X','X');
        reverse.put('Y','Y');
        reverse.put('Z','5');
        reverse.put('1','1');
        reverse.put('2','S');
        reverse.put('3','E');
        reverse.put('5','Z');
        reverse.put('8','8');

		while ( in.hasNextLine() ) {
			String str = in.nextLine();
			boolean isPalindrome = true;
			boolean isMirrored = true;
			int size = str.length();
			for(int i=0; i < size/2; i++){
				char lChar = str.charAt(i);
				char rChar = str.charAt(size - 1 -i);
				if(lChar != rChar){
					isPalindrome = false;
				}//check whether any of the chars are reversed?
				if(!reverse.containsKey(lChar) || reverse.get(lChar) != rChar) {
					isMirrored = false;
				}
				if(!isPalindrome && !isMirrored){
					break;
				}
			}
			if(size % 2 != 0){
				char middleChar = str.charAt(size/2);
				if(!reverse.containsKey(middleChar) || (reverse.get(middleChar) != middleChar)){
					isMirrored = false;
				}
		}
		if(isPalindrome && isMirrored){
			System.out.println(str+" -- is a mirrored palindrome.\n");
		} else if(isPalindrome){
			System.out.println(str+" -- is a regular palindrome.\n");
		} else if(isMirrored){
			System.out.println(str+" -- is a mirrored string.\n");
		} else {
			System.out.println(str+" -- is not a palindrome.\n");
		}

	}
	in.close();
}

}
