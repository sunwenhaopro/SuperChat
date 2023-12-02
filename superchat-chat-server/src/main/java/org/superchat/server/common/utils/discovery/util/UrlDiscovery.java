package org.superchat.server.common.utils.discovery.util;

import org.jsoup.nodes.Document;
import org.superchat.server.common.utils.discovery.domain.UrlInfo;

import javax.annotation.Nullable;
import java.util.Map;

public interface UrlDiscovery {

    @Nullable
    Map<String, UrlInfo> getUrlContentMap(String content);


    @Nullable
    UrlInfo getContent(String url);

    @Nullable
    String getTitle(Document document);

    @Nullable
    String getImage(String url,Document document);

    @Nullable
    String getDesc(Document document);


}
