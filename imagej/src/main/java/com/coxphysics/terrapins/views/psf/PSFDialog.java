package com.coxphysics.terrapins.views.psf;

import com.coxphysics.terrapins.models.psf.PsfCalculator;
import ij.gui.GenericDialog;

//Dialogue for calculating the PSF size in SR pixels
public class PSFDialog extends GenericDialog {

    private PSFDialog() {
        super("PSF calculator");
    }
    public static PSFDialog create()
    {
        PSFDialog dialog = new PSFDialog();
        dialog.addNumericField("Instrument PSF FWHM (nm)", 270, 0);
        dialog.addNumericField("Camera pixel size (nm)", 160, 0);
        dialog.addNumericField("Reconstruction Magnification factor ", 10, 0);
        return dialog;
    }

    public double calculated_psf()
    {
        return to_settings().calculate_psf();
    }

    public PsfCalculator to_settings()
    {
        PsfCalculator settings = PsfCalculator.default_();

        double instrument_psf = this.getNextNumber();
        settings.set_instrument_psf_fwhm_nm(instrument_psf);

        double camera_pixel_size = this.getNextNumber();
        settings.set_camera_pixel_size_nm_(camera_pixel_size);

        double recon_mag_factor = this.getNextNumber();
        settings.set_reconstruction_magnification_factor(recon_mag_factor);

        return settings;
    }
}
