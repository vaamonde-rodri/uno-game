plugins {
	java
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.flywaydb.flyway") version "10.10.0"
	jacoco
}

group = "dev.rodrigovaamonde"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("com.h2database:h2")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
	dependsOn(tasks.test) // tests are required to run before generating the report
	reports {
		xml.required.set(true)
		html.required.set(true)
		csv.required.set(false)
	}

	classDirectories.setFrom(files(classDirectories.files.map {
		fileTree(it).apply {
			exclude(
				"**/config/**",
				"**/dto/**",
				"**/*Application*",
				"**/UnoServerApplication*"
			)
		}
	}))
}

tasks.jacocoTestCoverageVerification {
	violationRules {
		rule {
			limit {
				minimum = "0.80".toBigDecimal() // 80% de cobertura mínima
			}
		}
	}
}

// Configuración de Flyway usando variables de entorno
flyway {
	url = project.findProperty("flyway.url") as String? ?: System.getenv("FLYWAY_URL") ?: "jdbc:postgresql://localhost:5432/uno_game_db"
	user = project.findProperty("flyway.user") as String? ?: System.getenv("FLYWAY_USER") ?: "uno_user"
	password = project.findProperty("flyway.password") as String? ?: System.getenv("FLYWAY_PASSWORD") ?: "uno_password"
	locations = arrayOf("classpath:db/migration")
	baselineOnMigrate = true
	validateOnMigrate = true
	cleanDisabled = true
}
