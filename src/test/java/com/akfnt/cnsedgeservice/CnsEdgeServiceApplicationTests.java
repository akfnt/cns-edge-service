package com.akfnt.cnsedgeservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
class CnsEdgeServiceApplicationTests {
	private static final int REDIS_PORT = 6379;

	@Container
	static GenericContainer<?> redis = new GenericContainer<>((DockerImageName.parse("redis:7.0")))
			.withExposedPorts(REDIS_PORT);

	// 테스트 인스턴스를 사용하도록 레디스 설정 변경
	@DynamicPropertySource
	static void redisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.redis.host", () -> redis.getHost());
		registry.add("spring.redis.port", () -> redis.getMappedPort(REDIS_PORT));
	}

	// 애플리케이션 콘텍스트가 올바르게 로드되었는지, 레디스 연결이 성공적으로 됐는지를 확인하기 위한 테스트로서 내부는 비어있다
	@Test
	void verifyThatSpringContextLoads() {}
}
