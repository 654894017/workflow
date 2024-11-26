package com.damon.workflow.condition_parser;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.utils.StrUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@RequiredArgsConstructor
@Slf4j
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
        StringBuilder lockIdentifier = new StringBuilder("workflow_lock:");
        lockIdentifier.append(processIdentifier).append(":").append(currentStateId);
        if (StrUtils.isEmpty(context.getBusinessId())) {
            String errorMessage = String.format(
                    "Process ID: %s, Current State: %s, the RedissionSafeConditionParser requires an associated Business ID to be provided.",
                    processIdentifier, currentStateId
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
