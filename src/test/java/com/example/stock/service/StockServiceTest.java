package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.facade.LettuceLockStockFacade;
import com.example.stock.facade.NamedLockStockFacade;
import com.example.stock.facade.RedissonLockStockFacade;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class StockServiceTest {
    @Autowired
//    private StockService stockService;
//    private PessimisticLockStockService stockService;
//    private OptimisticLockStockFacade stockService;
//    private NamedLockStockFacade stockService;
//    private LettuceLockStockFacade stockService;
    private RedissonLockStockFacade stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
//    public void 재고감소() {
    public void 재고감소() throws InterruptedException {
        stockService.decrease(1L, 1L);

        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertEquals(99, stock.getQuantity());
    }

    @Test
    public void 동시에_100의_요청() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i=0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertEquals(0, stock.getQuantity());
    }
}
