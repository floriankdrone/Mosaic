package kdrone.mosaicapp.photo;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import kdrone.mosaicapp.MosaicParameter;

public class Mosaic {

	private Photo[][] tiles_;
	private int width_, height_;

	public Mosaic(Photo[][] photos, int width, int height) {
		tiles_ = photos;
		width_ = width;
		height_ = height;
	}

	public Photo generate() {
		long start = System.currentTimeMillis();
		BufferedImage bufferedImageMosaic_ = new BufferedImage(
				MosaicParameter.MOSAIC_WIDTH, MosaicParameter.MOSAIC_HEIGHT,
				BufferedImage.TYPE_INT_BGR);
		Graphics2D g2 = bufferedImageMosaic_.createGraphics();
		g2.fillRect(0, 0, width_, height_);
		for (int row = 0; row < MosaicParameter.MOSAIC_ROWS; row++) {
			for (int col = 0; col < MosaicParameter.MOSAIC_COLS; col++) {
				BufferedImage tilePhoto = new BufferedImage(MosaicParameter.TILE_WIDTH, MosaicParameter.TILE_HEIGHT,
						BufferedImage.TYPE_INT_BGR);
				Graphics2D g = tilePhoto.createGraphics();
				g.drawImage(tiles_[row][col].getBufferedImage(), 0, 0, MosaicParameter.TILE_WIDTH,
						MosaicParameter.TILE_HEIGHT, null);
				g.dispose();
				g2.drawImage(tilePhoto, null, MosaicParameter.TILE_WIDTH * row,
						MosaicParameter.TILE_HEIGHT * col);
			}
		}
		g2.dispose();
		long end = System.currentTimeMillis();
		System.out.println("Mosaic Generation duration = " + (end - start));
		return new Photo("mosaic"
				+ (MosaicParameter.MOSAIC_HEIGHT / MosaicParameter.TILE_HEIGHT)
				+ "-"
				+ (MosaicParameter.MOSAIC_WIDTH / MosaicParameter.TILE_WIDTH)
				+ ".jpg", bufferedImageMosaic_);
	}
}
