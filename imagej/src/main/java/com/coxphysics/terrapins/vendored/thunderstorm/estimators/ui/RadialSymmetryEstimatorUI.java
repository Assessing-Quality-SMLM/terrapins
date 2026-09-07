package com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui;

import com.coxphysics.terrapins.vendored.thunderstorm.estimators.IEstimator;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.MultipleLocationsImageFitting;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.RadialSymmetryFitter;
import com.coxphysics.terrapins.vendored.thunderstorm.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RadialSymmetryEstimatorUI extends IEstimatorUI {

    private final String name = "Radial symmetry";
    private int fittingRadius;
    private transient ParameterKey.Integer FITRAD;

    public RadialSymmetryEstimatorUI() {
        FITRAD = parameters.createIntField("fitradius", IntegerValidatorFactory.positiveNonZero(), 5);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        JTextField fitregsizeTextField = new JTextField("", 20);
        parameters.registerComponent(FITRAD, fitregsizeTextField);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Estimation radius [px]:"), GridBagHelper.leftCol());
        panel.add(fitregsizeTextField, GridBagHelper.rightCol());

        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IEstimator getImplementation() {
        return new MultipleLocationsImageFitting(fittingRadius = FITRAD.getValue(), new RadialSymmetryFitter());
    }
}
