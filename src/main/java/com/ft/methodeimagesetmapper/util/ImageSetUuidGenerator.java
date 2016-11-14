package com.ft.methodeimagesetmapper.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.BitSet;
import java.util.UUID;

public class ImageSetUuidGenerator {

    private static final Charset UTF8 = Charset.forName("UTF8");

    private static final BitSet magic = BitSet.valueOf(leastSignificantUuidPartToBytes(UUID.nameUUIDFromBytes("imageset".getBytes(UTF8))));

    public static UUID fromImageUuid(UUID imageUuid) {
        return otherUuid(imageUuid);
    }

    public static UUID originalImageUuid(UUID imageSetUuid) {
        return otherUuid(imageSetUuid);
    }

    private static UUID otherUuid(UUID uuid) {
        BitSet uuidBits = BitSet.valueOf(leastSignificantUuidPartToBytes(uuid));
        uuidBits.xor(magic);
        
        // pad to the end of the array in case the bitset has shrunk by 8 bits or more
        byte[] xor = uuidBits.toByteArray();
        byte[] padded = new byte[8];
        Arrays.fill(padded, (byte)0);
        System.arraycopy(xor, 0, padded, 0, xor.length);
        
        return bytesToUuid(uuid, padded);
    }

    private static UUID bytesToUuid(UUID uuid, byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long least = bb.getLong();

        return new UUID(uuid.getMostSignificantBits(), least);
    }

    private static byte[] leastSignificantUuidPartToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[8]);
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
