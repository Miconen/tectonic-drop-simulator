package weighttables;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageGenerator {

    public static String GenerateLootImage(HashMap<String, Integer> LootMap,
            String filepath, String fileformat, String HeaderText) throws Exception {
        // Dimensions of each item tile
        int TILE_WIDTH = 32;
        int TILE_HEIGHT = 32;

        // Buffers between each item tile
        int BUFFER_WIDTH = 16;
        int BUFFER_HEIGHT = 4;

        // Initial offsets for the item numerical text
        int TEXT_OFFSET_X = 1;
        int TEXT_OFFSET_Y = 9;

        // Buffer space at the top to include the text header
        int HEADER_OFFSET_Y = 20;

        // Initial X and Y
        int X = BUFFER_WIDTH;
        int Y = BUFFER_HEIGHT + HEADER_OFFSET_Y;

        int CANVAS_WIDTH = 400;
        int CANVAS_HEIGHT = (HEADER_OFFSET_Y + ((LootMap.size() / 8) + 1) * (TILE_HEIGHT + BUFFER_HEIGHT));

        BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();

        // Font information
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        Font font = Font.createFont(Font.TRUETYPE_FONT,
                ImageGenerator.class.getResourceAsStream("/RuneScape-Plain-11.ttf"));

        ge.registerFont(font);
        Font osrsFont = new Font(font.getFontName(), Font.PLAIN, 16);
        g2d.setFont(osrsFont);

        for (Map.Entry<String, Integer> item : LootMap.entrySet()) {
            String itemName = item.getKey();
            int num = item.getValue();

            // Debug statement, used to see which specific .png is missing
            // System.out.println(IMAGE_PATH+itemName+"."+fileformat);
            InputStream spriteStream = ImageGenerator.class.getResourceAsStream(
                    "/LootSourceImages/" + itemName + "." + fileformat);
            BufferedImage tempImage = ImageIO.read(spriteStream);

            g2d.drawImage(tempImage, X, Y, null);

            if (num >= 100000 && num < 10000000) {
                num = num / 1000;

                g2d.setColor(Color.BLACK);
                g2d.drawString(Integer.toString(num) + "K", X + TEXT_OFFSET_X + 1, Y + TEXT_OFFSET_Y + 1);

                g2d.setColor(Color.WHITE);
                g2d.drawString(Integer.toString(num) + "K", X + TEXT_OFFSET_X, Y + TEXT_OFFSET_Y);
            } else if (num >= 10000000) {
                num = num / 1000000;

                g2d.setColor(Color.BLACK);
                g2d.drawString(Integer.toString(num) + "M", X + TEXT_OFFSET_X + 1, Y + TEXT_OFFSET_Y + 1);

                g2d.setColor(Color.GREEN);
                g2d.drawString(Integer.toString(num) + "M", X + TEXT_OFFSET_X, Y + TEXT_OFFSET_Y);
            } else {
                g2d.setColor(Color.BLACK);
                g2d.drawString(Integer.toString(num), X + TEXT_OFFSET_X + 1, Y + TEXT_OFFSET_Y + 1);

                g2d.setColor(Color.YELLOW);
                g2d.drawString(Integer.toString(num), X + TEXT_OFFSET_X, Y + TEXT_OFFSET_Y);
            }

            X += TILE_WIDTH;
            X += BUFFER_WIDTH;

            if (X >= 400) {
                X = BUFFER_WIDTH;
                Y += BUFFER_HEIGHT;
                Y += TILE_HEIGHT;
            }
        }

        g2d.setColor(Color.BLACK);
        g2d.drawString(HeaderText, BUFFER_WIDTH + 1, BUFFER_HEIGHT + TEXT_OFFSET_Y + 1);
        g2d.setColor(Color.YELLOW);
        g2d.drawString(HeaderText, BUFFER_WIDTH, BUFFER_HEIGHT + TEXT_OFFSET_Y);

        ImageIO.write(image, fileformat, new File(filepath));

        return filepath;
    }
}
