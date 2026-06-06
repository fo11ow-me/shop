package com.qiujie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiujie.entity.SeckillSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀活动场次 Mapper
 *
 * @author qiujie
 */
@Mapper
public interface SeckillSessionMapper extends BaseMapper<SeckillSession> {

    /**
     * 查询当前正在进行中的秒杀场次
     *
     * @param now 当前时间
     * @return 进行中的秒杀场次列表
     */
    List<SeckillSession> selectActiveSessions(@Param("now") LocalDateTime now);

    /**
     * 查询即将开始的秒杀场次
     *
     * @param now 当前时间
     * @return 即将开始的秒杀场次列表
     */
    List<SeckillSession> selectUpcomingSessions(@Param("now") LocalDateTime now);
}
