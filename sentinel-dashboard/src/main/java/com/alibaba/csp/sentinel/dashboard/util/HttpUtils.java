package com.alibaba.csp.sentinel.dashboard.util;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description:http/https
 * @author Lyle
 * @date 2019-06-11
 */
public class HttpUtils {

	private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	
	private static CloseableHttpClient httpClient;

	private static final int TIME_OUT = 30 * 1000;
	private static PoolingHttpClientConnectionManager cm;

	static {
		try {
			init();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private HttpUtils() {

	}

	public static void init() throws Exception {
		X509TrustManager xtm = new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[] {};
			}
		};
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, new TrustManager[] { xtm }, null);
		SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE);
		RequestConfig defaultConfig = RequestConfig.custom().setSocketTimeout(TIME_OUT).setConnectTimeout(TIME_OUT)
				.setConnectionRequestTimeout(TIME_OUT).setCookieSpec(CookieSpecs.STANDARD_STRICT)
				.setExpectContinueEnabled(false)
				.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
				.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
		Registry<ConnectionSocketFactory> sfr = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", scsf).build();
		cm = new PoolingHttpClientConnectionManager(sfr);
		cm.setMaxTotal(500);
		cm.setDefaultMaxPerRoute(100);
		httpClient = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(defaultConfig).build();
	}
	
	public static String get(String url) throws Exception {
		HttpGet httpget = new HttpGet(url);
		
		HttpResponse response = httpClient.execute(httpget);
		
		return handleResult(response);
	}
	
	public static HttpResponse get(String url, Header... header) throws Exception {
		HttpGet httpget = new HttpGet(url);
		
		if (header != null) {
			httpget.setHeaders(header);
		}

		return httpClient.execute(httpget);
	}

	public static String post(String url, String json) throws Exception {
		HttpPost httppost = new HttpPost(url);

		httppost.setHeader("Content-Type", "application/json; charset=utf-8");
		
		HttpEntity requestEntity = new StringEntity(json, "utf-8");
		httppost.setEntity(requestEntity);
		
		HttpResponse response = httpClient.execute(httppost);

		return handleResult(response);

	}
	
	public static String post(String url, String json, Map<String, String> headparameters) throws Exception {
		HttpPost httppost = new HttpPost(url);
		
		httppost.setHeader("Content-Type", "application/json; charset=utf-8");
		if (headparameters != null) {
			for (Entry<String, String> headparameter : headparameters.entrySet()) {
				httppost.setHeader(headparameter.getKey(), headparameter.getValue());
			}
		}
		
		HttpEntity requestEntity = new StringEntity(json, "utf-8");
		httppost.setEntity(requestEntity);
		
		HttpResponse response = httpClient.execute(httppost);
		
		return handleResult(response);
		
	}

	/**
	 * 发送form表单
	 * @param url
	 * @param nvps
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse post(String url, List<NameValuePair> nvps)
			throws Exception {
		HttpPost httppost = new HttpPost(url);
		
		httppost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		
		if (null != nvps)
			httppost.setEntity(new UrlEncodedFormEntity(nvps, "UTF8"));
		
		return httpClient.execute(httppost);
	}
	
	public static String post(String url, List<NameValuePair> nvps, Header... header)
			throws Exception {
		HttpPost httppost = new HttpPost(url);
		
		httppost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		if (header != null) {
			httppost.setHeaders(header);
		}
		
		if (null != nvps)
			httppost.setEntity(new UrlEncodedFormEntity(nvps, "UTF8"));
		
		HttpResponse response = httpClient.execute(httppost);
		
		return handleResult(response);
	}
	
	public static String put(String url, String json) throws Exception {
		HttpPut httppost = new HttpPut(url);

		httppost.setHeader("Content-Type", "application/json; charset=utf-8");
		
		HttpEntity requestEntity = new StringEntity(json, "utf-8");
		httppost.setEntity(requestEntity);
		
		HttpResponse response = httpClient.execute(httppost);
		
		return handleResult(response);
	}
	
	public static String delete(String url) throws Exception {
		HttpDelete httpdelete = new HttpDelete(url);
		
		HttpResponse response = httpClient.execute(httpdelete);
		
		return handleResult(response);
	}
	
	private static String handleResult(HttpResponse response) throws Exception {
		if (response != null) {
			HttpEntity entity = response.getEntity();
			int resStatu = response.getStatusLine().getStatusCode();
			String html = EntityUtils.toString(entity);

			if (resStatu != HttpStatus.SC_OK) {
				logger.info("status code : {}, result :{}", resStatu, html);
			}

			return html;
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(delete("https://www.baidu.com/"));
	}
	
}