package org.palermo.totalbattle.player;

import org.palermo.totalbattle.player.state.AutomationState;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.util.IoUtil;

import java.io.File;

public enum SharedData {

    INSTANCE;
    
    public final MyRobot robot = MyRobot.INSTANCE;

    private final File AUTOMATION_STATE_FILE = new File("automation_state.json");
    private AutomationState automationState = IoUtil.readJson(AUTOMATION_STATE_FILE, AutomationState.class);

    public AutomationState getAutomationState() {
        return this.automationState;
    }
    
    public void saveAutomationState() {
        IoUtil.writeJson(AUTOMATION_STATE_FILE, this.automationState);
    }
}
