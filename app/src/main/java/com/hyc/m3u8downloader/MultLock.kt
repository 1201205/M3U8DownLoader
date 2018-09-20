package com.hyc.m3u8downloader

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.AbstractQueuedSynchronizer
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock

class MultLock(var max: Int) : Lock {
    var sync: Sync = Sync(max)
    override fun lock() {
        sync.acquireShared(1)
//        throw UnsupportedOperationException("please use tryLock")
    }

    override fun tryLock(): Boolean {
        throw UnsupportedOperationException("please use lock")
    }

    override fun tryLock(time: Long, unit: TimeUnit?): Boolean {
        throw UnsupportedOperationException("please use lock")
    }

    override fun unlock() {
        sync.releaseShared(1)
    }

    fun getLiveCount() = sync.getLiveState()

    override fun lockInterruptibly() {
        throw UnsupportedOperationException()
    }

    override fun newCondition(): Condition {
        throw UnsupportedOperationException()
    }

    class Sync(max: Int) : AbstractQueuedSynchronizer() {
        init {
            if (max <= 0) {
                throw IllegalArgumentException("max thread count must larger than 0")
            }
            state = max
        }

        fun getLiveState() = state
        public override fun tryAcquireShared(arg: Int): Int {
            var current = state
            var newCount = current - arg
            if (newCount >= 0) {
                if (compareAndSetState(current, newCount)) {
                    return newCount
                }
            }
            return -1
        }

        public override fun tryReleaseShared(arg: Int): Boolean {
            var current = state
            var newCount = current + arg
            if (compareAndSetState(current, newCount)) {
                return true
            }
            return false
        }

    }
}