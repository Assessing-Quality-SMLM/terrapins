package com.coxphysics.terrapins.views.TERRAPINS;

import com.coxphysics.terrapins.models.utils.ActionableListener;
import com.coxphysics.terrapins.view_models.TERRAPINS.TERRAPINSVM;
import com.coxphysics.terrapins.views.oneclick.OneClickView;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ij.IJ;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.Semaphore;

class CloseListener implements WindowListener
{
    private TERRAPINSTabView view_;

    public CloseListener(TERRAPINSTabView view)
    {
        view_ = view;
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        view_.release_semaphore();

    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
public class TERRAPINSTabView extends JFrame {
    private JPanel root_;
    private JTabbedPane tab_pane_;
    private JPanel pre_processing_pane_;
    private JPanel localisation_panel_;
    private JPanel images_panel_;
    private LocalisationView localisations_ctrl_;
    private ImagesView images_ctrl_;
    private PathSelectorView settings_view_;
    private AuxillarySettingsView localisation_equipment_view_;
    private AuxillarySettingsView images_equipment_view_;
    private JButton localistations_run_btn_;
    private JButton images_run_btn_;
    private SquirrelInputs localisations_squirrel_inputs_view_;
    private SquirrelInputs images_squirrel_inputs_view_;
    private PathSelectorView working_directory_view_;
    private JScrollPane scroll_pane_;
    private PreProcessingView pre_processing_view_;
    private OneClickView one_click_view_;

    private boolean cancelled_ = true;

    private Semaphore semaphore_ = new Semaphore(0);

    private TERRAPINSVM view_model_;

    private TERRAPINSTabView()
    {
        super("TERRAPINS");
        add(root_);
        localistations_run_btn_.addActionListener(ActionableListener.from(this, TERRAPINSTabView::run_localisations));
        images_run_btn_.addActionListener(ActionableListener.from(this, TERRAPINSTabView::run_images));
        promote_one_click();
        this.addWindowListener(new CloseListener(this));
    }

    /**
     * Makes one-click the front door and moves the existing workflows behind "Advanced".
     *
     * Done here rather than in {@code $$$setupUI$$$} on purpose. That method is regenerated
     * wholesale whenever someone opens the .form in the GUI designer and saves it, so anything
     * written into it by hand is one careless save away from being lost. Restructuring afterwards
     * survives regeneration: the designer keeps owning the panels it built, and this owns only
     * their arrangement.
     *
     * The tabs the designer produced are, in order: Pre-Processing, Localisation Workflow, Images
     * Workflow, Advanced. All four are still reachable - nothing is removed - but the two that
     * require a localisation table the user had to produce elsewhere are no longer the first
     * thing a new user meets.
     */
    private void promote_one_click()
    {
        JTabbedPane advanced = new JTabbedPane();
        // Moving a tab removes it, so index 0 repeatedly rather than counting up.
        while (tab_pane_.getTabCount() > 0)
        {
            String title = tab_pane_.getTitleAt(0);
            Component page = tab_pane_.getComponentAt(0);
            tab_pane_.removeTabAt(0);
            advanced.addTab(title, page);
        }

        one_click_view_ = new OneClickView();
        JPanel one_click_panel = new JPanel(new BorderLayout());
        one_click_panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        one_click_panel.add(one_click_view_, BorderLayout.NORTH);

        tab_pane_.addTab("One Click", one_click_panel);
        tab_pane_.addTab("Advanced", advanced);
        tab_pane_.setSelectedIndex(0);
    }

    public static TERRAPINSTabView from(TERRAPINSVM view_model) {
        TERRAPINSTabView view = new TERRAPINSTabView();
        view.set_view_model(view_model);
        return view;
    }

    private void set_view_model(TERRAPINSVM view_model) {
        view_model_ = view_model;
        one_click_view_.set_view_model(view_model_.one_click_vm());
        localisations_squirrel_inputs_view_.set_view_model(view_model_.localisation_squirrel_inputs_vm());
        localisation_equipment_view_.set_view_model(view_model_.localisation_equipment_settings_vm());
        localisations_ctrl_.set_view_model(view_model_.localisation_vm());
        images_equipment_view_.set_view_model(view_model_.images_equipment_settings_vm());
        images_squirrel_inputs_view_.set_view_model(view_model.images_squirrel_inputs_vm());
        images_ctrl_.set_view_model(view_model_.images_vm());
        working_directory_view_.set_view_model(view_model.working_directory_vm());
        settings_view_.set_view_model(view_model_.settings_vm());
    }

    private void run_localisations() {
        view_model_.run_localisations();
    }

    private void run_images() {
        view_model_.run_images();
    }

    public boolean was_canceled() {
        try {
            semaphore_.acquire();
        } catch (InterruptedException ex) {
            IJ.handleException(ex);
        }
        return cancelled_;
    }

    @Override
    public void dispose()
    {
        super.dispose();
        release_semaphore();
    }

    public void release_semaphore()
    {
        semaphore_.release();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        root_ = new JPanel();
        root_.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        scroll_pane_ = new JScrollPane();
        root_.add(scroll_pane_, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tab_pane_ = new JTabbedPane();
        scroll_pane_.setViewportView(tab_pane_);
        pre_processing_pane_ = new JPanel();
        pre_processing_pane_.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        tab_pane_.addTab("Pre-Processing", pre_processing_pane_);
        pre_processing_view_ = new PreProcessingView();
        pre_processing_pane_.add(pre_processing_view_.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        localisation_panel_ = new JPanel();
        localisation_panel_.setLayout(new GridLayoutManager(5, 1, new Insets(5, 5, 5, 5), -1, -1));
        tab_pane_.addTab("Localisation Workflow", localisation_panel_);
        localisation_equipment_view_ = new AuxillarySettingsView();
        localisation_panel_.add(localisation_equipment_view_.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        localisation_panel_.add(spacer1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        localistations_run_btn_ = new JButton();
        localistations_run_btn_.setText("Run");
        localisation_panel_.add(localistations_run_btn_, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        localisations_squirrel_inputs_view_ = new SquirrelInputs();
        localisation_panel_.add(localisations_squirrel_inputs_view_.$$$getRootComponent$$$(), new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        localisations_ctrl_ = new LocalisationView();
        localisation_panel_.add(localisations_ctrl_.$$$getRootComponent$$$(), new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        images_panel_ = new JPanel();
        images_panel_.setLayout(new GridLayoutManager(5, 1, new Insets(5, 5, 5, 5), -1, -1));
        tab_pane_.addTab("Images Workflow", images_panel_);
        images_ctrl_ = new ImagesView();
        images_panel_.add(images_ctrl_.$$$getRootComponent$$$(), new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        images_panel_.add(spacer2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        images_equipment_view_ = new AuxillarySettingsView();
        images_panel_.add(images_equipment_view_.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        images_run_btn_ = new JButton();
        images_run_btn_.setText("Run");
        images_panel_.add(images_run_btn_, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        images_squirrel_inputs_view_ = new SquirrelInputs();
        images_panel_.add(images_squirrel_inputs_view_.$$$getRootComponent$$$(), new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        tab_pane_.addTab("Advanced", panel1);
        settings_view_ = new PathSelectorView();
        panel1.add(settings_view_.$$$getRootComponent$$$(), new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        working_directory_view_ = new PathSelectorView();
        panel1.add(working_directory_view_.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return root_;
    }

}
