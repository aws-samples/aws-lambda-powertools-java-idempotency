// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.dao;

import software.amazonaws.example.entity.Product;
import software.amazonaws.example.entity.Products;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;

import java.util.Optional;

public interface ProductDao {
    @Logging
    @Tracing
    Optional<Product> getProduct(String id);

    @Logging
    @Tracing
    void putProduct(Product product);

    @Logging
    @Tracing
    void deleteProduct(String id);

    @Logging
    @Tracing
    Products getAllProduct();

    @Logging
    @Tracing
    String createProduct(Product product);
}
