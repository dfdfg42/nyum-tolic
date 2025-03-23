package com.nyumtolic.nyumtolic.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CloudinaryImageService {

    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryImageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * @param originalUrl   S3 원본 URL
     * @param width         요청 이미지 너비 (px)
     * @param format        요청 포맷 (ex: "avif", "webp", "jpg")
     * @return              최적화된 이미지 CDN URL
     */
    public String getOptimizedUrl(String originalUrl, int width, int height, String format) {
        try {
            String transformedUrl = cloudinary.url()
                    .secure(true)
                    .resourceType("image")
                    .type("fetch")
                    .transformation(new Transformation()
                            .width(width)
                            .height(height)
                            .crop("fill")     // "fill"로 변경하여 지정 크기에 맞게 자름
                            .gravity("center") // 중앙 기준 자르기
                            .quality("auto")
                            .fetchFormat(format))
                    .generate(originalUrl);
            return transformedUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return originalUrl;
        }
    }
}
