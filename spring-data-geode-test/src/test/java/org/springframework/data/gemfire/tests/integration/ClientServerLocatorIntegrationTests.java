package org.springframework.data.gemfire.tests.integration;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.LocatorApplication;
import org.springframework.data.gemfire.tests.integration.config.ClientServerIntegrationTestsConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ClientServerIntegrationTests.TestGeodeClientConfiguration.class)
public class ClientServerLocatorIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	private static final String GEODE_LOG_LEVEL = "error";

	@BeforeClass
	public static void startGeodeServer() throws IOException {
		startGemFireLocator(TestGeodeLocatorConfiguration.class);
		startGemFireServer(TestGeodeServerConfiguration.class);
	}

	@Resource(name = "Example")
	private Region<Object, Object> example;

	@Test
	public void clientServerInteractionSuccessful() {

		assertThat(this.example.put(1L, "test")).isNull();
		assertThat(this.example.get(1L)).isEqualTo("test");
	}

	@ClientCacheApplication(logLevel = GEODE_LOG_LEVEL, locators = {})
	//@Import(ClientServerIntegrationTestsConfiguration.class)
	static class TestGeodeClientConfiguration extends ClientServerIntegrationTestsConfiguration {

		@Bean("Example")
		public ClientRegionFactoryBean<Object, Object> exampleRegion(GemFireCache gemfireCache) {

			ClientRegionFactoryBean<Object, Object> exampleRegion = new ClientRegionFactoryBean<>();

			exampleRegion.setCache(gemfireCache);
			exampleRegion.setClose(false);
			exampleRegion.setShortcut(ClientRegionShortcut.PROXY);

			return exampleRegion;
		}
	}

	@CacheServerApplication(name = "ClientServerLocatorIntegrationTests", logLevel = GEODE_LOG_LEVEL)
	static class TestGeodeServerConfiguration {

		public static void main(String[] args) {

			AnnotationConfigApplicationContext applicationContext =
					new AnnotationConfigApplicationContext(ClientServerIntegrationTests.TestGeodeServerConfiguration.class);

			applicationContext.registerShutdownHook();
		}

		@Bean("Example")
		public PartitionedRegionFactoryBean<Object, Object> exampleRegion(GemFireCache gemfireCache) {

			PartitionedRegionFactoryBean<Object, Object> exampleRegion = new PartitionedRegionFactoryBean<>();

			exampleRegion.setCache(gemfireCache);
			exampleRegion.setClose(false);
			exampleRegion.setPersistent(false);

			return exampleRegion;
		}
	}

	@LocatorApplication(logLevel = GEODE_LOG_LEVEL)
	static class TestGeodeLocatorConfiguration {

	}
}
