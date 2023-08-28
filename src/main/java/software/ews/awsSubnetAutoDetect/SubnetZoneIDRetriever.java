// SPDX -License-Identifier: Apache-2.0
// Copyright 2023 John Mille

package software.ews.awsSubnetAutoDetect;

import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeSubnetsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeSubnetsResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Subnet;

public class SubnetZoneIDRetriever {

    public static @Nullable Subnet findSubnetFromCidr(String ipCidrBlock) {
        // Retrieves the Subnet from the CIDR
        Ec2Client ec2Client = Ec2Client.builder()
                .build();

        DescribeSubnetsRequest describeSubnetsRequest = DescribeSubnetsRequest.builder()
                .filters(Filter.builder().name("cidrBlock").values(ipCidrBlock).build())
                .build();
        try {
            DescribeSubnetsResponse describeSubnetsResponse = ec2Client.describeSubnets(describeSubnetsRequest);
            if (!describeSubnetsResponse.subnets().isEmpty()) {
                return describeSubnetsResponse.subnets().get(0);// Assuming there's only one subnet matching the CIDR matching the CIDR
            }

        } finally {
            ec2Client.close();
        }
        return null;
    }

    public static @Nullable Subnet describeSubnet(String subnetId) {
        // Retrieves the Subnet from the ID
        Ec2Client ec2Client = Ec2Client.builder()
                .build();

        DescribeSubnetsRequest describeSubnetsRequest = DescribeSubnetsRequest.builder()
                .subnetIds(subnetId)
                .build();

        try {
            DescribeSubnetsResponse describeSubnetsResponse = ec2Client.describeSubnets(describeSubnetsRequest);
            if (!describeSubnetsResponse.subnets().isEmpty()) {
                return describeSubnetsResponse.subnets().get(0); // Assuming there's only one subnet matching the ID
            }
        } finally {
            ec2Client.close();
        }
        return null;

    }
}
