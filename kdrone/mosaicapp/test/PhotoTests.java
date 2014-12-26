package kdrone.mosaicapp.test;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import kdrone.mosaicapp.photo.Photo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PhotoTests {

	private File photoFile;
	private FileInputStream fis;
	private BufferedImage img;
	private Photo photo;
	
	@Before
	public void setUp() throws Exception {
		photoFile = new File("index2.jpg");
		try {
			fis = new FileInputStream(photoFile);
			img = ImageIO.read(fis);
			photo = new Photo(photoFile.getName(), img);
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void checkPhotoWithoutOutputFolderIsCreated() {
		photo.createPhoto();
		assertTrue(photo.exists());
	}

}
