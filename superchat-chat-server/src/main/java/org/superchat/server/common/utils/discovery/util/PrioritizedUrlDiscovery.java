package org.superchat.server.common.utils.discovery.util;

import cn.hutool.core.util.StrUtil;
import org.jsoup.nodes.Document;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PrioritizedUrlDiscovery extends AbstractUrlDiscovery {

    private final List<UrlDiscovery> urlDiscoveries = new ArrayList<>(2);

    public PrioritizedUrlDiscovery() {
        this.urlDiscoveries.add(new CommonUrlDiscovery());
        this.urlDiscoveries.add(new WxUrlDiscovery());
    }

    @Nullable
    @Override
    public String getTitle(Document document) {
        for (UrlDiscovery urlDiscover : urlDiscoveries) {
            String urlTitle = urlDiscover.getTitle(document);
            if (StrUtil.isNotBlank(urlTitle)) {
                return urlTitle;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getImage(String url, Document document) {
        for (UrlDiscovery urlDiscovery : urlDiscoveries) {
            String image = urlDiscovery.getImage(url, document);
            if (StrUtil.isNotBlank(image)) {
                return image;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getDesc(Document document) {
        for (UrlDiscovery urlDiscovery : urlDiscoveries) {
            String desc = urlDiscovery.getDesc(document);
            if (StrUtil.isNotBlank(desc)) {
                return desc;
            }
        }
        return null;
    }
}
