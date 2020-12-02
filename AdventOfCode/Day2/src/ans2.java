import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.*;

public class ans2 {
    public static void main(String[] args) {
        try {
            File obj = new File("/home/jonah/Documents/ForFun/AdventOfCode/Day2/data.txt");
            Scanner reader = new Scanner(obj);
            List<String> data = new ArrayList<>();
            while(reader.hasNextLine()) {
                data.add(reader.nextLine());
            }
            System.out.println(solve(data));
            reader.close();
        } catch (FileNotFoundException e){
            System.out.println("ERROR: No file detected");
            e.printStackTrace();
        }
    }
    public static int solve(List<String> data){
        int validPart1 = 0;
        int validPart2 = 0;
        for (String line : data) {
            Matcher m = Pattern.compile("(\\d+)-(\\d+) (\\w): (\\w+)").matcher(line);
            if (!m.find()) {
                throw new IllegalArgumentException("ERROR: Unable to parse: " + line);
            }
            int lower = Integer.parseInt(m.group(1));
            int upper = Integer.parseInt(m.group(2));
            char c = m.group(3).charAt(0);
            String pass = m.group(4);

            int count = 0;
            for (int i = 0; i < pass.length(); i++) {
                if (pass.charAt(i) == c)
                    count++;
            }
            if (count >= lower && count <= upper)
                validPart1++;

            boolean l = pass.charAt(lower - 1) == c;
            boolean u = pass.charAt(upper - 1) == c;
            if (l ^ u)
                validPart2++;
        }
        System.out.println(validPart1 + " valid Part1 passwords.");
        System.out.println(validPart2 + " valid Part2 passwords.");
        return 0;
    }
}
