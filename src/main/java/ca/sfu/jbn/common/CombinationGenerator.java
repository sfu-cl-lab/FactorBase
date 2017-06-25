package ca.sfu.jbn.common;

//--------------------------------------
//Systematically generate combinations.
//--------------------------------------

import java.math.BigInteger;

public class CombinationGenerator {

    private int[] a;
    private int n;
    private int r;
    private BigInteger numLeft;
    private BigInteger total;

//------------
// Constructor
//------------

    public CombinationGenerator(int n, int r) {
        if (r > n) {
            throw new IllegalArgumentException();
        }
        if (n < 1) {
            throw new IllegalArgumentException();
        }
        this.n = n;
        this.r = r;
        a = new int[r];
        BigInteger nFact = getFactorial(n);
        BigInteger rFact = getFactorial(r);
        BigInteger nminusrFact = getFactorial(n - r);
        total = nFact.divide(rFact.multiply(nminusrFact));
        reset();
    }

//------
// Reset
//------

    private static BigInteger getFactorial(int n) {
        BigInteger fact = BigInteger.ONE;
        for (int i = n; i > 1; i--) {
            fact = fact.multiply(new BigInteger(Integer.toString(i)));
        }
        return fact;
    }

//------------------------------------------------
// Return number of combinations not yet generated
//------------------------------------------------

    public static void main(String[] args) {
        String[] elements = {"*", "f", "t", ""};
        int[] indices;
        CombinationGenerator x = new CombinationGenerator(elements.length, 3);
        StringBuffer combination;
        while (x.hasMore()) {
            combination = new StringBuffer();
            indices = x.getNext();
            for (int i = 0; i < indices.length; i++) {
                combination.append(elements[indices[i]]);
            }
            System.out.println(combination.toString());
        }

    }

//-----------------------------
// Are there more combinations?
//-----------------------------

    public void reset() {
        for (int i = 0; i < a.length; i++) {
            a[i] = i;
        }
        numLeft = new BigInteger(total.toString());
    }

//------------------------------------
// Return total number of combinations
//------------------------------------

    public BigInteger getNumLeft() {
        return numLeft;
    }

//------------------
// Compute factorial
//------------------

    public boolean hasMore() {
        return numLeft.compareTo(BigInteger.ZERO) == 1;
    }

//--------------------------------------------------------
// Generate next combination (algorithm from Rosen p. 286)
//--------------------------------------------------------

    public BigInteger getTotal() {
        return total;
    }

    public int[] getNext() {

        if (numLeft.equals(total)) {
            numLeft = numLeft.subtract(BigInteger.ONE);
            return a;
        }

        int i = r - 1;
        while (a[i] == n - r + i) {
            i--;
        }
        a[i] = a[i] + 1;
        for (int j = i + 1; j < r; j++) {
            a[j] = a[i] + j - i;
        }

        numLeft = numLeft.subtract(BigInteger.ONE);
        return a;

    }
}