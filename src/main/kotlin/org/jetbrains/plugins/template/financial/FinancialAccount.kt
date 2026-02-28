package org.jetbrains.plugins.template.financial

/**
 * Represents a financial account with a total balance and committed (reserved) funds.
 * Free usable funds = balance - committed.
 */
data class FinancialAccount(
    val id: String,
    val name: String,
    val balance: Double,
    val committed: Double = 0.0
) {
    val freeUsableFunds: Double
        get() = balance - committed
}
