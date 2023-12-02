package org.superchat.server.common.utils.discovery.util;

import cn.hutool.core.util.StrUtil;
import org.jsoup.nodes.Document;

import javax.annotation.Nullable;

/**
 * 普通URL解析
 */
public class CommonUrlDiscovery extends AbstractUrlDiscovery {

    @Nullable
    @Override
    public String getTitle(Document document) {
        return document.title();
    }

    @Nullable
    @Override
    public String getImage(String url, Document document) {
        String image = document.select("link[type=image/x-icon]").attr("href");
        //如果没有去匹配含有icon属性的logo
        String href = StrUtil.isEmpty(image) ? document.select("link[rel$=icon]").attr("href") : image;
        //如果url已经包含了logo
        if (StrUtil.containsAny(url, "favicon")) {
            return url;
        }
        //如果icon可以直接访问或者包含了http
        if (isConnect(!StrUtil.startWith(href, "http") ? "http:" + href : href)) {
            return href;
        }

        return StrUtil.format("{}/{}", url, StrUtil.removePrefix(href, "/"));
    }

    @Nullable
    @Override
    public String getDesc(Document document) {
        String desc = document.head().select("meta[name=description]").attr("content");
        String keywords = document.head().select("meta[name=keywords]").attr("content");
        return StrUtil.isNotBlank(desc) ? desc : keywords;
    }
}
