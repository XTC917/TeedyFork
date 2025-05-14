package com.sismics.docs.core.util;

import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.util.ConfigUtil;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateRequest;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translation service using Tencent Cloud Translation API.
 */
public class TranslationService {
    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);
    private static TranslationService instance;
    private final TmtClient client;

    private TranslationService() {
        try {
            // Initialize Tencent Cloud client
            Credential cred = new Credential(
                ConfigUtil.getConfigStringValue(ConfigType.TENCENT_SECRET_ID),
                ConfigUtil.getConfigStringValue(ConfigType.TENCENT_SECRET_KEY)
            );

            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("tmt.tencentcloudapi.com");

            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            client = new TmtClient(cred, "ap-guangzhou", clientProfile);
        } catch (Exception e) {
            log.error("Error initializing translation service", e);
            throw new RuntimeException("Failed to initialize translation service", e);
        }
    }

    public static synchronized TranslationService getInstance() {
        if (instance == null) {
            instance = new TranslationService();
        }
        return instance;
    }

    /**
     * Translate text from source language to target language.
     *
     * @param text Text to translate
     * @param sourceLanguage Source language code (e.g., "en", "zh")
     * @param targetLanguage Target language code (e.g., "zh", "en")
     * @return Translated text
     */
    public String translateText(String text, String sourceLanguage, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        try {
            TextTranslateRequest req = new TextTranslateRequest();
            req.setSourceText(text);
            req.setSource(sourceLanguage);
            req.setTarget(targetLanguage);
            req.setProjectId(0L);

            TextTranslateResponse resp = client.TextTranslate(req);
            return resp.getTargetText();
        } catch (Exception e) {
            log.error("Error translating text", e);
            return text; // Return original text if translation fails
        }
    }
} 