import java.util.Scanner;

public class solution {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
        String[] in = line.split(" ");
        if(Integer.parseInt(in[0]) > Integer.parseInt(in[1]) ){
            System.out.println("1");
        } else {
            System.out.println("0");

        }

    }
    
}
