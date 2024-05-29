package com.blog.controller;

import com.blog.annotation.OptLog;
import com.blog.model.dto.AboutDTO;
import com.blog.model.dto.BlogAdminInfoDTO;
import com.blog.model.dto.blogHomeInfoDTO;
import com.blog.model.dto.WebsiteConfigDTO;
import com.blog.enums.FilePathEnum;
import com.blog.model.vo.ResultVO;
import com.blog.service.blogInfoService;
import com.blog.strategy.context.UploadStrategyContext;
import com.blog.model.vo.AboutVO;
import com.blog.model.vo.WebsiteConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import static com.blog.constant.OptTypeConstant.UPDATE;
import static com.blog.constant.OptTypeConstant.UPLOAD;

@Api(tags = "blog信息")
@RestController
public class blogInfoController {

    @Autowired
    private blogInfoService blogInfoService;

    @Autowired
    private UploadStrategyContext uploadStrategyContext;

    @ApiOperation(value = "上报访客信息")
    @PostMapping("/report")
    public ResultVO<?> report() {
        blogInfoService.report();
        return ResultVO.ok();
    }

    @ApiOperation(value = "获取系统信息")
    @GetMapping("/")
    public ResultVO<blogHomeInfoDTO> getBlogHomeInfo() {
        return ResultVO.ok(blogInfoService.getblogHomeInfo());
    }

    @ApiOperation(value = "获取系统后台信息")
    @GetMapping({"/admin","/info"})
    public ResultVO<BlogAdminInfoDTO> getBlogBackInfo() {
        return ResultVO.ok(blogInfoService.getblogAdminInfo());
    }

    @OptLog(optType = UPDATE)
    @ApiOperation(value = "更新网站配置")
    @PutMapping("/admin/website/config")
    public ResultVO<?> updateWebsiteConfig(@Valid @RequestBody WebsiteConfigVO websiteConfigVO) {
        blogInfoService.updateWebsiteConfig(websiteConfigVO);
        return ResultVO.ok();
    }

    @ApiOperation(value = "获取网站配置")
    @GetMapping("/admin/website/config")
    public ResultVO<WebsiteConfigDTO> getWebsiteConfig() {
        return ResultVO.ok(blogInfoService.getWebsiteConfig());
    }

    @ApiOperation(value = "查看关于我信息")
    @GetMapping("/about")
    public ResultVO<AboutDTO> getAbout() {
        return ResultVO.ok(blogInfoService.getAbout());
    }

    @OptLog(optType = UPDATE)
    @ApiOperation(value = "修改关于我信息")
    @PutMapping("/admin/about")
    public ResultVO<?> updateAbout(@Valid @RequestBody AboutVO aboutVO) {
        blogInfoService.updateAbout(aboutVO);
        return ResultVO.ok();
    }

    @OptLog(optType = UPLOAD)
    @ApiOperation(value = "上传博客配置图片")
    @ApiImplicitParam(name = "file", value = "图片", required = true, dataType = "MultipartFile")
    @PostMapping("/admin/config/images")
    public ResultVO<String> savePhotoAlbumCover(MultipartFile file) {
        return ResultVO.ok(uploadStrategyContext.executeUploadStrategy(file, FilePathEnum.CONFIG.getPath()));
    }

}
