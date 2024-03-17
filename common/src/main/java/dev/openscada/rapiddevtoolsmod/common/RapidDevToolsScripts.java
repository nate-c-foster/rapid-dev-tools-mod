package dev.openscada.rapiddevtoolsmod.common;

//import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;

public class RapidDevToolsScripts {


    @ScriptFunction(docBundlePrefix = "RapidDevToolsScripts")
    public String helloWorld(String name) {
        return "Hello " + name;
    }

    
}
