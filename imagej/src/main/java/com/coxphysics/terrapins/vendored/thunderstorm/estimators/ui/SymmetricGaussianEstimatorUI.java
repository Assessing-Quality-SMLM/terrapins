package com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui;

import com.coxphysics.terrapins.vendored.thunderstorm.estimators.FullImageFitting;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.LSQFitter;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.IEstimator;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.MLEFitter;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.MLEFitterLM;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.MultipleLocationsImageFitting;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.IOneLocationFitter;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.PSFModel.Params;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.SymmetricGaussianPSF;
import com.coxphysics.terrapins.vendored.thunderstorm.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.StringValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SymmetricGaussianEstimatorUI extends IEstimatorUI {

    public transient static final String MLE = "Maximum likelihood";
    public transient static final String MLE_FAST = "Maximum likelihood (fast, experimental)";
    public transient static final String LSQ = "Least squares";
    public transient static final String WLSQ = "Weighted Least squares";
    //
    protected String name = "PSF: Gaussian";
    protected int fittingRadius;
    protected String method;
    protected double initialSigma;
    protected boolean fullImageFitting;
    protected CrowdedFieldEstimatorUI crowdedField;
    //params
    protected transient ParameterKey.Integer FITRAD;
    protected transient ParameterKey.String METHOD;
    protected transient ParameterKey.Double SIGMA;
    protected transient ParameterKey.Boolean FULL_IMAGE_FITTING;

    public SymmetricGaussianEstimatorUI() {
        crowdedField = new CrowdedFieldEstimatorUI();
        FITRAD = parameters.createIntField("fitradius", IntegerValidatorFactory.positiveNonZero(), 3);
        METHOD = parameters.createStringField("method", StringValidatorFactory.isMember(new String[]{MLE, MLE_FAST, LSQ, WLSQ}), MLE);
        SIGMA = parameters.createDoubleField("sigma", DoubleValidatorFactory.positiveNonZero(), 1.6);
        FULL_IMAGE_FITTING = parameters.createBooleanField("full_image_fitting", null, false);
    }

    @Override
    public String getName() {
        return name;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public JPanel getOptionsPanel() {
        JTextField fitregsizeTextField = new JTextField("", 20);
        JComboBox<String> methodComboBox = new JComboBox<String>(new String[]{LSQ, WLSQ, MLE, MLE_FAST});
        JTextField sigmaTextField = new JTextField("");
        parameters.registerComponent(FITRAD, fitregsizeTextField);
        parameters.registerComponent(METHOD, methodComboBox);
        parameters.registerComponent(SIGMA, sigmaTextField);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Fitting radius [px]:"), GridBagHelper.leftCol());
        panel.add(fitregsizeTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Fitting method:"), GridBagHelper.leftCol());
        panel.add(methodComboBox, GridBagHelper.rightCol());
        panel.add(new JLabel("Initial sigma [px]:"), GridBagHelper.leftCol());
        panel.add(sigmaTextField, GridBagHelper.rightCol());
        crowdedField.getOptionsPanel(panel);

        parameters.loadPrefs();
        return panel;
    }

    @Override
    public void readParameters() {
        super.readParameters();
        crowdedField.readParameters();
    }

    @Override
    public IEstimator getImplementation() {
        method = METHOD.getValue();
        initialSigma = SIGMA.getValue();
        fittingRadius = FITRAD.getValue();
        fullImageFitting = FULL_IMAGE_FITTING.getValue();
        SymmetricGaussianPSF psf = new SymmetricGaussianPSF(initialSigma);
        IOneLocationFitter fitter;
        if(LSQ.equals(method) || WLSQ.equals(method)) {
            if(crowdedField.isEnabled()) {
                fitter = crowdedField.getLSQImplementation(psf, initialSigma);
            } else {
                fitter = new LSQFitter(psf, WLSQ.equals(method), Params.BACKGROUND);
            }
        } else if(MLE.equals(method)) {
            if(crowdedField.isEnabled()) {
                fitter = crowdedField.getMLEImplementation(psf, initialSigma);
            } else {
                fitter = new MLEFitter(psf, Params.BACKGROUND);
            }
        } else if(MLE_FAST.equals(method)) {
            if(crowdedField.isEnabled()) {
                // MLEFitterLM isn't wired into multi-emitter model selection (MFA_*Fitter) yet;
                // crowded-field mode keeps using the Nelder-Mead MLEFitter regardless of this choice.
                fitter = crowdedField.getMLEImplementation(psf, initialSigma);
            } else {
                fitter = new MLEFitterLM(psf, Params.BACKGROUND);
            }
        } else {
            throw new IllegalArgumentException("Unknown fitting method: " + method);
        }
        if(fullImageFitting) {
            return new FullImageFitting(fitter);
        } else {
            return new MultipleLocationsImageFitting(fittingRadius, fitter);
        }
    }

    @Override
    public void recordOptions() {
        super.recordOptions();
        crowdedField.recordOptions();
    }

    @Override
    public void readMacroOptions(String options) {
        super.readMacroOptions(options);
        crowdedField.readMacroOptions(options);
    }

    @Override
    public void resetToDefaults() {
        super.resetToDefaults();
        crowdedField.resetToDefaults();
    }
}
