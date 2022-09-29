// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.dao;

import software.amazonaws.example.entity.Product;
import software.amazonaws.example.entity.Products;

import java.util.Optional;

public interface ProductDao {
    Optional<Product> getProduct(String id);

    void putProduct(Product product);

    void deleteProduct(String id);

    Products getAllProduct();

    String createProduct(Product product);
}
