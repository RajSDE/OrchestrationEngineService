package com.orchestrationengine.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * Utility to generate UUIDv7 (time-ordered sequential UUIDs) conforming to RFC 9562.
 * This ensures sequential index ordering in DB inserts to prevent fragmentation and optimize writes.
 */
public class SequencedUuidGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static UUID generateV7() {
        long timestamp = Instant.now().toEpochMilli();
        long msb = 0;

        // 48-bit timestamp
        msb |= (timestamp & 0xFFFFFFFFFFFFL) << 16;

        // 4-bit version (7) -> 0111 binary at bits 12-15
        msb |= 0x7000L;

        // 12-bit random
        long rand12 = random.nextInt(4096);
        msb |= rand12;

        long lsb = 0;
        // 2-bit variant (2) -> 10 binary at bits 62-63 of LSB (0x8000000000000000L)
        lsb |= 0x8000000000000000L;

        // 62-bit random
        long rand62 = random.nextLong() & 0x3FFFFFFFFFFFFFFFL;
        lsb |= rand62;

        return new UUID(msb, lsb);
    }
}
