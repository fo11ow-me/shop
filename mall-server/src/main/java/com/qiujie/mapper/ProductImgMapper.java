package com.qiujie.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiujie.entity.ProductImg;
import org.apache.ibatis.annotations.Param;
import java.util.List;
public interface ProductImgMapper extends BaseMapper<ProductImg> {
    List<ProductImg> selectByProductId(@Param("productId") Integer productId);
    List<ProductImg> selectByProductIds(@Param("productIds") List<Integer> productIds);
    ProductImg selectFirstByProductId(@Param("productId") Integer productId);
}
