package com.qiujie.rag;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 知识库服务 — Redis Stack 向量检索
 *
 * @author qiujie
 */
@Service
public class KnowledgeBaseService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseService.class);
    private static final String INDEX_NAME = "kb:idx";
    private static final String KEY_PREFIX = "kb:";
    private static final int CHUNK_SIZE = 500;

    private final StringRedisTemplate redis;
    private final EmbeddingService embeddingService;
    private volatile boolean ready = false;

    public KnowledgeBaseService(StringRedisTemplate redis, EmbeddingService embeddingService) {
        this.redis = redis;
        this.embeddingService = embeddingService;
    }

    @PostConstruct
    void init() {
        try {
            loadDocuments();
            ready = true;
        } catch (Exception e) {
            log.error("知识库初始化失败", e);
        }
    }

    /** 加载 knowledge/*.md 文档，分块 → embed → 存入 Redis */
    private void loadDocuments() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:knowledge/*.md");
        if (resources.length == 0) {
            log.warn("知识库目录为空");
            return;
        }

        int id = 0;
        for (Resource res : resources) {
            String text = new String(res.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            List<String> chunks = splitChunks(text);
            for (String chunk : chunks) {
                float[] vec = embeddingService.embed(chunk);
                if (vec.length == 0) continue;
                String key = KEY_PREFIX + (++id);

                Map<byte[], byte[]> hash = new LinkedHashMap<>();
                hash.put("content".getBytes(StandardCharsets.UTF_8), chunk.getBytes(StandardCharsets.UTF_8));
                hash.put("embedding".getBytes(StandardCharsets.UTF_8), floatsToBytes(vec));

                redis.execute((RedisCallback<Object>) connection -> {
                    connection.hashCommands().hMSet(key.getBytes(StandardCharsets.UTF_8), hash);
                    return null;
                });
            }
        }
        log.info("知识库初始化完成: {} 个文档块", id);
    }

    /** 检索与查询最相关的 topK 个文本片段 */
    @SuppressWarnings("unchecked")
    public List<String> search(String query) {
        if (!ready) return List.of();
        float[] qVec = embeddingService.embed(query);
        if (qVec.length == 0) return List.of();

        return redis.execute((RedisCallback<List<String>>) connection -> {
            List<byte[]> args = buildSearchArgs(qVec);
            Object resp = connection.execute("FT.SEARCH", args.toArray(new byte[0][]));
            if (resp == null) return List.of();
            return parseSearchResult(resp);
        });
    }

    private List<byte[]> buildSearchArgs(float[] vec) {
        byte[] blob = floatsToBytes(vec);
        List<byte[]> args = new ArrayList<>();
        args.add(INDEX_NAME.getBytes(StandardCharsets.UTF_8));
        args.add("*=>[KNN 3 @embedding $BLOB]".getBytes(StandardCharsets.UTF_8));
        args.add("PARAMS".getBytes(StandardCharsets.UTF_8));
        args.add("2".getBytes(StandardCharsets.UTF_8));
        args.add("BLOB".getBytes(StandardCharsets.UTF_8));
        args.add(blob);
        args.add("RETURN".getBytes(StandardCharsets.UTF_8));
        args.add("2".getBytes(StandardCharsets.UTF_8));
        args.add("content".getBytes(StandardCharsets.UTF_8));
        args.add("__embedding_score".getBytes(StandardCharsets.UTF_8));
        args.add("DIALECT".getBytes(StandardCharsets.UTF_8));
        args.add("2".getBytes(StandardCharsets.UTF_8));
        return args;
    }

    private List<String> parseSearchResult(Object resp) {
        if (!(resp instanceof List)) return List.of();
        List<?> list = (List<?>) resp;
        if (list.isEmpty()) return List.of();

        List<String> contents = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            Object entry = list.get(i);
            if (entry instanceof List) {
                List<?> fields = (List<?>) entry;
                for (int j = 0; j + 1 < fields.size(); j += 2) {
                    Object keyObj = fields.get(j);
                    Object valObj = fields.get(j + 1);
                    if (keyObj instanceof byte[] && valObj instanceof byte[]) {
                        String key = new String((byte[]) keyObj, StandardCharsets.UTF_8);
                        if ("content".equals(key)) {
                            contents.add(new String((byte[]) valObj, StandardCharsets.UTF_8));
                        }
                    }
                }
            }
        }
        return contents;
    }

    private List<String> splitChunks(String text) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            if (end < text.length()) {
                int br = text.lastIndexOf('\n', end);
                if (br > start + CHUNK_SIZE / 2) end = br;
                else {
                    int dot = text.lastIndexOf('。', end);
                    if (dot > start + CHUNK_SIZE / 2) end = dot + 1;
                }
            }
            chunks.add(text.substring(start, end).trim());
            start = end;
        }
        return chunks;
    }

    private static byte[] floatsToBytes(float[] arr) {
        ByteBuffer buf = ByteBuffer.allocate(arr.length * Float.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (float f : arr) buf.putFloat(f);
        return buf.array();
    }
}
