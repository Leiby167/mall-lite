package com.example.malllite.controller;

import com.example.malllite.annotation.RequirePermission;
import com.example.malllite.common.BaseResponse;
import com.example.malllite.common.PageResponse;
import com.example.malllite.common.ResultUtils;
import com.example.malllite.dto.StockRequest;
import com.example.malllite.request.ProductAddRequest;
import com.example.malllite.request.ProductQueryRequest;
import com.example.malllite.request.ProductUpdateRequest;
import com.example.malllite.service.ProductService;
import com.example.malllite.vo.ProductVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public BaseResponse<ProductVO> getById(@PathVariable("id") Long id) {
        return ResultUtils.success(productService.getProductById(id));
    }

    @GetMapping("/page")
    public BaseResponse<PageResponse<ProductVO>> pageProduct(@Valid ProductQueryRequest request) {
        return ResultUtils.success(productService.pageProduct(request));
    }

    @RequirePermission(permissionKey = "product:add")
    @PostMapping("/add")
    public BaseResponse<Boolean> addProduct(@RequestBody @Valid ProductAddRequest request) {
        return ResultUtils.success(productService.addProduct(request));
    }

    @RequirePermission(permissionKey = "product:update")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateProduct(@RequestBody @Valid ProductUpdateRequest request) {
        return ResultUtils.success(productService.updateProduct(request));
    }

    @RequirePermission(permissionKey = "product:delete")
    @DeleteMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteProduct(@PathVariable Long id) {
        return ResultUtils.success(productService.deleteProduct(id));
    }

    @PostMapping("/inner/decrease-stock")
    public BaseResponse<Boolean> decreaseStock(@RequestBody StockRequest request) {
        productService.decreaseStock(request.getProductId(), request.getQuantity());
        return ResultUtils.success(true);
    }

    @PostMapping("/inner/increase-stock")
    public BaseResponse<Boolean> increaseStock(@RequestBody StockRequest request) {
        productService.increaseStock(request.getProductId(), request.getQuantity());
        return ResultUtils.success(true);
    }
}