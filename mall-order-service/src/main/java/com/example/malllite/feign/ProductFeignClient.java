package com.example.malllite.feign;

import com.example.malllite.common.BaseResponse;
import com.example.malllite.dto.StockRequest;
import com.example.malllite.vo.ProductVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mall-product-service")
public interface ProductFeignClient {

    @GetMapping("/product/{id}")
    BaseResponse<ProductVO> getProductDetail(@PathVariable("id") Long id);

    @PostMapping("/product/inner/decrease-stock")
    BaseResponse<Boolean> decreaseStock(@RequestBody StockRequest request);

    @PostMapping("/product/inner/increase-stock")
    BaseResponse<Boolean> increaseStock(@RequestBody StockRequest request);
}
