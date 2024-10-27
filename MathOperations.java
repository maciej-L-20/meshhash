public class MathOperations {
    public static long addModulo264(long a, long b) {
        return (a + b) & 0xFFFFFFFFFFFFFFFFL;
    }

    public static long multiplyModulo264(long a, long b) {
        return (a * b) & 0xFFFFFFFFFFFFFFFFL;
    }
    public static long multiplyModulo264(long a, String b) {
        long bValue = Long.parseUnsignedLong(b, 16);
        return (a * bValue) & 0xFFFFFFFFFFFFFFFFL;
    }
    public static long addModulo264(long a, String b) {
        long bValue = Long.parseUnsignedLong(b, 16);
        return (a + bValue) & 0xFFFFFFFFFFFFFFFFL;
    }
    public static long rotRi(String word, int i) {
        i = i % 64; // upewnij się, że i jest w zakresie 0-63

        long longValue = Long.parseUnsignedLong(word, 16); // przekształć słowo na liczbę typu long
        long rotatedValue = Long.rotateRight(longValue, i); // wykonaj obrót bitowy

        return rotatedValue;
    }
    public static long rotRi(long word, int i) {
        i = i % 64; // upewnij się, że i jest w zakresie 0-63
        long rotatedValue = Long.rotateRight(word, i); // wykonaj obrót bitowy

        return rotatedValue;
    }
}
