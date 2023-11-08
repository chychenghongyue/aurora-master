package com.blog.service;

import com.blog.model.dto.AboutDTO;
import com.blog.model.dto.BlogAdminInfoDTO;
import com.blog.model.dto.blogHomeInfoDTO;
import com.blog.model.dto.WebsiteConfigDTO;
import com.blog.model.vo.AboutVO;
import com.blog.model.vo.WebsiteConfigVO;

public interface blogInfoService {

    void report();

    blogHomeInfoDTO getblogHomeInfo();

    BlogAdminInfoDTO getblogAdminInfo();

    void updateWebsiteConfig(WebsiteConfigVO websiteConfigVO);

    WebsiteConfigDTO getWebsiteConfig();

    void updateAbout(AboutVO aboutVO);

    AboutDTO getAbout();

}
