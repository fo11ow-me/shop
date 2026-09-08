package com.qiujie.service;

import com.qiujie.vo.CategorySalesVO;

import java.util.List;
import java.util.Map;

public interface HomeService {

    Map<String, Object> getCount();

    List<Map<String, Object>> getTrend(Integer days);

    List<CategorySalesVO> getCategorySales();
}
