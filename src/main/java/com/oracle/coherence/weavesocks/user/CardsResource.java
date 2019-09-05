package com.oracle.coherence.weavesocks.user;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.tangosol.net.NamedCache;

import static com.oracle.coherence.weavesocks.user.JsonHelpers.embed;
import static com.oracle.coherence.weavesocks.user.JsonHelpers.obj;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@ApplicationScoped
@Path("/cards")
public class CardsResource {

    @Inject
    private NamedCache<String, User> users;

    @GET
    @Produces(APPLICATION_JSON)
    public Response getAllCards() {
        return Response.ok(embed("card", Collections.emptyList())).build();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response registerCard(AddCardRequest req) {
        Card card = new Card(req.longNum, req.expires, req.ccv);
        Card.Id id = users.invoke(req.userID, entry -> {
            User user = entry.getValue();
            entry.setValue(user.addCard(card));
            return card.getId();
        });

        return Response.ok(obj().add("id", id.toString()).build()).build();
    }

    @GET
    @Path("{id}")
    @Produces(APPLICATION_JSON)
    public Card getCard(@PathParam("id") Card.Id id) {
        return users.invoke(id.getCustomerId(), entry -> {
            User user = entry.getValue();
            return user.getCard(id);
        }).mask();
    }

    @DELETE
    @Path("{id}")
    @Produces(APPLICATION_JSON)
    public Response deleteCard(@PathParam("id") Card.Id id) {
        try {
            users.invoke(id.getCustomerId(), entry -> {
                User user = entry.getValue();
                entry.setValue(user.removeCard(id));
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

    public static class AddCardRequest {
        public String longNum;
        public String expires;
        public String ccv;
        public String userID;
    }
}
