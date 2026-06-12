package com.qiujie.rag;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek Embedding API 封装
 *
 * @author qiujie
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
    private static final int DIM = 1536;

    private final OkHttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;

    public EmbeddingService(@Value("${deepseek.api-key}") String apiKey,
                            @Value("${deepseek.base-url}") String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /** 对单段文本生成向量 */
    public float[] embed(String text) {
        List<float[]> results = embedBatch(List.of(text));
        return results.isEmpty() ? new float[0] : results.get(0);
    }

    /** 批量生成向量 */
    public List<float[]> embedBatch(List<String> texts) {
        JSONObject body = new JSONObject();
        body.set("model", "deepseek-chat");
        body.set("input", texts);

        Request request = new Request.Builder()
                .url(baseUrl + "/v1/embeddings")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Embedding API error: {}", response.code());
                return List.of();
            }
            String resp = response.body() != null ? response.body().string() : "";
            JSONObject json = JSONUtil.parseObj(resp);
            JSONArray data = json.getJSONArray("data");
            List<float[]> results = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                JSONArray vec = data.getJSONObject(i).getJSONArray("embedding");
                float[] arr = new float[vec.size()];
                for (int j = 0; j < vec.size(); j++) arr[j] = vec.getFloat(j).floatValue();
                results.add(arr);
            }
            return results;
        } catch (IOException e) {
            log.error("Embedding failed", e);
            return List.of();
        }
    }
}
