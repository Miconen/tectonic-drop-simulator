package weighttables;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Random;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DT2 {

    public static boolean HAS_TABLET = false; // Default false.
    public static int VESTIGE_ROLLS = 0; // default 0.

    public static int FOOD_MINIMUM = 3;
    public static int FOOD_MAXIMUM = 4;

    public static HashMap<String, Integer> simulateBoss(String InputBossName, int numKills) {
        JSONParser parser = new JSONParser();
        JSONArray BossArray = null;
        JSONArray UniversalDT2Uniques = null;
        JSONArray VirtusTable = null;
        HAS_TABLET = false;

        HashMap<String, Integer> output = new HashMap<String, Integer>();

        InputStream bossStream = DT2.class.getResourceAsStream("/" + InputBossName + ".json");
        if (bossStream == null) {
            throw new IllegalArgumentException("Unknown boss: " + InputBossName);
        }

        try {
            BossArray = (JSONArray) parser.parse(new InputStreamReader(bossStream));
            UniversalDT2Uniques = (JSONArray) parser.parse(new InputStreamReader(
                    DT2.class.getResourceAsStream("/DT2Uniques.json")));
            VirtusTable = (JSONArray) parser.parse(new InputStreamReader(
                    DT2.class.getResourceAsStream("/Virtus.json")));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load boss data: " + e.getMessage(), e);
        }

        JSONObject BossUniques = (JSONObject) BossArray.get(0);
        JSONObject BossSupplies = (JSONObject) BossArray.get(1);

        int PetChance = ((Long) BossUniques.get("Pet Chance")).intValue();
        String PetName = (String) BossUniques.get("Pet Name");

        Random rand = new Random();
        int roll;

        System.out.println("The Player killed " + InputBossName + " " + numKills + " times, receiving: ");

        for (int i = 1; i <= numKills; i++) {
            String LootName = rollDT2Loot(BossArray, UniversalDT2Uniques, VirtusTable);

            switch (LootName) {
                case "Supply_roll":
                    String FoodName = (String) BossSupplies.get("food");
                    int FoodNum = rand.nextInt(FOOD_MINIMUM, FOOD_MAXIMUM + 1); // 2nd argument is exclusive.
                    String Potion1Name = (String) BossSupplies.get("potion1");
                    String Potion2Name = (String) BossSupplies.get("potion2");

                    if (output.containsKey(FoodName)) {
                        output.put(FoodName, (output.get(FoodName) + FoodNum));
                        output.put(Potion1Name, (output.get(Potion1Name) + 1));
                        output.put(Potion2Name, (output.get(Potion2Name) + 1));
                    } else {
                        output.put(FoodName, FoodNum);
                        output.put(Potion1Name, 1);
                        output.put(Potion2Name, 1);
                    }
                    break;
                case "Normal_loot":
                    JSONObject NormalLoot = WeightFunctions.rollItem(BossArray);
                    String NormalLootName = (String) NormalLoot.get("name");
                    int Quantity = ((Long) NormalLoot.get("quantity")).intValue();

                    if (output.containsKey(NormalLootName)) {
                        output.put(NormalLootName, (output.get(NormalLootName) + Quantity));
                    } else {
                        output.put(NormalLootName, Quantity);
                    }
                    break;
                default:
                    if (output.containsKey(LootName)) {
                        output.put(LootName, (output.get(LootName) + 1));
                    } else {
                        output.put(LootName, 1);
                    }
                    break;
            }

            // Roll for pet
            roll = rand.nextInt(PetChance + 1);
            if (roll == PetChance) {
                if (output.containsKey(PetName)) {
                    output.put(PetName, (output.get(PetName) + 1));
                } else {
                    output.put(PetName, 1);
                }
            }
        }

        return output;
    }

    public static String rollDT2Loot(JSONArray BossArray, JSONArray UniversalUniques,
            JSONArray VirtusTable) {
        JSONObject BossUniques = (JSONObject) BossArray.get(0);

        Random rand = new Random();
        int roll;

        // First, check if the loot roll is a Unique
        int UniqueChance = ((Long) BossUniques.get("Unique Chance")).intValue();

        roll = rand.nextInt(UniqueChance + 1);
        if (roll == UniqueChance) {
            // Roll the Unique Table
            JSONObject Unique = WeightFunctions.rollItem(UniversalUniques);
            String name = (String) Unique.get("name");

            // Check if the Unique is a Vestige Roll
            if (name.equals("Vestige_roll")) {
                VESTIGE_ROLLS++;
                if (VESTIGE_ROLLS >= 3) {
                    VESTIGE_ROLLS = 0;
                    String vestigeName = (String) BossUniques.get("Vestige Name");
                    return vestigeName;
                }

                int SupplyChance = ((Long) BossUniques.get("Supply Chance")).intValue();
                int supplyCheck = rand.nextInt(SupplyChance + 1);
                if (supplyCheck == SupplyChance) {
                    return "Supply_roll";
                }
                return "Normal_loot";
            }

            // Check if the Unique is a Virtus Piece
            if (name.equals("Virtus_piece")) {
                // Roll the Virtus Table
                JSONObject VirtusPiece = WeightFunctions.rollItem(VirtusTable);
                String virtusName = (String) VirtusPiece.get("name");
                return virtusName;
            }

            // Check if the Unique was the Axe Piece
            if (name.equals("Axe_piece")) {
                String AxePieceName = (String) BossUniques.get("Axe Piece Name");
                return AxePieceName;
            }

            // Return Chromium Ingot
            return name;
        }

        // Next, roll for the Orb
        int OrbChance = ((Long) BossUniques.get("Orb Chance")).intValue();
        String orb = "Awakener's_orb";

        roll = rand.nextInt(OrbChance + 1);
        if (roll == OrbChance) {
            return orb;
        }

        // Next, roll for the Tablet
        if (!HAS_TABLET) {
            int TabletChance = ((Long) BossUniques.get("Tablet Chance")).intValue();
            String tabletName = (String) BossUniques.get("Tablet Name");

            roll = rand.nextInt(TabletChance + 1);
            if (roll == TabletChance) {
                HAS_TABLET = true;
                return tabletName;
            }
        }

        // Next, roll for the Quartz
        int QuartzChance = ((Long) BossUniques.get("Quartz Chance")).intValue();
        String quartzName = (String) BossUniques.get("Quartz Name");

        roll = rand.nextInt(QuartzChance + 1);
        if (roll == QuartzChance) {
            return quartzName;
        }

        // Next, roll for Supply Drop.
        int SupplyChance = ((Long) BossUniques.get("Supply Chance")).intValue();

        roll = rand.nextInt(SupplyChance + 1);
        if (roll == SupplyChance) {
            return "Supply_roll";
        }

        // Finally, return Normal Loot.
        return "Normal_loot";
    }
}
