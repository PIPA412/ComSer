package com.zsc.module.common.tools;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 *
 * @author zsc
 */
public class QRCodeUtils {

    /** 默认宽度 */
    private static final int DEFAULT_WIDTH = 300;
    /** 默认高度 */
    private static final int DEFAULT_HEIGHT = 300;
    /** 默认图片格式 */
    private static final String FORMAT = "PNG";

    /**
     * 生成二维码 Base64（PNG 格式，300x300）
     *
     * @param content 二维码内容
     * @return Base64 编码的二维码图片字符串
     */
    public static String generateBase64(String content) {
        return generateBase64(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成二维码 Base64
     *
     * @param content 二维码内容
     * @param width   宽度
     * @param height  高度
     * @return Base64 编码的二维码图片字符串
     */
    public static String generateBase64(String content, int width, int height) {
        try {
            BufferedImage image = generateImage(content, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, FORMAT, baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("二维码生成失败", e);
        }
    }

    /**
     * 生成二维码图片
     */
    private static BufferedImage generateImage(String content, int width, int height)
            throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        MatrixToImageConfig config = new MatrixToImageConfig(0xFF000000, 0xFFFFFFFF);
        return MatrixToImageWriter.toBufferedImage(bitMatrix, config);
    }
}
