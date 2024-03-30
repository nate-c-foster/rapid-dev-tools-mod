package dev.openscada.rapiddevtoolsmod.designer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.inductiveautomation.ignition.client.tags.tree.node.BrowseTreeNode;
import com.inductiveautomation.ignition.common.document.DocumentArray;
import com.inductiveautomation.ignition.common.document.DocumentElement;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.tags.model.TagPath;
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser;
import com.inductiveautomation.ignition.designer.model.DesignerContext;



public class CopyConfig {

    //private DesignerContext context;
    private List<CopyConfigElement> copyConfig; 


    public List<CopyConfigElement> getCopyConfig() {
        return copyConfig;
    }


    public CopyConfig(List<CopyConfigElement> copyConfig, DesignerContext context) {

       // this.context = context;
        this.copyConfig = copyConfig;
    }
    

    public static Optional<CopyConfig> fromTagNode(BrowseTreeNode tagNode, DesignerContext context) {

        List<CopyConfigElement> copyConfig = new ArrayList<CopyConfigElement>();

        TagPath dropConfigPath = TagPathParser.parseSafe(tagNode.getTagPath().toString() + ".copyConfig");

        try {
            List<QualifiedValue> qvs = context.getTagManager().readAsync(Arrays.asList(dropConfigPath)).get(1, TimeUnit.SECONDS);
            QualifiedValue qv = qvs.get(0);
            if (qv.getQuality().isGood()) {
                 DocumentArray docArray = (DocumentArray) qv.getValue();
                if (docArray.size() > 0) {
                    for (DocumentElement doc : docArray) {
                        if (CopyConfigElement.isValidCopyConfigDoc(doc.getAsDocument())){
                            copyConfig.add(new CopyConfigElement(doc.getAsDocument(), tagNode.getName(), context));
                        }
                    }
                }
            }

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return Optional.empty();
        }

        if (copyConfig.size() > 0) {
            return Optional.of(new CopyConfig(copyConfig, context));
        } else {
            return Optional.empty();
        }

    }

}

