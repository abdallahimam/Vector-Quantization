/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vectorquantization;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Compress {

    public Compress(String filePath, int blockSize, int codebookSize) {
        int[][] image = readImage(filePath);
        int iHeight = image[0].length;
        int iWidth = image.length;
        ArrayList<Block> blocks = new ArrayList<>();
        Block b = null;
        int blockWidth = blockSize;
        int rBlocks = iHeight / blockSize , cBlocks = iWidth / blockSize;
        if(iHeight % blockSize != 0) {
            rBlocks++;
        }
        if(iWidth % blockSize != 0) {
            cBlocks++;
        }
        int height = blockSize * rBlocks;
        int width = blockSize * cBlocks;
        int [][] pixels = new int[height][width];
        for (int i = 0; i < iHeight;i++) {
            System.arraycopy(image[i], 0, pixels[i], 0, iWidth);
        }
        for (int i = 0; i < height;) {
            for (int j = 0; j < width;) {
                b = new Block();
                for (int x = i; x < i + blockWidth; x++) {
                    for (int y = j; y < j + blockWidth; y++) {
                        b.values.add(pixels[x][y]);
                    }
                }
                blocks.add(b);
                j += blockWidth;
            }
            i += blockWidth;
        }
        Node root = new Node();
        root.setBlocks(blocks);
        root.calcAverage();
        root.splitte();
        root.left.setAverages(root.averages, -1);
        root.right.setAverages(root.averages, 1);
        ArrayList<Node> lastLevel = new ArrayList<>();
        getLastLevel(root, lastLevel);
        associate(root.blocks, lastLevel);
        while (lastLevel.size() < codebookSize) {
            for (int i = 0; i < lastLevel.size(); i++) {
                lastLevel.get(i).calcAverage();
                lastLevel.get(i).splitte();
            }
            lastLevel.clear();
            lastLevel = new ArrayList<>();
            getLastLevel(root, lastLevel);
            associate(blocks, lastLevel);
        }
        ArrayList<Node> old = new ArrayList<>(lastLevel);
        while (true) {
            associate(blocks, lastLevel);
            for (int i = 0; i < lastLevel.size(); i++) {
                lastLevel.get(i).calcAverage();
            }
            if (isChanging(old, lastLevel)) {
                old.clear();
                old = new ArrayList<>(lastLevel);
            } else {
                break;
            }
        }
        int[] codeBook = new int[codebookSize];
        for (int i = 0; i < codebookSize; i++) {
            codeBook[i] = i;
        }
        int size = height / blockSize;
        int hSize = height / blockSize;
        int wSize = width / blockSize;
        int[][] compress = new int[hSize][wSize];
        for (int i = 0; i < blocks.size(); i++) {
            int least = 10000;
            int pos = 0;
            for (int j = 0; j < lastLevel.size(); j++) {
                int value = findLeast(blocks.get(i).values, lastLevel.get(j).averages);
                if (value < least) {
                    least = value;
                    pos = j;
                }
            }
            compress[i / hSize][i % wSize] = codeBook[pos];
        }
        saveCompressToFile(compress);
        writeImage(compress, "lenacompress.jpg", wSize, hSize);
        saveCodebookToFile(lastLevel);

    }

    private void saveCompressToFile(int[][] compress) {
        try {
            PrintStream PS = new PrintStream(new FileOutputStream("saving.txt"));
            System.setOut(PS);
            for (int i = 0; i < compress.length; i++) {
                for (int j = 0; j < compress[i].length; j++) {
                    System.out.print(compress[i][j] + " ");
                }
                System.out.println();
            }
            PS.close();
        } catch (Exception e) {

        }

    }

    private void saveCodebookToFile(ArrayList<Node> codebook) {
        try {
            PrintStream PS = new PrintStream(new FileOutputStream("codebook.txt"));
            System.setOut(PS);
            int q = 0;
            for (Node n : codebook) {
                System.out.print(q++ + " ");
                for (int i = 0; i < n.averages.size(); i++) {
                    System.out.print(n.averages.get(i) + " ");
                }
                System.out.println();
            }
            PS.close();
        } catch (Exception e) {
            System.out.println("There is an exception occure!.");
        }

    }

    public static boolean isChanging(ArrayList<Node> list1, ArrayList<Node> list2) {

        return !list1.equals(list2);
    }

    public static void getLastLevel(Node root, ArrayList<Node> parents) {
        parents.add(root);
        while (parents.isEmpty() == false) {
            Node temp = parents.get(0);
            if (temp.left == null) {
                break;
            }
            parents.add(temp.left);
            parents.add(temp.right);
            parents.remove(0);
        }
    }

    public static int[][] readImage(String filePath) {
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
                // int alpha = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                // int g = (rgb >> 8) & 0xff;
                // int b = (rgb >> 0) & 0xff;

                pixels[y][x] = r;
            }
        }

        return pixels;
    }

    public static int findLeast(ArrayList<Integer> f, ArrayList<Integer> s) {
        int least = 0;
        if (!s.isEmpty()) {
            for (int i = 0; i < f.size(); i++) {
                if (s.get(i) != 0) {
                    least += Math.abs(f.get(i) - s.get(i));
                }
            }
        }
        return least;
    }

    public static void associate(ArrayList<Block> blocks, ArrayList<Node> lastLevel) {
        long least = 100000;
        int pos = 0;
        for (int i = 0; i < blocks.size(); i++) {
            Block b = blocks.get(i);
            least = 100000;
            for (int j = 0; j < lastLevel.size(); j++) {

                int lleast = findLeast(b.values, lastLevel.get(j).averages);
                if (lleast <= least) {
                    pos = j;
                    least = lleast;
                }
            }
            lastLevel.get(pos).blocks.add(b);
        }

    }

    public static void writeImage(int[][] pixels, String outputFilePath, int width, int height) {
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
