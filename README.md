## aws-subnet-detector

This small library aims to automatically detect and retrieve the EC2 Subnet the EC2 Instance or ECS Container runs onto.
This will allow for further automation to help the client be AWS Zone aware and potentially chose to talk to other
services in the same Zone to reduce network traffic costs.


### Logic

First, it will try to retrieve the Subnet ID based on the EC2 Metadata, if available. This is done by getting the
subnet ID from `/meta-data/latest/network/interfaces/macs/<MAC>/subnet-id`

Otherwise, it will try to retrieve the Subnet ID from the EC2 Metadata, using the Metadata version 4. This works for
both EC2 and Fargate hosts.

Then with the subnet ID, simply makes the EC2DescribeSubnets API call, using the Subnet ID, and retrieve all the details,
returning the `software.amazon.awssdk.services.ec2.model` `Subnet` model.

### Where to use?

I wrote this to enable my Kafka Consumers to be aware of the Subnet **ZoneID** which is the same across all AWS Accounts.
AWS MSK uses that ZoneID in the brokers `rack.id` property. Setting that property on the client and enable follow-fetcher
will improve client election process to use the "nearest" available broker and avoid, when possible, cross AZ traffic.


### IAM Requirements

For the DescribeSubnets to work, you need to grant `ec2:DescribeSubnets` on resource `*`.
If you want to restrict it further, see [EC2 Conditions](https://docs.aws.amazon.com/service-authorization/latest/reference/list_amazonec2.html)

Below an example policy that allows to describe Subnets only in `eu-west-1` region.

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Condition": {
                "StringEquals": {
                    "ec2:Region": "eu-west-1"
                }
            },
            "Action": [
                "ec2:DescribeSubnets"
            ],
            "Resource": [
                "*"
            ],
            "Effect": "Allow",
            "Sid": "GrantVpcSubnetsDescribeAccess"
        }
    ]
}
```

For an EC2 Instance, the permissions must be set on the Instance Profile.
For an ECS Service, it must be set on the Task IAM Role (not the execution one).


Feel free to contribute. Be nice, this is my first using Java.
