package com.team6.team6.question.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class InMemoryKeywordLockManagerTest {

    private InMemoryKeywordLockManager lockManager;

    @BeforeEach
    void setUp() {
        lockManager = new InMemoryKeywordLockManager();
    }

    @Test
    void try_lock_테스트() {
        // given
        String keyword = "LOL";

        // when
        boolean first = lockManager.tryLock(keyword);
        boolean second = lockManager.tryLock(keyword);

        // then
        assertSoftly(softly -> {
            softly.assertThat(first).isTrue();
            softly.assertThat(second).isFalse();
        });
    }

    @Test
    void unlock_후_다시_tryLock_시도_테스트() {
        // given
        String keyword = "LOL";

        // when
        boolean first = lockManager.tryLock(keyword);
        lockManager.unlock(keyword);
        boolean second = lockManager.tryLock(keyword);

        // then
        assertSoftly(softly -> {
            softly.assertThat(first).isTrue();
            softly.assertThat(second).isTrue();
        });
    }

    @Test
    void 다른_키워드_독립_잠금_테스트() {
        // given
        String keyword1 = "LOL";
        String keyword2 = "Netflix";

        // when
        boolean lock1 = lockManager.tryLock(keyword1);
        boolean lock2 = lockManager.tryLock(keyword2);

        // then
        assertSoftly(softly -> {
            softly.assertThat(lock1).isTrue();
            softly.assertThat(lock2).isTrue();
        });
    }

    @Test
    void tryLock_동시에_호출하면_하나는_성공_하나는_실패한다() throws InterruptedException, ExecutionException {
        // given
        String keyword = "LOL";
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Callable<Boolean> task = () -> {
            readyLatch.countDown();     // 두 스레드가 모두 준비될 때까지 대기
            startLatch.await();         // 동시에 시작
            return lockManager.tryLock(keyword);
        };

        // when
        Future<Boolean> result1 = executor.submit(task);
        Future<Boolean> result2 = executor.submit(task);

        // 두 스레드 준비될 때까지 대기 후 시작
        readyLatch.await();
        startLatch.countDown();

        // then
        boolean res1 = result1.get();
        boolean res2 = result2.get();
        assertThat(res1 ^ res2).isTrue(); // 하나만 true일 때 XOR 결과가 true

        executor.shutdown();
    }
}
