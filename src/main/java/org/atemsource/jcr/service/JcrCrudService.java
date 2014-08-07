package org.atemsource.jcr.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.jcr.Credentials;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.commons.JcrUtils;
import org.atemsource.atem.api.attribute.Attribute;
import org.atemsource.atem.api.attribute.relation.SingleAttribute;
import org.atemsource.atem.api.infrastructure.exception.TechnicalException;
import org.atemsource.atem.api.service.AttributeQuery;
import org.atemsource.atem.api.service.DeletionService;
import org.atemsource.atem.api.service.FindByAttributeService;
import org.atemsource.atem.api.service.IdentityAttributeService;
import org.atemsource.atem.api.service.InsertionCallback;
import org.atemsource.atem.api.service.InsertionService;
import org.atemsource.atem.api.service.SingleAttributeQuery;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.Type;
import org.atemsource.atem.service.entity.EntityRestService.Result;
import org.atemsource.atem.service.entity.FindByIdService;
import org.atemsource.atem.service.entity.FindByTypeService;
import org.atemsource.atem.service.entity.GetCollectionService;
import org.atemsource.atem.service.entity.ListCallback;
import org.atemsource.atem.service.entity.ReturnErrorObject;
import org.atemsource.atem.service.entity.SingleCallback;
import org.atemsource.atem.service.entity.StatefulUpdateService;
import org.atemsource.atem.service.entity.UpdateCallback;
import org.atemsource.atem.service.entity.search.AttributePredicate;
import org.atemsource.atem.service.entity.search.AttributeSorting;
import org.atemsource.atem.service.entity.search.Operator;
import org.atemsource.atem.service.entity.search.Paging;
import org.atemsource.atem.service.entity.search.Query;
import org.atemsource.atem.service.entity.search.Sorting;
import org.atemsource.atem.service.refresolver.CollectionResource;
import org.atemsource.atem.utility.transform.api.meta.DerivedType;
import org.atemsource.atem.utility.transform.service.CreationService;
import org.atemsource.jcr.entitytype.JcrEntityType;
import org.atemsource.jcr.entitytype.JcrRefType;
import org.codehaus.jackson.node.ObjectNode;

/**
 * 
 * 
 * insert: need to specify parent. Use parent prop to get parent and initialize
 * the node. Then merge onto node. update: find node by identifier. If node's
 * parent has changed, then ode (is setParent enough to do so???? delete: easy
 * findByType: query where type="typeCode"
 * 
 * @author stefan
 * 
 */
public class JcrCrudService implements InsertionService, StatefulUpdateService,
		CreationService<Node, Object>, FindByIdService, FindByTypeService,
		DeletionService, IdentityAttributeService, FindByAttributeService {

	public interface Callback<T> {

		void process(T value);

	}

	private static final String SELECTOR = "Selector";
	private Repository repository;
	private Credentials credentials;
	private String workspace = "default";

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	public static void setSessionHolder(ThreadLocal<Session> sessionHolder) {
		JcrCrudService.sessionHolder = sessionHolder;
	}

	private static ThreadLocal<Session> sessionHolder = new ThreadLocal<Session>();

	Session createSession() {
		if (hasSession()) {
			return sessionHolder.get();
		}
		Session session;
		try {
			if (credentials == null) {
				session = repository.login(workspace);
			} else {
				session = repository.login(credentials, workspace);
			}
		} catch (Exception e) {
			throw new TechnicalException("cannot get session", e);
		}
		sessionHolder.set(session);
		return session;
	}

	@PostConstruct
	public void initialize() {

	}

	@Override
	public <E> Serializable insert(EntityType<E> entityType,
			InsertionCallback<E> callback) {
		Request request = new Request();
		Session session = null;
		try {
			session = request.getSession();
			Node entity = (Node) callback.get();
			// transfomer already added node to tree
			session.save();
			return entity.getIdentifier();
		} catch (Exception e) {
			throw new TechnicalException("cannot persist node", e);
		} finally {
			request.close();
		}
	}

	@Override
	public <E> boolean isPersistent(EntityType<E> entityType, E entity) {
		return true;
	}

	public void closeSession() {
		sessionHolder.get().logout();
		sessionHolder.remove();
	}

	@Override
	public Node create(EntityType<Node> entityType,
			EntityType<Object> jsonType, Object bean) {
		DerivedType<Node, Object> derivedType = (DerivedType<Node, Object>) jsonType
				.getMetaType()
				.getMetaAttribute(DerivedType.META_ATTRIBUTE_CODE)
				.getValue(jsonType);
		Attribute pathAttribute = derivedType.getTransformation()
				.getDerivedAttribute(entityType.getAttribute("path"));
		String path = (String) pathAttribute.getValue(bean);
		try {
			Session session = getSession();
			Node node = JcrUtils.getOrCreateByPath(path, true,
					NodeType.NT_UNSTRUCTURED, NodeType.NT_UNSTRUCTURED, session,
					true);
			return node;
		} catch (Exception e) {
			throw new TechnicalException("technical error during insert", e);
		}

	}

	private Session getSession() {
		Session session = sessionHolder.get();
		if (session == null) {
			throw new TechnicalException(
					"no session exists. Cannot create new node");
		}
		return session;
	}

	private boolean hasSession() {
		return sessionHolder.get() != null;
	}

	private class Request {
		private Session session;
		private boolean useExisting;

		public Request() {

			useExisting = hasSession();
			if (useExisting) {
				session = getSession();
			} else {
				session = createSession();
			}
		}

		public Session getSession() {
			return session;
		}

		public void close() {
			if (!useExisting) {
				closeSession();
			}
		}
	}

	@Override
	public <O, T> T findById(EntityType<O> entityType, Serializable id,
			SingleCallback<O, T> callback) {
		Session session = createSession();
		try {
			Node node = session.getNodeByIdentifier(id.toString());
			return callback.process((O) node);

		} catch (ItemNotFoundException e) {
			return null;
		} catch (RepositoryException e) {
			throw new TechnicalException("cannot find node", e);
		} finally {
			closeSession();
		}
	}

	public <O> QueryObjectModel createQuery(JcrEntityType entityType,
			Query query, Sorting sorting, Paging paging)
			throws InvalidQueryException, RepositoryException {
		QueryObjectModelFactory qomFactory = getSession().getWorkspace()
				.getQueryManager().getQOMFactory();
		Value typeValue = getSession().getValueFactory().createValue(
				entityType.getCode());
		Comparison typeComparison = qomFactory
				.comparison(
						qomFactory.propertyValue(SELECTOR,
								entityType.getTypeProperty()),
						qomFactory.JCR_OPERATOR_EQUAL_TO,
						qomFactory.literal(typeValue));

		Ordering[] orderings = null;
		if (sorting != null) {
			orderings = new Ordering[sorting.getAttributeSortings().size()];
			for (AttributeSorting attributeSorting : sorting
					.getAttributeSortings()) {
				if (attributeSorting.isAsc()) {
					qomFactory.ascending(qomFactory.propertyValue(SELECTOR,
							attributeSorting.getAttribute().getCode()));
				}
			}
		}

		Constraint constraint;
		if (query != null) {
			Constraint subquery = createSubquery(query, qomFactory);
			constraint = qomFactory.and(typeComparison, subquery);
		} else {
			constraint = typeComparison;
		}

		Selector selector = qomFactory.selector(NodeType.NT_UNSTRUCTURED,
				SELECTOR);
		return qomFactory.createQuery(selector, constraint, orderings, null);

	}

	private Constraint createSubquery(Query query,
			QueryObjectModelFactory qomFactory)
			throws UnsupportedRepositoryOperationException, RepositoryException {
		Constraint previousConstraint = null;
		for (AttributePredicate<?> predicate : query.getPredicates()) {
			Value value = getValue(predicate.getValue());
			Constraint constraint = qomFactory.comparison(
					qomFactory.propertyValue(SELECTOR, predicate.getAttribute()
							.getCode()), getOperator(predicate.getOperator()),
					qomFactory.literal(value));

			if (previousConstraint != null) {
				if (query.isOr()) {
					previousConstraint = qomFactory.or(previousConstraint,
							constraint);
				} else {
					previousConstraint = qomFactory.and(previousConstraint,
							constraint);
				}
			} else {
				previousConstraint = constraint;
			}
		}
		return previousConstraint;

	}

	private String getOperator(Operator operator) {
		switch (operator) {
		case EQUAL:
			return QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO;
		case GET:
			return QueryObjectModelFactory.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO;
		case GT:
			return QueryObjectModelFactory.JCR_OPERATOR_GREATER_THAN;
		case LET:
			return QueryObjectModelFactory.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO;
		case LIKE:
			return QueryObjectModelFactory.JCR_OPERATOR_LIKE;
		case LT:
			return QueryObjectModelFactory.JCR_OPERATOR_LESS_THAN;
		default:
			throw new IllegalArgumentException("oeprator " + operator.name()
					+ " is not supported by JCR");
		}
	}

	private Value getValue(Object value)
			throws UnsupportedRepositoryOperationException, RepositoryException {
		if (value instanceof Long) {
			return getSession().getValueFactory().createValue((Long) value);
		} else if (value instanceof Long) {
			return getSession().getValueFactory().createValue((Long) value);
		} else if (value instanceof Integer) {
			return getSession().getValueFactory().createValue(
					new Long((Integer) value));
		} else if (value instanceof Double) {
			return getSession().getValueFactory().createValue((Double) value);
		} else if (value instanceof String) {
			return getSession().getValueFactory().createValue((String) value);
		} else if (value instanceof Boolean) {
			return getSession().getValueFactory().createValue((Boolean) value);
		} else {
			throw new IllegalArgumentException("don't know how to convert "
					+ value + " to jcr value");
		}
	}

	@Override
	public <O> Result getEntities(EntityType<O> entityType, Query query,
			Sorting sorting, Paging paging, ListCallback<O> listCallback) {
		try {
			Session session = createSession();
			QueryObjectModel qom = createQuery((JcrEntityType) entityType,
					query, sorting, paging);
			if (paging != null) {
				qom.setLimit(paging.getCount());
				qom.setOffset(paging.getStart());
			}
			QueryResult queryResult = qom.execute();
			NodeIterator nodes = queryResult.getNodes();
			List<O> entities = new LinkedList<O>();
			for (; nodes.hasNext();) {
				entities.add((O) nodes.nextNode());
			}
			return listCallback.process(entities, -1);
		} catch (ItemNotFoundException e) {
			return null;
		} catch (RepositoryException e) {
			throw new TechnicalException("cannot find node", e);
		} finally {
			closeSession();
		}
	}

	@Override
	public <E> Collection<String> getQueryableFields(EntityType<E> entityType) {
		return null;
	}

	@Override
	public void delete(EntityType<?> entityType, Serializable id) {
		Session session = createSession();
		try {
			// assuming that id = path
			session.removeItem((String) id);
			session.save();
		} catch (Exception e) {
			throw new TechnicalException("cannot remove node", e);
		} finally {
			closeSession();
		}
	}

	@Override
	public ReturnErrorObject update(Serializable id,
			EntityType<?> originalType, UpdateCallback callback) {
		Session session = createSession();
		try {
			Node node = session.getNode((String) id);
			callback.update(node);
			session.save();
			return null;
		} catch (Exception e) {
			throw new TechnicalException("cannot update entity " + id, e);

		} finally {
			closeSession();
		}
	}

	@Override
	public <E> Serializable getId(EntityType<E> entityType, E entity) {
		return getIdAttribute(entityType).getValue(entity);
	}

	@Override
	public Type<?> getIdType(EntityType<?> entityType) {
		return getIdAttribute(entityType).getTargetType();
	}

	@Override
	public SingleAttribute<? extends Serializable> getIdAttribute(
			EntityType<?> entityType) {
		return (SingleAttribute<? extends Serializable>) entityType
				.getAttribute("identifier");
	}

	@Override
	public AttributeQuery prepareQuery(EntityType<?> entityType,
			Attribute<?, ?> attribute) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public SingleAttributeQuery prepareSingleQuery(EntityType<?> entityType,
			Attribute<?, ?> attribute) {
		return new JcrSingleAttributeQuery(attribute);
	}

	public class JcrSingleAttributeQuery implements SingleAttributeQuery {

		private Attribute refAttribute;

		public JcrSingleAttributeQuery(Attribute refAttribute) {
			super();
			this.refAttribute = refAttribute;
		}

		@Override
		public Object getResult(Object value) {

			Node result = null;
			Session session = getSession();

			try {
				JcrRefType refType = (JcrRefType) refAttribute.getTargetType();
				String identifier = refType.getId((String) refAttribute
						.getValue(value));
				if (identifier != null) {
					result = session.getNodeByIdentifier(identifier);
				}

			} catch (ItemNotFoundException e) {
				// not found return null
			} catch (RepositoryException e) {
				throw new TechnicalException("cannot find referenced node", e);
			} finally {
				closeSession();
			}
			return result;

		}

	}

	public void getRoot(Callback<Node> callback) {
		try {
			Session session = createSession();
			callback.process(session.getRootNode());
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeSession();
		}
	}

	public void getChildren(Serializable parentId,
			Callback<NodeIterator> callback) {
		try {
			Session session = createSession();
			Node parent = session.getNodeByIdentifier(String.valueOf(parentId));

			NodeIterator nodes = parent.getNodes();
			callback.process(nodes);

		} catch (ItemNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeSession();
		}
	}

	
}
