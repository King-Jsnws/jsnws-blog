package com.jsnws.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsnws.server.common.ApiException;
import com.jsnws.server.common.Result;
import com.jsnws.server.entity.ResourceInfo;
import com.jsnws.server.entity.SysConfig;
import com.jsnws.server.mapper.ResourceMapper;
import com.jsnws.server.mapper.SysConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 资源模块：本地上传、资源记录管理、七牛 token
 */
@RestController
public class ResourceController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ResourceController.class);

    private final ResourceMapper resourceMapper;
    private final SysConfigMapper sysConfigMapper;

    @Value("${jsnws.upload-base-dir}")
    private String uploadBaseDir;

    @Value("${jsnws.upload-url-prefix}")
    private String uploadUrlPrefix;

    public ResourceController(ResourceMapper resourceMapper, SysConfigMapper sysConfigMapper) {
        this.resourceMapper = resourceMapper;
        this.sysConfigMapper = sysConfigMapper;
    }

    /** 允许上传的扩展名（按类型归类，统一小写） */
    private static final java.util.Set<String> IMAGE_EXT = new java.util.HashSet<>(java.util.Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "ico"));
    private static final java.util.Set<String> AUDIO_EXT = new java.util.HashSet<>(java.util.Arrays.asList(
            "mp3", "m4a", "wav", "ogg", "flac", "aac"));
    private static final java.util.Set<String> VIDEO_EXT = new java.util.HashSet<>(java.util.Arrays.asList(
            "mp4", "webm", "mov", "m4v"));

    /**
     * 本地上传（multipart：file, type, originalName）
     * 返回可直接访问的 URL 字符串。key 一律由服务端生成，忽略客户端传入值。
     */
    @PostMapping("/resource/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "type", required = false) String type,
                                 @RequestParam(value = "storeType", required = false, defaultValue = "local") String storeType,
                                 @RequestParam(value = "register", required = false, defaultValue = "false") boolean register) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ApiException("文件不能为空！");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        }
        String mime = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        boolean allowed;
        if (IMAGE_EXT.contains(ext)) {
            allowed = mime.startsWith("image/") && !mime.contains("svg");
        } else if (AUDIO_EXT.contains(ext)) {
            allowed = mime.startsWith("audio/");
        } else if (VIDEO_EXT.contains(ext)) {
            allowed = mime.startsWith("video/");
        } else {
            allowed = false;
        }
        if (!allowed) {
            throw new ApiException("不支持的文件类型，仅允许常见图片、音频、视频！");
        }

        String folder = (type == null || type.isEmpty() ? "resource" : type).replaceAll("[^a-zA-Z0-9_-]", "");
        if (folder.isEmpty()) {
            folder = "resource";
        }
        String safeKey = folder + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;

        File base = new File(uploadBaseDir).getAbsoluteFile();
        File target = new File(base, safeKey);
        if (!target.getParentFile().exists()) {
            target.getParentFile().mkdirs();
        }
        file.transferTo(target);

        String url = uploadUrlPrefix + "/" + safeKey.replace("\\", "/");

        // 仅「资源管理」页上传时登记资源记录，避免影响其他页面（如网站设置）的本地图片上传。
        // 七牛上传仍由前端上传成功后调用 /resource/saveResource 登记。
        if (register) {
            ResourceInfo resource = new ResourceInfo();
            resource.setPath(url);
            resource.setName(safeKey);
            resource.setOriginalName(original);
            resource.setSize(file.getSize());
            resource.setMimeType(mime);
            resource.setType(folder);
            resource.setStoreType(storeType);
            resource.setStatus(true);
            resource.setCreateTime(new Date());
            resourceMapper.insert(resource);
        }

        return Result.ok(url);
    }

    /** 保存资源记录（上传成功后由前端调用登记） */
    @PostMapping("/resource/saveResource")
    public Result<Void> saveResource(@RequestBody Map<String, Object> body) {
        ResourceInfo resource = new ResourceInfo();
        resource.setPath(strVal(body.get("path")));
        resource.setName(strVal(body.get("name")));
        resource.setOriginalName(strVal(body.get("originalName")));
        Object size = body.get("size");
        if (size instanceof Number) {
            resource.setSize(((Number) size).longValue());
        }
        resource.setMimeType(strVal(body.get("mimeType")));
        resource.setType(strVal(body.get("type")));
        resource.setStoreType(strVal(body.get("storeType")) == null ? "local" : strVal(body.get("storeType")));
        resource.setStatus(true);
        resource.setCreateTime(new Date());
        resourceMapper.insert(resource);
        return Result.ok();
    }

    /** 资源记录分页（管理员） */
    @PostMapping("/resource/listResource")
    public Result<Page<ResourceInfo>> listResource(@RequestBody Map<String, Object> params) {
        Page<ResourceInfo> page = new Page<>(longVal(params.get("current"), 1), longVal(params.get("size"), 10));
        QueryWrapper<ResourceInfo> wrapper = new QueryWrapper<>();
        String resourceType = strVal(params.get("resourceType"));
        if (resourceType != null && !resourceType.isEmpty()) {
            wrapper.eq("type", resourceType);
        }
        wrapper.orderByDesc("create_time");
        return Result.ok(resourceMapper.selectPage(page, wrapper));
    }

    /** 删除资源记录并清理本地文件（管理员） */
    @PostMapping("/resource/deleteResource")
    public Result<Void> deleteResource(@RequestParam("path") String path) {
        ResourceInfo resource = resourceMapper.selectOne(new QueryWrapper<ResourceInfo>().eq("path", path));
        if (resource != null) {
            if ("local".equals(resource.getStoreType())) {
                deleteLocalFile(resource.getName());
            }
            resourceMapper.deleteById(resource.getId());
        }
        return Result.ok();
    }

    /** 启用/禁用资源记录（管理员） */
    @GetMapping("/resource/changeResourceStatus")
    public Result<Void> changeResourceStatus(@RequestParam("id") Long id,
                                             @RequestParam("flag") Boolean flag) {
        ResourceInfo resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new ApiException("资源不存在！");
        }
        resource.setStatus(flag);
        resourceMapper.updateById(resource);
        return Result.ok();
    }

    /** 七牛云上传 token（未配置时如实报错，不返回假数据） */
    @GetMapping("/qiniu/getUpToken")
    public Result<String> getUpToken(@RequestParam(value = "key", required = false) String key) {
        SysConfig config = sysConfigMapper.selectOne(new QueryWrapper<SysConfig>()
                .eq("config_key", "qiniuUrl").last("LIMIT 1"));
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isEmpty()) {
            throw new ApiException("七牛云未配置，请在后台系统管理-系统配置中设置七牛云参数，或使用本地上传");
        }
        throw new ApiException("七牛云上传未接入，请使用本地上传（storeType=local）");
    }

    /**
     * 删除上传目录内的本地文件。
     * name 来自资源记录（客户端可写），规范化后必须仍位于 uploadBaseDir 之内，
     * 否则跳过删除并记日志，避免通过 ../ 删掉上传目录之外的文件。
     */
    private void deleteLocalFile(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }
        try {
            File base = new File(uploadBaseDir).getCanonicalFile();
            File file = new File(base, name).getCanonicalFile();
            if (!file.getPath().startsWith(base.getPath() + File.separator)) {
                log.warn("拒绝删除上传目录之外的文件：{}", name);
                return;
            }
            if (file.isFile() && file.exists() && !file.delete()) {
                log.warn("删除本地文件失败：{}", file.getPath());
            }
        } catch (IOException e) {
            log.warn("删除本地文件失败：{}", name, e);
        }
    }

    private String strVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private long longVal(Object value, long def) {
        if (value == null) return def;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return def;
        }
    }
}
