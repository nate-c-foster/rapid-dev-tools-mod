package dev.openscada.rapiddevtoolsmod.designer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.inductiveautomation.ignition.client.tags.tree.node.BrowseTreeNode;
import com.inductiveautomation.ignition.common.document.Document;
import com.inductiveautomation.ignition.common.document.DocumentArray;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.project.resource.ProjectResource;
import com.inductiveautomation.ignition.common.project.resource.ResourcePath;
import com.inductiveautomation.ignition.common.tags.model.TagPath;
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.project.DesignableProject;
import com.inductiveautomation.perspective.common.PerspectiveModule;
import com.inductiveautomation.perspective.common.config.ComponentConfig;
import com.inductiveautomation.perspective.common.config.ViewConfig;


public class CopyConfig {

    private DesignerContext context;
    private String name;
    private String componentJson;
    private String componentName;
    

    public CopyConfig(Document copyConfigDoc, String componentName, DesignerContext context) {
        this.name = copyConfigDoc.getAsDocumentPrimitive("name").getAsString();
        this.componentJson = copyConfigDoc.getAsDocumentPrimitive("componentJson").getAsString();
        this.componentName = componentName;
        this.context = context;
    }
    

    public static Optional<CopyConfig> fromTagNode(BrowseTreeNode tagNode, DesignerContext context) {

        TagPath dropConfigPath = TagPathParser.parseSafe(tagNode.getTagPath().toString() + ".copyConfig");

        try {
            List<QualifiedValue> qvs = context.getTagManager().readAsync(Arrays.asList(dropConfigPath)).get(); // this is blocking, do better?
            QualifiedValue qv = qvs.get(0);
            if (qv.getQuality().isGood()) {
                 DocumentArray docArray = (DocumentArray) qv.getValue();
                if (docArray.size() > 0) {
                    if (isValidCopyConfigDoc(docArray.get(0).getAsDocument())){
                        return Optional.of(new CopyConfig(docArray.get(0).getAsDocument(), tagNode.getName(), context));
                    }
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            return Optional.empty();
        }

        return Optional.empty();
    }


    public static boolean isValidCopyConfigDoc(Document copyConfigDoc) {
        if (copyConfigDoc.has("name") && copyConfigDoc.has("componentJson")) {
            return true;
        } else {
            return false;
        }
    }


    public String getName() {
        return name;
    }


    public String getComponentJson() {
        return componentJson;
    }

    public ComponentConfig getComponentConfig() {
        ComponentConfig componentConfig =  PerspectiveModule.createPerspectiveCompatibleGson().fromJson(this.getComponentJson(), ComponentConfig.class);
        componentConfig.meta.addProperty("name", this.componentName);
        return componentConfig;
    } 


    public void addComponentToViewRootContainer(String viewPath) {

        DesignableProject designableProject = context.getProject();
        ResourcePath resourcePath = new ResourcePath(ViewConfig.RESOURCE_TYPE, viewPath);
        ProjectResource projectResource = designableProject.getLocalResource(resourcePath).get();  // this is Optional type, do better handling
        ViewConfig viewConfig = ViewConfig.fromProjectResource(projectResource, PerspectiveModule.createPerspectiveCompatibleGson());
        viewConfig.root.children.add(PerspectiveModule.createPerspectiveCompatibleGson().fromJson(getComponentJson(), ComponentConfig.class));
        ProjectResource newProjectResource = projectResource.toBuilder()
                                                            .putData(ViewConfig.RESOURCE_FILENAME, PerspectiveModule.createPerspectiveCompatibleGson().toJson(viewConfig).getBytes())
                                                            .build();
        designableProject.createOrModify(newProjectResource);

    }


}

