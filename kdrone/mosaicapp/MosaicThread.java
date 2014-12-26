package kdrone.mosaicapp;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import kdrone.mosaicapp.photo.Mosaic;
import kdrone.mosaicapp.photo.Photo;

public class MosaicThread implements Runnable {

	private static final Logger logger_ = Logger.getLogger(MosaicThread.class
			.getName());
	private static final File photoFolder_ = new File(MosaicParameter.PHOTO_FOLDER_NAME);

	private Photo photoModel_;
	private Photo[][] mosaicTiles_;
	private Hashtable<Integer, ArrayList<Photo>> photoHashtable_;
	private int photoModelWidth_, photoModelHeight_,
			photoChunkWidth_, photoChunkHeight_;

	public MosaicThread() {
		initializeLogger();
		logger_.log(Level.FINER, "beg.");
		logger_.fine("Tile Size determined: width= "+MosaicParameter.TILE_WIDTH+ " and height= "+MosaicParameter.TILE_HEIGHT);
		photoModel_ = new Photo(MosaicParameter.MOSAIC_MODEL_FILENAME);;
		logger_.log(Level.FINE,
				"Create a mosaic with model " + photoModel.getAbsolutePath());
		photoModelWidth_ = photoModel.getWidth();
		photoModelHeight_ = photoModel.getHeight();
		photoChunkWidth_ = photoModelWidth_ / MosaicParameter.MOSAIC_COLS;
		photoChunkHeight_ = photoModelHeight_ / MosaicParameter.MOSAIC_ROWS;
		photoHashtable_ = new Hashtable<Integer, ArrayList<Photo>>();
		mosaicTiles_ = new Photo[MosaicParameter.MOSAIC_ROWS][MosaicParameter.MOSAIC_COLS];
		logger_.log(Level.FINER, "end.");
	}

	@Override
	public void run() {
		long start = System.currentTimeMillis();
		logger_.log(Level.FINER, "beg.");
		long preCalTime = populatePhotoList();
		splitMainPhoto();
		Mosaic mosaic = new Mosaic(mosaicTiles_, photoModelWidth_,
				photoModelHeight_);
		Photo photoMosaic = mosaic.generate();
		photoMosaic.createPhoto();
		logger_.log(Level.FINER, "end.");
		long end = System.currentTimeMillis();
		logger_.log(Level.FINE, "PreCalTime duration = " + preCalTime);
		logger_.log(Level.FINE, "Whole Thread duration = " + (end - start));
	}

	public long populatePhotoList() {
		long start = System.currentTimeMillis();
		logger_.log(Level.FINER, "beg");
		File[] listOfFiles = photoFolder_.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				if (isPhotoType(listOfFiles[i])) {
					logger_.finer("Analyzing Photo: "+listOfFiles[i].getAbsolutePath());
					Photo tmp = new Photo(listOfFiles[i].getAbsolutePath());
					int[] rgbVal = tmp.determineColorsAverage();
					float[] hsv = new float[3];
					Color.RGBtoHSB(rgbVal[0], rgbVal[1], rgbVal[2], hsv);
					populateHastables(tmp);
				}
			}
		}
		// Sort ArrayLists
		Enumeration<Integer> e = photoHashtable_.keys();
		while (e.hasMoreElements()) {
			Collections.sort(photoHashtable_.get(e.nextElement()));
		}
		logger_.log(Level.FINER, "Hashtable_ has " + photoHashtable_.size()
				+ " keys.");
		logger_.log(Level.FINER, "end");
		long end = System.currentTimeMillis();
		logger_.log(Level.FINE, "Precalculation duration = " + (end - start));
		return (end - start);
	}

	private void splitMainPhoto() {
		long start = System.currentTimeMillis();
		logger_.log(Level.FINER, "beg");
		for (int x = 0; x < MosaicParameter.MOSAIC_ROWS; x++) {
			for (int y = 0; y < MosaicParameter.MOSAIC_COLS; y++) {
				int[] chunkRgbAverage = calculateRGBAverage(x, y);
				float[] hsv = new float[3];
				Color.RGBtoHSB(chunkRgbAverage[0], chunkRgbAverage[1],
						chunkRgbAverage[2], hsv);
				mosaicTiles_[x][y] = getMinimumDistanceForPhoto(
						chunkRgbAverage, hsv[0]);
				logger_.log(Level.FINE,
						"MosaicTile["+ x + "][" + y + "] is "+ mosaicTiles_[x][y].getAbsolutePath());
				logger_.log(Level.FINER, "end");
			}
		}
		logger_.log(Level.FINER, "end.");
		long end = System.currentTimeMillis();
		logger_.log(Level.FINE, "Split Main Photo duration = " + (end - start));
	}

	private void initializeLogger() {
		logger_.setLevel(Level.FINE);
		try {
			FileHandler fileHandler = new FileHandler("mosaic.log");
			fileHandler.setFormatter(new SimpleFormatter());
			ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(new SimpleFormatter());
			logger_.addHandler(consoleHandler);
			logger_.addHandler(fileHandler);
			consoleHandler.setLevel(Level.FINE);
			fileHandler.setLevel(Level.ALL);
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isPhotoType(File file) {
		return file.getName().endsWith(".jpg")
				|| file.getName().endsWith(".jpeg")
				|| file.getName().endsWith(".png")
				|| file.getName().endsWith(".JPG");
	}

	private int[] calculateRGBAverage(int row, int col) {
		// Calculate better
		logger_.log(Level.FINEST,
				"Calculating the RGB average for chunk from pixel "
						+ (photoChunkWidth_ * row) + "/"
						+ (photoChunkHeight_ * col) + " to pixel "
						+ (photoChunkWidth_ * (row + 1)) + "/"
						+ (photoChunkHeight_ * (col + 1)));
		int[] rgb = new int[3];
		rgb[0] = 0;
		rgb[1] = 0;
		rgb[2] = 0;
		int pixelNum = photoChunkWidth_ * photoChunkHeight_;
		for (int i = row * photoChunkWidth_; i < ((row + 1) * photoChunkWidth_); i++) {
			for (int j = col * photoChunkHeight_; j < ((col + 1) * photoChunkHeight_); j++) {
				int rgbValue = photoModel_.getBufferedImage().getRGB(i, j);
				Color pixelColor = new Color(rgbValue);
				rgb[0] += pixelColor.getRed();
				rgb[1] += pixelColor.getGreen();
				rgb[2] += pixelColor.getBlue();
			}
		}
		rgb[0] = rgb[0] / pixelNum;
		rgb[1] = rgb[1] / pixelNum;
		rgb[2] = rgb[2] / pixelNum;
		return rgb;
	}

	private Photo getMinimumDistanceForPhoto(int[] chunksRGBValues, float hue) {
		// Binary search algo
		logger_.log(Level.FINER, "beg");
		int bucketNumber = getClosestAvailableBucket(hue);
		
		/*Photo[] photoArray = new Photo[photoHashtable_.get(bucketNumber).size()];
		logger_.finest("Bucket size is "+photoHashtable_.get(bucketNumber).size());
		Photo chosenPhoto = findBestMatch(chunksRGBValues, photoHashtable_.get(bucketNumber).toArray(photoArray));
		if(chosenPhoto == null) {
			if(photoHashtable_.get(bucketNumber - 1) != null) {
				photoArray = new Photo[photoHashtable_.get(bucketNumber-1).size()];
				chosenPhoto = findBestMatch(chunksRGBValues, photoHashtable_.get(bucketNumber-1).toArray(photoArray));
			} else if (photoHashtable_.get(bucketNumber + 1) != null) {
				photoArray = new Photo[photoHashtable_.get(bucketNumber+1).size()];
				chosenPhoto = findBestMatch(chunksRGBValues, photoHashtable_.get(bucketNumber+1).toArray(photoArray));
			}
		}*/
		
		Photo chosenPhoto = photoHashtable_.get(bucketNumber).get(0);
		chosenPhoto.determineColorsAverage();
		double minDist = calculateColorDistance(chosenPhoto, chunksRGBValues);
		for (int i = 1; i < photoHashtable_.get(bucketNumber).size(); i++) {
			logger_.log(Level.FINER, "Calculating for photo "
					+ photoHashtable_.get(bucketNumber).get(i).getName());
			photoHashtable_.get(bucketNumber).get(i).determineColorsAverage();
			double colourDistance = calculateColorDistance(
					photoHashtable_.get(bucketNumber).get(i), chunksRGBValues);
			logger_.log(Level.FINER, "colourDistance = " + colourDistance);
			if (minDist > colourDistance) {
				minDist = colourDistance;
				chosenPhoto = photoHashtable_.get(bucketNumber).get(i);
				logger_.log(Level.FINER,
						"New photo chosen: " + chosenPhoto.getName()
								+ " with Minimum Distance = " + minDist);
			}
		}
		if (photoHashtable_.get(bucketNumber - 1) != null) {
			for (int i = 0; i < photoHashtable_.get(bucketNumber - 1).size(); i++) {
				logger_.log(Level.FINER, "Calculating for photo "
						+ photoHashtable_.get(bucketNumber - 1).get(i)
								.getName());
				photoHashtable_.get(bucketNumber - 1).get(i)
						.determineColorsAverage();
				double colourDistance = calculateColorDistance(photoHashtable_
						.get(bucketNumber - 1).get(i), chunksRGBValues);
				logger_.log(Level.FINER, "colourDistance = " + colourDistance);
				if (minDist > colourDistance) {
					minDist = colourDistance;
					chosenPhoto = photoHashtable_.get(bucketNumber - 1).get(i);
					logger_.log(Level.FINER,
							"New photo chosen: " + chosenPhoto.getName()
									+ " with Minimum Distance = " + minDist);
				}
			}
		}
		if (photoHashtable_.get(bucketNumber + 1) != null) {
			for (int i = 0; i < photoHashtable_.get(bucketNumber + 1).size(); i++) {
				logger_.log(Level.FINER, "Calculating for photo "
						+ photoHashtable_.get(bucketNumber + 1).get(i)
								.getName());
				photoHashtable_.get(bucketNumber + 1).get(i)
						.determineColorsAverage();
				double colourDistance = calculateColorDistance(photoHashtable_
						.get(bucketNumber + 1).get(i), chunksRGBValues);
				logger_.log(Level.FINER, "colourDistance = " + colourDistance);
				if (minDist > colourDistance) {
					minDist = colourDistance;
					chosenPhoto = photoHashtable_.get(bucketNumber + 1).get(i);
					logger_.log(Level.FINER,
							"New photo chosen: " + chosenPhoto.getName()
									+ " with Minimum Distance = " + minDist);
				}
			}

		}
		logger_.log(Level.FINER,
				"Minimum distance photo is " + chosenPhoto.getAbsolutePath());
		logger_.log(Level.FINER, "end");
		return chosenPhoto;
	}

	private double calculateColorDistance(Photo photo, int[] chunksRGBValues) {
		if (chunksRGBValues.length != 3) {
			logger_.log(Level.WARNING,
					"Color distance calculation failed: Array lenght should be 3 not "
							+ chunksRGBValues.length);
			return -1;
		}
		long rmean = (photo.getAverageRedColor() + chunksRGBValues[0]) / 2;
		long r = photo.getAverageRedColor() - chunksRGBValues[0];
		long g = photo.getAverageGreenColor() - chunksRGBValues[1];
		long b = photo.getAverageBlueColor() - chunksRGBValues[2];
		return Math.sqrt((((512 + rmean) * r * r) >> 8) + 4 * g * g
				+ (((767 - rmean) * b * b) >> 8));
	}
	
	private Photo findBestMatch(int[] tileRGBValues, Photo[] photoList) {
		logger_.finest("photoList size is "+ photoList.length);
		if (photoList.length == 0) {
			logger_.log(Level.SEVERE, "Nothing in this list, try another one.");
			return null;
		} else if (photoList.length == 1) {
			return photoList[0];
		} else {
			Photo tileA = photoList[0];
			Photo tileB = findBestMatch(tileRGBValues, Arrays.copyOfRange(photoList, 1, photoList.length));
			logger_.finest("tile A found "+ tileA.getPhotoName()+" | tile B found "+ tileB.getPhotoName());
			if (closerColor(tileRGBValues, tileA.determineColorsAverage(), tileB.determineColorsAverage())) {
				return tileA;
			} else {
				return tileB;
			}
		}
	}
	
	private boolean closerColor(int[] rgbValues, int[] tileA, int[] tileB) {
		logger_.log(Level.FINEST, "Comparing ["+tileA[0]+", "+tileA[1]+", "+tileA[2]+"] to ["+tileB[0]+", "+tileB[1]+", "+tileB[2]+"].");
		int tileAdiff = Math.abs(rgbValues[0] - tileA[0]) + Math.abs(rgbValues[1] - tileA[1]) + Math.abs(rgbValues[2] - tileA[2]);
		int tileBdiff = Math.abs(rgbValues[0] - tileB[0]) + Math.abs(rgbValues[1] - tileB[1]) + Math.abs(rgbValues[2] - tileB[2]);
		if (tileAdiff <= tileBdiff) {
			logger_.finest("Tile A chosen.");
			return true;
		} else {
			logger_.finest("Tile B chosen.");
			return false;
		}
	}

	private void populateHastables(Photo photo) {
		logger_.log(Level.FINER, "beg");
		logger_.log(Level.FINEST, "Hue value: " + photo.getHue());
		int bucketNumber = (int) ((MosaicParameter.NUMBER_OF_HUE_BUCKET * photo.getHue()) / MosaicParameter.MAX_HUE_VALUE);
		if (photoHashtable_.containsKey(bucketNumber)) {
			ArrayList<Photo> photoList = (ArrayList<Photo>) photoHashtable_
					.get(bucketNumber);
			photoList.add(photo);
		} else {
			ArrayList<Photo> photoList = new ArrayList<Photo>();
			photoHashtable_.put(bucketNumber, photoList);
			logger_.log(Level.FINEST, "ArrayList was created for bucket: "
					+ bucketNumber);
			photoList.add(photo);
		}
		logger_.log(Level.FINE, "Photo " + photo.getName()
				+ " has been added to bucket: " + bucketNumber);
		logger_.log(Level.FINER, "end");
	}

	private int getClosestAvailableBucket(float hue) {
		int bucketNumber = (int) ((MosaicParameter.NUMBER_OF_HUE_BUCKET * hue) / MosaicParameter.MAX_HUE_VALUE);
		if (photoHashtable_.get(bucketNumber) != null
				&& photoHashtable_.get(bucketNumber).get(0) != null) {
			logger_.log(Level.FINER, "Bucket Number: " + bucketNumber);
			return bucketNumber;
		}
		for (int i = 1; i < MosaicParameter.NUMBER_OF_HUE_BUCKET; i++) {
			if (photoHashtable_.get(bucketNumber + i) != null
					&& photoHashtable_.get(bucketNumber + i).get(0) != null) {
				logger_.log(Level.FINER, "Bucket Number: " + (bucketNumber + i));
				return bucketNumber + i;
			}
			if (photoHashtable_.get(bucketNumber - i) != null
					&& photoHashtable_.get(bucketNumber - i).get(0) != null) {
				logger_.log(Level.FINER, "Bucket Number: " + (bucketNumber - i));
				return bucketNumber - i;
			}
		}
		return -1;
	}
}
