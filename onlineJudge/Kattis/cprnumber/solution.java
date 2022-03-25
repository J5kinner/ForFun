import java.util.Scanner;
import java.util.ArrayList;
import java.lang.String;

public class solution {
    public static void main(String[] args) {
    Scanner in = new Scanner(System.in);
        ArrayList<Integer> digits = new ArrayList<>();
        String line = in.nextLine();

        for (int i = 0; i < line.length(); i++) {
            if (i == 6) {
                continue;
            }
            digits.add(Integer.parseInt(line.substring(i, i + 1)));
        }

        int[] multipliers = { 4, 3, 2, 7, 6, 5, 4, 3, 2, 1 };
        int total = 0;
        for (int i = 0; i < digits.size(); i++) {
            total += digits.get(i) * multipliers[i];
        }

        if (total % 11 == 0) {
            System.out.println("1");
        } else {
            System.out.println("0");

        }

        in.close();

    }

}