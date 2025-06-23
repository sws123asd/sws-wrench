package fun.wswj.wrench.dcc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "wswj.wrench.config", ignoreInvalidFields = true)
public class DCCAutoProperties {
    /**
     * 系统名称
     */
    private String system;

    public String getKey(String attributeName) {
        return this.system + "_" + attributeName;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }
}
