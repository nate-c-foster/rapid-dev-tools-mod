package dev.openscada.rapiddevtoolsmod.designer;

import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.sqltags.dialog.OnTagSelectedListener;
import com.inductiveautomation.ignition.designer.tags.frame.TagBrowserFrame;
import com.inductiveautomation.perspective.common.PerspectiveModule;
import com.inductiveautomation.ignition.client.tags.tree.node.BrowseTreeNode;
import com.inductiveautomation.ignition.common.tags.config.types.TagObjectType;
import com.inductiveautomation.ignition.designer.querybrowser.QueryBrowser;
import com.inductiveautomation.ignition.designer.querybrowser.ResultTable;
import com.inductiveautomation.ignition.common.BasicDataset;
import com.inductiveautomation.ignition.common.util.csv.CSVWriter;

import com.jidesoft.grid.JideTable;
import com.jidesoft.grid.SortableTableModel;


import java.awt.*;
import java.io.IOException;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.JFileChooser;


import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.io.File;
import java.io.FileWriter;

import java.lang.reflect.Field;



import dev.openscada.rapiddevtoolsmod.common.RapidDevToolsScripts;
import dev.openscada.rapiddevtoolsmod.designer.utils.IconUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FilenameUtils;

public class RapidDevToolsModDesignerHook extends AbstractDesignerModuleHook {

    //private static final Logger logger = LoggerFactory.getLogger(RapidDevToolsModDesignerHook.class);

    private DesignerContext context;

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public void startup(DesignerContext context, LicenseState activationState) throws Exception {
        // implement functionality as required
        this.context = context;
        logger.info("DesignerHook Constructor");
        addMenuItemToTagBrowser();
        addButtonToQueryBrowser();

    }

    @Override
    public void initializeScriptManager(ScriptManager manager){
        super.initializeScriptManager(manager);
        manager.addScriptModule("system.rapiddev", new RapidDevToolsScripts(), new PropertiesFileDocProvider());
    }


    private void addButtonToQueryBrowser() {

        logger.info("DesignerHook add button to query browser");

        QueryBrowser queryBrowser = context.getQueryBrowserPanel();

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem menuItem = new JMenuItem("Export");
        menuItem.setIcon(IconUtil.getIcon("/dev/openscada/rapiddevtoolsmod/designer/icons/kevin-icon.png"));

        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {

                    ResultTable resultTable = queryBrowser.getSelectedTab();
                    Field tableField = resultTable.getClass().getDeclaredField("table");
                    tableField.setAccessible(true);

                    JideTable jideTable = (JideTable) tableField.get(resultTable);
                    
                    Field tableModelField = resultTable.getClass().getDeclaredField("tableModel");
                    tableModelField.setAccessible(true);

                    JFileChooser fileChooser = new JFileChooser();

                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.addChoosableFileFilter(new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            String filename = f.getName().toLowerCase();
                            return filename.endsWith(".csv");
                        }

                        @Override
                        public String getDescription() {
                            return "CSV";
                        }
                    });

                    int fileChooserReturnVal = fileChooser.showSaveDialog(queryBrowser);

                    if (fileChooserReturnVal ==  JFileChooser.APPROVE_OPTION) {
                        File exportFile = fileChooser.getSelectedFile();


                        if (!FilenameUtils.getExtension(exportFile.getName()).equalsIgnoreCase("csv")) {
                            //exportFile = new File(exportFile.toString() + ".csv");
                            exportFile = new File(exportFile.getParentFile(), FilenameUtils.getBaseName(exportFile.getName()) + ".csv");
                        }


                        try (FileWriter fw = new FileWriter(exportFile)) {
                            CSVWriter csvWriter = new CSVWriter(fw);
                            ArrayList<String> nextLine = new ArrayList<>();

                            // Header
                            for (int col=0; col < jideTable.getColumnCount(); col++) {
                                nextLine.add(jideTable.getColumnName(col));
                            }
                            csvWriter.writeNext(nextLine);

                            // Rows
                            for (int row=0; row < jideTable.getRowCount(); row++) {
                                nextLine.clear();
                                for (int col=0; col < jideTable.getColumnCount(); col++) {
                                    nextLine.add(jideTable.getValueAt(row,col) != null ? jideTable.getValueAt(row,col).toString() : "");
                                }
                                csvWriter.writeNext(nextLine);
                            }
                            csvWriter.close();

                        } catch (IOException exception) {
                            logger.error("FileWrite IOException");
                        }

                    } else {
                        logger.info("File chooser canceled");
                    }



                } catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException except) {
                    logger.error("No such field");
                }
            }
        });

        menu.add(menuItem);
        menuBar.add(menu);
        queryBrowser.setJMenuBar(menuBar);

    }



    private void addMenuItemToTagBrowser() {

        JMenu copyConfigMenu = new JMenu("Copy View Config");

        copyConfigMenu.setIcon(IconUtil.getIcon("/dev/openscada/rapiddevtoolsmod/designer/icons/copy-image.svg"));
        OnTagSelectedListener tagListener = new OnTagSelectedListener() {
  
            public void tagSelectionChanged(List<BrowseTreeNode> selectedNodes) {

                copyConfigMenu.removeAll();

                // ---------------------  Only one tag selected  ----------------------------
                if ( selectedNodes.size() == 1 ) {

                    if ( selectedNodes.get(0).getInfo().getObjectType() == TagObjectType.UdtInstance ) {
                        Optional<CopyConfig> copyConfigOpt = CopyConfig.fromTagNode(selectedNodes.get(0), context);
                        if ( copyConfigOpt.isPresent() ){
                            List<CopyConfigElement> copyConfigElements = copyConfigOpt.get().getCopyConfig();

                            // ------- Add a submenu item for each copy config element found
                            copyConfigMenu.setEnabled(true);
                            for (CopyConfigElement element : copyConfigElements) {
                                JMenuItem copyConfigSubMenuItem = new JMenuItem(element.getName());
                                copyConfigSubMenuItem.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent event) {
                                        addCopyConfigToClipboard(Arrays.asList(element));
                                    }
                                });
                                copyConfigMenu.add(copyConfigSubMenuItem);
                            }
                        } else {
                            copyConfigMenu.setEnabled(false);
                        }
                    } else {
                        copyConfigMenu.setEnabled(false);
                    }


                // --------------------  Multiple tags selected  ------------------------------
                } else if ( selectedNodes.size() > 1 ) {

                    List<CopyConfigElement> copyConfigElements = new ArrayList<CopyConfigElement>();
                    for (BrowseTreeNode selectedNode : selectedNodes) {
                        if ( selectedNode.getInfo().getObjectType() == TagObjectType.UdtInstance  ) {
                            Optional<CopyConfig> copyConfigOpt = CopyConfig.fromTagNode(selectedNode, context);
                            if ( copyConfigOpt.isPresent() ) {
                                // just get the first element for each
                                CopyConfigElement defaultConfigElement = copyConfigOpt.get().getCopyConfig().get(0);
                                copyConfigElements.add(defaultConfigElement);
                            }
                        }
                    }
                    
                    if (copyConfigElements.size() > 1) {
                        copyConfigMenu.setEnabled(true);
                        JMenuItem copyConfigSubMenuItem = new JMenuItem("Default Configs");
                        copyConfigSubMenuItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent event) {
                                addCopyConfigToClipboard(copyConfigElements);
                            }
                        });
                        copyConfigMenu.add(copyConfigSubMenuItem);
                    } else {
                        copyConfigMenu.setEnabled(false);
                    }

                }
            }
        };



        TagBrowserFrame tagBrowserFrame = context.getTagBrowser();
        tagBrowserFrame.addOnTagSelectedListener(tagListener);
        tagBrowserFrame.addTagPopupMenuComponent(copyConfigMenu, 10);
    }




    public void addCopyConfigToClipboard(List<CopyConfigElement> copyConfigElementList) {
        if (copyConfigElementList.size() > 0) {
            String copyConfigListString = PerspectiveModule.createPerspectiveCompatibleGson()
                                                            .toJson(copyConfigElementList.stream().map(CopyConfigElement::getComponentConfig).toArray());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(copyConfigListString), null);
        }
    }

}

