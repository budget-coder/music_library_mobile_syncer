package main;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

public class JUnitExecutionListener extends RunListener {
    @Override
    public void testRunStarted(Description description) throws Exception {
        System.out.println("Number of tests to execute: " + description.testCount() + "\nTaking a backup of the last session file...");
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        System.out.println("Number of tests executed: " + result.getRunCount() + "\nRestoring the backup of the last session file...");
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        System.out.println("Ignored: " + description.getMethodName());
    }
}
