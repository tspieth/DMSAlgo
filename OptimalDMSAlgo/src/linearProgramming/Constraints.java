package linearProgramming;

import java.util.ArrayList;

// Constraints as Ax <= b
public class Constraints {
    final double[][] A;
    final double[] b;

    Constraints(double[][] A, double[] b) {
        this.A = A;
        this.b = b;
    }

}
