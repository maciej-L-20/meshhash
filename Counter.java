public class Counter {
    public long[] counterArray;

    public Counter() {
        counterArray = new long[4];
    }

    public void increment() {
        boolean carry = true;
        for (int i = 0; i < 4 && carry; i++) {
            counterArray[i]++;
            if (counterArray[i] != 0) {
                carry = false;
            }
        }
    }

    public long getValue() {
        long value = 0;
        for (int i = 0; i < 4; i++) {
            value += counterArray[i] * Math.pow(2, 64 * i);
        }
        return value;
    }
}
