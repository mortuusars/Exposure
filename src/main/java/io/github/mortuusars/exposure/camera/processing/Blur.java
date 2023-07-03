package io.github.mortuusars.exposure.camera.processing;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class Blur {
    public static BufferedImage applyGaussianBlur(BufferedImage image, int radius) {
        // Define the kernel size based on the radius
        int size = radius * 2 + 1;

        // Create a Gaussian blur kernel
        float[] kernelData = createGaussianKernel(radius);
        Kernel kernel = new Kernel(size, size, kernelData);

        // Create a ConvolveOp with the blur kernel
        ConvolveOp convolveOp = new ConvolveOp(kernel);

        // Apply the blur operation to the image
        BufferedImage blurredImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        convolveOp.filter(image, blurredImage);

        return blurredImage;
    }

    private static float[] createGaussianKernel(int radius) {
        int size = radius * 2 + 1;
        float[] kernelData = new float[size * size];
        double sigma = radius / 3.0; // Adjust the sigma value to control the blur intensity

        double twoSigmaSquare = 2.0 * sigma * sigma;
        double sigmaRoot = Math.sqrt(twoSigmaSquare * Math.PI);
        double total = 0.0;

        int index = 0;
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                double distance = (i * i + j * j) / twoSigmaSquare;
                double weight = Math.exp(-distance) / sigmaRoot;
                kernelData[index] = (float) weight;
                total += weight;
                index++;
            }
        }

        // Normalize the kernel values
        for (int i = 0; i < kernelData.length; i++) {
            kernelData[i] /= total;
        }

        return kernelData;
    }
}
