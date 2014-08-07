package org.atemsource.jcr;

import javax.jcr.Repository;

import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.api.ContentRepository;
import org.apache.jackrabbit.oak.jcr.repository.RepositoryImpl;
import org.apache.jackrabbit.oak.plugins.index.property.PropertyIndexProvider;
import org.apache.jackrabbit.oak.plugins.name.NameValidatorProvider;
import org.apache.jackrabbit.oak.plugins.nodetype.write.InitialContent;
import org.apache.jackrabbit.oak.spi.security.OpenSecurityProvider;
import org.apache.jackrabbit.oak.spi.security.SecurityProvider;
import org.apache.jackrabbit.oak.spi.whiteboard.DefaultWhiteboard;
import org.springframework.beans.factory.FactoryBean;

public class InMemoryTestRepository implements FactoryBean<Repository> {

	
	

	@Override
	public Repository getObject() throws Exception {
		SecurityProvider securityProvider = new OpenSecurityProvider();
		Oak oak = new Oak().with(new InitialContent()) // add initial content
				.with(new NameValidatorProvider()) // allow only valid JCR names
				.with(securityProvider) // use the default security
				.with(new PropertyIndexProvider());
		ContentRepository contentRepository = oak // search support for the
													// indexes
				.createContentRepository();
		return new RepositoryImpl(contentRepository,
				new DefaultWhiteboard(), securityProvider, 12, null);
		//Node rootNode = repository.login("default").getRootNode();
	
	}

	@Override
	public Class<?> getObjectType() {
		return Repository.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
