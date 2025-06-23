package fun.wswj.wrench.dcc.domain.model.valobj;

/**
 * 属性值调整值对象
 */
public class AttributeVO {

    /** 键 - 属性 fileName */
    private String attribute;

    /** 值 */
    private String value;

    public AttributeVO() {
    }

    public AttributeVO(String attribute, String value) {
        this.attribute = attribute;
        this.value = value;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
