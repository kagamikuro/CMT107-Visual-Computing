package package1;

import package1.Filter;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;


public class Harris 
{
    /**
     * this is the main method for doing the harris corner detector algorithm
      * @param image the input image
     * @return
     */
    public static BufferedImage Apply_Harris(BufferedImage image)
        {
            //instantiate a Filter object
            Filter filter = new Filter();

            //Firstly doing edge filter for the input image
            BufferedImage horizontal_sobel_image = filter.Apply_Sobel_Horizontal_Edge_Filter(deepCopy(image));
            BufferedImage vertical_sobel_image = filter.Apply_Sobel_Vertical_Edge_Filter(deepCopy(image));

            int[][] Ix2 = new int[horizontal_sobel_image.getHeight()][ horizontal_sobel_image.getWidth()];
            int[][] Iy2 = new int[horizontal_sobel_image.getHeight()][horizontal_sobel_image.getWidth()];
            int[][] Ixy = new int[horizontal_sobel_image.getHeight()][horizontal_sobel_image.getWidth()];

            for (int i = 0; i < horizontal_sobel_image.getHeight(); i++)
            {
                for (int j = 0; j < horizontal_sobel_image.getWidth(); j++)
                {
                    Ix2[i][j] = (int)Math.pow(new Color(horizontal_sobel_image.getRGB(j, i)).getRed(), 2);
                    Iy2[i][j] = (int)Math.pow(new Color(vertical_sobel_image.getRGB(j, i)).getRed(), 2);
                    Ixy[i][j] = new Color(horizontal_sobel_image.getRGB(j, i)).getRed() * new Color(vertical_sobel_image.getRGB(j, i)).getRed();
                }
            }

            //Then doing Gaussian derivative for each pixel
            filter.Apply_Gaussian_7x7_Filter(Ix2, Iy2, Ixy);

            int height = Ix2.length, width = Ix2[0].length;
            int[][] result = new int[height][width];
            int[][] R = new int[height][ width];
            int Rmax = 0;

            for (int i = 0; i < height; i++)
            {
                for (int j = 0; j < width; j++)
                {
                    //doing R = I1*I2 - a(I1+I2)
                    int Ix2_pixel = Ix2[i][j], Iy2_pixel = Iy2[i][j], Ixy_pixel = Ixy[i][j];
                    int det = Ix2_pixel * Iy2_pixel - Ixy_pixel * Ixy_pixel;
                    int trace = Ix2_pixel + Iy2_pixel;
                    R[i][j] = det - (int)(0.01 * Math.pow(trace, 2));
                    if (R[i][j] > Rmax)
                        Rmax = R[i][j];
                }
            }
            int cnt = 0;
            for (int i = 1; i < height - 1; i++)
            {
                for (int j = 1; j < width - 1; j++)
                {
                    //if the pixel is detect as Corner , the we set this image pixel point as red color
                    if (R[i][j] > 0.001 * Rmax && R[i][j] > R[i - 1][ j - 1] && R[i][j] > R[i - 1][j] && R[i][j] > R[i - 1][j + 1] && R[i][j] > R[i] [j - 1] && R[i][j] > R[i][j + 1] && R[i][j] > R[i + 1][j - 1] && R[i][j] > R[i + 1][j] && R[i][j] > R[i + 1][j + 1])
                    {
                        result[i][j] = 255;
                        cnt++;
                    }
                }
            }
            
            int count = 0;


            //load the processed data into image
            for (int i = 0; i < height; i++)
            {
                for (int j = 0; j < width; j++)
                {
                	if(result[i][j] == 255) {
                		image.setRGB(j, i, new Color(result[i][j],0,0).getRGB());
                		count ++;
                	}
                }
            }
            
            
            System.out.println("number of corner point: "+count);

            return image;
        }

    /**
     * doing a deep copy for the input image
     * @param image the input image
     * @return
     */
    public static BufferedImage deepCopy(BufferedImage image) {
        	 ColorModel cm = image.getColorModel();
        	 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        	 WritableRaster raster = image.copyData(null);
        	 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        	}

    }



