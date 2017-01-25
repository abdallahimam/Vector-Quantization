/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vectorquantization;

import java.util.ArrayList;

public class Node {

    public ArrayList<Integer> averages;
    public ArrayList<Block> blocks;
    public Node left;
    public Node right;

    public Node() {
        left = right = null;
        averages = new ArrayList<>();
        blocks = new ArrayList<>();
    }

    public void setAverages(ArrayList<Integer> ave, int step) {
        for (int i = 0; i < ave.size(); i++) {
            averages.add(ave.get(i) + step);
        }
    }

    public void calcAverage() {
        int summation = 0;
        ArrayList<Integer> tempAverage = new ArrayList<>();
        if (!blocks.isEmpty()) {
            Block temp = blocks.get(0);
            for (int i = 0; i < temp.values.size(); i++) {
                for (Block i_b : blocks) {
                    summation += i_b.values.get(i);
                }
                tempAverage.add((int) summation / blocks.size());
                summation = 0;
            }
        }
        averages = tempAverage;
    }

    public void splitte() {

        left = new Node();
        left.setAverages(averages, -1);
        right = new Node();
        right.setAverages(averages, 1);
    }

    public void setBlocks(ArrayList<Block> blocks2) {
        for (Block temp : blocks2) {
            blocks.add(temp);
        }

    }
}
