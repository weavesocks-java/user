package com.oracle.coherence.weavesocks.user;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.tangosol.net.NamedCache;

import static com.oracle.coherence.weavesocks.user.JsonHelpers.embed;
import static com.oracle.coherence.weavesocks.user.JsonHelpers.obj;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@ApplicationScoped
@Path("/addresses")
public class AddressesResource {

    @Inject
    private NamedCache<String, User> users;

    @GET
    @Produces(APPLICATION_JSON)
    public Response getAllAddresses() {
        return Response.ok(embed("address", Collections.emptyList())).build();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response registerAddress(AddAddressRequest req) {
        Address address = new Address(req.number, req.street, req.city, req.postcode, req.country);
        Address.Id id = users.invoke(req.userID, entry -> {
            User user = entry.getValue();
            entry.setValue(user.addAddress(address));
            return address.getId();
        });

        return Response.ok(obj().add("id", id.toString()).build()).build();
    }

    @GET
    @Path("{id}")
    @Produces(APPLICATION_JSON)
    public Address getAddress(@PathParam("id") Address.Id id) {
        return users.invoke(id.getCustomerId(), entry -> {
            User user = entry.getValue();
            return user.getAddress(id);
        });
    }

    @DELETE
    @Path("{id}")
    @Produces(APPLICATION_JSON)
    public Response deleteAddress(@PathParam("id") Address.Id id) {
        try {
            users.invoke(id.getCustomerId(), entry -> {
                User user = entry.getValue();
                entry.setValue(user.removeAddress(id));
                return null;
            });
            return status(true);
        }
        catch (RuntimeException e) {
            return status(false);
        }
    }

    // --- helpers ----------------------------------------------------------

    private static Response status(boolean fSuccess) {
        return Response.ok(obj().add("status", fSuccess).build()).build();
    }

    public static class AddAddressRequest {
        public String number;
        public String street;
        public String city;
        public String postcode;
        public String country;
        public String userID;
    }
}
