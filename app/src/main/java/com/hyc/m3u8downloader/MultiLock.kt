package com.hyc.m3u8downloader

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.AbstractQueuedSynchronizer
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock

class MultiLock(max: Int) : Lock {
    private var sync: Sync = Sync(max)
    private var threadCount = 1
    override fun lock() {
        sync.acquireShared(threadCount)
    }

    override fun tryLock(): Boolean {
        throw UnsupportedOperationException("please use lock")
    }

    override fun tryLock(time: Long, unit: TimeUnit?): Boolean {
        throw UnsupportedOperationException("please use lock")
    }

    override fun unlock() {
        sync.releaseShared(threadCount)
    }

    fun getLiveCount() = sync.getLiveState()

    fun changeState(size: Int) {
        sync.changeState(size)
    }

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

        fun changeState(size: Int) {
            while (true) {
                val current = state
                val newCount = current + size
                if (compareAndSetState(current, newCount)) {
                    return
                }
            }
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