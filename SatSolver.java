import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SolutionCounter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

//Taken directly from Avi's posted code, no changes were made

public class SatSolver {

    public enum SolverStatus {
        INIT,
        SAT,
        UNSAT,
        TIMEOUT,
        ERROR
    }
    private ISolver solver;
    private IProblem problem;
    private SolverStatus status;
    private Reader reader;
    private boolean[] assignment;

    public SatSolver() {
        solver = SolverFactory.newDefault();
        status = SolverStatus.INIT;
        reader = new DimacsReader(solver);

    }


    private boolean readDimacsFile(String filename) {
        try {
            problem = reader.parseInstance(filename);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public SolverStatus solve(String filename) {
        return solve(filename, 0);
    }

    public SolverStatus solve(String filename, int timeout) {
        if (timeout > 0) {
            solver.setTimeout(timeout);
        }
        if (!readDimacsFile(filename)) {
            status = SolverStatus.ERROR;
        } else {
            try {
                if (problem.isSatisfiable()) {
                    status = SolverStatus.SAT;
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter out = new PrintWriter(stringWriter);
                    reader.decode(problem.model(), out);
                    parseAssignment(stringWriter.toString());
                } else {
                    status = SolverStatus.UNSAT;
                }
            } catch (TimeoutException e) {
                e.printStackTrace();
                status = SolverStatus.TIMEOUT;
            }
        }
        return status;
    }

    public SolverStatus solve(ArrayList<int[]> prob, int max_var) {
        int NBCLAUSES = prob.size();
        solver = SolverFactory.newDefault();
        solver.newVar(max_var);
        solver.setExpectedNumberOfClauses(NBCLAUSES);
        for (int[] clause : prob) {
            try {
                solver.addClause(new VecInt(clause)); // adapt Array to IVecInt
            } catch (ContradictionException e) {
//                e.printStackTrace();
                status = SolverStatus.UNSAT;
                return status;
            }
        }
        problem = solver;
        try {
            if (problem.isSatisfiable()) {
                status = SolverStatus.SAT;
                StringWriter stringWriter = new StringWriter();
                PrintWriter out = new PrintWriter(stringWriter);
                reader.decode(problem.model(), out);
                parseAssignment(stringWriter.toString());
            } else {
                status = SolverStatus.UNSAT;
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
            status = SolverStatus.TIMEOUT;
        }
        return status;
    }

    public long count_solutions(ArrayList<int[]> problem, int max_var) throws ContradictionException {
        int NBCLAUSES = problem.size();
        solver = SolverFactory.newDefault();
        solver.newVar(max_var);
        solver.setExpectedNumberOfClauses(NBCLAUSES);
        for (int[] clause : problem) {
            solver.addClause(new VecInt(clause)); // adapt Array to IVecInt
        }
        try {
            SolutionCounter counter = new SolutionCounter(solver);
            return counter.countSolutions();

        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public long get_cell_single_solution(ArrayList<int[]> problem, int max_var) throws ContradictionException {
        int NBCLAUSES = problem.size();
        solver = SolverFactory.newDefault();
        solver.newVar(max_var);
        solver.setExpectedNumberOfClauses(NBCLAUSES);
        for (int[] clause : problem) {
            solver.addClause(new VecInt(clause)); // adapt Array to IVecInt
        }
        IProblem iproblem = solver;
        try {
            if (iproblem.isSatisfiable()) {
                for (int i = 0; i < max_var; i++) {
                    if (iproblem.model(i + 1)) {
                        return i + 1;
                    }
                }
            }
            return -1;
        } catch (TimeoutException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public ArrayList<Integer> get_cell_solutions(ArrayList<int[]> problem, int max_var) throws ContradictionException {
        ArrayList<Integer> ans = new ArrayList<>();
        int NBCLAUSES = problem.size();
        solver = SolverFactory.newDefault();
        solver.newVar(max_var);
        solver.setExpectedNumberOfClauses(NBCLAUSES);
        for (int[] clause : problem) {
            solver.addClause(new VecInt(clause)); // adapt Array to IVecInt
        }
        IProblem iproblem = solver;
        try {
            while (iproblem.isSatisfiable()) {
                for (int i = 0; i < max_var; i++) {
                    if (iproblem.model(i + 1)) {
                        ans.add(i + 1);
                    }
                }
                int[] new_clause = {-ans.get(ans.size() - 1)};
                solver.addClause(new VecInt(new_clause));
            }
            return ans;
        } catch (TimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void parseAssignment(String assignmentStr) {
        StringTokenizer st = new StringTokenizer(assignmentStr);
        assignment = new boolean[solver.nVars() + 1];
        while (st.hasMoreTokens()) {
            int x = Integer.parseInt(st.nextToken());
            if (x == 0) break;
            assignment[Math.abs(x)] = x > 0;
        }
    }

    public boolean valueOf(int var) {
        if (status != SolverStatus.SAT) throw new IllegalStateException();
        if (var > solver.nVars()) throw new IllegalArgumentException("var does not exist");

        return var > 0 ? assignment[var] : !assignment[Math.abs(var)];
    }
}
