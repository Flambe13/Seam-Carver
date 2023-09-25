import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Stopwatch;

import java.awt.Color;

public class SeamCarver {


    // the current picture
    Picture current;
    // the current width
    int width;
    // the current height
    int height;


    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException("Picture cannot be null");
        }
        // initialize
        this.current = new Picture(picture);
        width = picture.width();
        height = picture.height();


    }

    // returns current picture
    public Picture picture() {
        return this.current;
    }

    // returns width of current picture
    public int width() {
        return width;
    }

    // returns height of current picture
    public int height() {
        return height;
    }


    // returns the gradient of the pixels left or right
    private double gradientX(int x, int y) {
        int xLeft;
        int xRight;
        // if x is 0
        if (x == 0) {
            xLeft = width - 1;
        }
        // if x is not 0
        else {
            xLeft = x - 1;
        }
        // if x is width - 1
        if (x == width - 1) {
            xRight = 0;
        }
        // if x is not width - 1
        else {
            xRight = x + 1;
        }
        // get colors and compute gradients
        Color colorLeft = current.get(xLeft, y);
        Color colorRight = current.get(xRight, y);
        int redDiff = colorLeft.getRed() - colorRight.getRed();
        int blueDiff = colorLeft.getBlue() - colorRight.getBlue();
        int greenDiff = colorLeft.getGreen() - colorRight.getGreen();
        double gradient = redDiff * redDiff + blueDiff * blueDiff + greenDiff * greenDiff;
        return gradient;
    }

    // returns the gradient of the pixels above or below
    private double gradientY(int x, int y) {
        int yUp;
        int yDown;
        // if y is 0
        if (y == 0) {
            yUp = height - 1;
        }
        // if y is not 0
        else {
            yUp = y - 1;
        }
        // if y is height - 1
        if (y == height - 1) {
            yDown = 0;
        }
        // if y is not height - 1
        else {
            yDown = y + 1;
        }
        // get colors and compute gradients
        Color colorUp = current.get(x, yUp);
        Color colorDown = current.get(x, yDown);
        int redDiff = colorUp.getRed() - colorDown.getRed();
        int blueDiff = colorUp.getBlue() - colorDown.getBlue();
        int greenDiff = colorUp.getGreen() - colorDown.getGreen();
        double gradient = redDiff * redDiff + blueDiff * blueDiff + greenDiff * greenDiff;
        return gradient;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        // throw exceptions if needed
        if (x < 0 || x > width - 1) {
            throw new IllegalArgumentException("x is not range of the picture");
        }
        if (y < 0 || y > height - 1) {
            throw new IllegalArgumentException("y is not range of the picture");
        }
        // compute energy
        double energy = Math.sqrt(gradientX(x, y) + gradientY(x, y));
        return energy;
    }

    // relaxes the various arrays if needed
    private void relaxArrays(int xParent, int yParent, int xChild, int yChild,
                             double[][] distToGrid, double[][] energies,
                             int[][] shortParents) {
        // only relaxes valid points
        if (xChild == width || xChild == -1) {
            return;
        }
        // relax points
        double energy = energies[xChild][yChild];
        if (distToGrid[xParent][yParent] + energy < distToGrid[xChild][yChild]) {
            distToGrid[xChild][yChild] = distToGrid[xParent][yParent] + energy;
            shortParents[xChild][yChild] = xParent;
        }
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        // initialize arrays
        double[][] distToGrid = new double[width][height];
        int[][] shortParents = new int[width][height];
        double[][] energies = new double[width][height];
        // fill energies array
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                energies[col][row] = energy(col, row);
            }
        }
        // fill in first row using energies
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (row == 0) {
                    distToGrid[col][row] = energies[col][row];
                }
                else {
                    distToGrid[col][row] = Double.POSITIVE_INFINITY;
                }
            }
        }
        // relax all other pixels to find shortest distance
        for (int row = 1; row < height; row++) {
            for (int col = 0; col < width; col++) {
                relaxArrays(col, row - 1, col - 1, row, distToGrid, energies,
                            shortParents);
                relaxArrays(col, row - 1, col, row, distToGrid, energies,
                            shortParents);
                relaxArrays(col, row - 1, col + 1, row, distToGrid, energies,
                            shortParents);
            }
        }
        // find the smallest distance to on the bottom row
        double min = distToGrid[0][height - 1];
        int colSmall = 0;
        for (int col = 1; col < width; col++) {
            if (distToGrid[col][height - 1] < min) {
                colSmall = col;
                min = distToGrid[col][height - 1];
            }
        }
        // find the rest of the path
        int[] vertSeam = new int[height];
        int row = height - 1;
        vertSeam[row] = colSmall;
        while (0 < row) {
            vertSeam[row - 1] = shortParents[vertSeam[row]][row];
            row--;
        }

        return vertSeam;
    }

    // returns the transposed picture of the input
    private Picture transpose(Picture picture) {
        Picture transposed = new Picture(picture.height(), picture.width());
        for (int i = 0; i < picture.height(); i++) {
            for (int j = 0; j < picture.width(); j++) {
                transposed.set(i, j, picture.get(j, i));
            }
        }
        int heightTracker = height;
        height = width;
        width = heightTracker;
        return transposed;
    }


    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        current = transpose(current);
        int[] horSeam = findVerticalSeam();
        current = transpose(current);
        return horSeam;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        // throw exceptions if needed
        if (height == 1) {
            throw new IllegalArgumentException("Height is 1");
        }
        if (seam == null) {
            throw new IllegalArgumentException("Seam cannot be null");
        }
        if (seam.length != width) {
            throw new IllegalArgumentException("Seam is not the right length");
        }
        if (seam[0] > height || seam[0] < 0) {
            throw new IllegalArgumentException("Value is out of range");
        }
        for (int col = 1; col < width; col++) {
            if (seam[col] > height || seam[col] < 0) {
                throw new IllegalArgumentException("Value is out of range");
            }
            if (seam[col] - seam[col - 1] < -1 || seam[col] - seam[col - 1] > 1) {
                throw new IllegalArgumentException("Move between nodes is "
                                                           + "greater than 1");
            }
        }
        // remove seam
        int heightNew = height;
        Picture newPic = new Picture(width, heightNew - 1);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < heightNew - 1; row++) {
                if (row < seam[col]) {
                    newPic.set(col, row, current.get(col, row));
                }
                if (row >= seam[col]) {
                    newPic.set(col, row, current.get(col, row + 1));
                }
            }
        }
        // update current values
        current = newPic;
        width = current.width();
        height = current.height();
    }


    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (width == 1) {
            throw new IllegalArgumentException("Width is 1");
        }
        if (seam == null) {
            throw new IllegalArgumentException("Seam cannot be null");
        }
        if (seam.length > height) {
            throw new IllegalArgumentException("Seam is not the right length");
        }
        if (seam[0] > width || seam[0] < 0) {
            throw new IllegalArgumentException("Value is out of range");
        }
        for (int row = 1; row < height; row++) {
            if (seam[row] > width || seam[row] < 0) {
                throw new IllegalArgumentException("Value is out of range");
            }
            if (seam[row] - seam[row - 1] < -1 || seam[row] - seam[row - 1] > 1) {
                throw new IllegalArgumentException("Move between nodes is "
                                                           + "greater than 1");
            }
        }
        int widthNew = width;
        Picture newPic = new Picture(widthNew - 1, height);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < widthNew - 1; col++) {
                if (col < seam[row]) {
                    newPic.set(col, row, current.get(col, row));
                }
                if (col >= seam[row]) {
                    newPic.set(col, row, current.get(col + 1, row));
                }
            }
        }
        current = newPic;
        width = current.width();
        height = current.height();
    }

    //  unit testing (required)
    public static void main(String[] args) {
        Picture pic = new Picture("city8000-by-2000.png");
        SeamCarver sc = new SeamCarver(pic);
        double energy = sc.energy(1, 1);
        int height = sc.height();
        int width = sc.width();
        Stopwatch time = new Stopwatch();
        for (int i = 0; i < 1; i++) {
            int[] seam1 = sc.findHorizontalSeam();
            sc.removeHorizontalSeam(seam1);
        }
        for (int i = 0; i < 1; i++) {
            int[] seam1 = sc.findVerticalSeam();
            sc.removeVerticalSeam(seam1);
        }
        StdOut.println(time.elapsedTime());
        pic = sc.picture();


    }

}
