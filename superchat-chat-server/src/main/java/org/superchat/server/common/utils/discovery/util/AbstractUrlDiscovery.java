package org.superchat.server.common.utils.discovery.util;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.util.Pair;
import org.superchat.server.common.utils.FutureUtils;
import org.superchat.server.common.utils.discovery.domain.UrlInfo;

import javax.annotation.Nullable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractUrlDiscovery implements UrlDiscovery {
    private static final Pattern PATTERN = Pattern.compile("((http|https)://)?(www.)?([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?");


    @Nullable
    @Override
    public Map<String, UrlInfo> getUrlContentMap(String content) {

        if (StrUtil.isBlank(content)) {
            return new HashMap<>();
        }
        List<String> matchList = ReUtil.findAll(PATTERN, content, 0);
        List<CompletableFuture<org.springframework.data.util.Pair<String, UrlInfo>>> futures = matchList.stream().map(match ->
            CompletableFuture.supplyAsync(() -> {
                UrlInfo urlInfo = getContent(match);
                return Objects.isNull(urlInfo) ? null : Pair.of(match, urlInfo);
            })).collect(Collectors.toList());
        CompletableFuture<List<org.springframework.data.util.Pair<String, UrlInfo>>> future = FutureUtils.sequenceNonNull(futures);
        return future.join().stream().collect(Collectors.toMap(Pair::getFirst, org.springframework.data.util.Pair::getSecond, (a, b) -> a));

    }

    @Nullable
    @Override
    public UrlInfo getContent(String url) {
        Document document = getUrlDocument(url);
        if (Objects.isNull(document)) {
            return null;
        }
        return UrlInfo.builder()
                .image(getImage(assemble(url), document))
                .title(getTitle(document))
                .description(getDesc(document)).build();
    }

    private String assemble(String url) {
        return StrUtil.startWith(url, "http") ? url : "http://" + url;
    }

    private Document getUrlDocument(String url) {
        try {
            Connection connection = Jsoup.connect(url);
            connection.timeout(2000);
            return connection.get();
        } catch (Exception e) {
            log.error("find error:url:{}", url, e);
        }
        return null;
    }
    public static boolean isConnect(String href) {
        //请求地址
        URL url;
        //请求状态码
        int state;
        //下载链接类型
        String fileType;
        try {
            url = new URL(href);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            state = httpURLConnection.getResponseCode();
            fileType = httpURLConnection.getHeaderField("Content-Disposition");
            //如果成功200，缓存304，移动302都算有效链接，并且不是下载链接
            if ((state == 200 || state == 302 || state == 304) && fileType == null) {
                return true;
            }
            httpURLConnection.disconnect();
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
