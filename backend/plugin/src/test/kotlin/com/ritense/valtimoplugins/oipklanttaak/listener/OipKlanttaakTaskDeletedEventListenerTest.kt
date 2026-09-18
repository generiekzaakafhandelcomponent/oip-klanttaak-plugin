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

import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.service.PluginService
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.event.OperatonTaskEvent
import com.ritense.valtimoplugins.oipklanttaak.ProcessVariables
import com.ritense.valtimoplugins.oipklanttaak.plugin.OipKlanttaakPlugin
import com.ritense.valtimoplugins.oipklanttaak.service.OipKlanttaakService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.operaton.bpm.engine.delegate.DelegateTask
import java.net.URI
import java.util.UUID

class OipKlanttaakTaskDeletedEventListenerTest {
    private lateinit var processLinkServiceMock: ProcessLinkService
    private lateinit var pluginServiceMock: PluginService
    private lateinit var oipKlanttaakServiceMock: OipKlanttaakService

    private lateinit var listener: OipKlanttaakTaskDeletedEventListener

    @BeforeEach
    fun setup() {
        processLinkServiceMock = mock()
        pluginServiceMock = mock()
        oipKlanttaakServiceMock = mock()

        listener =
            OipKlanttaakTaskDeletedEventListener(
                processLinkService = processLinkServiceMock,
                pluginService = pluginServiceMock,
                oipKlanttaakService = oipKlanttaakServiceMock,
            )
    }

    @Test
    fun `onTaskDeleted should withdraw delegated task when link and stamped url are present`() {
        // given
        val event = operatonTaskEvent()

        doReturn(listOf(delegateTaskProcessLink()))
            .whenever(processLinkServiceMock)
            .getProcessLinks(eq(processDefinitionId()), eq(taskDefinitionKey()))

        doReturn(oipKlanttaakPlugin())
            .whenever(pluginServiceMock)
            .createInstance<OipKlanttaakPlugin>(eq(pluginConfigurationId()))

        // when
        assertDoesNotThrow {
            listener.onTaskDeleted(event)
        }

        // then
        verify(oipKlanttaakServiceMock).withdrawDelegatedTask(
            objectManagementId = eq(objectManagementConfigurationId()),
            klanttaakObjectUrl = eq(objectUrl()),
        )
    }

    @Test
    fun `onTaskDeleted should do nothing when task is not bound to delegate-task`() {
        // given
        val event = operatonTaskEvent()

        doReturn(listOf<ProcessLink>(nonDelegateTaskProcessLink()))
            .whenever(processLinkServiceMock)
            .getProcessLinks(eq(processDefinitionId()), eq(taskDefinitionKey()))

        // when
        assertDoesNotThrow {
            listener.onTaskDeleted(event)
        }

        // then
        verifyNoInteractions(pluginServiceMock, oipKlanttaakServiceMock)
    }

    @Test
    fun `onTaskDeleted should do nothing when there are no process links`() {
        // given
        val event = operatonTaskEvent()

        doReturn(emptyList<ProcessLink>())
            .whenever(processLinkServiceMock)
            .getProcessLinks(eq(processDefinitionId()), eq(taskDefinitionKey()))

        // when
        assertDoesNotThrow {
            listener.onTaskDeleted(event)
        }

        // then
        verifyNoInteractions(pluginServiceMock, oipKlanttaakServiceMock)
    }

    @Test
    fun `onTaskDeleted should not withdraw when object url variable is missing`() {
        // given
        val event = operatonTaskEvent(objectUrl = null)

        doReturn(listOf(delegateTaskProcessLink()))
            .whenever(processLinkServiceMock)
            .getProcessLinks(eq(processDefinitionId()), eq(taskDefinitionKey()))

        // when
        assertDoesNotThrow {
            listener.onTaskDeleted(event)
        }

        // then
        verify(oipKlanttaakServiceMock, never()).withdrawDelegatedTask(any(), any())
        verifyNoInteractions(pluginServiceMock)
    }

    private fun operatonTaskEvent(objectUrl: String? = objectUrl().toString()): OperatonTaskEvent {
        val delegateTask =
            mock<DelegateTask> {
                on { id } doReturn taskId().toString()
                on { processDefinitionId } doReturn processDefinitionId()
                on { taskDefinitionKey } doReturn taskDefinitionKey()
                on { getVariable(eq(ProcessVariables.oipTaskObjectUrlKey(taskId().toString()))) } doReturn objectUrl
            }
        return mock<OperatonTaskEvent> {
            on { this.delegateTask } doReturn delegateTask
            on { eventName } doReturn "delete"
        }
    }

    private fun delegateTaskProcessLink() =
        mock<PluginProcessLink> {
            on { pluginActionDefinitionKey } doReturn "delegate-task"
            on { pluginConfigurationId } doReturn PluginConfigurationId.existingId(pluginConfigurationId())
        }

    private fun nonDelegateTaskProcessLink() =
        mock<PluginProcessLink> {
            on { pluginActionDefinitionKey } doReturn "complete-delegated-task"
        }

    private fun oipKlanttaakPlugin() =
        mock<OipKlanttaakPlugin> {
            on { objectManagementConfigurationId } doReturn objectManagementConfigurationId()
        }

    private fun taskId() = UUID.fromString("99ab6902-80a1-46f9-8c79-5c040ebd118f")

    private fun processDefinitionId() = "oip-procesflow:1:abc"

    private fun taskDefinitionKey() = "ut-oip-gegevens-aanleveren"

    private fun pluginConfigurationId() = UUID.fromString("5489f080-8355-4f28-a305-5ebd88ecdd22")

    private fun objectManagementConfigurationId() = UUID.fromString("135851f4-ace3-4e8c-832b-ec71b0a28352")

    private fun objectId() = UUID.fromString("fbf7f397-946f-4656-9cce-46d539ea43a7")

    private fun objectUrl() = URI.create("https://example.com/object/${objectId()}")
}
