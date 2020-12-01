import java.util.Scanner;
import java.util.HashMap;


class Main2 {
    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);

        HashMap<Character,Character> reverses = new HashMap<>();
       
        reverses.put('A','A');
        reverses.put('E','3');
        reverses.put('H','H');
        reverses.put('I','I');
        reverses.put('J','L');
        reverses.put('L','J');
        reverses.put('M','M');
        reverses.put('O','O');
        reverses.put('S','2');
        reverses.put('T','T');
        reverses.put('U','U');
        reverses.put('V','V');
        reverses.put('W','W');
        reverses.put('X','X');
        reverses.put('Y','Y');
        reverses.put('Z','5');
        reverses.put('1','1');
        reverses.put('2','S');
        reverses.put('3','E');
        reverses.put('5','Z');
        reverses.put('8','8');

        while(sc.hasNextLine()) {
            String str = sc.nextLine();

            boolean isPalindrome = true;
            boolean isMirrored = true;
            for(int i = 0; i < str.length()/2; i++) {
                char leftChar = str.charAt(i);
                char rightChar = str.charAt(str.length() - 1 - i);
                if(leftChar != rightChar) {
                    isPalindrome = false;
                }
                if(!reverses.containsKey(leftChar) || reverses.get(leftChar) != rightChar) {
                    isMirrored = false;
                }

                if(!isPalindrome && !isMirrored) {
                    break;
                }
            }

            // Check if odd then check if middle char is reverse
            if(str.length() % 2 != 0) {
                char middleChar = str.charAt(str.length()/2);
                if(!reverses.containsKey(middleChar) || reverses.get(middleChar) != middleChar) {
                    isMirrored = false;
                }
            }

            if(isPalindrome && isMirrored) {
                System.out.println(str + " -- is a mirrored palindrome.\n");
            } else if(isPalindrome) {
                System.out.println(str + " -- is a regular palindrome.\n");
            } else if(isMirrored) {
                System.out.println(str + " -- is a mirrored string.\n");
            } else {
                System.out.println(str + " -- is not a palindrome.\n");
            }
        }
    }
}
