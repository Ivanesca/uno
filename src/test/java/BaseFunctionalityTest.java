import vindur.Main;

import java.io.IOException;

public class BaseFunctionalityTest {
    @org.junit.jupiter.api.Test
    public void test() throws IOException {
        String destFileName = "src/test/resources/output.txt";
        String sourceFileName = "src/test/resources/test.txt";

        Main.main(new String[] {sourceFileName, destFileName});
    }
}
