package dev.openscada.rapiddevtoolsmod.designer;

import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.designer.IgnitionDesigner;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.tags.frame.TagBrowserFrame;
import com.inductiveautomation.ignition.designer.tags.tree.TagBrowserPanel;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import dev.openscada.rapiddevtoolsmod.designer.utils.IconUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Designer-scope module hook.  The minimal implementation contains a startup method.
 */
public class RapidDevToolsModDesignerHook extends AbstractDesignerModuleHook {

    private static final Logger logger = LoggerFactory.getLogger(RapidDevToolsModDesignerHook.class);

    public static DesignerContext context;
    public static String projectName;


    @Override
    public void startup(DesignerContext context, LicenseState activationState) throws Exception {
        // implelement functionality as required
        RapidDevToolsModDesignerHook.context = context;
        RapidDevToolsModDesignerHook.projectName = context.getProjectName();
        addMenuItemToTagBrowser();

    }


    private void addMenuItemToTagBrowser() {

        JMenuItem addToViewMenuItem = new JMenuItem("Add to View");
        addToViewMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addToViewMenuItem.setText("Pressed!");
            }
        });

        TagBrowserFrame tagBrowserFrame = context.getTagBrowser();
        tagBrowserFrame.addTagPopupMenuComponent(addToViewMenuItem, 0);
    }


}


//         JMenuItem addToViewMenuItem = new JMenuItem("Add to View", IconUtil.getIcon("/dev/openscada/rapiddevtoolsmod/designer/icons/ic_pull.svg"));
