/**
 * 
 */
package com.strategicgains.restx.route;

import static org.jboss.netty.handler.codec.http.HttpMethod.DELETE;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
import static org.jboss.netty.handler.codec.http.HttpMethod.PUT;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpMethod;

import com.strategicgains.restx.Request;
import com.strategicgains.restx.Response;
import com.strategicgains.restx.exception.ConfigurationException;

/**
 * @author toddf
 *
 */
public class RouteBuilder
{
	// SECTION: CONSTANTS

	static final String DELETE_ACTION_NAME = "delete";
	static final String GET_ACTION_NAME = "read";
	static final String POST_ACTION_NAME = "create";
	static final String PUT_ACTION_NAME = "update";
	static final List<HttpMethod> ALL_HTTP_METHODS = Arrays.asList(new HttpMethod[] {GET, POST, PUT, DELETE});
	static final Map<HttpMethod, String> ACTION_MAPPING = new HashMap<HttpMethod, String>();

	static
	{
		ACTION_MAPPING.put(HttpMethod.DELETE, DELETE_ACTION_NAME);
		ACTION_MAPPING.put(HttpMethod.GET, GET_ACTION_NAME);
		ACTION_MAPPING.put(HttpMethod.POST, POST_ACTION_NAME);
		ACTION_MAPPING.put(HttpMethod.PUT, PUT_ACTION_NAME);
	}

	
	// SECTION: INSTANCE VARIABLES

	private String uri;
	private List<HttpMethod> methods = new ArrayList<HttpMethod>();
	private Map<HttpMethod, String> actionNames = new HashMap<HttpMethod, String>();
	private Object controller;
	private boolean shouldSerializeResponse = true;
	
	public RouteBuilder(String uri, Object controller)
	{
		super();
		this.uri = uri;
		this.controller = controller;
	}
	
	public RouteBuilder action(String action, HttpMethod method)
	{
		if (!actionNames.containsKey(method))
		{
			actionNames.put(method, action);
		}

		if (!methods.contains(method))
		{
			methods.add(method);
		}

		return this;
	}
	
	public RouteBuilder method(HttpMethod... methods)
	{
		for (HttpMethod method : methods)
		{
			if (!this.methods.contains(method))
			{
				this.methods.add(method);
			}
		}

		return this;
	}

	public RouteBuilder noSerialization()
	{
		this.shouldSerializeResponse = false;
		return this;
	}

	public RouteBuilder serializeResults()
	{
		this.shouldSerializeResponse = true;
		return this;
	}
	
	public List<Route> createRoutes()
	{
		if (methods.isEmpty())
		{
			methods = ALL_HTTP_METHODS;
		}

		List<Route> routes = new ArrayList<Route>();
		
		for (HttpMethod method : methods)
		{
			String actionName = actionNames.get(method);

			if (actionName == null)
			{
				actionName = ACTION_MAPPING.get(method);
			}
			
			Method action = determineActionMethod(controller, actionName);
			routes.add(new Route(uri, controller, action, method, shouldSerializeResponse));
		}
		
		return routes;
	}


	// SECTION: UTILITY - PRIVATE

	/**
	 * Attempts to find the actionName on the controller, assuming a signature of actionName(Request, Response), 
	 * and returns the action as a Method to be used later when the route is invoked.
	 * 
	 * @param controller a pojo that implements a method named by the action, with Request and Response as parameters.
	 * @param actionName the name of a method on the given controller pojo.
	 * @return a Method instance referring to the action on the controller.
	 * @throws ConfigurationException if an error occurs.
	 */
	private Method determineActionMethod(Object controller, String actionName)
	{
		try
		{
			return controller.getClass().getMethod(actionName, Request.class, Response.class);
		}
		catch (Exception e)
		{
			throw new ConfigurationException(e);
		}
	}
}