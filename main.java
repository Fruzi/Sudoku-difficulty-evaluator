import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

class Main {
    private static int number_restarts_per_puzzle = 10;
    private static int num_different_puzzles =1;

    public static void main(String[] args) {
        int dim = 3;
        show_minimal_easy_sudoku(dim);
        show_minimal_hard_sudoku(dim);
    }

    public static void show_minimal_easy_sudoku(int dim){
        int[][] puzzle = find_minimal_easy_sudoku(dim);
        int[][] temp = deepCopy(puzzle);
        int score = scoreSudoku(temp);
        int hints = (int)Math.pow(dim,4) - num_missing(puzzle);
        System.out.println("easiest was puzzle of score " + score + " with " + hints + " hints");
        increase_by_one(puzzle);
        print_puzzle(puzzle);
    }

    private static int[][] find_minimal_easy_sudoku(int dim) {
        int[][] sudoku = create_empty_board(dim * dim);
        NewSudokuSolver solver = new NewSudokuSolver(sudoku);
        sudoku = solver.solve();
        int[][] ans = deepCopy(sudoku);
        for (int num__puzzle=0;num__puzzle<num_different_puzzles;num__puzzle++){
            System.out.println("starting new puzzle");
            int[][] initial_puzzle = deepCopy(sudoku);
            decrease_by_one(sudoku);
            int most_removed = 0;
            for (int restart_attempt = 0; restart_attempt < number_restarts_per_puzzle; restart_attempt++) {
                sudoku = deepCopy(initial_puzzle);
                decrease_by_one(sudoku);
                int number_removal_attemps = (int)Math.pow(dim, 4);
                int removed = 0;
                while (number_removal_attemps > 0) {
                    if(try_to_remove(sudoku, solver, true)){
                        removed++;
                    }
                    number_removal_attemps--;
                }
                if(removed > most_removed){
                    most_removed = removed;
                    ans = deepCopy(sudoku);
                }
            }
            sudoku = solver.create_new(initial_puzzle);
        }
        return ans;
    }

    public static void show_minimal_hard_sudoku(int dim){
        int[][] puzzle = find_minimal_hard_sudoku(dim);
        int[][] temp = deepCopy(puzzle);
        int score = scoreSudoku(temp);
        int hints = (int)Math.pow(dim,4) - num_missing(puzzle);
        System.out.println("hardest was puzzle of score " + score + " with " + hints + " hints");
        increase_by_one(puzzle);
        print_puzzle(puzzle);
    }

    private static int[][] find_minimal_hard_sudoku(int dim) {
        int[][] sudoku = create_empty_board(dim * dim);
        NewSudokuSolver solver = new NewSudokuSolver(sudoku);
        sudoku = solver.solve();
        int[][] ans = deepCopy(sudoku);
        for (int num__puzzle=0;num__puzzle<num_different_puzzles;num__puzzle++){
            System.out.println("starting new puzzle");
            int[][] initial_puzzle = deepCopy(sudoku);
            decrease_by_one(sudoku);
            int score;
            int maximal = 0;
            int most_removed = 0;
            for (int restart_attempt = 0; restart_attempt < number_restarts_per_puzzle; restart_attempt++) {
                sudoku = deepCopy(initial_puzzle);
                decrease_by_one(sudoku);
                int number_removal_attemps = (int)Math.pow(dim, 4);
                int removed = 0;
                while (number_removal_attemps > 0) {
                    if (try_to_remove(sudoku, solver, false)){
                        removed++;
                    }
                    number_removal_attemps--;
                }
                int[][] temp = deepCopy(sudoku);
                score = scoreSudoku(temp);
                if (score > maximal || (score==maximal && removed > most_removed)) {
                    maximal = score;
                    most_removed = removed;
                    ans = deepCopy(sudoku);
                }
            }
            sudoku = solver.create_new(initial_puzzle);
        }
        return ans;
    }

    public static int scoreSudoku(String filename){
        NewSudokuEncoder enc = new NewSudokuEncoder(filename);
        int[][] sudoku = enc.getMap();
        return scoreSudoku(sudoku, 1);
    }

    public static int scoreSudoku(int[][] sudoku) {
        return scoreSudoku(sudoku, 1);
    }

    private static int scoreSudoku(int[][] sudoku, int depth) {
        int score = 0;
        NewSudokuSolver solver = new NewSudokuSolver(sudoku);
        if (!solver.found_contradiction()) {
            boolean loop = true;
            while (loop) {
                while (solver.single_assignments_exists()) {
                    solver.apply_single_assignment();
                }
                int val = solver.deduce_cell();
                if (val == 0) {
                    loop = false;
                }
                if (solver.isDone()) {
//                    System.out.println("solution");
//                    print_puzzle(sudoku);
                    return score;
                }
            }
            Pair<Integer, Integer> cell = solver.choose_minimal_multi_assign();
            ArrayList<Integer> possible_values = solver.get_possible_values(cell);
            score += possible_values.size() * depth;
            for (Integer value : possible_values) {
                int[][] new_puzzle = deepCopy(solver.getSudoku());
                new_puzzle[cell.getKey()][cell.getValue()] = value;
                score += scoreSudoku(new_puzzle, depth + 1);
            }
            return score;

        }
        return score;
    }

    private static int[][] deepCopy(int[][] original) {
        if (original == null) {
            return null;
        }

        final int[][] result = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return result;
    }

    private static void print_puzzle(int[][] sudoku) {
        for (int i = 0; i < sudoku[0].length; i++) {
            for (int j = 0; j < sudoku[0].length; j++) {
                System.out.print(sudoku[i][j] + " ");
            }
            System.out.println("");
        }
    }

    private static int[][] create_empty_board(int dim) {
        int[][] ans = new int[dim][dim];
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                ans[i][j] = -1;
            }
        }
        return ans;
    }

    private static boolean try_to_remove(int[][] puzzle, NewSudokuSolver solver, boolean keep_easy) {
        Random r = new Random();
        int dim = puzzle[0].length;
        int missing = num_missing(puzzle);
        int attempts = (int) (Math.pow(dim, 2) - missing) / 2;
        while (attempts > 0) {
            int i = r.nextInt(dim);
            int j = r.nextInt(dim);
            if (puzzle[i][j] != -1) {
                attempts--;
                int temp = puzzle[i][j];
                puzzle[i][j] = -1;
                if (solver.is_legal_sudoku(puzzle)) {
                    if(!keep_easy){
                        return true;
                    }
                    else{
                        int[][] sudoku_copy = deepCopy(puzzle);
                        if (scoreSudoku(sudoku_copy)==0){
                            return true;
                        }
                    }
                } else {
                    puzzle[i][j] = temp;
                }
            }
        }
        return false;
    }

    private static int num_missing(int[][] puzzle) {
        int missing = 0;
        int dim = puzzle[0].length;
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (puzzle[i][j] == -1) {
                    missing++;
                }
            }
        }
        return missing;
    }

    private static void decrease_by_one(int[][] sudoku) {
        int dim = sudoku[0].length;
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (sudoku[i][j] != -1) {
                    sudoku[i][j]--;
                }
            }
        }
    }

    private static void increase_by_one(int[][] sudoku){
        int dim = sudoku[0].length;
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (sudoku[i][j] != -1) {
                    sudoku[i][j]++;
                }
            }
        }
    }
}
