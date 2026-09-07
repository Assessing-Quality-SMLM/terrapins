package smlm.fastfit;

/**
 * Dense linear least squares by Householder QR.
 * <p>
 * Used only during calibration (a few thousand rows, at most 28 columns), so speed is
 * irrelevant and stability is not: the design matrix contains powers up to the sixth of the
 * moment values, which makes the normal equations noticeably ill-conditioned. QR avoids
 * squaring the condition number.
 */
public final class LeastSquares {

    private LeastSquares() {
    }

    /**
     * Solves {@code min ||A x - b||} in the least-squares sense.
     *
     * @param a design matrix, {@code m} rows by {@code n} columns; modified in place
     * @param b right-hand side of length {@code m}; modified in place
     * @return the coefficient vector of length {@code n}
     * @throws IllegalArgumentException if the system is rank deficient or badly shaped
     */
    public static double[] solve(double[][] a, double[] b) {
        int m = a.length;
        if (m == 0) {
            throw new IllegalArgumentException("empty design matrix");
        }
        int n = a[0].length;
        if (m < n) {
            throw new IllegalArgumentException(
                    "under-determined system: " + m + " rows, " + n + " unknowns");
        }
        if (b.length != m) {
            throw new IllegalArgumentException("A and b have different row counts");
        }

        double[] rDiagonal = new double[n];

        for (int k = 0; k < n; k++) {
            // Householder vector for column k
            double norm = 0.0;
            for (int i = k; i < m; i++) {
                norm = Math.hypot(norm, a[i][k]);
            }
            if (norm < 1e-300) {
                throw new IllegalArgumentException(
                        "rank-deficient design matrix at column " + k);
            }
            // Choose the sign that avoids cancellation when forming the Householder vector.
            if (a[k][k] < 0) {
                norm = -norm;
            }
            for (int i = k; i < m; i++) {
                a[i][k] /= norm;
            }
            a[k][k] += 1.0;

            // apply the reflection to the remaining columns
            for (int j = k + 1; j < n; j++) {
                double s = 0.0;
                for (int i = k; i < m; i++) {
                    s += a[i][k] * a[i][j];
                }
                s = -s / a[k][k];
                for (int i = k; i < m; i++) {
                    a[i][j] += s * a[i][k];
                }
            }
            // and to the right-hand side
            double s = 0.0;
            for (int i = k; i < m; i++) {
                s += a[i][k] * b[i];
            }
            s = -s / a[k][k];
            for (int i = k; i < m; i++) {
                b[i] += s * a[i][k];
            }

            rDiagonal[k] = -norm;
        }

        // back substitution on the upper triangle
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double s = b[i];
            for (int j = i + 1; j < n; j++) {
                s -= a[i][j] * x[j];
            }
            if (Math.abs(rDiagonal[i]) < 1e-300) {
                throw new IllegalArgumentException("singular R at row " + i);
            }
            x[i] = s / rDiagonal[i];
        }
        return x;
    }
}
