package com.rest.resource;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.rest.domain.Customer;
import com.rest.domain.Customers;
import com.rest.exception.BadRequestException;
import com.rest.exception.ErrorMessage;
import com.rest.exception.InternalServerErrorException;
import com.rest.exception.NotFoundException;
import com.rest.service.CustomerService;
import com.rest.service.validation.ValidationException;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * REST layer for customers.
 */
@Controller
// Browsers are generally restricted when client-side web applications running from one origin request
// data from another origin. Enabling cross-origin access is generally termed as CORS (Cross-Origin Resource Sharing).
// With microservices, or multiple instances running behind a load balancer, as each service runs in it own origin, 
// it will easily get into the issue of a client-side web application consuming data from multiple origins.
@CrossOrigin
@Path("customers")
public class CustomerResource {

	private static final Logger LOG = LogManager.getLogger(CustomerResource.class);

	@Autowired
	private CustomerService customerService;

	/**
	 * Create a Customer.
	 *
	 * @param customer The customer to create.
	 * @param request  The HttpServletRequest used for the run-time caller resolution.
	 * @return <p>HTTP Status CREATED (201) the bean with all the fields including the id populated.</p>
	 * @throws BadRequestException if the validation of the Customer failed.
	 * @throws InternalServerErrorException if a server side error occurred.
	 */
	@POST
	@Consumes({ "application/xml", "application/json" })
	@Produces({ "application/xml", "application/json" })
	@TypeHint(Customer.class)
	public Response createCustomer(Customer customer, @Context HttpServletRequest request)
			throws BadRequestException, InternalServerErrorException {
		try {
			customerService.createCustomer(customer);
			return Response.ok(customer).status(Status.CREATED).build(); // Returns a 201 CREATED with the customer.
		} catch (ValidationException e) {
			ErrorMessage message = new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),
					e.getValidationErrors().stream().map(ve -> ve.toString()).collect(Collectors.joining(",")),
					"http://localhost:8080/error400.jsp", Response.Status.BAD_REQUEST.getReasonPhrase());
			LOG.warn(message);
			throw new BadRequestException(message, request.getHeader("accept"));
		} catch (Exception e) {
			ErrorMessage message = new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					e.getMessage(), "http://localhost:8080/error500.jsp",
					Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
			LOG.error(message, e);
			throw new InternalServerErrorException(message, request.getHeader("accept"));
		}
	}

	/**
	 * Update a Customer.
	 *
	 * @param id       The id of the customer to update.
	 * @param customer The customer to update.
	 * @param request  The HttpServletRequest used for the run-time caller resolution.
	 * @return <p>HTTP Status No Content (204)</p>
	 * @throws BadRequestException if the validation of the Customer failed.
	 * @throws InternalServerErrorException if a server side error occurred.
	 */
	@PUT
	@Path("{id}")
	@Consumes({ "application/xml", "application/json" })
	@TypeHint(Customer.class)
	public Response updateCustomer(@PathParam("id") int id, Customer customer, @Context HttpServletRequest request)
			throws BadRequestException, InternalServerErrorException {
		try {
			customer.setId(id);
			customerService.updateCustomer(id, customer);
			return Response.noContent().build(); // Returns a 204 No Content - The server processed the request successfully, but is not returning any content.
		} catch (ValidationException e) {
			ErrorMessage message = new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),
					e.getValidationErrors().stream().map(ve -> ve.toString()).collect(Collectors.joining(",")),
					"http://localhost:8080/error400.jsp", Response.Status.BAD_REQUEST.getReasonPhrase());
			LOG.warn(message);
			throw new BadRequestException(message, request.getHeader("accept"));
		} catch (Exception e) {
			ErrorMessage message = new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					e.getMessage(), "http://localhost:8080/error500.jsp",
					Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
			LOG.error(message, e);
			throw new InternalServerErrorException(message, request.getHeader("accept"));
		}
	}

	/**
	 * Delete a Customer.
	 *
	 * @param id      The id of the customer to delete.
	 * @param request The HttpServletRequest used for the run-time caller resolution.
	 * @return <p>HTTP Status No Content (204)</p>
	 * @throws InternalServerErrorException if a server side error occurred.
	 */
	@DELETE
	@Path("{id}")
	public Response deleteCustomer(@PathParam("id") int id, @Context HttpServletRequest request)
			throws InternalServerErrorException {
		try {
			customerService.deleteCustomer(id);
			return Response.noContent().build(); // Returns a 204 No Content - The server processed the request successfully, but is not returning any content.
		} catch (Exception e) {
			ErrorMessage message = new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					e.getMessage(), "http://localhost:8080/error500.jsp",
					Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
			LOG.error(message, e);
			throw new InternalServerErrorException(message, request.getHeader("accept"));
		}
	}

	/**
	 * Retrieve a Customer.
	 *
	 * @param id      The id of the customer to retrieve.
	 * @param request The HttpServletRequest used for the run-time caller resolution.
	 * @return <p>HTTP Status OK (200) the Customer.</p>
	 * @throws NotFoundException if the Customer was not found.
	 * @throws InternalServerErrorException if a server side error occurred.
	 */
	@GET
	@Path("{id}")
	@Produces({ "application/xml", "application/json" })
	@TypeHint(Customer.class)
	public Response getCustomer(@PathParam("id") int id, @Context HttpServletRequest request)
			throws NotFoundException, InternalServerErrorException {
		Customer customer = null;
		try {
			customer = customerService.getCustomer(id);
		} catch (Exception e) {
			ErrorMessage message = new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					e.getMessage(), "http://localhost:8080/error500.jsp",
					Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
			LOG.error(message, e);
			throw new InternalServerErrorException(message, request.getHeader("accept"));
		}

		if (customer == null) {
			ErrorMessage message = new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "Customer not found",
					"http://localhost:8080/error404.jsp", Response.Status.NOT_FOUND.getReasonPhrase());
			LOG.warn(message);
			throw new NotFoundException(message, request.getHeader("accept"));
		}

		return Response.ok(customer).build(); // Returns a 200 OK with the customer.
	}

	/**
	 * Retrieve all the Customers.
	 *
	 * @return <p>HTTP Status OK (200) all the Customers.</p>
	 * @throws InternalServerErrorException if a server side error occurred.
	 */
	@GET
	@Produces({ "application/xml", "application/json" })
	@TypeHint(Customers.class)
	public Response getCustomers(@Context HttpServletRequest request) throws InternalServerErrorException {
		try {
			List<Customer> customers = customerService.getCustomers();
			// Wrap the list of customers in a Customers object for clients requesting XML
			// or JSON. This is primarily done for XML, for JSON you can just pass a List<Customer>
			// and it will convert it into an array. We wrap the JSON so that we don't have to have
			// additional methods that produce and consume Customers for XML and List<Customer> for JSON.
			Customers customersObject = new Customers();
			customersObject.setCustomers(customers);
			return Response.ok(customersObject).build(); // Returns a 200 OK with the customers object.
		} catch (Exception e) {
			ErrorMessage message = new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					e.getMessage(), "http://localhost:8080/error500.jsp",
					Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
			LOG.error(message, e);
			throw new InternalServerErrorException(message, request.getHeader("accept"));
		}
	}
}
