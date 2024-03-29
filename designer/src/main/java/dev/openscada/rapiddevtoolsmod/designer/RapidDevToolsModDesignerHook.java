package dev.openscada.rapiddevtoolsmod.designer;

import com.inductiveautomation.ignition.common.QualifiedPathUtils;
import com.inductiveautomation.ignition.common.document.DocumentArray;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.project.resource.ProjectResource;
import com.inductiveautomation.ignition.common.project.resource.ResourcePath;
import com.inductiveautomation.ignition.designer.IgnitionDesigner;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.project.DesignableProject;
import com.inductiveautomation.ignition.designer.sqltags.dialog.OnTagSelectedListener;
import com.inductiveautomation.ignition.designer.tags.frame.TagBrowserFrame;
import com.inductiveautomation.ignition.designer.tags.tree.TagBrowserPanel;
import com.inductiveautomation.perspective.common.PerspectiveModule;
import com.inductiveautomation.perspective.common.config.ComponentConfig;
import com.inductiveautomation.perspective.common.config.ViewConfig;
import com.inductiveautomation.ignition.client.tags.tree.node.BrowseTreeNode;
import com.inductiveautomation.ignition.common.tags.config.types.TagObjectType;
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser;
import com.inductiveautomation.ignition.common.tags.model.TagPath;

import java.util.List;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.Arrays;
import dev.openscada.rapiddevtoolsmod.designer.utils.IconUtil;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Designer-scope module hook.  The minimal implementation contains a startup method.
 */
public class RapidDevToolsModDesignerHook extends AbstractDesignerModuleHook {

    private static final Logger logger = LoggerFactory.getLogger(RapidDevToolsModDesignerHook.class);

    private DesignerContext context;
    private String projectName;
    List<BrowseTreeNode> selectedTags;

    @Override
    public void startup(DesignerContext context, LicenseState activationState) throws Exception {
        // implelement functionality as required
        this.context = context;
        this.projectName = context.getProjectName();
        addMenuItemToTagBrowser();

    }


    private void addMenuItemToTagBrowser() {

        JMenuItem menuItem = new JMenuItem("Add to View");
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                for (BrowseTreeNode tagNode : selectedTags){
                    logger.info(tagNode.getTagPath().toString());
                    // logger.info(TagPathParser.parseSafe(tagNode.getTagPath().toString() + "/Parameters.opcPrefix").toString());
                    // logger.info(tagNode.getTagType().toString());
                    // logger.info(tagNode.getInfo().getObjectType().name());
                    // logger.info(tagNode.getInfo().getSubTypeId());
                    // logger.info(tagNode.getInfo().getAttributes().toString());

                    TagPath dropConfigPath = TagPathParser.parseSafe(tagNode.getTagPath().toString() + ".dropConfig");
                    try {
                        List<QualifiedValue> qvs = context.getTagManager().readAsync(Arrays.asList(dropConfigPath)).get(); // this is CompletableFuture, do better
                        QualifiedValue qv = qvs.get(0);
                        if (qv.getQuality().isGood()) {
                            DocumentArray docArray = (DocumentArray) qv.getValue();
                            if (docArray.size() > 0) {
                                logger.info(docArray.get(0).getAsDocument().getAsDocumentPrimitive("name").getAsString());
                                addSymbolToView(docArray.get(0).getAsDocument().getAsDocumentPrimitive("symbolJson").getAsString(), "Test Views/View 1/P&ID");
                            }
                            
                        } 
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        OnTagSelectedListener tagListener = new OnTagSelectedListener() {
            public void tagSelectionChanged(List<BrowseTreeNode> selectedNodes) {
                selectedTags = selectedNodes;

                // Only enable menuItem if tag(s) is UdtInstance and has .dropConfig custom property
                if (    selectedTags.size() > 0 && 
                        selectedTags.stream().allMatch( selectedTag -> {
                            if (selectedTag.getInfo().getObjectType() == TagObjectType.UdtInstance) {
                                TagPath dropConfigPath = TagPathParser.parseSafe(selectedTag.getTagPath().toString() + ".dropConfig");
                                try {
                                    List<QualifiedValue> qvs = context.getTagManager().readAsync(Arrays.asList(dropConfigPath)).get();
                                    QualifiedValue qv = qvs.get(0);
                                    if (qv.getQuality().isGood()) {
                                        // TODO: if length of dropConfig > 1, add submenus
                                        return true;
                                    } else {
                                        return false;
                                    }
                                } catch (InterruptedException | ExecutionException e) {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        })) {
                    menuItem.setEnabled(true);
                }
                else {
                    menuItem.setEnabled(false);
                }

            }     
        };

        
        menuItem.addActionListener(actionListener);
        TagBrowserFrame tagBrowserFrame = context.getTagBrowser();
        tagBrowserFrame.addOnTagSelectedListener(tagListener);
        tagBrowserFrame.addTagPopupMenuComponent(menuItem, 0);
    }




    void addSymbolToView(String symbolJson, String viewPath) {
        DesignableProject designableProject = context.getProject();
        ResourcePath resourcePath = new ResourcePath(ViewConfig.RESOURCE_TYPE, viewPath);
        ProjectResource projectResource = designableProject.getLocalResource(resourcePath).get();  // this is Optional type, do better
        ViewConfig viewConfig = ViewConfig.fromProjectResource(projectResource, PerspectiveModule.createPerspectiveCompatibleGson());
        viewConfig.root.children.add(PerspectiveModule.createPerspectiveCompatibleGson().fromJson(symbolJson, ComponentConfig.class));
        ProjectResource newProjectResource = projectResource.toBuilder()
                                                            .putData(ViewConfig.RESOURCE_FILENAME, PerspectiveModule.createPerspectiveCompatibleGson().toJson(viewConfig).getBytes())
                                                            .build();
        designableProject.createOrModify(newProjectResource);
    }

}


//  JMenuItem addToViewMenuItem = new JMenuItem("Add to View", IconUtil.getIcon("/dev/openscada/rapiddevtoolsmod/designer/icons/ic_pull.svg"));
