package com.coxphysics.terrapins.vendored.thunderstorm.results;

import com.coxphysics.terrapins.vendored.thunderstorm.ImportExportPlugIn;
import com.coxphysics.terrapins.vendored.thunderstorm.ModuleLoader;
import com.coxphysics.terrapins.vendored.thunderstorm.UI.MacroParser;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.Molecule;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.MoleculeDescriptor;
import static com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.MoleculeDescriptor.LABEL_DETECTIONS;
import static com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.PSFModel.Params.LABEL_X;
import static com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.PSFModel.Params.LABEL_Y;
import com.coxphysics.terrapins.vendored.thunderstorm.rendering.IncrementalRenderingMethod;
import com.coxphysics.terrapins.vendored.thunderstorm.rendering.RenderingQueue;
import com.coxphysics.terrapins.vendored.thunderstorm.rendering.ui.ASHRenderingUI;
import com.coxphysics.terrapins.vendored.thunderstorm.rendering.ui.IRendererUI;
import com.coxphysics.terrapins.vendored.thunderstorm.util.PluginCommands;
import com.coxphysics.terrapins.vendored.thunderstorm.util.VectorMath;
import ij.IJ;
import ij.ImagePlus;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ResultsTableWindow extends GenericTableWindow {

    private JButton io_import;
    private JButton io_export;
    private JButton showHist;
    private JButton render;
    private JButton defaultsButton;
    private JCheckBox preview;
    private JLabel status;
    private RenderingQueue previewRenderer;
    private boolean livePreview;
    private JButton resetButton;
    private JTabbedPane tabbedPane;
    private OperationsHistoryPanel operationsStackPanel;
    List<? extends PostProcessingModule> postProcessingModules;

    public ResultsTableWindow(String frameTitle) {
        super(frameTitle);
    }

    @Override
    protected void packFrame() {
        frame.setPreferredSize(new Dimension(600, 750));
        //
        status = new JLabel(" ");
        status.setAlignmentX(Component.CENTER_ALIGNMENT);
        status.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        //buttons
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        defaultsButton = new JButton("Defaults");
        showHist = new JButton("Plot histogram");
        io_import = new JButton("Import");
        io_export = new JButton("Export");
        render = new JButton("Visualization");
        defaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(PostProcessingModule module : postProcessingModules){
                    module.resetParamsToDefaults();
                }
            }
        });
        showHist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new IJDistribution().run(IJResultsTable.IDENTIFIER);
            }
        });
        io_import.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MacroParser.runNestedWithRecording(PluginCommands.IMPORT_RESULTS.getValue(), null);
            }
        });
        io_export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MacroParser.runNestedWithRecording(PluginCommands.EXPORT_RESULTS.getValue(), null);
            }
        });
        render.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MacroParser.runNestedWithRecording(PluginCommands.RENDERING.getValue(), null);
            }
        });
        livePreview = false;
        preview = new JCheckBox("Preview", livePreview);
        preview.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                livePreview = (e.getStateChange() == ItemEvent.SELECTED);
                showPreview();
            }
        });
        buttons.add(preview);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(defaultsButton);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(showHist);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(render);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(io_import);
        buttons.add(Box.createHorizontalStrut(3));
        buttons.add(io_export);
        //
        postProcessingModules = ModuleLoader.getPostProcessingModules();

        //fill tabbed pane
        // Each module's real panel (GridBagLayouts, BalloonTips, DropTargets, ...) is built lazily,
        // the first time its tab is actually selected, rather than eagerly for all modules right here.
        // Building all of them synchronously on the EDT as part of this constructor -- which runs
        // right after the results table (and, in the common case, ImageJ itself) has just started up --
        // was observed to matter: an unresponsive-for-too-long top-level window can make some window
        // managers conclude the app has hung and send it a close request, which races ImageJ's own
        // quit-handling thread against whatever this constructor is still doing on the EDT. Only the
        // tab a user actually looks at needs to exist; PostProcessingModule#run() (macro or button
        // click) separately guarantees its own module's panel gets built before it's needed.
        tabbedPane = new JTabbedPane();
        for(PostProcessingModule module : postProcessingModules) {
            module.setModel(model);
            module.setTable(this);
            tabbedPane.addTab(module.getTabName(), new JPanel());
        }
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int idx = tabbedPane.getSelectedIndex();
                if(idx < 0) {
                    return;
                }
                JPanel realPanel = postProcessingModules.get(idx).getUIPanel();
                if(tabbedPane.getComponentAt(idx) != realPanel) {
                    tabbedPane.setComponentAt(idx, realPanel);
                }
            }
        });
        // JTabbedPane selects tab 0 as soon as the first tab is added, before the listener above
        // existed to see that selection change -- build that one tab's real panel explicitly so a
        // freshly-opened window doesn't show a blank first tab.
        if(tabbedPane.getTabCount() > 0) {
            tabbedPane.setComponentAt(0, postProcessingModules.get(0).getUIPanel());
        }

        //history pane
        JPanel historyPane = new JPanel(new GridBagLayout());
        operationsStackPanel = new OperationsHistoryPanel();
        resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.copyOriginalToActual();
                model.convertAllColumnsToAnalogUnits();
                operationsStackPanel.removeAllOperations();
                setStatus("Results reset.");
                showPreview();
                TableHandlerPlugin.recordReset();
            }
        });
        historyPane.add(operationsStackPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        historyPane.add(resetButton);

        Container contentPane = frame.getContentPane();
        JPanel controlsPane = new JPanel();
        controlsPane.setLayout(new BoxLayout(controlsPane, BoxLayout.PAGE_AXIS));

        contentPane.add(tableScrollPane, BorderLayout.CENTER);
        contentPane.add(controlsPane, BorderLayout.SOUTH);

        controlsPane.add(tabbedPane);
        controlsPane.add(historyPane);
        controlsPane.add(new JSeparator(SwingConstants.HORIZONTAL));
        controlsPane.add(buttons);
        controlsPane.add(new JSeparator(SwingConstants.HORIZONTAL));
        controlsPane.add(status);
        //
        frame.setContentPane(contentPane);
        frame.pack();
    }

    public void setLivePreview(boolean enabled) {
        livePreview = enabled;
        preview.setSelected(enabled);
    }

    public void showPreview() {
        IJResultsTable rt = IJResultsTable.getResultsTable();
        if(livePreview && !rt.isEmpty()) {
            if(!rt.columnExists(LABEL_X) || !rt.columnExists(LABEL_Y)) {
                IJ.error(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", LABEL_X, LABEL_Y, rt.getColumnNames()));
                return;
            }
            if(previewRenderer == null) {
                IRendererUI renderer = new ASHRenderingUI();
                ImagePlus analyzedImage = rt.getAnalyzedImage();
                if(analyzedImage != null) {
                    renderer.setSize(analyzedImage.getWidth(), analyzedImage.getHeight());
                } else {
                    double[] xpos = rt.getColumnAsDoubles(LABEL_X, MoleculeDescriptor.Units.PIXEL);
                    double[] ypos = rt.getColumnAsDoubles(LABEL_Y, MoleculeDescriptor.Units.PIXEL);
                    int left = Math.max((int) Math.floor(VectorMath.min(xpos)) - 1, 0);
                    int top = Math.max((int) Math.floor(VectorMath.min(ypos)) - 1, 0);
                    int right = (int) Math.ceil(VectorMath.max(xpos)) + 1;
                    int bottom = (int) Math.ceil(VectorMath.max(ypos)) + 1;
                    renderer.setSize(left, top, right - left + 1, bottom - top + 1);
                }
                IncrementalRenderingMethod rendererImplementation = renderer.getImplementation();
                previewRenderer = new RenderingQueue(rendererImplementation,
                        new RenderingQueue.DefaultRepaintTask(rendererImplementation.getRenderedImage()),
                        renderer.getRepaintFrequency());
            }
            //
            previewRenderer.resetLater();
            previewRenderer.renderLater(rt.getData());
            previewRenderer.repaintLater();
            // Without this, showPreview() returns as soon as the above are merely queued, not
            // once they've actually run. Every post-processing module (filter, grouping, drift
            // correction, ...) calls showPreview() to refresh the live preview after modifying the
            // table, and WorkerThread already makes that modification itself block synchronously
            // when run from a macro -- but the preview re-render it triggers here is a separate,
            // still-asynchronous RenderingQueue job. A macro doing
            // `run("Show results table", "action=filter ..."); saveAs(...);` would otherwise race
            // this re-render exactly like AnalysisPlugIn#showResults did (see there for the
            // original instance of this same bug).
            previewRenderer.waitForFinished();
        }
        rt.repaintAnalyzedImageOverlay();
    }

    public void setPreviewRenderer(RenderingQueue renderer) {
        previewRenderer = renderer;
        livePreview = (renderer != null);
        preview.setSelected(livePreview);
    }

    public ImagePlus getPreviewImage() {
        if(!livePreview || previewRenderer == null) {
            return null;
        }
        return previewRenderer.method.getRenderedImage();
    }

    public void setStatus(String text) {
        if(text == null) {
            text = " ";
        }
        this.status.setText(text);
    }

    public ResultsFilter getFilter() {
        if(postProcessingModules != null) {
            for(PostProcessingModule module : postProcessingModules) {
                if(module instanceof ResultsFilter) {
                    return (ResultsFilter) module;
                }
            }
        }
        return null;
    }

    public OperationsHistoryPanel getOperationHistoryPanel() {
        return operationsStackPanel;
    }

    public List<? extends PostProcessingModule> getPostProcessingModules() {
        return postProcessingModules;
    }

    @Override
    protected void tableMouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e)) {
            if(e.getClickCount() == 2) {
                IJResultsTable rt = IJResultsTable.getResultsTable();
                int row = table.getSelectedRow();
                int rowIndex = rt.convertViewRowIndexToModel(row);
                Molecule mol = rt.getRow(rowIndex);
                if(mol.hasParam(LABEL_DETECTIONS)) {
                    if(mol.getParam(LABEL_DETECTIONS) > 1) {
                        List<Molecule> detections = mol.getDetections();
                        Collections.sort(detections);
                        new MergedMoleculesPopUp(table, row, 0, detections);
                    }
                }
            }
        } else if(SwingUtilities.isRightMouseButton(e)) {
            if(table.getSelectedRowCount() > 0) {
                new TableRowsPopUpMenu(e, this);
            }
        }
    }

    @Override
    protected void tableMouseMoved(MouseEvent e) {
        IJResultsTable rt = IJResultsTable.getResultsTable();
        int rowIndex = rt.convertViewRowIndexToModel(table.rowAtPoint(e.getPoint()));
        Molecule mol = rt.getRow(rowIndex);
        if(mol.hasParam(LABEL_DETECTIONS)) {
            if(mol.getParam(LABEL_DETECTIONS) > 1) {
                table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
        }
        table.setCursor(Cursor.getDefaultCursor()); // reset
    }

    @Override
    protected void dropFile(File f) {
        new ImportExportPlugIn(f.getAbsolutePath()).run(ImportExportPlugIn.IMPORT + ";" + IJResultsTable.IDENTIFIER);
    }

}
