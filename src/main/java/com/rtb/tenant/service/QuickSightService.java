package com.rtb.tenant.service;

import com.rtb.core.entity.tenant.QuickSightDashboard;
import com.rtb.core.repository.QuickSightDashboardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.quicksight.QuickSightClient;
import software.amazon.awssdk.services.quicksight.model.GetDashboardEmbedUrlRequest;
import software.amazon.awssdk.services.quicksight.model.GetDashboardEmbedUrlResponse;
import software.amazon.awssdk.services.quicksight.model.QuickSightException;
import java.util.List;
import org.springframework.cache.CacheManager;
import java.util.Optional;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class QuickSightService {

    private static final Logger logger = LoggerFactory.getLogger(QuickSightService.class);

    @Value("${aws.access-key-id}")
    private String accessKeyId;

    @Value("${aws.secret-access-key}")
    private String secretAccessKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.account-id}")
    private String awsAccountId;

    private final AtomicBoolean isFirstRequest = new AtomicBoolean(true);
    private String subsequentUrl;
    private final CacheManager cacheManager;
    private final QuickSightDashboardRepository quickSightDashboardRepository;

    public QuickSightService(CacheManager cacheManager,
                             QuickSightDashboardRepository quickSightDashboardRepository) {
        this.cacheManager = cacheManager;
        this.quickSightDashboardRepository = quickSightDashboardRepository;
    }

    /**
     * Fetches or generates the embed URL. The first request generates a new URL, while
     * subsequent requests return a predefined static URL without query parameters.
     */
    public String generateEmbedUrl(String categoryName, String reportName) {
        // Extract JWT token from SecurityContext
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("JWT token is not available in the security context.");
        }
        String jwtToken = null;

        if (authentication != null) {
            // Get the JWT from the credentials
            Jwt jwt = (Jwt) authentication.getCredentials();

            // Retrieve the token value (JWT string)
            jwtToken = jwt.getTokenValue();
        }


        // Check cache for an existing entry for this JWT token
        String cachedUrl = cacheManager
                .getCache("embedUrlCache")
                .get(jwtToken, String.class);



        // Find QuickSight Dashboard based on categoryName and reportName
        Optional<QuickSightDashboard> quickSightDashboard =
                quickSightDashboardRepository.findByCategoryAndReportName(categoryName, reportName);

        if (quickSightDashboard.isPresent()) {
            // Record exists, proceed to generate the embed URL
            logger.info("Record found for categoryName: {} and dashboardId: {}", categoryName,
                    quickSightDashboard.get().getDashboardId().toString());

            if (cachedUrl != null) {
                logger.info("Returning cached embed URL for JWT token: {}", jwtToken);
                return cachedUrl + "/" + quickSightDashboard.get()
                        .getDashboardId().toString();
            }

            String embedUrl;
                logger.info("Generating first-time embed URL");
                embedUrl = fetchEmbedUrl(quickSightDashboard.get());
                subsequentUrl = extractBaseUrl(embedUrl);

                // Store the subsequent URL and the JWT token in the cache
                cacheManager.getCache("embedUrlCache").put(jwtToken, subsequentUrl);
            return embedUrl;
        } else {
            // Record does not exist
            logger.warn(
                    "No record found for categoryName: {} "
                            + "and Report: {}", categoryName, reportName);
            throw new RuntimeException("Dashboard with the provided category and ID not found.");
        }
    }


    /**
     * Extracts the base URL by removing query parameters from the given URL.
     */
    private String extractBaseUrl(String url) {
        try {
            URL fullUrl = new URL(url);
            String path = fullUrl.getPath();

            // Find the index of "/dashboards" in the path
            int dashboardsIndex = path.indexOf("/dashboards");

            // If "/dashboards" is present, truncate the path to include only up to "/dashboards"
            if (dashboardsIndex != -1) {
                path = path.substring(0, dashboardsIndex + "/dashboards".length());
            }

            // Construct the base URL
            String baseUrl = fullUrl.getProtocol() + "://" + fullUrl.getHost() + path;

            logger.info("Extracted base URL: {}", baseUrl);
            return baseUrl;
        } catch (IOException e) {
            logger.error("Error extracting base URL: {}", e.getMessage());
            throw new RuntimeException("Failed to extract base URL");
        }
    }


    private String fetchEmbedUrl(QuickSightDashboard quickSightDashboard) {
        AwsBasicCredentials awsCredentials =
                AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        try (QuickSightClient QUICKSIGHTCLIENT = QuickSightClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build()) {

            GetDashboardEmbedUrlRequest request = GetDashboardEmbedUrlRequest.builder()
                    .awsAccountId(awsAccountId)
                    .dashboardId(quickSightDashboard.getDashboardId().toString())
                    .identityType("IAM")
                    .sessionLifetimeInMinutes(600L) // 10 hours
                    .build();

            GetDashboardEmbedUrlResponse response =
                    QUICKSIGHTCLIENT.getDashboardEmbedUrl(request);
            quickSightDashboard.setAuthenticatedUrl(response.embedUrl());
            quickSightDashboardRepository.save(quickSightDashboard);
            return response.embedUrl();
        } catch (QuickSightException e) {
            logger.error("Error generating embed URL: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to generate embed URL");
        }
    }

    public List<QuickSightDashboard> getAllReports() {
        // Fetch all QuickSightDashboard objects from the database
        return quickSightDashboardRepository.findAll();
    }
}