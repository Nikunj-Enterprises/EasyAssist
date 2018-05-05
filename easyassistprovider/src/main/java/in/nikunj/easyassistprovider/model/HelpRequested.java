package in.nikunj.easyassistprovider.model;

import java.io.Serializable;


public class HelpRequested implements Serializable{
    private String helpSeekerId;
    private String acknowledgedBy;

    public String getHelpSeekerId() {
        return helpSeekerId;
    }

    public void setHelpSeekerId(String helpSeekerId) {
        this.helpSeekerId = helpSeekerId;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }
}
