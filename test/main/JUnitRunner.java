package main;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class JUnitRunner extends BlockJUnit4ClassRunner {
    public JUnitRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    public void run(RunNotifier notifier) {
        // Register the listener to the test execution of JUnit.
        notifier.addListener(new JUnitExecutionListener());
        // For some reason, testRunStarted() in our listener does
        // not start automatically. We have to start it manually.
        notifier.fireTestRunStarted(getDescription());
        // Now we can execute our JUnit tests.
        super.run(notifier);
    }
}
