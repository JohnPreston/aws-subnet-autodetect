// SPDX -License-Identifier: Apache-2.0
// Copyright 2023 John Mille
package software.ews.awsSubnetAutoDetect;

import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.imds.Ec2MetadataClient;
import software.amazon.awssdk.imds.Ec2MetadataResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Subnet;


public class App {

    public static @Nullable String getSubnetIdFromEC2Metadata() {
        try {
            // If cannot retrieve EC2 Metadata, try EC2 Metadata
            Ec2MetadataClient client = Ec2MetadataClient.create();
            Ec2MetadataResponse macsResponse = client.get("/latest/meta-data/network/interfaces/macs/");
            String macAddress = macsResponse.asString();
            Ec2MetadataResponse response = client.get("/latest/meta-data/network/interfaces/macs/" + macAddress + "subnet-id");
            client.close();
            return response.asString();
        } catch (Exception imds) {
            System.err.println("Failed to retrieve EC2 Metadata");
            return null;
        }
    }

    public static @Nullable String getSubnetCidrString() {
        // Try to get the subnet CIDR from the ECS Metadata
        try {
            String metadata = ECSMetadataReader.getECSMetadata();
            return ECSMetadataReader.getSubnetCidrFromMetadata(metadata);
        } catch (Exception e) {
            System.err.println("Failed to retrieve Subnet CIDR based on ECS Metadata");
        }
        return null;
    }

    public static @Nullable Subnet getSubnet() {
        Subnet subnet = null;
        String subnetId = getSubnetIdFromEC2Metadata();
        // if subnetId is not null from EC2 Metadata, then describe the Subnet directly
        if (subnetId != null) {
            try {
                subnet = SubnetZoneIDRetriever.describeSubnet(subnetId);
            } catch (Ec2Exception e) {
                System.err.println(e.awsErrorDetails().errorMessage());
            }

        } else {
            // If EC2 Metadata is not available, try to get the subnet CIDR from the ECS Metadata
            try {
                String subnetCidr = getSubnetCidrString();
                try {
                    subnet = SubnetZoneIDRetriever.findSubnetFromCidr(subnetCidr);
                } catch (Ec2Exception e) {
                    System.err.println(e.awsErrorDetails().errorMessage());
                }

            } catch (Exception getSubnetException) {
                System.err.println("Failed to retrieve Subnet CIDR based on ECS Metadata");
            }
        }
        if (subnet == null) {
            System.err.println("Failed to detect subnet");
        }
        return subnet;
    }

    public static void main(String[] args) {
        // Main function to test the Subnet Zone ID Retriever in EC2 Instance/ECS Service
        Subnet subnet = getSubnet();
        if (subnet != null) {
            try {
                System.out.println(subnet.availabilityZoneId());
                System.exit(0);
            } catch (Exception e) {
                System.err.println("Interrupted");
            }
        }
    }
}
