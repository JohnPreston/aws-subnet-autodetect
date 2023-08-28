// SPDX -License-Identifier: Apache-2.0
// Copyright 2023 John Mille

package software.ews.awsSubnetAutoDetect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ECSMetadataReader {
    public static @NotNull String getECSMetadata() throws IOException {
        String v3Url = System.getenv("ECS_CONTAINER_METADATA_URI");
        String v4Url = System.getenv("ECS_CONTAINER_METADATA_URI_V4");

        // If V4 url is not null, use it, else use v3
        URL url = v4Url != null ? new URL(v4Url) : new URL(v3Url);

        // If V4 url is null, check if V3 url is null
        if (v4Url == null && v3Url == null) {
            throw new IOException("Unable to retrieve ECS metadata url");
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    @org.jetbrains.annotations.Nullable
    public static String getSubnetCidrFromMetadata(String metadata) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(metadata);

            // From ECS Metadata, if networks list is set, use the first one and retrieve the IPv4SubnetCIDRBlock string value
            if (jsonObject != null && jsonObject.has("Networks")) {

                return jsonObject.getJSONArray("Networks").getJSONObject(0).getString("IPv4SubnetCIDRBlock");
            }
        } catch (Exception e) {
            System.out.println("Failed to get Networks from ECS Metadata");
        }
        return null;
    }
}
