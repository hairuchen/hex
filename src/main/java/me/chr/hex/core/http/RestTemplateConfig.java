//package me.chr.hex.core.http;
//
//
//import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
//import org.apache.hc.client5.http.impl.classic.HttpClients;
//import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
//import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
//import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
//import org.apache.hc.core5.ssl.SSLContexts;
//import org.apache.hc.core5.ssl.TrustStrategy;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.springframework.web.client.RestTemplate;
//
//import javax.net.ssl.SSLContext;
//
//@Configuration
//public class RestTemplateConfig {
//    @Bean
//    public RestTemplate restTemplate() {
//        try {
//            // 1. 构建信任策略：忽略所有 SSL 证书校验
//            TrustStrategy trustStrategy = (chain, authType) -> true;
//
//            // 2. 构建 SSL 上下文
//            SSLContext sslContext = SSLContexts.custom()
//                    .loadTrustMaterial(null, trustStrategy)
//                    .build();
//
//            // 3. 构建 SSL 连接工厂（HttpClient 5.x 写法）
//            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
//                    sslContext,
//                    NoopHostnameVerifier.INSTANCE // 忽略域名校验
//            );
//
//            // 4. 构建 HttpClient（HttpClient 5.x 写法）
//            CloseableHttpClient httpClient = HttpClients.custom()
//                    .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
//                            .setSSLSocketFactory(sslSocketFactory)
//                            .build())
//                    .build();
//
//            // 5. 构建请求工厂
//            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
//            factory.setConnectTimeout(5000);  // 连接超时 5 秒
//            factory.setReadTimeout(10000);    // 读取超时 10 秒
//
//            // 6. 创建 RestTemplate
//            return new RestTemplate(factory);
//        } catch (Exception e) {
//            throw new RuntimeException("创建忽略 SSL 的 RestTemplate 失败", e);
//        }
//    }
//}