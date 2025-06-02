package com.damon.workflow.conditionparser;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.StateIdentifier;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.utils.StrUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class RedissionSafeConditionParser implements IConditionParser {
    private final Logger log = LoggerFactory.getLogger(RedissionSafeConditionParser.class);

    private final RedissonClient redissonClient;

    public RedissionSafeConditionParser(RedissonClient redissonClient) {
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
        StateIdentifier currentStateIdentifier = context.getCurrentStateIdentifier();
        StringBuilder lockIdentifier = new StringBuilder("workflow_lock:");
        lockIdentifier.append(currentStateIdentifier.getFullPaths());
        if (StrUtils.isEmpty(context.getBusinessId())) {
            String errorMessage = String.format(
                    "Process ID: %s, Current State: %s, the RedissionSafeConditionParser requires an associated Business ID to be provided.",
                    currentStateIdentifier.getCurrentStateProcessIdentifier(), currentStateIdentifier.getCurrentStateId()
            );
            throw new ProcessException(errorMessage);
        }
        lockIdentifier.append(":").append(context.getBusinessId());
        return lockIdentifier.toString();
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
