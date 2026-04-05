package weighttables;

import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WeightFunctions {

    public static int getTotalWeight(JSONArray UniquesList) {
        int totalWeight = 0;

        for (Object entry : UniquesList) {
            JSONObject item = (JSONObject) entry;
            if (item.containsKey("weight")) {
                int weight = ((Long) item.get("weight")).intValue();
                totalWeight += weight;
            }
        }

        return totalWeight;
    }

    public static JSONObject rollItem(JSONArray ItemList) {
        int totalWeight = getTotalWeight(ItemList);

        if (totalWeight <= 0) {
            return null;
        }

        Random r = new Random();
        int roll = r.nextInt(totalWeight + 1);

        JSONObject output = null;

        int weight = -1;

        for (Object entry : ItemList) {
            JSONObject item = (JSONObject) entry;

            if (item.containsKey("weight")) {
                weight = ((Long) item.get("weight")).intValue();
                if (roll <= weight) {
                    return output = item;
                } else {
                    roll -= weight;
                }
            }

            // If the item does not have a weight, skip over it
        }

        if (weight <= -1) {
            return null;
        }

        return output;
    }
}
