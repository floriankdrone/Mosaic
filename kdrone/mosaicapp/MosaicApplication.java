package kdrone.mosaicapp;

import kdrone.mosaicapp.photo.Photo;
import kdrone.mosaicapp.MosaicParameter;

public class MosaicApplication {
	public static void main(String[] args) {
		new MosaicThread(photo).run();
	}
}