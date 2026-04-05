package weighttables;

import java.io.FileOutputStream;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");

        // Universal
        int NUM_KILLS = 10000;
        // Chambers
        int COX_POINTS = 3000000;
        int PARTY_SIZE = 1;
        // DT2
        String Duke = "Duke";
        String Leviathan = "Leviathan";
        String Whisperer = "Whisperer";
        String Vardorvis = "Vardorvis";
        // int TOA_INVOCATION = 300;

        // HashMap<String, Integer> test = CoX.runCoX(NUM_KILLS, COX_POINTS,
        // PARTY_SIZE);
        // String imageTest = ImageGenerator.GenerateLootImage(test,
        // COX_LOOT_IMAGE_PATH, IMAGE_FILE_FORMAT);

        // String ImageText = ("Loot from " + NUM_KILLS + " Chambers of Xeric, each with
        // "
        // + COX_POINTS + " points:");

        String HeaderText = ("Loot from " + NUM_KILLS + " " + Duke + " kills:");

        HashMap<String, Integer> test = DT2.simulateBoss(Duke, NUM_KILLS);
        byte[] imageBytes = ImageGenerator.GenerateLootImage(test, HeaderText);

        // Temporary: write to disk just to verify it still works
        try (FileOutputStream fos = new FileOutputStream("output.png")) {
            fos.write(imageBytes);
        }

        System.out.println("Generated " + imageBytes.length + " bytes");

        // HashMap<String, Integer> test = CoX.runCoX(NUM_KILLS, COX_POINTS,
        // PARTY_SIZE);
        // String imageTest = ImageGenerator.GenerateLootImage(test,
        // COX_LOOT_IMAGE_PATH, IMAGE_FILE_FORMAT, ImageText);

        // System.out.println(test.toString());

        // CoX.runCoX(NUM_KILLS, COX_POINTS, PARTY_SIZE, SHOW_NORMAL_LOOT);
        // CoX.getTbowChance(COX_POINTS)
        // DT2.simulateBoss(Vardorvis, NUM_KILLS, SHOW_NORMAL_LOOT);
    }
}
