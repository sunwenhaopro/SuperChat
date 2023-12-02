package org.superchat.server.common.utils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.geo.Point;
import org.superchat.server.user.domain.entity.IpDetail;


import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
@Slf4j
public class IpUtil {
    private static DatabaseReader reader ;

    static {
        try {
            InputStream inputStream= new ClassPathResource("/GeoLite2-City.mmdb").getInputStream();
            reader=new DatabaseReader.Builder(inputStream).build();
            System.out.println("---------------Geo加载成功--------------");
        } catch (IOException e) {
            System.out.println("---------------Geo加载失败---------------");
        }
    }
    public static IpDetail getIpDetail(String ip)
    {
        if("127.0.0.1".equals(ip)||
                "localhost".equals(ip)||
                "0:0:0:0:0:0:0:1".equals(ip))
        {
            return new IpDetail();
        }

        try {
            IpDetail ipDetail=new IpDetail();
            ipDetail.setIp(ip);
            ipDetail.setCity(getCity(ip));
            ipDetail.setProvince(getCountry(ip));
            ipDetail.setCity(getCity(ip));
            ipDetail.setPoint(getPoint(ip));
            return ipDetail;
        }catch (Exception e)
        {
            log.error("ip地址解析失败: "+ip);
        }
        //兜底
        return null;
    }

    //城市
    public static String getCity(String ip) throws IOException, GeoIp2Exception {
        return reader.city(InetAddress.getByName(ip)).getCity().getNames().get("zh-CN");
    }
    //国家
    public static String getCountry(String ip) throws IOException, GeoIp2Exception {
        return reader.city(InetAddress.getByName(ip)).getCountry().getNames().get("zh-CN");
    }
    //省份
    public static String getProvince(String ip) throws IOException, GeoIp2Exception {
        return reader.city(InetAddress.getByName(ip)).getMostSpecificSubdivision().getNames().get("zh-CN");
    }
    //经纬度
    public static Point getPoint(String ip) throws IOException, GeoIp2Exception {
        Double longitude=reader.city(InetAddress.getByName(ip)).getLocation().getLongitude();
        Double latitude=reader.city(InetAddress.getByName(ip)).getLocation().getLatitude();
        Point point=new Point(longitude,latitude);
        return point;
    }
}
