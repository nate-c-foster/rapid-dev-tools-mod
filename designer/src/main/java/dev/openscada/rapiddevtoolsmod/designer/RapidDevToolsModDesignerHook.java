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

import com.jidesoft.grid.JideTable;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;


import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import java.lang.reflect.Field;

import dev.openscada.rapiddevtoolsmod.common.RapidDevToolsScripts;
import dev.openscada.rapiddevtoolsmod.designer.utils.IconUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {

                    ResultTable resultTable = queryBrowser.getSelectedTab();
                    Field tableField = resultTable.getClass().getDeclaredField("table");
                    tableField.setAccessible(true);

                    JideTable jideTable = (JideTable) tableField.get(resultTable);

                    logger.info("Tab Name: " + resultTable.getTabName());
                    logger.info("Last Query: " + resultTable.getLastQuery());
                    logger.info("Index: " + Integer.toString(resultTable.getIndex()));
                    logger.info("Data 0,0: " + jideTable.getValueAt(0,0).toString());

                } catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException except) {
                    logger.error("No such field");
                }



            }
        });

        menu.add(menuItem);
        menuBar.add(menu);
        queryBrowser.setJMenuBar(menuBar);


//        JButton button = new JButton("Export Button");
//        Container container = queryBrowser.getContentPane();
//        BasicSplitPaneDivider splitPaneDivider = (BasicSplitPaneDivider) container.getComponent(0);
//        JSplitPane splitPane = splitPaneDivider.getBasicSplitPaneUI().getSplitPane();
//        splitPane.setBottomComponent(button);
//        queryBrowser.setContentPane(container);


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

