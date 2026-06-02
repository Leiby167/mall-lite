package com.example.malllite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.malllite.common.BaseResponse;
import com.example.malllite.common.ErrorCode;
import com.example.malllite.common.LoginUserHolder;
import com.example.malllite.common.PageResponse;
import com.example.malllite.dto.OrderMessageDTO;
import com.example.malllite.dto.StockRequest;
import com.example.malllite.entity.OrderInfo;
import com.example.malllite.entity.OrderItem;
import com.example.malllite.exception.BusinessException;
import com.example.malllite.feign.ProductFeignClient;
import com.example.malllite.mapper.OrderInfoMapper;
import com.example.malllite.mapper.OrderItemMapper;
import com.example.malllite.mq.OrderMessageProducer;
import com.example.malllite.request.OrderCreateItemRequest;
import com.example.malllite.request.OrderCreateRequest;
import com.example.malllite.request.OrderQueryRequest;
import com.example.malllite.service.OrderService;
import com.example.malllite.vo.OrderItemVO;
import com.example.malllite.vo.OrderVO;
import com.example.malllite.vo.ProductVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderServiceImpl implements OrderService {

    private static final int STATUS_WAIT_PAY = 0;
    private static final int STATUS_PAID = 1;
    private static final int STATUS_SHIPPED = 2;
    private static final int STATUS_COMPLETED = 3;
    private static final int STATUS_CANCELED = 4;

    private final OrderInfoMapper orderInfoMapper;

    private final OrderItemMapper orderItemMapper;

    private final OrderMessageProducer orderMessageProducer;

    private final ProductFeignClient productFeignClient;

    public OrderServiceImpl(OrderInfoMapper orderInfoMapper,
                            OrderItemMapper orderItemMapper,
                            ProductFeignClient productFeignClient,
                            OrderMessageProducer orderMessageProducer) {
        this.orderInfoMapper = orderInfoMapper;
        this.orderItemMapper = orderItemMapper;
        this.productFeignClient = productFeignClient;
        this.orderMessageProducer = orderMessageProducer;
    }

    @Override
    @Transactional
    public OrderVO createOrder(OrderCreateRequest request) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单商品不能为空");
        }

        List<OrderItem> orderItemList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderCreateItemRequest itemRequest : request.getItems()) {
            if (itemRequest == null || itemRequest.getProductId() == null || itemRequest.getProductId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID不合法");
            }

            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "购买数量不合法");
            }

            ProductVO product = getProductFromRemote(itemRequest.getProductId());

            BigDecimal itemTotalPrice = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getProductName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setTotalPrice(itemTotalPrice);

            orderItemList.add(orderItem);
            totalAmount = totalAmount.add(itemTotalPrice);
        }

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderNo(generateOrderNo(userId));
        orderInfo.setUserId(userId);
        orderInfo.setTotalAmount(totalAmount);
        orderInfo.setStatus(STATUS_WAIT_PAY);

        int orderRows = orderInfoMapper.insert(orderInfo);
        if (orderRows <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建订单失败");
        }

        for (OrderItem orderItem : orderItemList) {
            decreaseStockByRemote(orderItem.getProductId(), orderItem.getQuantity());

            orderItem.setOrderId(orderInfo.getId());

            int itemRows = orderItemMapper.insert(orderItem);
            if (itemRows <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建订单明细失败");
            }
        }

        OrderMessageDTO message = new OrderMessageDTO();
        message.setOrderId(orderInfo.getId());
        message.setUserId(userId);
        message.setTotalAmount(orderInfo.getTotalAmount());
        message.setEventType("ORDER_CREATED");
        message.setCreateTime(LocalDateTime.now());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderMessageProducer.sendOrderCreatedMessage(message);
            }
        });

        return toOrderVO(orderInfo);
    }

    @Override
    public PageResponse<OrderVO> pageMyOrders(OrderQueryRequest request) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        if (request == null) {
            request = new OrderQueryRequest();
        }

        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getUserId, userId);

        if (StringUtils.hasText(request.getOrderNo())) {
            queryWrapper.like(OrderInfo::getOrderNo, request.getOrderNo().trim());
        }

        if (request.getStatus() != null) {
            queryWrapper.eq(OrderInfo::getStatus, request.getStatus());
        }

        queryWrapper.orderByDesc(OrderInfo::getId);

        Page<OrderInfo> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<OrderInfo> orderPage = orderInfoMapper.selectPage(page, queryWrapper);

        List<OrderVO> records = new ArrayList<>();
        if (orderPage.getRecords() != null) {
            for (OrderInfo orderInfo : orderPage.getRecords()) {
                records.add(toOrderVO(orderInfo));
            }
        }

        return new PageResponse<>(
                records,
                orderPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public OrderVO getOrderById(Long id) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID不合法");
        }

        OrderInfo orderInfo = orderInfoMapper.selectById(id);
        if (orderInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        if (!orderInfo.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "无权查看该订单");
        }

        return toOrderVO(orderInfo);
    }

    @Override
    @Transactional
    public Boolean cancelOrder(Long id) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID不合法");
        }

        OrderInfo orderInfo = orderInfoMapper.selectById(id);
        if (orderInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        if (!orderInfo.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "无权操作该订单");
        }

        if (orderInfo.getStatus() == null || orderInfo.getStatus() != STATUS_WAIT_PAY) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只有待支付订单可以取消");
        }

        LambdaQueryWrapper<OrderItem> itemQueryWrapper = new LambdaQueryWrapper<>();
        itemQueryWrapper.eq(OrderItem::getOrderId, id);
        List<OrderItem> orderItems = orderItemMapper.selectList(itemQueryWrapper);

        for (OrderItem item : orderItems) {
            increaseStockByRemote(item.getProductId(), item.getQuantity());
        }

        OrderInfo updateOrder = new OrderInfo();
        updateOrder.setId(id);
        updateOrder.setStatus(STATUS_CANCELED);
        updateOrder.setCancelTime(LocalDateTime.now());

        int rows = orderInfoMapper.updateById(updateOrder);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "取消订单失败");
        }

        return true;
    }

    @Override
    @Transactional
    public Boolean payOrder(Long id) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID不合法");
        }

        OrderInfo orderInfo = orderInfoMapper.selectById(id);
        if (orderInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        if (!orderInfo.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "无权操作该订单");
        }

        if (orderInfo.getStatus() == null || orderInfo.getStatus() != STATUS_WAIT_PAY) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只有待支付订单可以支付");
        }

        OrderInfo updateOrder = new OrderInfo();
        updateOrder.setId(id);
        updateOrder.setStatus(STATUS_PAID);
        updateOrder.setPayTime(LocalDateTime.now());

        int rows = orderInfoMapper.updateById(updateOrder);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "支付订单失败");
        }

        return true;
    }

    @Override
    public PageResponse<OrderVO> pageAllOrders(OrderQueryRequest request) {
        if (request == null) {
            request = new OrderQueryRequest();
        }

        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();

        if (request.getUserId() != null && request.getUserId() > 0) {
            queryWrapper.eq(OrderInfo::getUserId, request.getUserId());
        }

        if (StringUtils.hasText(request.getOrderNo())) {
            queryWrapper.like(OrderInfo::getOrderNo, request.getOrderNo().trim());
        }

        if (request.getStatus() != null) {
            queryWrapper.eq(OrderInfo::getStatus, request.getStatus());
        }

        queryWrapper.orderByDesc(OrderInfo::getId);

        Page<OrderInfo> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<OrderInfo> orderPage = orderInfoMapper.selectPage(page, queryWrapper);

        List<OrderVO> records = new ArrayList<>();
        if (orderPage.getRecords() != null) {
            for (OrderInfo orderInfo : orderPage.getRecords()) {
                records.add(toOrderVO(orderInfo));
            }
        }

        return new PageResponse<>(
                records,
                orderPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    @Transactional
    public Boolean shipOrder(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID不合法");
        }

        OrderInfo orderInfo = orderInfoMapper.selectById(id);
        if (orderInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        if (orderInfo.getStatus() == null || orderInfo.getStatus() != STATUS_PAID) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只有已支付订单可以发货");
        }

        OrderInfo updateOrder = new OrderInfo();
        updateOrder.setId(id);
        updateOrder.setStatus(STATUS_SHIPPED);
        updateOrder.setShipTime(LocalDateTime.now());

        int rows = orderInfoMapper.updateById(updateOrder);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "订单发货失败");
        }

        return true;
    }

    @Override
    @Transactional
    public Boolean completeOrder(Long id) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID不合法");
        }

        OrderInfo orderInfo = orderInfoMapper.selectById(id);
        if (orderInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        if (!orderInfo.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "无权操作该订单");
        }

        if (orderInfo.getStatus() == null || orderInfo.getStatus() != STATUS_SHIPPED) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只有已发货订单可以确认收货");
        }

        OrderInfo updateOrder = new OrderInfo();
        updateOrder.setId(id);
        updateOrder.setStatus(STATUS_COMPLETED);
        updateOrder.setCompleteTime(LocalDateTime.now());

        int rows = orderInfoMapper.updateById(updateOrder);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "确认收货失败");
        }

        return true;
    }

    @Override
    @Transactional
    public Integer cancelTimeoutOrders(Integer timeoutMinutes, Integer batchSize) {
        if (timeoutMinutes == null || timeoutMinutes <= 0) {
            timeoutMinutes = 30;
        }

        if (batchSize == null || batchSize <= 0) {
            batchSize = 100;
        }

        LocalDateTime deadlineTime = LocalDateTime.now().minusMinutes(timeoutMinutes);

        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getStatus, STATUS_WAIT_PAY)
                .le(OrderInfo::getCreateTime, deadlineTime)
                .orderByAsc(OrderInfo::getId)
                .last("LIMIT " + batchSize);

        List<OrderInfo> timeoutOrders = orderInfoMapper.selectList(queryWrapper);
        if (timeoutOrders == null || timeoutOrders.isEmpty()) {
            return 0;
        }

        int cancelCount = 0;

        for (OrderInfo orderInfo : timeoutOrders) {
            LambdaQueryWrapper<OrderItem> itemQueryWrapper = new LambdaQueryWrapper<>();
            itemQueryWrapper.eq(OrderItem::getOrderId, orderInfo.getId());
            List<OrderItem> orderItems = orderItemMapper.selectList(itemQueryWrapper);

            LambdaUpdateWrapper<OrderInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(OrderInfo::getId, orderInfo.getId())
                    .eq(OrderInfo::getStatus, STATUS_WAIT_PAY)
                    .set(OrderInfo::getStatus, STATUS_CANCELED)
                    .set(OrderInfo::getCancelTime, LocalDateTime.now());

            int rows = orderInfoMapper.update(null, updateWrapper);

            if (rows > 0) {
                for (OrderItem item : orderItems) {
                    increaseStockByRemote(item.getProductId(), item.getQuantity());
                }
                cancelCount++;
            }
        }

        return cancelCount;
    }

    private ProductVO getProductFromRemote(Long productId) {
        BaseResponse<ProductVO> response = productFeignClient.getProductDetail(productId);
        if (response == null || response.getCode() != 0 || response.getData() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "商品信息获取失败");
        }
        return response.getData();
    }

    private void decreaseStockByRemote(Long productId, Integer quantity) {
        StockRequest stockRequest = new StockRequest();
        stockRequest.setProductId(productId);
        stockRequest.setQuantity(quantity);

        BaseResponse<Boolean> response = productFeignClient.decreaseStock(stockRequest);
        if (response == null || response.getCode() != 0 || !Boolean.TRUE.equals(response.getData())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "库存扣减失败");
        }
    }

    private void increaseStockByRemote(Long productId, Integer quantity) {
        StockRequest stockRequest = new StockRequest();
        stockRequest.setProductId(productId);
        stockRequest.setQuantity(quantity);

        BaseResponse<Boolean> response = productFeignClient.increaseStock(stockRequest);
        if (response == null || response.getCode() != 0 || !Boolean.TRUE.equals(response.getData())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "库存恢复失败");
        }
    }

    private OrderVO toOrderVO(OrderInfo orderInfo) {
        OrderVO orderVO = new OrderVO();
        orderVO.setId(orderInfo.getId());
        orderVO.setOrderNo(orderInfo.getOrderNo());
        orderVO.setUserId(orderInfo.getUserId());
        orderVO.setTotalAmount(orderInfo.getTotalAmount());
        orderVO.setStatus(orderInfo.getStatus());
        orderVO.setStatusText(getStatusText(orderInfo.getStatus()));
        orderVO.setCreateTime(orderInfo.getCreateTime());

        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getOrderId, orderInfo.getId());

        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);
        List<OrderItemVO> itemVOList = new ArrayList<>();

        for (OrderItem item : orderItems) {
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setProductId(item.getProductId());
            itemVO.setProductName(item.getProductName());
            itemVO.setProductPrice(item.getProductPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setTotalPrice(item.getTotalPrice());
            itemVOList.add(itemVO);
        }

        orderVO.setItems(itemVOList);
        return orderVO;
    }

    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }

        if (status == 0) {
            return "待支付";
        }

        if (status == 1) {
            return "已支付";
        }

        if (status == 2) {
            return "已发货";
        }

        if (status == 3) {
            return "已完成";
        }

        if (status == 4) {
            return "已取消";
        }

        return "未知";
    }

    private String generateOrderNo(Long userId) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "M" + time + userId % 10000 + random;
    }
}
