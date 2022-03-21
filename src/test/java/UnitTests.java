import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Unit Tests")
@SelectPackages("net.minestom.server")
@ExcludeClassNamePatterns(".*IntegrationTest")
public class UnitTests {
}
