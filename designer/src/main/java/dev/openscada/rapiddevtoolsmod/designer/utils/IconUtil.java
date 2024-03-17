package dev.openscada.rapiddevtoolsmod.designer.utils;

import dev.openscada.rapiddevtoolsmod.designer.RapidDevToolsModDesignerHook;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.io.InputStream;
import javax.swing.*;

public class IconUtil {
    private static final Logger logger = LoggerFactory.getLogger(Icon.class);

    public static Icon getIcon(String bundleKey){
        InputStream iconStream = RapidDevToolsModDesignerHook.class.getResourceAsStream(bundleKey);
        BufferedImage buffer = null;
        try {
            buffer = ImageIO.read(iconStream);
        } catch (IOException e) {
            logger.warn(e.toString(), e);
        }
        return buffer != null ? new ImageIcon(buffer) : null;
    }
}
