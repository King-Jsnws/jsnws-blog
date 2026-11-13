package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.entity.SponsorRecord;
import com.jsnws.server.mapper.SponsorRecordMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 赞助记录：前台赞助页展示 + 后台登记管理
 */
@RestController
public class SponsorController {

    private final SponsorRecordMapper sponsorRecordMapper;

    public SponsorController(SponsorRecordMapper sponsorRecordMapper) {
        this.sponsorRecordMapper = sponsorRecordMapper;
    }

    /** 前台赞助记录（按赞助时间倒序） */
    @GetMapping("/webInfo/getAdmire")
    public Result<List<SponsorRecord>> getAdmire() {
        return Result.ok(sponsorRecordMapper.selectList(new QueryWrapper<SponsorRecord>()
                .orderByDesc("create_time").orderByDesc("id")));
    }

    /** 后台赞助记录分页 */
    @PostMapping("/admin/sponsor/list")
    public Result<Page<SponsorRecord>> list(@RequestBody Map<String, Object> params) {
        long current = params.get("current") == null ? 1 : Long.parseLong(String.valueOf(params.get("current")));
        long size = params.get("size") == null ? 10 : Long.parseLong(String.valueOf(params.get("size")));
        Page<SponsorRecord> page = new Page<>(current, size);
        QueryWrapper<SponsorRecord> wrapper = new QueryWrapper<>();
        String searchKey = params.get("searchKey") == null ? null : String.valueOf(params.get("searchKey")).trim();
        if (searchKey != null && !searchKey.isEmpty()) {
            wrapper.and(w -> w.like("name", searchKey).or().like("remark", searchKey));
        }
        wrapper.orderByDesc("create_time").orderByDesc("id");
        return Result.ok(sponsorRecordMapper.selectPage(page, wrapper));
    }

    /** 新增赞助记录 */
    @PostMapping("/admin/sponsor/save")
    public Result<Void> save(@RequestBody SponsorRecord record) {
        normalize(record);
        record.setId(null);
        sponsorRecordMapper.insert(record);
        return Result.ok();
    }

    /** 更新赞助记录 */
    @PostMapping("/admin/sponsor/update")
    public Result<Void> update(@RequestBody SponsorRecord record) {
        if (record.getId() == null || sponsorRecordMapper.selectById(record.getId()) == null) {
            throw new ApiException("赞助记录不存在！");
        }
        normalize(record);
        sponsorRecordMapper.updateById(record);
        return Result.ok();
    }

    /** 删除赞助记录 */
    @GetMapping("/admin/sponsor/delete")
    public Result<Void> delete(@RequestParam("id") Long id) {
        sponsorRecordMapper.deleteById(id);
        return Result.ok();
    }

    private void normalize(SponsorRecord record) {
        if (record.getName() == null || record.getName().trim().isEmpty()) {
            throw new ApiException("赞助人不能为空！");
        }
        record.setName(record.getName().trim());
        if (record.getAmount() == null || record.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException("赞助金额不正确！");
        }
        if (record.getCreateTime() == null) {
            record.setCreateTime(new Date());
        }
    }
}
