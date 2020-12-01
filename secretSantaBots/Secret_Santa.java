import java.util.ArrayList;
import java.util.Collections;

public class secretSanta {
    public static void main(String[] args) {
        ArrayList<String> donor = new ArrayList<String>();
        donor.add("Bröthńar");
        donor.add("Byörk");
        donor.add("Onyx");
        donor.add("Bone Daddy");
        donor.add("Njørd VPN");
        donor.add("Jean Baptiste...");
        donor.add("Franklin 0.5");
        donor.add("Tall Scary One");
        donor.add("RoboHobo");
        ArrayList<String> receiver = (ArrayList<String>) donor.clone();

        Collections.shuffle(donor);
        for (int i = 0; i < donor.size(); i++) {
            Collections.shuffle(receiver);
            int target = 0;
            if(receiver.get(target).equals(donor.get(i))) {
                target++;
            }
            System.out.println("Hey there good lookin, " + donor.get(i) + " you're secret gift is for " + receiver.get(target));
            receiver.remove(receiver.get(target));
        }
    }
}
