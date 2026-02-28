package com.mall.app.controller;

import com.mall.module.product.entity.PmsAsset;
import com.mall.module.product.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 图片资源接口 — 从 MySQL BLOB 读取并返回二进制
 * 所有商品图片统一通过此接口获取，禁止任何外链/CDN/磁盘读取
 */
@RestController
@RequestMapping("/asset")
@Tag(name = "AssetController", description = "图片资源接口")
public class AssetController {

    private static final Logger log = LoggerFactory.getLogger(AssetController.class);
    private static final String VARIANT_THUMB = "thumb";
    private static final String VARIANT_SEARCH = "search";
    private static final int THUMB_MAX_WIDTH = 360;
    private static final int THUMB_MAX_HEIGHT = 360;
    private static final int SEARCH_MAX_WIDTH = 220;
    private static final int SEARCH_MAX_HEIGHT = 220;
    private static final float SEARCH_JPEG_QUALITY = 0.72f;
    private static final int THUMB_CACHE_MAX_ENTRIES = 512;
    private static final long THUMB_CACHE_TTL_MILLIS = 300_000L;

    private final AssetService assetService;
    private final Map<String, VariantCacheEntry> variantCache = Collections.synchronizedMap(
            new LinkedHashMap<>(THUMB_CACHE_MAX_ENTRIES, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, VariantCacheEntry> eldest) {
                    return size() > THUMB_CACHE_MAX_ENTRIES;
                }
            }
    );

    private static final class ImagePayload {
        private final byte[] bytes;
        private final String mimeType;

        private ImagePayload(byte[] bytes, String mimeType) {
            this.bytes = bytes;
            this.mimeType = mimeType;
        }
    }

    private static final class VariantCacheEntry {
        private final ImagePayload payload;
        private final long expireAtMillis;

        private VariantCacheEntry(ImagePayload payload, long expireAtMillis) {
            this.payload = payload;
            this.expireAtMillis = expireAtMillis;
        }

        private boolean isExpired(long nowMillis) {
            return nowMillis >= expireAtMillis;
        }
    }

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @Operation(summary = "根据 hash 获取图片二进制")
    @GetMapping("/image/{hash}")
    public void getImage(
            @PathVariable("hash") String hash,
            @RequestParam(value = "variant", required = false) String variant,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        // 验证 hash 格式（SHA-256 = 64 位 hex）
        if (hash == null || !hash.matches("[a-fA-F0-9]{64}")) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return;
        }

        String normalizedVariant = normalizeVariant(variant);
        String etagToken = normalizedVariant.isEmpty() ? hash : hash + "-" + normalizedVariant;

        // ETag / If-None-Match 缓存协商
        String etag = "\"" + etagToken + "\"";
        String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        if (etag.equals(ifNoneMatch)) {
            response.setStatus(HttpStatus.NOT_MODIFIED.value());
            response.setHeader(HttpHeaders.ETAG, etag);
            response.setHeader(HttpHeaders.CACHE_CONTROL, normalizedVariant.isEmpty()
                    ? "public, max-age=31536000, immutable"
                    : "public, max-age=86400");
            return;
        }

        // 从 MySQL 读取 BLOB
        PmsAsset asset = assetService.getByHash(hash);
        if (asset == null || asset.getImageData() == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        ImagePayload payload = resolvePayload(hash, asset, normalizedVariant);
        if (payload == null || payload.bytes == null || payload.bytes.length == 0) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        // 设置响应头
        response.setContentType(payload.mimeType);
        response.setContentLength(payload.bytes.length);
        response.setHeader(HttpHeaders.ETAG, etag);
        response.setHeader(HttpHeaders.CACHE_CONTROL, normalizedVariant.isEmpty()
                ? "public, max-age=31536000, immutable"
                : "public, max-age=86400");
        response.setHeader("X-Content-Hash", hash);
        if (!normalizedVariant.isEmpty()) {
            response.setHeader("X-Asset-Variant", normalizedVariant);
        }

        // 输出二进制
        response.getOutputStream().write(payload.bytes);
        response.getOutputStream().flush();
    }

    private String normalizeVariant(String variant) {
        if (variant == null) {
            return "";
        }
        String normalized = variant.trim().toLowerCase();
        if (VARIANT_THUMB.equals(normalized)) {
            return VARIANT_THUMB;
        }
        if (VARIANT_SEARCH.equals(normalized)) {
            return VARIANT_SEARCH;
        }
        return "";
    }

    private ImagePayload resolvePayload(String hash, PmsAsset asset, String normalizedVariant) {
        byte[] originBytes = asset.getImageData();
        if (originBytes == null || originBytes.length == 0) {
            return null;
        }
        ImagePayload original = new ImagePayload(originBytes, safeMimeType(asset.getMimeType()));
        if (!VARIANT_THUMB.equals(normalizedVariant) && !VARIANT_SEARCH.equals(normalizedVariant)) {
            return original;
        }

        long nowMillis = System.currentTimeMillis();
        String cacheKey = hash + "#" + normalizedVariant;
        VariantCacheEntry cached = variantCache.get(cacheKey);
        if (cached != null && !cached.isExpired(nowMillis)) {
            return cached.payload;
        }

        ImagePayload variantPayload = VARIANT_SEARCH.equals(normalizedVariant)
                ? generateVariantPayload(asset, original, SEARCH_MAX_WIDTH, SEARCH_MAX_HEIGHT, true, SEARCH_JPEG_QUALITY)
                : generateVariantPayload(asset, original, THUMB_MAX_WIDTH, THUMB_MAX_HEIGHT, false, 1.0f);
        variantCache.put(cacheKey, new VariantCacheEntry(variantPayload, nowMillis + THUMB_CACHE_TTL_MILLIS));
        return variantPayload;
    }

    private ImagePayload generateVariantPayload(PmsAsset asset,
                                                ImagePayload fallback,
                                                int maxWidth,
                                                int maxHeight,
                                                boolean forceJpeg,
                                                float jpegQuality) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(asset.getImageData())) {
            BufferedImage source = ImageIO.read(input);
            if (source == null) {
                return fallback;
            }

            int sourceWidth = source.getWidth();
            int sourceHeight = source.getHeight();
            if (!forceJpeg && sourceWidth <= maxWidth && sourceHeight <= maxHeight) {
                return fallback;
            }

            double ratio = Math.min((double) maxWidth / sourceWidth, (double) maxHeight / sourceHeight);
            ratio = Math.min(ratio, 1.0d);
            int targetWidth = Math.max(1, (int) Math.round(sourceWidth * ratio));
            int targetHeight = Math.max(1, (int) Math.round(sourceHeight * ratio));

            boolean hasAlpha = source.getColorModel() != null && source.getColorModel().hasAlpha();
            int imageType = (hasAlpha && !forceJpeg) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage resized = new BufferedImage(targetWidth, targetHeight, imageType);
            Graphics2D g2d = resized.createGraphics();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!hasAlpha || forceJpeg) {
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(0, 0, targetWidth, targetHeight);
                }
                g2d.drawImage(source, 0, 0, targetWidth, targetHeight, null);
            } finally {
                g2d.dispose();
            }

            if (forceJpeg) {
                byte[] compressed = encodeJpeg(resized, jpegQuality);
                if (compressed == null || compressed.length == 0) {
                    return fallback;
                }
                return new ImagePayload(compressed, "image/jpeg");
            }

            String format = hasAlpha ? "png" : "jpeg";
            String mimeType = hasAlpha ? "image/png" : "image/jpeg";
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                boolean written = ImageIO.write(resized, format, output);
                if (!written) {
                    return fallback;
                }
                byte[] bytes = output.toByteArray();
                if (bytes.length == 0) {
                    return fallback;
                }
                return new ImagePayload(bytes, mimeType);
            }
        } catch (Exception ex) {
            log.debug("Generate variant failed, fallback to original asset. hash={}, variant={}", asset.getImageHash(),
                    forceJpeg ? VARIANT_SEARCH : VARIANT_THUMB, ex);
            return fallback;
        }
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = writers.hasNext() ? writers.next() : null;
        if (writer == null) {
            return null;
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(ios);
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(Math.max(0.1f, Math.min(quality, 1.0f)));
            }
            writer.write(null, new IIOImage(image, null, null), writeParam);
            return output.toByteArray();
        } finally {
            writer.dispose();
        }
    }

    private String safeMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return "image/jpeg";
        }
        return mimeType;
    }
}
