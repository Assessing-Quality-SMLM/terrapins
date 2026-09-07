package com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui;

import com.coxphysics.terrapins.vendored.thunderstorm.estimators.IEstimator;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.CentroidFitter;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.MultipleLocationsImageFitting;
import com.coxphysics.terrapins.vendored.thunderstorm.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CenterOfMassEstimatorUI extends IEstimatorUI {

    private final String name = "Centroid of local neighborhood";
    private int fittingRadius;
    private transient ParameterKey.Integer FITRAD;

    public CenterOfMassEstimatorUI() {
        FITRAD = parameters.createIntField("fitradius", IntegerValidatorFactory.positiveNonZero(), 3);
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
        return new MultipleLocationsImageFitting(fittingRadius = FITRAD.getValue(), new CentroidFitter());
    }
}
