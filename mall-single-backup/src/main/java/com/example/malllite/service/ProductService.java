package com.example.malllite.service;

import com.example.malllite.common.PageResponse;
import com.example.malllite.entity.Product;
import com.example.malllite.request.ProductAddRequest;
import com.example.malllite.request.ProductQueryRequest;
import com.example.malllite.request.ProductUpdateRequest;
import com.example.malllite.vo.ProductVO;

public interface ProductService {

    ProductVO getProductById(Long id);

    Product getProductEntityById(Long id);

    PageResponse<ProductVO> pageProduct(ProductQueryRequest request);

    Boolean addProduct(ProductAddRequest request);

    Boolean updateProduct(ProductUpdateRequest request);

    Boolean deleteProduct(Long id);

    void decreaseStock(Long productId, Integer quantity);

    void increaseStock(Long productId, Integer quantity);
}