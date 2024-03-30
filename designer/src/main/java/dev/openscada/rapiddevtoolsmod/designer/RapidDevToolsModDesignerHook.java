package dev.openscada.rapiddevtoolsmod.designer;

import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.sqltags.dialog.OnTagSelectedListener;
import com.inductiveautomation.ignition.designer.tags.frame.TagBrowserFrame;
import com.inductiveautomation.perspective.common.PerspectiveModule;
import com.inductiveautomation.ignition.client.tags.tree.node.BrowseTreeNode;
import com.inductiveautomation.ignition.common.tags.config.types.TagObjectType;


import java.util.List;
import java.util.Optional;

import javax.swing.JMenu;
//import javax.swing.Icon;
import javax.swing.JMenuItem;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
//import dev.openscada.rapiddevtoolsmod.designer.utils.IconUtil;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * This is the Designer-scope module hook.  The minimal implementation contains a startup method.
 */
public class RapidDevToolsModDesignerHook extends AbstractDesignerModuleHook {

    //private static final Logger logger = LoggerFactory.getLogger(RapidDevToolsModDesignerHook.class);

    private DesignerContext context;


    @Override
    public void startup(DesignerContext context, LicenseState activationState) throws Exception {
        // implelement functionality as required
        this.context = context;
        addMenuItemToTagBrowser();

    }



    private void addMenuItemToTagBrowser() {

        JMenu copyConfigMenu = new JMenu("Copy View Config");
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
        tagBrowserFrame.addTagPopupMenuComponent(copyConfigMenu, 0);
    }




    public void addCopyConfigToClipboard(List<CopyConfigElement> copyConfigElementList) {
        if (copyConfigElementList.size() > 0) {
            String copyConfigListString = PerspectiveModule.createPerspectiveCompatibleGson()
                                                            .toJson(copyConfigElementList.stream().map(CopyConfigElement::getComponentConfig).toArray());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(copyConfigListString), null);
        }
    }

}


//  JMenuItem addToViewMenuItem = new JMenuItem("Add to View", IconUtil.getIcon("/dev/openscada/rapiddevtoolsmod/designer/icons/ic_pull.svg"));
