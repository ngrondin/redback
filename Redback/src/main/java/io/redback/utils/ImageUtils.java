package io.redback.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

import io.redback.exceptions.RedbackException;

public class ImageUtils {

	public static String getBase64ThumbnailOfImage(byte[] bytes) throws RedbackException 
	{
		String b64img = null;
		try
		{
			int orientation = getOrientation(bytes);
			BufferedImage orig = getImage(bytes, -1, 80, orientation);
			return getBase64Thumbnail(orig);
		}
		catch(Exception e) 
		{
		}
		return b64img;
	}
	
	public static String getBase64ThumbnailOfPDF(byte[] bytes) throws RedbackException
	{
		try {
			PDDocument doc = PDDocument.load(new ByteArrayInputStream(bytes));
		    PDFRenderer renderer = new PDFRenderer(doc);
		    BufferedImage img = renderer.renderImage(0, 0.1f);
		    img = getImage(img, -1, 80, 1);
		    return getBase64Thumbnail(img);
		} catch(Exception e) {
			throw new RedbackException("Error reading PDF", e);
		}
	}
	
	public static String getBase64Thumbnail(BufferedImage orig) throws RedbackException
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(orig, "png", baos);
			return ("data:image/png;base64, " + (new String(Base64.getEncoder().encode(baos.toByteArray()), "UTF-8")));
		}
		catch(Exception e) 
		{
			throw new RedbackException("Error creating thumbnail", e);
		}
	}
	
	public static int getOrientation(byte[] bytes) throws RedbackException {
		int orientation = 1;
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(bytes));
	        for (Directory directory : metadata.getDirectories())  {
	        	if(directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION))
	        		orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
	        }
		} catch(Exception e) {
			throw new RedbackException("Error getting image orientation", e);
		}
	    return orientation;
	}

	public static BufferedImage getImage(byte[] bytes) throws RedbackException {
		try {
		    return ImageIO.read(new ByteArrayInputStream(bytes));
		} catch(Exception e) {
			throw new RedbackException("Error getting image", e);
		}
	}
	
	public static BufferedImage getImage(byte[] bytes, int w, int h, int r) throws RedbackException {
		try {
		    BufferedImage origImg = getImage(bytes);
		    return getImage(origImg, w, h, r);
		} catch(Exception e) {
			throw new RedbackException("Error getting image", e);
		}
	}
	
	public static BufferedImage getImage(BufferedImage origImg, int w, int h, int r) throws RedbackException {
		try {
			int ow = origImg.getWidth();
			int oh = origImg.getHeight();
			int nw = w;
			int nh = h;
			double scalex = 1.0;
			double scaley = 1.0;
			if(nw != ow && nh != oh) {
				if(nw == -1 && nh > 0) {
					scaley = (double)nh / (double)oh;
					scalex = scaley;
					nw = (int)(scalex * (double)ow);
				} else if(nh == -1 && nw > 0) {
					scalex = (double)nw / (double)ow;
					scaley = scalex;
					nh = (int)(scaley * (double)oh);
				} else if(nh == -1 && nw == -1) {
					scalex = 1.0;
					scaley = 1.0;
					nw = ow;
					nh = oh;
				} else {
					scalex = (double)nw / (double)ow;
					scaley = (double)nh / (double)oh;
				}
			}
			if(r != 1 || scalex != 1.0 || scaley != 1.0) {
			    double rotate = 0.0;
			    double tx = 0.0;
			    double ty = 0.0;
			    int temp = 0;
			    switch (r) {
				    case 1: 
				        break;
				    case 2: // Flip X
				        scalex *= -1.0;
				        tx = nw;
				        break;
				    case 3: // PI rotation
				    	rotate = Math.PI;
				    	tx = nw;
				    	ty = nh;
				        break;
				    case 4: // Flip Y
				    	scaley *= -1.0;
				    	ty = nh;
				        break;
				    case 5: // -PI/2 and Flip X
				    	scalex *= 1.0;
				    	rotate = Math.PI / 2;
				    	temp = nw;
				    	nw = nh;
				    	nh = temp;
				    	tx = nw;
				    	ty = nh;
				        break;
				    case 6: // -PI/2 
				    	rotate = Math.PI / 2;
				    	temp = nw;
				    	nw = nh;
				    	nh = temp;
				    	tx = nw;
				        break;
				    case 7: // PI/2 and Flip
				    	scalex *= -1.0;
				    	rotate = -Math.PI / 2;
				    	temp = nw;
				    	nw = nh;
				    	nh = temp;
				        break;
				    case 8: // PI / 2
				    	rotate = -Math.PI / 2;
				    	temp = nw;
				    	nw = nh;
				    	nh = temp;
				    	ty = nh;
				        break;
			    }
			    AffineTransform all = new AffineTransform();
			    all.concatenate(AffineTransform.getTranslateInstance(tx, ty));
			    all.concatenate(AffineTransform.getRotateInstance(rotate));
			    all.concatenate(AffineTransform.getScaleInstance(scalex, scaley));
			    AffineTransformOp op = new AffineTransformOp(all, AffineTransformOp.TYPE_BILINEAR);
			    BufferedImage destImage = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
			    Graphics2D g = destImage.createGraphics();
			    g.setBackground(Color.WHITE);
			    g.clearRect(0, 0, destImage.getWidth(), destImage.getHeight());
			    destImage = op.filter(origImg, destImage);
			    return destImage;
			} else {
				return origImg;
			}
		} catch(Exception e) {
			throw new RedbackException("Error getting image", e);
		}
	}
	
	public static byte[] getBytes(BufferedImage image, String type) throws RedbackException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, type, baos);
			return baos.toByteArray();
		} catch(Exception e) {
			throw new RedbackException("Error getting image bytes", e); 
		}
	}
		

}
