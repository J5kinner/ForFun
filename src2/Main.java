import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		String tc = in.nextLine();
		int i = 0;
		while(!tc.equals("*")) {
				if (tc.equals("Hajj") ){
					System.out.println(String.format("Case %d: Hajj-e-Akbar", i + 1));
					i++;
				} else {
					System.out.println(String.format("Case %d: Hajj-e-Asghar", i + 1));
					i++;
				}
				tc = in.nextLine();
		}
		in.close();
	}
}
