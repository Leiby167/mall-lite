package com.example.malllite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.malllite.common.ErrorCode;
import com.example.malllite.common.PageResponse;
import com.example.malllite.entity.Product;
import com.example.malllite.exception.BusinessException;
import com.example.malllite.mapper.ProductMapper;
import com.example.malllite.request.ProductAddRequest;
import com.example.malllite.request.ProductQueryRequest;
import com.example.malllite.request.ProductUpdateRequest;
import com.example.malllite.service.ProductService;
import com.example.malllite.vo.ProductVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private static final String PRODUCT_DETAIL_CACHE_KEY_PREFIX = "mall-lite:product:detail:";

    private static final long PRODUCT_DETAIL_CACHE_TTL_MINUTES = 30L;

    private final ProductMapper productMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public ProductServiceImpl(ProductMapper productMapper,
                              StringRedisTemplate stringRedisTemplate) {
        this.productMapper = productMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public ProductVO getProductById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID不合法");
        }

        String cacheKey = getProductDetailCacheKey(id);

        try {
            String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cacheValue)) {
                ProductVO productVO = objectMapper.readValue(cacheValue, ProductVO.class);
                log.info("商品详情缓存命中，productId={}", id);
                return productVO;
            }
        } catch (Exception e) {
            log.warn("读取商品详情缓存失败，productId={}", id, e);
        }

        Product product = getProductEntityById(id);
        ProductVO productVO = toProductVO(product);

        try {
            String json = objectMapper.writeValueAsString(productVO);
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    json,
                    PRODUCT_DETAIL_CACHE_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
            log.info("商品详情写入缓存，productId={}", id);
        } catch (Exception e) {
            log.warn("写入商品详情缓存失败，productId={}", id, e);
        }

        return productVO;
    }

    @Override
    public Product getProductEntityById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID不合法");
        }

        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }

        return product;
    }

    @Override
    public PageResponse<ProductVO> pageProduct(ProductQueryRequest request) {
        if (request == null) {
            request = new ProductQueryRequest();
        }

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getProductName())) {
            queryWrapper.like(Product::getProductName, request.getProductName().trim());
        }

        if (request.getStatus() != null) {
            queryWrapper.eq(Product::getStatus, request.getStatus());
        }

        queryWrapper.orderByDesc(Product::getId);

        Page<Product> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<Product> productPage = productMapper.selectPage(page, queryWrapper);

        List<ProductVO> records = new ArrayList<>();
        if (productPage.getRecords() != null) {
            for (Product product : productPage.getRecords()) {
                records.add(toProductVO(product));
            }
        }

        return new PageResponse<>(
                records,
                productPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public Boolean addProduct(ProductAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        validateProductName(request.getProductName());
        validatePrice(request.getPrice());
        validateStock(request.getStock());

        Integer status = request.getStatus() == null ? 1 : request.getStatus();
        validateStatus(status);

        Product product = new Product();
        product.setProductName(request.getProductName().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setStatus(status);

        int rows = productMapper.insert(product);
        return rows > 0;
    }

    @Override
    public Boolean updateProduct(ProductUpdateRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID不能为空");
        }

        Product oldProduct = productMapper.selectById(request.getId());
        if (oldProduct == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }

        Product product = new Product();
        product.setId(request.getId());

        if (StringUtils.hasText(request.getProductName())) {
            product.setProductName(request.getProductName().trim());
        }

        if (request.getPrice() != null) {
            validatePrice(request.getPrice());
            product.setPrice(request.getPrice());
        }

        if (request.getStock() != null) {
            validateStock(request.getStock());
            product.setStock(request.getStock());
        }

        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
            product.setStatus(request.getStatus());
        }

        int rows = productMapper.updateById(product);

        if (rows > 0) {
            deleteProductDetailCache(request.getId());
        }

        return rows > 0;
    }

    @Override
    public Boolean deleteProduct(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID不合法");
        }

        Product oldProduct = productMapper.selectById(id);
        if (oldProduct == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }

        Product product = new Product();
        product.setId(id);
        product.setStatus(0);

        int rows = productMapper.updateById(product);

        if (rows > 0) {
            deleteProductDetailCache(id);
        }

        return rows > 0;
    }

    @Override
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID不合法");
        }

        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "扣减数量不合法");
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }

        if (product.getStatus() == null || product.getStatus() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品已下架");
        }

        LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Product::getId, productId)
                .eq(Product::getStatus, 1)
                .ge(Product::getStock, quantity)
                .setSql("stock = stock - " + quantity);

        int rows = productMapper.update(null, updateWrapper);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品库存不足");
        }

        deleteProductDetailCache(productId);
    }

    @Override
    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID不合法");
        }

        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "恢复数量不合法");
        }

        LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Product::getId, productId)
                .setSql("stock = stock + " + quantity);

        int rows = productMapper.update(null, updateWrapper);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "恢复库存失败");
        }

        deleteProductDetailCache(productId);
    }

    private void validateProductName(String productName) {
        if (!StringUtils.hasText(productName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品名称不能为空");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品价格必须大于0");
        }
    }

    private void validateStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "库存不能小于0");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态不合法");
        }
    }

    private ProductVO toProductVO(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setProductName(product.getProductName());
        vo.setPrice(product.getPrice());
        vo.setStock(product.getStock());
        vo.setStatus(product.getStatus());
        vo.setCreateTime(product.getCreateTime());

        if (product.getStatus() != null) {
            vo.setStatusText(product.getStatus() == 1 ? "上架" : "下架");
        }

        return vo;
    }

    private String getProductDetailCacheKey(Long productId) {
        return PRODUCT_DETAIL_CACHE_KEY_PREFIX + productId;
    }

    private void deleteProductDetailCache(Long productId) {
        if (productId == null || productId <= 0) {
            return;
        }

        String cacheKey = getProductDetailCacheKey(productId);

        try {
            stringRedisTemplate.delete(cacheKey);
            log.info("删除商品详情缓存，productId={}", productId);
        } catch (Exception e) {
            log.warn("删除商品详情缓存失败，productId={}", productId, e);
        }
    }
}