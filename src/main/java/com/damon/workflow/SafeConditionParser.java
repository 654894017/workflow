package com.damon.workflow;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

public abstract class SafeConditionParser implements IConditionParser {

    private final RedissonClient redissonClient;

    protected SafeConditionParser(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

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
        String processId = context.getProcessDefinition().getId();
        String currentStateId = context.getCurrentState().getId();
        return "workflow_lock_" + processId + "_" + currentStateId;
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
