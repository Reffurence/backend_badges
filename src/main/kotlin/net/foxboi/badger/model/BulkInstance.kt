package net.foxboi.badger.model

import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.model.dyn.ScopeStack

class BulkInstance(
    val bulk: Bulk,
    val entries: List<BulkEntry>
) {
    suspend inline fun iterateBatchEntries(
        stack: ScopeStack,
        assets: AssetManager,
        action: (
            index: Int,
            bulkEntry: BulkEntry,
            batch: Batch,
            name: String,
            entry: BatchEntry,
            stack: ScopeStack
        ) -> Unit
    ) {
        val localStack = ScopeStack()
        localStack.pushFront(bulk.scope)

        var index = 0
        for (bulkEntry in entries) {
            localStack.pushFront(bulkEntry.option.scope)
            localStack.pushFront(bulkEntry.params)

            stack.withBack(localStack) {
                val batch = bulkEntry.option.batch(stack, assets)

                for ((name, entry) in batch.entries) {
                    action(index, bulkEntry, batch, name, entry, stack)
                }
            }

            localStack.popFront()
            localStack.popFront()

            index++
        }
    }

    suspend inline fun iterateBatches(
        stack: ScopeStack,
        assets: AssetManager,
        action: (
            index: Int,
            bulkEntry: BulkEntry,
            batch: Batch,
            stack: ScopeStack
        ) -> Unit
    ) {
        val localStack = ScopeStack()
        localStack.pushFront(bulk.scope)

        var index = 0
        for (bulkEntry in entries) {
            localStack.pushFront(bulkEntry.option.scope)
            localStack.pushFront(bulkEntry.params)

            stack.withBack(localStack) {
                val batch = bulkEntry.option.batch(stack, assets)

                action(index, bulkEntry, batch, stack)
            }

            localStack.popFront()
            localStack.popFront()

            index++
        }
    }
}