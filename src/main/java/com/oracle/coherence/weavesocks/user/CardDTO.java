package com.oracle.coherence.weavesocks.user;

import java.io.Serializable;

import com.oracle.io.pof.annotation.Portable;
import com.oracle.io.pof.annotation.PortableType;

@PortableType(id = 4)
public class CardDTO implements Serializable {
    @Portable String longNum;
    @Portable String expires;
    @Portable String ccv;

    public CardDTO() {
    }

    public CardDTO(Card card) {
        this.longNum = card.longNum;
        this.expires = card.expires;
        this.ccv = card.ccv;
    }
}
