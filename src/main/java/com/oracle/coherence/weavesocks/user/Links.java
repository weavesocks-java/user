package com.oracle.coherence.weavesocks.user;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import io.helidon.common.CollectionsHelper;

public class Links extends LinkedHashMap<String, Links.Link> implements Serializable {
    private static Map<String, String> ENTITY_MAP = CollectionsHelper.mapOf("customer", "customers",
                                                                            "address", "addresses",
                                                                            "card", "cards");

    private Links addLink(String entity, String id) {
        Link link = Link.to(ENTITY_MAP.get(entity), id);
        put(entity, link);
        put("self", link);
        return this;
    }

    private Links addAttrLink(String entity, String id, String attr) {
        Link link = Link.to(ENTITY_MAP.get(entity), id, attr);
        put(attr, link);
        return this;
    }

    public static Links customer(String id) {
        return new Links()
            .addLink("customer", id)
            .addAttrLink("customer", id, "addresses")
            .addAttrLink("customer", id, "cards");
    }

    public static Links address(String id) {
        return new Links().addLink("address", id);
    }

    public static Links card(String id) {
        return new Links().addLink("card", id);
    }

    public static class Link implements Serializable {
        public String href;

        public Link() {
        }

        Link(String href) {
            this.href = href;
        }

        static Link to(Object... pathElements) {
            StringBuilder sb = new StringBuilder("http://user");
            for (Object e : pathElements) {
                sb.append('/').append(e);
            }
            return new Link(sb.toString());
        }
    }
}
