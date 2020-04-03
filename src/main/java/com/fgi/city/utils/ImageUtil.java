package com.fgi.city.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageUtil {

    private Logger logger = LogManager.getLogger(ImageUtil.class);

    private volatile static ImageUtil imageUtil;

    private ImageUtil() {
    }

    public static ImageUtil getInstance() {
        if (imageUtil == null) {
            synchronized (ImageUtil.class) {
                if (imageUtil == null) {
                    imageUtil = new ImageUtil();
                }
            }
        }
        return imageUtil;
    }

    /**
     * 校验格式是否符合要求
     *
     * @param imgString
     * @param imageSize
     * @param imgRangeLow
     * @param imgRangeHigh
     * @param imageFormat
     * @return -1:文件大小不符合要求, -2:文件宽高不符合要求, -3:文件格式不符合要求, -4:文件解析错误
     * @throws IOException
     */
    public int checkFormat(String imgString, double imageSize, int imgRangeLow, int imgRangeHigh, String imageFormat) {
        //允许的图片格式
        String imgType = "png";
        try {
            imgString = replacePre(imgString, imageFormat);
            byte[] bytes = new BASE64Decoder().decodeBuffer(imgString);
            //判断文件大小是否符合要求
            if (!checkSize(bytes, imageSize)) {
                // 文件大小不符合要求
                return -1;
            }
            //不带类似data:image/jpg;base64,前缀的解析
            ImageInputStream imageInputstream = new MemoryCacheImageInputStream(new ByteArrayInputStream(
                    bytes));
            //不使用磁盘缓存
            ImageIO.setUseCache(false);
            Iterator<ImageReader> it = ImageIO.getImageReaders(imageInputstream);
            if (it.hasNext()) {
                ImageReader imageReader = it.next();
                // 设置解码器的输入流
                imageReader.setInput(imageInputstream, true, true);
                // 图像文件格式后缀
                String suffix = imageReader.getFormatName().trim().toLowerCase();
                int height = imageReader.getHeight(0);
                int width = imageReader.getWidth(0);
                imageInputstream.close();
                //校验宽和高是否符合要求
                if (!checkWidthAndHeight(width, height, imgRangeLow, imgRangeHigh)) {
                    // 文件宽高不符合要求
                    return -2;
                }
                String[] imgTypes = imgType.split(",");
                for (String type : imgTypes) {
                    if (type.equalsIgnoreCase(suffix)) {
                        return 0;
                    }
                }
                return -3;
            }
            return -4;
        } catch (IOException e) {
            e.printStackTrace();
            // 解析异常
            return -4;
        }
    }

    /**
     * 去掉base64图片前缀
     *
     * @param imgString
     * @return
     */
    public String replacePre(String imgString, String imgType) {
        //允许的图片格式（可配置）
//        String imgType = "jpg,png,jpeg";
        // 先去掉最后的等号
        Integer equalIndex = imgString.indexOf("=");
        if (equalIndex > 0) {
            imgString = imgString.substring(0, equalIndex);
        }
        // 去掉base64图片前缀
        if (!StringUtils.isEmpty(imgType)) {
            String[] imgTypes = imgType.split(",");
            Pattern pattern;
            Matcher matcher;
            String regex;
            for (String v : imgTypes) {
                regex = MessageFormat.format("data:image/{0};base64,", v);
                pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                matcher = pattern.matcher(imgString);
                if (matcher.lookingAt()) {
                    return matcher.replaceFirst("");
                }
            }
        }
        return imgString;
    }

    /**
     * 校验文件大小
     *
     * @param bytes
     * @return
     */
    private boolean checkSize(byte[] bytes, double imgSize) {
        //符合条件的照片大小（可配置） 单位：M
//        double imgSize = 1.0;
        //图片转base64字符串一般会大，这个变量就是设置偏移量。可配置在文件中，随时修改。目前配的是0。后续看情况适当做修改
        double deviation = 0.0;
        int length = bytes.length;
        //原照片大小
        double size = (double) length / 1024 / 1024 * (1 - deviation);
        return size <= imgSize;
    }

    /**
     * 校验宽高
     *
     * @param
     * @return
     */
    private boolean checkWidthAndHeight(int width, int height, int imgRangeLow, int imgRangeHigh) {
        //宽高最小值（可配置） 单位px
//        int imgRangeLow = 25;
        //宽高最大值（可配置） 单位px
//        int imgRangeHigh = 35;
        if (width > imgRangeLow && width <= imgRangeHigh &&
                height > imgRangeLow && height <= imgRangeHigh) {
            return true;
        }
        return false;
    }
}
