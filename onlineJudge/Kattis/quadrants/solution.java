import java.util.Scanner;

class solution {
    public static void main(String[] args){
        Scanner in = new Scanner(System.in);
        int x = in.nextInt();
        int y = in.nextInt();

        if(x < 0) {
            if(y < 0){
                System.out.println(3);
                return;
            }
            System.out.println(2);
        }
        if(x > 0) {
            if(y < 0) {
                System.out.println(4);
                return;
            }
            System.out.println(1);

        }




    }
}