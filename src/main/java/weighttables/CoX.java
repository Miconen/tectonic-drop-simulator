package weighttables;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class CoX {

    static final int LOOT_ROLLS = 2;
    static final int CAPPED_POINTS = 570000;
    static final int PURPLE_LIMIT = 6;
    static final int ACCURACY = 100;
    static final double PURPLE_RATE = 8676.0;
    static final int TOTAL_P_WEIGHT = ACCURACY * ACCURACY;

    static final Random rand = new Random();

    public static HashMap<String, Integer> runCoX(int numRaids, int raidPoints, int partySize) {
        HashMap<String, Integer> output = new HashMap<>();

        JSONArray uniques = loadJson("/CoxUniques.json");
        JSONArray generics = loadJson("/CoxGenerics.json");
        if (uniques == null || generics == null)
            return output;

        for (int i = 0; i < numRaids; i++) {
            simulateRaid(output, uniques, generics, raidPoints, partySize);
        }

        return output;
    }

    private static void simulateRaid(HashMap<String, Integer> output,
            JSONArray uniques, JSONArray generics, int raidPoints, int partySize) {

        int cappedRolls = Math.min(raidPoints / CAPPED_POINTS, PURPLE_LIMIT);
        int remainingPoints = raidPoints % CAPPED_POINTS;
        int totalLoots = 0;
        int totalPurples = 0;

        // Phase 1: purple rolls using capped points
        for (int j = 0; j < cappedRolls && totalLoots < partySize && totalPurples < PURPLE_LIMIT; j++) {
            if (rollPurple(CAPPED_POINTS)) {
                addUnique(output, uniques);
                totalLoots++;
                totalPurples++;
            }
        }

        // Phase 2: fill remaining loot slots
        while (totalLoots < partySize) {
            boolean canRollPurple = totalPurples == 0 && totalPurples < PURPLE_LIMIT;

            if (canRollPurple && rollPurple(remainingPoints / partySize)) {
                addUnique(output, uniques);
                totalPurples++;
            } else {
                addNormalLoot(output, generics, raidPoints);
            }
            totalLoots++;
        }
    }

    private static boolean rollPurple(int points) {
        int weight = (int) Math.ceil(ACCURACY * (points / PURPLE_RATE));
        return rand.nextInt(TOTAL_P_WEIGHT + 1) <= weight;
    }

    private static void addUnique(HashMap<String, Integer> output, JSONArray uniques) {
        JSONObject item = WeightFunctions.rollItem(uniques);
        String name = (String) item.get("name");
        output.merge(name, 1, Integer::sum);
    }

    private static void addNormalLoot(HashMap<String, Integer> output, JSONArray generics, int raidPoints) {
        for (int j = 0; j < LOOT_ROLLS; j++) {
            JSONObject item = WeightFunctions.rollItem(generics);
            String name = (String) item.get("name");
            int divisor = ((Long) item.get("divisor")).intValue();
            int quantity = divisor == 0 ? 1 : raidPoints / divisor;
            output.merge(name, quantity, Integer::sum);
        }
    }

    private static JSONArray loadJson(String path) {
        try {
            return (JSONArray) new JSONParser().parse(
                    new InputStreamReader(CoX.class.getResourceAsStream(path)));
        } catch (Exception e) {
            System.err.println("Failed to load " + path + ": " + e.getMessage());
            return null;
        }
    }
}
