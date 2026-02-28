package org.jetbrains.plugins.template.toolWindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import org.jetbrains.plugins.template.MyBundle
import org.jetbrains.plugins.template.financial.FinancialService
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel


class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow()
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow {

        private val financialService = ApplicationManager.getApplication().getService(FinancialService::class.java)

        fun getContent(): JPanel {
            val panel = JBPanel<JBPanel<*>>(GridLayout(0, 1, 4, 4))
            panel.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

            val totalLabel = JBLabel(formatTotal())
            val committedLabel = JBLabel(formatCommitted())
            val freeLabel = JBLabel(formatFree())

            panel.add(JBLabel(MyBundle.message("financialDashboardTitle")))
            panel.add(totalLabel)
            panel.add(committedLabel)
            panel.add(freeLabel)

            panel.add(JButton(MyBundle.message("financialRefresh")).apply {
                addActionListener {
                    totalLabel.text = formatTotal()
                    committedLabel.text = formatCommitted()
                    freeLabel.text = formatFree()
                }
            })

            return panel
        }

        private fun formatTotal() =
            MyBundle.message("financialTotalBalance", "%.2f".format(financialService.getTotalBalance()))

        private fun formatCommitted() =
            MyBundle.message("financialCommitted", "%.2f".format(financialService.getTotalCommitted()))

        private fun formatFree() =
            MyBundle.message("financialFreeUsable", "%.2f".format(financialService.getFreeUsableFunds()))
    }
}
