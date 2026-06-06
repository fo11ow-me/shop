package com.qiujie.repository;

import com.qiujie.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 商品 ES 搜索仓库
 *
 * @author qiujie
 */
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Integer> {

    Page<ProductDocument> findByStatusAndNameOrDetail(Integer status, String name, String detail, Pageable pageable);

    void deleteByStatus(Integer status);
}
