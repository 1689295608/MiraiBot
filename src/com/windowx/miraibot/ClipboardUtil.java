package com.windowx.miraibot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ClipboardUtil {
	
	public static byte[] getImageFromClipboard() throws Exception {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable cc = clip.getContents(null);
		if (cc == null) {
			return null;
		} else if (cc.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				Image image = (Image) cc.getTransferData(DataFlavor.imageFlavor);
				ImageIO.write((RenderedImage) image, "jpg", out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return out.toByteArray();
		}
		return null;
	}
}
