import java.io.*;
import java.util.ArrayList;


public class NewSudokuEncoder {

    private int n;
    private int max_val;
    private int[][] map;
    ArrayList<SudokuHint> hints;

    public NewSudokuEncoder(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            hints = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.charAt(0) == 'p') {
                    this.n = Integer.parseInt(line.split(" ")[2]);
                }
                if (line.charAt(0) == 'v') {
                    hints.add(new SudokuHint(line));
                }
            }
            this.max_val = (int) Math.pow(this.n, 2);
            map = new int[max_val][max_val];
            for (int i = 0; i < max_val; i++) {
                for (int j = 0; j < max_val; j++) {
                    map[i][j] = -1;
                }
            }
            for (SudokuHint hint : this.hints) {
                map[hint.getI() - 1][hint.getJ() - 1] = hint.getK() - 1;
            }
        } catch (FileNotFoundException e) {
            System.err.println("Oops! Please check for the presence of file in the path specified.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Oops! Unable to read the file.");
            e.printStackTrace();
        }
    }


    public int getMax_val() {
        return max_val;
    }


    public int[][] getMap() {
        return map;
    }

    public int getN() {
        return n;
    }
}
