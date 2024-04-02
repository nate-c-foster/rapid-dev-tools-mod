/**
 * This class corresponds to a single array element from the .copyConfig custom property of a component UDT.
 * 
 * Behind the scenes, this is a Document class from the Ignition API.
 */



package dev.openscada.rapiddevtoolsmod.designer;

import com.inductiveautomation.ignition.common.document.Document;
import com.inductiveautomation.ignition.common.project.resource.ProjectResource;
import com.inductiveautomation.ignition.common.project.resource.ResourcePath;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.project.DesignableProject;
import com.inductiveautomation.perspective.common.PerspectiveModule;
import com.inductiveautomation.perspective.common.config.ComponentConfig;
import com.inductiveautomation.perspective.common.config.ViewConfig;


public class CopyConfigElement {

    private DesignerContext designerContext;
    private String name;
    private ComponentConfig componentConfig;
    

    public CopyConfigElement(Document copyConfigDoc, String componentName, DesignerContext designerContext) {
        this.designerContext = designerContext;
        this.name = copyConfigDoc.getAsDocumentPrimitive("name").getAsString();
        this.componentConfig = getComponentConfig(copyConfigDoc.getAsDocumentPrimitive("componentJson").getAsString(), componentName);
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


    public ComponentConfig getComponentConfig() {
        return componentConfig;
    }



    public static ComponentConfig getComponentConfig(String componentJson, String componentName) {
        ComponentConfig componentConfig =  PerspectiveModule.createPerspectiveCompatibleGson().fromJson(componentJson, ComponentConfig.class);
        componentConfig.meta.addProperty("name", componentName);
        return componentConfig;
    } 


    // this is currently not being used. it adds a component directly to a view without copy/paste
    public void addComponentToViewRootContainer(String viewPath) {

        DesignableProject designableProject = this.designerContext.getProject();
        ResourcePath resourcePath = new ResourcePath(ViewConfig.RESOURCE_TYPE, viewPath);
        ProjectResource projectResource = designableProject.getLocalResource(resourcePath).get();  // this is Optional type, do better handling
        ViewConfig viewConfig = ViewConfig.fromProjectResource(projectResource, PerspectiveModule.createPerspectiveCompatibleGson());
        viewConfig.root.children.add(this.componentConfig);
        ProjectResource newProjectResource = projectResource.toBuilder()
                                                            .putData(ViewConfig.RESOURCE_FILENAME, PerspectiveModule.createPerspectiveCompatibleGson().toJson(viewConfig).getBytes())
                                                            .build();
        designableProject.createOrModify(newProjectResource);

    }


}