package org.atemsource.jcr;

import javax.jcr.Repository;

import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.api.ContentRepository;
import org.apache.jackrabbit.oak.jcr.repository.RepositoryImpl;
import org.apache.jackrabbit.oak.kernel.KernelNodeStore;
import org.apache.jackrabbit.oak.plugins.document.DocumentMK;
import org.apache.jackrabbit.oak.plugins.document.util.MongoConnection;
import org.apache.jackrabbit.oak.plugins.index.property.PropertyIndexProvider;
import org.apache.jackrabbit.oak.plugins.name.NameValidatorProvider;
import org.apache.jackrabbit.oak.plugins.nodetype.write.InitialContent;
import org.apache.jackrabbit.oak.spi.security.OpenSecurityProvider;
import org.apache.jackrabbit.oak.spi.security.SecurityProvider;
import org.apache.jackrabbit.oak.spi.whiteboard.DefaultWhiteboard;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

import com.mongodb.DB;

public class MongoDbJcrRepository implements FactoryBean<Repository>, DisposableBean {

	
	
	private RepositoryImpl repository;

	@Override
	public Repository getObject() throws Exception {
		MongoConnection connection = new MongoConnection("127.0.0.1", 27017,
				"oak");

		DB db = connection.getDB();

		DocumentMK.Builder m = new DocumentMK.Builder();

		DocumentMK kernel = m.setMongoDB(db).open();

		 SecurityProvider securityProvider = new OpenSecurityProvider();
		 Oak oak = new Oak(new KernelNodeStore(kernel)).with(new InitialContent()) // add initial
		 .with(new NameValidatorProvider()) // allow only valid JCR names
		 .with(securityProvider) // use the default security
		 .with(new PropertyIndexProvider());
		 ContentRepository contentRepository = oak // search support for the
		 // indexes
		 .createContentRepository();
		 
		 repository = new RepositoryImpl(contentRepository,
		 new DefaultWhiteboard(), securityProvider, 12, null);
		return repository;
	}

	@Override
	public Class<?> getObjectType() {
		return Repository.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void destroy() throws Exception {
		repository.shutdown();
	}
}
