package org.atemsource.jcr.entitytype;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.atemsource.atem.api.EntityTypeRepository;
import org.atemsource.atem.api.type.EntityType;

public class EntityTypeInvocationHandler implements InvocationHandler {

	private String typeCode;
	public EntityTypeInvocationHandler(
			EntityTypeRepository entityTypeRepository,String typeCode) {
		super();
		this.typeCode = typeCode;
		this.entityTypeRepository = entityTypeRepository;
	}
	private EntityTypeRepository entityTypeRepository;
	@Override
	public Object invoke(Object target, Method method, Object[] arguments)
			throws Throwable {
		EntityType<?> targetType=entityTypeRepository.getEntityType(typeCode);
		return method.invoke(targetType, arguments);
	}

}
