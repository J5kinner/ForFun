/*
 * Written by Jonah Skinner
 * Advent of Code 2020
 * Finds the 3 numbers from a text file which add up to 2020
 * Then multiplies them together to find the total expense for your vacation
 *
 */
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
public class solver {
    public static void main(String[] args) {
        try {
            File obj = new File("/home/jonah/Documents/AdventOfCode/Day1/data.txt");
            Scanner reader = new Scanner(obj);
            int i = 0;
            int[] data = new int[200];
            while(reader.hasNextInt()) {
                data[i] = reader.nextInt();
                i++;
            }
            System.out.println(solve(data));

            reader.close();

        } catch (FileNotFoundException e){
                System.out.println("No file detected error");
                e.printStackTrace();
        }


    }
    public static int solve(int[] data) {
        int sum ;
        int mult ;
        for(int i: data){
            for(int j=1; j<data.length; j++){
                for(int k=2; k<data.length; k++) {
                    sum = data[i] + data[j] + data[k];
                    if (sum == 2020) {
                        mult = data[i] * data[j] * data[k];
                        System.out.println(sum);
                        return mult;
                    }
                }
            }
        }
        return 1;
    }
}
