import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageNormalization {

  public BufferedImage normalize(BufferedImage src) {
    BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(),
        BufferedImage.TYPE_BYTE_GRAY);

    WritableRaster wr = src.getRaster();
    WritableRaster er = out.getRaster();

    int totpix = wr.getWidth() * wr.getHeight();

    int[] histogram = new int[256];

    //представление изображения в виде гистограммы при помощи получения определенного пикселя
    for (int x = 0; x < wr.getWidth(); x++) {
      for (int y = 0; y < wr.getHeight(); y++) {
        histogram[wr.getSample(x, y, 0)]++;
      }
    }

    int[] chistogram = new int[256];
    chistogram[0] = histogram[0];
    for (int i = 1; i < 256; i++) {
      chistogram[i] = chistogram[i - 1] + histogram[i];
    }

    float[] arr = new float[256];
    for (int i = 0; i < 256; i++) {
      arr[i] = (float) ((chistogram[i] * 255.0) / (float) totpix);
    }

    for (int x = 0; x < wr.getWidth(); x++) {
      for (int y = 0; y < wr.getHeight(); y++) {
        int nVal = (int) arr[wr.getSample(x, y, 0)];
        er.setSample(x, y, 0, nVal);
      }
    }
    out.setData(er);
    return out;
  }

  public static void main(String[] args) throws Exception {
    String path = args[0];
    String imageName = args[1];
    String fileExt = args[2];

    String originalImagePath = path + imageName + "." + fileExt;
    String bluredImagePath = path + imageName + "-normalized";
    BufferedImage source = ImageIO.read(new File(originalImagePath));

    ImageNormalization imageNormalization = new ImageNormalization();
    BufferedImage processedImage = imageNormalization.normalize(source);

    ImageIO.write(processedImage, fileExt, new File(bluredImagePath + "." + fileExt));
  }
}
