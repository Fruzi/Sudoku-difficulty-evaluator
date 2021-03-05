import javafx.util.Pair;
import org.sat4j.specs.ContradictionException;

import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class NewSudokuSolver {

    private int[][] sudoku;
    private int size;
    private int box_size;
    private ArrayList[][] possebility_map;
    private int[][][] map;
    private Types ArrayUtils;

    public NewSudokuSolver(int[][] sudoku) {
        this.sudoku = sudoku;
        this.size = sudoku[0].length;
        this.box_size = (int) Math.sqrt(this.size);
    }

    public int[][] solve() {
        map = create_map();
        int[][] solution = new int[size][size];
        ArrayList<int[]> cons = new ArrayList<>();
        cons.addAll(get_hint_cons());
        cons.addAll(get_row_cons());
        cons.addAll(get_col_cons());
        cons.addAll(get_box_cons());
        cons.addAll(get_exactly_one_cons());
        SatSolver solver = new SatSolver();
        SatSolver.SolverStatus status = solver.solve(cons, (int) Math.pow(size, 3));
        if (status == SatSolver.SolverStatus.SAT) {
            // decode solution
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    solution[i][j] = -1;
                    for (int k = 0; k < size; k++) {
                        if (solver.valueOf(map[i][j][k])) {
                            if (solution[i][j] > -1) {
                                System.out.println("ERROR - the same cell has two different value assignments");
                                return null;
                            }
                            solution[i][j] = k + 1;
                            break;
                        }
                    }
                }
            }
        }
        return solution;
    }

    public boolean is_legal_sudoku(int[][] puzzle) {
        int size = puzzle[0].length;
        int[][] solution = new int[size][size];
        int[][][] this_map = create_map(puzzle);
        ArrayList<int[]> cons = new ArrayList<>();
        ArrayList<int[]> hint_cons = get_hint_cons(puzzle, size, this_map);
        if (hint_cons.size() > 0) {
            cons.addAll(get_hint_cons(puzzle, size, this_map));
        }
        cons.addAll(get_row_cons(size, this_map));
        cons.addAll(get_col_cons(size, this_map));
        cons.addAll(get_box_cons(size, this_map));
        cons.addAll(get_exactly_one_cons(size, this_map));
        SatSolver solver = new SatSolver();
        SatSolver.SolverStatus status = solver.solve(cons, (int) Math.pow(size, 3));
        if (status == SatSolver.SolverStatus.SAT) {
            // decode solution
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    solution[i][j] = -1;
                    for (int k = 0; k < size; k++) {
                        if (solver.valueOf(this_map[i][j][k])) {
                            if (solution[i][j] > -1) {
                                System.out.println("ERROR - the same cell has two different value assignments");
                                return false;
                            }
                            solution[i][j] = k + 1;
                            break;
                        }
                    }
                }
            }
            cons.add(require_new(solution, this_map));
            status = solver.solve(cons, (int) Math.pow(size, 3));
            return status == SatSolver.SolverStatus.UNSAT;
        }
        return false;
    }

    public int[][] create_new(int[][] sudoku){
        int size = sudoku[0].length;
        int[][][] this_map = create_map(sudoku);
        int[][] solution = new int[size][size];
        ArrayList<int[]> cons = new ArrayList<>();
        cons.addAll(get_hint_cons());
        cons.addAll(get_row_cons());
        cons.addAll(get_col_cons());
        cons.addAll(get_box_cons());
        cons.addAll(get_exactly_one_cons());
        cons.add(require_new(sudoku, this_map));
//        cons.addAll(create_some_new(size));
        SatSolver solver = new SatSolver();
        SatSolver.SolverStatus status = solver.solve(cons, (int) Math.pow(size, 3));
        if (status == SatSolver.SolverStatus.SAT) {
            // decode solution
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    solution[i][j] = -1;
                    for (int k = 0; k < size; k++) {
                        if (solver.valueOf(this_map[i][j][k])) {
                            if (solution[i][j] > -1) {
                                System.out.println("ERROR - the same cell has two different value assignments");
                                return null;
                            }
                            solution[i][j] = k + 1;
                            break;
                        }
                    }
                }
            }
        }
        return solution;
    }

    private int[] require_new(int[][] solution, int[][][] this_map) {
        int size = solution[0].length;
        int num_cons = (int)Math.pow(size, 2);
        int[] ans = new int[num_cons];
        int cur=0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                ans[cur] = -(this_map[i][j][solution[i][j]-1]);
                cur++;
            }
        }
        return ans;
    }

    private ArrayList<int[]> create_some_new(int size){
        int max_val = (int) Math.pow(size,3);
        ArrayList<int[]> chosen = new ArrayList<>();
        int cur;
        Random r = new Random();
        for (int i=0;i<5;i++){
            cur = r.nextInt(max_val);
            int[] arr = {cur};
            if (chosen.contains(cur)){
                i--;
            }
            else{
                chosen.add(arr);
            }
        }
        return chosen;
    }

    private int[][][] create_map() {
        return create_map(null);
    }

    private int[][][] create_map(int[][] puzzle) {
        int size;
        if (puzzle != null) {
            size = puzzle[0].length;
        } else {
            size = this.size;
        }
        int[][][] map = new int[size][size][size];
        int var = 1;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    map[i][j][k] = var;
                    var++;
                }
            }
        }
        return map;
    }

    private ArrayList<int[]> get_hint_cons() {
        return get_hint_cons(null, -1, null);
    }

    private ArrayList<int[]> get_hint_cons(int[][] puzzle, int dim, int[][][] puzzle_map) {
        int[][] working_puzzle;
        int size;
        int[][][] map;
        if (puzzle != null) {
            working_puzzle = puzzle;
            size = dim;
            map = puzzle_map;
        } else {
            working_puzzle = this.sudoku;
            size = this.size;
            map = this.map;
        }
        ArrayList<int[]> ans = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (working_puzzle[i][j] != -1) {
                    int[] arr = {map[i][j][working_puzzle[i][j]]};
                    ans.add(arr);
                }
            }
        }
        return ans;
    }

    private ArrayList<int[]> get_row_cons() {
        return get_row_cons(-1, null);
    }

    private ArrayList<int[]> get_row_cons(int dim, int[][][] puzzle_map) {
        int size;
        int[][][] map;
        if (puzzle_map != null) {
            size = dim;
            map = puzzle_map;
        } else {
            size = this.size;
            map = this.map;
        }
        ArrayList<int[]> ans = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    for (int l = j + 1; l < size; l++) {
                        int[] arr = {-map[i][j][k], -map[i][l][k]};
                        ans.add(arr);
                    }
                }
            }
        }
        return ans;
    }

    private ArrayList<int[]> get_col_cons() {
        return get_col_cons(-1, null);
    }

    private ArrayList<int[]> get_col_cons(int dim, int[][][] puzzle_map) {
        int size;
        int[][][] map;
        if (puzzle_map != null) {
            size = dim;
            map = puzzle_map;
        } else {
            size = this.size;
            map = this.map;
        }
        ArrayList<int[]> ans = new ArrayList<>();
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                for (int k = 0; k < size; k++) {
                    for (int l = i + 1; l < size; l++) {
                        int[] arr = {-map[i][j][k], -map[l][j][k]};
                        ans.add(arr);
                    }
                }
            }
        }
        return ans;
    }

    private ArrayList<int[]> get_box_cons() {
        return get_box_cons(-1, null);
    }

    private ArrayList<int[]> get_box_cons(int dim, int[][][] puzzle_map) {
        int size;
        int[][][] map;
        int box_size;
        if (puzzle_map != null) {
            size = dim;
            map = puzzle_map;
            box_size = (int) Math.sqrt(size);
        } else {
            size = this.size;
            map = this.map;
            box_size = this.box_size;
        }
        ArrayList<int[]> ans = new ArrayList<>();
        for (int b = 0; b < size; b++) {
            int s_row = (b / box_size) * box_size;
            int s_col = (b % box_size) * box_size;
            for (int i = s_row; i < s_row + box_size; i++) {
                for (int j = s_col; j < s_col + box_size; j++) {
                    for (int k = 0; k < size; k++) {
                        for (int i_2 = i; i_2 < s_row + box_size; i_2++) {
                            for (int j_2 = s_col; j_2 < s_col + box_size; j_2++) {
                                if (i_2 > i || j_2 > j) {
                                    int[] arr = {-map[i][j][k], -map[i_2][j_2][k]};
                                    ans.add(arr);
                                }
                            }
                        }
                    }
                }
            }
        }
        return ans;
    }

    private ArrayList<int[]> get_exactly_one_cons() {
        return get_exactly_one_cons(-1, null);
    }

    private ArrayList<int[]> get_exactly_one_cons(int dim, int[][][] puzzle_map) {
        int size;
        int[][][] map;
        if (puzzle_map != null) {
            size = dim;
            map = puzzle_map;
        } else {
            size = this.size;
            map = this.map;
        }
        ArrayList<int[]> ans = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int p = 0; p < size; p++) {
                    for (int q = (p + 1); q < size; q++) {
                        ans.add(new int[]{-(map[i][j][p]), -(map[i][j][q])});
                    }
                }
                int[] arr = new int[size];
                System.arraycopy(map[i][j], 0, arr, 0, size);
                ans.add(arr);
            }
        }
        return ans;
    }

    public boolean single_assignments_exists() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (sudoku[i][j] == -1 && number_assignments(i, j) == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    public void apply_single_assignment() {
        ArrayList<Pair<Integer, Integer>> to_assign = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (sudoku[i][j] == -1 && number_assignments(i, j) == 1) {
                    to_assign.add(new Pair<>(i, j));
                }
            }
        }
        for (Pair<Integer, Integer> integerIntegerPair : to_assign) {
            int value = cell_single_assignment(integerIntegerPair.getKey(), integerIntegerPair.getValue());
            if (value > -1) {
                sudoku[integerIntegerPair.getKey()][integerIntegerPair.getValue()] = value - 1;
            }
        }
    }

    private int cell_single_assignment(int i, int j) {
        ArrayList<int[]> prob = only_one();
        ArrayList<int[]> row = row_constraint(i, j);
        if (row.size() > 0) {
            prob.addAll(row);
        }
        ArrayList<int[]> col = col_constraint(i, j);
        if (col.size() > 0) {
            prob.addAll(col);
        }
        ArrayList<int[]> box = box_constraint(i, j);
        if (box.size() > 0) {
            prob.addAll(box);
        }
        SatSolver solver = new SatSolver();
        try {
            return (int) solver.get_cell_single_solution(prob, size);
        } catch (ContradictionException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean found_contradiction() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (sudoku[i][j] == -1 && number_assignments(i, j) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public Pair<Integer, Integer> choose_minimal_multi_assign() {
        long min = size;
        int k = 0;
        int q = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (sudoku[i][j] == -1) {
                    long num = number_assignments(i, j);
                    if (num > 1 && num <= min) {
                        min = num;
                        k = i;
                        q = j;
                    }
                }
            }
        }
        return new Pair<>(k, q);
    }

    public ArrayList<Integer> get_possible_values(Pair<Integer, Integer> cell) {
        int i = cell.getKey();
        int j = cell.getValue();
        ArrayList<int[]> prob = only_one();
        ArrayList<int[]> row = row_constraint(i, j);
        if (row.size() > 0) {
            prob.addAll(row);
        }
        ArrayList<int[]> col = col_constraint(i, j);
        if (col.size() > 0) {
            prob.addAll(col);
        }
        ArrayList<int[]> box = box_constraint(i, j);
        if (box.size() > 0) {
            prob.addAll(box);
        }
        SatSolver solver = new SatSolver();
        try {
            return solver.get_cell_solutions(prob, size);
        } catch (ContradictionException e) {
            e.printStackTrace();
            return null;
        }

    }

    private long number_assignments(int i, int j) {
        ArrayList<int[]> prob = only_one();
        ArrayList<int[]> row = row_constraint(i, j);
        if (row.size() > 0) {
            prob.addAll(row);
        }
        ArrayList<int[]> col = col_constraint(i, j);
        if (col.size() > 0) {
            prob.addAll(col);
        }
        ArrayList<int[]> box = box_constraint(i, j);
        if (box.size() > 0) {
            prob.addAll(box);
        }
        SatSolver solver = new SatSolver();
        try {
            return solver.count_solutions(prob, size);
        } catch (ContradictionException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private ArrayList<int[]> only_one() {
        ArrayList<int[]> ans = new ArrayList<>();
        int[] at_least_one = new int[size];
        for (int i = 0; i < size; i++) {
            at_least_one[i] = i + 1;
        }
        ans.add(at_least_one);
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                int[] temp = new int[2];
                temp[0] = -(i + 1);
                temp[1] = -(j + 1);
                ans.add(temp);
            }
        }
        return ans;
    }

    private ArrayList<int[]> row_constraint(int i, int j) {
        ArrayList<int[]> ans = new ArrayList<>();
        for (int k = 0; k < size; k++) {
            if (sudoku[i][k] > -1) {
                int[] arr = {-(sudoku[i][k] + 1)};
                ans.add(arr);
            }
        }
        return ans;
    }

    private ArrayList<int[]> col_constraint(int i, int j) {
        ArrayList<int[]> ans = new ArrayList<>();
        for (int k = 0; k < size; k++) {
            if (sudoku[k][j] > -1) {
                int[] arr = {-(sudoku[k][j] + 1)};
                ans.add(arr);
            }
        }
        return ans;
    }

    private ArrayList<int[]> box_constraint(int i, int j) {
        ArrayList<int[]> ans = new ArrayList<>();
        int box_start_row = (i / box_size) * box_size;
        int box_start_col = j / box_size * box_size;
        for (int k = box_start_row; k < box_start_row + box_size; k++) {
            for (int q = box_start_col; q < box_start_col + box_size; q++) {
                int value = sudoku[k][q];
                if (value > -1) {
                    int[] arr = {-(value + 1)};
                    ans.add(arr);
                }
            }
        }
        return ans;
    }

    public int deduce_cell() {
        int num_changes = 0;
        possebility_map = new ArrayList[sudoku[0].length][sudoku[0].length];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (sudoku[i][j] == -1) {
                    possebility_map[i][j] = get_possible_values(new Pair<>(i, j));
                } else {
                    possebility_map[i][j] = new ArrayList();
                }
            }
        }
        for (int i = 0; i < size; i++) {
            num_changes += apply_row_deduction(i);
            num_changes += apply_col_deduction(i);
            num_changes += apply_box_deduction(i);
        }
        return num_changes;
    }

    private int apply_row_deduction(int i) {
        int num_changes = 0;
        int num_appearances;
        int col = 0;
        for (int k = 0; k < size; k++) {
            num_appearances = 0;
            for (int j = 0; j < size; j++) {
                if (sudoku[i][j] == -1 && possebility_map[i][j].contains(k + 1)) {
//                    System.out.println("cell (" + i +"," + j + ") can be assigned " + k);
                    num_appearances++;
                    col = j;
                }
            }
            if (num_appearances == 1) {
//                System.out.println("row changed cell (" + i + "," + col + ") from " + sudoku[i][col] + " to " + k);
                sudoku[i][col] = k;
                num_changes++;
            }
        }
//        if (num_changes > 0){
//            System.out.println("row " + i + "produced " + num_changes + " deductions");
//        }
        return num_changes;
    }

    private int apply_col_deduction(int j) {
        int num_changes = 0;
        int num_appearances;
        int row = 0;
        for (int k = 0; k < size; k++) {
            num_appearances = 0;
            for (int i = 0; i < size; i++) {
                if (sudoku[i][j] == -1 && possebility_map[i][j].contains(k + 1)) {
                    num_appearances++;
                    row = i;
                }
            }
            if (num_appearances == 1) {
//                System.out.println("col changed cell (" + row + "," + j + ") from " + sudoku[row][j] + " to " + k);
                sudoku[row][j] = k;
                num_changes++;
            }
        }
//        if (num_changes > 0){
//            System.out.println("col " + j + "produced " + num_changes + " deductions");
//        }
        return num_changes;
    }

    private int apply_box_deduction(int q) {
        int num_changes = 0;
        int num_appearances;
        int row = 0;
        int col = 0;
        int box_row_start = (q / box_size) * box_size;
        int box_col_start = q % box_size * box_size;
        for (int k = 0; k < size; k++) {
            num_appearances = 0;
            for (int i = box_row_start; i < box_row_start + box_size; i++) {
                for (int j = box_col_start; j < box_col_start + box_size; j++) {
//                    System.out.println("i: " + i + " j: " + j);
                    if (sudoku[i][j] == -1 && possebility_map[i][j].contains(k + 1)) {
                        num_appearances++;
                        row = i;
                        col = j;
                    }
                }
            }
            if (num_appearances == 1) {
//                System.out.println("box changed cell (" + row + "," + col + ") from " + sudoku[row][col] + " to " + k);
                sudoku[row][col] = k;
                num_changes++;
            }
        }
//        if (num_changes > 0){
//            System.out.println("box " + q + "produced " + num_changes + " deductions");
//        }
        return num_changes;
    }

    public boolean isDone() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (sudoku[i][j] == -1) {
                    return false;
                }
            }
        }
        return true;
    }

    public int[][] getSudoku() {
        return sudoku;
    }


}
