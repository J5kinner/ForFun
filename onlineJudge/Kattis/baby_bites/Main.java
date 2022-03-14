import java.util.Scanner;

public class Main {
    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);
        int length = sc.nextInt(); 
        

        for(int i=0; i<length; i++ ) {
            if( sc.next() != "mumble") {
                System.out.println(sc.next());
            }
        }

    }

}