package com.jsnws.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsnws.server.entity.Comment;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {

    /** 站点统计「最近评论」：文章评论与留言板留言合并，按时间倒序取最新若干条 */
    @Select("SELECT * FROM comment WHERE comment_type IN ('article', 'message') " +
            "ORDER BY create_time DESC, id DESC LIMIT #{limit}")
    List<Comment> latestComments(int limit);
}
