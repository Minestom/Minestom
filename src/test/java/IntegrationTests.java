import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Integration Tests")
@SelectPackages("net.minestom.server")
@IncludeClassNamePatterns(".*IntegrationTest")
public class IntegrationTests {
}
