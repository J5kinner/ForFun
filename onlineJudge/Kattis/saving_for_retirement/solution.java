import java.util.Scanner;
import java.util.ArrayList;

public class solution {
    public static void main(String[] args){
        Scanner in = new Scanner(System.in);   

        String line = in.nextLine();
        String[] spl = line.split(" ");

        int bobAge = Integer.parseInt(spl[0]);
        int bobR = Integer.parseInt(spl[1]);
        int bobEarnings = Integer.parseInt(spl[2]);
        int aliceAge = Integer.parseInt(spl[3]);
        int aliceEarnings = Integer.parseInt(spl[4]);

        int years = bobR - bobAge;
        int aTotal = 0;
        int bTotal = years * bobEarnings;


        while(aTotal <= bTotal){
            aliceAge++;
            aTotal += aliceEarnings;
        }
        System.out.println(aliceAge);



        

        in.close();
    }

    
}
