import java.util.Scanner;

public class solution {
    public static void main(String[] args){
        Scanner in = new Scanner(System.in);
        int testCases = in.nextInt();
        String line = in.nextLine();
        int rows = in.nextInt();
        int cols = in.nextInt();
        String arr[][] = new String[rows][cols];
        char flip[][] = new char[rows][cols];

        for(int i=0; i<rows; i++){
            for(int j=0; j<cols; j++){
                arr[i][j] = in.next();
                System.out.println(arr[j][i]);
            }
        }
        in.close();
    }
}
