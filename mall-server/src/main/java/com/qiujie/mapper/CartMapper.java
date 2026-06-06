package com.qiujie.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiujie.entity.Cart;
import com.qiujie.vo.CartVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;
public interface CartMapper extends BaseMapper<Cart> {
    List<CartVO> selectByUserId(@Param("userId") Integer userId);
    Cart selectByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);
}
