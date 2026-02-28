package org.jetbrains.plugins.template.financial

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class FinancialServiceTest : BasePlatformTestCase() {

    private lateinit var service: FinancialService

    override fun setUp() {
        super.setUp()
        service = FinancialService()
    }

    fun testInitialAccountsLoaded() {
        val accounts = service.getAccounts()
        assertTrue("Should have default accounts", accounts.isNotEmpty())
    }

    fun testFreeUsableFundsCalculation() {
        val accounts = service.getAccounts()
        val expectedFree = accounts.sumOf { it.balance } - accounts.sumOf { it.committed }
        assertEquals(expectedFree, service.getFreeUsableFunds(), 0.001)
    }

    fun testTotalBalanceCalculation() {
        val accounts = service.getAccounts()
        val expected = accounts.sumOf { it.balance }
        assertEquals(expected, service.getTotalBalance(), 0.001)
    }

    fun testTotalCommittedCalculation() {
        val accounts = service.getAccounts()
        val expected = accounts.sumOf { it.committed }
        assertEquals(expected, service.getTotalCommitted(), 0.001)
    }

    fun testCommitFundsReducesFreeUsable() {
        val before = service.getFreeUsableFunds()
        val accountId = service.getAccounts().first().id
        val result = service.commitFunds(accountId, 100.0)
        assertTrue("Commit should succeed", result)
        assertEquals(before - 100.0, service.getFreeUsableFunds(), 0.001)
    }

    fun testCommitFundsFailsWhenInsufficientFunds() {
        val account = service.getAccounts().first()
        val result = service.commitFunds(account.id, account.freeUsableFunds + 1.0)
        assertFalse("Commit should fail when funds are insufficient", result)
    }

    fun testReleaseFundsIncreaseFreeUsable() {
        val accountId = service.getAccounts().first().id
        service.commitFunds(accountId, 500.0)
        val before = service.getFreeUsableFunds()
        service.releaseFunds(accountId, 200.0)
        assertEquals(before + 200.0, service.getFreeUsableFunds(), 0.001)
    }

    fun testUpsertNewAccount() {
        val countBefore = service.getAccounts().size
        service.upsertAccount(FinancialAccount("acc-new", "New Account", 10_000.0, 1_000.0))
        assertEquals(countBefore + 1, service.getAccounts().size)
        assertEquals(9_000.0, service.getAccounts().last().freeUsableFunds, 0.001)
    }

    fun testUpsertExistingAccountUpdatesBalance() {
        val account = service.getAccounts().first()
        val updated = account.copy(balance = account.balance + 5_000.0)
        service.upsertAccount(updated)
        val found = service.getAccounts().first { it.id == account.id }
        assertEquals(account.balance + 5_000.0, found.balance, 0.001)
    }

    fun testFinancialAccountFreeUsableFunds() {
        val account = FinancialAccount("x", "Test", 1_000.0, 300.0)
        assertEquals(700.0, account.freeUsableFunds, 0.001)
    }
}
