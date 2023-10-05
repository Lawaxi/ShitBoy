package net.lawaxi.util;

import net.coobird.thumbnailator.Thumbnails;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ThumbnailatorUtil {
    public static InputStream generateThumbnail(InputStream videoInputStream) throws IOException {
        BufferedImage thumbnailImage = Thumbnails.of(videoInputStream)
                .size(320, 240)
                .outputFormat("jpg")
                .asBufferedImage();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(thumbnailImage).outputFormat("jpg").toOutputStream(outputStream);

        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
