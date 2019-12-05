package package1;

import java.awt.Color;
import java.awt.image.BufferedImage;


 public class Filter
    {
        //initialise data for each necessary filters
        float[][] average_filter=
                {
                	{(float)1/9,(float)1/9,(float)1/9},
                	{(float)1/9,(float)1/9,(float)1/9},
                	{(float)1/9,(float)1/9,(float)1/9}
                };

        float[][] sobel_vertical_edge_filter=
                	{
                        {(float)1,(float)0,(float)-1},
                        {(float)2,(float)0,(float)-2},
                        {(float)1,(float)0,(float)-1}
                    };
        float[][] sobel_horizontal_edge_filter= 
                	{
                        {(float)1,(float)2,(float)1},
                        {(float)0,(float)0,(float)0},
                        {(float)-1,(float)-2,(float)-1}
                    };

        float[][] gaussian_filter=
        	{
                {0.0001f  ,  0.0015f  ,  0.0067f  ,  0.0111f  ,  0.0067f  ,  0.0015f  ,  0.0001f},
                {0.0015f  ,  0.0183f  ,  0.0821f  ,  0.1353f  ,  0.0821f  ,  0.0183f  ,  0.0015f},
                {0.0067f  ,  0.0821f  ,  0.3679f  ,  0.6065f  ,  0.3679f  ,  0.0821f  ,  0.0067f},
                {0.0111f  ,  0.1352f  ,  0.6065f  ,  1.0000f  ,  0.6065f  ,  0.1352f  ,  0.0111f},
                {0.0067f  ,  0.0821f  ,  0.3679f  ,  0.6065f  ,  0.3679f  ,  0.0821f  ,  0.0067f},
                {0.0015f  ,  0.0183f  ,  0.0821f  ,  0.1353f  ,  0.0821f  ,  0.0183f  ,  0.0015f},
                {0.0001f  ,  0.0015f  ,  0.0067f  ,  0.0111f  ,  0.0067f  ,  0.0015f  ,  0.0001f}
            };
        

        float[][] selected_filter;
        BufferedImage image;
        public Filter()
        {
        }

        /**
         * this step is necessary for doing 2 edge filter
         */
        public void Apply_Selected_Filter()
        {
            //get image object
        	BufferedImage tmp_image = new BufferedImage(image.getWidth() + average_filter.length - 1, image.getHeight() + average_filter[0].length - 1,BufferedImage.TYPE_INT_RGB);// zero padded image
            //copy image with zero-padding
            for (int i = 0; i < tmp_image.getHeight(); i++)
            {
                for (int j = 0; j < tmp_image.getWidth(); j++)
                {
                    
                    if (i < average_filter.length / 2 || i >= tmp_image.getHeight() - average_filter.length / 2 || j < average_filter[0].length / 2 || j >= tmp_image.getWidth() - average_filter[0].length / 2)
                        tmp_image.setRGB(j, i, new Color(0,0,0).getRGB());
                    else                        
                        tmp_image.setRGB(j, i, image.getRGB(j - average_filter.length / 2, i - average_filter[0].length / 2));
                }
            }
            
            // slide filter over image
            for (int i = average_filter.length / 2; i < tmp_image.getHeight() - average_filter.length / 2; i++)
            {
                for (int j = average_filter[0].length / 2; j < tmp_image.getWidth() - average_filter[0].length / 2; j++)
                {
                    float red = 0, green = 0, blue = 0;               // loop to do
                    for (int ki = 0; ki < selected_filter.length; ki++)
                    {
                        for (int kj = 0; kj < selected_filter[0].length; kj++)
                        {
                            red += selected_filter[ki][kj] * new Color(tmp_image.getRGB(kj + j - selected_filter[0].length / 2, ki + i - selected_filter.length / 2)).getRed();
                            green += selected_filter[ki][kj] * new Color(tmp_image.getRGB(kj + j - selected_filter[0].length / 2, ki + i - selected_filter.length / 2)).getGreen();
                            blue += selected_filter[ki][kj] * new Color(tmp_image.getRGB(kj + j - selected_filter[0].length / 2, ki + i - selected_filter.length / 2)).getBlue();
                        }
                    }
                    red = Math.max(red, 0); green = Math.max(green, 0); blue = Math.max(blue, 0);
                    red = Math.min(red, 255); green = Math.min(green, 255); blue = Math.min(blue, 255);
                    image.setRGB(j - average_filter[0].length / 2, i - average_filter.length / 2, new Color((int)red, (int)green, (int)blue).getRGB());
                }
            }
            
        }
        

        
        public BufferedImage Apply_Sobel_Vertical_Edge_Filter(BufferedImage image1)
        {
            selected_filter = sobel_vertical_edge_filter;
            image = image1;
            Apply_Selected_Filter();
            return image;
        }
        
        public BufferedImage Apply_Sobel_Horizontal_Edge_Filter(BufferedImage image1)
        {
            selected_filter = sobel_horizontal_edge_filter;
            image = image1;
            Apply_Selected_Filter();
            return image;
        }
        

        public void Apply_Gaussian_7x7_Filter(int[][] Ix2, int[][] Iy2, int[][] Ixy)
        {
            selected_filter = gaussian_filter;
            apply_gaussian_filter(Ix2, Iy2, Ixy);
        }

        /**
         * the main method for gaussian filter,which remove high frequency component from image
         * @param Ix2 I1
         * @param Iy2 I2
         * @param Ixy I1*I2
         */
        public void apply_gaussian_filter(int[][] Ix2, int[][] Iy2, int[][] Ixy)
        {
            int[][] Ix2_tmp = new int[Ix2.length + selected_filter.length - 1][Ix2[0].length+ selected_filter[0].length - 1];// zero padded image
            int[][] Iy2_tmp = new int[Ix2.length + selected_filter.length - 1][Ix2[0].length + selected_filter[0].length - 1];// zero padded image
            int[][] Ixy_tmp = new int[Ix2.length + selected_filter.length - 1][Ix2[0].length + selected_filter[0].length - 1];// zero padded image

            // copy image with zero-padding
            for (int i = 0; i < Ix2_tmp.length; i++)
            {
                for (int j = 0; j < Ix2_tmp[0].length; j++)
                {
                    
                    if (i < selected_filter.length / 2 || i >= Ix2_tmp.length - selected_filter.length / 2 || j < selected_filter[0].length / 2 || j >= Ix2_tmp[0].length - selected_filter[0].length / 2)
                    {
                        Ix2_tmp[i][j] = 0;
                        Iy2_tmp[i][j] = 0;
                        Ixy_tmp[i][j] = 0;
                    }
                    else
                    {
                        Ix2_tmp[i][j] = Ix2[i - selected_filter.length / 2][ j - selected_filter[0].length / 2];
                        Iy2_tmp[i][j] = Iy2[i - selected_filter.length / 2][ j - selected_filter[0].length / 2];
                        Ixy_tmp[i][j] = Ixy[i - selected_filter.length / 2][ j - selected_filter[0].length / 2];
                    }
                }
            }
           
            //slide filter over image
            for (int i = selected_filter.length / 2; i < Ix2.length - selected_filter.length / 2; i++)
            {
                for (int j = selected_filter[0].length / 2; j < Ix2[0].length- selected_filter[0].length / 2; j++)
                {
                    double Ix2_value = 0, Iy2_value = 0, Ixy_value = 0;
                    for (int ki = 0; ki < selected_filter.length; ki++)
                    {
                        for (int kj = 0; kj < selected_filter[0].length; kj++)
                        {
                            Ix2_value += selected_filter[ki][kj] * Ix2_tmp[ki + i - selected_filter.length / 2][kj + j - selected_filter[0].length / 2];
                            Iy2_value += selected_filter[ki][kj] * Iy2_tmp[ki + i - selected_filter.length / 2][kj + j - selected_filter[0].length / 2];
                            Ixy_value += selected_filter[ki][kj] * Ixy_tmp[ki + i - selected_filter.length / 2][kj + j - selected_filter[0].length / 2];
                        }
                    }
                    Ix2[i][j] = (int)Ix2_value;
                    Iy2[i][j] = (int)Iy2_value;
                    Ixy[i][j] = (int)Ixy_value;
                }
            }
          
        }

    }
