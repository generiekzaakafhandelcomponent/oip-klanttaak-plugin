/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

val kotlinLoggingVersion: String by project
val mockitoKotlinVersion: String by project

dockerCompose {
    setProjectName("oip-klanttaak")
    isRequiredBy(project.tasks.test)

    tasks.test {
        useComposeFiles.addAll("$rootDir/docker-resources/docker-compose-base-test.yml")
    }
}

dependencies {
    compileOnly("com.ritense.valtimo:contract")
    compileOnly("com.ritense.valtimo:case")
    compileOnly("com.ritense.valtimo:core")
    compileOnly("com.ritense.valtimo:notificaties-api")
    compileOnly("com.ritense.valtimo:objecten-api")
    compileOnly("com.ritense.valtimo:objecttypen-api")
    compileOnly("com.ritense.valtimo:object-management")
    compileOnly("com.ritense.valtimo:notificaties-api")
    compileOnly("com.ritense.valtimo:plugin-valtimo")
    compileOnly("com.ritense.valtimo:process-document")
    compileOnly("com.ritense.valtimo:value-resolver")
    compileOnly("com.ritense.valtimo:zaken-api")

    // Logging
    compileOnly("io.github.oshai:kotlin-logging:$kotlinLoggingVersion")

    // Testing
    testImplementation("com.ritense.valtimo:building-block")
    testImplementation("com.ritense.valtimo:contract")
    testImplementation("com.ritense.valtimo:core")
    testImplementation("com.ritense.valtimo:plugin")
    testImplementation("com.ritense.valtimo:temporary-resource-storage")
    testImplementation("com.ritense.valtimo:test-utils-common")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation("org.postgresql:postgresql")

    testImplementation("com.ritense.valtimo:case")
    testImplementation("com.ritense.valtimo:notificaties-api")
    testImplementation("com.ritense.valtimo:objecten-api")
    testImplementation("com.ritense.valtimo:objecttypen-api")
    testImplementation("com.ritense.valtimo:object-management")
    testImplementation("com.ritense.valtimo:notificaties-api")
    testImplementation("com.ritense.valtimo:plugin-valtimo")
    testImplementation("com.ritense.valtimo:process-document")
    testImplementation("com.ritense.valtimo:value-resolver")
    testImplementation("com.ritense.valtimo:zaken-api")

    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

apply(from = "gradle/publishing.gradle")
