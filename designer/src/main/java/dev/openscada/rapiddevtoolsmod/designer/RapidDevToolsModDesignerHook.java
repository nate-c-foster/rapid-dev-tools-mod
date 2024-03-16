package dev.openscada.rapiddevtoolsmod.designer;

import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;


/**
 * This is the Designer-scope module hook.  The minimal implementation contains a startup method.
 */
public class RapidDevToolsModDesignerHook extends AbstractDesignerModuleHook {

    // override additonal methods as requried

    @Override
    public void startup(DesignerContext context, LicenseState activationState) throws Exception {
        // implelement functionality as required
    }
}
