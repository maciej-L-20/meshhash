public class MeshHash {
    private int P;
    private long [] pipes;
    private int block_round_counter;
    private int key_counter;
    private int key_length;
    private Counter bit_counter;
    private Counter block_counter;
    private int hash_bit_length;
    private byte[] message;
    private long[] key;
    private long[] dataStream;

    private MeshHash(byte[] message, byte[] key, int hash_length) {
        this.message = message;
        this.hash_bit_length = hash_length;
        this.P = computeNumberOfPipes(hash_length);
        this.pipes = new long[P];
        int messBits = message.length*8;
        this.bit_counter = new Counter();
        this.block_counter = new Counter();
        this.block_round_counter = 0;
        this.key_counter = 0;

        for (int i = 0; i < messBits; i++) {
            bit_counter.increment();
        }

        this.key = convertBytesToWords(key);
        this.key_length = key.length / 8;
        this.dataStream = prepareDataStream();
    }

    public static String computeMeshHash(String message, String key, int digestLength) {
        byte[] messageByteArr = message.getBytes();
        byte[] keyByteArr = key.getBytes();
        MeshHash meshHash = new MeshHash(messageByteArr, keyByteArr, digestLength);
        byte[] computedDigest = meshHash.computeMeshHash();
        StringBuilder sb = new StringBuilder();
        for (byte b : computedDigest) {
            sb.append(Integer.toHexString(b & 0xFF));
        }
        return sb.toString();
    }
    public static String computeMeshHash(String message, int digestLength){
        String key = "";
        return computeMeshHash(message,key,digestLength);
    }

    public static void main(String[] args) {
        System.out.println("Ala ma koty");
        System.out.println(computeMeshHash("ala ma koty","bdan",100));
        System.out.println("Ala ma kota");
        System.out.println(computeMeshHash("ala ma kota","bdan",100));
        System.out.println("Ala ma koty");
        System.out.println(computeMeshHash("ala ma koty","bdan",100));
    }

    private byte[] computeMeshHash() {
        for (int i = 0; i < dataStream.length; i++) {
            long data = dataStream[i];
            while (block_round_counter < P) {
                normalRound(data, block_round_counter);
            }
            finalBlockRound();
        }
        final_rounds();
        byte[] hashValue = computeHashValue();
        return hashValue;
    }

    private byte[] computeHashValue() {
        byte[] hashValue = new byte[hash_bit_length / 8];
        for (int i = 0; i < hashValue.length; i++) {
            for (int j = 0; j < P; j++) {
                normalRound(0, j);
            }
            byte temp = 0;
            for (int j = 0; j < P; j += 2)
                temp ^= pipes[j];
            hashValue[i] = (byte) (temp & 255);

            if (i % P == P - 1) finalBlockRound();
        }
        return hashValue;
    }

    private void final_rounds() {
        /* process bit_counter */
        for (int i = 0; i < bit_counter.counterArray.length; i++) {
            for (int j = 0; j < P; j++) {
                pipes[j] = SBox(pipes[j] ^ bit_counter.counterArray[i] ^ (Long.parseUnsignedLong("0101010101010101", 16) * j));
            }
        }


        /* process hashbitlen */
        for (int i = 0; i < P; i++) {
            {
                pipes[i] = SBox(pipes[i] ^ hash_bit_length ^ (Long.parseUnsignedLong("0101010101010101", 16) * i));
            }
        }
    }

    private void finalBlockRound() {
        processBlockCounter();
        if (key_length > 0) {
            processKey();
        }
    }

    private void processBlockCounter() {
        block_round_counter = 0;
        for (int i = 0; i < P; i++) {
            pipes[i] = SBox(pipes[i] ^ block_counter.counterArray[i % 4]);
        }
        block_counter.increment();
    }

    private void processKey() {
        for (int i = key_counter; i < key_length + key_counter; i += P) {
            for (int j = 0; j < P; j++) {
                pipes[j] = SBox(pipes[j] ^ key[(i + j) % key_length]);
            }
        }
        key_counter = (key_counter + 1) % key_length;

        /* process key_length */
        for (int i = 0; i < P; i++) {
            pipes[i] = SBox(pipes[i] ^ key_length ^ (Long.parseUnsignedLong("0101010101010101", 16) * i));
        }
    }

    private void normalRound(long data, int index) {
        long sBoxInput = pipes[index] ^ (MathOperations.multiplyModulo264(index, "0101010101010101") ^ data);
        sBoxInput = MathOperations.rotRi(sBoxInput, 37 * index);
        pipes[index] = MathOperations.addModulo264(SBox(sBoxInput), pipes[(index + 1) % P]);
        block_round_counter++;
    }

    private long SBox(long input) {
        input = MathOperations.multiplyModulo264(input, "9e3779b97f4a7bb9");
        input = MathOperations.addModulo264(input, "5e2d58d8b3bcdef7");
        input = MathOperations.rotRi(input, 37);
        input = MathOperations.multiplyModulo264(input, "9e3779b97f4a7bb9");
        input = MathOperations.addModulo264(input, "5e2d58d8b3bcdef7");
        input = MathOperations.rotRi(input, 37);
        return input;
    }

    private long[] prepareDataStream() {
        int r = 0;
        while ((key_length * 64 + bit_counter.getValue() + r) % (64 * P) != 0) r++;
        long[] dataStream = concatenate(key, convertBytesToWords(message));
        byte[] rArr = new byte[r];
        for (int i = 0; i < r; i++) {
            rArr[i] = 0;
        }
        dataStream = concatenate(dataStream, convertBytesToWords(rArr));
        return dataStream;
    }

    private long[] convertBytesToWords(byte[] bytes) {
        int numWords = (int) Math.ceil((double) bytes.length / 8); // Determine the number of words
        long[] words = new long[numWords];

        for (int i = 0; i < numWords; i++) {
            int startIndex = i * 8;
            int endIndex = Math.min(startIndex + 8, bytes.length);
            int wordLength = endIndex - startIndex;

            long value = 0;

            for (int j = 0; j < wordLength; j++) {
                value <<= 8; // Shift existing bits to the left
                value |= (bytes[startIndex + j] & 0xFF); // Add the current byte
            }

            words[i] = value;
        }

        return words;
    }

    private int computeNumberOfPipes(int hashBitlen) {
        int smallest = (hashBitlen / 64) + 1;
        if (smallest < 4) return 4;
        if (smallest > 256) return 256;
        return smallest;
    }

    public long[] concatenate(long[] array1, long[] array2) {
        int array1Length = array1.length;
        int array2Length = array2.length;
        long[] concatenatedArray = new long[array1Length + array2Length];

        for (int i = 0; i < array1Length; i++) {
            concatenatedArray[i] = array1[i];
        }

        for (int i = 0; i < array2Length; i++) {
            concatenatedArray[array1Length + i] = array2[i];
        }

        return concatenatedArray;
    }

}
