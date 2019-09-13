package com.oracle.coherence.weavesocks.user;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import javax.json.JsonObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.tangosol.net.NamedCache;

import static com.oracle.coherence.weavesocks.user.JsonHelpers.obj;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@ApplicationScoped
@Path("/")
public class UserResource {
    static final String HEADER_AUTHENTICATION_REQUIRED = "WWW-Authenticate";
    static final String HEADER_AUTHENTICATION = "Authorization";
    static final String BASIC_PREFIX = "Basic ";

    private static final Logger LOGGER = Logger.getLogger(UserResource.class.getName());
    private static final Pattern CREDENTIAL_PATTERN = Pattern.compile("(.*):(.*)");

    @Inject
    private NamedCache<String, User> users;

    @GET
    @Path("login")
    //@Authenticated
    @Produces(APPLICATION_JSON)
    public Response login(@HeaderParam("Authorization") String auth) {
        if (!auth.startsWith(BASIC_PREFIX)) {
            return fail("Basic authentication header is missing");
        }
        String  b64 = auth.substring(BASIC_PREFIX.length());
        String  usernameAndPassword = new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
        Matcher matcher = CREDENTIAL_PATTERN.matcher(usernameAndPassword);
        if (!matcher.matches()) {
            LOGGER.finest(() -> "Basic authentication header with invalid content: " + usernameAndPassword);
            return fail("Basic authentication header with invalid content");
        }

        final String username = matcher.group(1);
        final String password = matcher.group(2);

        boolean fAuth = users.invoke(username, entry -> {
            User user = entry.getValue();
            return user != null && user.authenticate(password);
        });

        if (fAuth) {
            JsonObject entity = obj()
                    .add("user",
                         obj().add("id", username))
                    .build();
            return Response.ok(entity).build();
        }
        else {
            return fail("Invalid username or password");
        }
    }

    @POST
    @Path("register")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response register(User user) {
        User prev = users.putIfAbsent(user.getId(), user);
        if (prev != null) {
            return Response.status(CONFLICT).entity("User with that ID already exists").build();
        }
        return Response.ok(obj().add("id", user.getId()).build()).build();
    }

    // ---- helpers ---------------------------------------------------------

    private Response fail(String message) {
        return Response
                .status(UNAUTHORIZED)
                .header(HEADER_AUTHENTICATION_REQUIRED, "Basic realm=\"weavesocks\"")
                .entity(message)
                .build();
    }

    @PostConstruct
    public void createTestUsers() {
        LOGGER.info("Creating test users...");
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
            LOGGER.info("Created user: " + user.getFirstName() + " " + user.getLastName());
        }
    }

}
