import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		int tc = in.nextInt();
		for (int i = 0; i < tc; i++) {
			int l = in.nextInt(), w = in.nextInt(), h = in.nextInt();
			if (l <= 20 && w <= 20 && h <= 20)
				System.out.println(String.format("Case %d: good", i + 1));
			else
				System.out.println(String.format("Case %d: bad", i + 1));
		}
		in.close();

	}

}
