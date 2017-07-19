package com.viafoura.examples.simpleapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PalindromesServiceTest {
    private static final Logger logger = LogManager.getLogger(PalindromesServiceTest.class);

    @Test
    public void collectPalindromeKeys() throws Exception {
        final PalindromesService service = new PalindromesService();

        // Prepare.
        final List<String> lines = new ArrayList<>();
        lines.add("{\"foo\":\"bar\"}");
        lines.add("{\"key\":\"racecar\"}");
        lines.add("{\"key\":\"not a palindrome\",\"word\":\"sentence\"}");
        lines.add("{\"key\":\"abccba\",\"word\":\"swwswsw\"}");
        lines.add("{\"key\":\"abccb\",\"word\":\"swwswsw\"}");

        // Write lines to a temp file.
        final File outFile = File.createTempFile("palindrome-test-", "tmp");
        outFile.deleteOnExit();
        Files.write(outFile.toPath(), lines);

        // Execute.
        service.collectPalindromeKeys(outFile);

        // Verify.
        final String[] expectedKeys = {"racecar", "abccba"};
        final List<String> actualKeys = service.getPalindromeKeys();
        logger.info("actualKeys: {}", actualKeys);
        Assert.assertArrayEquals(expectedKeys, actualKeys.toArray());
    }


    @Test
    public void isPalindrome() {
        final PalindromesService service = new PalindromesService();
        Assert.assertFalse(service.isPalindrome("bar"));
        Assert.assertFalse(service.isPalindrome("not a palindrome"));
        Assert.assertTrue(service.isPalindrome("racecar"));
        Assert.assertTrue(service.isPalindrome("abccba"));
        Assert.assertFalse(service.isPalindrome("abccb"));
    }
}