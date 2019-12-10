import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class LinearBlurFilterByMask {

  private static final Double[][] BLUR_MASK = {{0.000789, 0.006581, 0.013347, 0.006581, 0.000789},
      {0.006581, 0.054901, 0.111345, 0.054901, 0.006581},
      {0.013347, 0.111345, 0.225821, 0.111345, 0.013347},
      {0.006581, 0.054901, 0.111345, 0.054901, 0.006581},
      {0.000789, 0.006581, 0.013347, 0.006581, 0.000789}};

  public BufferedImage blur(BufferedImage source, int n) {
    int sourceWidth = source.getWidth();
    int sourceHeight = source.getHeight();
    int halfKernelLength = (n / 2);

    int tmpH = sourceHeight + 2 * halfKernelLength;
    int tmpW = sourceWidth + 2 * halfKernelLength;

    int[][] sourcePixels = imageToArray(source);
    int[][] newPixels = new int[sourceHeight][sourceWidth];

    //применение ядра свертки
    for (int x = halfKernelLength; x < tmpH - halfKernelLength; x++) {
      for (int y = halfKernelLength; y < tmpW - halfKernelLength; y++) {

        double rSum = 0;
        double gSum = 0;
        double bSum = 0;
        double kSum = 0;
        for (int i = 0; i < n; i++) {
          for (int j = 0; j < n; j++) {
            int posX = x - halfKernelLength + i;
            int posY = y - halfKernelLength + j;

            //предотвращение выхода за приделы изображения
            if (posX < 0 || posX >= sourceHeight) {
              continue;
            }
            if (posY < 0 || posY >= sourceWidth) {
              continue;
            }

            int pixel = sourcePixels[posX][posY];
            Double kernelVal = BLUR_MASK[i][j];

            //вычисление нового цвета путем преобразования пикселя в необходимый цвет
            // и умножением на значение матрицы свертки
            rSum += kernelVal * ((pixel & 0x00FF0000) >> 16);
            gSum += kernelVal * ((pixel & 0x0000FF00) >> 8);
            bSum += kernelVal * (pixel & 0x000000FF);

            kSum += kernelVal;
          }
        }
        //контролируем переполнение переменных
        rSum /= kSum;
        if (rSum < 0) {
          rSum = 0;
        }
        if (rSum > 255) {
          rSum = 255;
        }

        gSum /= kSum;
        if (gSum < 0) {
          gSum = 0;
        }
        if (gSum > 255) {
          gSum = 255;
        }

        bSum /= kSum;
        if (bSum < 0) {
          bSum = 0;
        }
        if (bSum > 255) {
          bSum = 255;
        }

        newPixels[x - halfKernelLength][y - halfKernelLength] =
            //восстановление значения пикселя
            0xFF000000 | (int) rSum << 16 | (int) gSum << 8 | (int) bSum;
      }
    }

    BufferedImage blurredImage = new BufferedImage(sourceWidth, sourceHeight, TYPE_INT_RGB);

    return arrayToImage(newPixels, blurredImage);
  }

  private int[][] imageToArray(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();
    int[][] result = new int[height][width];

    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width; col++) {
        result[row][col] = image.getRGB(col, row);
      }
    }

    return result;
  }

  private BufferedImage arrayToImage(int[][] pixelData, BufferedImage outputImage) {
    int width = outputImage.getWidth();
    int height = outputImage.getHeight();

    for (int y = 0, pos = 0; y < height; y++) {
      for (int x = 0; x < width; x++, pos++) {
        outputImage.setRGB(x, y, pixelData[y][x]);
      }
    }

    return outputImage;
  }

  public static void main(String[] args) throws Exception {
    String path = args[0];
    String imageName = args[1];
    String fileExt = args[2];

    String originalImagePath = path + imageName + "." + fileExt;
    String bluredImagePath = path + imageName + "-blurred";
    BufferedImage source = ImageIO.read(new File(originalImagePath));

    LinearBlurFilterByMask linearBlurFilterByMask = new LinearBlurFilterByMask();
    BufferedImage processedImage = linearBlurFilterByMask.blur(source, 5);

    ImageIO.write(processedImage, fileExt, new File(bluredImagePath + "." + fileExt));
  }
}
