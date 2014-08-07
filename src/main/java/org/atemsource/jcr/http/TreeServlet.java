package org.atemsource.jcr.http;

import java.io.IOException;
import java.io.Writer;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.atemsource.atem.api.BeanLocator;
import org.atemsource.atem.api.infrastructure.exception.TechnicalException;
import org.atemsource.atem.service.meta.service.Cors;
import org.atemsource.jcr.service.JcrCrudService;
import org.atemsource.jcr.service.JcrCrudService.Callback;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

public class TreeServlet extends HttpServlet {

	private static final String TYPE_PROPERTY = "template";
	private static final long serialVersionUID = 1L;
	private ObjectMapper objectMapper = new ObjectMapper();
	private Cors cors = new Cors();

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		cors.appendCors(resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		cors.appendCors(resp);

		JcrCrudService crudService = BeanLocator.getInstance().getInstance(
				JcrCrudService.class);
		String parent = req.getParameter("parent");

		final Writer writer = resp.getWriter();
		if (StringUtils.isEmpty(parent)) {
			crudService.getRoot(new Callback<Node>() {

				@Override
				public void process(Node rootNode) {
					try {
						writer.write("["+convert(rootNode)+"]");
					} catch (Exception e) {
						throw new TechnicalException("cannot write result");
					}
				}

			});
		} else {
			crudService.getChildren(parent, new Callback<NodeIterator>() {

				@Override
				public void process(NodeIterator iterator) {
					try {
						writer.write("[");
						while (iterator.hasNext()) {

							try {
								writer.write(convert(iterator.nextNode()));
								if (iterator.hasNext()) {
									writer.write(",");
								}
							} catch (IOException e) {
								throw new TechnicalException(
										"cannot write result");
							}
						}
						writer.write("]");
					} catch (Exception e) {
						throw new TechnicalException("cannot render result", e);
					}
				}
			});
		}
	}

	private String convert(Node node) throws RepositoryException {
		ObjectNode json = objectMapper.createObjectNode();
		json.put("name", node.getName());
		json.put("id", node.getIdentifier());
		boolean entity = node.hasProperty(TYPE_PROPERTY);
		json.put("folder", !entity);
		if(entity) {
			// TODO replace conversion to json type by transformation logic
			json.put("template", node.getProperty("template").getString().substring("jcr:".length()));
		}
		return json.toString();
	}

	@Override
	public void init() throws ServletException {
		this.objectMapper = new ObjectMapper();
		super.init();
	}

}
