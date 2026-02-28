package org.jetbrains.plugins.template.financial

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger

/**
 * Application-level service that autonomously manages financial accounts and tracks free usable funds.
 *
 * "Free usable funds" are calculated as total balance minus committed (reserved) amounts across all accounts.
 */
@Service(Service.Level.APP)
class FinancialService {

    private val accounts = mutableListOf(
        FinancialAccount("acc-1", "Operating Account", balance = 50_000.0, committed = 12_000.0),
        FinancialAccount("acc-2", "Reserve Fund", balance = 30_000.0, committed = 5_000.0),
        FinancialAccount("acc-3", "Investment Pool", balance = 20_000.0, committed = 8_000.0)
    )

    /** Returns a snapshot of all accounts. */
    fun getAccounts(): List<FinancialAccount> = accounts.toList()

    /** Total balance across all accounts. */
    fun getTotalBalance(): Double = accounts.sumOf { it.balance }

    /** Total committed (reserved) funds across all accounts. */
    fun getTotalCommitted(): Double = accounts.sumOf { it.committed }

    /** Free usable funds = total balance minus total committed. */
    fun getFreeUsableFunds(): Double = getTotalBalance() - getTotalCommitted()

    /**
     * Adds or updates an account.
     * If an account with the same id already exists it is replaced; otherwise the account is appended.
     */
    fun upsertAccount(account: FinancialAccount) {
        val index = accounts.indexOfFirst { it.id == account.id }
        if (index >= 0) {
            accounts[index] = account
        } else {
            accounts.add(account)
        }
        thisLogger().info("FinancialService: upserted account '${account.name}', freeUsableFunds=${account.freeUsableFunds}")
        autonomousRebalance()
    }

    /**
     * Commits (reserves) an amount from the account with the given id.
     * Returns true if the commitment was applied successfully (sufficient free funds available), false otherwise.
     */
    fun commitFunds(accountId: String, amount: Double): Boolean {
        val index = accounts.indexOfFirst { it.id == accountId }
        if (index < 0) return false
        val account = accounts[index]
        if (account.freeUsableFunds < amount) {
            thisLogger().warn("FinancialService: insufficient free funds in '${account.name}' to commit $amount")
            return false
        }
        accounts[index] = account.copy(committed = account.committed + amount)
        thisLogger().info("FinancialService: committed $amount from '${account.name}'")
        autonomousRebalance()
        return true
    }

    /**
     * Releases previously committed funds back to free status.
     */
    fun releaseFunds(accountId: String, amount: Double): Boolean {
        val index = accounts.indexOfFirst { it.id == accountId }
        if (index < 0) return false
        val account = accounts[index]
        val newCommitted = (account.committed - amount).coerceAtLeast(0.0)
        accounts[index] = account.copy(committed = newCommitted)
        thisLogger().info("FinancialService: released $amount from '${account.name}'")
        autonomousRebalance()
        return true
    }

    /**
     * Autonomous rebalancing triggered automatically after every account mutation (upsert, commit, release).
     * Logs a warning when any account's free usable funds fall below 10 % of its total balance.
     * Extend this method to trigger automated fund transfers, notifications, or other corrective actions
     * as part of the autonomous fund management strategy.
     */
    private fun autonomousRebalance() {
        accounts.forEach { account ->
            val threshold = account.balance * 0.10
            if (account.freeUsableFunds < threshold) {
                thisLogger().warn(
                    "FinancialService [autonomous]: '${account.name}' free usable funds (${account.freeUsableFunds}) " +
                        "are below 10% threshold ($threshold). Consider rebalancing."
                )
            }
        }
    }
}
