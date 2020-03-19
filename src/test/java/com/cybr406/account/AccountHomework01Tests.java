package com.cybr406.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpStatusCodeException;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.cybr406.account.util.TestUtil.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * In this homework assignment you will add simple security to the user application using HTTP basic authentication.
 * you will...
 *
 *     * Create a security config class
 *     * Configure the authentication manager to use the database
 *     * Configure access based on http method and url patterns
 *     * Configure csrf & session
 *
 * An admin user is pre-built for you and can be found in src/main/resources/db/changelog/data
 * username: admin
 * password: admin
 *
 * The database changelog already contains change sets for creating the default Spring Security tables for users.
 *
 * As you work, refer to the books demo as a point of comparison:
 * https://github.com/ryl/cybr406-books-demo
 *
 * The SecurityConfiguration class should be especially helpful:
 * https://github.com/ryl/cybr406-books-demo/blob/master/src/main/java/com/cybr406/bookdemo/SecurityConfiguration.java
 */
class AccountHomework01Tests {

	@Nested
	@SpringBootTest
	@AutoConfigureMockMvc
	public class Part_01_ConfigurationTests {

		@Autowired
		private MockMvc mockMvc;

		@Autowired
		private ObjectMapper objectMapper;

		/**
		 * Problem 01: create a security config class.
		 *
		 * Create a new class called SecurityConfiguration that extends WebSecurityConfigurerAdapter.
		 *
		 * WebSecurityConfigurerAdapter is a base class that is meant to be extended. It contains the vast majority of
		 * methods related to configuring security in you application.
		 */
		@Test
		public void problem_01_createSecurityConfigurationClass() {
			assertClassExists("com.cybr406.account.configuration.SecurityConfiguration");
			assertClassExtends(
					"com.cybr406.account.configuration.SecurityConfiguration",
					WebSecurityConfigurerAdapter.class);
			assertClassAnnotationIsPresent(
					"com.cybr406.account.configuration.SecurityConfiguration",
					Configuration.class);
		}

		/**
		 * Problem 02: configure the authentication manager
		 *
		 * There are numerous strategies for authenticating users: in-memory, database, and more.
		 *
		 * For our application we would like to be able to store and retrieve users from the database. Use the
		 * jdbcAuthentication() method on the AuthenticationManagerBuilder and supply a datasource.
		 */
		@Test
		public void problem_02_configureAuthenticationManager() throws Exception {
			// The Datasource contains information about the connection to your database. You will need it @Autowired
			// in to use when configuring the authentication manager.
			Field datasourceField = assertFieldExists(
					"Add the field \"@Autowried Datasource dataSource;\" to SecurityConfiguration",
					"com.cybr406.account.configuration.SecurityConfiguration",
					DataSource.class,
					"dataSource");
			assertFieldAnnotationExists(
					"Be sure to add @Autowired to the dataSource field",
					datasourceField,
					Autowired.class);
			assertClassDeclaresMethod(
					"You must implement the void configureGlobal(AuthenticationManagerBuilder auth) method",
					"com.cybr406.account.configuration.SecurityConfiguration",
					"configureGlobal",
					AuthenticationManagerBuilder.class);
			assertMethodAnnotationIsPresent(
					"Add @Autowired to the configureGlobal method",
					"com.cybr406.account.configuration.SecurityConfiguration",
					Autowired.class,
					"configureGlobal",
					AuthenticationManagerBuilder.class);
		}

		/**
		 * Problem 03: override the configure method
		 *
		 * The configure(HttpSecurity http) method in WebSecurityConfigurerAdapter is one of the most important methods
		 * when configuring security. Override it from WebSecurityConfigurerAdapter.
		 *
		 * The next problems will focus on filling in this method.
		 */
		@Test
		public void problem_03_overrideConfigureHttpSecurity() throws Exception {
			assertClassDeclaresMethod(
					"You must override the configure(HttpSecurity http) method in WebSecurityConfigurerAdapter",
					"com.cybr406.account.configuration.SecurityConfiguration",
					"configure",
					HttpSecurity.class);
		}

		/**
		 * Problem 04: allow get requests from anon users
		 *
		 * Inside configure(HttpSecurity http), use the http object's configuration methods to make GET requests
		 * to any URL accessible to everyone without requiring a username & password.
		 *
		 * Some config commands you will use:
		 *     .authorizeRequests()
		 * 	   .mvcMatchers()
		 * 	   .permitAll()
		 */
		@Test
		public void problem_04_allowGETRequestsFromAnonymousUsers() throws Exception {
			mockMvc.perform(get("/"))
					.andExpect(status().isOk());
			mockMvc.perform(get("/profiles"))
					.andExpect(status().isOk());
		}

		/**
		 * Problem 05: disallow all other requests from anonymous users
		 *
		 * Any request that is not a GET should require a username and password.
		 *
		 * Inside configure(HttpSecurity http) you need to add:
		 *     .anyRequest()
		 *     .authenticated()
		 */
		@Test
		public void problem_05_disallowAllOtherHttpMethodsForAnonymousUsers() throws Exception {
			Map<String, Object> post = new HashMap<>();
			post.put("username", "test");
			post.put("firstName", "Test");
			post.put("lastName", "Testerton");

			mockMvc.perform(post("/profiles")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(post)))
					.andExpect(status().is4xxClientError());
		}

		/**
		 * Problem 06: allow all other requests from authenticated users
		 *
		 * Before any POST, PATCH, or PUT methods will work, you must disable CSRF protection. This protection
		 * works by injecting a secret field into forms that sites outside our own can't guess. However, for an API
		 * without any HTML forms, this protection can result in unwanted access denied messages.
		 *
		 * Since we want to supply a username and password using http basic, we need to also turn on that feature.
		 *
		 * Inside configure(HttpSecurity http) you need to add:
		 * 		.csrf().disable()
		 * 	    .httpBasic()
		 */
		@Test
		public void problem_06_allowAllOtherHttpMethodsForAuthenticatedUsers() throws Exception {
			Map<String, Object> post = new HashMap<>();
			post.put("username", "test");
			post.put("firstName", "Test");
			post.put("lastName", "Testerton");

			mockMvc.perform(post("/profiles")
					.contentType(MediaType.APPLICATION_JSON)
					.with(httpBasic("admin", "admin"))
					.content(objectMapper.writeValueAsString(post)))
					.andExpect(status().isCreated());
		}
	}

	@Nested
	@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
	public class Part_02_SessionTests {

		@LocalServerPort
		int port;

		/**
		 * Problem 07: disable sessions
		 *
		 * Session tracking via cookies is what makes a CSRF attack possible in the first place. Disabling sessions
		 * in our security settings can help mitigate the problem.
		 *
		 * Inside configure(HttpSecurity http) you need to add:
		 *     .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		 */
		@Test
		public void problem_07_disableSessions(@Autowired TestRestTemplate restTemplate) throws Exception {
			Map<String, Object> post = new HashMap<>();
			post.put("username", "test");
			post.put("firstName", "Test");
			post.put("lastName", "Testerton");

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			httpHeaders.setBasicAuth("admin", "admin");

			try {
				ResponseEntity<String> response = restTemplate.exchange(
						"http://localhost:" + port + "/profiles",
						HttpMethod.POST,
						new HttpEntity<>(post, httpHeaders),
						String.class);

				Assertions.assertFalse(
						response.getHeaders().containsKey("Set-Cookie"),
						"You must set session creation policy to STATELESS");
			} catch (HttpStatusCodeException e) {
				fail("Ensure authenticated users can POST (in problem 6) before attempting this problem.");
			}
		}

	}
}
