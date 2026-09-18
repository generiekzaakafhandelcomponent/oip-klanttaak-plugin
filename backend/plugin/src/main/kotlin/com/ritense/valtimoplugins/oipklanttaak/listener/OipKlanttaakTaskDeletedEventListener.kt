/*
 * Copyright 2015-2026 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.valtimoplugins.oipklanttaak.listener

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.service.PluginService
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.event.OperatonTaskEvent
import com.ritense.valtimoplugins.oipklanttaak.ProcessVariables
import com.ritense.valtimoplugins.oipklanttaak.plugin.OipKlanttaakPlugin
import com.ritense.valtimoplugins.oipklanttaak.service.OipKlanttaakService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.transaction.annotation.Transactional
import java.net.URI

open class OipKlanttaakTaskDeletedEventListener(
    private val processLinkService: ProcessLinkService,
    private val pluginService: PluginService,
    private val oipKlanttaakService: OipKlanttaakService,
) {
    @Transactional
    @RunWithoutAuthorization
    @EventListener(
        condition = "#event.eventName == T(org.operaton.bpm.engine.delegate.TaskListener).EVENTNAME_DELETE",
    )
    open fun onTaskDeleted(event: OperatonTaskEvent) {
        val task = event.delegateTask

        val oipLink =
            processLinkService
                .getProcessLinks(task.processDefinitionId, task.taskDefinitionKey)
                .filterIsInstance<PluginProcessLink>()
                .firstOrNull { it.pluginActionDefinitionKey == DELEGATE_TASK_ACTION }
                ?: return

        val objectUrl = task.getVariable(ProcessVariables.oipTaskObjectUrlKey(task.id)) as? String
        if (objectUrl == null) {
            logger.warn {
                "Skipping withdraw for deleted Task(id=${task.id}): " +
                    "no '${ProcessVariables.oipTaskObjectUrlKey(task.id)}' variable found."
            }
            return
        }

        val pluginConfigurationId =
            requireNotNull(oipLink.pluginConfigurationId) {
                "Plugin configuration id is required for the delegate-task process link."
            }.id

        val oipKlanttaakPlugin = pluginService.createInstance<OipKlanttaakPlugin>(pluginConfigurationId)

        logger.info {
            "Deleted delegated Task(id=${task.id}) detected, withdrawing Klanttaak object with URL '$objectUrl'"
        }
        oipKlanttaakService.withdrawDelegatedTask(
            objectManagementId = oipKlanttaakPlugin.objectManagementConfigurationId,
            klanttaakObjectUrl = URI.create(objectUrl),
        )
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        private const val DELEGATE_TASK_ACTION = "delegate-task"
    }
}
