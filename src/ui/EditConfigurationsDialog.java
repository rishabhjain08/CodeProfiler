package ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rishajai on 9/25/16.
 */
public class EditConfigurationsDialog extends DialogWrapper {

    public enum ConnectionMode {
        ATTACH,
        LISTEN,
        UNKNOWN;

        public static ConnectionMode getConnectionMode(String modeStr) {
            if (modeStr.toLowerCase().equals("attach")) {
                return ConnectionMode.ATTACH;
            }
            else if (modeStr.toLowerCase().equals("listen")) {
                return ConnectionMode.LISTEN;
            }
            else {
                return ConnectionMode.UNKNOWN;
            }
        }
    };

    private EditConfigurationsPanel configurationsPanel;
    private String jvmFlag;
    private String remoteHost;
    private String remotePort;
    private boolean listenMode;
    private boolean attachMode;
    private boolean stateSaved;

    protected EditConfigurationsDialog(@Nullable Project project, boolean canBeParent) {
        super(project, canBeParent);
        init();
        setTitle("Edit Code Profiler Configurations...");
        stateSaved = false;
    }

    public EditConfigurationsDialog(Project project) {
        this(project, true);
    }

    @Override
    public boolean showAndGet() {
        final boolean result = super.showAndGet();
        if (!result) {
            EditConfigurationsDialog.this.restoreState();
        }
        else {
            EditConfigurationsDialog.this.saveState();
        }
        return result;
    }

    private void saveState() {
        if (configurationsPanel == null)
            return;
        jvmFlag = configurationsPanel.jvmFlagTextArea.getText();
        remoteHost = configurationsPanel.hostTextBox.getText();
        remotePort = configurationsPanel.portTextBox.getText();
        listenMode = configurationsPanel.listenRadioButton.isSelected();
        attachMode = configurationsPanel.attachRadioButton.isSelected();
        stateSaved = true;
    }

    private void restoreState() {
        if (configurationsPanel == null || !stateSaved)
            return;
        configurationsPanel.jvmFlagTextArea.setText(jvmFlag);
        configurationsPanel.hostTextBox.setText(remoteHost);
        configurationsPanel.portTextBox.setText(remotePort);
        configurationsPanel.listenRadioButton.setSelected(listenMode);
        configurationsPanel.attachRadioButton.setSelected(attachMode);
        stateSaved = false;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        configurationsPanel = new EditConfigurationsPanel();
        return configurationsPanel;
    }

    public String getJVMFlag() {
        if (configurationsPanel == null)
            return null;
        return configurationsPanel.jvmFlagTextArea.getText();
    }

    public String getRemoteHost() {
        if (configurationsPanel == null)
            return null;
        return configurationsPanel.hostTextBox.getText();
    }

    public Integer getRemotePort() {
        if (configurationsPanel == null)
            return null;
        return Integer.parseInt(configurationsPanel.portTextBox.getText());
    }

    public ConnectionMode getConnectionMode() {
        if (configurationsPanel == null)
            return null;
        String connectionModeStr = configurationsPanel.jvmFlagTextArea.getText();
        return ConnectionMode.getConnectionMode(connectionModeStr);
    }

    public Map<String, Object> getValues() {
        Map<String, Object> map = new HashMap<>();
        map.put("host", this.getRemoteHost());
        map.put("port", this.getRemotePort());
        map.put("mode", this.getConnectionMode());
        map.put("flag", this.getJVMFlag());
        return map;
    }

    public String toString() {
        String str = "{JVM flag: " + this.getJVMFlag() + ", ";
        str += "host: " + this.getRemoteHost() + ", ";
        str += "port: " + this.getRemotePort() + ", ";
        str += "mode: " + this.getConnectionMode() + "}";
        return str;
    }
}
