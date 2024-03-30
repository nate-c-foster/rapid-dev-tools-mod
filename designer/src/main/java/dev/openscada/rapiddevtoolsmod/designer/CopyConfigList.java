package dev.openscada.rapiddevtoolsmod.designer;

import java.util.ArrayList;
import java.util.List;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import com.inductiveautomation.perspective.common.PerspectiveModule;

public class CopyConfigList {


    private List<CopyConfig> copyConfigs;


    public CopyConfigList() {
        this.copyConfigs = new ArrayList<CopyConfig>();
    }



    public void add(CopyConfig copyConfig) {
        copyConfigs.add(copyConfig);
    }


    public List<CopyConfig> getCopyConfigs() {
        return copyConfigs;
    }


    public void addToClipboard() {
        if (getCopyConfigs().size() > 0) {
            String copyConfigListString = PerspectiveModule.createPerspectiveCompatibleGson()
                                                            .toJson(getCopyConfigs().stream().map(CopyConfig::getComponentConfig).toArray());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(copyConfigListString), null);
        }
    }
                    

}
