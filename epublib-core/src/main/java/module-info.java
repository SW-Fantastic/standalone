module epublib.core {

    exports net.sf.jazzlib;
    exports nl.siegmann.epublib.epub;
    exports nl.siegmann.epublib.domain;
    exports nl.siegmann.epublib.util.commons.io;
    exports nl.siegmann.epublib.util;
    exports nl.siegmann.epublib.service;
    exports nl.siegmann.epublib.browsersupport;
    exports nl.siegmann.epublib.utilities;

    requires org.slf4j;
    requires kxml2;
    requires java.xml;

}