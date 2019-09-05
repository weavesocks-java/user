package com.oracle.coherence.weavesocks.user;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.helidon.security.providers.httpauth.UserStore;

import com.tangosol.net.NamedCache;

@ApplicationScoped
public class UserStoreImpl implements UserStore {

    @Inject
    private NamedCache<String, com.oracle.coherence.weavesocks.user.User> users;

    @Override
    public Optional<User> user(String login) {
        com.oracle.coherence.weavesocks.user.User user = users.get(login);
        if (user == null) {
            return Optional.empty();
        }
        else {
            return Optional.of(new User() {
                @Override
                public String login() {
                    return login;
                }

                @Override
                public char[] password() {
                    return user.getPassword().toCharArray();
                }
            });
        }
    }
}
