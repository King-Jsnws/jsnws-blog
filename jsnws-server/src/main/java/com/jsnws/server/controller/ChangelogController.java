package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.entity.Changelog;
import com.jsnws.server.mapper.ChangelogMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 更新日志：前台只读列表 + 后台录入管理
 */
@RestController
public class ChangelogController {

    private final ChangelogMapper changelogMapper;

    public ChangelogController(ChangelogMapper changelogMapper) {
        this.changelogMapper = changelogMapper;
    }

    /** 前台更新日志（按日期倒序，同日按 id 倒序） */
    @GetMapping("/webInfo/listChangelog")
    public Result<List<Changelog>> listChangelog() {
        return Result.ok(changelogMapper.selectList(new QueryWrapper<Changelog>()
                .orderByDesc("log_date").orderByDesc("id")));
    }

    /** 后台更新日志分页 */
    @PostMapping("/admin/changelog/list")
    public Result<Page<Changelog>> list(@RequestBody Map<String, Object> params) {
        long current = params.get("current") == null ? 1 : Long.parseLong(String.valueOf(params.get("current")));
        long size = params.get("size") == null ? 10 : Long.parseLong(String.valueOf(params.get("size")));
        Page<Changelog> page = new Page<>(current, size);
        QueryWrapper<Changelog> wrapper = new QueryWrapper<>();
        String searchKey = params.get("searchKey") == null ? null : String.valueOf(params.get("searchKey")).trim();
        if (searchKey != null && !searchKey.isEmpty()) {
            wrapper.and(w -> w.like("title", searchKey).or().like("kind", searchKey).or().like("version", searchKey));
        }
        wrapper.orderByDesc("log_date").orderByDesc("id");
        return Result.ok(changelogMapper.selectPage(page, wrapper));
    }

    /** 新增更新记录 */
    @PostMapping("/admin/changelog/save")
    public Result<Void> save(@RequestBody Changelog record) {
        normalize(record);
        record.setId(null);
        Date now = new Date();
        record.setCreateTime(now);
        record.setUpdateTime(now);
        changelogMapper.insert(record);
        return Result.ok();
    }

    /** 更新更新记录 */
    @PostMapping("/admin/changelog/update")
    public Result<Void> update(@RequestBody Changelog record) {
        if (record.getId() == null || changelogMapper.selectById(record.getId()) == null) {
            throw new ApiException("更新记录不存在！");
        }
        normalize(record);
        record.setUpdateTime(new Date());
        changelogMapper.updateById(record);
        return Result.ok();
    }

    /** 删除更新记录 */
    @GetMapping("/admin/changelog/delete")
    public Result<Void> delete(@RequestParam("id") Long id) {
        changelogMapper.deleteById(id);
        return Result.ok();
    }

    /** 字段校验与默认值，不额外扩展字段 */
    private void normalize(Changelog record) {
        if (record.getTitle() == null || record.getTitle().trim().isEmpty()) {
            throw new ApiException("标题不能为空！");
        }
        if (record.getLogDate() == null || record.getLogDate().trim().isEmpty()) {
            throw new ApiException("日期不能为空！");
        }
        record.setTitle(record.getTitle().trim());
        record.setLogDate(record.getLogDate().trim());
        String tone = record.getTone() == null ? "" : record.getTone().trim();
        if (!"major".equals(tone) && !"feature".equals(tone) && !"minor".equals(tone)
                && !"fix".equals(tone) && !"content".equals(tone)) {
            throw new ApiException("更新类型不正确！");
        }
        record.setTone(tone);
        if (record.getKind() == null) {
            record.setKind("");
        }
        if (record.getVersion() == null) {
            record.setVersion("");
        }
        if (record.getSummary() == null) {
            record.setSummary("");
        }
        if (record.getTopics() == null || record.getTopics().trim().isEmpty()) {
            record.setTopics("[]");
        }
        if (record.getDetails() == null || record.getDetails().trim().isEmpty()) {
            record.setDetails("[]");
        }
        if (record.getRelatedIds() == null) {
            record.setRelatedIds("");
        }
    }
}
