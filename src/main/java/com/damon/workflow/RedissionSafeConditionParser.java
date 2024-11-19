package com.damon.workflow;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@RequiredArgsConstructor
public abstract class RedissionSafeConditionParser implements IConditionParser {

    private final RedissonClient redissonClient;

    @Override
    public boolean test(RuntimeContext context) {
        String lockIdentifier = createLockIdentifier(context);
        lock(lockIdentifier);
        try {
            return doTest(context);
        } finally {
            unlock(lockIdentifier);
        }
    }

    private String createLockIdentifier(RuntimeContext context) {
        String processIdentifier = context.getProcessDefinition().getIdentifier();
        String currentStateId = context.getCurrentState().getId();
        return "workflow_lock_" + processIdentifier + "_" + currentStateId;
    }


    private void lock(String lockName) {
        RLock lock = this.redissonClient.getLock(lockName);
        lock.lock();
    }

    private void unlock(String lockName) {
        RLock lock = this.redissonClient.getLock(lockName);
        if (lock != null) {
            lock.forceUnlock();
        }
    }

    public abstract boolean doTest(RuntimeContext context);

}
