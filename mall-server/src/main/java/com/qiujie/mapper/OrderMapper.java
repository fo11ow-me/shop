package com.qiujie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiujie.entity.Order;
import com.qiujie.vo.CategorySalesVO;
import com.qiujie.vo.OrderVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OrderMapper extends BaseMapper<Order> {

    List<OrderVO> selectByUserId(@Param("userId") Integer userId);

    OrderVO selectDetailById(@Param("id") Integer id);

    IPage<OrderVO> selectPageWithParams(Page<OrderVO> page,
                                        @Param("orderSn") String orderSn,
                                        @Param("userName") String userName,
                                        @Param("statusList") List<Integer> statusList,
                                        @Param("startTime") String startTime,
                                        @Param("endTime") String endTime);

    List<Map<String, Object>> selectTrendData(@Param("days") Integer days);

    List<CategorySalesVO> selectCategorySales();
}
