package com.oracle.coherence.weavesocks.user;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.tangosol.net.NamedCache;

import static com.oracle.coherence.weavesocks.user.JsonHelpers.embed;
import static com.oracle.coherence.weavesocks.user.JsonHelpers.obj;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@ApplicationScoped
@Path("/customers")
public class CustomersResource {

    @Inject
    private NamedCache<String, User> users;

    @GET
    @Produces(APPLICATION_JSON)
    public Response getAllCustomers() {
        return Response.ok(embed("customer", users.values())).build();
    }

    @GET
    @Path("{id}")
    @Produces(APPLICATION_JSON)
    public Response getCustomer(@PathParam("id") String id) {
        return Response.ok(users.getOrDefault(id, new User())).build();
    }

    @DELETE
    @Path("{id}")
    @Produces(APPLICATION_JSON)
    public Response deleteCustomer(@PathParam("id") String id) {
        User prev = users.remove(id);
        return Response.ok(obj().add("status", prev != null).build()).build();
    }

    @GET
    @Path("{id}/cards")
    @Produces(APPLICATION_JSON)
    public Response getCustomerCards(@PathParam("id") String id) {
        User user = users.get(id);
        return Response.ok(embed("card", user.getCards().stream().map(Card::mask).toArray())).build();
    }

    @GET
    @Path("{id}/addresses")
    @Produces(APPLICATION_JSON)
    public Response getCustomerAddresses(@PathParam("id") String id) {
        User user = users.get(id);
        return Response.ok(embed("address", user.getAddresses())).build();
    }

    // --- helpers ----------------------------------------------------------

    @PostConstruct
    public void createTestUsers() {
        User user   = new User("Test", "User", "user@weavesocks.com", "user", "pass")
                .addCard(new Card("1234123412341234", "12/19", "123"))
                .addAddress(new Address("123", "Main St", "Springfield", "12123", "USA"));
        User aleks  = new User("Aleks", "Seovic", "aleks@weavesocks.com", "aleks", "pass")
                .addCard(new Card("4567456745674567", "10/20", "007"))
                .addAddress(new Address("555", "Spruce St", "Tampa", "33633", "USA"));
        User bin    = new User("Bin", "Chen", "bin@weavesocks.com", "bin", "pass")
                .addCard(new Card("3691369136913691", "01/21", "789"))
                .addAddress(new Address("123", "Boston St", "Boston", "01555", "USA"));
        User harvey = new User("Harvey", "Raja", "harvey@weavesocks.com", "harvey", "pass")
                .addCard(new Card("6854657645765476", "03/22", "456"))
                .addAddress(new Address("123", "O'Farrell St", "San Francisco", "99123", "USA"));
        User randy  = new User("Randy", "Stafford", "randy@weavesocks.com", "randy", "pass")
                .addCard(new Card("6543123465437865", "08/23", "042"))
                .addAddress(new Address("123", "Mountain St", "Denver", "74765", "USA"));

        register(user, aleks, bin, harvey, randy);
    }

    private void register(User... users) {
        for (User user : users) {
            this.users.put(user.getId(), user);
        }
    }
}
