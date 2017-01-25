package vectorquantization;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

public class Decompress {

    public Decompress() {
        ArrayList<ArrayList<Integer>> codeBook = null;
        ArrayList<Integer> values = null;
        int h = 0;
        int w = 0;
        String codeBookFile = "codebook.txt";
        String imagePath = "saving.txt";
        try {
            codeBook = new ArrayList<>();
            FileInputStream FIS = new FileInputStream(new File(codeBookFile));
            System.setIn(FIS);
            Scanner sc = new Scanner(System.in);
            String line = null;
            ArrayList<Integer> averages = null;
            ArrayList<Integer> codebookInsex = new ArrayList<>();
            while ((line = sc.nextLine()) != null) {
                StringTokenizer b = new StringTokenizer(line, " ");
                codebookInsex.add(Integer.parseInt(b.nextToken()));
                averages = new ArrayList<>();
                while (b.hasMoreTokens()) {
                    averages.add(Integer.parseInt(b.nextToken()));
                }
                codeBook.add(averages);
            }
            FIS.close();
            sc.close();
        } catch (NumberFormatException | NoSuchElementException | IOException e) {
            System.out.println("the first");
        }
        try {
            BufferedReader bw = null;
            bw = new BufferedReader(new FileReader("saving.txt"));
            String line = null;
            values = new ArrayList<>();
            try {
                while ((line = bw.readLine()) != null) {
                    w = 0;
                    h++;
                    StringTokenizer val = new StringTokenizer(line, " ");
                    while (val.hasMoreTokens()) {
                        w++;
                        values.add(Integer.parseInt(val.nextToken()));
                    }
                }
            } catch (IOException | NoSuchElementException ex) {

            }
        } catch (NumberFormatException e) {
            System.out.println("the second");
        } catch (FileNotFoundException ex) {

        }
        int[][] pixels = new int[h][w];
        int count = 0;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                pixels[i][j] = (int) values.get(count++);
            }
        }
        int blockWidth = (int) Math.sqrt(codeBook.get(0).size());
        int height = pixels.length * blockWidth;
        int width = pixels[0].length * blockWidth;
        System.out.println(width + " " + height);
        int[][] newPixels = new int[height][width];
        int row = 0;
        int col = 0;
        for (int i = 0; i < height;) {
            col = 0;
            for (int j = 0; j < width;) {
                int codeBookIndex = pixels[row][col];
                int d = 0;
                for (int x = i; x < i + blockWidth; x++) {
                    for (int y = j; y < j + blockWidth; y++) {
                        if (!codeBook.get(codeBookIndex).isEmpty()) {
                            newPixels[x][y] = codeBook.get(codeBookIndex).get(d++);
                        } else {
                            newPixels[x][y] = 0;
                        }
                    }
                }
                col++;
                j += blockWidth;
            }
            row++;
            i += blockWidth;
        }
        System.out.println(width + " " + height);
        writeImage(newPixels, "decompress.jpg", width, height);

    }

    private int[][] readImage(String filePath) {
        int width = 0;
        int height = 0;
        File file = new File(filePath);
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        width = image.getWidth();
        height = image.getHeight();
        int[][] pixels = new int[height][width];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb >> 0) & 0xff;

                pixels[y][x] = r;
            }
        }

        return pixels;
    }

    private void writeImage(int[][] pixels, String outputFilePath, int width, int height) {
        File fileout = new File(outputFilePath);
        BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image2.setRGB(x, y, (pixels[y][x] << 16) | (pixels[y][x] << 8) | (pixels[y][x]));
            }
        }
        try {
            ImageIO.write(image2, "jpg", fileout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
