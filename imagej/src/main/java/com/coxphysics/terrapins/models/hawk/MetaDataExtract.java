package com.coxphysics.terrapins.models.hawk;


import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.gui.GenericDialog;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;


public class MetaDataExtract implements PlugIn {

	private String get_metadata()
	{
		ImagePlus imp = WindowManager.getCurrentImage();
		return get_metadata(imp);
	}

	public static String get_metadata(ImagePlus image)
	{
		if (image == null)
			return null;
        return image.getProp("hawk_metadata");
	}

	private void copy_to_clipboard(String message)
	{
		StringSelection stringSelection = new StringSelection(message);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	@Override
	public void run(String args)
	{
		String metadata = get_metadata();
		if (metadata == null)
			return;
		GenericDialog dialog = new GenericDialog("HAWK metadata");
		dialog.addMessage(metadata);
		copy_to_clipboard(metadata);
		dialog.showDialog();
	}
}
