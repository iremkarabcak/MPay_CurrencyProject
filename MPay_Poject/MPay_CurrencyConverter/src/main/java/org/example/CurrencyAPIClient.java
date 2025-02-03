package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CurrencyAPIClient {

    private static final String BASE_URL = "https://restcountries.com/v3.1/alpha/";
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public CurrencyAPIClient() throws NoSuchAlgorithmException {
        this.client = createHttpClientWithSSL();
        this.objectMapper = new ObjectMapper();
    }

    private HttpClient createHttpClientWithSSL() throws NoSuchAlgorithmException {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }}, new java.security.SecureRandom());

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL Context", e);
        }
    }

    public List<String> getCurrenciesForCountries(List<String> countryCodes) throws IOException, InterruptedException {
        List<String> currencies = new ArrayList<>();

        for (String code : countryCodes) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + code))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<Map<String, Object>> countryData = objectMapper.readValue(response.body(), new TypeReference<>() {});
                if (!countryData.isEmpty() && countryData.get(0).containsKey("currencies")) {
                    Map<String, Object> currenciesMap = (Map<String, Object>) countryData.get(0).get("currencies");
                    currencies.addAll(currenciesMap.keySet());
                }
            } else {
                System.out.println("API request failed. Status code: " + code);
            }
        }
        return currencies;
    }

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
        CurrencyAPIClient apiClient = new CurrencyAPIClient();
        List<String> countryCodes = List.of("GB", "CH");
        List<String> currencies = apiClient.getCurrenciesForCountries(countryCodes);

        System.out.println("Currencies from API: " + currencies);
    }
}