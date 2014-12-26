package kdrone.mosaicapp.photo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

@SuppressWarnings("serial")
public class Photo extends File {

	private int width_, height_, averageRedColor_, averageGreenColor_,
			averageBlueColor_;
	private float[] hsv_;
	private String photoName_;
	private BufferedImage bufferedImage_;

	public Photo(String imageName, BufferedImage bufferedImage) {
		super(imageName);
		this.photoName_ = imageName;
		bufferedImage_ = bufferedImage;
		width_ = bufferedImage_.getWidth();
		height_ = bufferedImage_.getHeight();
	}
	
	public Photo(String filePath) {
		super(filePath);
		this.photoName_ = this.getName();
		try {
			bufferedImage_ = ImageIO.read(this);
			width_ = bufferedImage_.getWidth();
			height_ = bufferedImage_.getHeight();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getAverageRedColor() {
		return averageRedColor_;
	}

	public int getAverageGreenColor() {
		return averageGreenColor_;
	}

	public int getAverageBlueColor() {
		return averageBlueColor_;
	}

	public BufferedImage getBufferedImage() {
		return bufferedImage_;
	}

	public Graphics2D createGraphics() {
		return bufferedImage_.createGraphics();
	}

	public String toInfoString() {
		return this.getAbsolutePath() + " " + averageRedColor_ + " "
				+ averageGreenColor_ + " " + averageBlueColor_;
	}

	public int getWidth() {
		return width_;
	}

	public int getHeight() {
		return height_;
	}

	public String getPhotoName() {
		return photoName_;
	}

	public int getType() {
		return bufferedImage_.getType();
	}

	public int[] determineColorsAverage() {
		// Do some downsampling
		int[] rgb = new int[3];
		int blueTotal = 0;
		int redTotal = 0;
		int greenTotal = 0;
		int pixelNum = width_ * height_;
		for (int i = 0; i < width_; i++) {
			for (int j = 0; j < height_; j++) {
				int rgbValue = bufferedImage_.getRGB(i, j);
				Color pixelColor = new Color(rgbValue);
				blueTotal += pixelColor.getBlue();
				greenTotal += pixelColor.getGreen();
				redTotal += pixelColor.getRed();
			}
		}
		averageRedColor_ = redTotal / pixelNum;
		averageGreenColor_ = greenTotal / pixelNum;
		averageBlueColor_ = blueTotal / pixelNum;
		rgb[0] = averageRedColor_;
		rgb[1] = averageGreenColor_;
		rgb[2] = averageBlueColor_;
		hsv_ = new float[3];
		Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsv_);
		return rgb;
	}

	public float getHue() {
		return hsv_[0];
	}

	public void createPhoto(File outputFolder) {
		try {
			ImageIO.write(bufferedImage_, "jpg", new File(outputFolder + "/"
					+ photoName_));
		} catch (IOException e) {
			// TODO Auto-generated catch bloc
			e.printStackTrace();
		}
	}

	public void createPhoto() {
		try {
			ImageIO.write(bufferedImage_, "jpg", this);
		} catch (IOException e) {
			// TODO Auto-generated catch bloc
			e.printStackTrace();
		}
	}
}
