public class Main {
    public static void main(String[] args) {
        System.out.println(MeshHash.computeMeshHash("abcdefghijklmnoprst",256));
        System.out.println(MeshHash.computeMeshHash("abcdefghijxlmnoprst",256));
        System.out.println(MeshHash.computeMeshHash("abcdefghijxlmnoprst",264));
    }
}
