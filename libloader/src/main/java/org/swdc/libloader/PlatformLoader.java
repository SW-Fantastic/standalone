package org.swdc.libloader;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.List;

public class PlatformLoader {

    public void load(File desc) {
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(desc);

            Element root = document.getRootElement();
            Element platforms = root.element("platforms");

            List<Element> platformList = platforms.elements("platform");
            String osName = System.getProperty("os.name").trim().toLowerCase();

            Element platformElem = null;

            for (Element element: platformList) {
                String name = element.attribute("name").getValue();
                if (osName.contains(name.toLowerCase())) {
                    platformElem = element;
                    break;
                }
            }

            if (platformElem == null) {
                throw new RuntimeException("Sorry, Library : " +
                        root.element("name").getText()
                        + " does not support " + osName +
                        " System yet.");
            }

            List<Element> deps = platformElem.elements("dependency");
            for (Element dep: deps) {
                File depFile = new File(dep.attribute("path").getText());
                System.load(depFile.getAbsolutePath());
            }

            File mainModule = new File(platformElem.attribute("path").getText());
            System.load(mainModule.getAbsolutePath());

        } catch (Exception e) {

        }
    }

}
