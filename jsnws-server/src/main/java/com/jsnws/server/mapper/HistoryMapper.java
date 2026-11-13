package com.jsnws.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsnws.server.entity.History;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface HistoryMapper extends BaseMapper<History> {

    /** 总访问量（每个 IP 每天记一次） */
    @Select("SELECT COUNT(*) FROM (SELECT DISTINCT ip, DATE(create_time) FROM history) t")
    Integer countIpHistory();

    /** 访问省份 TOP */
    @Select("SELECT province AS province, COUNT(*) AS num FROM history GROUP BY province ORDER BY num DESC LIMIT #{limit}")
    List<Map<String, Object>> topProvince(int limit);

    /** 访问 IP TOP */
    @Select("SELECT ip AS ip, COUNT(*) AS num FROM history GROUP BY ip ORDER BY num DESC LIMIT #{limit}")
    List<Map<String, Object>> topIp(int limit);

    /** 指定日期（今天/昨天）的访问量：每个 IP 每天记一次 */
    @Select("SELECT COUNT(DISTINCT ip) FROM history WHERE DATE(create_time) = #{day}")
    Integer countIpOfDay(String day);

    /** 指定日期访问用户（去重） */
    @Select("SELECT DISTINCT username AS username, avatar AS avatar FROM history " +
            "WHERE DATE(create_time) = #{day} AND username IS NOT NULL LIMIT #{limit}")
    List<Map<String, Object>> usersOfDay(String day, int limit);

    /** 指定日期访问省份统计 */
    @Select("SELECT province AS province, COUNT(*) AS num FROM history WHERE DATE(create_time) = #{day} " +
            "GROUP BY province ORDER BY num DESC LIMIT #{limit}")
    List<Map<String, Object>> provinceOfDay(String day, int limit);
}
