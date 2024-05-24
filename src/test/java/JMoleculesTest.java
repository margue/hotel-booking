import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesDddRules;

@AnalyzeClasses(packages = {"persistence", "service"})
public class JMoleculesTest {

    @ArchTest
    ArchRule dddRules = JMoleculesDddRules.all();
}
