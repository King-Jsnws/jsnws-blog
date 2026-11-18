package com.jsnws.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsnws.server.entity.History;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface HistoryMapper extends BaseMapper<History> {

    /** 累计访客：按独立 IP 去重，与站点统计口径一致 */
    @Select("SELECT COUNT(DISTINCT ip) FROM history")
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

    /** 指定日期的页面访问量：每次访问记一条，不去重 */
    @Select("SELECT COUNT(*) FROM history WHERE DATE(create_time) = #{day}")
    Integer countPageViewOfDay(String day);

    /** 总页面访问量：每次访问记一条，不去重 */
    @Select("SELECT COUNT(*) FROM history")
    Integer countPageViewHistory();

    /** 指定日期访问用户（去重） */
    @Select("SELECT DISTINCT username AS username, avatar AS avatar FROM history " +
            "WHERE DATE(create_time) = #{day} AND username IS NOT NULL LIMIT #{limit}")
    List<Map<String, Object>> usersOfDay(String day, int limit);

    /** 指定日期访问省份统计 */
    @Select("SELECT province AS province, COUNT(*) AS num FROM history WHERE DATE(create_time) = #{day} " +
            "GROUP BY province ORDER BY num DESC LIMIT #{limit}")
    List<Map<String, Object>> provinceOfDay(String day, int limit);

    // ==================== 站点统计（关于页「站点统计」） ====================

    /** 区间内每日浏览量/独立访客 */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS day, COUNT(*) AS pv, COUNT(DISTINCT ip) AS uv " +
            "FROM history WHERE create_time >= #{start} AND create_time < #{end} GROUP BY day ORDER BY day")
    List<Map<String, Object>> dailyStats(@Param("start") Date start, @Param("end") Date end);

    /** 区间浏览量 */
    @Select("SELECT COUNT(*) FROM history WHERE create_time >= #{start} AND create_time < #{end}")
    Integer countPageViewBetween(@Param("start") Date start, @Param("end") Date end);

    /** 区间独立访客 */
    @Select("SELECT COUNT(DISTINCT ip) FROM history WHERE create_time >= #{start} AND create_time < #{end}")
    Integer countVisitorBetween(@Param("start") Date start, @Param("end") Date end);

    /** 页面排行：只统计有 path 的记录，按独立访客倒序 */
    @Select("SELECT path AS path, COUNT(DISTINCT ip) AS num FROM history " +
            "WHERE path IS NOT NULL AND path <> '' AND create_time >= #{start} " +
            "GROUP BY path ORDER BY num DESC, path ASC LIMIT #{limit}")
    List<Map<String, Object>> topPages(@Param("start") Date start, @Param("limit") int limit);

    /** 活跃时间热力图：周一=0 … 周日=6，0-23 时 */
    @Select("SELECT WEEKDAY(create_time) AS wd, HOUR(create_time) AS hr, COUNT(DISTINCT ip) AS num " +
            "FROM history WHERE create_time >= #{start} GROUP BY wd, hr")
    List<Map<String, Object>> activityCells(@Param("start") Date start);

    /** 设备/浏览器分布：按 User-Agent 原始串分组，解析在服务端完成 */
    @Select("SELECT user_agent AS ua, COUNT(DISTINCT ip) AS num FROM history " +
            "WHERE user_agent IS NOT NULL AND user_agent <> '' AND create_time >= #{start} GROUP BY user_agent")
    List<Map<String, Object>> userAgentStats(@Param("start") Date start);

    /** 访问省份分布：只统计已解析出省份的记录 */
    @Select("SELECT province AS province, COUNT(DISTINCT ip) AS num FROM history " +
            "WHERE province IS NOT NULL AND province <> '' AND province <> '未知' AND create_time >= #{start} " +
            "GROUP BY province ORDER BY num DESC")
    List<Map<String, Object>> regionStats(@Param("start") Date start);

    /** 流量来源：按来源主机名分组（直接访问写入「直接」） */
    @Select("SELECT referrer AS referrer, COUNT(DISTINCT ip) AS num FROM history " +
            "WHERE referrer IS NOT NULL AND referrer <> '' AND create_time >= #{start} " +
            "GROUP BY referrer ORDER BY num DESC")
    List<Map<String, Object>> sourceStats(@Param("start") Date start);
}
