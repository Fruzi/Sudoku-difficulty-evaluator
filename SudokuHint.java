public class SudokuHint {
    private final int i;
    private final int j;
    private final int k;

    public SudokuHint(int i,int j,int k){
        this.i = i;
        this.j = j;
        this.k = k;
    }

    public SudokuHint(String s){
        String[] split = s.split(" ");
        this.i = Integer.parseInt(split[1]);
        this.j = Integer.parseInt(split[2]);
        this.k = Integer.parseInt(split[3]);
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public int getK() {
        return k;
    }

    @Override
    public String toString() {
        return "v " +
                i + " " +
                j + " " +
                k;
    }

}
