package com.qiujie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiujie.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 原子扣减库存。利用 MySQL 行锁保证不超卖
     * @return affected rows，0 表示库存不足
     */
    @Update("UPDATE pms_product SET stock = stock - #{delta} WHERE id = #{id} AND stock >= #{delta}")
    int decrementStock(@Param("id") Integer id, @Param("delta") int delta);

    /** 订单取消时释放库存 */
    @Update("UPDATE pms_product SET stock = stock + #{delta} WHERE id = #{id}")
    int incrementStock(@Param("id") Integer id, @Param("delta") int delta);

    List<Product> selectByCategoryId(@Param("categoryId") Integer categoryId);

    Product selectDetailById(@Param("id") Integer id);

    List<Product> selectByCategoryIdLimit(@Param("categoryId") Integer categoryId, @Param("limit") Integer limit);

    IPage<Product> selectPageByCategoryId(Page<Product> page, @Param("categoryId") Integer categoryId);

    IPage<Product> selectPageByKeyword(Page<Product> page, @Param("keyword") String keyword);

    IPage<Product> selectPageByName(Page<Product> page, @Param("name") String name, @Param("status") Integer status, @Param("categoryId") Integer categoryId);

    List<Product> selectByUpdateTime(@Param("minUpdateTime") LocalDateTime minUpdateTime);

    List<Integer> selectAllIds();
}
