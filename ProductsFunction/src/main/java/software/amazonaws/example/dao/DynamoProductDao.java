// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazonaws.example.entity.Product;
import software.amazonaws.example.entity.Products;

import java.util.*;


public class DynamoProductDao implements ProductDao {
    private static final Logger logger = LogManager.getLogger(DynamoProductDao.class);
    private static final String PRODUCT_TABLE_NAME = System.getenv("PRODUCT_TABLE_NAME");

    private final DynamoDbAsyncClient dynamoDbClient = DynamoDbAsyncClient.builder()
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .httpClient(AwsCrtAsyncHttpClient.builder().maxConcurrency(50).build())
            .build();

    @Override
    public Optional<Product> getProduct(String id) {

        try {
            GetItemResponse getItemResponse = dynamoDbClient.getItem(GetItemRequest.builder()
                    .key(Map.of("PK", AttributeValue.builder().s(id).build()))
                    .tableName(PRODUCT_TABLE_NAME)
                    .build()).get();

            if(id.equals("ForceError")) {
                // This simulates an error response from DynamoDb
                throw new RuntimeException("BOOM");
            }

            if (getItemResponse.hasItem()) {
                return Optional.of(ProductMapper.productFromDynamoDB(getItemResponse.item()));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error in Get item - " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putProduct(Product product) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(PRODUCT_TABLE_NAME)
                .item(ProductMapper.productToDynamoDb(product))
                .build());
    }


    @Override
    public String createProduct(Product product) {
        String productId = UUID.randomUUID().toString();
        product.setId(productId);
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(PRODUCT_TABLE_NAME)
                .item(ProductMapper.productToDynamoDb(product))
                .build());
        return productId;
    }

    @Override
    public void deleteProduct(String id) {
        dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                .tableName(PRODUCT_TABLE_NAME)
                .key(Map.of("PK", AttributeValue.builder().s(id).build()))
                .build());
    }

    @Override
    public Products getAllProduct() {
        try {
            ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder()
                    .tableName(PRODUCT_TABLE_NAME)
                    .limit(20)
                    .build()).get();

            logger.info("Scan returned: {} item(s)", scanResponse.count());

            List<Product> productList = new ArrayList<>();

            for (Map<String, AttributeValue> item : scanResponse.items()) {
                productList.add(ProductMapper.productFromDynamoDB(item));
            }

            return new Products(productList);
        } catch (Exception e) {
            logger.error("Error in Scan items - " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
