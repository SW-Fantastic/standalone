package org.swdc.djvu.test;

import com.lizardtech.djvu.DjVuImageFilter;
import com.lizardtech.djvu.DjVuInfo;
import com.lizardtech.djvu.DjVuPage;
import com.lizardtech.djvu.Document;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestRender {

    public static void main(String[] args) throws IOException {
        Document document = new Document(new File("test.djvu").toURI().toURL());
        int pages = document.getDjVmDir().get_pages_num();
        DjVuPage page = document.getPage(373, DjVuPage.MAX_PRIORITY,true);

        DjVuImageFilter filter = new DjVuImageFilter(
                new Rectangle(0,0,794,1123),
                new Dimension(794,1123),
                page,
                true
        );
        Image image = filter.getImage();
        ImageIO.write(
                convertToBufferedImage(image),
                "png",
                new FileOutputStream("./Test Page.png")
        );
        System.err.println(pages);
    }

    public static BufferedImage convertToBufferedImage(Image image)
    {
        BufferedImage newImage = new BufferedImage(
                image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }

}
