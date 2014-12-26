package kdrone.mosaicapp;

public class MosaicParameter {
	public static final String MOSAIC_MODEL_FILENAME = "index.jpg";
	public static final String MOSAIC_FILENAME = "mosaic.jpg";
	public static final int MOSAIC_WIDTH = 8640;
	public static final int MOSAIC_HEIGHT = 6380;
	public static final int MOSAIC_ROWS = 100;
	public static final int MOSAIC_COLS = 100;
	public static final int TILE_WIDTH = MOSAIC_WIDTH / MOSAIC_COLS;
	public static final int TILE_HEIGHT = MOSAIC_HEIGHT / MOSAIC_ROWS;
	public static final int MAX_HUE_VALUE = 1;
	public static final int NUMBER_OF_HUE_BUCKET = 10;
	public static final String PHOTO_FOLDER_NAME = "C:\\Users\\Administrator\\workspace\\PhotoMosaic\\images\\";
}