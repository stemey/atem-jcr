package org.atemsource.jcr.entitytype;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.login.LoginException;

import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.api.ContentRepository;
import org.apache.jackrabbit.oak.jcr.repository.RepositoryImpl;
import org.apache.jackrabbit.oak.plugins.index.property.PropertyIndexProvider;
import org.apache.jackrabbit.oak.plugins.name.NameValidatorProvider;
import org.apache.jackrabbit.oak.plugins.nodetype.write.InitialContent;
import org.apache.jackrabbit.oak.spi.security.OpenSecurityProvider;
import org.apache.jackrabbit.oak.spi.security.SecurityProvider;
import org.apache.jackrabbit.oak.spi.whiteboard.DefaultWhiteboard;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public class AbstractJcrTest {

	private static RepositoryImpl repository;
	protected Session session;

	@BeforeClass
	public static void setup() throws LoginException, RepositoryException {
		SecurityProvider securityProvider = new OpenSecurityProvider();
		Oak oak = new Oak().with(new InitialContent()) // add initial content
				.with(new NameValidatorProvider()) // allow only valid JCR names
				.with(securityProvider) // use the default security
				.with(new PropertyIndexProvider());
		ContentRepository contentRepository = oak // search support for the
													// indexes
				.createContentRepository();
		repository = new RepositoryImpl(contentRepository,
				new DefaultWhiteboard(), securityProvider, 12, null);
		//Node rootNode = repository.login("default").getRootNode();
	
	}

	public AbstractJcrTest() {
		super();
	}

	@Before
	public void createSession() throws RepositoryException {
		session = repository.login("default");
	}

	@After
	public void closeSession() {
		session.logout();
	}

}