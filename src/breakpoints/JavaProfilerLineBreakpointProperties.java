package breakpoints;

import com.intellij.util.xmlb.annotations.OptionTag;
import org.jetbrains.java.debugger.breakpoints.properties.JavaBreakpointProperties;

/**
 * Created by rishajai on 10/1/16.
 */
public class JavaProfilerLineBreakpointProperties extends JavaBreakpointProperties<JavaProfilerLineBreakpointProperties> {
    private Integer myLambdaOrdinal = null;

    public JavaProfilerLineBreakpointProperties() {
    }

    @OptionTag("lambda-ordinal")
    public Integer getLambdaOrdinal() {
        return this.myLambdaOrdinal;
    }

    public void setLambdaOrdinal(Integer lambdaOrdinal) {
        this.myLambdaOrdinal = lambdaOrdinal;
    }

    public void loadState(JavaProfilerLineBreakpointProperties state) {
        super.loadState(state);
        this.myLambdaOrdinal = state.myLambdaOrdinal;
    }
}
