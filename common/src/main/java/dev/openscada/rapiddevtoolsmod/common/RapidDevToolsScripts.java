package dev.openscada.rapiddevtoolsmod.common;

//import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;

public class RapidDevToolsScripts {
    //public static final String MODULE_ID = "dev.openscada.rapiddevtoolsmod.RapidDevToolsMod";

    @ScriptFunction(docBundlePrefix = "RapidDevToolsScripts")
    public String helloWorld(String name) {
        return "Hello " + name;
    }
}
