package weighttables;
import java.util.HashMap;
import java.util.Random;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CoX {

    public static int lootRolls = 2;
    public static int cappedPoints = 570000;
    public static int purpleLimit = 6;
    public static int accuracy = 100; // More 0s, more accurate purple chance. Default 100.
    public static double purpleRate = 8676.0;

    public static int LOOT_IMAGE_WIDTH;
    public static int LOOT_IMAGE_HEIGHT;

    public static String COX_UNIQUES_PATH = "WeightTables\\CoxUniques.json";
    public static String COX_GENERICS_PATH = "WeightTables\\CoxGenerics.json";
    public static String COX_LOOT_IMAGE_PATH = "WeightTables\\CoxLoot.png";
    public static String IMAGE_FILE_FORMAT = "png";

    // This function simulates completions of the Chambers of Xeric raid.
    // Requires 3 Integer inputs:
    //      numRaids: The number of raid completions to simulate
    //      raidPoints: The number of points obtained during the raid
    //      partySize: The number of people in the raiding party
    // The rewards of the raid are stored and returned in a HashMap
    //      The HashMap is made of String, Integer Key/Value pairs:
    //      String: Name of the Loot rewarded.
    //      Integer: The quantity of the Loot rewarded.
    public static HashMap<String, Integer> runCoX(int numRaids, int raidPoints, int partySize)
    {
        // Variable declarations
        HashMap<String, Integer> output = new HashMap<String, Integer>();
        // JSON I/O
        JSONParser parser = new JSONParser();
        JSONArray CoxUniques = null;
        JSONArray CoxGenerics = null;
        JSONObject Purple = null;
        JSONObject NormalLoot = null;
        // Random Number Generator
        Random rand = new Random();
        // Helper variables
        int checkPurple;
        int purpleWeight;
        int totalPWeight = accuracy * accuracy;
        int cappedRolls;
        int remainingPoints;
        int totalLoots;
        int totalPurples;

        // Populate the possible rewards offered.
        // Two types of loot: Uniques, and Generics.
        // The player(s) are guranteed either a Unique loot, or a Generic loot.
        try
        {
            // CoxUniques is the array of possible Unique loots offered by the raid.
            // CoxGenerics is the array of possible Generic loots offered by the raid.
            CoxUniques = (JSONArray) parser.parse(new FileReader(COX_UNIQUES_PATH));
            CoxGenerics = (JSONArray) parser.parse(new FileReader(COX_GENERICS_PATH));
        }
        catch (IOException | ParseException e)
        {
            System.out.println("Invalid File Setup");
        }
        
        // This loop iterates between 1 and the numRaids.
        // Every iteration simulates one completion of the raid
        // 
        for (int i = 1; i <= numRaids; i++)
        {
            totalLoots = 0;
            totalPurples = 0;
            cappedRolls = raidPoints / cappedPoints;
            remainingPoints = raidPoints % cappedPoints;

            // No more purples than the purple limit may be rolled.
            if(cappedRolls > purpleLimit){cappedRolls = purpleLimit;}
            
            while(cappedRolls >= 1 && totalLoots < partySize && totalPurples < purpleLimit)
            {
                // Roll purple using capped points.
                purpleWeight = (int)Math.ceil(accuracy * (cappedPoints / purpleRate));
                checkPurple = rand.nextInt(totalPWeight + 1);
                if (checkPurple <= purpleWeight)
                {
                    // Award purple
                    Purple = WeightFunctions.rollItem(CoxUniques);
                    String name = (String) Purple.get("name");
                    if (output.containsKey(name))
                    {
                        output.put(name, (output.get(name)+1));
                    }
                    else
                    {
                        output.put(name, 1);
                    }
                    
                    totalLoots++;
                    totalPurples++;
                }
                cappedRolls -= 1;
            }

            if(totalLoots < partySize)
            {
                // Roll purple using the remaining points.
                while(partySize - totalLoots > 0)
                {
                    if(totalPurples <= 0 && totalPurples < purpleLimit)
                    {
                        purpleWeight = (int)Math.ceil(accuracy * ((remainingPoints/partySize) / purpleRate));
                        checkPurple = rand.nextInt(totalPWeight + 1);
                        if (checkPurple <= purpleWeight)
                        {
                            Purple = WeightFunctions.rollItem(CoxUniques);
                            String name = (String) Purple.get("name");
                            if (output.containsKey(name))
                            {
                                output.put(name, (output.get(name)+1));
                            }
                            else
                            {
                                output.put(name, 1);
                            }

                            totalLoots++;
                            totalPurples++;
                        }
                        else
                        {
                            // Roll normal loot;
                            for(int j = 0; j < lootRolls; j++)
                            {
                                NormalLoot = WeightFunctions.rollItem(CoxGenerics);
                                String name = (String) NormalLoot.get("name");
                                int divisor = ((Long) NormalLoot.get("divisor")).intValue();
                                int quantity;

                                if (divisor == 0) 
                                {quantity = 1;}
                                else
                                {quantity = (raidPoints / divisor);}

                                if (output.containsKey(name))
                                {
                                    output.put(name, (output.get(name)+quantity));
                                }
                                else
                                {
                                    output.put(name, quantity);
                                }
                            }
                            
                            totalLoots++;
                        }
                    }
                    else
                    {
                        // Roll normal loot;
                        for(int j = 0; j < lootRolls; j++)
                        {
                            NormalLoot = WeightFunctions.rollItem(CoxGenerics);
                            String name = (String) NormalLoot.get("name");
                            int divisor = ((Long) NormalLoot.get("divisor")).intValue();
                            int quantity;

                            if (divisor == 0) 
                            {quantity = 1;}
                            else
                            {quantity = (raidPoints / divisor);}

                            if (output.containsKey(name))
                            {
                                output.put(name, (output.get(name)+quantity));
                            }
                            else
                            {
                                output.put(name, quantity);
                            }
                        }
                        totalLoots++;
                    }
                }
            }
        }
        return output;
    }

    public static void getTbowChance(int raidPoints)
    {
        JSONParser parser = new JSONParser();
        JSONArray CoxUniques = null;

        try
        {
            CoxUniques = (JSONArray) parser.parse(new FileReader(COX_UNIQUES_PATH));
        }
        catch (IOException | ParseException e)
        {
            System.out.println("Invalid File Setup");
        }

        String msg1 = "With ";
        String msg2 = " Points, tbow chance is 1/";

        int totalPWeight = WeightFunctions.getTotalWeight(CoxUniques);

        // 1 out of purple weight
        double purpleWeight = (double)accuracy * (((double)raidPoints) / purpleRate); // large int
        double purpleRate = ((double)accuracy * 100.0)/purpleWeight; //  10k / large int
        double tbowRate = totalPWeight/2.0;
        int tbowChance = (int)Math.round(tbowRate * purpleRate);

        System.out.println(msg1 + raidPoints + msg2 + tbowChance);
    }
}
