package com.qiujie.config;

import com.qiujie.util.CacheClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.qiujie.constants.RedisConstants.*;

/**
 * 启动时清空首页和分类缓存，防止部署后旧缓存结构与新代码不兼容
 *
 * @author qiujie
 */
@Component
public class CacheInit {

    private static final Logger log = LoggerFactory.getLogger(CacheInit.class);
    private final CacheClient cacheClient;

    public CacheInit(CacheClient cacheClient) {
        this.cacheClient = cacheClient;
    }

    @PostConstruct
    void clear() {
        cacheClient.delete(CACHE_HOME_KEY);
        cacheClient.delete(CACHE_CATEGORY_TREE_KEY);
        log.info("启动清空首页和分类缓存完成");
    }
}
